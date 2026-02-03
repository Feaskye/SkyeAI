package com.skyeai.jarvis.plugin.controller;

import com.skyeai.jarvis.plugin.service.PluginService;
import com.skyeai.jarvis.plugin.service.plugin.Plugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plugin")
public class PluginController {

    @Autowired
    private PluginService pluginService;

    @GetMapping("/list")
    public List<Plugin> getPlugins() {
        return pluginService.getPlugins();
    }

    @GetMapping("/get")
    public Plugin getPlugin(@RequestParam String pluginName) {
        return pluginService.getPlugin(pluginName);
    }

    @PostMapping("/install")
    public Map<String, Object> installPlugin(@RequestParam("pluginFile") MultipartFile pluginFile) {
        return pluginService.installPlugin(pluginFile);
    }

    @PostMapping("/uninstall")
    public Map<String, Object> uninstallPlugin(@RequestParam String pluginName) {
        return pluginService.uninstallPlugin(pluginName);
    }

    @PostMapping("/update")
    public Map<String, Object> updatePlugin(
            @RequestParam String pluginName,
            @RequestParam("pluginFile") MultipartFile pluginFile) {
        return pluginService.updatePlugin(pluginName, pluginFile);
    }

    @PostMapping("/enable")
    public Map<String, Object> enablePlugin(@RequestParam String pluginName) {
        return pluginService.enablePlugin(pluginName);
    }

    @PostMapping("/disable")
    public Map<String, Object> disablePlugin(@RequestParam String pluginName) {
        return pluginService.disablePlugin(pluginName);
    }

    @PostMapping("/execute")
    public Map<String, Object> executePlugin(
            @RequestParam String pluginName,
            @RequestBody Map<String, Object> parameters) {
        return pluginService.executePlugin(pluginName, parameters);
    }

    @GetMapping("/configuration")
    public Map<String, Object> getPluginConfiguration(@RequestParam String pluginName) {
        return pluginService.getPluginConfiguration(pluginName);
    }

    @PostMapping("/configuration")
    public Map<String, Object> updatePluginConfiguration(
            @RequestParam String pluginName,
            @RequestBody Map<String, Object> configuration) {
        return pluginService.updatePluginConfiguration(pluginName, configuration);
    }
}