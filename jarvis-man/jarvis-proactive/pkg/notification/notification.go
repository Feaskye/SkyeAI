package notification

import (
	"fmt"
	"log"
	"time"

	"github.com/skyeai/jarvis-proactive/pkg/mq"
	"github.com/skyeai/jarvis-proactive/pkg/skeleton"
)

// NotificationConfig 通知服务配置

type NotificationConfig struct {
	Enabled bool
}

// FrontendMessage 前端消息

type FrontendMessage struct {
	Type      string                 `json:"type"`
	Title     string                 `json:"title"`
	Content   string                 `json:"content"`
	Level     string                 `json:"level"` // info, warning, error, success
	Timestamp time.Time              `json:"timestamp"`
	Data      map[string]interface{} `json:"data,omitempty"`
}

// NotificationService 通知服务

type NotificationService struct {
	messageBus     *skeleton.MessageBus
	rabbitMQClient *mq.RabbitMQClient
	enabled        bool
	subscribeChan  chan skeleton.Message
}

// NewNotificationService 创建通知服务实例

func NewNotificationService(messageBus *skeleton.MessageBus, rabbitMQClient *mq.RabbitMQClient, enabled bool) *NotificationService {
	return &NotificationService{
		messageBus:     messageBus,
		rabbitMQClient: rabbitMQClient,
		enabled:        enabled,
		subscribeChan:  nil,
	}
}

// Start 启动通知服务

func (ns *NotificationService) Start() {
	if !ns.enabled {
		log.Println("Notification service is disabled")
		return
	}

	log.Println("Starting notification service...")

	// 订阅决策结果消息
	ns.subscribeChan = ns.messageBus.Subscribe("decision")

	// 启动消息处理
	go ns.processMessages()

	log.Println("Notification service started")
}

// Stop 停止通知服务

func (ns *NotificationService) Stop() {
	if !ns.enabled {
		return
	}

	log.Println("Stopping notification service...")

	// 取消订阅
	if ns.subscribeChan != nil {
		ns.messageBus.Unsubscribe("decision", ns.subscribeChan)
	}

	log.Println("Notification service stopped")
}

// processMessages 处理消息

func (ns *NotificationService) processMessages() {
	for message := range ns.subscribeChan {
		ns.handleMessage(message)
	}
}

// handleMessage 处理消息

func (ns *NotificationService) handleMessage(message skeleton.Message) {
	log.Printf("Notification service received message: %s from %s", message.Type, message.Source)

	// 分析决策消息
	if message.Type == "decision" {
		ns.handleDecisionMessage(message)
	}
}

// handleDecisionMessage 处理决策消息

func (ns *NotificationService) handleDecisionMessage(message skeleton.Message) {
	// 提取决策信息
	eventType, ok := message.Data["event_type"].(string)
	if !ok {
		log.Println("Invalid event type in decision message")
		return
	}

	filePath, ok := message.Data["file_path"].(string)
	if !ok {
		log.Println("Invalid file path in decision message")
		return
	}

	decision, ok := message.Data["decision"].(string)
	if !ok {
		log.Println("Invalid decision in decision message")
		return
	}

	// 构建前端消息
	frontendMsg := ns.buildFrontendMessage(eventType, filePath, decision)

	// 发送到RabbitMQ
	if ns.rabbitMQClient != nil && ns.rabbitMQClient.IsConnected() {
		if err := ns.rabbitMQClient.Publish(frontendMsg); err != nil {
			log.Printf("Error publishing frontend message: %v", err)
		} else {
			log.Printf("Frontend message published: %s", frontendMsg.Type)
		}
	} else {
		log.Println("RabbitMQ not connected, frontend message not published")
	}
}

// buildFrontendMessage 构建前端消息

func (ns *NotificationService) buildFrontendMessage(eventType, filePath, decision string) FrontendMessage {
	// 根据决策类型设置消息级别
	level := "info"
	switch decision {
	case "execute":
		level = "success"
	case "notify":
		level = "warning"
	case "analyze":
		level = "info"
	case "monitor":
		level = "info"
	case "ignore":
		level = "info"
	default:
		level = "info"
	}

	// 构建消息内容
	title := "文件系统事件处理"
	content := ns.buildMessageContent(eventType, filePath, decision)

	// 构建消息数据
	data := map[string]interface{}{
		"event_type": eventType,
		"file_path":  filePath,
		"decision":   decision,
	}

	return FrontendMessage{
		Type:      "decision",
		Title:     title,
		Content:   content,
		Level:     level,
		Timestamp: time.Now(),
		Data:      data,
	}
}

// buildMessageContent 构建消息内容

func (ns *NotificationService) buildMessageContent(eventType, filePath, decision string) string {
	switch decision {
	case "execute":
		return fmt.Sprintf("对文件 %s 的 %s 事件执行了操作", filePath, eventType)
	case "notify":
		return fmt.Sprintf("文件 %s 发生了 %s 事件，需要您的关注", filePath, eventType)
	case "analyze":
		return fmt.Sprintf("正在分析文件 %s 的 %s 事件", filePath, eventType)
	case "monitor":
		return fmt.Sprintf("正在监控文件 %s 的 %s 事件", filePath, eventType)
	case "ignore":
		return fmt.Sprintf("忽略了文件 %s 的 %s 事件", filePath, eventType)
	default:
		return fmt.Sprintf("文件 %s 发生了 %s 事件，处理决策：%s", filePath, eventType, decision)
	}
}
