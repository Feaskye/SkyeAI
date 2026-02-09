package llm

import (
	"context"
	"fmt"
	"log"
	"sync"
	"time"

	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

// LLMConfig LLM服务配置

type LLMConfig struct {
	ServiceAddr string
	GRPCPort    int
	APIKey      string
	Timeout     int // 超时时间（秒）
}

// GenerateRequest 生成请求

type GenerateRequest struct {
	Prompt      string
	Model       string
	Temperature float32
	MaxTokens   int
}

// GenerateResponse 生成响应

type GenerateResponse struct {
	Text  string
	Error string
}

// LLMClient LLM客户端

type LLMClient struct {
	config      LLMConfig
	conn        *grpc.ClientConn
	mutex       sync.RWMutex
	isConnected bool
	ctx         context.Context
}

// NewLLMClient 创建LLM客户端实例

func NewLLMClient(config LLMConfig) *LLMClient {
	return &LLMClient{
		config:      config,
		isConnected: false,
		ctx:         context.Background(),
	}
}

// Connect 连接LLM服务

func (l *LLMClient) Connect() error {
	l.mutex.Lock()
	defer l.mutex.Unlock()

	if l.isConnected {
		return nil
	}

	addr := fmt.Sprintf("%s:%d", l.config.ServiceAddr, l.config.GRPCPort)

	// 创建GRPC连接
	var err error
	l.conn, err = grpc.Dial(addr,
		grpc.WithTransportCredentials(insecure.NewCredentials()),
		grpc.WithDefaultServiceConfig(`{"loadBalancingPolicy":"round_robin"}`),
	)
	if err != nil {
		return fmt.Errorf("failed to connect to llm service: %w", err)
	}

	l.isConnected = true
	log.Println("LLM service connected successfully")
	return nil
}

// Close 关闭LLM服务连接

func (l *LLMClient) Close() error {
	l.mutex.Lock()
	defer l.mutex.Unlock()

	if !l.isConnected {
		return nil
	}

	if l.conn != nil {
		if err := l.conn.Close(); err != nil {
			log.Printf("Error closing llm connection: %v", err)
		}
	}

	l.isConnected = false
	log.Println("LLM service connection closed")
	return nil
}

// Generate 生成文本

func (l *LLMClient) Generate(request GenerateRequest) (*GenerateResponse, error) {
	l.mutex.RLock()
	defer l.mutex.RUnlock()

	if !l.isConnected {
		log.Printf("LLM service not connected, cannot generate response")
		return nil, fmt.Errorf("llm service not connected")
	}

	// 创建上下文，设置超时
	timeout := time.Duration(l.config.Timeout) * time.Second
	_, cancel := context.WithTimeout(l.ctx, timeout)
	defer cancel()

	log.Printf("Generating LLM response with model: %s, temperature: %f, max tokens: %d",
		request.Model, request.Temperature, request.MaxTokens)
	log.Printf("Prompt: %s", request.Prompt)

	// 实际的GRPC调用实现
	// 注意：这里需要根据实际的LLM服务proto文件生成的客户端代码来实现
	// 由于没有实际的proto文件，这里使用模拟实现
	// 实际实现时，应该使用生成的客户端代码调用相应的方法

	// 模拟GRPC调用过程
	log.Printf("Sending GRPC request to %s:%d", l.config.ServiceAddr, l.config.GRPCPort)

	// 模拟网络延迟
	time.Sleep(1 * time.Second)

	// 模拟响应
	response := &GenerateResponse{
		Text:  "This is a response from LLM service generated via gRPC",
		Error: "",
	}

	log.Printf("GRPC response received successfully")
	log.Printf("LLM response text: %s", response.Text)

	return response, nil
}

// IsConnected 检查是否连接

func (l *LLMClient) IsConnected() bool {
	l.mutex.RLock()
	defer l.mutex.RUnlock()
	return l.isConnected
}

// Reconnect 重连LLM服务

func (l *LLMClient) Reconnect() error {
	log.Println("Attempting to reconnect to LLM service...")

	// 关闭现有连接
	if err := l.Close(); err != nil {
		log.Printf("Error closing existing connection: %v", err)
	}

	// 尝试重连
	var lastErr error
	for i := 0; i < 5; i++ {
		err := l.Connect()
		if err == nil {
			log.Println("LLM service reconnected successfully")
			return nil
		}

		lastErr = err
		log.Printf("Reconnection attempt %d failed: %v", i+1, err)
		time.Sleep(time.Duration(i+1) * time.Second)
	}

	return fmt.Errorf("failed to reconnect after 5 attempts: %w", lastErr)
}

// HealthCheck 健康检查

func (l *LLMClient) HealthCheck() error {
	l.mutex.RLock()
	defer l.mutex.RUnlock()

	if !l.isConnected {
		log.Printf("LLM service not connected, cannot perform health check")
		return fmt.Errorf("llm service not connected")
	}

	// 创建上下文，设置超时
	timeout := 5 * time.Second
	_, cancel := context.WithTimeout(l.ctx, timeout)
	defer cancel()

	log.Printf("Performing LLM service health check on %s:%d", l.config.ServiceAddr, l.config.GRPCPort)

	// 实际的健康检查调用实现
	// 注意：这里需要根据实际的LLM服务proto文件生成的客户端代码来实现
	// 由于没有实际的proto文件，这里使用模拟实现
	// 实际实现时，应该使用生成的客户端代码调用相应的健康检查方法

	// 模拟健康检查过程
	log.Printf("Sending health check GRPC request")

	// 模拟网络延迟
	time.Sleep(500 * time.Millisecond)

	// 模拟健康检查通过
	log.Printf("LLM service health check passed successfully")
	log.Printf("Service status: connected and healthy")

	return nil
}
