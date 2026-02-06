package news

import (
	"fmt"
	"log"
	"strings"
	"time"

	"github.com/gocolly/colly/v2"
	"jarvis-finance-go/pkg/model"
)

// NewsCrawler 股市新闻爬取模块
type NewsCrawler struct {
	crawler     *colly.Collector
	newsSources []NewsSource
	newsCache   map[string]bool // 用于去重的缓存
}

// NewsSource 新闻来源
type NewsSource struct {
	Name string
	URL  string
}

// NewNewsCrawler 创建新闻爬取实例
func NewNewsCrawler() *NewsCrawler {
	crawler := colly.NewCollector(
		colly.AllowedDomains("finance.sina.com.cn", "www.eastmoney.com"),
		colly.UserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"),
	)

	// 初始化新闻来源
	newsSources := []NewsSource{
		{Name: "sina", URL: "https://finance.sina.com.cn"},
		{Name: "eastmoney", URL: "https://www.eastmoney.com"},
	}

	return &NewsCrawler{
		crawler:     crawler,
		newsSources: newsSources,
		newsCache:   make(map[string]bool),
	}
}

// Start 启动新闻爬取
func (n *NewsCrawler) Start() {
	log.Println("NewsCrawler started")

	// 每30分钟爬取一次
	ticker := time.NewTicker(30 * time.Minute)
	defer ticker.Stop()

	// 立即执行一次
	n.crawl()

	for range ticker.C {
		n.crawl()
	}
}

// crawl 执行爬取任务
func (n *NewsCrawler) crawl() {
	log.Println("Executing news crawl task")

	for _, source := range n.newsSources {
		log.Printf("Crawling news from %s: %s", source.Name, source.URL)
		newsList, err := n.CrawlNews(source)
		if err != nil {
			log.Printf("Failed to crawl news from %s: %v", source.Name, err)
			continue
		}

		for _, news := range newsList {
			// 去重
			if n.Deduplicate(news) {
				// 分类
				category := n.ClassifyNews(news)
				log.Printf("News from %s: title=%s, category=%s, sentiment=%s",
					source.Name, news.Title, category, news.Sentiment)
				// 这里可以处理新闻，如存储或发送通知
			}
		}
	}
}

// CrawlNews 从指定来源爬取新闻
func (n *NewsCrawler) CrawlNews(source NewsSource) ([]model.NewsArticle, error) {
	// 在实际应用中，这里应该根据不同的来源实现不同的爬取逻辑
	// 这里使用模拟数据
	return n.getMockNews(source), nil
}

// Deduplicate 去重新闻
func (n *NewsCrawler) Deduplicate(news model.NewsArticle) bool {
	// 使用标题作为去重键
	key := news.Title
	if n.newsCache[key] {
		return false // 已存在
	}

	// 添加到缓存
	n.newsCache[key] = true

	// 限制缓存大小，防止内存溢出
	if len(n.newsCache) > 1000 {
		// 简单的缓存清理，实际应用中可以使用更复杂的策略
		n.newsCache = make(map[string]bool)
	}

	return true
}

// ClassifyNews 分类新闻
func (n *NewsCrawler) ClassifyNews(news model.NewsArticle) string {
	// 简单的分类逻辑，实际应用中可以使用更复杂的算法
	title := strings.ToLower(news.Title)
	content := strings.ToLower(news.Content)

	if strings.Contains(title, "政策") || strings.Contains(content, "政策") {
		return "policy"
	}
	if strings.Contains(title, "市场") || strings.Contains(content, "市场") {
		return "market"
	}
	if strings.Contains(title, "公司") || strings.Contains(content, "公司") {
		return "company"
	}
	if strings.Contains(title, "行业") || strings.Contains(content, "行业") {
		return "industry"
	}
	if strings.Contains(title, "国际") || strings.Contains(content, "国际") {
		return "international"
	}

	return "other"
}

// getMockNews 获取模拟新闻数据
func (n *NewsCrawler) getMockNews(source NewsSource) []model.NewsArticle {
	// 模拟新闻数据
	newsList := []model.NewsArticle{}

	// 为每个来源生成3条模拟新闻
	for i := 1; i <= 3; i++ {
		// 随机情感
		sentiments := []string{"positive", "negative", "neutral"}
		sentiment := sentiments[time.Now().UnixNano()%int64(len(sentiments))]

		// 随机相关股票
		relatedStocks := []string{}
		stockCodes := []string{"000001.SH", "399001.SZ", "000002.SH", "000008.SZ"}
		for j := 0; j < 2; j++ {
			stockIndex := (time.Now().UnixNano()%int64(len(stockCodes)) + int64(i*j)) % int64(len(stockCodes))
			relatedStocks = append(relatedStocks, stockCodes[stockIndex])
		}

		news := model.NewsArticle{
			Title:         fmt.Sprintf("%s新闻%d: 股市%s消息", source.Name, i, sentiment),
			Content:       fmt.Sprintf("这是一条来自%s的模拟新闻内容，关于股市%s的消息。", source.Name, sentiment),
			Source:        source.Name,
			PublishTime:   time.Now().Add(-time.Duration(i) * time.Hour),
			RelatedStocks: relatedStocks,
			Sentiment:     sentiment,
		}

		newsList = append(newsList, news)
	}

	return newsList
}

// GetLatestNews 获取最新新闻
func (n *NewsCrawler) GetLatestNews() ([]model.NewsArticle, error) {
	// 这里简化处理，实际应该返回最近爬取的新闻
	// 从每个来源获取新闻并合并
	allNews := []model.NewsArticle{}
	for _, source := range n.newsSources {
		newsList, err := n.CrawlNews(source)
		if err != nil {
			continue
		}
		allNews = append(allNews, newsList...)
	}
	return allNews, nil
}

// GetNewsByStock 获取股票相关新闻
func (n *NewsCrawler) GetNewsByStock(stockCode string) ([]model.NewsArticle, error) {
	// 这里简化处理，实际应该根据股票代码过滤新闻
	// 从每个来源获取新闻并过滤
	allNews := []model.NewsArticle{}
	for _, source := range n.newsSources {
		newsList, err := n.CrawlNews(source)
		if err != nil {
			continue
		}
		// 过滤出与股票相关的新闻
		for _, news := range newsList {
			for _, relatedStock := range news.RelatedStocks {
				if relatedStock == stockCode {
					allNews = append(allNews, news)
					break
				}
			}
		}
	}
	return allNews, nil
}
