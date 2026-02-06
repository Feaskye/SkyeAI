package decision

import (
	"fmt"
	"log"
	"sync"
	"time"

	"github.com/spf13/viper"
	"jarvis-finance-go/pkg/model"
)

// DecisionService AI决策服务
type DecisionService struct {
	decisionHistory  map[string]time.Time // 记录各股票的决策时间
	mutex            sync.RWMutex         // 用于并发访问决策历史
	decisionInterval time.Duration        // 决策间隔时间
}

// NewDecisionService 创建AI决策服务实例
func NewDecisionService() *DecisionService {
	// 从配置中获取决策间隔时间
	intervalMinutes := viper.GetInt("decision.interval_minutes")
	if intervalMinutes <= 0 {
		intervalMinutes = 5 // 默认5分钟
	}

	return &DecisionService{
		decisionHistory:  make(map[string]time.Time),
		decisionInterval: time.Duration(intervalMinutes) * time.Minute,
	}
}

// Start 启动决策服务
func (d *DecisionService) Start() {
	log.Println("DecisionService started")
	// 决策服务会在其他模块需要时被调用，不需要单独的定时任务
}

// AnalyzeStock 分析股票并生成决策
func (d *DecisionService) AnalyzeStock(stockData *model.StockData, marketData *model.MarketData, news []model.NewsItem) (*model.DecisionResult, error) {
	// 检查决策频率
	if !d.canMakeDecision(stockData.StockCode) {
		return nil, fmt.Errorf("decision frequency limit exceeded for stock %s", stockData.StockCode)
	}

	// 标记决策时间
	d.markDecisionTime(stockData.StockCode)

	// 生成决策输入
	decisionInput := d.generateDecisionInput(stockData, marketData, news)

	// 在实际应用中，这里应该调用LLM服务获取AI决策
	// 这里使用模拟决策
	decisionResult := d.mockAIDecision(decisionInput)

	log.Printf("Generated decision for %s: %s, confidence: %.2f",
		stockData.StockCode, decisionResult.Action, decisionResult.Confidence)

	return decisionResult, nil
}

// canMakeDecision 检查是否可以进行决策
func (d *DecisionService) canMakeDecision(stockCode string) bool {
	d.mutex.RLock()
	defer d.mutex.RUnlock()

	lastDecision, exists := d.decisionHistory[stockCode]
	if !exists {
		return true
	}

	return time.Since(lastDecision) >= d.decisionInterval
}

// markDecisionTime 标记决策时间
func (d *DecisionService) markDecisionTime(stockCode string) {
	d.mutex.Lock()
	defer d.mutex.Unlock()

	d.decisionHistory[stockCode] = time.Now()
}

// generateDecisionInput 生成决策输入
func (d *DecisionService) generateDecisionInput(stockData *model.StockData, marketData *model.MarketData, news []model.NewsItem) string {
	// 构建决策输入
	input := fmt.Sprintf(
		"Stock: %s\n", stockData.StockCode,
	)
	input += fmt.Sprintf(
		"Current Price: %.2f\n", stockData.CurrentPrice,
	)
	input += fmt.Sprintf(
		"Change: %.2f%%\n", stockData.ChangePercent,
	)
	input += fmt.Sprintf(
		"Market Status: %s\n", marketData.Status,
	)
	input += fmt.Sprintf(
		"Market Change: %.2f%%\n", marketData.ChangePercent,
	)

	// 添加新闻信息
	input += "Recent News:\n"
	for i, newsItem := range news {
		if i >= 3 { // 只取最近3条新闻
			break
		}
		input += fmt.Sprintf("- %s\n", newsItem.Title)
	}

	// 添加技术指标
	input += "Technical Indicators:\n"
	for indicator, value := range stockData.TechnicalIndicators {
		input += fmt.Sprintf("- %s: %.4f\n", indicator, value)
	}

	return input
}

// mockAIDecision 模拟AI决策
func (d *DecisionService) mockAIDecision(input string) *model.DecisionResult {
	// 基于简单规则模拟决策
	// 实际应用中应该调用LLM服务

	// 解析输入获取关键信息
	// 这里简化处理，直接生成模拟决策

	// 随机生成决策类型
	actions := []string{"BUY", "SELL", "HOLD"}
	action := actions[time.Now().UnixNano()%int64(len(actions))]

	// 生成信心度
	confidence := 0.7 + float64(time.Now().UnixNano()%30)/100

	// 生成决策理由
	reason := "基于技术指标分析和市场趋势判断"
	switch action {
	case "BUY":
		reason = "技术指标呈现金叉，市场趋势向上，建议买入"
	case "SELL":
		reason = "技术指标呈现死叉，市场趋势向下，建议卖出"
	case "HOLD":
		reason = "技术指标和市场趋势均不明显，建议持有观望"
	}

	return &model.DecisionResult{
		Action:      action,
		Confidence:  confidence,
		Reason:      reason,
		Timestamp:   time.Now(),
		InputData:   input,
		MarketTrend: "UP", // 模拟市场趋势
	}
}

// AnalyzeMarket 分析大盘并生成决策
func (d *DecisionService) AnalyzeMarket(marketData *model.MarketData, sectorData []model.SectorData, news []model.NewsItem) (*model.MarketDecision, error) {
	// 检查决策频率（大盘决策）
	if !d.canMakeDecision("MARKET") {
		return nil, fmt.Errorf("market decision frequency limit exceeded")
	}

	// 标记决策时间
	d.markDecisionTime("MARKET")

	// 生成决策输入
	input := fmt.Sprintf(
		"Market Status: %s\n", marketData.Status,
	)
	input += fmt.Sprintf(
		"Market Change: %.2f%%\n", marketData.ChangePercent,
	)

	// 添加版块信息
	input += "Sector Data:\n"
	for i, sector := range sectorData {
		if i >= 5 { // 只取前5个版块
			break
		}
		input += fmt.Sprintf("- %s: %.2f%%\n", sector.SectorName, sector.ChangePercent)
	}

	// 添加新闻信息
	input += "Market News:\n"
	for i, newsItem := range news {
		if i >= 3 { // 只取最近3条新闻
			break
		}
		input += fmt.Sprintf("- %s\n", newsItem.Title)
	}

	// 模拟大盘决策
	marketDecision := &model.MarketDecision{
		MarketStatus:   marketData.Status,
		Trend:          "UP", // 模拟趋势
		Confidence:     0.75, // 模拟信心度
		Recommendation: "保持积极仓位",
		Reason:         "大盘趋势向上，热点版块活跃，建议保持积极仓位",
		Timestamp:      time.Now(),
		InputData:      input,
	}

	log.Printf("Generated market decision: %s, confidence: %.2f",
		marketDecision.Recommendation, marketDecision.Confidence)

	return marketDecision, nil
}

// GetDecisionHistory 获取决策历史
func (d *DecisionService) GetDecisionHistory(stockCode string, limit int) ([]model.DecisionResult, error) {
	// 在实际应用中，这里应该从存储中获取历史决策
	// 这里返回空列表
	return []model.DecisionResult{}, nil
}
