package main

import (
	"context"
	"fmt"
	"log"
	"net"
	"net/http"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/pelletier/go-toml/v2"
	"github.com/skyeai/jarvis-edge/pkg/api"
	"github.com/skyeai/jarvis-edge/pkg/edge"
	"github.com/skyeai/jarvis-edge/pkg/mqtt"
	"github.com/skyeai/jarvis-edge/pkg/multimodal/input"
	"github.com/skyeai/jarvis-edge/pkg/multimodal/vision"
	"github.com/skyeai/jarvis-edge/pkg/multimodal/voice"
	"google.golang.org/grpc"
)

// 配置结构体
type Config struct {
	Service struct {
		Name    string `toml:"NAME"`
		Version string `toml:"VERSION"`
		UserID  string `toml:"USER_ID"`
	} `toml:"SERVICE"`
	Port struct {
		GRPCPort      string `toml:"GRPC_PORT"`
		HTTPPort      string `toml:"HTTP_PORT"`
		EdgeHTTPPort  string `toml:"EDGE_HTTP_PORT"`
	} `toml:"PORT"`
	MQTT struct {
		BrokerURL      string `toml:"BROKER_URL"`
		ClientID       string `toml:"CLIENT_ID"`
		Username       string `toml:"USERNAME"`
		Password       string `toml:"PASSWORD"`
		QoS            int    `toml:"QOS"`
		KeepAlive      int    `toml:"KEEP_ALIVE"`
		ConnectTimeout int    `toml:"CONNECT_TIMEOUT"`
		AutoReconnect  bool   `toml:"AUTO_RECONNECT"`
		CleanSession   bool   `toml:"CLEAN_SESSION"`
	} `toml:"MQTT"`
	Sensor struct {
		Enabled          bool   `toml:"ENABLED"`
		PollInterval     int    `toml:"POLL_INTERVAL"`
		SimulateData     bool   `toml:"SIMULATE_DATA"`
		SupportedSensors string `toml:"SUPPORTED_SENSORS"`
	} `toml:"SENSOR"`
	Nacos struct {
		Enabled     bool   `toml:"ENABLED"`
		ServerAddr  string `toml:"SERVER_ADDR"`
		NamespaceId string `toml:"NAMESPACE_ID"`
		GroupName   string `toml:"GROUP_NAME"`
		ServiceName string `toml:"SERVICE_NAME"`
		ClusterName string `toml:"CLUSTER_NAME"`
		Port        int    `toml:"PORT"`
		IP          string `toml:"IP"`
	} `toml:"NACOS"`
}

// 全局配置变量
var config Config

