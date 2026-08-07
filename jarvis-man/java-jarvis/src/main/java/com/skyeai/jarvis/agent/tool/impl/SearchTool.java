package com.skyeai.jarvis.agent.tool.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * 网络搜索工具
 * v10 新增：使用 Spring AI 2.0 @Tool 注解声明
 * 支持网络搜索（可配置真实API，默认模拟数据降级）
 */
@Slf4j
@Component
public class SearchTool {

    @Value("${search.api.enabled:false}")
    private boolean apiEnabled;

    @Value("${search.api.url:}")
    private String apiUrl;

    @Value("${search.api.key:}")
    private String apiKey;

    private WebClient searchWebClient;

    @org.springframework.beans.factory.annotation.Autowired
    public void init() {
        if (apiUrl != null && !apiUrl.isBlank()) {
            this.searchWebClient = WebClient.builder().baseUrl(apiUrl).build();
        }
        log.info("SearchTool初始化完成，API启用: {}", apiEnabled);
    }

    @Tool(description = "搜索网络获取信息，返回搜索结果摘要")
    public String search(
            @ToolParam(description = "搜索关键词") String query) {
        if (query == null || query.isBlank()) {
            return "搜索关键词不能为空";
        }

        log.debug("执行搜索: {}, API启用: {}", query, apiEnabled);

        if (apiEnabled && searchWebClient != null) {
            try {
                String result = searchFromApi(query);
                if (result != null && !result.isBlank()) {
                    return result;
                }
                log.warn("搜索API返回空结果，使用模拟数据");
            } catch (Exception e) {
                log.warn("搜索API调用失败，使用模拟数据: {}", e.getMessage());
            }
        }

        return simulateSearch(query);
    }

    @Tool(description = "获取指定主题的百科信息")
    public String getWikiInfo(
            @ToolParam(description = "要查询的主题或概念") String topic) {
        if (topic == null || topic.isBlank()) {
            return "查询主题不能为空";
        }

        log.debug("查询百科: {}", topic);
        return simulateWiki(topic);
    }

    private String searchFromApi(String query) {
        try {
            Map<String, Object> response = searchWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("q", query)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                if (results != null && !results.isEmpty()) {
                    StringBuilder sb = new StringBuilder("搜索结果：\n");
                    for (int i = 0; i < Math.min(results.size(), 5); i++) {
                        Map<String, Object> item = results.get(i);
                        sb.append(String.format("%d. %s\n   %s\n",
                                i + 1,
                                item.getOrDefault("title", "无标题"),
                                item.getOrDefault("snippet", "")));
                    }
                    return sb.toString();
                }
            }
            return null;
        } catch (Exception e) {
            log.error("搜索API调用异常", e);
            return null;
        }
    }

    private String simulateSearch(String query) {
        return String.format("搜索 \"%s\" 的模拟结果：\n" +
                "1. 相关资讯一：关于%s的最新动态\n" +
                "2. 相关资讯二：%s的详细解析\n" +
                "3. 相关资讯三：%s相关技术文档\n" +
                "（注：当前为模拟数据，配置 search.api.enabled=true 可启用真实搜索）", query, query, query, query);
    }

    private String simulateWiki(String topic) {
        return String.format("【%s 百科】\n%s是一个常见概念/技术/人物，" +
                "具有广泛的认知基础和应用场景。\n" +
                "（注：当前为模拟数据，配置 search.api.enabled=true 可启用真实百科查询）", topic, topic);
    }
}
