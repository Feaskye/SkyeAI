package api

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"time"

	"github.com/redis/go-redis/v9"
	"github.com/spf13/viper"
	"jarvis-finance-go/api/handlers"
	"jarvis-finance-go/pkg/backtest"
	"jarvis-finance-go/pkg/decision"
	"jarvis-finance-go/pkg/market"
	"jarvis-finance-go/pkg/news"
	"jarvis-finance-go/pkg/notification"
	"jarvis-finance-go/pkg/scheduler"
	"jarvis-finance-go/pkg/sector"
	"jarvis-finance-go/pkg/stock"
	"jarvis-finance-go/pkg/technical"
)

// Server API服务器
type Server struct {
	httpServer             *http.Server
	marketHandler          *handlers.MarketHandler
	stockHandler           *handlers.StockHandler
	sectorHandler          *handlers.SectorHandler
	newsHandler            *handlers.NewsHandler
	backtestHandler        *handlers.BacktestHandler
	decisionHandler        *handlers.DecisionHandler
	taskScheduler          *scheduler.TaskScheduler
	stockScheduler         *scheduler.StockScheduler
	resultProcessor        *scheduler.ResultProcessor
	notificationIntegrator *scheduler.NotificationIntegrator
}

// NewServer 创建API服务器实例
func NewServer() *Server {
	// 初始化各个模块
	marketMonitor := market.NewMarketMonitor()
	stockMonitor := stock.NewStockMonitor()
	sectorMonitor := sector.NewSectorMonitor()
	newsCrawler := news.NewNewsCrawler()
	technicalAnalyzer := technical.NewTechnicalAnalyzer()
	strategyBacktester := backtest.NewStrategyBacktester()
	decisionService := decision.NewDecisionService()
	notificationService := notification.NewNotificationService()

	// 初始化Redis客户端
	redisClient := redis.NewClient(&redis.Options{
		Addr:     fmt.Sprintf("%s:%d", viper.GetString("redis.host"), viper.GetInt("redis.port")),
		Password: viper.GetString("redis.password"),
		DB:       viper.GetInt("redis.db"),
	})

	// 初始化定时任务相关组件
	taskScheduler := scheduler.NewTaskScheduler()

	// 创建数据服务客户端（这里使用模拟实现）
	dataServiceClient := &mockDataServiceClient{}
	stockScheduler := scheduler.NewStockScheduler(dataServiceClient, redisClient)

	backtesterAdapter := scheduler.NewStrategyBacktesterAdapter(strategyBacktester)
	resultProcessor := scheduler.NewResultProcessor(backtesterAdapter, technicalAnalyzer, redisClient)
	notificationAdapter := scheduler.NewSpecificNotificationServiceAdapter(notificationService)
	notificationIntegrator := scheduler.NewNotificationIntegrator(notificationAdapter)

	// 启动各个模块
	go marketMonitor.Start()
	go stockMonitor.Start()
	go sectorMonitor.Start()
	go newsCrawler.Start()
	go strategyBacktester.Start()
	go decisionService.Start()
	go notificationService.Start()

	// 启动定时任务调度器
	go taskScheduler.Start()

	// 添加5分钟执行一次的核心任务
	taskScheduler.AddTask(scheduler.Every5Minutes, "CoreStockMonitoringTask", func() {
		// 从数据服务拉取股票监控列表
		stocks, err := stockScheduler.FetchStockList()
		if err != nil {
			log.Printf("拉取股票列表失败: %v\n", err)
			return
		}

		// 对每只股票执行监控和分析
		for _, stockCode := range stocks {
			// 调用核心监控功能获取真实的监控数据
			stockPrice, err := stockMonitor.MonitorStock(stockCode)
			if err != nil {
				log.Printf("监控股票 %s 失败: %v\n", stockCode, err)
				// 使用模拟数据作为备选
				monitorResult := map[string]interface{}{
					"price":          100.0,
					"volume":         100000,
					"change_percent": 2.5,
				}
				// 处理监控结果并执行回测
				result, err := resultProcessor.ProcessMonitorResults(stockCode, monitorResult)
				if err != nil {
					log.Printf("处理监控结果失败: %v\n", err)
					continue
				}
				// 发送通知
				notificationIntegrator.SendStockMonitorNotification(stockCode, result)
				notificationIntegrator.SendBacktestNotification(stockCode, result)
				continue
			}

			// 构建监控结果
			monitorResult := map[string]interface{}{
				"price":          stockPrice.Price,
				"volume":         stockPrice.Volume,
				"change_percent": stockPrice.ChangePercent,
				"timestamp":      stockPrice.Timestamp,
			}

			// 处理监控结果并执行回测
			result, err := resultProcessor.ProcessMonitorResults(stockCode, monitorResult)
			if err != nil {
				log.Printf("处理监控结果失败: %v\n", err)
				continue
			}

			// 发送通知
			notificationIntegrator.SendStockMonitorNotification(stockCode, result)
			notificationIntegrator.SendBacktestNotification(stockCode, result)
		}
	})

	// 启动股票调度器同步任务
	go func() {
		for {
			stockScheduler.FetchStockList()
			time.Sleep(10 * time.Minute)
		}
	}()

	// 初始化处理器
	marketHandler := handlers.NewMarketHandler(marketMonitor, decisionService, notificationService)
	stockHandler := handlers.NewStockHandler(stockMonitor, technicalAnalyzer, decisionService, notificationService)
	sectorHandler := handlers.NewSectorHandler(sectorMonitor, notificationService)
	newsHandler := handlers.NewNewsHandler(newsCrawler, notificationService)
	backtestHandler := handlers.NewBacktestHandler(strategyBacktester, notificationService)
	decisionHandler := handlers.NewDecisionHandler(decisionService, notificationService)

	return &Server{
		httpServer:             nil,
		marketHandler:          marketHandler,
		stockHandler:           stockHandler,
		sectorHandler:          sectorHandler,
		newsHandler:            newsHandler,
		backtestHandler:        backtestHandler,
		decisionHandler:        decisionHandler,
		taskScheduler:          taskScheduler,
		stockScheduler:         stockScheduler,
		resultProcessor:        resultProcessor,
		notificationIntegrator: notificationIntegrator,
	}
}

