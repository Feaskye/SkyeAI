package mqtt

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"time"

	"github.com/eclipse/paho.mqtt.golang"
)

// Client MQTT客户端接口
type Client interface {
	// Connect 连接到MQTT服务器
	Connect() error
	// Disconnect 断开MQTT连接
	Disconnect()
	// Publish 发布消息到指定主题
	Publish(ctx context.Context, topic string, payload interface{}, qos int) error
	// Subscribe 订阅指定主题
	Subscribe(topic string, qos int, callback MessageHandler) error
	// Unsubscribe 取消订阅指定主题
	Unsubscribe(topic string) error
	// IsConnected 检查客户端是否已连接
	IsConnected() bool
	// GetClientID 获取客户端ID
	GetClientID() string
}

// MessageHandler 消息处理函数类型
type MessageHandler func(ctx context.Context, topic string, payload []byte) error

// Config MQTT客户端配置
type Config struct {
	BrokerURL      string        `json:"broker_url"`
	ClientID       string        `json:"client_id"`
	Username       string        `json:"username"`
	Password       string        `json:"password"`
	QoS            int           `json:"qos"`
	KeepAlive      time.Duration `json:"keep_alive"`
	ConnectTimeout time.Duration `json:"connect_timeout"`
	AutoReconnect  bool          `json:"auto_reconnect"`
	CleanSession   bool          `json:"clean_session"`
}

// client MQTT客户端实现
type client struct {
	config     Config
	mqttClient mqtt.Client
	clientID   string
}

// NewClient 创建新的MQTT客户端
func NewClient(config Config) Client {
	if config.ClientID == "" {
		config.ClientID = fmt.Sprintf("jarvis-edge-%d", time.Now().UnixNano())
	}

	if config.KeepAlive == 0 {
		config.KeepAlive = 30 * time.Second
	}

	if config.ConnectTimeout == 0 {
		config.ConnectTimeout = 10 * time.Second
	}

	if config.QoS < 0 || config.QoS > 2 {
		config.QoS = 1
	}

	return &client{
		config:   config,
		clientID: config.ClientID,
	}
}

// Connect 连接到MQTT服务器
func (c *client) Connect() error {
	opts := mqtt.NewClientOptions()
	opts.AddBroker(c.config.BrokerURL)
	opts.SetClientID(c.config.ClientID)
	opts.SetUsername(c.config.Username)
	opts.SetPassword(c.config.Password)
	opts.SetKeepAlive(c.config.KeepAlive)
	opts.SetConnectTimeout(c.config.ConnectTimeout)
	opts.SetAutoReconnect(c.config.AutoReconnect)
	opts.SetCleanSession(c.config.CleanSession)

	// 设置连接回调
	opts.OnConnect = func(client mqtt.Client) {
		log.Printf("MQTT client connected: %s", c.clientID)
	}

	// 设置断开连接回调
	opts.OnConnectionLost = func(client mqtt.Client, err error) {
		log.Printf("MQTT connection lost: %s, error: %v", c.clientID, err)
	}

	// 创建客户端
	c.mqttClient = mqtt.NewClient(opts)

	// 连接到MQTT服务器
	ctx, cancel := context.WithTimeout(context.Background(), c.config.ConnectTimeout)
	defer cancel()

	token := c.mqttClient.Connect()
	select {
	case <-token.Done():
		if token.Error() != nil {
			return fmt.Errorf("MQTT connect failed: %w", token.Error())
		}
	case <-ctx.Done():
		return fmt.Errorf("MQTT connect timed out: %w", ctx.Err())
	}

	log.Printf("MQTT client connected successfully: %s", c.clientID)
	return nil
}

// Disconnect 断开MQTT连接
func (c *client) Disconnect() {
	if c.mqttClient != nil && c.mqttClient.IsConnected() {
		c.mqttClient.Disconnect(250)
		log.Printf("MQTT client disconnected: %s", c.clientID)
	}
}

// Publish 发布消息到指定主题
func (c *client) Publish(ctx context.Context, topic string, payload interface{}, qos int) error {
	if !c.IsConnected() {
		return fmt.Errorf("MQTT client is not connected")
	}

	// 序列化消息
	var payloadBytes []byte
	var err error

	switch v := payload.(type) {
	case []byte:
		payloadBytes = v
	case string:
		payloadBytes = []byte(v)
	default:
		payloadBytes, err = json.Marshal(v)
		if err != nil {
			return fmt.Errorf("failed to marshal payload: %w", err)
		}
	}

	// 确保QoS值在有效范围内
	if qos < 0 || qos > 2 {
		qos = c.config.QoS
	}

	// 发布消息
	token := c.mqttClient.Publish(topic, byte(qos), false, payloadBytes)

	select {
	case <-token.Done():
		if token.Error() != nil {
			return fmt.Errorf("MQTT publish failed: %w", token.Error())
		}
		log.Printf("MQTT message published: topic=%s, qos=%d", topic, qos)
		return nil
	case <-ctx.Done():
		return fmt.Errorf("MQTT publish timed out: %w", ctx.Err())
	}
}

