package com.skyeai.jarvis.plugin.service.impl;

import com.skyeai.jarvis.plugin.service.PluginService;
import com.skyeai.jarvis.plugin.service.plugin.Plugin;
import com.skyeai.jarvis.plugin.service.plugin.PluginManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PluginServiceImpl implements PluginService {
    private PluginManager pluginManager;
    
    @PostConstruct
    public void initialize() {
        pluginManager = new PluginManager();
        pluginManager.loadPlugins();
    }
    
    @PreDestroy
    public void shutdown() {
        if (pluginManager != null) {
            pluginManager.shutdown();
        }
    }
    
    @Override
    public List<Plugin> getPlugins() {
        return pluginManager.getPlugins();
    }
    
    @Override
    public Plugin getPlugin(String pluginName) {
        return pluginManager.getPlugin(pluginName);
    }
    
    @Override
    public Map<String, Object> installPlugin(MultipartFile pluginFile) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            File tempFile = File.createTempFile("plugin", ".jar");
            pluginFile.transferTo(tempFile);
            
            pluginManager.installPlugin(tempFile);
            
            tempFile.delete();
            
            result.put("success", true);
            result.put("message", "插件安装成功");
        } catch (IOException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "插件安装失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> uninstallPlugin(String pluginName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            pluginManager.uninstallPlugin(pluginName);
            result.put("success", true);
            result.put("message", "插件卸载成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "插件卸载失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> updatePlugin(String pluginName, MultipartFile pluginFile) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 先卸载旧插件
            pluginManager.uninstallPlugin(pluginName);
            
            // 再安装新插件
            File tempFile = File.createTempFile("plugin", ".jar");
            pluginFile.transferTo(tempFile);
            
            pluginManager.installPlugin(tempFile);
            
            tempFile.delete();
            
            result.put("success", true);
            result.put("message", "插件更新成功");
        } catch (IOException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "插件更新失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> enablePlugin(String pluginName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            pluginManager.enablePlugin(pluginName);
            result.put("success", true);
            result.put("message", "插件启用成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "插件启用失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> disablePlugin(String pluginName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            pluginManager.disablePlugin(pluginName);
            result.put("success", true);
            result.put("message", "插件禁用成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "插件禁用失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> executePlugin(String pluginName, Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Plugin plugin = pluginManager.getPlugin(pluginName);
            if (plugin == null) {
                result.put("success", false);
                result.put("message", "插件不存在");
                return result;
            }
            
            if (!plugin.isEnabled()) {
                result.put("success", false);
                result.put("message", "插件未启用");
                return result;
            }
            
            Map<String, Object> executeResult = plugin.execute(parameters);
            pluginManager.notifyPluginExecuted(plugin, executeResult);
            
            result.put("success", true);
            result.put("data", executeResult);
        } catch (Exception e) {
            e.printStackTrace();
            pluginManager.notifyPluginError(pluginManager.getPlugin(pluginName), e);
            result.put("success", false);
            result.put("message", "插件执行失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> getPluginConfiguration(String pluginName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Plugin plugin = pluginManager.getPlugin(pluginName);
            if (plugin == null) {
                result.put("success", false);
                result.put("message", "插件不存在");
                return result;
            }
            
            Map<String, Object> configuration = plugin.getConfiguration();
            result.put("success", true);
            result.put("data", configuration);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取插件配置失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> updatePluginConfiguration(String pluginName, Map<String, Object> configuration) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Plugin plugin = pluginManager.getPlugin(pluginName);
            if (plugin == null) {
                result.put("success", false);
                result.put("message", "插件不存在");
                return result;
            }
            
            plugin.updateConfiguration(configuration);
            result.put("success", true);
            result.put("message", "插件配置更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "更新插件配置失败: " + e.getMessage());
        }
        
        return result;
    }
}