package com.xiaomi.auto.midemo.controller;

import com.xiaomi.auto.midemo.agent.OpinionAnalysisAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/opinion")
public class OpinionAnalysisController {
    private final OpinionAnalysisAgent opinionAnalysisAgent;

    @Autowired
    public OpinionAnalysisController(OpinionAnalysisAgent opinionAnalysisAgent) {
        this.opinionAnalysisAgent = opinionAnalysisAgent;
    }

    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeXiaomiAutoOpinion(
            @RequestParam String userInput,
            @RequestParam String recipientEmail) {
        try {
            // 验证用户输入是否符合触发条件
            if ("分析小米汽车的舆情".equals(userInput.trim())) {
                String result = opinionAnalysisAgent.analyzeOpinion(userInput, recipientEmail);
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body("不支持的指令，请输入: '分析小米汽车的舆情'");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("分析过程出错: " + e.getMessage());
        }
    }
}