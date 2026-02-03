package vision

import (
	"log"
	"sync"
)

// ImageUnderstanding 图像理解模块
type ImageUnderstanding struct {
	imageClassifier    *ImageClassifier
	objectDetector     *ObjectDetector
	ocrEngine         *OCREngine
	imageCaptioner     *ImageCaptioner
	faceRecognizer     *FaceRecognizer
	mu                sync.Mutex
}

// ImageUnderstandingResult 图像理解结果
type ImageUnderstandingResult struct {
	Classification   []ClassificationResult
	Objects         []ObjectDetectionResult
	Text            []OCRResult
	Caption         string
	Faces           []FaceDetectionResult
}

// NewImageUnderstanding 创建一个新的图像理解模块
func NewImageUnderstanding() *ImageUnderstanding {
	return &ImageUnderstanding{
		imageClassifier:    NewImageClassifier(),
		objectDetector:     NewObjectDetector(),
		ocrEngine:         NewOCREngine(),
		imageCaptioner:     NewImageCaptioner(),
		faceRecognizer:     NewFaceRecognizer(),
	}
}

// ProcessImage 处理图像
func (iu *ImageUnderstanding) ProcessImage(imageData []byte) (*ImageUnderstandingResult, error) {
	 iu.mu.Lock()
	defer iu.mu.Unlock()

	log.Printf("开始处理图像，大小: %d bytes", len(imageData))

	// 初始化结果
	result := &ImageUnderstandingResult{}

	// 图像分类
	classification, err := iu.imageClassifier.Classify(imageData)
	if err != nil {
		log.Printf("图像分类失败: %v", err)
	} else {
		result.Classification = classification
	}

	// 物体检测
	objects, err := iu.objectDetector.Detect(imageData)
	if err != nil {
		log.Printf("物体检测失败: %v", err)
	} else {
		result.Objects = objects
	}

	// OCR
	text, err := iu.ocrEngine.RecognizeText(imageData)
	if err != nil {
		log.Printf("OCR失败: %v", err)
	} else {
		result.Text = text
	}

	// 图像描述
	caption, err := iu.imageCaptioner.GenerateCaption(imageData)
	if err != nil {
		log.Printf("图像描述生成失败: %v", err)
	} else {
		result.Caption = caption
	}

	// 人脸识别
	faces, err := iu.faceRecognizer.DetectFaces(imageData)
	if err != nil {
		log.Printf("人脸识别失败: %v", err)
	} else {
		result.Faces = faces
	}

	log.Println("图像处理完成")

	return result, nil
}

// ClassifyImage 仅进行图像分类
func (iu *ImageUnderstanding) ClassifyImage(imageData []byte) ([]ClassificationResult, error) {
	 iu.mu.Lock()
	defer iu.mu.Unlock()

	return iu.imageClassifier.Classify(imageData)
}

// DetectObjects 仅进行物体检测
func (iu *ImageUnderstanding) DetectObjects(imageData []byte) ([]ObjectDetectionResult, error) {
	 iu.mu.Lock()
	defer iu.mu.Unlock()

	return iu.objectDetector.Detect(imageData)
}

// RecognizeText 仅进行文本识别
func (iu *ImageUnderstanding) RecognizeText(imageData []byte) ([]OCRResult, error) {
	 iu.mu.Lock()
	defer iu.mu.Unlock()

	return iu.ocrEngine.RecognizeText(imageData)
}

// GenerateCaption 仅生成图像描述
func (iu *ImageUnderstanding) GenerateCaption(imageData []byte) (string, error) {
	 iu.mu.Lock()
	defer iu.mu.Unlock()

	return iu.imageCaptioner.GenerateCaption(imageData)
}

// DetectFaces 仅进行人脸识别
func (iu *ImageUnderstanding) DetectFaces(imageData []byte) ([]FaceDetectionResult, error) {
	 iu.mu.Lock()
	defer iu.mu.Unlock()

	return iu.faceRecognizer.DetectFaces(imageData)
}
