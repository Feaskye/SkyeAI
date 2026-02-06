package scheduler

import (
	"fmt"
	"log"
	"time"

	"github.com/robfig/cron/v3"
)

// TaskScheduler 定时任务调度器
type TaskScheduler struct {
	cron *cron.Cron
	jobs map[string]cron.EntryID
}

// NewTaskScheduler 创建新的任务调度器
func NewTaskScheduler() *TaskScheduler {
	// 使用带秒级精度的cron调度器
	c := cron.New(cron.WithSeconds())
	return &TaskScheduler{
		cron: c,
		jobs: make(map[string]cron.EntryID),
	}
}

// Start 启动任务调度器
func (ts *TaskScheduler) Start() {
	ts.cron.Start()
	log.Println("定时任务调度器已启动")
}

// Stop 停止任务调度器
func (ts *TaskScheduler) Stop() {
	ts.cron.Stop()
	log.Println("定时任务调度器已停止")
}

// AddTask 添加定时任务
// cronSpec: cron表达式，如 "0 */5 * * * *" 表示每5分钟执行一次
// taskName: 任务名称
// taskFunc: 任务执行函数
func (ts *TaskScheduler) AddTask(cronSpec string, taskName string, taskFunc func()) error {
	entryID, err := ts.cron.AddFunc(cronSpec, func() {
		log.Printf("开始执行任务: %s\n", taskName)
		start := time.Now()
		defer func() {
			if r := recover(); r != nil {
				log.Printf("任务执行出错: %s, 错误: %v\n", taskName, r)
			}
			duration := time.Since(start)
			log.Printf("任务执行完成: %s, 耗时: %v\n", taskName, duration)
		}()
		taskFunc()
	})

	if err != nil {
		return fmt.Errorf("添加任务失败: %v", err)
	}

	ts.jobs[taskName] = entryID
	log.Printf("任务已添加: %s, cron表达式: %s\n", taskName, cronSpec)
	return nil
}

// RemoveTask 移除定时任务
func (ts *TaskScheduler) RemoveTask(taskName string) bool {
	entryID, exists := ts.jobs[taskName]
	if !exists {
		log.Printf("任务不存在: %s\n", taskName)
		return false
	}

	ts.cron.Remove(entryID)
	delete(ts.jobs, taskName)
	log.Printf("任务已移除: %s\n", taskName)
	return true
}

// ListTasks 列出所有任务
func (ts *TaskScheduler) ListTasks() {
	log.Println("当前任务列表:")
	for taskName := range ts.jobs {
		log.Printf("- %s\n", taskName)
	}
}

// 常用的cron表达式常量
const (
	// Every5Minutes 每5分钟执行一次
	Every5Minutes = "0 */5 * * * *"
	// Every10Minutes 每10分钟执行一次
	Every10Minutes = "0 */10 * * * *"
	// EveryHour 每小时执行一次
	EveryHour = "0 0 * * * *"
	// EveryDay 每天执行一次
	EveryDay = "0 0 0 * * *"
)
