package handlers

import (
	"encoding/json"
	"log"
	"net/http"

	"jarvis-finance-go/pkg/decision"
	"jarvis-finance-go/pkg/market"
	"jarvis-finance-go/pkg/model"
	"jarvis-finance-go/pkg/notification"
)

// MarketHandler 市场处理器
type MarketHandler struct {
	marketMonitor       *market.MarketMonitor
	decisionService     *decision.DecisionService
	notificationService *notification.NotificationService
}

// NewMarketHandler 创建市场处理器实例
func NewMarketHandler(marketMonitor *market.MarketMonitor, decisionService *decision.DecisionService, notificationService *notification.NotificationService) *MarketHandler {
	return &MarketHandler{
		marketMonitor:       marketMonitor,
		decisionService:     decisionService,
		notificationService: notificationService,
	}
}

// GetIndexData 获取指数数据
func (h *MarketHandler) GetIndexData(w http.ResponseWriter, r *http.Request) {
	// 获取指数代码参数
	indexCode := r.URL.Query().Get("indexCode")
	if indexCode == "" {
		indexCode = "000001.SH" // 默认上证指数
	}

	indexData, err := h.marketMonitor.GetIndexData(indexCode)
	if err != nil {
		log.Printf("Failed to get index data: %v", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(indexData)
}

// AnalyzeTrend 分析趋势
func (h *MarketHandler) AnalyzeTrend(w http.ResponseWriter, r *http.Request) {
	// 获取指数代码参数
	indexCode := r.URL.Query().Get("indexCode")
	if indexCode == "" {
		indexCode = "000001.SH" // 默认上证指数
	}

	// 获取指数数据
	indexData, err := h.marketMonitor.GetIndexData(indexCode)
	if err != nil {
		log.Printf("Failed to get index data: %v", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	// 分析趋势
	trend := h.marketMonitor.AnalyzeTrend(indexData)

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(trend)
}

// GetMarketDecision 获取大盘决策
func (h *MarketHandler) GetMarketDecision(w http.ResponseWriter, r *http.Request) {
	// 获取指数代码参数
	indexCode := r.URL.Query().Get("indexCode")
	if indexCode == "" {
		indexCode = "000001.SH" // 默认上证指数
	}

	// 获取指数数据
	indexData, err := h.marketMonitor.GetIndexData(indexCode)
	if err != nil {
		log.Printf("Failed to get index data: %v", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	// 构建市场数据
	marketData := &model.MarketData{
		MarketName:    "A股",
		IndexCode:     indexData.IndexCode,
		IndexName:     indexData.IndexName,
		CurrentPrice:  indexData.Price,
		ChangePercent: indexData.ChangePercent,
		Timestamp:     indexData.Timestamp,
		Status:        "OPEN",
	}

	// 获取版块数据
	// 这里简化处理，实际应该从sector模块获取
	sectorData := []model.SectorData{
		{SectorName: "Technology", ChangePercent: 2.5},
		{SectorName: "Finance", ChangePercent: 1.2},
		{SectorName: "Healthcare", ChangePercent: 0.8},
		{SectorName: "Energy", ChangePercent: -0.5},
		{SectorName: "Consumer", ChangePercent: 1.5},
	}

	// 获取新闻数据
	// 这里简化处理，实际应该从news模块获取
	newsItems := []model.NewsItem{
		{Title: "市场回暖，科技股领涨", Sentiment: "positive"},
		{Title: "央行降准，流动性改善", Sentiment: "positive"},
		{Title: "外围市场波动，A股承压", Sentiment: "negative"},
	}

	// 分析大盘决策
	decisionResult, err := h.decisionService.AnalyzeMarket(marketData, sectorData, newsItems)
	if err != nil {
		log.Printf("Failed to analyze market: %v", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	// 发送通知
	if err := h.notificationService.NotifyMarketDecision(decisionResult); err != nil {
		log.Printf("Failed to send market decision notification: %v", err)
		// 继续执行，不返回错误
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(decisionResult)
}
