package cache

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"sync"
	"time"

	"github.com/go-redis/redis/v8"
)

// RedisConfig Redis配置

type RedisConfig struct {
	Host        string
	Port        int
	Password    string
	DB          int
	CacheExpiry int // 缓存过期时间（分钟）
}

// RedisClient Redis客户端

type RedisClient struct {
	config      RedisConfig
	client      *redis.Client
	mutex       sync.RWMutex
	isConnected bool
	ctx         context.Context
}

// NewRedisClient 创建Redis客户端实例

func NewRedisClient(config RedisConfig) *RedisClient {
	return &RedisClient{
		config:      config,
		isConnected: false,
		ctx:         context.Background(),
	}
}

// Connect 连接Redis

func (r *RedisClient) Connect() error {
	r.mutex.Lock()
	defer r.mutex.Unlock()

	if r.isConnected {
		return nil
	}

	r.client = redis.NewClient(&redis.Options{
		Addr:     fmt.Sprintf("%s:%d", r.config.Host, r.config.Port),
		Password: r.config.Password,
		DB:       r.config.DB,
	})

	// 测试连接
	_, err := r.client.Ping(r.ctx).Result()
	if err != nil {
		return fmt.Errorf("failed to connect to redis: %w", err)
	}

	r.isConnected = true
	log.Println("Redis connected successfully")
	return nil
}

// Close 关闭Redis连接

func (r *RedisClient) Close() error {
	r.mutex.Lock()
	defer r.mutex.Unlock()

	if !r.isConnected {
		return nil
	}

	if r.client != nil {
		if err := r.client.Close(); err != nil {
			log.Printf("Error closing redis connection: %v", err)
		}
	}

	r.isConnected = false
	log.Println("Redis connection closed")
	return nil
}

// Set 设置缓存

func (r *RedisClient) Set(key string, value interface{}, expiry ...time.Duration) error {
	r.mutex.RLock()
	defer r.mutex.RUnlock()

	if !r.isConnected {
		return fmt.Errorf("redis not connected")
	}

	// 序列化值
	var err error
	var data []byte

	switch v := value.(type) {
	case string:
		data = []byte(v)
	case []byte:
		data = v
	default:
		data, err = json.Marshal(value)
		if err != nil {
			return fmt.Errorf("failed to marshal value: %w", err)
		}
	}

	// 设置过期时间
	exp := time.Duration(r.config.CacheExpiry) * time.Minute
	if len(expiry) > 0 {
		exp = expiry[0]
	}

	// 设置缓存
	if err := r.client.Set(r.ctx, key, data, exp).Err(); err != nil {
		return fmt.Errorf("failed to set cache: %w", err)
	}

	log.Printf("Cache set: %s, expiry: %v", key, exp)
	return nil
}

// Get 获取缓存

func (r *RedisClient) Get(key string, dest interface{}) error {
	r.mutex.RLock()
	defer r.mutex.RUnlock()

	if !r.isConnected {
		return fmt.Errorf("redis not connected")
	}

	// 获取缓存
	data, err := r.client.Get(r.ctx, key).Bytes()
	if err != nil {
		if err == redis.Nil {
			return fmt.Errorf("cache miss")
		}
		return fmt.Errorf("failed to get cache: %w", err)
	}

	// 反序列化值
	switch v := dest.(type) {
	case *string:
		*v = string(data)
	case *[]byte:
		*v = data
	default:
		if err := json.Unmarshal(data, dest); err != nil {
			return fmt.Errorf("failed to unmarshal value: %w", err)
		}
	}

	log.Printf("Cache hit: %s", key)
	return nil
}

// Delete 删除缓存

func (r *RedisClient) Delete(key string) error {
	r.mutex.RLock()
	defer r.mutex.RUnlock()

	if !r.isConnected {
		return fmt.Errorf("redis not connected")
	}

	// 删除缓存
	if err := r.client.Del(r.ctx, key).Err(); err != nil {
		return fmt.Errorf("failed to delete cache: %w", err)
	}

	log.Printf("Cache deleted: %s", key)
	return nil
}

// Exists 检查缓存是否存在

func (r *RedisClient) Exists(key string) (bool, error) {
	r.mutex.RLock()
	defer r.mutex.RUnlock()

	if !r.isConnected {
		return false, fmt.Errorf("redis not connected")
	}

	// 检查缓存
	result, err := r.client.Exists(r.ctx, key).Result()
	if err != nil {
		return false, fmt.Errorf("failed to check cache existence: %w", err)
	}

	return result > 0, nil
}

// IsConnected 检查是否连接

func (r *RedisClient) IsConnected() bool {
	r.mutex.RLock()
	defer r.mutex.RUnlock()
	return r.isConnected
}

// CheckRateLimit 检查频率限制

func (r *RedisClient) CheckRateLimit(key string, maxRequests int, interval time.Duration) (bool, error) {
	r.mutex.RLock()
	defer r.mutex.RUnlock()

	if !r.isConnected {
		return false, fmt.Errorf("redis not connected")
	}

	// 使用滑动窗口计数器
	currentTime := time.Now().UnixNano()
	windowStart := currentTime - int64(interval)

	// 移除窗口外的记录
	if err := r.client.ZRemRangeByScore(r.ctx, key, "0", fmt.Sprintf("%d", windowStart)).Err(); err != nil {
		return false, fmt.Errorf("failed to remove old records: %w", err)
	}

	// 获取当前窗口内的请求数
	count, err := r.client.ZCard(r.ctx, key).Result()
	if err != nil {
		return false, fmt.Errorf("failed to get request count: %w", err)
	}

	// 检查是否超过限制
	if count >= int64(maxRequests) {
		log.Printf("Rate limit exceeded for %s: %d/%d", key, count, maxRequests)
		return false, nil
	}

	// 添加当前请求
	if err := r.client.ZAdd(r.ctx, key, &redis.Z{
		Score:  float64(currentTime),
		Member: fmt.Sprintf("%d", currentTime),
	}).Err(); err != nil {
		return false, fmt.Errorf("failed to add request: %w", err)
	}

	// 设置键过期时间
	if err := r.client.Expire(r.ctx, key, interval+time.Second).Err(); err != nil {
		return false, fmt.Errorf("failed to set expiry: %w", err)
	}

	log.Printf("Rate limit check passed for %s: %d/%d", key, count+1, maxRequests)
	return true, nil
}

// CheckDecisionRateLimit 检查决策服务频率限制

func (r *RedisClient) CheckDecisionRateLimit(maxIntervalMinutes int) (bool, error) {
	key := "decision:rate_limit"
	// 每maxIntervalMinutes分钟最多1次请求
	interval := time.Duration(maxIntervalMinutes) * time.Minute
	return r.CheckRateLimit(key, 1, interval)
}

// Reconnect 重连Redis

func (r *RedisClient) Reconnect() error {
	log.Println("Attempting to reconnect to Redis...")

	// 关闭现有连接
	if err := r.Close(); err != nil {
		log.Printf("Error closing existing connection: %v", err)
	}

	// 尝试重连
	var lastErr error
	for i := 0; i < 5; i++ {
		err := r.Connect()
		if err == nil {
			log.Println("Redis reconnected successfully")
			return nil
		}

		lastErr = err
		log.Printf("Reconnection attempt %d failed: %v", i+1, err)
		time.Sleep(time.Duration(i+1) * time.Second)
	}

	return fmt.Errorf("failed to reconnect after 5 attempts: %w", lastErr)
}
