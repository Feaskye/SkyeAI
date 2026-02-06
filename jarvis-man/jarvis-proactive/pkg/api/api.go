package api

import (
	"log"
	"net/http"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/skyeai/jarvis-proactive/pkg/skeleton"
)

// APIService API服务
type APIService struct {
	router     *gin.Engine
	host       string
	port       int
	messageBus *skeleton.MessageBus
	enabled    bool
}

// NewAPIService 创建API服务实例
func NewAPIService(host string, port int, messageBus *skeleton.MessageBus, enabled bool) *APIService {
	return &APIService{
		router:     gin.Default(),
		host:       host,
		port:       port,
		messageBus: messageBus,
		enabled:    enabled,
	}
}

// Start 启动API服务
func (as *APIService) Start() {
	if !as.enabled {
		log.Println("API service is disabled")
		return
	}

	log.Println("Starting API service...")

	// 设置路由
	as.setupRoutes()

	// 启动HTTP服务器
	serverAddr := as.host + ":" + strconv.Itoa(as.port)
	log.Printf("API service listening on %s", serverAddr)

	// 注意：这会阻塞当前goroutine
	if err := as.router.Run(serverAddr); err != nil {
		log.Fatalf("Failed to start API server: %v", err)
	}
}

// Stop 停止API服务
func (as *APIService) Stop() {
	if !as.enabled {
		return
	}

	log.Println("Stopping API service...")
	// Gin的Run方法会阻塞，所以这里实际上不会被调用
	// 实际应用中可能需要使用更复杂的服务器管理
	log.Println("API service stopped")
}

// setupRoutes 设置路由
func (as *APIService) setupRoutes() {
	// 健康检查
	as.router.GET("/health", as.healthCheck)

	// 状态查询
	as.router.GET("/status", as.getStatus)

	// 触发感知
	as.router.POST("/perception/trigger", as.triggerPerception)

	// 执行命令
	as.router.POST("/execution/command", as.executeCommand)

	// 决策查询
	as.router.GET("/decision/history", as.getDecisionHistory)
}

// healthCheck 健康检查
func (as *APIService) healthCheck(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{
		"status":    "healthy",
		"timestamp": time.Now(),
		"service":   "jarvis-proactive",
	})
}

// getStatus 获取状态
func (as *APIService) getStatus(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{
		"status":    "running",
		"timestamp": time.Now(),
		"service":   "jarvis-proactive",
		"version":   "1.0.0",
	})
}

// triggerPerception 触发感知
func (as *APIService) triggerPerception(c *gin.Context) {
	var request struct {
		Path string `json:"path" binding:"required"`
	}

	if err := c.ShouldBindJSON(&request); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// 发送感知触发消息
	message := skeleton.Message{
		Type:      "perception_trigger",
		Source:    "api",
		Timestamp: time.Now(),
		Data: map[string]interface{}{
			"path":      request.Path,
			"timestamp": time.Now(),
		},
	}

	as.messageBus.SendMessage(message)

	c.JSON(http.StatusOK, gin.H{
		"status":    "triggered",
		"timestamp": time.Now(),
		"path":      request.Path,
	})
}

// executeCommand 执行命令
func (as *APIService) executeCommand(c *gin.Context) {
	var request struct {
		Command string   `json:"command" binding:"required"`
		Args    []string `json:"args"`
	}

	if err := c.ShouldBindJSON(&request); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// 发送命令执行消息
	message := skeleton.Message{
		Type:      "command_execution",
		Source:    "api",
		Timestamp: time.Now(),
		Data: map[string]interface{}{
			"command":   request.Command,
			"args":      request.Args,
			"timestamp": time.Now(),
		},
	}

	as.messageBus.SendMessage(message)

	c.JSON(http.StatusOK, gin.H{
		"status":    "executed",
		"timestamp": time.Now(),
		"command":   request.Command,
		"args":      request.Args,
	})
}

// getDecisionHistory 获取决策历史
func (as *APIService) getDecisionHistory(c *gin.Context) {
	// 这里可以实现获取决策历史的逻辑
	// 实际应用中可能需要从数据库或缓存中查询

	c.JSON(http.StatusOK, gin.H{
		"status":    "success",
		"timestamp": time.Now(),
		"history":   []interface{}{}, // 空历史
	})
}
