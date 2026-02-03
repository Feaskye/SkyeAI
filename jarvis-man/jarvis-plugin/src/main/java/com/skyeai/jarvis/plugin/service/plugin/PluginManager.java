package com.skyeai.jarvis.plugin.service.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginManager {
    private final List<Plugin> plugins = new ArrayList<>();
    private final Map<String, Plugin> pluginMap = new ConcurrentHashMap<>();
    private final List<PluginListener> listeners = new ArrayList<>();
    private final File pluginsDirectory;

    public PluginManager() {
        this.pluginsDirectory = new File("plugins");
        if (!pluginsDirectory.exists()) {
            pluginsDirectory.mkdirs();
        }
    }

    public void loadPlugins() {
        File[] pluginFiles = pluginsDirectory.listFiles((dir, name) -> name.endsWith(".jar"));
        if (pluginFiles != null) {
            for (File pluginFile : pluginFiles) {
                loadPlugin(pluginFile);
            }
        }
    }

    private void loadPlugin(File pluginFile) {
        try {
            URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{pluginFile.toURI().toURL()},
                    getClass().getClassLoader()
            );

            // 这里需要根据实际的插件实现来加载插件
            // 假设插件有一个Main类，实现了Plugin接口
            // 实际实现中，可能需要读取插件的配置文件来确定主类
            Plugin plugin = createDummyPlugin(pluginFile.getName());
            plugin.initialize();
            
            plugins.add(plugin);
            pluginMap.put(plugin.getName(), plugin);
            
            notifyPluginLoaded(plugin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void installPlugin(File pluginFile) {
        try {
            File destFile = new File(pluginsDirectory, pluginFile.getName());
            java.nio.file.Files.copy(pluginFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            loadPlugin(destFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uninstallPlugin(String pluginName) {
        Plugin plugin = pluginMap.get(pluginName);
        if (plugin != null) {
            plugin.shutdown();
            plugins.remove(plugin);
            pluginMap.remove(pluginName);
            
            notifyPluginUnloaded(plugin);
            
            // 删除插件文件
            File pluginFile = new File(pluginsDirectory, pluginName + ".jar");
            if (pluginFile.exists()) {
                pluginFile.delete();
            }
        }
    }

    public void enablePlugin(String pluginName) {
        Plugin plugin = pluginMap.get(pluginName);
        if (plugin != null) {
            plugin.setEnabled(true);
            notifyPluginEnabled(plugin);
        }
    }

    public void disablePlugin(String pluginName) {
        Plugin plugin = pluginMap.get(pluginName);
        if (plugin != null) {
            plugin.setEnabled(false);
            notifyPluginDisabled(plugin);
        }
    }

    public List<Plugin> getPlugins() {
        return new ArrayList<>(plugins);
    }

    public Plugin getPlugin(String pluginName) {
        return pluginMap.get(pluginName);
    }

    public void addListener(PluginListener listener) {
        listeners.add(listener);
    }

    public void removeListener(PluginListener listener) {
        listeners.remove(listener);
    }

    public void shutdown() {
        for (Plugin plugin : plugins) {
            try {
                plugin.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        plugins.clear();
        pluginMap.clear();
    }

    private void notifyPluginLoaded(Plugin plugin) {
        for (PluginListener listener : listeners) {
            try {
                listener.onPluginLoaded(plugin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyPluginUnloaded(Plugin plugin) {
        for (PluginListener listener : listeners) {
            try {
                listener.onPluginUnloaded(plugin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyPluginEnabled(Plugin plugin) {
        for (PluginListener listener : listeners) {
            try {
                listener.onPluginEnabled(plugin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyPluginDisabled(Plugin plugin) {
        for (PluginListener listener : listeners) {
            try {
                listener.onPluginDisabled(plugin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyPluginExecuted(Plugin plugin, Object result) {
        for (PluginListener listener : listeners) {
            try {
                listener.onPluginExecuted(plugin, result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyPluginError(Plugin plugin, Exception e) {
        for (PluginListener listener : listeners) {
            try {
                listener.onPluginError(plugin, e);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // 创建一个虚拟的插件，用于测试
    private Plugin createDummyPlugin(String name) {
        return new Plugin() {
            private boolean enabled = true;
            private final Map<String, Object> configuration = new ConcurrentHashMap<>();

            @Override
            public String getName() {
                return name.replace(".jar", "");
            }

            @Override
            public String getVersion() {
                return "1.0.0";
            }

            @Override
            public String getDescription() {
                return "A dummy plugin for testing";
            }

            @Override
            public boolean isEnabled() {
                return enabled;
            }

            @Override
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            @Override
            public Map<String, Object> execute(Map<String, Object> parameters) {
                Map<String, Object> result = new ConcurrentHashMap<>();
                result.put("success", true);
                result.put("message", "Plugin executed successfully");
                result.put("parameters", parameters);
                return result;
            }

            @Override
            public Map<String, Object> getConfiguration() {
                return configuration;
            }

            @Override
            public void updateConfiguration(Map<String, Object> configuration) {
                this.configuration.putAll(configuration);
            }

            @Override
            public void initialize() {
                System.out.println("Initializing plugin: " + getName());
            }

            @Override
            public void shutdown() {
                System.out.println("Shutting down plugin: " + getName());
            }
        };
    }
}