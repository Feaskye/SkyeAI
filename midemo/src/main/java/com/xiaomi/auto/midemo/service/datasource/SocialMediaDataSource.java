package com.xiaomi.auto.midemo.service.datasource;

import com.xiaomi.auto.midemo.dto.SocialMediaPost;
import java.util.List;

/**
 * 社交媒体数据源接口，定义多平台数据获取标准
 */
public interface SocialMediaDataSource {
    /**
     * 搜索相关帖子
     * @param keyword 搜索关键词
     * @param limit 最大结果数
     * @return 帖子列表
     */
    List<SocialMediaPost> searchPosts(String keyword, int limit);

    /**
     * 获取数据源平台名称
     * @return 平台名称（如"微博"、"知乎"、"抖音"）
     */
    String getPlatformName();

    /**
     * 检查数据源是否可用
     * @return true表示可用
     */
    boolean isAvailable();
}