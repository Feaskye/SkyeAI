package scheduler

import (
	"context"
	"log"
	"strings"
	"time"

	"github.com/redis/go-redis/v9"
)

// StockScheduler 股票监控调度器
type StockScheduler struct {
	dataServiceClient DataServiceClient
	redisClient       *redis.Client
	stockList         []string
	lastSyncTime      time.Time
	ctx               context.Context
}

// DataServiceClient 数据服务客户端接口
type DataServiceClient interface {
	GetStockMonitorList(ctx context.Context) ([]string, error)
}

// NewStockScheduler 创建新的股票监控调度器
func NewStockScheduler(dataServiceClient DataServiceClient, redisClient *redis.Client) *StockScheduler {
	return &StockScheduler{
		dataServiceClient: dataServiceClient,
		redisClient:       redisClient,
		stockList:         make([]string, 0),
		lastSyncTime:      time.Time{},
		ctx:               context.Background(),
	}
}

// FetchStockList 从数据服务拉取股票监控列表
func (ss *StockScheduler) FetchStockList() ([]string, error) {
	// 尝试从数据服务拉取
	stocks, err := ss.dataServiceClient.GetStockMonitorList(ss.ctx)
	if err != nil {
		log.Printf("从数据服务拉取股票列表失败: %v, 尝试从Redis获取缓存数据\n", err)
		// 从Redis获取缓存数据
		cachedStocks, err := ss.getCachedStockList()
		if err != nil {
			log.Printf("从Redis获取缓存数据失败: %v, 使用本地缓存\n", err)
			// 使用本地缓存
			return ss.stockList, nil
		}
		return cachedStocks, nil
	}

	// 更新本地缓存和Redis缓存
	ss.stockList = stocks
	ss.lastSyncTime = time.Now()
	ss.cacheStockList(stocks)

	log.Printf("成功从数据服务拉取股票列表，共 %d 只股票\n", len(stocks))
	return stocks, nil
}

// getCachedStockList 从Redis获取缓存的股票列表
func (ss *StockScheduler) getCachedStockList() ([]string, error) {
	val, err := ss.redisClient.Get(ss.ctx, "stock:monitor:list").Result()
	if err != nil {
		return nil, err
	}

	// 这里简化处理，实际应该解析JSON
	// 假设val是逗号分隔的股票代码
	var stocks []string
	if val != "" {
		// 分割逗号分隔的股票代码字符串
		stocks = strings.Split(val, ",")
	}
	return stocks, nil
}

// cacheStockList 将股票列表缓存到Redis
func (ss *StockScheduler) cacheStockList(stocks []string) error {
	// 这里简化处理，实际应该序列化为JSON
	// 假设stocks是股票代码列表，转换为逗号分隔的字符串
	stockStr := ""
	for i, stock := range stocks {
		if i > 0 {
			stockStr += ","
		}
		stockStr += stock
	}

	err := ss.redisClient.Set(ss.ctx, "stock:monitor:list", stockStr, 24*time.Hour).Err()
	if err != nil {
		log.Printf("缓存股票列表到Redis失败: %v\n", err)
		return err
	}

	log.Printf("股票列表已缓存到Redis\n")
	return nil
}

// GetStockList 获取当前股票列表
func (ss *StockScheduler) GetStockList() []string {
	return ss.stockList
}

// GetLastSyncTime 获取最后同步时间
func (ss *StockScheduler) GetLastSyncTime() time.Time {
	return ss.lastSyncTime
}

// SyncStockData 同步股票数据
func (ss *StockScheduler) SyncStockData() error {
	_, err := ss.FetchStockList()
	if err != nil {
		return err
	}
	return nil
}
