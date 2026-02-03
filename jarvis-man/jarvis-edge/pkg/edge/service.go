package edge

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"sync"
	"time"

	"github.com/nacos-group/nacos-sdk-go/v2/clients"
	"github.com/nacos-group/nacos-sdk-go/v2/common/constant"
	"github.com/nacos-group/nacos-sdk-go/v2/vo"
	"github.com/skyeai/jarvis-edge/pkg/mqtt"
)

// Service 边缘节点服务接口
type Service interface {
	// Start 启动边缘节点服务
	Start(ctx context.Context) error
	// Stop 停止边缘节点服务
	Stop(ctx context.Context) error
	// GetStatus 获取服务状态
	GetStatus() ServiceStatus
	// SendHealthData 发送健康数据
	SendHealthData(ctx context.Context, data mqtt.HealthData) error
	// ExecuteIoTCommand 执行IoT设备命令
	ExecuteIoTCommand(ctx context.Context, command mqtt.IoTCommand) error
	// GetHealthStats 获取健康数据统计
	GetHealthStats(ctx context.Context, userID string, startTime, endTime time.Time) (map[string]float64, error)
}

// ServiceStatus 边缘节点服务状态
type ServiceStatus struct {
	Component   string            `json:"component"`
	Status      string            `json:"status"`
	Version     string            `json:"version"`
	Metrics     map[string]string `json:"metrics"`
	MQTTStatus  string            `json:"mqtt_status"`
	StartTime   time.Time         `json:"start_time"`
	Uptime      time.Duration     `json:"uptime"`
	ActiveTasks int               `json:"active_tasks"`
}

// Config 边缘节点服务配置
type Config struct {
	Name         string       `json:"name"`
	Version      string       `json:"version"`
	UserID       string       `json:"user_id"`
	MQTTConfig   mqtt.Config  `json:"mqtt_config"`
	SensorConfig SensorConfig `json:"sensor_config"`
	HTTPConfig   HTTPConfig   `json:"http_config"`
	NacosConfig  NacosConfig  `json:"nacos_config"`
}

// NacosConfig Nacos配置
type NacosConfig struct {
	Enabled     bool   `json:"enabled"`
	ServerAddr  string `json:"server_addr"`
	NamespaceId string `json:"namespace_id"`
	GroupName   string `json:"group_name"`
	ServiceName string `json:"service_name"`
	ClusterName string `json:"cluster_name"`
	Port        int    `json:"port"`
	IP          string `json:"ip"`
}

// SensorConfig 传感器配置
type SensorConfig struct {
	Enabled          bool          `json:"enabled"`
	PollInterval     time.Duration `json:"poll_interval"`
	SimulateData     bool          `json:"simulate_data"`
	SupportedSensors []string      `json:"supported_sensors"`
}

// HTTPConfig HTTP服务器配置
type HTTPConfig struct {
	Enabled bool   `json:"enabled"`
	Port    string `json:"port"`
}

// service 边缘节点服务实现
type service struct {
	config      Config
	mqttClient  mqtt.Client
	status      ServiceStatus
	httpServer  *http.Server
	nacosClient interface {
		RegisterInstance(param vo.RegisterInstanceParam) (bool, error)
		DeregisterInstance(param vo.DeregisterInstanceParam) (bool, error)
		CloseClient()
	}
	ctx         context.Context
	cancel      context.CancelFunc
	wg          sync.WaitGroup
	mu          sync.RWMutex
	sensorCh    chan mqtt.HealthData
	commandCh   chan mqtt.IoTCommand
	activeTasks int
	startTime   time.Time
}

