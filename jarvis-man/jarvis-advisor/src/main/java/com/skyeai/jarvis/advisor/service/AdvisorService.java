package com.skyeai.jarvis.advisor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdvisorService {

    @Value("${advisor.max-sources}")
    private int maxSources;

    @Value("${advisor.confidence-threshold}")
    private double confidenceThreshold;

    @Value("${advisor.enable-tracing}")
    private boolean enableTracing;

    @Value("${advisor.enable-metadata}")
    private boolean enableMetadata;

    @Value("${metadata.max-tags}")
    private int maxTags;

    @Value("${metadata.max-tag-length}")
    private int maxTagLength;

    @Value("${traceability.enable-references}")
    private boolean enableReferences;

    @Value("${traceability.max-references}")
    private int maxReferences;

    @Value("${fusion.enable-weighted}")
    private boolean enableWeighted;

    @Value("${fusion.default-weight}")
    private double defaultWeight;

    @PostConstruct
    public void init() {
        System.out.println("AdvisorService initialized successfully");
        System.out.println("Enable tracing: " + enableTracing);
        System.out.println("Enable metadata: " + enableMetadata);
    }

    /**
     * 生成溯源回答
     * @param query 用户查询
     * @param sources 信息来源列表
     * @return 溯源回答
     */
    public TraceableAnswer generateTraceableAnswer(String query, List<InformationSource> sources) {
        try {
            // 过滤低可信度的来源
            List<InformationSource> filteredSources = sources.stream()
                    .filter(source -> source.getConfidence() >= confidenceThreshold)
                    .limit(maxSources)
                    .toList();

            // 融合信息
            String fusedContent = fuseInformation(filteredSources);

            // 生成引用
            List<Reference> references = generateReferences(filteredSources);

            // 评估整体可信度
            double overallConfidence = evaluateConfidence(filteredSources);

            // 创建溯源回答
            TraceableAnswer answer = new TraceableAnswer();
            answer.setQuery(query);
            answer.setContent(fusedContent);
            answer.setReferences(references);
            answer.setConfidence(overallConfidence);
            answer.setTimestamp(System.currentTimeMillis());

            System.out.println("Generated traceable answer with " + references.size() + " references");
            return answer;
        } catch (Exception e) {
            System.err.println("Failed to generate traceable answer: " + e.getMessage());
            throw new RuntimeException("Failed to generate traceable answer", e);
        }
    }

    /**
     * 融合多源信息
     * @param sources 信息来源列表
     * @return 融合后的内容
     */
    private String fuseInformation(List<InformationSource> sources) {
        if (sources.isEmpty()) {
            return "No information available";
        }

        StringBuilder fusedContent = new StringBuilder();
        fusedContent.append("Based on the available information:\n\n");

        for (int i = 0; i < sources.size(); i++) {
            InformationSource source = sources.get(i);
            fusedContent.append("Source " + (i + 1) + " (Confidence: " + source.getConfidence() + "):\n");
            fusedContent.append(source.getContent() + "\n\n");
        }

        fusedContent.append("Conclusion:\n");
        fusedContent.append(sources.get(0).getContent());

        return fusedContent.toString();
    }

    /**
     * 生成引用
     * @param sources 信息来源列表
     * @return 引用列表
     */
    private List<Reference> generateReferences(List<InformationSource> sources) {
        List<Reference> references = new ArrayList<>();

        if (enableReferences) {
            for (int i = 0; i < Math.min(sources.size(), maxReferences); i++) {
                InformationSource source = sources.get(i);
                Reference reference = new Reference();
                reference.setId("ref_" + System.currentTimeMillis() + "_" + i);
                reference.setSourceId(source.getId());
                reference.setSourceType(source.getType());
                reference.setContent(source.getContent());
                reference.setConfidence(source.getConfidence());
                references.add(reference);
            }
        }

        return references;
    }

    /**
     * 评估可信度
     * @param sources 信息来源列表
     * @return 整体可信度
     */
    private double evaluateConfidence(List<InformationSource> sources) {
        if (sources.isEmpty()) {
            return 0.0;
        }

        if (enableWeighted) {
            double totalWeight = 0.0;
            double weightedSum = 0.0;

            for (InformationSource source : sources) {
                double weight = source.getWeight() != null ? source.getWeight() : defaultWeight;
                weightedSum += source.getConfidence() * weight;
                totalWeight += weight;
            }

            return totalWeight > 0 ? weightedSum / totalWeight : 0.0;
        } else {
            return sources.stream()
                    .mapToDouble(InformationSource::getConfidence)
                    .average()
                    .orElse(0.0);
        }
    }

    /**
     * 创建元数据
     * @param metadata 元数据
     * @return 元数据ID
     */
    public String createMetadata(Metadata metadata) {
        try {
            // 实际应用中应该持久化到数据库
            String metadataId = "meta_" + System.currentTimeMillis();
            metadata.setId(metadataId);
            metadata.setCreatedAt(System.currentTimeMillis());
            metadata.setUpdatedAt(System.currentTimeMillis());

            System.out.println("Created metadata: " + metadataId);
            return metadataId;
        } catch (Exception e) {
            System.err.println("Failed to create metadata: " + e.getMessage());
            throw new RuntimeException("Failed to create metadata", e);
        }
    }

    /**
     * 更新元数据
     * @param metadataId 元数据ID
     * @param metadata 元数据
     * @return 是否成功
     */
    public boolean updateMetadata(String metadataId, Metadata metadata) {
        try {
            // 实际应用中应该从数据库中更新
            metadata.setId(metadataId);
            metadata.setUpdatedAt(System.currentTimeMillis());

            System.out.println("Updated metadata: " + metadataId);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to update metadata: " + e.getMessage());
            throw new RuntimeException("Failed to update metadata", e);
        }
    }

    /**
     * 获取元数据
     * @param metadataId 元数据ID
     * @return 元数据
     */
    public Metadata getMetadata(String metadataId) {
        try {
            // 实际应用中应该从数据库中查询
            Metadata metadata = new Metadata();
            metadata.setId(metadataId);
            metadata.setName("Sample Metadata");
            metadata.setDescription("Sample metadata description");
            metadata.setTags(List.of("sample", "metadata"));
            metadata.setProperties(Map.of("key", "value"));
            metadata.setCreatedAt(System.currentTimeMillis() - 3600000);
            metadata.setUpdatedAt(System.currentTimeMillis());

            return metadata;
        } catch (Exception e) {
            System.err.println("Failed to get metadata: " + e.getMessage());
            throw new RuntimeException("Failed to get metadata", e);
        }
    }

    /**
     * 信息来源类
     */
    public static class InformationSource {
        private String id;
        private String type;
        private String content;
        private double confidence;
        private Double weight;
        private Map<String, Object> metadata;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public Double getWeight() {
            return weight;
        }

        public void setWeight(Double weight) {
            this.weight = weight;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }

    /**
     * 溯源回答类
     */
    public static class TraceableAnswer {
        private String query;
        private String content;
        private List<Reference> references;
        private double confidence;
        private long timestamp;

        // Getters and Setters
        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public List<Reference> getReferences() {
            return references;
        }

        public void setReferences(List<Reference> references) {
            this.references = references;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }

    /**
     * 引用类
     */
    public static class Reference {
        private String id;
        private String sourceId;
        private String sourceType;
        private String content;
        private double confidence;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSourceId() {
            return sourceId;
        }

        public void setSourceId(String sourceId) {
            this.sourceId = sourceId;
        }

        public String getSourceType() {
            return sourceType;
        }

        public void setSourceType(String sourceType) {
            this.sourceType = sourceType;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }
    }

    /**
     * 元数据类
     */
    public static class Metadata {
        private String id;
        private String name;
        private String description;
        private List<String> tags;
        private Map<String, Object> properties;
        private long createdAt;
        private long updatedAt;

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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(long createdAt) {
            this.createdAt = createdAt;
        }

        public long getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(long updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}