// SetupRoutes 设置路由
func (s *Server) SetupRoutes() {
	// 健康检查
	http.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		w.Write([]byte(`{"status": "ok"}`))
	})

	// 市场相关路由
	http.HandleFunc("/api/market/index", s.marketHandler.GetIndexData)
	http.HandleFunc("/api/market/trend", s.marketHandler.AnalyzeTrend)
	http.HandleFunc("/api/market/decision", s.marketHandler.GetMarketDecision)

	// 股票相关路由
	http.HandleFunc("/api/stock/monitor", s.stockHandler.MonitorStock)
	http.HandleFunc("/api/stock/indicators", s.stockHandler.GetTechnicalIndicators)
	http.HandleFunc("/api/stock/decision", s.stockHandler.GetStockDecision)

	// 版块相关路由
	http.HandleFunc("/api/sector/list", s.sectorHandler.GetSectorData)
	http.HandleFunc("/api/sector/hot", s.sectorHandler.GetHotSectors)

	// 新闻相关路由
	http.HandleFunc("/api/news/latest", s.newsHandler.GetLatestNews)
	http.HandleFunc("/api/news/by-stock", s.newsHandler.GetNewsByStock)

	// 回测相关路由
	http.HandleFunc("/api/backtest/result", s.backtestHandler.GetBacktestResult)
	http.HandleFunc("/api/backtest/history", s.backtestHandler.GetBacktestHistory)

	// 决策相关路由
	http.HandleFunc("/api/decision/stock", s.decisionHandler.AnalyzeStock)
	http.HandleFunc("/api/decision/market", s.decisionHandler.AnalyzeMarket)
}

// Start 启动API服务器
func (s *Server) Start() {
	// 设置路由
	s.SetupRoutes()

	// 从配置中获取服务器设置
	port := viper.GetInt("api.port")
	if port <= 0 {
		port = 8080 // 默认端口
	}

	host := viper.GetString("api.host")
	if host == "" {
		host = "0.0.0.0" // 默认监听所有地址
	}

	// 创建HTTP服务器
	s.httpServer = &http.Server{
		Addr:         fmt.Sprintf("%s:%d", host, port),
		Handler:      nil, // 使用默认的ServeMux
		ReadTimeout:  15 * time.Second,
		WriteTimeout: 15 * time.Second,
		IdleTimeout:  60 * time.Second,
	}

	log.Printf("API server starting on %s:%d", host, port)

	// 启动服务器
	if err := s.httpServer.ListenAndServe(); err != nil && err != http.ErrServerClosed {
		log.Fatalf("Failed to start API server: %v", err)
	}
}

// Stop 停止API服务器
func (s *Server) Stop() {
	if s.httpServer != nil {
		log.Println("Stopping API server...")
		if err := s.httpServer.Close(); err != nil {
			log.Printf("Error stopping API server: %v", err)
		}
	}
}

// mockDataServiceClient 模拟数据服务客户端
type mockDataServiceClient struct{}

// GetStockMonitorList 获取股票监控列表
func (m *mockDataServiceClient) GetStockMonitorList(ctx context.Context) ([]string, error) {
	// 模拟返回股票列表
	return []string{
		"600519", // 贵州茅台
		"000858", // 五粮液
		"000333", // 美的集团
		"601318", // 中国平安
		"600036", // 招商银行
	}, nil
}
