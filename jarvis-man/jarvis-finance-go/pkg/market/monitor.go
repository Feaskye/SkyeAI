package market

import (
	"log"
	"time"

	"github.com/go-resty/resty/v2"
	"jarvis-finance-go/pkg/model"
)

// MarketMonitor 大盘监控模块
type MarketMonitor struct {
	client         *resty.Client
	indexes        []string
	alertThreshold float64
}

// NewMarketMonitor 创建大盘监控实例
func NewMarketMonitor() *MarketMonitor {
	client := resty.New()
	client.SetTimeout(10 * time.Second)

	// 初始化监控的指数列表
	indexes := []string{
		"000001.SH", // 上证指数
		"399001.SZ", // 深证成指
		"399006.SZ", // 创业板指
	}

	return &MarketMonitor{
		client:         client,
		indexes:        indexes,
		alertThreshold: 2.0, // 默认阈值为2%
	}
}

// Start 启动大盘监控
func (m *MarketMonitor) Start() {
	log.Println("MarketMonitor started")

	// 每5分钟监控一次
	ticker := time.NewTicker(5 * time.Minute)
	defer ticker.Stop()

	// 立即执行一次
	m.monitor()

	for range ticker.C {
		m.monitor()
	}
}

// monitor 执行监控任务
func (m *MarketMonitor) monitor() {
	log.Println("Executing market monitor task")

	for _, indexCode := range m.indexes {
		indexData, err := m.GetIndexData(indexCode)
		if err != nil {
			log.Printf("Failed to get index data for %s: %v", indexCode, err)
			continue
		}

		// 分析趋势
		trend := m.AnalyzeTrend(indexData)
		log.Printf("Index %s: price=%.2f, change=%.2f%%, trend=%s",
			indexData.IndexCode, indexData.Price, indexData.ChangePercent, trend)

		// 检查阈值
		if m.CheckThreshold(indexData) {
			log.Printf("Alert: Index %s changed by %.2f%%, exceeding threshold of %.2f%%",
				indexData.IndexCode, indexData.ChangePercent, m.alertThreshold)
			// 这里可以发送告警通知
		}
	}
}

// GetIndexData 获取指数数据
func (m *MarketMonitor) GetIndexData(indexCode string) (*model.IndexData, error) {
	// 在实际应用中，这里应该调用真实的API
	// 这里使用模拟数据
	return m.getMockIndexData(indexCode), nil
}

// AnalyzeTrend 分析指数趋势
func (m *MarketMonitor) AnalyzeTrend(indexData *model.IndexData) string {
	change := indexData.ChangePercent

	switch {
	case change > 1.5:
		return "strong_up"
	case change > 0.5:
		return "up"
	case change > -0.5:
		return "stable"
	case change > -1.5:
		return "down"
	default:
		return "strong_down"
	}
}

// CheckThreshold 检查是否超过阈值
func (m *MarketMonitor) CheckThreshold(indexData *model.IndexData) bool {
	return abs(indexData.ChangePercent) >= m.alertThreshold
}

// getMockIndexData 获取模拟指数数据
func (m *MarketMonitor) getMockIndexData(indexCode string) *model.IndexData {
	// 模拟指数数据
	indexMap := map[string]string{
		"000001.SH": "上证指数",
		"399001.SZ": "深证成指",
		"399006.SZ": "创业板指",
	}

	indexName, ok := indexMap[indexCode]
	if !ok {
		indexName = "未知指数"
	}

	// 生成随机价格和涨跌幅
	basePrice := 3000.0
	if indexCode == "399001.SZ" {
		basePrice = 12000.0
	} else if indexCode == "399006.SZ" {
		basePrice = 2400.0
	}

	// 随机涨跌幅 (-3% 到 +3%)
	changePercent := (randFloat()*6 - 3)
	price := basePrice * (1 + changePercent/100)

	return &model.IndexData{
		IndexCode:     indexCode,
		IndexName:     indexName,
		Price:         price,
		Volume:        int64(randFloat() * 100000000),
		Timestamp:     time.Now(),
		ChangePercent: changePercent,
	}
}

// randFloat 生成0-1之间的随机浮点数
func randFloat() float64 {
	return float64(time.Now().UnixNano()%1000) / 1000.0
}

// abs 获取绝对值
func abs(x float64) float64 {
	if x < 0 {
		return -x
	}
	return x
}
