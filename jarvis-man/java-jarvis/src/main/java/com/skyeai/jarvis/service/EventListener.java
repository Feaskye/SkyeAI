package com.skyeai.jarvis.service;

import java.util.Map;

public interface EventListener {
    void onEvent(Event event);
    
    class Event {
        private String type;
        private Map<String, Object> data;
        private long timestamp;
        
        public Event(String type, Map<String, Object> data) {
            this.type = type;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getType() {
            return type;
        }
        
        public Map<String, Object> getData() {
            return data;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
}
