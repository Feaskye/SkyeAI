package handlers

import (
	"encoding/json"
	"log"
	"net/http"

	"jarvis-finance-go/pkg/notification"
	"jarvis-finance-go/pkg/sector"
)

// SectorHandler 版块处理器
type SectorHandler struct {
	sectorMonitor       *sector.SectorMonitor
	notificationService *notification.NotificationService
}

// NewSectorHandler 创建版块处理器实例
func NewSectorHandler(sectorMonitor *sector.SectorMonitor, notificationService *notification.NotificationService) *SectorHandler {
	return &SectorHandler{
		sectorMonitor:       sectorMonitor,
		notificationService: notificationService,
	}
}

// GetSectorData 获取版块数据
func (h *SectorHandler) GetSectorData(w http.ResponseWriter, r *http.Request) {
	sectorData, err := h.sectorMonitor.GetSectorData()
	if err != nil {
		log.Printf("Failed to get sector data: %v", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(sectorData)
}

// GetHotSectors 获取热点版块
func (h *SectorHandler) GetHotSectors(w http.ResponseWriter, r *http.Request) {
	// 获取版块数据
	sectorData, err := h.sectorMonitor.GetSectorData()
	if err != nil {
		log.Printf("Failed to get sector data: %v", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	// 识别热点版块
	hotSectors := h.sectorMonitor.IdentifyHotSectors(sectorData)

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(hotSectors)
}
