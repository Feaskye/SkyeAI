package com.skyeai.jarvis.service;


import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Service
public class SkillMarketService {

    private static final Logger logger = Logger.getLogger(SkillMarketService.class.getName());

    // 技能注册表
    private final Map<String, SkillInfo> skills = new HashMap<>();

    // 社区贡献数据
    private final Map<String, CommunityData> communityData = new HashMap<>();

    /**
     * 技能信息实体类
     */
    public static class SkillInfo {
        private String id;
        private String name;
        private String version;
        private String description;
        private String author;
        private String category;
        private String license;
        private String homepage;
        private String downloadUrl;
        private double rating;
        private int downloadCount;
        private int reviewCount;
        private Map<String, Object> metadata;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getLicense() {
            return license;
        }

        public void setLicense(String license) {
            this.license = license;
        }

        public String getHomepage() {
            return homepage;
        }

        public void setHomepage(String homepage) {
            this.homepage = homepage;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public void setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

        public double getRating() {
            return rating;
        }

        public void setRating(double rating) {
            this.rating = rating;
        }

        public int getDownloadCount() {
            return downloadCount;
        }

        public void setDownloadCount(int downloadCount) {
            this.downloadCount = downloadCount;
        }

        public int getReviewCount() {
            return reviewCount;
        }

        public void setReviewCount(int reviewCount) {
            this.reviewCount = reviewCount;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }

    /**
     * 社区数据实体类
     */
    public static class CommunityData {
        private Map<String, Object> contributors;
        private Map<String, Object> statistics;
        private List<Map<String, Object>> reviews;
        private List<Map<String, Object>> feedback;

        // Getters and Setters
        public Map<String, Object> getContributors() {
            return contributors;
        }

        public void setContributors(Map<String, Object> contributors) {
            this.contributors = contributors;
        }

        public Map<String, Object> getStatistics() {
            return statistics;
        }

        public void setStatistics(Map<String, Object> statistics) {
            this.statistics = statistics;
        }

        public List<Map<String, Object>> getReviews() {
            return reviews;
        }

        public void setReviews(List<Map<String, Object>> reviews) {
            this.reviews = reviews;
        }

        public List<Map<String, Object>> getFeedback() {
            return feedback;
        }

        public void setFeedback(List<Map<String, Object>> feedback) {
            this.feedback = feedback;
        }
    }

    /**
     * 初始化技能市场服务
     */
    @PostConstruct
    public void initialize() {
        logger.info("初始化技能市场服务");
        
        // 加载默认技能
        loadDefaultSkills();
    }

    /**
     * 关闭技能市场服务
     */
    @PreDestroy
    public void shutdown() {
        logger.info("关闭技能市场服务");
        skills.clear();
        communityData.clear();
    }

    /**
     * 加载默认技能
     */
    private void loadDefaultSkills() {
        // 添加默认技能
        SkillInfo browserSkill = new SkillInfo();
        browserSkill.setId("browser-1");
        browserSkill.setName("Browser Control");
        browserSkill.setVersion("1.0.0");
        browserSkill.setDescription("控制无头浏览器，抓取/操作网页");
        browserSkill.setAuthor("SkyeAI");
        browserSkill.setCategory("Productivity");
        browserSkill.setLicense("MIT");
        browserSkill.setHomepage("https://github.com/skyeai/jarvis-plugins");
        browserSkill.setDownloadUrl("https://plugins.example.com/browser-1.0.0.jar");
        browserSkill.setRating(4.5);
        browserSkill.setDownloadCount(1000);
        browserSkill.setReviewCount(50);
        skills.put(browserSkill.getId(), browserSkill);
        
        SkillInfo sandboxSkill = new SkillInfo();
        sandboxSkill.setId("sandbox-1");
        sandboxSkill.setName("Code Sandbox");
        sandboxSkill.setVersion("1.0.0");
        sandboxSkill.setDescription("在安全沙箱中执行代码");
        sandboxSkill.setAuthor("SkyeAI");
        sandboxSkill.setCategory("Development");
        sandboxSkill.setLicense("MIT");
        sandboxSkill.setHomepage("https://github.com/skyeai/jarvis-plugins");
        sandboxSkill.setDownloadUrl("https://plugins.example.com/sandbox-1.0.0.jar");
        sandboxSkill.setRating(4.7);
        sandboxSkill.setDownloadCount(800);
        sandboxSkill.setReviewCount(40);
        skills.put(sandboxSkill.getId(), sandboxSkill);
        
        logger.info("加载默认技能: " + skills.keySet());
    }

    /**
     * 获取技能列表
     */
    public List<SkillInfo> getSkills() {
        return skills.values().stream().toList();
    }

    /**
     * 获取技能详情
     */
    public SkillInfo getSkill(String skillId) {
        return skills.get(skillId);
    }

    /**
     * 搜索技能
     */
    public List<SkillInfo> searchSkills(String keyword) {
        return skills.values().stream()
                .filter(skill -> skill.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                        skill.getDescription().toLowerCase().contains(keyword.toLowerCase()) ||
                        skill.getCategory().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }

    /**
     * 按分类获取技能
     */
    public List<SkillInfo> getSkillsByCategory(String category) {
        return skills.values().stream()
                .filter(skill -> skill.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    /**
     * 下载技能
     */
    public CompletableFuture<Boolean> downloadSkill(String skillId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                SkillInfo skill = skills.get(skillId);
                if (skill == null) {
                    throw new RuntimeException("技能不存在: " + skillId);
                }
                
                logger.info("下载技能: " + skill.getName() + " (" + skill.getVersion() + ")");
                
                // 模拟下载过程
                Thread.sleep(2000);
                
                // 更新下载计数
                skill.setDownloadCount(skill.getDownloadCount() + 1);
                
                logger.info("技能下载完成: " + skill.getName());
                return true;
            } catch (Exception e) {
                logger.severe("下载技能失败: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * 提交评分
     */
    public boolean submitRating(String skillId, double rating, String review) {
        try {
            SkillInfo skill = skills.get(skillId);
            if (skill == null) {
                throw new RuntimeException("技能不存在: " + skillId);
            }
            
            // 模拟提交评分
            logger.info("提交评分: " + skillId + " - " + rating + "星");
            
            // 更新评分
            int currentReviewCount = skill.getReviewCount();
            double currentRating = skill.getRating();
            double newRating = (currentRating * currentReviewCount + rating) / (currentReviewCount + 1);
            skill.setRating(Math.round(newRating * 10) / 10.0);
            skill.setReviewCount(currentReviewCount + 1);
            
            // 添加评论
            CommunityData data = communityData.computeIfAbsent(skillId, k -> new CommunityData());
            if (data.getReviews() == null) {
                data.setReviews(new java.util.ArrayList<>());
            }
            Map<String, Object> reviewMap = new HashMap<>();
            reviewMap.put("rating", rating);
            reviewMap.put("review", review);
            reviewMap.put("timestamp", System.currentTimeMillis());
            data.getReviews().add(reviewMap);
            
            logger.info("评分提交成功: " + skillId);
            return true;
        } catch (Exception e) {
            logger.severe("提交评分失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 提交反馈
     */
    public boolean submitFeedback(String skillId, Map<String, Object> feedback) {
        try {
            SkillInfo skill = skills.get(skillId);
            if (skill == null) {
                throw new RuntimeException("技能不存在: " + skillId);
            }
            
            logger.info("提交反馈: " + skillId);
            
            // 添加反馈
            CommunityData data = communityData.computeIfAbsent(skillId, k -> new CommunityData());
            if (data.getFeedback() == null) {
                data.setFeedback(new java.util.ArrayList<>());
            }
            feedback.put("timestamp", System.currentTimeMillis());
            data.getFeedback().add(feedback);
            
            logger.info("反馈提交成功: " + skillId);
            return true;
        } catch (Exception e) {
            logger.severe("提交反馈失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取社区数据
     */
    public CommunityData getCommunityData(String skillId) {
        return communityData.get(skillId);
    }

    /**
     * 发布技能
     */
    public boolean publishSkill(SkillInfo skillInfo) {
        try {
            String skillId = skillInfo.getName().toLowerCase().replace(" ", "-") + "-" + skillInfo.getVersion();
            skillInfo.setId(skillId);
            skills.put(skillId, skillInfo);
            
            logger.info("发布技能: " + skillInfo.getName() + " (" + skillInfo.getVersion() + ")");
            return true;
        } catch (Exception e) {
            logger.severe("发布技能失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 更新技能
     */
    public boolean updateSkill(String skillId, SkillInfo skillInfo) {
        try {
            if (!skills.containsKey(skillId)) {
                throw new RuntimeException("技能不存在: " + skillId);
            }
            
            skillInfo.setId(skillId);
            skills.put(skillId, skillInfo);
            
            logger.info("更新技能: " + skillId);
            return true;
        } catch (Exception e) {
            logger.severe("更新技能失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 删除技能
     */
    public boolean deleteSkill(String skillId) {
        try {
            if (!skills.containsKey(skillId)) {
                throw new RuntimeException("技能不存在: " + skillId);
            }
            
            skills.remove(skillId);
            communityData.remove(skillId);
            
            logger.info("删除技能: " + skillId);
            return true;
        } catch (Exception e) {
            logger.severe("删除技能失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取热门技能
     */
    public List<SkillInfo> getPopularSkills() {
        return skills.values().stream()
                .sorted((s1, s2) -> Integer.compare(s2.getDownloadCount(), s1.getDownloadCount()))
                .limit(10)
                .toList();
    }

    /**
     * 获取评分最高的技能
     */
    public List<SkillInfo> getTopRatedSkills() {
        return skills.values().stream()
                .sorted((s1, s2) -> Double.compare(s2.getRating(), s1.getRating()))
                .limit(10)
                .toList();
    }
}
