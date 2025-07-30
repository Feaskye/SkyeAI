package com.xiaomi.auto.midemo.service.datasource;

import com.xiaomi.auto.midemo.dto.SocialMediaPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据源聚合器，统一管理和调用多个社交媒体数据源
 */
@Service
public class DataSourceAggregator {
    private final List<SocialMediaDataSource> dataSources;

    @Autowired
    public DataSourceAggregator(List<SocialMediaDataSource> dataSources) {
        // 注入所有SocialMediaDataSource实现类
        this.dataSources = dataSources;
    }

    /**
     * 从所有可用数据源搜索帖子
     * @param keyword 搜索关键词
     * @param limitPerSource 每个数据源的最大结果数
     * @return 合并后的帖子列表
     */
    public List<SocialMediaPost> searchAllPlatforms(String keyword, int limitPerSource) {
        List<SocialMediaPost> allPosts = new ArrayList<>();

        // 并行调用所有可用数据源
        dataSources.parallelStream()
                .filter(SocialMediaDataSource::isAvailable)
                .forEach(dataSource -> {
                    List<SocialMediaPost> posts = dataSource.searchPosts(keyword, limitPerSource);
                    synchronized (allPosts) {
                        allPosts.addAll(posts);
                    }
                });

        // 按发布时间排序（降序）
        return allPosts.stream()
                .sorted((p1, p2) -> p2.getPublishTime().compareTo(p1.getPublishTime()))
                .collect(Collectors.toList());
    }

    /**
     * 获取所有可用数据源名称
     */
    public List<String> getAvailablePlatforms() {
        return dataSources.stream()
                .filter(SocialMediaDataSource::isAvailable)
                .map(SocialMediaDataSource::getPlatformName)
                .collect(Collectors.toList());
    }
}