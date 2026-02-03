package main

import (
	"fmt"
	"log"
	"os"
	"os/signal"
	"syscall"

	mqtt "github.com/mochi-co/mqtt/server"
	"github.com/mochi-co/mqtt/server/listeners"
)

func main() {
	// 创建MQTT服务器实例
	srv := mqtt.New()

	// 创建TCP监听器，监听9003端口
	tcp := listeners.NewTCP("tcp", fmt.Sprintf(":%s", getEnv("MQTT_PORT", "9003")))
	// 配置监听器，使用默认认证器，允许匿名访问
	if err := srv.AddListener(tcp, &listeners.Config{}); err != nil {
		log.Fatalf("Failed to add TCP listener: %v", err)
	}

	// 创建WebSocket监听器，监听9004端口
	ws := listeners.NewWebsocket("ws", fmt.Sprintf(":%s", getEnv("MQTT_WS_PORT", "9004")))
	// 配置监听器，使用默认认证器，允许匿名访问
	if err := srv.AddListener(ws, &listeners.Config{}); err != nil {
		log.Fatalf("Failed to add WebSocket listener: %v", err)
	}

	// 启动服务器
	go func() {
		if err := srv.Serve(); err != nil {
			log.Fatalf("Failed to start MQTT server: %v", err)
		}
	}()

	log.Printf("Mock MQTT server started, TCP port: %s, WebSocket port: %s", 
		getEnv("MQTT_PORT", "9003"), 
		getEnv("MQTT_WS_PORT", "9004"))

	// 等待中断信号
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	log.Println("Shutting down MQTT server...")

	// 停止服务器
	if err := srv.Close(); err != nil {
		log.Fatalf("Failed to shutdown MQTT server: %v", err)
	}

	log.Println("MQTT server stopped successfully")
}

// 获取环境变量，如果不存在则返回默认值
func getEnv(key, defaultValue string) string {
	value := os.Getenv(key)
	if value == "" {
		return defaultValue
	}
	return value
}
