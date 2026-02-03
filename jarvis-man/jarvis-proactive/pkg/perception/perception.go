package perception

import (
	"log"
	"os"
	"path/filepath"
	"strings"
	"time"

	"github.com/fsnotify/fsnotify"
	"github.com/skyeai/jarvis-proactive/pkg/skeleton"
)

// PerceptionService 持续感知服务
type PerceptionService struct {
	watcher        *fsnotify.Watcher
	watchPaths     []string
	fileExtensions []string
	pollInterval   time.Duration
	messageBus     *skeleton.MessageBus
	enabled        bool
}

// NewPerceptionService 创建感知服务实例
func NewPerceptionService(watchPaths []string, fileExtensions []string, pollInterval int, messageBus *skeleton.MessageBus, enabled bool) (*PerceptionService, error) {
	watcher, err := fsnotify.NewWatcher()
	if err != nil {
		return nil, err
	}

	return &PerceptionService{
		watcher:        watcher,
		watchPaths:     watchPaths,
		fileExtensions: fileExtensions,
		pollInterval:   time.Duration(pollInterval) * time.Second,
		messageBus:     messageBus,
		enabled:        enabled,
	}, nil
}

// Start 启动感知服务
func (ps *PerceptionService) Start() {
	if !ps.enabled {
		log.Println("Perception service is disabled")
		return
	}

	log.Println("Starting perception service...")

	// 启动文件监控
	go ps.watchFiles()

	// 启动轮询监控
	go ps.pollForChanges()

	log.Println("Perception service started")
}

// Stop 停止感知服务
func (ps *PerceptionService) Stop() {
	if !ps.enabled {
		return
	}

	log.Println("Stopping perception service...")
	ps.watcher.Close()
	log.Println("Perception service stopped")
}

// watchFiles 监控文件变化
func (ps *PerceptionService) watchFiles() {
	// 扩展路径，处理 ~ 符号
	extendedPaths := make([]string, 0, len(ps.watchPaths))
	for _, path := range ps.watchPaths {
		if strings.HasPrefix(path, "~") {
			homeDir, err := os.UserHomeDir()
			if err != nil {
				log.Printf("Error getting home directory: %v", err)
				continue
			}
			path = strings.Replace(path, "~", homeDir, 1)
		}
		extendedPaths = append(extendedPaths, path)
	}

	// 添加路径到监控器
	for _, path := range extendedPaths {
		if err := ps.addPathRecursive(path); err != nil {
			log.Printf("Error adding path to watcher: %v", err)
		}
	}

	// 处理事件
	for {
		select {
		case event, ok := <-ps.watcher.Events:
			if !ok {
				return
			}
			ps.handleFileEvent(event)

		case err, ok := <-ps.watcher.Errors:
			if !ok {
				return
			}
			log.Printf("Watcher error: %v", err)
		}
	}
}

// addPathRecursive 递归添加路径到监控器
func (ps *PerceptionService) addPathRecursive(path string) error {
	// 检查路径是否存在
	info, err := os.Stat(path)
	if err != nil {
		return err
	}

	// 添加路径到监控器
	if err := ps.watcher.Add(path); err != nil {
		return err
	}

	// 如果是目录，递归添加子目录
	if info.IsDir() {
		return filepath.Walk(path, func(p string, info os.FileInfo, err error) error {
			if err != nil {
				return err
			}
			if info.IsDir() {
				return ps.watcher.Add(p)
			}
			return nil
		})
	}

	return nil
}

// handleFileEvent 处理文件事件
func (ps *PerceptionService) handleFileEvent(event fsnotify.Event) {
	// 检查文件扩展名是否在监控列表中
	fileExt := strings.ToLower(filepath.Ext(event.Name))
	if len(ps.fileExtensions) > 0 {
		matched := false
		for _, ext := range ps.fileExtensions {
			if ext == fileExt {
				matched = true
				break
			}
		}
		if !matched {
			return
		}
	}

	// 构建事件消息
	eventType := "unknown"
	switch {
	case event.Op&fsnotify.Create != 0:
		eventType = "create"
	case event.Op&fsnotify.Write != 0:
		eventType = "write"
	case event.Op&fsnotify.Remove != 0:
		eventType = "remove"
	case event.Op&fsnotify.Rename != 0:
		eventType = "rename"
	case event.Op&fsnotify.Chmod != 0:
		eventType = "chmod"
	}

	message := skeleton.Message{
		Type:      "file_event",
		Source:    "perception",
		Timestamp: time.Now(),
		Data: map[string]interface{}{
			"event_type": eventType,
			"file_path":  event.Name,
			"timestamp":  time.Now(),
		},
	}

	// 发送消息到消息总线
	ps.messageBus.SendMessage(message)
	log.Printf("File event detected: %s %s", eventType, event.Name)
}

// pollForChanges 轮询监控变化
func (ps *PerceptionService) pollForChanges() {
	// 这里可以实现额外的轮询监控逻辑
	// 例如监控系统状态、邮件、日历等
	for {
		time.Sleep(ps.pollInterval)
		// 实现轮询逻辑
	}
}
