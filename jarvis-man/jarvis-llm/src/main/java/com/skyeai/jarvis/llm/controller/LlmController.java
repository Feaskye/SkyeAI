package com.skyeai.jarvis.llm.controller;

import com.skyeai.jarvis.llm.service.LlmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/llm")
public class LlmController {

    @Autowired
    private LlmService llmService;

    @PostMapping("/react")
    public String executeReact(@RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        return llmService.executeReact(query);
    }

    @PostMapping("/generate/text")
    public Map<String, Object> generateText(@RequestBody Map<String, Object> request) {
        String prompt = (String) request.get("prompt");
        String systemPrompt = (String) request.getOrDefault("systemPrompt", "You are a helpful assistant");
        String result = llmService.generateText(systemPrompt, prompt);
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("result", result);
        return response;
    }

    @PostMapping("/task/plan")
    public String generateTaskPlan(@RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        String contextInfo = (String) request.getOrDefault("contextInfo", "");
        String userPreferences = (String) request.getOrDefault("userPreferences", "");
        return llmService.generateTaskPlan(query, contextInfo, userPreferences);
    }

    @PostMapping("/think")
    public String generateThought(@RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        String history = (String) request.getOrDefault("history", "");
        String contextInfo = (String) request.getOrDefault("contextInfo", "");
        String userPreferences = (String) request.getOrDefault("userPreferences", "");
        String taskPlan = (String) request.getOrDefault("taskPlan", "");
        java.util.List<String> tools = (java.util.List<String>) request.getOrDefault("tools", new java.util.ArrayList<>());
        return llmService.generateThought(query, history, contextInfo, userPreferences, taskPlan, tools);
    }

    @PostMapping("/action/decide")
    public String decideAction(@RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        String history = (String) request.getOrDefault("history", "");
        String contextInfo = (String) request.getOrDefault("contextInfo", "");
        String userPreferences = (String) request.getOrDefault("userPreferences", "");
        String taskPlan = (String) request.getOrDefault("taskPlan", "");
        java.util.List<String> tools = (java.util.List<String>) request.getOrDefault("tools", new java.util.ArrayList<>());
        return llmService.decideAction(query, history, contextInfo, userPreferences, taskPlan, tools);
    }

    @PostMapping("/progress/evaluate")
    public boolean evaluateProgress(@RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        String history = (String) request.getOrDefault("history", "");
        String taskPlan = (String) request.getOrDefault("taskPlan", "");
        String observation = (String) request.getOrDefault("observation", "");
        return llmService.evaluateProgress(query, history, taskPlan, observation);
    }

    @PostMapping("/answer/generate")
    public String generateFinalAnswer(@RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        String history = (String) request.getOrDefault("history", "");
        String contextInfo = (String) request.getOrDefault("contextInfo", "");
        String userPreferences = (String) request.getOrDefault("userPreferences", "");
        String taskPlan = (String) request.getOrDefault("taskPlan", "");
        return llmService.generateFinalAnswer(query, history, contextInfo, userPreferences, taskPlan);
    }

    @GetMapping("/models")
    public java.util.List<String> listModels() {
        return llmService.listModels();
    }

    @PostMapping("/process/image")
    public com.skyeai.jarvis.llm.service.LlmService.ImageProcessingResult processImage(
            @RequestParam("image") org.springframework.web.multipart.MultipartFile image,
            @RequestParam(value = "imageType", required = false) String imageType) {
        try {
            if (imageType == null) {
                imageType = image.getContentType();
            }
            return llmService.processImage(image.getInputStream(), imageType);
        } catch (Exception e) {
            com.skyeai.jarvis.llm.service.LlmService.ImageProcessingResult result = new com.skyeai.jarvis.llm.service.LlmService.ImageProcessingResult();
            result.setSuccess(false);
            result.setErrorMessage("Error processing image: " + e.getMessage());
            return result;
        }
    }

    @PostMapping("/process/speech")
    public com.skyeai.jarvis.llm.service.LlmService.SpeechProcessingResult processSpeech(
            @RequestParam("audio") org.springframework.web.multipart.MultipartFile audio,
            @RequestParam(value = "audioType", required = false) String audioType) {
        try {
            if (audioType == null) {
                audioType = audio.getContentType();
            }
            return llmService.processSpeech(audio.getInputStream(), audioType);
        } catch (Exception e) {
            com.skyeai.jarvis.llm.service.LlmService.SpeechProcessingResult result = new com.skyeai.jarvis.llm.service.LlmService.SpeechProcessingResult();
            result.setSuccess(false);
            result.setErrorMessage("Error processing speech: " + e.getMessage());
            return result;
        }
    }

    @PostMapping("/process/multimodal")
    public com.skyeai.jarvis.llm.service.LlmService.MultimodalFusionResult processMultimodal(
            @RequestBody java.util.Map<String, Object> request) {
        return llmService.fuseMultimodalInformation(request);
    }
}
