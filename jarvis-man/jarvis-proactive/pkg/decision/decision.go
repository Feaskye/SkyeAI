package decision

import (
	"fmt"
	"log"
	"time"

	"github.com/skyeai/jarvis-proactive/pkg/cache"
	"github.com/skyeai/jarvis-proactive/pkg/llm"
	"github.com/skyeai/jarvis-proactive/pkg/skeleton"
)

// DecisionService 决策服务

type DecisionService struct {
	llmClient          *llm.LLMClient
	redisClient        *cache.RedisClient
	model              string
	temperature        float32
	maxTokens          int
	messageBus         *skeleton.MessageBus
	enabled            bool
	subscribeChan      chan skeleton.Message
	maxIntervalMinutes int // 最大交互间隔（分钟）
}

// NewDecisionService 创建决策服务实例

func NewDecisionService(llmClient *llm.LLMClient, redisClient *cache.RedisClient, model string, temperature float32, maxTokens int, messageBus *skeleton.MessageBus, enabled bool, maxIntervalMinutes int) *DecisionService {
	return &DecisionService{
		llmClient:          llmClient,
		redisClient:        redisClient,
		model:              model,
		temperature:        temperature,
		maxTokens:          maxTokens,
		messageBus:         messageBus,
		enabled:            enabled,
		subscribeChan:      nil,
		maxIntervalMinutes: maxIntervalMinutes,
	}
}

// Start 启动决策服务
func (ds *DecisionService) Start() {
	if !ds.enabled {
		log.Println("Decision service is disabled")
		return
	}

	log.Println("Starting decision service...")

	// 订阅感知事件
	ds.subscribeChan = ds.messageBus.Subscribe("file_event")

	// 启动消息处理
	go ds.processMessages()

	log.Println("Decision service started")
}

// Stop 停止决策服务
func (ds *DecisionService) Stop() {
	if !ds.enabled {
		return
	}

	log.Println("Stopping decision service...")

	// 取消订阅
	if ds.subscribeChan != nil {
		ds.messageBus.Unsubscribe("file_event", ds.subscribeChan)
	}

	log.Println("Decision service stopped")
}

// processMessages 处理消息
func (ds *DecisionService) processMessages() {
	for message := range ds.subscribeChan {
		ds.handleMessage(message)
	}
}

// handleMessage 处理消息
func (ds *DecisionService) handleMessage(message skeleton.Message) {
	log.Printf("Decision service received message: %s from %s", message.Type, message.Source)

	// 分析事件
	eventType, ok := message.Data["event_type"].(string)
	if !ok {
		log.Println("Invalid event type in message")
		return
	}

	filePath, ok := message.Data["file_path"].(string)
	if !ok {
		log.Println("Invalid file path in message")
		return
	}

	// 生成决策
	decision := ds.makeDecision(eventType, filePath)

	// 发送决策结果
	decisionMessage := skeleton.Message{
		Type:      "decision",
		Source:    "decision",
		Timestamp: time.Now(),
		Data: map[string]interface{}{
			"event_type": eventType,
			"file_path":  filePath,
			"decision":   decision,
			"timestamp":  time.Now(),
		},
	}

	ds.messageBus.SendMessage(decisionMessage)
	log.Printf("Decision made: %s for %s on %s", decision, eventType, filePath)
}

// makeDecision 做出决策

func (ds *DecisionService) makeDecision(eventType, filePath string) string {
	// 构建缓存键
	cacheKey := fmt.Sprintf("decision:%s:%s", eventType, filePath)

	// 1. 检查频率限制
	if ds.redisClient != nil && ds.redisClient.IsConnected() {
		allowed, err := ds.redisClient.CheckDecisionRateLimit(ds.maxIntervalMinutes)
		if err != nil {
			log.Printf("Error checking rate limit: %v", err)
		} else if !allowed {
			// 超过频率限制，尝试从缓存获取
			var cachedDecision string
			err := ds.redisClient.Get(cacheKey, &cachedDecision)
			if err == nil {
				log.Printf("Rate limit exceeded, using cached decision: %s", cachedDecision)
				return cachedDecision
			}
			log.Println("Rate limit exceeded, no cached decision available")
			return "monitor"
		}
	}

	// 2. 检查缓存
	if ds.redisClient != nil && ds.redisClient.IsConnected() {
		var cachedDecision string
		err := ds.redisClient.Get(cacheKey, &cachedDecision)
		if err == nil {
			log.Printf("Using cached decision: %s", cachedDecision)
			return cachedDecision
		}
	}

	// 3. 构建决策提示
	prompt := ds.buildDecisionPrompt(eventType, filePath)

	// 4. 调用LLM服务
	response, err := ds.callLLM(prompt)
	if err != nil {
		log.Printf("Error calling LLM: %v", err)
		return "monitor"
	}

	// 5. 缓存决策结果
	if ds.redisClient != nil && ds.redisClient.IsConnected() {
		err := ds.redisClient.Set(cacheKey, response, 10*time.Minute)
		if err != nil {
			log.Printf("Error caching decision: %v", err)
		}
	}

	return response
}

// buildDecisionPrompt 构建决策提示
func (ds *DecisionService) buildDecisionPrompt(eventType, filePath string) string {
	return `You are a proactive AI assistant that makes decisions based on file system events.

Current event:
- Type: ` + eventType + `
- File: ` + filePath + `

Available decisions:
1. monitor: Simply monitor the event without taking action
2. analyze: Analyze the file content to understand its importance
3. notify: Send a notification about the event
4. execute: Execute a specific action related to the event
5. ignore: Ignore the event completely

Based on the event type and file path, what is the most appropriate decision?

Please respond with only the decision keyword (e.g., "monitor", "analyze", etc.).`
}

// callLLM 调用LLM服务

func (ds *DecisionService) callLLM(prompt string) (string, error) {
	if ds.llmClient == nil || !ds.llmClient.IsConnected() {
		return "", fmt.Errorf("llm client not connected")
	}

	// 构建LLM请求
	request := llm.GenerateRequest{
		Prompt:      prompt,
		Model:       ds.model,
		Temperature: ds.temperature,
		MaxTokens:   ds.maxTokens,
	}

	// 调用LLM服务
	response, err := ds.llmClient.Generate(request)
	if err != nil {
		return "", err
	}

	if response.Error != "" {
		return "", fmt.Errorf("llm service error: %s", response.Error)
	}

	return response.Text, nil
}
