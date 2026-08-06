package com.skyeai.jarvis.skills.service.impl;

import com.skyeai.jarvis.skills.service.SearchService;
import com.skyeai.jarvis.skills.model.SearchRequest;
import com.skyeai.jarvis.skills.model.SearchResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BingSearchServiceImplTest {

    @Autowired
    private SearchService searchService;

    @Test
    public void testSearch() {
        // 测试搜索功能
        SearchRequest request = new SearchRequest();
        request.setQuery("人工智能最新发展");
        request.setCount(5);

        SearchResponse response = searchService.search(request);
        System.out.println("测试搜索结果:");
        System.out.println("查询: " + request.getQuery());
        System.out.println("结果数量: " + response.getResults().size());
        response.getResults().forEach(result -> {
            System.out.println("标题: " + result.getTitle());
            System.out.println("链接: " + result.getUrl());
            System.out.println("摘要: " + result.getSnippet());
            System.out.println("------------------------");
        });
    }

    @Test
    public void testSearchWithCache() {
        // 测试搜索缓存功能
        SearchRequest request = new SearchRequest();
        request.setQuery("Spring Boot 3.0 新特性");
        request.setCount(3);

        // 第一次搜索
        long startTime1 = System.currentTimeMillis();
        SearchResponse response1 = searchService.search(request);
        long endTime1 = System.currentTimeMillis();
        System.out.println("第一次搜索耗时: " + (endTime1 - startTime1) + "ms");

        // 第二次搜索（应该从缓存获取）
        long startTime2 = System.currentTimeMillis();
        SearchResponse response2 = searchService.search(request);
        long endTime2 = System.currentTimeMillis();
        System.out.println("第二次搜索耗时: " + (endTime2 - startTime2) + "ms");

        System.out.println("测试缓存功能结果:");
        System.out.println("第一次搜索结果数量: " + response1.getResults().size());
        System.out.println("第二次搜索结果数量: " + response2.getResults().size());
    }
}
