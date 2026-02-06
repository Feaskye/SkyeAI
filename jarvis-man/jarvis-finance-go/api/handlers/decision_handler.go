package handlers

import (
	"encoding/json"
	"log"
	"net/http"

	"jarvis-finance-go/pkg/decision"
	"jarvis-finance-go/pkg/model"
	"jarvis-finance-go/pkg/notification"
)

// DecisionHandler 决策处理器
type DecisionHandler struct {
	decisionService     *decision.DecisionService
	notificationService *notification.NotificationService
}

// NewDecisionHandler 创建决策处理器实例
func NewDecisionHandler(decisionService *decision.DecisionService, notificationService *notification.NotificationService) *DecisionHandler {
	return &DecisionHandler{
		decisionService:     decisionService,
		notificationService: notificationService,
	}
}

// AnalyzeStock 分析股票
func (h *DecisionHandler) AnalyzeStock(w http.ResponseWriter, r *http.Request) {
	// 获取股票代码参数
	stockCode := r.URL.Query().Get("stockCode")
	if stockCode == "" {
		http.Error(w, "stockCode is required", http.StatusBadRequest)
		return
	}

	// 这里简化处理，实际应该从各个模块获取真实数据
	// 股票数据
	stockData := &model.StockData{
		StockCode:     stockCode,
		CurrentPrice:  100.0,
		ChangePercent: 2.5,
		TechnicalIndicators: map[string]float64{
			"MACD": 0.5,
			"KDJ":  75.0,
			"RSI":  65.0,
			"BOLL": 105.0,
		},
	}

	// 市场数据
	marketData := &model.MarketData{
		Status:        "OPEN",
		ChangePercent: 1.2,
	}

	// 新闻数据
	newsItems := []model.NewsItem{
		{Title: "公司发布财报，业绩超预期"},
		{Title: "行业政策利好，板块上涨"},
	}

	// 分析股票
	decisionResult, err := h.decisionService.AnalyzeStock(stockData, marketData, newsItems)
	if err != nil {
		log.Printf("Failed to analyze stock %s: %v", stockCode, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	// 发送决策通知
	if err := h.notificationService.NotifyStockDecision(stockCode, decisionResult); err != nil {
		log.Printf("Failed to send stock decision notification: %v", err)
		// 继续执行，不返回错误
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(decisionResult)
}

// AnalyzeMarket 分析大盘
func (h *DecisionHandler) AnalyzeMarket(w http.ResponseWriter, r *http.Request) {
	// 这里简化处理，实际应该从各个模块获取真实数据
	// 市场数据
	marketData := &model.MarketData{
		Status:        "OPEN",
		ChangePercent: 1.2,
	}

	// 版块数据
	sectorData := []model.SectorData{
		{SectorName: "Technology", ChangePercent: 2.5},
		{SectorName: "Finance", ChangePercent: 1.2},
		{SectorName: "Healthcare", ChangePercent: 0.8},
		{SectorName: "Energy", ChangePercent: -0.5},
		{SectorName: "Consumer", ChangePercent: 1.5},
	}

	// 新闻数据
	newsItems := []model.NewsItem{
		{Title: "市场回暖，科技股领涨"},
		{Title: "央行降准，流动性改善"},
		{Title: "外围市场波动，A股承压"},
	}

	// 分析大盘
	decisionResult, err := h.decisionService.AnalyzeMarket(marketData, sectorData, newsItems)
	if err != nil {
		log.Printf("Failed to analyze market: %v", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	// 发送决策通知
	if err := h.notificationService.NotifyMarketDecision(decisionResult); err != nil {
		log.Printf("Failed to send market decision notification: %v", err)
		// 继续执行，不返回错误
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(decisionResult)
}
