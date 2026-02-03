package com.skyeai.jarvis.plugin.service.plugin;

import java.util.Map;

public interface Plugin {
    String getName();
    
    String getVersion();
    
    String getDescription();
    
    boolean isEnabled();
    
    void setEnabled(boolean enabled);
    
    Map<String, Object> execute(Map<String, Object> parameters);
    
    Map<String, Object> getConfiguration();
    
    void updateConfiguration(Map<String, Object> configuration);
    
    void initialize();
    
    void shutdown();
}