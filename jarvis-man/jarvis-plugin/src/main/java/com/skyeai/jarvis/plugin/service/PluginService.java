package com.skyeai.jarvis.plugin.service;

import com.skyeai.jarvis.plugin.service.plugin.Plugin;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface PluginService {
    List<Plugin> getPlugins();
    
    Plugin getPlugin(String pluginName);
    
    Map<String, Object> installPlugin(MultipartFile pluginFile);
    
    Map<String, Object> uninstallPlugin(String pluginName);
    
    Map<String, Object> updatePlugin(String pluginName, MultipartFile pluginFile);
    
    Map<String, Object> enablePlugin(String pluginName);
    
    Map<String, Object> disablePlugin(String pluginName);
    
    Map<String, Object> executePlugin(String pluginName, Map<String, Object> parameters);
    
    Map<String, Object> getPluginConfiguration(String pluginName);
    
    Map<String, Object> updatePluginConfiguration(String pluginName, Map<String, Object> configuration);
}