// NewService 创建新的边缘节点服务
func NewService(config Config) Service {
	// 设置默认值
	if config.Name == "" {
		config.Name = "jarvis-edge"
	}
	if config.Version == "" {
		config.Version = "1.0.0"
	}
	if config.UserID == "" {
		config.UserID = "default-user"
	}
	if config.SensorConfig.PollInterval == 0 {
		config.SensorConfig.PollInterval = 30 * time.Second
	}
	if config.HTTPConfig.Port == "" {
		config.HTTPConfig.Port = "8081"
	}

	// Nacos 配置默认值
	if config.NacosConfig.ServerAddr == "" {
		config.NacosConfig.ServerAddr = "localhost:8848"
	}
	if config.NacosConfig.NamespaceId == "" {
		config.NacosConfig.NamespaceId = "public"
	}
	if config.NacosConfig.GroupName == "" {
		config.NacosConfig.GroupName = "DEFAULT_GROUP"
	}
	if config.NacosConfig.ServiceName == "" {
		config.NacosConfig.ServiceName = "jarvis-edge"
	}
	if config.NacosConfig.ClusterName == "" {
		config.NacosConfig.ClusterName = "DEFAULT"
	}
	if config.NacosConfig.Port == 0 {
		config.NacosConfig.Port = 9091
	}

	return &service{
		config:    config,
		sensorCh:  make(chan mqtt.HealthData, 100),
		commandCh: make(chan mqtt.IoTCommand, 100),
		startTime: time.Now(),
		status: ServiceStatus{
			Component:  config.Name,
			Status:     "stopped",
			Version:    config.Version,
			Metrics:    make(map[string]string),
			MQTTStatus: "disconnected",
			StartTime:  time.Now(),
			Uptime:     0,
		},
	}
}

// Start 启动边缘节点服务
func (s *service) Start(ctx context.Context) error {
	s.mu.Lock()
	if s.status.Status == "running" {
		s.mu.Unlock()
		return fmt.Errorf("service already running")
	}

	// 更新状态为启动中
	s.status.Status = "starting"
	s.mu.Unlock()

	// 创建上下文
	s.ctx, s.cancel = context.WithCancel(ctx)
	s.startTime = time.Now()

	// 初始化Nacos客户端（如果启用）
	if s.config.NacosConfig.Enabled {
		// 创建Nacos配置
		serverConfigs := []constant.ServerConfig{
			{
				IpAddr: "localhost",
				Port:   8848,
			},
		}

		clientConfig := constant.ClientConfig{
			NamespaceId:         s.config.NacosConfig.NamespaceId,
			TimeoutMs:           5000,
			NotLoadCacheAtStart: true,
		}

		// 创建服务发现客户端
		discoveryClient, err := clients.NewNamingClient(
			vo.NacosClientParam{
				ClientConfig:  &clientConfig,
				ServerConfigs: serverConfigs,
			},
		)
		if err != nil {
			log.Printf("WARNING: Failed to create Nacos client: %v, service will continue to run", err)
		} else {
			s.nacosClient = discoveryClient
			
			// 注册服务实例
			_, err = s.nacosClient.RegisterInstance(vo.RegisterInstanceParam{
				Ip:          s.config.NacosConfig.IP,
				Port:        uint64(s.config.NacosConfig.Port),
				ServiceName: s.config.NacosConfig.ServiceName,
				GroupName:   s.config.NacosConfig.GroupName,
				ClusterName: s.config.NacosConfig.ClusterName,
				Weight:      10,
				Enable:      true,
				Healthy:     true,
			})
			if err != nil {
				log.Printf("WARNING: Failed to register service to Nacos: %v, service will continue to run", err)
			}
		}
	}

	// 初始化MQTT客户端
	log.Println("Initializing MQTT client...")
	s.mqttClient = mqtt.NewClient(s.config.MQTTConfig)
	if err := s.mqttClient.Connect(); err != nil {
		// MQTT连接失败，仅记录日志，不影响服务启动
		log.Printf("WARNING: Failed to connect to MQTT broker: %v, service will continue to run", err)
		s.mu.Lock()
		s.status.MQTTStatus = "disconnected"
		s.mu.Unlock()
	} else {
		// 更新MQTT状态
		s.mu.Lock()
		s.status.MQTTStatus = "connected"
		s.mu.Unlock()

		// 订阅相关主题
		log.Println("Subscribing to MQTT topics...")

		// 订阅IoT命令主题
		iotCommandTopic := mqtt.GetIoTCommandTopic("#") // 订阅所有设备命令
		if err := s.mqttClient.Subscribe(iotCommandTopic, 1, s.handleIoTCommand); err != nil {
			// 订阅失败，仅记录日志
			log.Printf("WARNING: Failed to subscribe to IoT command topic: %v", err)
		}

		// 订阅广播主题
		if err := s.mqttClient.Subscribe(mqtt.TopicBroadcast, 0, s.handleBroadcastMessage); err != nil {
			// 订阅失败，仅记录日志
			log.Printf("WARNING: Failed to subscribe to broadcast topic: %v", err)
		}
	}

	// 启动传感器数据采集（如果启用）
	if s.config.SensorConfig.Enabled {
		s.wg.Add(1)
		go s.sensorDataCollector(s.ctx)
	}

	// 启动健康数据发送器
	s.wg.Add(1)
	go s.healthDataSender(s.ctx)

	// 启动命令处理器
	s.wg.Add(1)
	go s.commandProcessor(s.ctx)

	// 启动HTTP服务器（如果启用）
	if s.config.HTTPConfig.Enabled {
		s.wg.Add(1)
		go s.startHTTPServer(s.ctx)
	}

	// 更新状态为运行中
	s.mu.Lock()
	s.status.Status = "running"
	s.status.StartTime = s.startTime
	s.mu.Unlock()

	log.Printf("Edge service started successfully: %s v%s", s.config.Name, s.config.Version)
	return nil
}

