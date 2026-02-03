package input

import (
	"log"
	"sync"

	"github.com/pion/webrtc/v3"
)

// MultimodalInput 多模态输入处理器
type MultimodalInput struct {
	webRTCHandler    *WebRTCHandler
	gestureHandler   *GestureHandler
	activeSessions   map[string]*Session
	mu               sync.Mutex
}

// Session 多模态会话
type Session struct {
	ID              string
	PeerConnection  *webrtc.PeerConnection
	AudioActive     bool
	VideoActive     bool
	GestureActive   bool
}

// NewMultimodalInput 创建一个新的多模态输入处理器
func NewMultimodalInput() *MultimodalInput {
	return &MultimodalInput{
		webRTCHandler:  NewWebRTCHandler(),
		gestureHandler: NewGestureHandler(),
		activeSessions: make(map[string]*Session),
	}
}

// CreateSession 创建一个新的多模态会话
func (m *MultimodalInput) CreateSession(sessionID string) (*Session, error) {
	m.mu.Lock()
	defer m.mu.Unlock()

	// 创建WebRTC对等连接
	peerConnection, err := m.webRTCHandler.CreatePeerConnection(sessionID)
	if err != nil {
		return nil, err
	}

	// 创建会话
	session := &Session{
		ID:              sessionID,
		PeerConnection:  peerConnection,
		AudioActive:     false,
		VideoActive:     false,
		GestureActive:   false,
	}

	// 存储会话
	m.activeSessions[sessionID] = session

	log.Printf("创建多模态会话: %s", sessionID)

	return session, nil
}

// CloseSession 关闭多模态会话
func (m *MultimodalInput) CloseSession(sessionID string) {
	m.mu.Lock()
	defer m.mu.Unlock()

	// 获取会话
	session, exists := m.activeSessions[sessionID]
	if !exists {
		log.Printf("会话不存在: %s", sessionID)
		return
	}

	// 关闭对等连接
	session.PeerConnection.Close()

	// 删除会话
	delete(m.activeSessions, sessionID)

	log.Printf("关闭多模态会话: %s", sessionID)
}

// GetSession 获取多模态会话
func (m *MultimodalInput) GetSession(sessionID string) (*Session, bool) {
	m.mu.Lock()
	defer m.mu.Unlock()

	session, exists := m.activeSessions[sessionID]
	return session, exists
}

// RegisterGestureCallback 注册手势回调
func (m *MultimodalInput) RegisterGestureCallback(gesture GestureType, callback func(GestureType)) {
	m.gestureHandler.RegisterCallback(gesture, callback)
}

// ProcessVideoFrame 处理视频帧
func (m *MultimodalInput) ProcessVideoFrame(sessionID string, frame []byte) {
	// 处理视频帧，进行手势识别
	m.gestureHandler.ProcessFrame(frame)
}

// GetSupportedGestures 获取支持的手势类型
func (m *MultimodalInput) GetSupportedGestures() []GestureType {
	return m.gestureHandler.GetSupportedGestures()
}

// Close 关闭多模态输入处理器
func (m *MultimodalInput) Close() {
	m.mu.Lock()
	defer m.mu.Unlock()

	// 关闭所有会话
	for sessionID := range m.activeSessions {
		m.CloseSession(sessionID)
	}

	// 清理WebRTC处理器
	m.webRTCHandler.Close()

	log.Println("关闭多模态输入处理器")
}
