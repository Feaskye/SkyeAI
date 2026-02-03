package com.skyeai.jarvis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Service
public class LocalFirstService {

    private static final Logger logger = Logger.getLogger(LocalFirstService.class.getName());

    @Value("${local.models.path}")
    private String modelsPath;

    @Value("${local.data.path}")
    private String dataPath;

    @Value("${local.cache.path}")
    private String cachePath;

    @Value("${ollama.base.url}")
    private String ollamaBaseUrl;

    // 本地模型注册表
    private final Map<String, ModelInfo> localModels = new HashMap<>();

    // 离线模式状态
    private boolean offlineMode = false;

    /**
     * 模型信息实体类
     */
    public static class ModelInfo {
        private String name;
        private String path;
        private long size;
        private String version;
        private boolean active;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    /**
     * 初始化本地优先服务
     */
    public void initialize() {
        logger.info("初始化本地优先服务");
        
        // 创建必要的目录
        createDirectories();
        
        // 加载本地模型
        loadLocalModels();
        
        // 检查离线模式
        checkOfflineMode();
    }

    /**
     * 创建必要的目录
     */
    private void createDirectories() {
        try {
            Path modelsDir = Paths.get(modelsPath);
            Path dataDir = Paths.get(dataPath);
            Path cacheDir = Paths.get(cachePath);

            if (!Files.exists(modelsDir)) {
                Files.createDirectories(modelsDir);
                logger.info("创建模型目录: " + modelsPath);
            }

            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
                logger.info("创建数据目录: " + dataPath);
            }

            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
                logger.info("创建缓存目录: " + cachePath);
            }
        } catch (IOException e) {
            logger.severe("创建目录失败: " + e.getMessage());
        }
    }

    /**
     * 加载本地模型
     */
    private void loadLocalModels() {
        try {
            Path modelsDir = Paths.get(modelsPath);
            if (Files.exists(modelsDir)) {
                Files.list(modelsDir)
                        .filter(Files::isDirectory)
                        .forEach(dir -> {
                            try {
                                ModelInfo modelInfo = new ModelInfo();
                                modelInfo.setName(dir.getFileName().toString());
                                modelInfo.setPath(dir.toString());
                                modelInfo.setSize(calculateDirectorySize(dir));
                                modelInfo.setVersion("1.0.0"); // 这里可以从模型配置文件中读取
                                modelInfo.setActive(true);

                                localModels.put(modelInfo.getName(), modelInfo);
                                logger.info("加载本地模型: " + modelInfo.getName() + " (" + formatSize(modelInfo.getSize()) + ")");
                            } catch (Exception e) {
                                logger.warning("加载模型失败: " + dir.getFileName() + ": " + e.getMessage());
                            }
                        });
            }
        } catch (IOException e) {
            logger.severe("加载本地模型失败: " + e.getMessage());
        }
    }

    /**
     * 计算目录大小
     */
    private long calculateDirectorySize(Path directory) throws IOException {
        return Files.walk(directory)
                .filter(Files::isRegularFile)
                .mapToLong(p -> {
                    try {
                        return Files.size(p);
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .sum();
    }

    /**
     * 格式化文件大小
     */
    private String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return (size / 1024) + " KB";
        } else if (size < 1024 * 1024 * 1024) {
            return (size / (1024 * 1024)) + " MB";
        } else {
            return (size / (1024 * 1024 * 1024)) + " GB";
        }
    }

    /**
     * 检查离线模式
     */
    private void checkOfflineMode() {
        try {
            // 尝试连接Ollama服务
            Process process = Runtime.getRuntime().exec("ping -n 1 " + ollamaBaseUrl.replace("http://", "").split(":")[0]);
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                logger.info("无法连接到Ollama服务，切换到离线模式");
                offlineMode = true;
            }
        } catch (Exception e) {
            logger.warning("检查网络连接失败: " + e.getMessage());
            offlineMode = true;
        }
    }

    /**
     * 获取所有本地模型
     */
    public Map<String, ModelInfo> getAllLocalModels() {
        return localModels;
    }

    /**
     * 下载模型到本地
     */
    public CompletableFuture<ModelInfo> downloadModel(String modelName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("开始下载模型: " + modelName);
                
                // 这里应该实现与Ollama的交互，下载模型
                // 暂时返回模拟结果
                Path modelPath = Paths.get(modelsPath, modelName);
                if (!Files.exists(modelPath)) {
                    Files.createDirectories(modelPath);
                }

                // 创建一个模拟的模型文件
                Path modelFile = Paths.get(modelPath.toString(), "model.bin");
                Files.createFile(modelFile);
                
                ModelInfo modelInfo = new ModelInfo();
                modelInfo.setName(modelName);
                modelInfo.setPath(modelPath.toString());
                modelInfo.setSize(Files.size(modelFile));
                modelInfo.setVersion("1.0.0");
                modelInfo.setActive(true);

                localModels.put(modelName, modelInfo);
                logger.info("模型下载完成: " + modelName);
                
                return modelInfo;
            } catch (Exception e) {
                logger.severe("模型下载失败: " + modelName + ": " + e.getMessage());
                throw new RuntimeException("模型下载失败: " + e.getMessage());
            }
        });
    }

    /**
     * 切换模型
     */
    public boolean switchModel(String modelName) {
        if (localModels.containsKey(modelName)) {
            // 停用所有模型
            localModels.values().forEach(model -> model.setActive(false));
            
            // 激活指定模型
            localModels.get(modelName).setActive(true);
            logger.info("切换到模型: " + modelName);
            return true;
        } else {
            logger.warning("模型不存在: " + modelName);
            return false;
        }
    }

    /**
     * 保存数据到本地
     */
    public boolean saveDataLocally(String key, String data) {
        try {
            Path dataFile = Paths.get(dataPath, key + ".json");
            Files.writeString(dataFile, data);
            logger.info("数据保存到本地: " + key);
            return true;
        } catch (Exception e) {
            logger.severe("保存数据失败: " + key + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * 从本地加载数据
     */
    public String loadDataFromLocal(String key) {
        try {
            Path dataFile = Paths.get(dataPath, key + ".json");
            if (Files.exists(dataFile)) {
                String data = Files.readString(dataFile);
                logger.info("从本地加载数据: " + key);
                return data;
            } else {
                logger.warning("本地数据不存在: " + key);
                return null;
            }
        } catch (Exception e) {
            logger.severe("加载数据失败: " + key + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * 缓存数据到本地
     */
    public boolean cacheData(String key, String data) {
        try {
            Path cacheFile = Paths.get(cachePath, key + ".cache");
            Files.writeString(cacheFile, data);
            logger.fine("数据缓存到本地: " + key);
            return true;
        } catch (Exception e) {
            logger.warning("缓存数据失败: " + key + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * 从缓存加载数据
     */
    public String loadFromCache(String key) {
        try {
            Path cacheFile = Paths.get(cachePath, key + ".cache");
            if (Files.exists(cacheFile)) {
                String data = Files.readString(cacheFile);
                logger.fine("从缓存加载数据: " + key);
                return data;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.warning("加载缓存失败: " + key + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        try {
            Path cacheDir = Paths.get(cachePath);
            if (Files.exists(cacheDir)) {
                Files.list(cacheDir)
                        .filter(Files::isRegularFile)
                        .forEach(file -> {
                            try {
                                Files.delete(file);
                            } catch (Exception e) {
                                logger.warning("删除缓存文件失败: " + file.getFileName() + ": " + e.getMessage());
                            }
                        });
                logger.info("缓存已清除");
            }
        } catch (Exception e) {
            logger.severe("清除缓存失败: " + e.getMessage());
        }
    }

    /**
     * 切换离线模式
     */
    public void setOfflineMode(boolean offline) {
        this.offlineMode = offline;
        logger.info("离线模式已" + (offline ? "开启" : "关闭"));
    }

    /**
     * 获取离线模式状态
     */
    public boolean isOfflineMode() {
        return offlineMode;
    }

    /**
     * 获取本地存储使用情况
     */
    public Map<String, Object> getStorageUsage() {
        Map<String, Object> usage = new HashMap<>();
        
        try {
            Path modelsDir = Paths.get(modelsPath);
            Path dataDir = Paths.get(dataPath);
            Path cacheDir = Paths.get(cachePath);

            usage.put("models", calculateDirectorySize(modelsDir));
            usage.put("data", calculateDirectorySize(dataDir));
            usage.put("cache", calculateDirectorySize(cacheDir));
            usage.put("total", (
                    (long) usage.get("models") + 
                    (long) usage.get("data") + 
                    (long) usage.get("cache")
            ));

        } catch (Exception e) {
            logger.severe("获取存储使用情况失败: " + e.getMessage());
        }

        return usage;
    }

    /**
     * 获取服务状态
     */
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        
        status.put("offlineMode", offlineMode);
        status.put("localModelsCount", localModels.size());
        status.put("activeModel", localModels.entrySet().stream()
                .filter(entry -> entry.getValue().isActive())
                .map(Map.Entry::getKey)
                .findFirst().orElse(null));
        status.put("storageUsage", getStorageUsage());
        status.put("directories", Map.of(
                "models", modelsPath,
                "data", dataPath,
                "cache", cachePath
        ));

        return status;
    }
}