// Stop 停止边缘节点服务
func (s *service) Stop(ctx context.Context) error {
	s.mu.Lock()
	if s.status.Status != "running" {
		s.mu.Unlock()
		return fmt.Errorf("service not running")
	}

	// 更新状态为停止中
	s.status.Status = "stopping"
	s.mu.Unlock()

	// 取消上下文
	s.cancel()

	// 等待所有goroutine完成
	done := make(chan struct{})
	go func() {
		s.wg.Wait()
		close(done)
	}()

	select {
	case <-done:
		// 从Nacos注销服务实例
		if s.nacosClient != nil {
			log.Printf("Deregistering service from Nacos: %s", s.config.NacosConfig.ServiceName)
			_, err := s.nacosClient.DeregisterInstance(vo.DeregisterInstanceParam{
				Ip:          s.config.NacosConfig.IP,
				Port:        uint64(s.config.NacosConfig.Port),
				ServiceName: s.config.NacosConfig.ServiceName,
				GroupName:   s.config.NacosConfig.GroupName,
			})
			if err != nil {
				log.Printf("WARNING: Failed to deregister service from Nacos: %v", err)
			}
			// 关闭Nacos客户端
			s.nacosClient.CloseClient()
		}

		// 关闭MQTT连接
		if s.mqttClient != nil {
			s.mqttClient.Disconnect()
		}

		// 更新状态
		s.mu.Lock()
		s.status.Status = "stopped"
		s.status.MQTTStatus = "disconnected"
		s.mu.Unlock()

		log.Printf("Edge service stopped: %s", s.config.Name)
		return nil
	case <-ctx.Done():
		return fmt.Errorf("stop timed out: %w", ctx.Err())
	}
}

// GetStatus 获取服务状态
func (s *service) GetStatus() ServiceStatus {
	s.mu.RLock()
	defer s.mu.RUnlock()

	status := s.status
	status.Uptime = time.Since(s.startTime)
	return status
}

// SendHealthData 发送健康数据
func (s *service) SendHealthData(ctx context.Context, data mqtt.HealthData) error {
	select {
	case s.sensorCh <- data:
		return nil
	case <-ctx.Done():
		return fmt.Errorf("context cancelled: %w", ctx.Err())
	}
}

// ExecuteIoTCommand 执行IoT设备命令
func (s *service) ExecuteIoTCommand(ctx context.Context, command mqtt.IoTCommand) error {
	select {
	case s.commandCh <- command:
		return nil
	case <-ctx.Done():
		return fmt.Errorf("context cancelled: %w", ctx.Err())
	}
}

