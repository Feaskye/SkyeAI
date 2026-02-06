package scheduler

import (
	"log"
	"time"

	"jarvis-finance-go/pkg/model"
	"jarvis-finance-go/pkg/notification"
)

// NotificationIntegrator 通知集成器
type NotificationIntegrator struct {
	notificationService  NotificationService
	lastNotificationTime map[string]time.Time
	notificationInterval time.Duration
}

// NotificationService 通知服务接口
type NotificationService interface {
	NotifyStockAlert(stockCode string, alertType string, message string) error
	NotifyMarketAlert(alertType string, message string) error
	NotifySectorAlert(sectorName string, alertType string, message string) error
	NotifyBacktestResult(result interface{}) error
	NotifyStockDecision(stockCode string, decision interface{}) error
	NotifyMarketDecision(decision interface{}) error
}

// NotificationServiceAdapter 通知服务适配器
type NotificationServiceAdapter struct {
	actualService NotificationService
}

// NewNotificationServiceAdapter 创建通知服务适配器
func NewNotificationServiceAdapter(actualService NotificationService) *NotificationServiceAdapter {
	return &NotificationServiceAdapter{
		actualService: actualService,
	}
}

// NotifyStockAlert 通知股票预警
func (a *NotificationServiceAdapter) NotifyStockAlert(stockCode string, alertType string, message string) error {
	return a.actualService.NotifyStockAlert(stockCode, alertType, message)
}

// NotifyMarketAlert 通知大盘预警
func (a *NotificationServiceAdapter) NotifyMarketAlert(alertType string, message string) error {
	return a.actualService.NotifyMarketAlert(alertType, message)
}

// NotifySectorAlert 通知版块预警
func (a *NotificationServiceAdapter) NotifySectorAlert(sectorName string, alertType string, message string) error {
	return a.actualService.NotifySectorAlert(sectorName, alertType, message)
}

// NotifyBacktestResult 通知回测结果
func (a *NotificationServiceAdapter) NotifyBacktestResult(result interface{}) error {
	return a.actualService.NotifyBacktestResult(result)
}

// NotifyStockDecision 通知股票决策
func (a *NotificationServiceAdapter) NotifyStockDecision(stockCode string, decision interface{}) error {
	return a.actualService.NotifyStockDecision(stockCode, decision)
}

// NotifyMarketDecision 通知大盘决策
func (a *NotificationServiceAdapter) NotifyMarketDecision(decision interface{}) error {
	return a.actualService.NotifyMarketDecision(decision)
}

// SpecificNotificationServiceAdapter 特定通知服务适配器，用于适配*notification.NotificationService
type SpecificNotificationServiceAdapter struct {
	notificationService *notification.NotificationService
}

// NewSpecificNotificationServiceAdapter 创建特定通知服务适配器
func NewSpecificNotificationServiceAdapter(notificationService *notification.NotificationService) *SpecificNotificationServiceAdapter {
	return &SpecificNotificationServiceAdapter{
		notificationService: notificationService,
	}
}

// NotifyStockAlert 通知股票预警
func (a *SpecificNotificationServiceAdapter) NotifyStockAlert(stockCode string, alertType string, message string) error {
	return a.notificationService.NotifyStockAlert(stockCode, alertType, message)
}

// NotifyMarketAlert 通知大盘预警
func (a *SpecificNotificationServiceAdapter) NotifyMarketAlert(alertType string, message string) error {
	return a.notificationService.NotifyMarketAlert(alertType, message)
}

// NotifySectorAlert 通知版块预警
func (a *SpecificNotificationServiceAdapter) NotifySectorAlert(sectorName string, alertType string, message string) error {
	return a.notificationService.NotifySectorAlert(sectorName, alertType, message)
}

// NotifyBacktestResult 通知回测结果
func (a *SpecificNotificationServiceAdapter) NotifyBacktestResult(result interface{}) error {
	if btResult, ok := result.(*model.BacktestResult); ok {
		return a.notificationService.NotifyBacktestResult(btResult)
	}
	return nil
}

// NotifyStockDecision 通知股票决策
func (a *SpecificNotificationServiceAdapter) NotifyStockDecision(stockCode string, decision interface{}) error {
	if decisionResult, ok := decision.(*model.DecisionResult); ok {
		return a.notificationService.NotifyStockDecision(stockCode, decisionResult)
	}
	return nil
}

