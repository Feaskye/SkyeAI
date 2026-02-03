package com.skyeai.jarvis.service;

import java.util.Map;

public interface ReverseControlService {
    void initialize();
    void shutdown();
    
    Map<String, Object> executeControl(String controlType, Map<String, Object> parameters);
    Map<String, Object> getControlStatus();
    Map<String, Object> getAvailableControls();
    
    void registerControlHandler(String controlType, ControlHandler handler);
    void unregisterControlHandler(String controlType);
    
    interface ControlHandler {
        Map<String, Object> handleControl(Map<String, Object> parameters);
    }
}
