package com.skyeai.jarvis.config;

import com.skyeai.jarvis.service.ServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.logging.Logger;

@Component
public class ToolConfig implements CommandLineRunner {
    
    private static final Logger logger = Logger.getLogger(ToolConfig.class.getName());
    
    @Autowired
    private ServiceClient serviceClient;
    
    @Override
    public void run(String... args) throws Exception {
        // 加载工具配置
        try {
            // 尝试从默认路径加载工具配置
            String yamlPath = "tools.yaml";
            Map<String, Object> request = Map.of("yamlPath", yamlPath);
            serviceClient.callSkillsService("/tools/load-from-yaml", request);
        } catch (Exception e) {
            logger.info("Failed to load tools from yaml: " + e.getMessage() + ", loading default tools instead");
            // 如果加载失败，会自动加载默认工具
        }
    }
}
