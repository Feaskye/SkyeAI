package com.skyeai.jarvis.knowledge.service;

import com.skyeai.jarvis.knowledge.model.KnowledgeNode;
import com.skyeai.jarvis.knowledge.model.KnowledgeRelationship;

import java.util.List;
import java.util.Map;

/**
 * 知识图谱服务，用于管理知识图谱的节点和关系
 */
public interface KnowledgeGraphService {

    /**
     * 创建知识节点
     */
    KnowledgeNode createNode(KnowledgeNode node);

    /**
     * 获取知识节点
     */
    KnowledgeNode getNode(String nodeId);

    /**
     * 更新知识节点
     */
    KnowledgeNode updateNode(KnowledgeNode node);

    /**
     * 删除知识节点
     */
    void deleteNode(String nodeId);

    /**
     * 根据类型获取节点
     */
    List<KnowledgeNode> getNodesByType(String type);

    /**
     * 根据属性获取节点
     */
    List<KnowledgeNode> getNodesByProperty(String propertyName, Object propertyValue);

    /**
     * 搜索节点
     */
    List<KnowledgeNode> searchNodes(String keyword);

    /**
     * 创建知识关系
     */
    KnowledgeRelationship createRelationship(KnowledgeRelationship relationship);

    /**
     * 获取知识关系
     */
    KnowledgeRelationship getRelationship(String relationshipId);

    /**
     * 更新知识关系
     */
    KnowledgeRelationship updateRelationship(KnowledgeRelationship relationship);

    /**
     * 删除知识关系
     */
    void deleteRelationship(String relationshipId);

    /**
     * 获取节点的入边
     */
    List<KnowledgeRelationship> getIncomingRelationships(String nodeId);

    /**
     * 获取节点的出边
     */
    List<KnowledgeRelationship> getOutgoingRelationships(String nodeId);

    /**
     * 获取节点的所有关系
     */
    List<KnowledgeRelationship> getAllRelationships(String nodeId);

    /**
     * 获取两个节点之间的关系
     */
    List<KnowledgeRelationship> getRelationshipsBetween(String sourceId, String targetId);

    /**
     * 执行图谱查询
     */
    List<Map<String, Object>> executeGraphQuery(String query);

    /**
     * 执行路径查询
     */
    List<List<KnowledgeNode>> findPaths(String startNodeId, String endNodeId, int maxDepth);

    /**
     * 执行最短路径查询
     */
    List<KnowledgeNode> findShortestPath(String startNodeId, String endNodeId);

    /**
     * 执行邻居查询
     */
    List<KnowledgeNode> findNeighbors(String nodeId, int depth);

    /**
     * 执行社区检测
     */
    Map<String, List<KnowledgeNode>> detectCommunities();

    /**
     * 执行中心性分析
     */
    Map<String, Double> calculateCentrality();

    /**
     * 从文本中提取知识
     */
    List<KnowledgeNode> extractKnowledgeFromText(String text);

    /**
     * 从聊天历史中提取知识
     */
    List<KnowledgeNode> extractKnowledgeFromChatHistory(String userId);

    /**
     * 融合知识
     */
    void fuseKnowledge(List<KnowledgeNode> nodes, List<KnowledgeRelationship> relationships);

    /**
     * 清理冗余知识
     */
    void cleanRedundantKnowledge();

    /**
     * 导出知识图谱
     */
    Map<String, Object> exportKnowledgeGraph();

    /**
     * 导入知识图谱
     */
    void importKnowledgeGraph(Map<String, Object> graphData);

    /**
     * 获取图谱统计信息
     */
    Map<String, Object> getGraphStatistics();
}