func main() {
	// 加载配置文件
	configFile := "config.toml"
	configData, err := os.ReadFile(configFile)
	if err != nil {
		log.Fatalf("Failed to read config file: %v", err)
	}

	// 解析TOML配置
	if err := toml.Unmarshal(configData, &config); err != nil {
		log.Fatalf("Failed to parse config file: %v", err)
	}

	// 设置日志格式
	log.SetFlags(log.LstdFlags | log.Lshortfile)

	// 创建上下文
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	// 初始化多模态模块
	multimodalInput := input.NewMultimodalInput()
	imageUnderstanding := vision.NewImageUnderstanding()
	voiceInteraction := voice.NewVoiceInteraction("models/whisper", "zh", "zh_CN-huayan-medium")

	// 启动gRPC服务器
	grpcServer := grpc.NewServer()
	api.RegisterEdgeServiceServer(grpcServer, &api.EdgeServiceImpl{})

	grpcListener, err := net.Listen("tcp", fmt.Sprintf(":%s", config.Port.GRPCPort))
	if err != nil {
		log.Fatalf("Failed to listen on gRPC port %s: %v", config.Port.GRPCPort, err)
	}

	go func() {
		log.Printf("gRPC server starting on port %s...", config.Port.GRPCPort)
		if err := grpcServer.Serve(grpcListener); err != nil {
			log.Fatalf("Failed to start gRPC server: %v", err)
		}
	}()

	// 初始化边缘节点服务配置
	edgeConfig := edge.Config{
		Name:    config.Service.Name,
		Version: config.Service.Version,
		UserID:  config.Service.UserID,
		MQTTConfig: mqtt.Config{
			BrokerURL:      config.MQTT.BrokerURL,
			ClientID:       config.MQTT.ClientID,
			Username:       config.MQTT.Username,
			Password:       config.MQTT.Password,
			QoS:            config.MQTT.QoS,
			KeepAlive:      time.Duration(config.MQTT.KeepAlive) * time.Second,
			ConnectTimeout: time.Duration(config.MQTT.ConnectTimeout) * time.Second,
			AutoReconnect:  config.MQTT.AutoReconnect,
			CleanSession:   config.MQTT.CleanSession,
		},
		SensorConfig: edge.SensorConfig{
			Enabled:          config.Sensor.Enabled,
			PollInterval:     time.Duration(config.Sensor.PollInterval) * time.Second,
			SimulateData:     config.Sensor.SimulateData,
			SupportedSensors: strings.Split(config.Sensor.SupportedSensors, ","),
		},
		HTTPConfig: edge.HTTPConfig{
			Enabled: true,
			Port:    config.Port.EdgeHTTPPort,
		},
		NacosConfig: edge.NacosConfig{
			Enabled:     config.Nacos.Enabled,
			ServerAddr:  config.Nacos.ServerAddr,
			NamespaceId: config.Nacos.NamespaceId,
			GroupName:   config.Nacos.GroupName,
			ServiceName: config.Nacos.ServiceName,
			ClusterName: config.Nacos.ClusterName,
			Port:        config.Nacos.Port,
			IP:          config.Nacos.IP,
		},
	}

	// 创建并启动边缘节点服务
	edgeService := edge.NewService(edgeConfig)
	if err := edgeService.Start(ctx); err != nil {
		log.Fatalf("Failed to start edge service: %v", err)
	}

	// 启动Gin HTTP服务器
	router := gin.Default()
	// 添加健康检查路由
	router.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{
			"status":  "ok",
			"service": "jarvis-edge",
			"version": "1.0.0",
		})
	})

	// 添加状态查询路由
	router.GET("/status", func(c *gin.Context) {
		// 获取边缘服务状态
		edgeStatus := edgeService.GetStatus()
		c.JSON(http.StatusOK, gin.H{
			"component":   "edge-agent",
			"status":      edgeStatus.Status,
			"version":     edgeStatus.Version,
			"mqtt_status": edgeStatus.MQTTStatus,
			"metrics": map[string]string{
				"grpc_connections": "0",
				"http_requests":    "0",
				"uptime":           fmt.Sprintf("%d", time.Now().Unix()),
			},
		})
	})

	// 新增健康数据API路由
	router.GET("/api/health-data/stats", func(c *gin.Context) {
		userID := c.DefaultQuery("user_id", "default-user")
		startTime := time.Now().Add(-24 * time.Hour)
		endTime := time.Now()

		stats, err := edgeService.GetHealthStats(ctx, userID, startTime, endTime)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to get health stats: " + err.Error()})
			return
		}

		c.JSON(http.StatusOK, gin.H{
			"status":  "success",
			"user_id": userID,
			"stats":   stats,
		})
	})

	// 新增IoT命令执行路由
	router.POST("/api/iot/command", func(c *gin.Context) {
		var command mqtt.IoTCommand
		if err := c.ShouldBindJSON(&command); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request: " + err.Error()})
			return
		}

		if err := edgeService.ExecuteIoTCommand(ctx, command); err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to execute IoT command: " + err.Error()})
			return
		}

		c.JSON(http.StatusOK, gin.H{
			"status":     "success",
			"command_id": command.CommandID,
		})
	})

	// 新增音频处理相关路由
	// 1. 语音识别API（用于处理前端发送的Base64编码音频）
	router.POST("/api/asr", func(c *gin.Context) {
		var request struct {
			Audio string `json:"audio" binding:"required"`
			Model string `json:"model" default:"whisper"`
		}

		if err := c.ShouldBindJSON(&request); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request: " + err.Error()})
			return
		}

		log.Printf("Received ASR request, audio length: %d chars, model: %s", len(request.Audio), request.Model)

		// 暂时返回模拟响应
		c.JSON(http.StatusOK, gin.H{
			"text":       "你好，贾维斯。",
			"model":      request.Model,
			"confidence": 0.95,
		})
	})

	// 2. TTS API（用于文本转语音）
	router.POST("/api/tts", func(c *gin.Context) {
		var request struct {
			Text  string `json:"text" binding:"required"`
			Voice string `json:"voice" default:"default"`
		}

		if err := c.ShouldBindJSON(&request); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request: " + err.Error()})
			return
		}

		log.Printf("Received TTS request, text: %s, voice: %s", request.Text, request.Voice)

		// 暂时返回模拟响应
		c.JSON(http.StatusOK, gin.H{
			"audio":    "base64-encoded-audio-data",
			"voice":    request.Voice,
			"format":   "wav",
			"duration": 2.5,
		})
	})

	// 3. 音频流WebSocket端点
	router.GET("/ws/audio", func(c *gin.Context) {
		// 这里应该实现WebSocket音频流处理
		c.JSON(http.StatusNotImplemented, gin.H{"error": "WebSocket audio streaming not implemented yet"})
	})

	// 新增阶段2：自动化执行引擎相关路由
	// 1. 沙箱执行器API（用于安全执行代码）
	router.POST("/api/sandbox", func(c *gin.Context) {
		var request struct {
			Code     string `json:"code" binding:"required"`
			Language string `json:"language" default:"python"`
			Timeout  int    `json:"timeout" default:"30"`
		}

		if err := c.ShouldBindJSON(&request); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request: " + err.Error()})
			return
		}

		log.Printf("Received sandbox request, language: %s, code length: %d chars", request.Language, len(request.Code))

		// 暂时返回模拟响应
		c.JSON(http.StatusOK, gin.H{
			"status":   "success",
			"output":   "代码执行成功，输出结果：Hello, World!",
			"language": request.Language,
			"duration": 1.23,
		})
	})

	// 2. 浏览器自动化API（用于控制无头浏览器）
	router.POST("/api/browser", func(c *gin.Context) {
		var request struct {
			Url    string            `json:"url" binding:"required"`
			Action string            `json:"action" default:"navigate"`
			Params map[string]string `json:"params"`
		}

		if err := c.ShouldBindJSON(&request); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request: " + err.Error()})
			return
		}

		log.Printf("Received browser request, action: %s, url: %s", request.Action, request.Url)

		// 暂时返回模拟响应
		c.JSON(http.StatusOK, gin.H{
			"status": "success",
			"action": request.Action,
			"url":    request.Url,
			"result": "浏览器操作成功完成",
			"title":  "示例网页标题",
			"html":   "<html><body>示例网页内容</body></html>",
		})
	})

	// 3. 浏览器截图API
	router.POST("/api/browser/screenshot", func(c *gin.Context) {
		var request struct {
			Url    string `json:"url" binding:"required"`
			Width  int    `json:"width" default:"1280"`
			Height int    `json:"height" default:"720"`
		}

		if err := c.ShouldBindJSON(&request); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request: " + err.Error()})
			return
		}

		log.Printf("Received screenshot request, url: %s, size: %dx%d", request.Url, request.Width, request.Height)

		// 暂时返回模拟响应
		c.JSON(http.StatusOK, gin.H{
			"status":     "success",
			"url":        request.Url,
			"screenshot": "base64-encoded-screenshot-data",
			"format":     "png",
			"size":       "1280x720",
		})
	})

	// 新增多模态交互API路由
	// 1. WebRTC连接管理
	router.POST("/api/webrtc/connect", func(c *gin.Context) {
		var request struct {
			SessionID string `json:"session_id" binding:"required"`
		}

		if err := c.ShouldBindJSON(&request); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request: " + err.Error()})
			return
		}

		// 创建多模态会话
		session, err := multimodalInput.CreateSession(request.SessionID)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to create session: " + err.Error()})
			return
		}

		c.JSON(http.StatusOK, gin.H{
			"status":     "success",
			"session_id": session.ID,
			"message":    "WebRTC session created successfully",
		})
	})

	router.POST("/api/webrtc/disconnect", func(c *gin.Context) {
		var request struct {
			SessionID string `json:"session_id" binding:"required"`
		}

		if err := c.ShouldBindJSON(&request); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request: " + err.Error()})
			return
		}

		// 关闭多模态会话
		multimodalInput.CloseSession(request.SessionID)

		c.JSON(http.StatusOK, gin.H{
			"status":     "success",
			"session_id": request.SessionID,
			"message":    "WebRTC session disconnected successfully",
		})
	})

	// 2. 语音交互API
	router.POST("/api/voice/interact", func(c *gin.Context) {
		var request struct {
			Audio    string `json:"audio" binding:"required"`
			Language string `json:"language" default:"zh"`
		}

		if err := c.ShouldBindJSON(&request); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request: " + err.Error()})
			return
		}

		// 处理语音输入
		audioData := []byte(request.Audio)
		result, emotion, err := voiceInteraction.ProcessSpeech(audioData)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to process speech: " + err.Error()})
			return
		}

		// 生成语音响应
		_, err = voiceInteraction.GenerateSpeech("我理解了你的请求，正在处理中。", emotion)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to generate speech: " + err.Error()})
			return
		}

		c.JSON(http.StatusOK, gin.H{
			"status":     "success",
			"text":       result.Text,
			"emotion":    emotion,
			"confidence": result.Confidence,
			"language":   result.Language,
			"response":   "base64-encoded-audio-data",
		})
	})

	// 3. 图像理解API
	router.POST("/api/vision/process", func(c *gin.Context) {
		var request struct {
			Image string   `json:"image" binding:"required"`
			Tasks []string `json:"tasks" default:"[\"classification\",\"detection\",\"ocr\",\"caption\",\"faces\"]"`
		}

		if err := c.ShouldBindJSON(&request); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request: " + err.Error()})
			return
		}

		// 处理图像
		imageData := []byte(request.Image)
		result, err := imageUnderstanding.ProcessImage(imageData)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to process image: " + err.Error()})
			return
		}

		c.JSON(http.StatusOK, gin.H{
			"status":         "success",
			"caption":        result.Caption,
			"classification": result.Classification,
			"objects":        result.Objects,
			"text":           result.Text,
			"faces":          result.Faces,
		})
	})

	// 4. 手势识别API
	router.POST("/api/gesture/recognize", func(c *gin.Context) {
		var request struct {
			Frame string `json:"frame" binding:"required"`
		}

		if err := c.ShouldBindJSON(&request); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request: " + err.Error()})
			return
		}

		// 处理手势识别
		// 这里应该使用手势识别器处理帧数据
		// 暂时返回模拟响应

		c.JSON(http.StatusOK, gin.H{
			"status":     "success",
			"gesture":    "WAVE",
			"confidence": 0.95,
			"message":    "Gesture recognized successfully",
		})
	})

	httpServer := &http.Server{
		Addr:    fmt.Sprintf(":%s", config.Port.HTTPPort),
		Handler: router,
	}

	go func() {
		log.Printf("HTTP server starting on port %s...", config.Port.HTTPPort)
		if err := httpServer.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("Failed to start HTTP server: %v", err)
		}
	}()

	// 等待中断信号
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	log.Println("Shutting down servers...")

	// 停止边缘服务
	if err := edgeService.Stop(ctx); err != nil {
		log.Printf("Error stopping edge service: %v", err)
	}

	// 关闭gRPC服务器
	grpcServer.GracefulStop()

	// 关闭HTTP服务器
	ctxShutdown, cancelShutdown := context.WithTimeout(ctx, 5*time.Second)
	defer cancelShutdown()
	if err := httpServer.Shutdown(ctxShutdown); err != nil {
		log.Fatalf("HTTP server forced to shutdown: %v", err)
	}

	log.Println("Servers stopped successfully")
}
