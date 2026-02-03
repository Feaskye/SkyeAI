package com.skyeai.jarvis.plugin.service.plugin;

public interface PluginListener {
    void onPluginLoaded(Plugin plugin);
    
    void onPluginUnloaded(Plugin plugin);
    
    void onPluginEnabled(Plugin plugin);
    
    void onPluginDisabled(Plugin plugin);
    
    void onPluginExecuted(Plugin plugin, Object result);
    
    void onPluginError(Plugin plugin, Exception e);
}