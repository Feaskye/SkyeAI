package handlers

import (
	"encoding/json"
	"log"
	"net/http"

	"jarvis-finance-go/pkg/decision"
	"jarvis-finance-go/pkg/model"
	"jarvis-finance-go/pkg/notification"
	"jarvis-finance-go/pkg/stock"
	"jarvis-finance-go/pkg/technical"
)

// StockHandler 股票处理器
type StockHandler struct {
	stockMonitor        *stock.StockMonitor
	technicalAnalyzer   *technical.TechnicalAnalyzer
	decisionService     *decision.DecisionService
	notificationService *notification.NotificationService
}

// NewStockHandler 创建股票处理器实例
func NewStockHandler(stockMonitor *stock.StockMonitor, technicalAnalyzer *technical.TechnicalAnalyzer, decisionService *decision.DecisionService, notificationService *notification.NotificationService) *StockHandler {
	return &StockHandler{
		stockMonitor:        stockMonitor,
		technicalAnalyzer:   technicalAnalyzer,
		decisionService:     decisionService,
		notificationService: notificationService,
	}
}

// MonitorStock 监控股票
func (h *StockHandler) MonitorStock(w http.ResponseWriter, r *http.Request) {
	// 获取股票代码参数
	stockCode := r.URL.Query().Get("stockCode")
	if stockCode == "" {
		http.Error(w, "stockCode is required", http.StatusBadRequest)
		return
	}

	// 监控股票
	stockData, err := h.stockMonitor.MonitorStock(stockCode)
	if err != nil {
		log.Printf("Failed to monitor stock %s: %v", stockCode, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(stockData)
}

// GetTechnicalIndicators 获取技术指标
func (h *StockHandler) GetTechnicalIndicators(w http.ResponseWriter, r *http.Request) {
	// 获取股票代码参数
	stockCode := r.URL.Query().Get("stockCode")
	if stockCode == "" {
		http.Error(w, "stockCode is required", http.StatusBadRequest)
		return
	}

	// 获取股票数据
	stockData, err := h.stockMonitor.GetStockData(stockCode)
	if err != nil {
		log.Printf("Failed to get stock data for %s: %v", stockCode, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	// 计算技术指标
	prices := []float64{stockData.CurrentPrice}
	highPrices := []float64{stockData.HighPrice}
	lowPrices := []float64{stockData.LowPrice}

	indicators := h.technicalAnalyzer.CalculateIndicators(stockCode, prices, highPrices, lowPrices)

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(indicators)
}

// GetStockDecision 获取股票决策
func (h *StockHandler) GetStockDecision(w http.ResponseWriter, r *http.Request) {
	// 获取股票代码参数
	stockCode := r.URL.Query().Get("stockCode")
	if stockCode == "" {
		http.Error(w, "stockCode is required", http.StatusBadRequest)
		return
	}

	// 获取股票数据
	stockData, err := h.stockMonitor.GetStockData(stockCode)
	if err != nil {
		log.Printf("Failed to get stock data for %s: %v", stockCode, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	// 获取市场数据
	// 这里简化处理，实际应该从market模块获取
	marketData := &model.MarketData{
		Status:        "OPEN",
		ChangePercent: 1.2,
	}

	// 获取新闻数据
	// 这里简化处理，实际应该从news模块获取
	newsItems := []model.NewsItem{
		{Title: "公司发布财报，业绩超预期"},
		{Title: "行业政策利好，板块上涨"},
	}

	// 分析股票决策
	decisionResult, err := h.decisionService.AnalyzeStock(stockData, marketData, newsItems)
	if err != nil {
		log.Printf("Failed to analyze stock %s: %v", stockCode, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	// 发送通知
	if err := h.notificationService.NotifyStockDecision(stockCode, decisionResult); err != nil {
		log.Printf("Failed to send stock decision notification for %s: %v", stockCode, err)
		// 继续执行，不返回错误
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(decisionResult)
}
