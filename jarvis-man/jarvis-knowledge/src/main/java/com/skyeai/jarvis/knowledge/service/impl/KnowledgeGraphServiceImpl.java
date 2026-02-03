package com.skyeai.jarvis.knowledge.service.impl;

import com.skyeai.jarvis.knowledge.model.KnowledgeNode;
import com.skyeai.jarvis.knowledge.model.KnowledgeRelationship;
import com.skyeai.jarvis.knowledge.service.KnowledgeGraphService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Transactional
public class KnowledgeGraphServiceImpl implements KnowledgeGraphService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public KnowledgeNode createNode(KnowledgeNode node) {
        node.setCreatedAt(new Date());
        node.setUpdatedAt(new Date());
        entityManager.persist(node);
        return node;
    }

    @Override
    public KnowledgeNode getNode(String nodeId) {
        return entityManager.find(KnowledgeNode.class, nodeId);
    }

    @Override
    public KnowledgeNode updateNode(KnowledgeNode node) {
        node.setUpdatedAt(new Date());
        return entityManager.merge(node);
    }

    @Override
    public void deleteNode(String nodeId) {
        KnowledgeNode node = entityManager.find(KnowledgeNode.class, nodeId);
        if (node != null) {
            entityManager.remove(node);
        }
    }

    @Override
    public List<KnowledgeNode> getNodesByType(String type) {
        return entityManager.createQuery("SELECT n FROM KnowledgeNode n WHERE n.type = :type", KnowledgeNode.class)
                .setParameter("type", type)
                .getResultList();
    }

    @Override
    public List<KnowledgeNode> getNodesByProperty(String propertyName, Object propertyValue) {
        return entityManager.createQuery(
                        "SELECT n FROM KnowledgeNode n JOIN n.properties p WHERE KEY(p) = :propertyName AND VALUE(p) = :propertyValue",
                        KnowledgeNode.class)
                .setParameter("propertyName", propertyName)
                .setParameter("propertyValue", propertyValue)
                .getResultList();
    }

    @Override
    public List<KnowledgeNode> searchNodes(String keyword) {
        return entityManager.createQuery(
                        "SELECT n FROM KnowledgeNode n WHERE n.content LIKE :keyword OR n.description LIKE :keyword",
                        KnowledgeNode.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
    }

    @Override
    public KnowledgeRelationship createRelationship(KnowledgeRelationship relationship) {
        relationship.setCreatedAt(new Date());
        relationship.setUpdatedAt(new Date());
        entityManager.persist(relationship);
        return relationship;
    }

    @Override
    public KnowledgeRelationship getRelationship(String relationshipId) {
        return entityManager.find(KnowledgeRelationship.class, relationshipId);
    }

    @Override
    public KnowledgeRelationship updateRelationship(KnowledgeRelationship relationship) {
        relationship.setUpdatedAt(new Date());
        return entityManager.merge(relationship);
    }

    @Override
    public void deleteRelationship(String relationshipId) {
        KnowledgeRelationship relationship = entityManager.find(KnowledgeRelationship.class, relationshipId);
        if (relationship != null) {
            entityManager.remove(relationship);
        }
    }

    @Override
    public List<KnowledgeRelationship> getIncomingRelationships(String nodeId) {
        return entityManager.createQuery(
                        "SELECT r FROM KnowledgeRelationship r WHERE r.targetId = :nodeId",
                        KnowledgeRelationship.class)
                .setParameter("nodeId", nodeId)
                .getResultList();
    }

    @Override
    public List<KnowledgeRelationship> getOutgoingRelationships(String nodeId) {
        return entityManager.createQuery(
                        "SELECT r FROM KnowledgeRelationship r WHERE r.sourceId = :nodeId",
                        KnowledgeRelationship.class)
                .setParameter("nodeId", nodeId)
                .getResultList();
    }

    @Override
    public List<KnowledgeRelationship> getAllRelationships(String nodeId) {
        List<KnowledgeRelationship> relationships = new ArrayList<>();
        relationships.addAll(getIncomingRelationships(nodeId));
        relationships.addAll(getOutgoingRelationships(nodeId));
        return relationships;
    }

    @Override
    public List<KnowledgeRelationship> getRelationshipsBetween(String sourceId, String targetId) {
        return entityManager.createQuery(
                        "SELECT r FROM KnowledgeRelationship r WHERE r.sourceId = :sourceId AND r.targetId = :targetId",
                        KnowledgeRelationship.class)
                .setParameter("sourceId", sourceId)
                .setParameter("targetId", targetId)
                .getResultList();
    }

    @Override
    public List<Map<String, Object>> executeGraphQuery(String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        try {
            List<Object[]> queryResults = entityManager.createNativeQuery(query).getResultList();
            for (Object[] row : queryResults) {
                Map<String, Object> resultMap = new HashMap<>();
                for (int i = 0; i < row.length; i++) {
                    resultMap.put("column_" + i, row[i]);
                }
                results.add(resultMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    @Override
    public List<List<KnowledgeNode>> findPaths(String startNodeId, String endNodeId, int maxDepth) {
        List<List<KnowledgeNode>> paths = new ArrayList<>();
        List<KnowledgeNode> currentPath = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        dfsFindPaths(startNodeId, endNodeId, maxDepth, 0, currentPath, visited, paths);
        return paths;
    }

    private void dfsFindPaths(String currentNodeId, String endNodeId, int maxDepth, int currentDepth, 
                             List<KnowledgeNode> currentPath, Set<String> visited, List<List<KnowledgeNode>> paths) {
        if (currentDepth > maxDepth) {
            return;
        }

        KnowledgeNode currentNode = getNode(currentNodeId);
        if (currentNode == null || visited.contains(currentNodeId)) {
            return;
        }

        currentPath.add(currentNode);
        visited.add(currentNodeId);

        if (currentNodeId.equals(endNodeId)) {
            paths.add(new ArrayList<>(currentPath));
        } else {
            List<KnowledgeRelationship> outgoingRelationships = getOutgoingRelationships(currentNodeId);
            for (KnowledgeRelationship relationship : outgoingRelationships) {
                dfsFindPaths(relationship.getTargetId(), endNodeId, maxDepth, currentDepth + 1, 
                           currentPath, visited, paths);
            }
        }

        currentPath.remove(currentPath.size() - 1);
        visited.remove(currentNodeId);
    }

    @Override
    public List<KnowledgeNode> findShortestPath(String startNodeId, String endNodeId) {
        List<List<KnowledgeNode>> allPaths = findPaths(startNodeId, endNodeId, 10);
        if (allPaths.isEmpty()) {
            return Collections.emptyList();
        }

        return allPaths.stream()
                .min(Comparator.comparingInt(List::size))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<KnowledgeNode> findNeighbors(String nodeId, int depth) {
        Set<KnowledgeNode> neighbors = new HashSet<>();
        Set<String> visited = new HashSet<>();
        bfsFindNeighbors(nodeId, depth, 0, neighbors, visited);
        return new ArrayList<>(neighbors);
    }

    private void bfsFindNeighbors(String currentNodeId, int maxDepth, int currentDepth, 
                                 Set<KnowledgeNode> neighbors, Set<String> visited) {
        if (currentDepth >= maxDepth || visited.contains(currentNodeId)) {
            return;
        }

        KnowledgeNode currentNode = getNode(currentNodeId);
        if (currentNode == null) {
            return;
        }

        visited.add(currentNodeId);

        List<KnowledgeRelationship> allRelationships = getAllRelationships(currentNodeId);
        for (KnowledgeRelationship relationship : allRelationships) {
            String neighborId = relationship.getSourceId().equals(currentNodeId) ? 
                              relationship.getTargetId() : relationship.getSourceId();
            KnowledgeNode neighbor = getNode(neighborId);
            if (neighbor != null) {
                neighbors.add(neighbor);
                bfsFindNeighbors(neighborId, maxDepth, currentDepth + 1, neighbors, visited);
            }
        }
    }

    @Override
    public Map<String, List<KnowledgeNode>> detectCommunities() {
        Map<String, List<KnowledgeNode>> communities = new HashMap<>();
        Set<String> visited = new HashSet<>();
        List<KnowledgeNode> allNodes = entityManager.createQuery("SELECT n FROM KnowledgeNode n", KnowledgeNode.class).getResultList();

        int communityId = 1;
        for (KnowledgeNode node : allNodes) {
            if (!visited.contains(node.getId())) {
                List<KnowledgeNode> community = new ArrayList<>();
                dfsDetectCommunity(node.getId(), community, visited);
                if (!community.isEmpty()) {
                    communities.put("community_" + communityId++, community);
                }
            }
        }

        return communities;
    }

    private void dfsDetectCommunity(String nodeId, List<KnowledgeNode> community, Set<String> visited) {
        if (visited.contains(nodeId)) {
            return;
        }

        KnowledgeNode node = getNode(nodeId);
        if (node == null) {
            return;
        }

        visited.add(nodeId);
        community.add(node);

        List<KnowledgeRelationship> relationships = getAllRelationships(nodeId);
        for (KnowledgeRelationship relationship : relationships) {
            String neighborId = relationship.getSourceId().equals(nodeId) ? 
                              relationship.getTargetId() : relationship.getSourceId();
            dfsDetectCommunity(neighborId, community, visited);
        }
    }

    @Override
    public Map<String, Double> calculateCentrality() {
        Map<String, Double> centrality = new HashMap<>();
        List<KnowledgeNode> allNodes = entityManager.createQuery("SELECT n FROM KnowledgeNode n", KnowledgeNode.class).getResultList();

        for (KnowledgeNode node : allNodes) {
            int degree = getAllRelationships(node.getId()).size();
            centrality.put(node.getId(), (double) degree);
        }

        return centrality;
    }

    @Override
    public List<KnowledgeNode> extractKnowledgeFromText(String text) {
        List<KnowledgeNode> nodes = new ArrayList<>();
        KnowledgeNode node = new KnowledgeNode();
        node.setType("text_extracted");
        node.setContent(text);
        node.setDescription("Extracted from text");
        node.setConfidence(0.8);
        node.setSource("text");
        nodes.add(createNode(node));
        return nodes;
    }

    @Override
    public List<KnowledgeNode> extractKnowledgeFromChatHistory(String userId) {
        List<KnowledgeNode> nodes = new ArrayList<>();
        KnowledgeNode node = new KnowledgeNode();
        node.setType("chat_history_extracted");
        node.setContent("Knowledge extracted from chat history for user: " + userId);
        node.setDescription("Extracted from chat history");
        node.setConfidence(0.7);
        node.setSource("chat_history");
        nodes.add(createNode(node));
        return nodes;
    }

    @Override
    public void fuseKnowledge(List<KnowledgeNode> nodes, List<KnowledgeRelationship> relationships) {
        for (KnowledgeNode node : nodes) {
            createNode(node);
        }
        for (KnowledgeRelationship relationship : relationships) {
            createRelationship(relationship);
        }
    }

    @Override
    public void cleanRedundantKnowledge() {
        List<KnowledgeNode> allNodes = entityManager.createQuery("SELECT n FROM KnowledgeNode n", KnowledgeNode.class).getResultList();
        Set<String> seenContents = new HashSet<>();
        for (KnowledgeNode node : allNodes) {
            if (seenContents.contains(node.getContent())) {
                deleteNode(node.getId());
            } else {
                seenContents.add(node.getContent());
            }
        }
    }

    @Override
    public Map<String, Object> exportKnowledgeGraph() {
        Map<String, Object> graphData = new HashMap<>();
        List<KnowledgeNode> nodes = entityManager.createQuery("SELECT n FROM KnowledgeNode n", KnowledgeNode.class).getResultList();
        List<KnowledgeRelationship> relationships = entityManager.createQuery("SELECT r FROM KnowledgeRelationship r", KnowledgeRelationship.class).getResultList();
        graphData.put("nodes", nodes);
        graphData.put("relationships", relationships);
        return graphData;
    }

    @Override
    public void importKnowledgeGraph(Map<String, Object> graphData) {
        List<KnowledgeNode> nodes = (List<KnowledgeNode>) graphData.get("nodes");
        List<KnowledgeRelationship> relationships = (List<KnowledgeRelationship>) graphData.get("relationships");
        if (nodes != null) {
            for (KnowledgeNode node : nodes) {
                createNode(node);
            }
        }
        if (relationships != null) {
            for (KnowledgeRelationship relationship : relationships) {
                createRelationship(relationship);
            }
        }
    }

    @Override
    public Map<String, Object> getGraphStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        long nodeCount = entityManager.createQuery("SELECT COUNT(n) FROM KnowledgeNode n", Long.class).getSingleResult();
        long relationshipCount = entityManager.createQuery("SELECT COUNT(r) FROM KnowledgeRelationship r", Long.class).getSingleResult();
        statistics.put("nodeCount", nodeCount);
        statistics.put("relationshipCount", relationshipCount);
        return statistics;
    }
}
