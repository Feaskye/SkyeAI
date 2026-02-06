package scheduler

import (
	"context"
	"log"
	"time"

	"github.com/redis/go-redis/v9"
	"jarvis-finance-go/pkg/backtest"
	"jarvis-finance-go/pkg/model"
)

// ResultProcessor 监控结果处理器
type ResultProcessor struct {
	backtester        Backtester
	technicalAnalyzer TechnicalAnalyzer
	redisClient       *redis.Client
	ctx               context.Context
}

// Backtester 回测器接口
type Backtester interface {
	BacktestIndicator(stockCode, indicatorType string) (interface{}, error)
	AnalyzeSuccessRate(result interface{}) float64
	ManageBacktestData(result interface{}) error
}

// BacktesterAdapter 回测器适配器
type BacktesterAdapter struct {
	actualBacktester interface {
		BacktestIndicator(stockCode, indicatorType string) (interface{}, error)
		AnalyzeSuccessRate(result interface{}) float64
		ManageBacktestData(result interface{}) error
	}
}

// NewBacktesterAdapter 创建回测器适配器
func NewBacktesterAdapter(actualBacktester interface {
	BacktestIndicator(stockCode, indicatorType string) (interface{}, error)
	AnalyzeSuccessRate(result interface{}) float64
	ManageBacktestData(result interface{}) error
}) *BacktesterAdapter {
	return &BacktesterAdapter{
		actualBacktester: actualBacktester,
	}
}

// BacktestIndicator 回测指定指标
func (a *BacktesterAdapter) BacktestIndicator(stockCode, indicatorType string) (interface{}, error) {
	return a.actualBacktester.BacktestIndicator(stockCode, indicatorType)
}

// AnalyzeSuccessRate 分析回测成功率
func (a *BacktesterAdapter) AnalyzeSuccessRate(result interface{}) float64 {
	return a.actualBacktester.AnalyzeSuccessRate(result)
}

// ManageBacktestData 管理回测数据
func (a *BacktesterAdapter) ManageBacktestData(result interface{}) error {
	return a.actualBacktester.ManageBacktestData(result)
}

// StrategyBacktesterAdapter 策略回测器适配器，用于适配*backtest.StrategyBacktester
type StrategyBacktesterAdapter struct {
	backtester *backtest.StrategyBacktester
}

// NewStrategyBacktesterAdapter 创建策略回测器适配器
func NewStrategyBacktesterAdapter(backtester *backtest.StrategyBacktester) *StrategyBacktesterAdapter {
	return &StrategyBacktesterAdapter{
		backtester: backtester,
	}
}

// BacktestIndicator 回测指定指标
func (a *StrategyBacktesterAdapter) BacktestIndicator(stockCode, indicatorType string) (interface{}, error) {
	return a.backtester.BacktestIndicator(stockCode, indicatorType)
}

// AnalyzeSuccessRate 分析回测成功率
func (a *StrategyBacktesterAdapter) AnalyzeSuccessRate(result interface{}) float64 {
	if btResult, ok := result.(*model.BacktestResult); ok {
		return a.backtester.AnalyzeSuccessRate(btResult)
	}
	return 0
}

// ManageBacktestData 管理回测数据
func (a *StrategyBacktesterAdapter) ManageBacktestData(result interface{}) error {
	if btResult, ok := result.(*model.BacktestResult); ok {
		return a.backtester.ManageBacktestData(btResult)
	}
	return nil
}

// TechnicalAnalyzer 技术指标分析器接口
type TechnicalAnalyzer interface {
	CalculateIndicators(stockCode string, prices []float64, highPrices []float64, lowPrices []float64) map[string]float64
}

// NewResultProcessor 创建新的结果处理器
func NewResultProcessor(backtester Backtester, technicalAnalyzer TechnicalAnalyzer, redisClient *redis.Client) *ResultProcessor {
	return &ResultProcessor{
		backtester:        backtester,
		technicalAnalyzer: technicalAnalyzer,
		redisClient:       redisClient,
		ctx:               context.Background(),
	}
}

