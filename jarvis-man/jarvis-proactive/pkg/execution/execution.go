package execution

import (
	"bytes"
	"context"
	"log"
	"os"
	"os/exec"
	"strings"
	"time"

	"github.com/skyeai/jarvis-proactive/pkg/skeleton"
)

// ExecutionService 主动执行服务
type ExecutionService struct {
	allowedCommands []string
	maxExecTime     time.Duration
	workingDir      string
	messageBus      *skeleton.MessageBus
	enabled         bool
	subscribeChan   chan skeleton.Message
}

// NewExecutionService 创建执行服务实例
func NewExecutionService(allowedCommands []string, maxExecTime int, workingDir string, messageBus *skeleton.MessageBus, enabled bool) *ExecutionService {
	return &ExecutionService{
		allowedCommands: allowedCommands,
		maxExecTime:     time.Duration(maxExecTime) * time.Second,
		workingDir:      workingDir,
		messageBus:      messageBus,
		enabled:         enabled,
		subscribeChan:   nil,
	}
}

// Start 启动执行服务
func (es *ExecutionService) Start() {
	if !es.enabled {
		log.Println("Execution service is disabled")
		return
	}

	log.Println("Starting execution service...")

	// 订阅决策事件
	es.subscribeChan = es.messageBus.Subscribe("decision")

	// 启动消息处理
	go es.processMessages()

	log.Println("Execution service started")
}

// Stop 停止执行服务
func (es *ExecutionService) Stop() {
	if !es.enabled {
		return
	}

	log.Println("Stopping execution service...")

	// 取消订阅
	if es.subscribeChan != nil {
		es.messageBus.Unsubscribe("decision", es.subscribeChan)
	}

	log.Println("Execution service stopped")
}

// processMessages 处理消息
func (es *ExecutionService) processMessages() {
	for message := range es.subscribeChan {
		es.handleMessage(message)
	}
}

// handleMessage 处理消息
func (es *ExecutionService) handleMessage(message skeleton.Message) {
	log.Printf("Execution service received message: %s from %s", message.Type, message.Source)

	// 分析决策
	decision, ok := message.Data["decision"].(string)
	if !ok {
		log.Println("Invalid decision in message")
		return
	}

	// 只有当决策是 execute 时才执行命令
	if decision != "execute" {
		return
	}

	filePath, ok := message.Data["file_path"].(string)
	if !ok {
		log.Println("Invalid file path in message")
		return
	}

	// 生成并执行命令
	es.executeCommand(filePath)
}

// executeCommand 执行命令
func (es *ExecutionService) executeCommand(filePath string) {
	// 根据文件路径生成命令
	command, args := es.generateCommand(filePath)

	// 检查命令是否在允许列表中
	if !es.isCommandAllowed(command) {
		log.Printf("Command %s is not allowed", command)
		return
	}

	// 执行命令
	result, err := es.runCommand(command, args)

	// 发送执行结果
	executionMessage := skeleton.Message{
		Type:      "execution_result",
		Source:    "execution",
		Timestamp: time.Now(),
		Data: map[string]interface{}{
			"file_path": filePath,
			"command":   command,
			"args":      args,
			"result":    result,
			"error":     err != nil,
			"error_msg": err, // 可能为 nil
			"timestamp": time.Now(),
		},
	}

	es.messageBus.SendMessage(executionMessage)
	log.Printf("Command executed: %s %s, error: %v", command, strings.Join(args, " "), err)
}

// generateCommand 根据文件路径生成命令
func (es *ExecutionService) generateCommand(filePath string) (string, []string) {
	// 简单的命令生成逻辑
	// 实际应用中可以根据文件类型、路径等生成更复杂的命令

	// 获取文件扩展名
	ext := strings.ToLower(filePath[strings.LastIndex(filePath, "."):])

	switch ext {
	case ".txt", ".md":
		// 查看文本文件内容
		if es.isWindows() {
			return "type", []string{filePath}
		}
		return "cat", []string{filePath}
	case ".pdf":
		// 查看PDF文件信息
		if es.isWindows() {
			return "dir", []string{filePath}
		}
		return "ls", []string{"-l", filePath}
	default:
		// 默认查看文件信息
		if es.isWindows() {
			return "dir", []string{filePath}
		}
		return "ls", []string{"-l", filePath}
	}
}

// isCommandAllowed 检查命令是否在允许列表中
func (es *ExecutionService) isCommandAllowed(command string) bool {
	for _, allowedCmd := range es.allowedCommands {
		if allowedCmd == command {
			return true
		}
	}
	return false
}

// runCommand 运行命令
func (es *ExecutionService) runCommand(command string, args []string) (string, error) {
	ctx, cancel := context.WithTimeout(context.Background(), es.maxExecTime)
	defer cancel()

	cmd := exec.CommandContext(ctx, command, args...)

	// 设置工作目录
	if es.workingDir != "" {
		cmd.Dir = es.workingDir
	}

	// 捕获标准输出和标准错误
	var stdout, stderr bytes.Buffer
	cmd.Stdout = &stdout
	cmd.Stderr = &stderr

	// 执行命令
	err := cmd.Run()

	// 组合输出
	output := stdout.String()
	if stderr.Len() > 0 {
		output += "\n" + stderr.String()
	}

	return output, err
}

// isWindows 检查是否为Windows系统
func (es *ExecutionService) isWindows() bool {
	return os.PathSeparator == '\\'
}