// NotifyMarketDecision 通知大盘决策
func (a *SpecificNotificationServiceAdapter) NotifyMarketDecision(decision interface{}) error {
	if marketDecision, ok := decision.(*model.MarketDecision); ok {
		return a.notificationService.NotifyMarketDecision(marketDecision)
	}
	return nil
}

// NewNotificationIntegrator 创建新的通知集成器
func NewNotificationIntegrator(notificationService NotificationService) *NotificationIntegrator {
	return &NotificationIntegrator{
		notificationService:  notificationService,
		lastNotificationTime: make(map[string]time.Time),
		notificationInterval: 5 * time.Minute, // 通知间隔为5分钟
	}
}

// SendNotification 发送通知
func (ni *NotificationIntegrator) SendNotification(alertType, stockCode, message string, severity string, data map[string]interface{}) error {
	// 检查通知频率
	key := alertType + ":" + stockCode
	lastTime, exists := ni.lastNotificationTime[key]
	if exists {
		elapsed := time.Since(lastTime)
		if elapsed < ni.notificationInterval {
			log.Printf("通知频率过高，跳过通知: %s - %s\n", alertType, stockCode)
			return nil
		}
	}

	// 根据alertType选择调用哪个通知方法
	var err error
	switch alertType {
	case "stock_monitor":
		err = ni.notificationService.NotifyStockAlert(stockCode, severity, message)
	case "backtest_result":
		// 从data中提取回测结果
		if backtestResult, ok := data["backtest"].(interface{}); ok {
			err = ni.notificationService.NotifyBacktestResult(backtestResult)
		} else {
			err = ni.notificationService.NotifyStockAlert(stockCode, "info", message)
		}
	case "stock_decision":
		if decision, ok := data["decision"].(interface{}); ok {
			err = ni.notificationService.NotifyStockDecision(stockCode, decision)
		} else {
			err = ni.notificationService.NotifyStockAlert(stockCode, "info", message)
		}
	case "market_decision":
		if decision, ok := data["decision"].(interface{}); ok {
			err = ni.notificationService.NotifyMarketDecision(decision)
		} else {
			err = ni.notificationService.NotifyMarketAlert("info", message)
		}
	default:
		err = ni.notificationService.NotifyStockAlert(stockCode, "info", message)
	}

	if err != nil {
		log.Printf("发送通知失败: %v\n", err)
		return err
	}

	// 更新最后通知时间
	ni.lastNotificationTime[key] = time.Now()

	log.Printf("成功发送通知: %s - %s, 严重程度: %s\n", alertType, stockCode, severity)
	return nil
}

// ControlFrequency 控制通知频率
func (ni *NotificationIntegrator) ControlFrequency() error {
	// 通知频率控制在SendNotification方法中已经实现
	// 这里可以添加额外的频率控制逻辑
	log.Println("通知频率控制检查")
	return nil
}

// HandleNotificationResult 处理通知结果
func (ni *NotificationIntegrator) HandleNotificationResult(result map[string]interface{}) error {
	// 这里可以添加通知结果的处理逻辑
	log.Printf("处理通知结果: %v\n", result)
	return nil
}

// SendStockMonitorNotification 发送股票监控通知
func (ni *NotificationIntegrator) SendStockMonitorNotification(stockCode string, result map[string]interface{}) error {
	message := "股票监控结果更新"
	severity := "info"

	// 根据监控结果设置严重程度
	if priceChange, ok := result["price_change"].(float64); ok {
		if priceChange > 5 {
			severity = "high"
			message = "股票价格大幅上涨"
		} else if priceChange < -5 {
			severity = "high"
			message = "股票价格大幅下跌"
		} else if priceChange > 2 {
			severity = "medium"
			message = "股票价格上涨"
		} else if priceChange < -2 {
			severity = "medium"
			message = "股票价格下跌"
		}
	}

	return ni.SendNotification("stock_monitor", stockCode, message, severity, result)
}

// SendBacktestNotification 发送回测结果通知
func (ni *NotificationIntegrator) SendBacktestNotification(stockCode string, result map[string]interface{}) error {
	message := "回测结果更新"
	severity := "info"

	// 根据回测结果设置严重程度
	if successRate, ok := result["success_rate"].(float64); ok {
		if successRate > 0.7 {
			severity = "high"
			message = "回测成功率较高"
		} else if successRate < 0.3 {
			severity = "medium"
			message = "回测成功率较低"
		}
	}

	return ni.SendNotification("backtest_result", stockCode, message, severity, result)
}
