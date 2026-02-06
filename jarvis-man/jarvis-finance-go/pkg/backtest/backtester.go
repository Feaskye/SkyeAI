package backtest

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"time"

	"github.com/redis/go-redis/v9"
	"jarvis-finance-go/pkg/model"
	"jarvis-finance-go/pkg/technical"

	"github.com/spf13/viper"
)

// StrategyBacktester 股票买卖指标回测模块
type StrategyBacktester struct {
	redisClient       *redis.Client
	technicalAnalyzer *technical.TechnicalAnalyzer
	backtestDays      int
	retentionDays     int
}

// NewStrategyBacktester 创建股票买卖指标回测实例
func NewStrategyBacktester() *StrategyBacktester {
	// 初始化Redis客户端
	redisClient := redis.NewClient(&redis.Options{
		Addr:     fmt.Sprintf("%s:%d", viper.GetString("redis.host"), viper.GetInt("redis.port")),
		Password: viper.GetString("redis.password"),
		DB:       viper.GetInt("redis.db"),
	})

	// 初始化技术指标分析器
	technicalAnalyzer := technical.NewTechnicalAnalyzer()

	// 从配置中获取回测天数和保留天数
	backtestDays := viper.GetInt("backtest.days")
	if backtestDays <= 0 {
		backtestDays = 10 // 默认回测10天
	}

	retentionDays := viper.GetInt("backtest.retention_days")
	if retentionDays <= 0 {
		retentionDays = 5 // 默认保留5天
	}

	return &StrategyBacktester{
		redisClient:       redisClient,
		technicalAnalyzer: technicalAnalyzer,
		backtestDays:      backtestDays,
		retentionDays:     retentionDays,
	}
}

// Start 启动回测任务
func (s *StrategyBacktester) Start() {
	log.Println("StrategyBacktester started")

	// 每1小时执行一次回测
	ticker := time.NewTicker(1 * time.Hour)
	defer ticker.Stop()

	// 立即执行一次
	s.runBacktest()

	for range ticker.C {
		s.runBacktest()
	}
}

// runBacktest 执行回测任务
func (s *StrategyBacktester) runBacktest() {
	log.Println("Executing strategy backtest task")

	// 回测的股票列表
	stocks := []string{
		"000001.SH", // 平安银行
		"000002.SH", // 万科A
		"600519.SH", // 贵州茅台
		"000858.SZ", // 五粮液
		"601318.SH", // 中国平安
	}

	// 回测的指标类型
	indicators := []string{
		"MACD",
		"KDJ",
		"RSI",
		"BOLL",
	}

	for _, stockCode := range stocks {
		for _, indicatorType := range indicators {
			result, err := s.BacktestIndicator(stockCode, indicatorType)
			if err != nil {
				log.Printf("Failed to backtest %s for %s: %v", indicatorType, stockCode, err)
				continue
			}

			// 分析回测成功率
			successRate := s.AnalyzeSuccessRate(result)
			log.Printf("Backtest result for %s on %s: success rate=%.2f%%, total trades=%d, profitable trades=%d",
				indicatorType, stockCode, successRate*100, result.TotalTrades, result.ProfitableTrades)

			// 保存回测结果到Redis
			s.ManageBacktestData(result)
		}
	}
}

// BacktestIndicator 回测指定指标
func (s *StrategyBacktester) BacktestIndicator(stockCode string, indicatorType string) (*model.BacktestResult, error) {
	// 在实际应用中，这里应该获取真实的历史数据
	// 这里使用模拟数据
	prices, highPrices, lowPrices := s.getMockPriceData(stockCode, s.backtestDays)

	// 计算技术指标
	indicators := s.technicalAnalyzer.CalculateIndicators(stockCode, prices, highPrices, lowPrices)

	// 模拟回测结果
	result := &model.BacktestResult{
		StockCode:        stockCode,
		IndicatorType:    indicatorType,
		SuccessRate:      0.65, // 模拟成功率为65%
		TotalTrades:      20,   // 模拟总交易次数为20次
		ProfitableTrades: 13,   // 模拟盈利交易次数为13次
		StartDate:        time.Now().AddDate(0, 0, -s.backtestDays),
		EndDate:          time.Now(),
		Parameters: map[string]interface{}{
			"indicator_value": indicators[indicatorType],
			"backtest_days":   s.backtestDays,
			"strategy":        "cross_over", // 模拟策略为金叉死叉
		},
	}

	return result, nil
}

