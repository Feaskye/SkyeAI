package notification

import (
	"encoding/json"
	"fmt"
	"log"
	"time"

	"github.com/spf13/viper"
	"jarvis-finance-go/pkg/model"

	"github.com/rabbitmq/amqp091-go"
)

// NotificationService 通知服务
type NotificationService struct {
	rabbitMQConn *amqp091.Connection
	rabbitMQChan *amqp091.Channel
	exchangeName string
	queueName    string
}

// NewNotificationService 创建通知服务实例
func NewNotificationService() *NotificationService {
	service := &NotificationService{
		exchangeName: "jarvis.notifications",
		queueName:    "jarvis.finance.notifications",
	}

	// 初始化RabbitMQ连接
	if err := service.initRabbitMQ(); err != nil {
		log.Printf("Failed to initialize RabbitMQ: %v", err)
		// 继续运行，后续会重试
	}

	return service
}

// Start 启动通知服务
func (n *NotificationService) Start() {
	log.Println("NotificationService started")
	// 通知服务会在其他模块需要时被调用，不需要单独的定时任务
}

// initRabbitMQ 初始化RabbitMQ连接
func (n *NotificationService) initRabbitMQ() error {
	// 从配置中获取RabbitMQ连接信息
	host := viper.GetString("rabbitmq.host")
	port := viper.GetInt("rabbitmq.port")
	user := viper.GetString("rabbitmq.username")
	pass := viper.GetString("rabbitmq.password")
	vhost := viper.GetString("rabbitmq.vhost")

	// 构建连接字符串
	connStr := fmt.Sprintf("amqp://%s:%s@%s:%d/%s", user, pass, host, port, vhost)

	// 连接到RabbitMQ
	conn, err := amqp091.Dial(connStr)
	if err != nil {
		return fmt.Errorf("failed to connect to RabbitMQ: %v", err)
	}

	// 创建通道
	ch, err := conn.Channel()
	if err != nil {
		conn.Close()
		return fmt.Errorf("failed to create channel: %v", err)
	}

	// 声明交换机
	err = ch.ExchangeDeclare(
		n.exchangeName,
		"topic",
		true,
		false,
		false,
		false,
		nil,
	)
	if err != nil {
		ch.Close()
		conn.Close()
		return fmt.Errorf("failed to declare exchange: %v", err)
	}

	// 声明队列
	_, err = ch.QueueDeclare(
		n.queueName,
		true,
		false,
		false,
		false,
		nil,
	)
	if err != nil {
		ch.Close()
		conn.Close()
		return fmt.Errorf("failed to declare queue: %v", err)
	}

	// 绑定队列到交换机
	err = ch.QueueBind(
		n.queueName,
		"finance.*",
		n.exchangeName,
		false,
		nil,
	)
	if err != nil {
		ch.Close()
		conn.Close()
		return fmt.Errorf("failed to bind queue: %v", err)
	}

	n.rabbitMQConn = conn
	n.rabbitMQChan = ch

	log.Println("RabbitMQ initialized successfully")
	return nil
}

// notify 通过RabbitMQ发送通知
func (n *NotificationService) notify(routingKey string, message interface{}) error {
	// 检查RabbitMQ连接是否正常
	if n.rabbitMQConn == nil || n.rabbitMQChan == nil {
		if err := n.initRabbitMQ(); err != nil {
			return fmt.Errorf("failed to reconnect to RabbitMQ: %v", err)
		}
	}

	// 序列化消息
	data, err := json.Marshal(message)
	if err != nil {
		return fmt.Errorf("failed to marshal message: %v", err)
	}

	// 发送消息
	err = n.rabbitMQChan.Publish(
		n.exchangeName,
		routingKey,
		false,
		false,
		amqp091.Publishing{
			ContentType:  "application/json",
			Body:         data,
			Timestamp:    time.Now(),
			DeliveryMode: amqp091.Persistent,
		},
	)
	if err != nil {
		// 重置连接，下次会重试
		n.rabbitMQChan = nil
		n.rabbitMQConn = nil
		return fmt.Errorf("failed to publish message: %v", err)
	}

	log.Printf("Notification sent: %s", routingKey)
	return nil
}

