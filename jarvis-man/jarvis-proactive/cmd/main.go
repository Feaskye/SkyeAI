package main

import (
	"fmt"
	"log"
	"os"
	"os/signal"
	"syscall"

	"github.com/joho/godotenv"
	"github.com/skyeai/jarvis-proactive/pkg/api"
	"github.com/skyeai/jarvis-proactive/pkg/cache"
	"github.com/skyeai/jarvis-proactive/pkg/decision"
	"github.com/skyeai/jarvis-proactive/pkg/execution"
	"github.com/skyeai/jarvis-proactive/pkg/llm"
	"github.com/skyeai/jarvis-proactive/pkg/mq"
	"github.com/skyeai/jarvis-proactive/pkg/notification"
	"github.com/skyeai/jarvis-proactive/pkg/perception"
	"github.com/skyeai/jarvis-proactive/pkg/skeleton"
	"gopkg.in/yaml.v3"
)

// Config 配置结构
type Config struct {
	Server struct {
		Port int    `yaml:"port"`
		Host string `yaml:"host"`
	} `yaml:"server"`

	Perception struct {
		Enabled        bool     `yaml:"enabled"`
		WatchPaths     []string `yaml:"watch_paths"`
		FileExtensions []string `yaml:"file_extensions"`
		PollInterval   int      `yaml:"poll_interval"`
	} `yaml:"perception"`

	Decision struct {
		Enabled       bool    `yaml:"enabled"`
		Model         string  `yaml:"model"`
		Temperature   float32 `yaml:"temperature"`
		MaxTokens     int     `yaml:"max_tokens"`
		OpenAIAPIKey  string  `yaml:"openai_api_key"`
		OpenAIBaseURL string  `yaml:"openai_base_url"`
	} `yaml:"decision"`

	Execution struct {
		Enabled          bool     `yaml:"enabled"`
		AllowedCommands  []string `yaml:"allowed_commands"`
		MaxExecutionTime int      `yaml:"max_execution_time"`
		WorkingDirectory string   `yaml:"working_directory"`
	} `yaml:"execution"`

	Skeleton struct {
		Enabled           bool `yaml:"enabled"`
		MessageBufferSize int  `yaml:"message_buffer_size"`
		MaxWorkers        int  `yaml:"max_workers"`
	} `yaml:"skeleton"`

	GRPC struct {
		Enabled bool   `yaml:"enabled"`
		Port    int    `yaml:"port"`
		Host    string `yaml:"host"`
	} `yaml:"grpc"`

	Nacos struct {
		Enabled     bool   `yaml:"enabled"`
		ServerAddr  string `yaml:"server_addr"`
		NamespaceID string `yaml:"namespace_id"`
		Group       string `yaml:"group"`
		ServiceName string `yaml:"service_name"`
		ClusterName string `yaml:"cluster_name"`
	} `yaml:"nacos"`

	Logging struct {
		Level      string `yaml:"level"`
		File       string `yaml:"file"`
		MaxSize    int    `yaml:"max_size"`
		MaxBackups int    `yaml:"max_backups"`
		MaxAge     int    `yaml:"max_age"`
	} `yaml:"logging"`

	RabbitMQ struct {
		Enabled      bool   `yaml:"enabled"`
		Host         string `yaml:"host"`
		Port         int    `yaml:"port"`
		Username     string `yaml:"username"`
		Password     string `yaml:"password"`
		VirtualHost  string `yaml:"virtual_host"`
		Exchange     string `yaml:"exchange"`
		ExchangeType string `yaml:"exchange_type"`
		QueueName    string `yaml:"queue_name"`
		RoutingKey   string `yaml:"routing_key"`
	} `yaml:"rabbitmq"`

	Redis struct {
		Enabled     bool   `yaml:"enabled"`
		Host        string `yaml:"host"`
		Port        int    `yaml:"port"`
		Password    string `yaml:"password"`
		DB          int    `yaml:"db"`
		CacheExpiry int    `yaml:"cache_expiry"` // 缓存过期时间（分钟）
	} `yaml:"redis"`

	LLM struct {
		Enabled     bool   `yaml:"enabled"`
		ServiceAddr string `yaml:"service_addr"`
		GRPCPort    int    `yaml:"grpc_port"`
		APIKey      string `yaml:"api_key"`
		Timeout     int    `yaml:"timeout"` // 超时时间（秒）
	} `yaml:"llm"`

	DecisionControl struct {
		Enabled               bool `yaml:"enabled"`
		MaxIntervalMinutes    int  `yaml:"max_interval_minutes"`    // 最大交互间隔（分钟）
		MaxConcurrentRequests int  `yaml:"max_concurrent_requests"` // 最大并发请求数
	} `yaml:"decision_control"`
}

