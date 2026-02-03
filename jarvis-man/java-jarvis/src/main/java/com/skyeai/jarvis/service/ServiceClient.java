package com.skyeai.jarvis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 服务客户端，用于调用其他专门服务
 */
@Service
public class ServiceClient {

    private final RestTemplate restTemplate;

    @Value("${llm.service.url:http://localhost:8081}")
    private String llmServiceUrl;

    @Value("${knowledge.service.url:http://localhost:8083}")
    private String knowledgeServiceUrl;

    @Value("${multimodal.service.url:http://localhost:8081}")
    private String multimodalServiceUrl;

    @Value("${user.service.url:http://localhost:8085}")
    private String userServiceUrl;

    @Value("${finance.service.url:http://localhost:8084}")
    private String financeServiceUrl;

    @Value("${skills.service.url:http://localhost:8086}")
    private String skillsServiceUrl;

    @Value("${cognition.service.url:http://localhost:8083}")
    private String cognitionServiceUrl;

    @Value("${recommendation.service.url:http://jarvis-recommendation:8088}")
    private String recommendationServiceUrl;

    @Value("${plugin.service.url:http://jarvis-plugin:8089}")
    private String pluginServiceUrl;

    @Autowired
    public ServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // LLM服务调用
    public Map<String, Object> callLlmService(String endpoint, Map<String, Object> request) {
        String url = llmServiceUrl + "/api/llm" + endpoint;
        return restTemplate.postForObject(url, request, Map.class);
    }

    // 知识服务调用
    public Map<String, Object> callKnowledgeService(String endpoint, Map<String, Object> request) {
        String url = knowledgeServiceUrl + "/api/knowledge" + endpoint;
        return restTemplate.postForObject(url, request, Map.class);
    }

    // 多模态服务调用
    public Map<String, Object> callMultimodalService(String endpoint, Map<String, Object> request) {
        String url = multimodalServiceUrl + "/api/multimodal" + endpoint;
        return restTemplate.postForObject(url, request, Map.class);
    }

    // 用户服务调用（健康数据和异常检测）
    public Map<String, Object> callUserService(String endpoint, Map<String, Object> request) {
        String url = userServiceUrl + "/api/user" + endpoint;
        return restTemplate.postForObject(url, request, Map.class);
    }

    // 金融服务调用（股票监测）
    public Map<String, Object> callFinanceService(String endpoint, Map<String, Object> request) {
        String url = financeServiceUrl + "/api/finance" + endpoint;
        return restTemplate.postForObject(url, request, Map.class);
    }

    // 技能服务调用（工具管理）
    public Map<String, Object> callSkillsService(String endpoint, Map<String, Object> request) {
        String url = skillsServiceUrl + "/api/skills" + endpoint;
        return restTemplate.postForObject(url, request, Map.class);
    }

    // 认知服务调用（ReAct决策）
    public String callCognitionService(String endpoint, Map<String, Object> request) {
        String url = cognitionServiceUrl + "/api/llm" + endpoint;
        return restTemplate.postForObject(url, request, String.class);
    }

    // 知识服务GET调用
    public Map<String, Object> getFromKnowledgeService(String endpoint) {
        String url = knowledgeServiceUrl + "/api/knowledge" + endpoint;
        return restTemplate.getForObject(url, Map.class);
    }

    // 多模态服务GET调用
    public Map<String, Object> getFromMultimodalService(String endpoint) {
        String url = multimodalServiceUrl + "/api/multimodal" + endpoint;
        return restTemplate.getForObject(url, Map.class);
    }

    // 用户服务GET调用
    public Map<String, Object> getFromUserService(String endpoint) {
        String url = userServiceUrl + "/api/user" + endpoint;
        return restTemplate.getForObject(url, Map.class);
    }

    // 金融服务GET调用
    public Map<String, Object> getFromFinanceService(String endpoint) {
        String url = financeServiceUrl + "/api/finance" + endpoint;
        return restTemplate.getForObject(url, Map.class);
    }

    // 技能服务GET调用
    public Map<String, Object> getFromSkillsService(String endpoint) {
        String url = skillsServiceUrl + "/api/skills" + endpoint;
        return restTemplate.getForObject(url, Map.class);
    }

    // 推荐服务调用
    public Map<String, Object> callRecommendationService(String endpoint, Map<String, Object> request) {
        String url = recommendationServiceUrl + "/api/recommendation" + endpoint;
        return restTemplate.postForObject(url, request, Map.class);
    }

    // 推荐服务GET调用
    public Map<String, Object> getFromRecommendationService(String endpoint) {
        String url = recommendationServiceUrl + "/api/recommendation" + endpoint;
        return restTemplate.getForObject(url, Map.class);
    }

    // 插件服务调用
    public Map<String, Object> callPluginService(String endpoint, Map<String, Object> request) {
        String url = pluginServiceUrl + "/api/plugin" + endpoint;
        return restTemplate.postForObject(url, request, Map.class);
    }

    // 插件服务GET调用
    public Map<String, Object> getFromPluginService(String endpoint) {
        String url = pluginServiceUrl + "/api/plugin" + endpoint;
        return restTemplate.getForObject(url, Map.class);
    }
}