// GetHealthStats 获取健康数据统计
func (s *service) GetHealthStats(ctx context.Context, userID string, startTime, endTime time.Time) (map[string]float64, error) {
	// 这里实现健康数据统计逻辑
	// 暂时返回模拟数据
	stats := map[string]float64{
		"heart_rate":    72.5,
		"sleep_quality": 85.0,
		"steps":         3500.0,
		"calories":      1800.0,
		"oxygen_level":  98.5,
	}
	return stats, nil
}

// sensorDataCollector 传感器数据采集器
func (s *service) sensorDataCollector(ctx context.Context) {
	defer s.wg.Done()

	log.Println("Starting sensor data collector...")

	ticker := time.NewTicker(s.config.SensorConfig.PollInterval)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			log.Println("Sensor data collector stopped")
			return
		case <-ticker.C:
			// 采集传感器数据
			if s.config.SensorConfig.SimulateData {
				// 生成模拟健康数据
				healthData := s.generateSimulatedHealthData()
				// 发送健康数据
				if err := s.SendHealthData(ctx, healthData); err != nil {
					log.Printf("Failed to send health data: %v", err)
				}
			} else {
				// 实际项目中应调用真实传感器API
				log.Println("Real sensor data collection not implemented yet")
			}
		}
	}
}

// healthDataSender 健康数据发送器
func (s *service) healthDataSender(ctx context.Context) {
	defer s.wg.Done()

	log.Println("Starting health data sender...")

	for {
		select {
		case <-ctx.Done():
			log.Println("Health data sender stopped")
			return
		case data := <-s.sensorCh:
			// 确保数据有时间戳
			if data.Timestamp.IsZero() {
				data.Timestamp = time.Now()
			}

			// 设置用户ID
			if data.UserID == "" {
				data.UserID = s.config.UserID
			}

			// 发布健康数据到MQTT
			topic := mqtt.GetHealthDataTopic(data.UserID)
			if err := s.mqttClient.Publish(ctx, topic, data, 1); err != nil {
				log.Printf("Failed to publish health data: %v", err)
			} else {
				log.Printf("Health data published: %+v", data)
			}
		}
	}
}

// commandProcessor 命令处理器
func (s *service) commandProcessor(ctx context.Context) {
	defer s.wg.Done()

	log.Println("Starting command processor...")

	for {
		select {
		case <-ctx.Done():
			log.Println("Command processor stopped")
			return
		case command := <-s.commandCh:
			log.Printf("Processing IoT command: %+v", command)

			// 执行命令并生成状态报告
			status := s.processCommand(ctx, command)

			// 发布设备状态到MQTT
			statusTopic := mqtt.GetIoTStatusTopic(command.DeviceID)
			if err := s.mqttClient.Publish(ctx, statusTopic, status, 1); err != nil {
				log.Printf("Failed to publish IoT status: %v", err)
			} else {
				log.Printf("IoT status published: %+v", status)
			}
		}
	}
}

// processCommand 处理IoT命令
func (s *service) processCommand(ctx context.Context, command mqtt.IoTCommand) mqtt.IoTStatus {
	// 模拟命令执行
	status := mqtt.IoTStatus{
		DeviceID:   command.DeviceID,
		Status:     "success",
		Properties: make(map[string]interface{}),
		Timestamp:  time.Now(),
		Source:     s.config.Name,
	}

	// 根据命令类型执行不同的操作
	switch command.Command {
	case "turn_on":
		status.Properties["power"] = true
		status.Properties["last_command"] = "turn_on"
	case "turn_off":
		status.Properties["power"] = false
		status.Properties["last_command"] = "turn_off"
	case "set_brightness":
		if brightness, ok := command.Parameters["brightness"].(float64); ok {
			status.Properties["brightness"] = brightness
		}
	case "set_temperature":
		if temp, ok := command.Parameters["temperature"].(float64); ok {
			status.Properties["temperature"] = temp
		}
	default:
		status.Status = "failed"
		status.Properties["error"] = fmt.Sprintf("unknown command: %s", command.Command)
	}

	return status
}

