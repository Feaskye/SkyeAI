package input

import (
	"fmt"
	"log"
	"sync"

	"github.com/pion/webrtc/v3"
)

// WebRTCHandler 处理WebRTC连接和媒体流
type WebRTCHandler struct {
	peers      map[string]*webrtc.PeerConnection
	mediaTracks map[string]*webrtc.TrackLocalStaticRTP
	mu         sync.Mutex
}

// NewWebRTCHandler 创建一个新的WebRTCHandler实例
func NewWebRTCHandler() *WebRTCHandler {
	return &WebRTCHandler{
		peers:      make(map[string]*webrtc.PeerConnection),
		mediaTracks: make(map[string]*webrtc.TrackLocalStaticRTP),
	}
}

// CreatePeerConnection 创建一个新的WebRTC对等连接
func (h *WebRTCHandler) CreatePeerConnection(peerID string) (*webrtc.PeerConnection, error) {
	h.mu.Lock()
	defer h.mu.Unlock()

	// 配置WebRTC API
	config := webrtc.Configuration{
		ICEServers: []webrtc.ICEServer{
			{
				URLs: []string{"stun:stun.l.google.com:19302"},
			},
		},
	}

	// 创建对等连接
	peerConnection, err := webrtc.NewPeerConnection(config)
	if err != nil {
		return nil, fmt.Errorf("创建对等连接失败: %w", err)
	}

	// 处理ICE连接状态变化
	peerConnection.OnICEConnectionStateChange(func(connectionState webrtc.ICEConnectionState) {
		log.Printf("ICE连接状态变化: %s", connectionState)
		if connectionState == webrtc.ICEConnectionStateFailed || 
		   connectionState == webrtc.ICEConnectionStateDisconnected {
			h.removePeer(peerID)
		}
	})

	// 处理传入的媒体轨道
	peerConnection.OnTrack(func(track *webrtc.TrackRemote, receiver *webrtc.RTPReceiver) {
		log.Printf("接收到媒体轨道: %s, 类型: %s", track.ID(), track.Kind())
		h.handleIncomingTrack(track, peerID)
	})

	// 存储对等连接
	h.peers[peerID] = peerConnection

	return peerConnection, nil
}

// handleIncomingTrack 处理传入的媒体轨道
func (h *WebRTCHandler) handleIncomingTrack(track *webrtc.TrackRemote, peerID string) {
	// 根据轨道类型创建相应的处理器
	switch track.Kind() {
	case webrtc.RTPCodecTypeAudio:
		go h.handleAudioTrack(track, peerID)
	case webrtc.RTPCodecTypeVideo:
		go h.handleVideoTrack(track, peerID)
	default:
		log.Printf("未知的媒体轨道类型: %s", track.Kind())
	}
}

// handleAudioTrack 处理音频轨道
func (h *WebRTCHandler) handleAudioTrack(track *webrtc.TrackRemote, peerID string) {
	// 这里可以添加音频处理逻辑
	// 例如：音频降噪、语音识别等
	log.Printf("开始处理音频轨道: %s", track.ID())

	// 创建一个音频处理管道
	// 实际实现中，这里应该使用Whisper或其他语音识别服务
	for {
		// 读取RTP包
		packet, _, err := track.ReadRTP()
		if err != nil {
			log.Printf("读取音频RTP包失败: %v", err)
			break
		}

		// 处理音频数据
		// 例如：将音频数据发送到语音识别服务
		log.Printf("处理音频RTP包: 序列号=%d, 时间戳=%d", packet.SequenceNumber, packet.Timestamp)
	}
}

// handleVideoTrack 处理视频轨道
func (h *WebRTCHandler) handleVideoTrack(track *webrtc.TrackRemote, peerID string) {
	// 这里可以添加视频处理逻辑
	// 例如：视频分析、手势识别等
	log.Printf("开始处理视频轨道: %s", track.ID())

	// 创建一个视频处理管道
	// 实际实现中，这里应该使用MediaPipe进行手势识别
	for {
		// 读取RTP包
		packet, _, err := track.ReadRTP()
		if err != nil {
			log.Printf("读取视频RTP包失败: %v", err)
			break
		}

		// 处理视频数据
		// 例如：将视频帧发送到手势识别服务
		log.Printf("处理视频RTP包: 序列号=%d, 时间戳=%d", packet.SequenceNumber, packet.Timestamp)
	}
}

// RemovePeer 移除对等连接
func (h *WebRTCHandler) removePeer(peerID string) {
	h.mu.Lock()
	defer h.mu.Unlock()

	if peer, exists := h.peers[peerID]; exists {
		peer.Close()
		delete(h.peers, peerID)
		log.Printf("移除对等连接: %s", peerID)
	}
}

// GetPeerConnection 获取对等连接
func (h *WebRTCHandler) GetPeerConnection(peerID string) (*webrtc.PeerConnection, bool) {
	h.mu.Lock()
	defer h.mu.Unlock()

	peer, exists := h.peers[peerID]
	return peer, exists
}

// Close 关闭所有对等连接
func (h *WebRTCHandler) Close() {
	h.mu.Lock()
	defer h.mu.Unlock()

	for peerID, peer := range h.peers {
		peer.Close()
		log.Printf("关闭对等连接: %s", peerID)
	}
	h.peers = make(map[string]*webrtc.PeerConnection)
}
