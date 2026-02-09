package mq

import (
	"encoding/json"
	"fmt"
	"log"
	"sync"
	"time"

	"github.com/streadway/amqp"
)

// RabbitMQConfig RabbitMQ配置

type RabbitMQConfig struct {
	Host         string
	Port         int
	Username     string
	Password     string
	VirtualHost  string
	Exchange     string
	ExchangeType string
	QueueName    string
	RoutingKey   string
}

// RabbitMQClient RabbitMQ客户端

type RabbitMQClient struct {
	config      RabbitMQConfig
	conn        *amqp.Connection
	channel     *amqp.Channel
	mutex       sync.RWMutex
	isConnected bool
}

// NewRabbitMQClient 创建RabbitMQ客户端实例

func NewRabbitMQClient(config RabbitMQConfig) *RabbitMQClient {
	return &RabbitMQClient{
		config:      config,
		isConnected: false,
	}
}

// Connect 连接RabbitMQ

func (r *RabbitMQClient) Connect() error {
	r.mutex.Lock()
	defer r.mutex.Unlock()

	if r.isConnected {
		return nil
	}

	addr := fmt.Sprintf("amqp://%s:%s@%s:%d/%s",
		r.config.Username,
		r.config.Password,
		r.config.Host,
		r.config.Port,
		r.config.VirtualHost,
	)

	var err error
	r.conn, err = amqp.Dial(addr)
	if err != nil {
		return fmt.Errorf("failed to connect to rabbitmq: %w", err)
	}

	r.channel, err = r.conn.Channel()
	if err != nil {
		r.conn.Close()
		return fmt.Errorf("failed to open channel: %w", err)
	}

	// 声明交换机
	err = r.channel.ExchangeDeclare(
		r.config.Exchange,
		r.config.ExchangeType,
		true,  // 持久化
		false, // 自动删除
		false, // 内部
		false, // 非阻塞
		nil,   // 参数
	)
	if err != nil {
		r.channel.Close()
		r.conn.Close()
		return fmt.Errorf("failed to declare exchange: %w", err)
	}

	// 声明队列
	_, err = r.channel.QueueDeclare(
		r.config.QueueName,
		true,  // 持久化
		false, // 自动删除
		false, // 排他
		false, // 非阻塞
		nil,   // 参数
	)
	if err != nil {
		r.channel.Close()
		r.conn.Close()
		return fmt.Errorf("failed to declare queue: %w", err)
	}

	// 绑定队列到交换机
	err = r.channel.QueueBind(
		r.config.QueueName,
		r.config.RoutingKey,
		r.config.Exchange,
		false,
		nil,
	)
	if err != nil {
		r.channel.Close()
		r.conn.Close()
		return fmt.Errorf("failed to bind queue: %w", err)
	}

	r.isConnected = true
	log.Println("RabbitMQ connected successfully")
	return nil
}

// Close 关闭RabbitMQ连接

func (r *RabbitMQClient) Close() error {
	r.mutex.Lock()
	defer r.mutex.Unlock()

	if !r.isConnected {
		return nil
	}

	if r.channel != nil {
		if err := r.channel.Close(); err != nil {
			log.Printf("Error closing channel: %v", err)
		}
	}

	if r.conn != nil {
		if err := r.conn.Close(); err != nil {
			log.Printf("Error closing connection: %v", err)
		}
	}

	r.isConnected = false
	log.Println("RabbitMQ connection closed")
	return nil
}

// Publish 发布消息

func (r *RabbitMQClient) Publish(message interface{}) error {
	r.mutex.RLock()
	defer r.mutex.RUnlock()

	if !r.isConnected {
		return fmt.Errorf("rabbitmq not connected")
	}

	// 序列化消息
	msgBytes, err := json.Marshal(message)
	if err != nil {
		return fmt.Errorf("failed to marshal message: %w", err)
	}

	// 发布消息
	err = r.channel.Publish(
		r.config.Exchange,
		r.config.RoutingKey,
		false, // 强制
		false, // 立即
		amqp.Publishing{
			ContentType:  "application/json",
			Body:         msgBytes,
			Timestamp:    time.Now(),
			DeliveryMode: amqp.Persistent, // 持久化
		},
	)
	if err != nil {
		return fmt.Errorf("failed to publish message: %w", err)
	}

	log.Printf("Message published to RabbitMQ: %s", string(msgBytes))
	return nil
}

// Consume 消费消息

func (r *RabbitMQClient) Consume() (<-chan amqp.Delivery, error) {
	r.mutex.RLock()
	defer r.mutex.RUnlock()

	if !r.isConnected {
		return nil, fmt.Errorf("rabbitmq not connected")
	}

	// 消费消息
	msgs, err := r.channel.Consume(
		r.config.QueueName,
		"",    // 消费者标签
		false, // 自动确认
		false, // 排他
		false, // 非本地
		false, // 非阻塞
		nil,   // 参数
	)
	if err != nil {
		return nil, fmt.Errorf("failed to register consumer: %w", err)
	}

	return msgs, nil
}

// IsConnected 检查是否连接

func (r *RabbitMQClient) IsConnected() bool {
	r.mutex.RLock()
	defer r.mutex.RUnlock()
	return r.isConnected
}

// Reconnect 重连RabbitMQ

func (r *RabbitMQClient) Reconnect() error {
	log.Println("Attempting to reconnect to RabbitMQ...")

	// 关闭现有连接
	if err := r.Close(); err != nil {
		log.Printf("Error closing existing connection: %v", err)
	}

	// 尝试重连
	var lastErr error
	for i := 0; i < 5; i++ {
		err := r.Connect()
		if err == nil {
			log.Println("RabbitMQ reconnected successfully")
			return nil
		}

		lastErr = err
		log.Printf("Reconnection attempt %d failed: %v", i+1, err)
		time.Sleep(time.Duration(i+1) * time.Second)
	}

	return fmt.Errorf("failed to reconnect after 5 attempts: %w", lastErr)
}
