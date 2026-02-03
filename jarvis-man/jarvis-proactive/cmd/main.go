package main

import (
	"fmt"
	"log"
	"os"
	"os/signal"
	"syscall"

	"github.com/joho/godotenv"
	"github.com/skyeai/jarvis-proactive/pkg/api"
	"github.com/skyeai/jarvis-proactive/pkg/decision"
	"github.com/skyeai/jarvis-proactive/pkg/execution"
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
		Enabled         bool     `yaml:"enabled"`
		WatchPaths      []string `yaml:"watch_paths"`
		FileExtensions  []string `yaml:"file_extensions"`
		PollInterval    int      `yaml:"poll_interval"`
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
		Enabled         bool     `yaml:"enabled"`
		AllowedCommands []string `yaml:"allowed_commands"`
		MaxExecutionTime int     `yaml:"max_execution_time"`
		WorkingDirectory string  `yaml:"working_directory"`
	} `yaml:"execution"`

	Skeleton struct {
		Enabled        bool `yaml:"enabled"`
		MessageBufferSize int `yaml:"message_buffer_size"`
		MaxWorkers     int  `yaml:"max_workers"`
	} `yaml:"skeleton"`

	GRPC struct {
		Enabled bool   `yaml:"enabled"`
		Port    int    `yaml:"port"`
		Host    string `yaml:"host"`
	} `yaml:"grpc"`

	Nacos struct {
		Enabled      bool   `yaml:"enabled"`
		ServerAddr   string `yaml:"server_addr"`
		NamespaceID  string `yaml:"namespace_id"`
		Group        string `yaml:"group"`
		ServiceName  string `yaml:"service_name"`
		ClusterName  string `yaml:"cluster_name"`
	} `yaml:"nacos"`

	Logging struct {
		Level     string `yaml:"level"`
		File      string `yaml:"file"`
		MaxSize   int    `yaml:"max_size"`
		MaxBackups int    `yaml:"max_backups"`
		MaxAge    int    `yaml:"max_age"`
	} `yaml:"logging"`
}

func main() {
	// 加载环境变量
	if err := godotenv.Load(); err != nil {
		log.Println("No .env file found")
	}

	// 加载配置文件
	config, err := loadConfig("config.toml")
	if err != nil {
		log.Fatalf("Failed to load config: %v", err)
	}

	// 初始化消息总线
	messageBus := skeleton.NewMessageBus(config.Skeleton.MessageBufferSize)
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
	decisionService := decision.NewDecisionService(
		config.Decision.OpenAIAPIKey,
		config.Decision.OpenAIBaseURL,
		config.Decision.Model,
		config.Decision.Temperature,
		config.Decision.MaxTokens,
		messageBus,
		config.Decision.Enabled,
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

	// 4. API服务
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
