package stock

import (
	"fmt"
	"log"
	"time"

	"github.com/go-resty/resty/v2"
	"github.com/redis/go-redis/v9"
	"jarvis-finance-go/pkg/model"

	"context"
	"github.com/spf13/viper"
)

// StockMonitor 个股盯盘模块
type StockMonitor struct {
	client          *resty.Client
	redisClient     *redis.Client
	watchedStocks   []string
	priceThreshold  float64
	volumeThreshold float64
}

// NewStockMonitor 创建个股盯盘实例
func NewStockMonitor() *StockMonitor {
	client := resty.New()
	client.SetTimeout(10 * time.Second)

	// 初始化Redis客户端
	redisClient := redis.NewClient(&redis.Options{
		Addr:     fmt.Sprintf("%s:%d", viper.GetString("redis.host"), viper.GetInt("redis.port")),
		Password: viper.GetString("redis.password"),
		DB:       viper.GetInt("redis.db"),
	})

	// 初始化关注的股票列表
	watchedStocks := []string{
		"000001.SH", // 平安银行
		"000002.SH", // 万科A
		"600519.SH", // 贵州茅台
		"000858.SZ", // 五粮液
		"601318.SH", // 中国平安
	}

	return &StockMonitor{
		client:          client,
		redisClient:     redisClient,
		watchedStocks:   watchedStocks,
		priceThreshold:  5.0,   // 价格变动阈值为5%
		volumeThreshold: 100.0, // 成交量变动阈值为100%
	}
}

// Start 启动个股盯盘
func (s *StockMonitor) Start() {
	log.Println("StockMonitor started")

	// 每3分钟监控一次
	ticker := time.NewTicker(3 * time.Minute)
	defer ticker.Stop()

	// 立即执行一次
	s.monitor()

	for range ticker.C {
		s.monitor()
	}
}

// monitor 执行监控任务
func (s *StockMonitor) monitor() {
	log.Println("Executing stock monitor task")

	for _, stockCode := range s.watchedStocks {
		// 获取股票价格
		stockPrice, err := s.MonitorStock(stockCode)
		if err != nil {
			log.Printf("Failed to monitor stock %s: %v", stockCode, err)
			continue
		}

		// 检测变化
		changes := s.DetectChanges(stockPrice)
		if len(changes) > 0 {
			log.Printf("Stock %s changes detected: %v", stockCode, changes)
			// 这里可以发送告警通知
		}

		// 打印股票数据
		log.Printf("Stock %s: price=%.2f, volume=%d, change=%.2f%%",
			stockPrice.StockCode, stockPrice.Price, stockPrice.Volume, stockPrice.ChangePercent)

		// 缓存股票数据到Redis
		s.cacheStockData(stockPrice)
	}
}

// MonitorStock 监控个股
func (s *StockMonitor) MonitorStock(stockCode string) (*model.StockPrice, error) {
	// 在实际应用中，这里应该调用真实的API
	// 这里使用模拟数据
	return s.getMockStockPrice(stockCode), nil
}

// DetectChanges 检测股票变化
func (s *StockMonitor) DetectChanges(stockPrice *model.StockPrice) []string {
	changes := []string{}

	// 检测价格变化
	if abs(stockPrice.ChangePercent) >= s.priceThreshold {
		changes = append(changes, fmt.Sprintf("price_change: %.2f%%", stockPrice.ChangePercent))
	}

	// 检测成交量变化（这里简化处理，实际应该与历史成交量比较）
	if stockPrice.Volume > 10000000 {
		changes = append(changes, fmt.Sprintf("high_volume: %d", stockPrice.Volume))
	}

	return changes
}

// CalculateIndicators 计算技术指标（这里作为占位符，实际由TechnicalAnalyzer模块实现）
func (s *StockMonitor) CalculateIndicators(stockCode string) (map[string]float64, error) {
	// 实际应用中，这里应该调用TechnicalAnalyzer模块
	return map[string]float64{
		"MACD": 0.5,
		"KDJ":  70.0,
		"RSI":  65.0,
		"BOLL": 10.0,
	}, nil
}

// GetStockData 获取股票数据
func (s *StockMonitor) GetStockData(stockCode string) (*model.StockData, error) {
	// 首先获取股票价格数据
	stockPrice, err := s.MonitorStock(stockCode)
	if err != nil {
		return nil, err
	}

	// 计算技术指标
	indicators, err := s.CalculateIndicators(stockCode)
	if err != nil {
		return nil, err
	}

	// 构建StockData
	stockData := &model.StockData{
		StockCode:           stockCode,
		CurrentPrice:        stockPrice.Price,
		ChangePercent:       stockPrice.ChangePercent,
		Volume:              stockPrice.Volume,
		Timestamp:           stockPrice.Timestamp,
		TechnicalIndicators: indicators,
		Status:              "NORMAL",
	}

	return stockData, nil
}

// cacheStockData 缓存股票数据到Redis
func (s *StockMonitor) cacheStockData(stockPrice *model.StockPrice) {
	ctx := context.Background()
	key := fmt.Sprintf("stock:price:%s", stockPrice.StockCode)
	value := fmt.Sprintf("%.2f", stockPrice.Price)

	// 缓存10分钟
	err := s.redisClient.Set(ctx, key, value, 10*time.Minute).Err()
	if err != nil {
		log.Printf("Failed to cache stock data for %s: %v", stockPrice.StockCode, err)
	}
}

// getMockStockPrice 获取模拟股票价格数据
func (s *StockMonitor) getMockStockPrice(stockCode string) *model.StockPrice {
	// 模拟股票价格数据
	stockMap := map[string]float64{
		"000001.SH": 15.0,
		"000002.SH": 12.0,
		"600519.SH": 1800.0,
		"000858.SZ": 160.0,
		"601318.SH": 45.0,
	}

	basePrice, ok := stockMap[stockCode]
	if !ok {
		basePrice = 50.0
	}

	// 随机价格变动 (-10% 到 +10%)
	changePercent := (float64(time.Now().UnixNano()%200)/10 - 10)
	price := basePrice * (1 + changePercent/100)

	// 随机成交量
	volume := int64(5000000 + time.Now().UnixNano()%15000000)

	return &model.StockPrice{
		StockCode:     stockCode,
		Price:         price,
		Volume:        volume,
		Timestamp:     time.Now(),
		ChangePercent: changePercent,
	}
}

// abs 获取绝对值
func abs(x float64) float64 {
	if x < 0 {
		return -x
	}
	return x
}