// handleIoTCommand 处理接收到的IoT命令
func (s *service) handleIoTCommand(ctx context.Context, topic string, payload []byte) error {
	log.Printf("Received IoT command on topic %s: %s", topic, string(payload))

	var command mqtt.IoTCommand
	if err := json.Unmarshal(payload, &command); err != nil {
		return fmt.Errorf("failed to unmarshal IoT command: %w", err)
	}

	// 执行命令
	return s.ExecuteIoTCommand(ctx, command)
}

// handleBroadcastMessage 处理广播消息
func (s *service) handleBroadcastMessage(ctx context.Context, topic string, payload []byte) error {
	log.Printf("Received broadcast message on topic %s: %s", topic, string(payload))
	// 处理广播消息逻辑
	return nil
}

// startHTTPServer 启动HTTP服务器
func (s *service) startHTTPServer(ctx context.Context) {
	defer s.wg.Done()

	log.Printf("Starting HTTP server on port %s...", s.config.HTTPConfig.Port)

	// 创建HTTP服务器
	httpServer := &http.Server{
		Addr: fmt.Sprintf(":%s", s.config.HTTPConfig.Port),
	}

	// 设置HTTP处理器
	http.HandleFunc("/health", s.handleHealthCheck)
	http.HandleFunc("/status", s.handleStatus)
	http.HandleFunc("/metrics", s.handleMetrics)

	// 启动HTTP服务器
	go func() {
		if err := httpServer.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Printf("HTTP server error: %v", err)
		}
	}()

	// 等待上下文取消
	<-ctx.Done()

	// 关闭HTTP服务器
	shutdownCtx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := httpServer.Shutdown(shutdownCtx); err != nil {
		log.Printf("HTTP server shutdown error: %v", err)
	} else {
		log.Println("HTTP server stopped")
	}
}

// handleHealthCheck 处理健康检查请求
func (s *service) handleHealthCheck(w http.ResponseWriter, r *http.Request) {
	status := s.GetStatus()
	response := map[string]string{
		"status":  status.Status,
		"service": s.config.Name,
		"version": s.config.Version,
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(response)
}

// handleStatus 处理状态请求
func (s *service) handleStatus(w http.ResponseWriter, r *http.Request) {
	status := s.GetStatus()

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(status)
}

// handleMetrics 处理指标请求
func (s *service) handleMetrics(w http.ResponseWriter, r *http.Request) {
	status := s.GetStatus()
	metrics := status.Metrics

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(metrics)
}

// generateSimulatedHealthData 生成模拟健康数据
func (s *service) generateSimulatedHealthData() mqtt.HealthData {
	// 生成随机健康数据
	heartRate := 60.0 + (float64(time.Now().UnixNano() % 40))    // 60-100 bpm
	sleepQuality := 70.0 + (float64(time.Now().UnixNano() % 30)) // 70-100 %
	steps := 1000.0 + (float64(time.Now().UnixNano() % 9000))    // 1000-10000 steps
	calories := 500.0 + (float64(time.Now().UnixNano() % 1500))  // 500-2000 calories
	oxygenLevel := 95.0 + (float64(time.Now().UnixNano() % 5))   // 95-100 %

	// 随机选择一种健康数据类型发送
	dataTypes := []string{
		"heart_rate",
		"sleep_quality",
		"steps",
		"calories",
		"oxygen_level",
	}

	// 简单的轮询机制
	dataType := dataTypes[int(time.Now().UnixNano())%len(dataTypes)]

	var value float64
	var unit string

	switch dataType {
	case "heart_rate":
		value = heartRate
		unit = "bpm"
	case "sleep_quality":
		value = sleepQuality
		unit = "%"
	case "steps":
		value = steps
		unit = "steps"
	case "calories":
		value = calories
		unit = "kcal"
	case "oxygen_level":
		value = oxygenLevel
		unit = "%"
	}

	return mqtt.HealthData{
		DataType:  dataType,
		Value:     value,
		Unit:      unit,
		Timestamp: time.Now(),
		Source:    "simulated",
		UserID:    s.config.UserID,
	}
}