// NotifyStockDecision 通知股票决策
func (n *NotificationService) NotifyStockDecision(stockCode string, decision *model.DecisionResult) error {
	notification := model.Notification{
		Type:       "STOCK_DECISION",
		StockCode:  stockCode,
		Title:      fmt.Sprintf("股票决策: %s", stockCode),
		Content:    decision.Reason,
		Action:     decision.Action,
		Confidence: decision.Confidence,
		Timestamp:  time.Now(),
		Decision:   decision,
	}

	return n.notify(fmt.Sprintf("finance.stock.%s", stockCode), notification)
}

// NotifyMarketDecision 通知大盘决策
func (n *NotificationService) NotifyMarketDecision(decision *model.MarketDecision) error {
	notification := model.Notification{
		Type:           "MARKET_DECISION",
		Title:          "大盘决策",
		Content:        decision.Reason,
		Action:         decision.Recommendation,
		Confidence:     decision.Confidence,
		Timestamp:      time.Now(),
		MarketDecision: decision,
	}

	return n.notify("finance.market", notification)
}

// NotifyStockAlert 通知股票预警
func (n *NotificationService) NotifyStockAlert(stockCode string, alertType string, message string) error {
	notification := model.Notification{
		Type:      "STOCK_ALERT",
		StockCode: stockCode,
		Title:     fmt.Sprintf("股票预警: %s", stockCode),
		Content:   message,
		AlertType: alertType,
		Timestamp: time.Now(),
	}

	return n.notify(fmt.Sprintf("finance.alert.%s", stockCode), notification)
}

// NotifyMarketAlert 通知大盘预警
func (n *NotificationService) NotifyMarketAlert(alertType string, message string) error {
	notification := model.Notification{
		Type:      "MARKET_ALERT",
		Title:     "大盘预警",
		Content:   message,
		AlertType: alertType,
		Timestamp: time.Now(),
	}

	return n.notify("finance.alert.market", notification)
}

// NotifySectorAlert 通知版块预警
func (n *NotificationService) NotifySectorAlert(sectorName string, alertType string, message string) error {
	notification := model.Notification{
		Type:       "SECTOR_ALERT",
		SectorName: sectorName,
		Title:      fmt.Sprintf("版块预警: %s", sectorName),
		Content:    message,
		AlertType:  alertType,
		Timestamp:  time.Now(),
	}

	return n.notify(fmt.Sprintf("finance.alert.sector.%s", sectorName), notification)
}

// NotifyBacktestResult 通知回测结果
func (n *NotificationService) NotifyBacktestResult(result *model.BacktestResult) error {
	notification := model.Notification{
		Type:           "BACKTEST_RESULT",
		StockCode:      result.StockCode,
		Title:          fmt.Sprintf("回测结果: %s - %s", result.StockCode, result.IndicatorType),
		Content:        fmt.Sprintf("成功率: %.2f%%, 总交易: %d, 盈利交易: %d", result.SuccessRate*100, result.TotalTrades, result.ProfitableTrades),
		SuccessRate:    result.SuccessRate,
		Timestamp:      time.Now(),
		BacktestResult: result,
	}

	return n.notify(fmt.Sprintf("finance.backtest.%s", result.StockCode), notification)
}

// Close 关闭通知服务
func (n *NotificationService) Close() error {
	if n.rabbitMQChan != nil {
		if err := n.rabbitMQChan.Close(); err != nil {
			log.Printf("Failed to close RabbitMQ channel: %v", err)
		}
	}

	if n.rabbitMQConn != nil {
		if err := n.rabbitMQConn.Close(); err != nil {
			log.Printf("Failed to close RabbitMQ connection: %v", err)
		}
	}

	log.Println("NotificationService closed")
	return nil
}