// ProcessMonitorResults 处理监控结果
func (rp *ResultProcessor) ProcessMonitorResults(stockCode string, monitorResult map[string]interface{}) (map[string]interface{}, error) {
	log.Printf("开始处理股票 %s 的监控结果\n", stockCode)

	// 生成模拟价格数据
	prices := make([]float64, 30)
	highPrices := make([]float64, 30)
	lowPrices := make([]float64, 30)

	// 填充模拟数据
	basePrice := 100.0
	if stockCode == "600519" {
		basePrice = 1800.0
	} else if stockCode == "000858" {
		basePrice = 160.0
	} else if stockCode == "601318" {
		basePrice = 45.0
	} else if stockCode == "000333" {
		basePrice = 50.0
	} else if stockCode == "600036" {
		basePrice = 35.0
	}

	currentPrice := basePrice
	for i := 0; i < 30; i++ {
		// 随机价格变动
		change := (float64(i%10) - 4.5) / 100
		currentPrice *= (1 + change)
		prices[i] = currentPrice
		highPrices[i] = currentPrice * 1.02
		lowPrices[i] = currentPrice * 0.98
	}

	// 计算技术指标
	indicatorsMap := rp.technicalAnalyzer.CalculateIndicators(stockCode, prices, highPrices, lowPrices)

	// 转换为map[string]interface{}
	indicators := make(map[string]interface{})
	for k, v := range indicatorsMap {
		indicators[k] = v
	}

	// 执行回测分析
	backtestResults := make(map[string]interface{})
	indicatorTypes := []string{"MACD", "KDJ", "RSI", "BOLL"}

	for _, indicatorType := range indicatorTypes {
		result, err := rp.backtester.BacktestIndicator(stockCode, indicatorType)
		if err != nil {
			log.Printf("回测指标 %s 失败: %v\n", indicatorType, err)
			continue
		}
		backtestResults[indicatorType] = result

		// 分析成功率
		successRate := rp.backtester.AnalyzeSuccessRate(result)
		backtestResults[indicatorType+"_success_rate"] = successRate

		// 管理回测数据
		err = rp.backtester.ManageBacktestData(result)
		if err != nil {
			log.Printf("管理回测数据失败: %v\n", err)
		}
	}

	// 合并结果
	result := map[string]interface{}{
		"stock_code":     stockCode,
		"timestamp":      time.Now().Unix(),
		"monitor_result": monitorResult,
		"indicators":     indicators,
		"backtest":       backtestResults,
	}

	// 存储结果到Redis
	rp.storeResult(stockCode, result)

	log.Printf("成功处理股票 %s 的监控结果\n", stockCode)
	return result, nil
}

// storeResult 存储结果到Redis
func (rp *ResultProcessor) storeResult(stockCode string, result map[string]interface{}) error {
	key := "stock:monitor:result:" + stockCode
	// 这里简化处理，实际应该序列化为JSON
	// err := rp.redisClient.Set(rp.ctx, key, result, 5*24*time.Hour).Err()
	// 暂时使用字符串模拟
	err := rp.redisClient.Set(rp.ctx, key, "result_data", 5*24*time.Hour).Err()
	if err != nil {
		log.Printf("存储监控结果到Redis失败: %v\n", err)
		return err
	}
	log.Printf("监控结果已存储到Redis，键: %s\n", key)
	return nil
}

// GetProcessedResult 获取处理后的结果
func (rp *ResultProcessor) GetProcessedResult(stockCode string) (map[string]interface{}, error) {
	key := "stock:monitor:result:" + stockCode
	val, err := rp.redisClient.Get(rp.ctx, key).Result()
	if err != nil {
		log.Printf("从Redis获取监控结果失败: %v\n", err)
		return nil, err
	}

	// 这里简化处理，实际应该解析JSON
	result := map[string]interface{}{
		"stock_code": stockCode,
		"result":     val,
		"timestamp":  time.Now().Unix(),
	}

	return result, nil
}
