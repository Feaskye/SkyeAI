package com.skyeai.jarvis.model;

/**
 * 知识图谱关系
 */
public class KnowledgeRelationship {
    private String id;
    private String type;
    private String sourceId;
    private String targetId;
    private String name;
    private String description;
    private java.util.Map<String, Object> properties;

    public KnowledgeRelationship() {
    }

    public KnowledgeRelationship(String id, String type, String sourceId, String targetId) {
        this.id = id;
        this.type = type;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.properties = new java.util.HashMap<>();
    }

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

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
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

    public java.util.Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(java.util.Map<String, Object> properties) {
        this.properties = properties;
    }

    public void addProperty(String key, Object value) {
        if (properties == null) {
            properties = new java.util.HashMap<>();
        }
        properties.put(key, value);
    }

    @Override
    public String toString() {
        return "KnowledgeRelationship{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", sourceId='" + sourceId + '\'' +
                ", targetId='" + targetId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", properties=" + properties +
                '}';
    }
}
