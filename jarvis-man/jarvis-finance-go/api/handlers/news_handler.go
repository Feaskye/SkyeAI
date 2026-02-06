package handlers

import (
	"encoding/json"
	"log"
	"net/http"

	"jarvis-finance-go/pkg/news"
	"jarvis-finance-go/pkg/notification"
)

// NewsHandler 新闻处理器
type NewsHandler struct {
	newsCrawler         *news.NewsCrawler
	notificationService *notification.NotificationService
}

// NewNewsHandler 创建新闻处理器实例
func NewNewsHandler(newsCrawler *news.NewsCrawler, notificationService *notification.NotificationService) *NewsHandler {
	return &NewsHandler{
		newsCrawler:         newsCrawler,
		notificationService: notificationService,
	}
}

// GetLatestNews 获取最新新闻
func (h *NewsHandler) GetLatestNews(w http.ResponseWriter, r *http.Request) {
	latestNews, err := h.newsCrawler.GetLatestNews()
	if err != nil {
		log.Printf("Failed to get latest news: %v", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(latestNews)
}

// GetNewsByStock 获取股票相关新闻
func (h *NewsHandler) GetNewsByStock(w http.ResponseWriter, r *http.Request) {
	// 获取股票代码参数
	stockCode := r.URL.Query().Get("stockCode")
	if stockCode == "" {
		http.Error(w, "stockCode is required", http.StatusBadRequest)
		return
	}

	newsByStock, err := h.newsCrawler.GetNewsByStock(stockCode)
	if err != nil {
		log.Printf("Failed to get news by stock %s: %v", stockCode, err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(newsByStock)
}
