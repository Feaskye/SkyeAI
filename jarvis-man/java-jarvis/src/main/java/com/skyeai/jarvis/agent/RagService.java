package com.skyeai.jarvis.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RAG服务接口
 * 提供知识库检索功能
 */
@Slf4j
@Component
public class RagService {
    
    @Value("${chat.rag.enabled:true}")
    private boolean enabled;
    
    @Value("${chat.rag.knowledge-loaded:false}")
    private boolean knowledgeLoaded;
    
    /**
     * 查询知识库
     * @param query 查询语句
     * @return 检索到的上下文内容
     */
    public String query(String query) {
        if (!enabled) {
            log.debug("RAG服务未启用");
            return null;
        }
        
        log.debug("RAG查询: {}", query);
        
        // 模拟RAG检索（实际应用中应调用真实的RAG服务）
        return simulateRagQuery(query);
    }
    
    /**
     * 检查知识库是否已加载
     * @return 是否已加载
     */
    public boolean isKnowledgeLoaded() {
        return knowledgeLoaded;
    }
    
    /**
     * 设置知识库加载状态
     */
    public void setKnowledgeLoaded(boolean loaded) {
        this.knowledgeLoaded = loaded;
    }
    
    /**
     * 模拟RAG检索
     */
    private String simulateRagQuery(String query) {
        // 根据查询关键词返回模拟的知识库内容
        String lowerQuery = query.toLowerCase();
        
        if (lowerQuery.contains("天气") || lowerQuery.contains("气候")) {
            return """
                知识库资料：天气与气候
                天气是指某一地区在某一瞬间或某一短时间内大气状态（如气温、湿度、气压等）和大气现象（如风、云、雾、降水等）的综合。
                气候是指某一地区大气的多年平均状况，主要的气候要素包括光照、气温和降水等。
                
                常见天气现象：
                - 晴天：天空云量少于30%
                - 多云：天空云量在30%-70%之间
                - 阴天：天空云量大于70%
                - 小雨：24小时降水量小于10毫米
                - 中雨：24小时降水量在10-25毫米之间
                - 大雨：24小时降水量在25-50毫米之间
                """;
        } else if (lowerQuery.contains("股票") || lowerQuery.contains("股市")) {
            return """
                知识库资料：股票基础知识
                股票是股份公司发行的所有权凭证，代表持有者对公司的部分所有权。
                
                股票类型：
                - 普通股：享有投票权和分红权
                - 优先股：优先获得股息，但通常没有投票权
                
                主要指数：
                - 上证指数（000001.SH）：上海证券交易所综合指数
                - 深证成指（399001.SZ）：深圳证券交易所成份指数
                - 创业板指（399006.SZ）：深圳证券交易所创业板指数
                
                股票交易时间：
                - 周一至周五：9:30-11:30，13:00-15:00
                - 法定节假日休市
                """;
        } else if (lowerQuery.contains("人工智能") || lowerQuery.contains("AI")) {
            return """
                知识库资料：人工智能概述
                人工智能（Artificial Intelligence，简称AI）是计算机科学的一个分支，旨在创建能够模拟人类智能的机器。
                
                AI主要领域：
                - 机器学习：让计算机从数据中学习模式
                - 深度学习：使用多层神经网络进行学习
                - 自然语言处理：让计算机理解和生成人类语言
                - 计算机视觉：让计算机"看"懂图像和视频
                
                著名AI模型：
                - GPT：OpenAI开发的大型语言模型
                - Claude：Anthropic开发的AI助手
                - Llama：Meta开发的开源大语言模型
                """;
        } else {
            return """
                知识库资料：通用信息
                贾维斯（J.A.R.V.I.S.）是一个先进的人工智能系统，由Tony Stark创建。
                J.A.R.V.I.S.代表"Just A Rather Very Intelligent System"。
                
                功能特点：
                - 自然语言交互
                - 实时数据分析
                - 任务自动化
                - 多模态支持（语音、视觉等）
                """;
        }
    }
}