func main() {
	// 加载环境变量
	if err := godotenv.Load(); err != nil {
		log.Println("No .env file found")
	}

	// 加载配置文件
	config, err := loadConfig("config.yaml")
	if err != nil {
		log.Fatalf("Failed to load config: %v", err)
	}

	// 初始化外部服务客户端

	// 1. 初始化RabbitMQ客户端
	var rabbitMQClient *mq.RabbitMQClient
	if config.RabbitMQ.Enabled {
		rabbitMQConfig := mq.RabbitMQConfig{
			Host:         config.RabbitMQ.Host,
			Port:         config.RabbitMQ.Port,
			Username:     config.RabbitMQ.Username,
			Password:     config.RabbitMQ.Password,
			VirtualHost:  config.RabbitMQ.VirtualHost,
			Exchange:     config.RabbitMQ.Exchange,
			ExchangeType: config.RabbitMQ.ExchangeType,
			QueueName:    config.RabbitMQ.QueueName,
			RoutingKey:   config.RabbitMQ.RoutingKey,
		}
		rabbitMQClient = mq.NewRabbitMQClient(rabbitMQConfig)
		if err := rabbitMQClient.Connect(); err != nil {
			log.Printf("Warning: Failed to connect to RabbitMQ: %v", err)
			// 继续执行，不终止程序
		}
	}

	// 2. 初始化Redis客户端
	var redisClient *cache.RedisClient
	if config.Redis.Enabled {
		redisConfig := cache.RedisConfig{
			Host:        config.Redis.Host,
			Port:        config.Redis.Port,
			Password:    config.Redis.Password,
			DB:          config.Redis.DB,
			CacheExpiry: config.Redis.CacheExpiry,
		}
		redisClient = cache.NewRedisClient(redisConfig)
		if err := redisClient.Connect(); err != nil {
			log.Printf("Warning: Failed to connect to Redis: %v", err)
			// 继续执行，不终止程序
		}
	}

	// 3. 初始化LLM客户端
	var llmClient *llm.LLMClient
	if config.LLM.Enabled {
		llmConfig := llm.LLMConfig{
			ServiceAddr: config.LLM.ServiceAddr,
			GRPCPort:    config.LLM.GRPCPort,
			APIKey:      config.LLM.APIKey,
			Timeout:     config.LLM.Timeout,
		}
		llmClient = llm.NewLLMClient(llmConfig)
		if err := llmClient.Connect(); err != nil {
			log.Printf("Warning: Failed to connect to LLM service: %v", err)
			// 继续执行，不终止程序
		}
	}

	// 初始化消息总线
	messageBus := skeleton.NewMessageBusWithExternal(config.Skeleton.MessageBufferSize, rabbitMQClient, redisClient)
	messageBus.Start()

	// 初始化服务管理器
	serviceManager := skeleton.NewServiceManager()

	// 初始化各服务

	// 1. 感知服务
	perceptionService, err := perception.NewPerceptionService(
		config.Perception.WatchPaths,
		config.Perception.FileExtensions,
		config.Perception.PollInterval,
		messageBus,
		config.Perception.Enabled,
	)
	if err != nil {
		log.Fatalf("Failed to create perception service: %v", err)
	}
	serviceManager.AddService(perceptionService)

	// 2. 决策服务
	maxIntervalMinutes := 5 // 默认5分钟
	if config.DecisionControl.Enabled {
		maxIntervalMinutes = config.DecisionControl.MaxIntervalMinutes
	}
	decisionService := decision.NewDecisionService(
		llmClient,
		redisClient,
		config.Decision.Model,
		config.Decision.Temperature,
		config.Decision.MaxTokens,
		messageBus,
		config.Decision.Enabled,
		maxIntervalMinutes,
	)
	serviceManager.AddService(decisionService)

	// 3. 执行服务
	executionService := execution.NewExecutionService(
		config.Execution.AllowedCommands,
		config.Execution.MaxExecutionTime,
		config.Execution.WorkingDirectory,
		messageBus,
		config.Execution.Enabled,
	)
	serviceManager.AddService(executionService)

	// 4. 通知服务
	notificationService := notification.NewNotificationService(
		messageBus,
		rabbitMQClient,
		config.RabbitMQ.Enabled,
	)
	serviceManager.AddService(notificationService)

	// 5. API服务
	apiService := api.NewAPIService(
		config.Server.Host,
		config.Server.Port,
		messageBus,
		true, // API服务默认启用
	)
	serviceManager.AddService(apiService)

	// 启动所有服务
	serviceManager.StartAll()

	// 等待中断信号
	waitForInterrupt()

	// 停止所有服务
	serviceManager.StopAll()
	messageBus.Stop()

	// 关闭外部服务连接
	if rabbitMQClient != nil {
		rabbitMQClient.Close()
	}
	if redisClient != nil {
		redisClient.Close()
	}
	if llmClient != nil {
		llmClient.Close()
	}

	log.Println("Jarvis Proactive Service stopped")
}

// loadConfig 加载配置文件
func loadConfig(filePath string) (*Config, error) {
	file, err := os.Open(filePath)
	if err != nil {
		return nil, fmt.Errorf("failed to open config file: %w", err)
	}
	defer file.Close()

	var config Config
	if err := yaml.NewDecoder(file).Decode(&config); err != nil {
		return nil, fmt.Errorf("failed to decode config: %w", err)
	}

	return &config, nil
}

// waitForInterrupt 等待中断信号
func waitForInterrupt() {
	sigChan := make(chan os.Signal, 1)
	signal.Notify(sigChan, syscall.SIGINT, syscall.SIGTERM)
	<-sigChan
	log.Println("Received interrupt signal, shutting down...")
}
