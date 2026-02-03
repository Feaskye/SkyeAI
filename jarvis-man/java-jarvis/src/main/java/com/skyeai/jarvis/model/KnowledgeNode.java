package com.skyeai.jarvis.model;

/**
 * 知识图谱节点
 */
public class KnowledgeNode {
    private String id;
    private String type;
    private String name;
    private String description;
    private java.util.Map<String, Object> properties;

    public KnowledgeNode() {
    }

    public KnowledgeNode(String id, String type, String name) {
        this.id = id;
        this.type = type;
        this.name = name;
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
        return "KnowledgeNode{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", properties=" + properties +
                '}';
    }
}
