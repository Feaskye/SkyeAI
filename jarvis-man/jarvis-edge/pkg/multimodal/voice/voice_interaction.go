package voice

import (
	"log"
	"sync"
)

// EmotionType 情感类型
type EmotionType string

const (
	// EmotionNeutral 中性
	EmotionNeutral EmotionType = "NEUTRAL"
	// EmotionHappy 高兴
	EmotionHappy EmotionType = "HAPPY"
	// EmotionSad 悲伤
	EmotionSad EmotionType = "SAD"
	// EmotionAngry 愤怒
	EmotionAngry EmotionType = "ANGRY"
	// EmotionSurprised 惊讶
	EmotionSurprised EmotionType = "SURPRISED"
	// EmotionFearful 恐惧
	EmotionFearful EmotionType = "FEARFUL"
	// EmotionUnknown 未知
	EmotionUnknown EmotionType = "UNKNOWN"
)

// VoiceInteraction 语音交互模块
type VoiceInteraction struct {
	recognizer  *SpeechRecognizer
	tts         *TextToSpeech
	emotionDetector *EmotionDetector
	mu          sync.Mutex
}

// NewVoiceInteraction 创建一个新的语音交互模块
func NewVoiceInteraction(modelPath string, language string, voice string) *VoiceInteraction {
	return &VoiceInteraction{
		recognizer:  NewSpeechRecognizer(modelPath, language),
		tts:         NewTextToSpeech(modelPath, voice),
		emotionDetector: NewEmotionDetector(),
	}
}

// ProcessSpeech 处理语音输入
func (vi *VoiceInteraction) ProcessSpeech(audioData []byte) (*SpeechRecognitionResult, EmotionType, error) {
	vi.mu.Lock()
	defer vi.mu.Unlock()

	log.Println("开始处理语音输入")

	// 识别语音
	result, err := vi.recognizer.Recognize(audioData)
	if err != nil {
		log.Printf("语音识别失败: %v", err)
		return nil, EmotionUnknown, err
	}

	// 检测情感
	emotion := vi.emotionDetector.DetectEmotion(audioData)

	log.Printf("语音处理完成，识别结果: %s, 情感: %s", result.Text, emotion)

	return result, emotion, nil
}

// GenerateSpeech 生成语音输出
func (vi *VoiceInteraction) GenerateSpeech(text string, emotion EmotionType) ([]byte, error) {
	vi.mu.Lock()
	defer vi.mu.Unlock()

	log.Printf("开始生成语音输出，情感: %s", emotion)

	// 根据情感调整文本
	adjustedText := vi.adjustTextForEmotion(text, emotion)

	// 合成语音
	audioData, err := vi.tts.Synthesize(adjustedText)
	if err != nil {
		log.Printf("语音合成失败: %v", err)
		return nil, err
	}

	log.Println("语音生成完成")

	return audioData, nil
}

// adjustTextForEmotion 根据情感调整文本
func (vi *VoiceInteraction) adjustTextForEmotion(text string, emotion EmotionType) string {
	// 根据情感类型调整文本
	// 例如：为高兴的情感添加感叹号，为悲伤的情感添加委婉的表达
	switch emotion {
	case EmotionHappy:
		return text + "！"
	case EmotionSad:
		return "很遗憾，" + text
	case EmotionAngry:
		return "请注意，" + text
	case EmotionSurprised:
		return "哇，" + text
	case EmotionFearful:
		return "不用担心，" + text
	default:
		return text
	}
}

// GetSupportedLanguages 获取支持的语言
func (vi *VoiceInteraction) GetSupportedLanguages() []string {
	return vi.recognizer.GetSupportedLanguages()
}

// GetSupportedVoices 获取支持的语音
func (vi *VoiceInteraction) GetSupportedVoices() []string {
	return vi.tts.GetSupportedVoices()
}
