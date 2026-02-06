package main

import (
	"fmt"
	"log"
	"os"
	"path/filepath"

	"jarvis-finance-go/api"

	"github.com/spf13/viper"
)

func main() {
	// 初始化配置
	if err := initConfig(); err != nil {
		log.Fatalf("Failed to initialize config: %v", err)
	}

	// 初始化日志
	initLogger()

	// 初始化API服务器
	apiServer := api.NewServer()

	// 启动API服务器
	apiServer.Start()
}

// initConfig 初始化配置
func initConfig() error {
	// 设置默认配置文件路径
	configDir := "config"
	configFile := "config.yaml"

	// 检查配置文件是否存在
	configPath := filepath.Join(configDir, configFile)
	if _, err := os.Stat(configPath); os.IsNotExist(err) {
		return fmt.Errorf("config file not found: %s", configPath)
	}

	// 加载配置文件
	viper.SetConfigName("config")
	viper.SetConfigType("yaml")
	viper.AddConfigPath(configDir)

	if err := viper.ReadInConfig(); err != nil {
		return fmt.Errorf("failed to read config file: %v", err)
	}

	return nil
}

// initLogger 初始化日志
func initLogger() {
	// 创建日志目录
	logDir := "logs"
	if _, err := os.Stat(logDir); os.IsNotExist(err) {
		if err := os.Mkdir(logDir, 0755); err != nil {
			log.Printf("Warning: Failed to create log directory: %v", err)
		}
	}

	// 设置日志文件
	logFile := viper.GetString("log.file")
	if logFile != "" {
		f, err := os.OpenFile(logFile, os.O_RDWR|os.O_CREATE|os.O_APPEND, 0666)
		if err != nil {
			log.Printf("Warning: Failed to open log file: %v", err)
		} else {
			log.SetOutput(f)
		}
	}
}
