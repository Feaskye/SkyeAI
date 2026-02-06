package sector

import (
	"fmt"
	"log"
	"sort"
	"time"

	"github.com/go-resty/resty/v2"
	"jarvis-finance-go/pkg/model"
)

// SectorMonitor 版块监控模块
type SectorMonitor struct {
	client  *resty.Client
	sectors []string
}

// NewSectorMonitor 创建版块监控实例
func NewSectorMonitor() *SectorMonitor {
	client := resty.New()
	client.SetTimeout(10 * time.Second)

	// 初始化监控的版块列表
	sectors := []string{
		"tech",      // 科技
		"finance",   // 金融
		"health",    // 医疗
		"energy",    // 能源
		"consumer",  // 消费
		"industry",  // 工业
		"realty",    // 房地产
		"materials", // 材料
	}

	return &SectorMonitor{
		client:  client,
		sectors: sectors,
	}
}

// Start 启动版块监控
func (s *SectorMonitor) Start() {
	log.Println("SectorMonitor started")

	// 每10分钟监控一次
	ticker := time.NewTicker(10 * time.Minute)
	defer ticker.Stop()

	// 立即执行一次
	s.monitor()

	for range ticker.C {
		s.monitor()
	}
}

// monitor 执行监控任务
func (s *SectorMonitor) monitor() {
	log.Println("Executing sector monitor task")

	// 获取所有版块数据
	sectorDataList, err := s.GetSectorData()
	if err != nil {
		log.Printf("Failed to get sector data: %v", err)
		return
	}

	// 识别热点版块
	hotSectors := s.IdentifyHotSectors(sectorDataList)
	log.Printf("Hot sectors: %v", hotSectors)

	// 分析版块联动
	linkage := s.AnalyzeLinkage(sectorDataList)
	log.Printf("Sector linkage analysis: %v", linkage)

	// 打印版块数据
	for _, sectorData := range sectorDataList {
		log.Printf("Sector %s: price=%.2f, change=%.2f%%",
			sectorData.SectorName, sectorData.Price, sectorData.ChangePercent)
	}
}

// GetSectorData 获取版块数据
func (s *SectorMonitor) GetSectorData() ([]model.SectorData, error) {
	// 在实际应用中，这里应该调用真实的API
	// 这里使用模拟数据
	return s.getMockSectorData(), nil
}

// IdentifyHotSectors 识别热点版块
func (s *SectorMonitor) IdentifyHotSectors(sectorDataList []model.SectorData) []string {
	// 按涨跌幅排序
	sort.Slice(sectorDataList, func(i, j int) bool {
		return sectorDataList[i].ChangePercent > sectorDataList[j].ChangePercent
	})

	// 取前3个涨幅最大的版块作为热点版块
	hotSectors := []string{}
	for i, sectorData := range sectorDataList {
		if i < 3 && sectorData.ChangePercent > 0 {
			hotSectors = append(hotSectors, sectorData.SectorName)
		}
	}

	return hotSectors
}

// AnalyzeLinkage 分析版块联动
func (s *SectorMonitor) AnalyzeLinkage(sectorDataList []model.SectorData) map[string][]string {
	// 简单的联动分析，实际应用中可以使用更复杂的算法
	linkage := make(map[string][]string)

	// 按涨跌幅分组
	risingSectors := []string{}
	fallingSectors := []string{}

	for _, sectorData := range sectorDataList {
		if sectorData.ChangePercent > 0 {
			risingSectors = append(risingSectors, sectorData.SectorName)
		} else {
			fallingSectors = append(fallingSectors, sectorData.SectorName)
		}
	}

	linkage["rising"] = risingSectors
	linkage["falling"] = fallingSectors

	return linkage
}

// getMockSectorData 获取模拟版块数据
func (s *SectorMonitor) getMockSectorData() []model.SectorData {
	sectorDataList := []model.SectorData{}

	for i, sectorName := range s.sectors {
		// 生成随机价格和涨跌幅
		basePrice := 1000.0 + float64(i)*100
		// 随机涨跌幅 (-5% 到 +5%)
		changePercent := (float64(time.Now().UnixNano()%100)/10 - 5) + float64(i%3)
		price := basePrice * (1 + changePercent/100)

		sectorData := model.SectorData{
			SectorCode:    fmt.Sprintf("%s%s", sectorName, fmt.Sprintf("%02d", i+1)),
			SectorName:    sectorName,
			Price:         price,
			ChangePercent: changePercent,
			Timestamp:     time.Now(),
		}

		sectorDataList = append(sectorDataList, sectorData)
	}

	return sectorDataList
}
