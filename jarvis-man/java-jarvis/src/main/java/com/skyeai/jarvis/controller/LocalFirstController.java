package com.skyeai.jarvis.controller;

import com.skyeai.jarvis.service.LocalFirstService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/local-first")
public class LocalFirstController {

    private static final Logger logger = Logger.getLogger(LocalFirstController.class.getName());

    @Autowired
    private LocalFirstService localFirstService;

    /**
     * 初始化本地优先服务
     */
    @PostMapping("/initialize")
    public ResponseEntity<?> initialize() {
        try {
            logger.info("初始化本地优先服务");
            localFirstService.initialize();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "本地优先服务初始化成功"
            ));
        } catch (Exception e) {
            logger.severe("初始化本地优先服务失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "初始化本地优先服务失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取本地模型列表
     */
    @GetMapping("/models")
    public ResponseEntity<?> getLocalModels() {
        try {
            logger.info("获取本地模型列表");
            var models = localFirstService.getAllLocalModels();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "models", models
            ));
        } catch (Exception e) {
            logger.severe("获取本地模型列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "获取本地模型列表失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 下载模型到本地
     */
    @PostMapping("/models/download")
    public ResponseEntity<?> downloadModel(@RequestBody Map<String, String> request) {
        try {
            String modelName = request.get("modelName");
            if (modelName == null || modelName.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "模型名称不能为空"
                ));
            }

            logger.info("开始下载模型: " + modelName);
            CompletableFuture<LocalFirstService.ModelInfo> future = localFirstService.downloadModel(modelName);
            
            // 等待下载完成
            LocalFirstService.ModelInfo modelInfo = future.join();
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "模型下载成功",
                    "model", modelInfo
            ));
        } catch (Exception e) {
            logger.severe("模型下载失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "模型下载失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 切换模型
     */
    @PostMapping("/models/switch")
    public ResponseEntity<?> switchModel(@RequestBody Map<String, String> request) {
        try {
            String modelName = request.get("modelName");
            if (modelName == null || modelName.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "模型名称不能为空"
                ));
            }

            boolean success = localFirstService.switchModel(modelName);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "模型切换成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "模型切换失败，模型不存在"
                ));
            }
        } catch (Exception e) {
            logger.severe("模型切换失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "模型切换失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 保存数据到本地
     */
    @PostMapping("/data/save")
    public ResponseEntity<?> saveDataLocally(@RequestBody Map<String, String> request) {
        try {
            String key = request.get("key");
            String data = request.get("data");
            
            if (key == null || key.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "数据键不能为空"
                ));
            }
            
            if (data == null || data.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "数据内容不能为空"
                ));
            }

            boolean success = localFirstService.saveDataLocally(key, data);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "数据保存成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "数据保存失败"
                ));
            }
        } catch (Exception e) {
            logger.severe("数据保存失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "数据保存失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 从本地加载数据
     */
    @GetMapping("/data/load")
    public ResponseEntity<?> loadDataFromLocal(@RequestParam String key) {
        try {
            if (key == null || key.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "数据键不能为空"
                ));
            }

            String data = localFirstService.loadDataFromLocal(key);
            if (data != null) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "数据加载成功",
                        "data", data
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "数据不存在"
                ));
            }
        } catch (Exception e) {
            logger.severe("数据加载失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "数据加载失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 缓存数据到本地
     */
    @PostMapping("/cache/save")
    public ResponseEntity<?> cacheData(@RequestBody Map<String, String> request) {
        try {
            String key = request.get("key");
            String data = request.get("data");
            
            if (key == null || key.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "缓存键不能为空"
                ));
            }
            
            if (data == null || data.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "缓存内容不能为空"
                ));
            }

            boolean success = localFirstService.cacheData(key, data);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "数据缓存成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "数据缓存失败"
                ));
            }
        } catch (Exception e) {
            logger.severe("数据缓存失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "数据缓存失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 从缓存加载数据
     */
    @GetMapping("/cache/load")
    public ResponseEntity<?> loadFromCache(@RequestParam String key) {
        try {
            if (key == null || key.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "缓存键不能为空"
                ));
            }

            String data = localFirstService.loadFromCache(key);
            if (data != null) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "缓存加载成功",
                        "data", data
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "缓存不存在"
                ));
            }
        } catch (Exception e) {
            logger.severe("缓存加载失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "缓存加载失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 清除缓存
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<?> clearCache() {
        try {
            localFirstService.clearCache();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "缓存已清除"
            ));
        } catch (Exception e) {
            logger.severe("清除缓存失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "清除缓存失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 切换离线模式
     */
    @PostMapping("/offline-mode")
    public ResponseEntity<?> setOfflineMode(@RequestBody Map<String, Boolean> request) {
        try {
            Boolean offline = request.get("offline");
            if (offline == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "离线模式状态不能为空"
                ));
            }

            localFirstService.setOfflineMode(offline);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "离线模式已" + (offline ? "开启" : "关闭")
            ));
        } catch (Exception e) {
            logger.severe("切换离线模式失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "切换离线模式失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取离线模式状态
     */
    @GetMapping("/offline-mode")
    public ResponseEntity<?> getOfflineMode() {
        try {
            boolean offlineMode = localFirstService.isOfflineMode();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "offlineMode", offlineMode
            ));
        } catch (Exception e) {
            logger.severe("获取离线模式状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "获取离线模式状态失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取本地存储使用情况
     */
    @GetMapping("/storage")
    public ResponseEntity<?> getStorageUsage() {
        try {
            var usage = localFirstService.getStorageUsage();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "storage", usage
            ));
        } catch (Exception e) {
            logger.severe("获取存储使用情况失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "获取存储使用情况失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取服务状态
     */
    @GetMapping("/status")
    public ResponseEntity<?> getServiceStatus() {
        try {
            var status = localFirstService.getServiceStatus();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", status
            ));
        } catch (Exception e) {
            logger.severe("获取服务状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "获取服务状态失败: " + e.getMessage()
            ));
        }
    }
}