// AnalyzeSuccessRate 分析回测成功率
func (s *StrategyBacktester) AnalyzeSuccessRate(result *model.BacktestResult) float64 {
	if result.TotalTrades == 0 {
		return 0
	}

	return float64(result.ProfitableTrades) / float64(result.TotalTrades)
}

// ManageBacktestData 管理回测数据
func (s *StrategyBacktester) ManageBacktestData(result *model.BacktestResult) error {
	ctx := context.Background()

	// 生成Redis键
	key := fmt.Sprintf("backtest:%s:%s:%s",
		result.StockCode,
		result.IndicatorType,
		result.EndDate.Format("20060102"))

	// 序列化回测结果
	data, err := json.Marshal(result)
	if err != nil {
		return fmt.Errorf("failed to marshal backtest result: %v", err)
	}

	// 计算过期时间（保留天数）
	expiry := time.Duration(s.retentionDays) * 24 * time.Hour

	// 保存到Redis
	err = s.redisClient.Set(ctx, key, data, expiry).Err()
	if err != nil {
		return fmt.Errorf("failed to save backtest result to Redis: %v", err)
	}

	log.Printf("Backtest result saved to Redis: key=%s, expiry=%v", key, expiry)
	return nil
}

// GetBacktestHistory 获取回测历史数据
func (s *StrategyBacktester) GetBacktestHistory(stockCode string, indicatorType string, limit int) ([]model.BacktestResult, error) {
	ctx := context.Background()
	results := []model.BacktestResult{}

	// 生成Redis键模式
	pattern := fmt.Sprintf("backtest:%s:%s:*", stockCode, indicatorType)

	// 查找匹配的键
	keys, err := s.redisClient.Keys(ctx, pattern).Result()
	if err != nil {
		return nil, fmt.Errorf("failed to find backtest keys: %v", err)
	}

	// 限制返回数量
	if limit > 0 && len(keys) > limit {
		keys = keys[:limit]
	}

	// 获取每个键对应的数据
	for _, key := range keys {
		data, err := s.redisClient.Get(ctx, key).Bytes()
		if err != nil {
			log.Printf("Failed to get backtest data for key %s: %v", key, err)
			continue
		}

		var result model.BacktestResult
		if err := json.Unmarshal(data, &result); err != nil {
			log.Printf("Failed to unmarshal backtest data for key %s: %v", key, err)
			continue
		}

		results = append(results, result)
	}

	return results, nil
}

// getMockPriceData 获取模拟价格数据
func (s *StrategyBacktester) getMockPriceData(stockCode string, days int) ([]float64, []float64, []float64) {
	prices := make([]float64, days)
	highPrices := make([]float64, days)
	lowPrices := make([]float64, days)

	// 基础价格
	basePrice := 100.0
	switch stockCode {
	case "600519.SH":
		basePrice = 1800.0
	case "000858.SZ":
		basePrice = 160.0
	case "601318.SH":
		basePrice = 45.0
	case "000002.SH":
		basePrice = 12.0
	case "000001.SH":
		basePrice = 15.0
	}

	// 生成模拟价格数据
	currentPrice := basePrice
	for i := 0; i < days; i++ {
		// 随机价格变动 (-2% 到 +2%)
		change := (float64(time.Now().UnixNano()%400)/100 - 2) + float64(i%3)*0.1
		currentPrice *= (1 + change/100)

		// 确保价格为正数
		if currentPrice < 0.1 {
			currentPrice = 0.1
		}

		prices[i] = currentPrice
		highPrices[i] = currentPrice * 1.02 // 最高价比收盘价高2%
		lowPrices[i] = currentPrice * 0.98  // 最低价比收盘价低2%
	}

	return prices, highPrices, lowPrices
}
