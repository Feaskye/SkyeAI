package com.skyeai.jarvis.user.controller;

import com.skyeai.jarvis.user.service.sensing.ActiveSensingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/sensing")
public class ActiveSensingController {

    @Autowired
    private ActiveSensingService activeSensingService;

    @PostMapping("/start")
    public void startSensing() {
        activeSensingService.startSensing();
    }

    @PostMapping("/stop")
    public void stopSensing() {
        activeSensingService.stopSensing();
    }

    @GetMapping("/status")
    public Map<String, Object> getSensingStatus() {
        return activeSensingService.getSensingStatus();
    }

    @GetMapping("/data")
    public Map<String, Object> getSensingData() {
        return activeSensingService.getSensingData();
    }

    @PostMapping("/configure")
    public Map<String, Object> configureSensing(@RequestBody Map<String, Object> configuration) {
        return activeSensingService.configureSensing(configuration);
    }

    @GetMapping("/predict-needs")
    public Map<String, Object> predictUserNeeds() {
        return activeSensingService.predictUserNeeds();
    }

    @GetMapping("/analyze-behavior")
    public Map<String, Object> analyzeUserBehavior() {
        return activeSensingService.analyzeUserBehavior();
    }

    @GetMapping("/generate-recommendations")
    public Map<String, Object> generateRecommendations() {
        return activeSensingService.generateRecommendations();
    }

    @PostMapping("/make-decision")
    public Map<String, Object> makeAutonomousDecision(@RequestBody Map<String, Object> context) {
        return activeSensingService.makeAutonomousDecision(context);
    }

    @GetMapping("/decision-history")
    public Map<String, Object> getDecisionHistory() {
        return activeSensingService.getDecisionHistory();
    }
}