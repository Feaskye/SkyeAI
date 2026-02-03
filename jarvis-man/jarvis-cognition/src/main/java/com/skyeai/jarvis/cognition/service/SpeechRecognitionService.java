package com.skyeai.jarvis.cognition.service;

import java.io.InputStream;
import java.util.Map;

/**
 * 语音识别服务，用于处理语音输入
 */
public interface SpeechRecognitionService {

    /**
     * 识别语音文件
     */
    String recognizeSpeech(InputStream audioStream, String format, int sampleRate, int channels);

    /**
     * 识别Base64编码的语音
     */
    String recognizeSpeech(String base64Audio, String format, int sampleRate, int channels);

    /**
     * 批量识别语音
     */
    Map<String, String> batchRecognizeSpeech(Map<String, InputStream> audioStreams, String format, int sampleRate, int channels);

    /**
     * 语音合成
     */
    byte[] synthesizeSpeech(String text, String voice, String format, int sampleRate);

    /**
     * 语音合成为Base64
     */
    String synthesizeSpeechToBase64(String text, String voice, String format, int sampleRate);

    /**
     * 语音情感分析
     */
    Map<String, Object> analyzeSpeechEmotion(InputStream audioStream, String format, int sampleRate, int channels);

    /**
     * 语音识别配置
     */
    void setConfig(Map<String, Object> config);

    /**
     * 获取支持的语言
     */
    String[] getSupportedLanguages();

    /**
     * 获取支持的声音
     */
    String[] getSupportedVoices();

    /**
     * 检查服务状态
     */
    boolean checkServiceStatus();
}