package voice

import (
	"log"
	"sync"
	"time"
)

// ConversationState 对话状态
type ConversationState string

const (
	// StateListening 监听模式
	StateListening ConversationState = "LISTENING"
	// StateProcessing 处理模式
	StateProcessing ConversationState = "PROCESSING"
	// StateSpeaking 说话模式
	StateSpeaking ConversationState = "SPEAKING"
	// StateIdle 空闲模式
	StateIdle ConversationState = "IDLE"
)

// VoiceConversation 语音对话引擎
type VoiceConversation struct {
	interaction    *VoiceInteraction
	state          ConversationState
	lastSpeechTime time.Time
	isInterrupting bool
	mu             sync.Mutex
}

// NewVoiceConversation 创建一个新的语音对话引擎
func NewVoiceConversation(modelPath string, language string, voice string) *VoiceConversation {
	return &VoiceConversation{
		interaction:    NewVoiceInteraction(modelPath, language, voice),
		state:          StateIdle,
		lastSpeechTime: time.Now(),
		isInterrupting: false,
	}
}

// StartConversation 开始对话
func (vc *VoiceConversation) StartConversation() {
	vc.mu.Lock()
	defer vc.mu.Unlock()

	vc.state = StateListening
	vc.lastSpeechTime = time.Now()
	vc.isInterrupting = false

	log.Println("对话开始，进入监听模式")
}

// EndConversation 结束对话
func (vc *VoiceConversation) EndConversation() {
	vc.mu.Lock()
	defer vc.mu.Unlock()

	vc.state = StateIdle
	log.Println("对话结束")
}

// GetState 获取当前对话状态
func (vc *VoiceConversation) GetState() ConversationState {
	vc.mu.Lock()
	defer vc.mu.Unlock()

	return vc.state
}

// ProcessAudio 处理音频输入
func (vc *VoiceConversation) ProcessAudio(audioData []byte) (*SpeechRecognitionResult, EmotionType, []byte, error) {
	vc.mu.Lock()
	currentState := vc.state
	vc.lastSpeechTime = time.Now()
	vc.mu.Unlock()

	log.Printf("处理音频输入，当前状态: %s", currentState)

	// 处理语义打断
	if currentState == StateSpeaking {
		if vc.shouldInterrupt(audioData) {
			vc.mu.Lock()
			vc.isInterrupting = true
			vc.state = StateProcessing
			vc.mu.Unlock()

			log.Println("检测到语义打断，切换到处理模式")
		}
	}

	// 处理正常的语音输入
	if currentState == StateListening || currentState == StateIdle {
		vc.mu.Lock()
		vc.state = StateProcessing
		vc.mu.Unlock()

		// 识别语音
		result, emotion, err := vc.interaction.ProcessSpeech(audioData)
		if err != nil {
			log.Printf("语音处理失败: %v", err)
			vc.mu.Lock()
			vc.state = StateListening
			vc.mu.Unlock()
			return nil, EmotionUnknown, nil, err
		}

		// 生成响应
		responseText := vc.generateResponse(result.Text, emotion)
		responseAudio, err := vc.interaction.GenerateSpeech(responseText, emotion)
		if err != nil {
			log.Printf("语音生成失败: %v", err)
			vc.mu.Lock()
			vc.state = StateListening
			vc.mu.Unlock()
			return result, emotion, nil, err
		}

		vc.mu.Lock()
		vc.state = StateSpeaking
		vc.mu.Unlock()

		log.Println("切换到说话模式")

		return result, emotion, responseAudio, nil
	}

	return nil, EmotionUnknown, nil, nil
}

// FinishSpeaking 完成说话
func (vc *VoiceConversation) FinishSpeaking() {
	vc.mu.Lock()
	defer vc.mu.Unlock()

	vc.state = StateListening
	vc.isInterrupting = false
	log.Println("说话完成，切换到监听模式")
}

// shouldInterrupt 判断是否应该打断当前说话
func (vc *VoiceConversation) shouldInterrupt(audioData []byte) bool {
	// 这里实现语义打断的逻辑
	// 1. 分析音频数据的能量和持续时间
	// 2. 进行初步的语音识别，判断是否有打断意图
	// 3. 根据打断规则判断是否应该打断

	// 简化实现：基于音频大小和能量判断
	// 实际实现中应该使用更复杂的算法
	if len(audioData) > 1000 {
		log.Println("检测到可能的打断意图")
		return true
	}

	return false
}

// generateResponse 生成响应
func (vc *VoiceConversation) generateResponse(text string, emotion EmotionType) string {
	// 这里实现响应生成的逻辑
	// 实际实现中应该调用LLM来生成响应
	// 简化实现：返回一个基于输入的响应

	log.Printf("生成响应，输入文本: %s, 情感: %s", text, emotion)

	// 简单的响应模板
	responses := map[string]string{
		"你好":                "你好！我是贾维斯，有什么可以帮助你的吗？",
		"今天天气怎么样":        "今天天气很好，适合户外活动。",
		"帮我设置一个闹钟":        "好的，请问你想设置几点的闹钟？",
		"谢谢":                "不客气，随时为你服务！",
		"再见":                "再见，祝你有愉快的一天！",
	}

	if response, ok := responses[text]; ok {
		return response
	}

	return "我理解你的意思，让我为你提供更多信息。"
}

// IsListening 检查是否处于监听模式
func (vc *VoiceConversation) IsListening() bool {
	vc.mu.Lock()
	defer vc.mu.Unlock()

	return vc.state == StateListening
}

// IsSpeaking 检查是否处于说话模式
func (vc *VoiceConversation) IsSpeaking() bool {
	vc.mu.Lock()
	defer vc.mu.Unlock()

	return vc.state == StateSpeaking
}

// IsProcessing 检查是否处于处理模式
func (vc *VoiceConversation) IsProcessing() bool {
	vc.mu.Lock()
	defer vc.mu.Unlock()

	return vc.state == StateProcessing
}

// IsIdle 检查是否处于空闲模式
func (vc *VoiceConversation) IsIdle() bool {
	vc.mu.Lock()
	defer vc.mu.Unlock()

	return vc.state == StateIdle
}

// GetLastSpeechTime 获取最后一次说话的时间
func (vc *VoiceConversation) GetLastSpeechTime() time.Time {
	vc.mu.Lock()
	defer vc.mu.Unlock()

	return vc.lastSpeechTime
}

// ResetLastSpeechTime 重置最后一次说话的时间
func (vc *VoiceConversation) ResetLastSpeechTime() {
	vc.mu.Lock()
	defer vc.mu.Unlock()

	vc.lastSpeechTime = time.Now()
}

// IsInterrupting 检查是否正在打断
func (vc *VoiceConversation) IsInterrupting() bool {
	vc.mu.Lock()
	defer vc.mu.Unlock()

	return vc.isInterrupting
}
