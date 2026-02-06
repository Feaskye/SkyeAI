package handlers

import (
	"encoding/json"
	"log"
	"net/http"
	"strconv"

	"jarvis-finance-go/pkg/backtest"
	"jarvis-finance-go/pkg/notification"
)

// BacktestHandler 回测处理器
type BacktestHandler struct {
	strategyBacktester  *backtest.StrategyBacktester
	notificationService *notification.NotificationService
}

// NewBacktestHandler 创建回测处理器实例
func NewBacktestHandler(strategyBacktester *backtest.StrategyBacktester, notificationService *notification.NotificationService) *BacktestHandler {
	return &BacktestHandler{
		strategyBacktester:  strategyBacktester,
		notificationService: notificationService,
	}
}

// GetBacktestResult 获取回测结果
func (h *BacktestHandler) GetBacktestResult(w http.ResponseWriter, r *http.Request) {
	// 获取股票代码参数
	stockCode := r.URL.Query().Get("stockCode")
	if stockCode == "" {
		http.Error(w, "stockCode is required", http.StatusBadRequest)
		return
	}

	// 获取指标类型参数
	indicatorType := r.URL.Query().Get("indicatorType")
	if indicatorType == "" {
		http.Error(w, "indicatorType is required", http.StatusBadRequest)
		return
	}

	// 执行回测
	result, err := h.strategyBacktester.BacktestIndicator(stockCode, indicatorType)
	if err != nil {
		log.Printf("Failed to backtest indicator %s for %s: %v", indicatorType, stockCode, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	// 发送回测结果通知
	if err := h.notificationService.NotifyBacktestResult(result); err != nil {
		log.Printf("Failed to send backtest result notification: %v", err)
		// 继续执行，不返回错误
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(result)
}

// GetBacktestHistory 获取回测历史
func (h *BacktestHandler) GetBacktestHistory(w http.ResponseWriter, r *http.Request) {
	// 获取股票代码参数
	stockCode := r.URL.Query().Get("stockCode")
	if stockCode == "" {
		http.Error(w, "stockCode is required", http.StatusBadRequest)
		return
	}

	// 获取指标类型参数
	indicatorType := r.URL.Query().Get("indicatorType")
	if indicatorType == "" {
		http.Error(w, "indicatorType is required", http.StatusBadRequest)
		return
	}

	// 获取限制参数
	limitStr := r.URL.Query().Get("limit")
	limit := 10 // 默认10条
	if limitStr != "" {
		if l, err := strconv.Atoi(limitStr); err == nil && l > 0 {
			limit = l
		}
	}

	// 获取回测历史
	history, err := h.strategyBacktester.GetBacktestHistory(stockCode, indicatorType, limit)
	if err != nil {
		log.Printf("Failed to get backtest history for %s: %v", stockCode, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(history)
}