// Subscribe 订阅指定主题
func (c *client) Subscribe(topic string, qos int, callback MessageHandler) error {
	if !c.IsConnected() {
		return fmt.Errorf("MQTT client is not connected")
	}

	// 确保QoS值在有效范围内
	if qos < 0 || qos > 2 {
		qos = c.config.QoS
	}

	// 订阅主题
	token := c.mqttClient.Subscribe(topic, byte(qos), func(client mqtt.Client, msg mqtt.Message) {
		ctx := context.Background()
		if err := callback(ctx, msg.Topic(), msg.Payload()); err != nil {
			log.Printf("Error handling MQTT message: topic=%s, error: %v", msg.Topic(), err)
		}
	})

	if token.Wait() && token.Error() != nil {
		return fmt.Errorf("MQTT subscribe failed: %w", token.Error())
	}

	log.Printf("MQTT topic subscribed: %s, qos=%d", topic, qos)
	return nil
}

// Unsubscribe 取消订阅指定主题
func (c *client) Unsubscribe(topic string) error {
	if !c.IsConnected() {
		return fmt.Errorf("MQTT client is not connected")
	}

	token := c.mqttClient.Unsubscribe(topic)
	if token.Wait() && token.Error() != nil {
		return fmt.Errorf("MQTT unsubscribe failed: %w", token.Error())
	}

	log.Printf("MQTT topic unsubscribed: %s", topic)
	return nil
}

// IsConnected 检查客户端是否已连接
func (c *client) IsConnected() bool {
	return c.mqttClient != nil && c.mqttClient.IsConnected()
}

// GetClientID 获取客户端ID
func (c *client) GetClientID() string {
	return c.clientID
}

// HealthData 健康数据消息结构
type HealthData struct {
	DataType   string    `json:"data_type"`   // 数据类型：heart_rate, sleep_quality, steps等
	Value      float64   `json:"value"`       // 数据值
	Unit       string    `json:"unit"`        // 数据单位
	Timestamp  time.Time `json:"timestamp"`   // 数据时间戳
	Source     string    `json:"source"`      // 数据来源
	UserID     string    `json:"user_id"`     // 用户ID
}

// IoTCommand IoT设备命令消息结构
type IoTCommand struct {
	CommandID  string                 `json:"command_id"`  // 命令ID
	DeviceID   string                 `json:"device_id"`   // 设备ID
	Command    string                 `json:"command"`     // 命令类型
	Parameters map[string]interface{} `json:"parameters"`  // 命令参数
	Timestamp  time.Time              `json:"timestamp"`   // 命令时间戳
	UserID     string                 `json:"user_id"`     // 用户ID
}

// IoTStatus IoT设备状态消息结构
type IoTStatus struct {
	DeviceID   string                 `json:"device_id"`   // 设备ID
	Status     string                 `json:"status"`      // 设备状态
	Properties map[string]interface{} `json:"properties"`  // 设备属性
	Timestamp  time.Time              `json:"timestamp"`   // 状态时间戳
	Source     string                 `json:"source"`      // 状态来源
}

// Topic 主题常量定义
const (
	// 健康数据主题前缀
	TopicHealthDataPrefix = "jarvis/health/"
	// IoT命令主题前缀
	TopicIoTCommandPrefix = "jarvis/iot/command/"
	// IoT状态主题前缀
	TopicIoTStatusPrefix = "jarvis/iot/status/"
	// 全局广播主题
	TopicBroadcast = "jarvis/broadcast"
)

// GetHealthDataTopic 获取健康数据主题
func GetHealthDataTopic(userID string) string {
	return fmt.Sprintf("%s%s", TopicHealthDataPrefix, userID)
}

// GetIoTCommandTopic 获取IoT设备命令主题
func GetIoTCommandTopic(deviceID string) string {
	return fmt.Sprintf("%s%s", TopicIoTCommandPrefix, deviceID)
}

// GetIoTStatusTopic 获取IoT设备状态主题
func GetIoTStatusTopic(deviceID string) string {
	return fmt.Sprintf("%s%s", TopicIoTStatusPrefix, deviceID)
}
