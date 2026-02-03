package com.skyeai.jarvis.controller;

import com.skyeai.jarvis.service.SkillMarketService;
import com.skyeai.jarvis.service.SkillMarketService.SkillInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/skill-market")
public class SkillMarketController {

    @Autowired
    private SkillMarketService skillMarketService;

    /**
     * 获取技能列表
     */
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getSkills() {
        List<SkillInfo> skills = skillMarketService.getSkills();
        List<Map<String, Object>> skillList = skills.stream().map(skill -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", skill.getId());
            map.put("name", skill.getName());
            map.put("version", skill.getVersion());
            map.put("description", skill.getDescription());
            map.put("author", skill.getAuthor());
            map.put("category", skill.getCategory());
            map.put("rating", skill.getRating());
            map.put("downloadCount", skill.getDownloadCount());
            map.put("reviewCount", skill.getReviewCount());
            return map;
        }).toList();
        return ResponseEntity.ok(skillList);
    }

    /**
     * 获取技能详情
     */
    @GetMapping("/get/{skillId}")
    public ResponseEntity<Map<String, Object>> getSkill(@PathVariable String skillId) {
        SkillInfo skill = skillMarketService.getSkill(skillId);
        if (skill == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "技能不存在"));
        }

        Map<String, Object> skillInfo = new HashMap<>();
        skillInfo.put("id", skill.getId());
        skillInfo.put("name", skill.getName());
        skillInfo.put("version", skill.getVersion());
        skillInfo.put("description", skill.getDescription());
        skillInfo.put("author", skill.getAuthor());
        skillInfo.put("category", skill.getCategory());
        skillInfo.put("license", skill.getLicense());
        skillInfo.put("homepage", skill.getHomepage());
        skillInfo.put("downloadUrl", skill.getDownloadUrl());
        skillInfo.put("rating", skill.getRating());
        skillInfo.put("downloadCount", skill.getDownloadCount());
        skillInfo.put("reviewCount", skill.getReviewCount());
        skillInfo.put("metadata", skill.getMetadata());

        return ResponseEntity.ok(Map.of("success", true, "data", skillInfo));
    }

    /**
     * 搜索技能
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchSkills(@RequestParam String keyword) {
        List<SkillInfo> skills = skillMarketService.searchSkills(keyword);
        List<Map<String, Object>> skillList = skills.stream().map(skill -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", skill.getId());
            map.put("name", skill.getName());
            map.put("version", skill.getVersion());
            map.put("description", skill.getDescription());
            map.put("author", skill.getAuthor());
            map.put("category", skill.getCategory());
            map.put("rating", skill.getRating());
            map.put("downloadCount", skill.getDownloadCount());
            return map;
        }).toList();
        return ResponseEntity.ok(skillList);
    }

    /**
     * 按分类获取技能
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Map<String, Object>>> getSkillsByCategory(@PathVariable String category) {
        List<SkillInfo> skills = skillMarketService.getSkillsByCategory(category);
        List<Map<String, Object>> skillList = skills.stream().map(skill -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", skill.getId());
            map.put("name", skill.getName());
            map.put("version", skill.getVersion());
            map.put("description", skill.getDescription());
            map.put("author", skill.getAuthor());
            map.put("rating", skill.getRating());
            map.put("downloadCount", skill.getDownloadCount());
            return map;
        }).toList();
        return ResponseEntity.ok(skillList);
    }

    /**
     * 下载技能
     */
    @PostMapping("/download/{skillId}")
    public ResponseEntity<Map<String, Object>> downloadSkill(@PathVariable String skillId) {
        boolean success = skillMarketService.downloadSkill(skillId).join();
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "技能下载成功"));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "技能下载失败"));
        }
    }

    /**
     * 提交评分
     */
    @PostMapping("/rate/{skillId}")
    public ResponseEntity<Map<String, Object>> submitRating(@PathVariable String skillId, @RequestBody Map<String, Object> ratingData) {
        double rating = (double) ratingData.getOrDefault("rating", 0.0);
        String review = (String) ratingData.getOrDefault("review", "");

        boolean success = skillMarketService.submitRating(skillId, rating, review);
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "评分提交成功"));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "评分提交失败"));
        }
    }

    /**
     * 提交反馈
     */
    @PostMapping("/feedback/{skillId}")
    public ResponseEntity<Map<String, Object>> submitFeedback(@PathVariable String skillId, @RequestBody Map<String, Object> feedback) {
        boolean success = skillMarketService.submitFeedback(skillId, feedback);
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "反馈提交成功"));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "反馈提交失败"));
        }
    }

    /**
     * 获取社区数据
     */
    @GetMapping("/community/{skillId}")
    public ResponseEntity<Map<String, Object>> getCommunityData(@PathVariable String skillId) {
        var communityData = skillMarketService.getCommunityData(skillId);
        if (communityData == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "社区数据不存在"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                        "contributors", communityData.getContributors(),
                        "statistics", communityData.getStatistics(),
                        "reviews", communityData.getReviews(),
                        "feedback", communityData.getFeedback()
                )
        ));
    }

    /**
     * 发布技能
     */
    @PostMapping("/publish")
    public ResponseEntity<Map<String, Object>> publishSkill(@RequestBody SkillInfo skillInfo) {
        boolean success = skillMarketService.publishSkill(skillInfo);
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "技能发布成功", "skillId", skillInfo.getId()));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "技能发布失败"));
        }
    }

    /**
     * 更新技能
     */
    @PutMapping("/update/{skillId}")
    public ResponseEntity<Map<String, Object>> updateSkill(@PathVariable String skillId, @RequestBody SkillInfo skillInfo) {
        boolean success = skillMarketService.updateSkill(skillId, skillInfo);
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "技能更新成功"));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "技能更新失败"));
        }
    }

    /**
     * 删除技能
     */
    @DeleteMapping("/delete/{skillId}")
    public ResponseEntity<Map<String, Object>> deleteSkill(@PathVariable String skillId) {
        boolean success = skillMarketService.deleteSkill(skillId);
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "技能删除成功"));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "技能删除失败"));
        }
    }

    /**
     * 获取热门技能
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Map<String, Object>>> getPopularSkills() {
        List<SkillInfo> skills = skillMarketService.getPopularSkills();
        List<Map<String, Object>> skillList = skills.stream().map(skill -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", skill.getId());
            map.put("name", skill.getName());
            map.put("version", skill.getVersion());
            map.put("description", skill.getDescription());
            map.put("author", skill.getAuthor());
            map.put("category", skill.getCategory());
            map.put("rating", skill.getRating());
            map.put("downloadCount", skill.getDownloadCount());
            return map;
        }).toList();
        return ResponseEntity.ok(skillList);
    }

    /**
     * 获取评分最高的技能
     */
    @GetMapping("/top-rated")
    public ResponseEntity<List<Map<String, Object>>> getTopRatedSkills() {
        List<SkillInfo> skills = skillMarketService.getTopRatedSkills();
        List<Map<String, Object>> skillList = skills.stream().map(skill -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", skill.getId());
            map.put("name", skill.getName());
            map.put("version", skill.getVersion());
            map.put("description", skill.getDescription());
            map.put("author", skill.getAuthor());
            map.put("category", skill.getCategory());
            map.put("rating", skill.getRating());
            map.put("reviewCount", skill.getReviewCount());
            return map;
        }).toList();
        return ResponseEntity.ok(skillList);
    }
    
    /**
     * 测试方法
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        return ResponseEntity.ok(Map.of(
                "success", "true",
                "message", "SkillMarketController is working!"
        ));
    }
}
