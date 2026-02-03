package decision

import (
	"context"
	"log"
	"time"

	"github.com/sashabaranov/go-openai"
	"github.com/skyeai/jarvis-proactive/pkg/skeleton"
)

// DecisionService 决策服务
type DecisionService struct {
	client         *openai.Client
	model          string
	temperature    float32
	maxTokens      int
	messageBus     *skeleton.MessageBus
	enabled        bool
	subscribeChan  chan skeleton.Message
}

// NewDecisionService 创建决策服务实例
func NewDecisionService(apiKey, baseURL, model string, temperature float32, maxTokens int, messageBus *skeleton.MessageBus, enabled bool) *DecisionService {
	config := openai.DefaultConfig(apiKey)
	if baseURL != "" {
		config.BaseURL = baseURL
	}

	client := openai.NewClientWithConfig(config)

	return &DecisionService{
		client:         client,
		model:          model,
		temperature:    temperature,
		maxTokens:      maxTokens,
		messageBus:     messageBus,
		enabled:        enabled,
		subscribeChan:  nil,
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
			"event_type":   eventType,
			"file_path":    filePath,
			"decision":     decision,
			"timestamp":    time.Now(),
		},
	}

	ds.messageBus.SendMessage(decisionMessage)
	log.Printf("Decision made: %s for %s on %s", decision, eventType, filePath)
}

// makeDecision 做出决策
func (ds *DecisionService) makeDecision(eventType, filePath string) string {
	// 构建决策提示
	prompt := ds.buildDecisionPrompt(eventType, filePath)

	// 调用AI模型
	response, err := ds.callAI(prompt)
	if err != nil {
		log.Printf("Error calling AI: %v", err)
		return "monitor"
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

// callAI 调用AI模型
func (ds *DecisionService) callAI(prompt string) (string, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	resp, err := ds.client.CreateChatCompletion(ctx, openai.ChatCompletionRequest{
		Model:     ds.model,
		Temperature: ds.temperature,
		MaxTokens:   ds.maxTokens,
		Messages: []openai.ChatCompletionMessage{
			{
				Role:    openai.ChatMessageRoleUser,
				Content: prompt,
			},
		},
	})

	if err != nil {
		return "", err
	}

	return resp.Choices[0].Message.Content, nil
}
