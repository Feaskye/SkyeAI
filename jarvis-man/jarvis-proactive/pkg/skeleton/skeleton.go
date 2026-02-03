package skeleton

import (
	"log"
	"sync"
	"time"
)

// Message 消息结构
type Message struct {
	Type      string                 `json:"type"`
	Source    string                 `json:"source"`
	Timestamp time.Time              `json:"timestamp"`
	Data      map[string]interface{} `json:"data"`
}

// MessageBus 消息总线
type MessageBus struct {
	messageChan   chan Message
	subscribers   map[string][]chan Message
	subscribersMu sync.RWMutex
	bufferSize    int
	stopChan      chan struct{}
	wg            sync.WaitGroup
}

// NewMessageBus 创建消息总线实例
func NewMessageBus(bufferSize int) *MessageBus {
	return &MessageBus{
		messageChan: make(chan Message, bufferSize),
		subscribers: make(map[string][]chan Message),
		bufferSize:  bufferSize,
		stopChan:    make(chan struct{}),
	}
}

// Start 启动消息总线
func (mb *MessageBus) Start() {
	mb.wg.Add(1)
	go mb.processMessages()
	log.Println("Message bus started")
}

// Stop 停止消息总线
func (mb *MessageBus) Stop() {
	close(mb.stopChan)
	mb.wg.Wait()
	log.Println("Message bus stopped")
}

// SendMessage 发送消息
func (mb *MessageBus) SendMessage(message Message) {
	select {
	case mb.messageChan <- message:
		// 消息发送成功
	default:
		// 消息通道已满，丢弃消息
		log.Println("Message bus buffer full, message discarded")
	}
}

// Subscribe 订阅消息
func (mb *MessageBus) Subscribe(messageType string) chan Message {
	mb.subscribersMu.Lock()
	defer mb.subscribersMu.Unlock()

	// 创建订阅者通道
	subChan := make(chan Message, mb.bufferSize)

	// 添加到订阅者列表
	if _, exists := mb.subscribers[messageType]; !exists {
		mb.subscribers[messageType] = []chan Message{}
	}
	mb.subscribers[messageType] = append(mb.subscribers[messageType], subChan)

	return subChan
}

// Unsubscribe 取消订阅
func (mb *MessageBus) Unsubscribe(messageType string, subChan chan Message) {
	mb.subscribersMu.Lock()
	defer mb.subscribersMu.Unlock()

	if subs, exists := mb.subscribers[messageType]; exists {
		for i, ch := range subs {
			if ch == subChan {
				// 移除订阅者
				mb.subscribers[messageType] = append(subs[:i], subs[i+1:]...)
				close(subChan)
				break
			}
		}
	}
}

// processMessages 处理消息
func (mb *MessageBus) processMessages() {
	defer mb.wg.Done()

	for {
		select {
		case message := <-mb.messageChan:
			mb.dispatchMessage(message)
		case <-mb.stopChan:
			return
		}
	}
}

// dispatchMessage 分发消息
func (mb *MessageBus) dispatchMessage(message Message) {
	mb.subscribersMu.RLock()
	defer mb.subscribersMu.RUnlock()

	// 分发到指定类型的订阅者
	if subs, exists := mb.subscribers[message.Type]; exists {
		for _, subChan := range subs {
			select {
			case subChan <- message:
				// 消息发送成功
			default:
				// 订阅者通道已满，丢弃消息
				log.Printf("Subscriber buffer full for message type %s, message discarded", message.Type)
			}
		}
	}

	// 分发到所有类型的订阅者（如果有）
	if subs, exists := mb.subscribers["*"]; exists {
		for _, subChan := range subs {
			select {
			case subChan <- message:
				// 消息发送成功
			default:
				// 订阅者通道已满，丢弃消息
				log.Println("Wildcard subscriber buffer full, message discarded")
			}
		}
	}
}

// ServiceManager 服务管理器
type ServiceManager struct {
	services []Service
	wg       sync.WaitGroup
	stopChan chan struct{}
}

// Service 服务接口
type Service interface {
	Start()
	Stop()
}

// NewServiceManager 创建服务管理器实例
func NewServiceManager() *ServiceManager {
	return &ServiceManager{
		services: []Service{},
		stopChan: make(chan struct{}),
	}
}

// AddService 添加服务
func (sm *ServiceManager) AddService(service Service) {
	sm.services = append(sm.services, service)
}

// StartAll 启动所有服务
func (sm *ServiceManager) StartAll() {
	for _, service := range sm.services {
		sm.wg.Add(1)
		go func(s Service) {
			defer sm.wg.Done()
			s.Start()
		}(service)
	}
	log.Println("All services started")
}

// StopAll 停止所有服务
func (sm *ServiceManager) StopAll() {
	close(sm.stopChan)
	for _, service := range sm.services {
		service.Stop()
	}
	sm.wg.Wait()
	log.Println("All services stopped")
}
