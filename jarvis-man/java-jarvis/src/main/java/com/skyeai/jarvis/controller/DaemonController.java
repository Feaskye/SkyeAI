package com.skyeai.jarvis.controller;

import com.skyeai.jarvis.service.DaemonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/daemon")
public class DaemonController {

    private static final Logger logger = Logger.getLogger(DaemonController.class.getName());

    @Autowired
    private DaemonService daemonService;

    /**
     * 启动守护进程服务
     */
    @PostMapping("/start")
    public ResponseEntity<?> start() {
        try {
            logger.info("启动守护进程服务");
            boolean success = daemonService.start();
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "守护进程服务启动成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "守护进程服务启动失败"
                ));
            }
        } catch (Exception e) {
            logger.severe("启动守护进程服务失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "启动守护进程服务失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 停止守护进程服务
     */
    @PostMapping("/stop")
    public ResponseEntity<?> stop() {
        try {
            logger.info("停止守护进程服务");
            boolean success = daemonService.stop();
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "守护进程服务停止成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "守护进程服务停止失败"
                ));
            }
        } catch (Exception e) {
            logger.severe("停止守护进程服务失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "停止守护进程服务失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 重启守护进程服务
     */
    @PostMapping("/restart")
    public ResponseEntity<?> restart() {
        try {
            logger.info("重启守护进程服务");
            boolean success = daemonService.restart();
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "守护进程服务重启成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "守护进程服务重启失败"
                ));
            }
        } catch (Exception e) {
            logger.severe("重启守护进程服务失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "重启守护进程服务失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取守护进程服务状态
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        try {
            logger.info("获取守护进程服务状态");
            var status = daemonService.getStatus();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", Map.of(
                            "status", status.toString(),
                            "serviceInfo", daemonService.getServiceInfo()
                    )
            ));
        } catch (Exception e) {
            logger.severe("获取守护进程服务状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "获取守护进程服务状态失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取守护进程服务信息
     */
    @GetMapping("/info")
    public ResponseEntity<?> getInfo() {
        try {
            logger.info("获取守护进程服务信息");
            var info = daemonService.getServiceInfo();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", info
            ));
        } catch (Exception e) {
            logger.severe("获取守护进程服务信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "获取守护进程服务信息失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取健康检查状态
     */
    @GetMapping("/health")
    public ResponseEntity<?> getHealth() {
        try {
            logger.info("获取健康检查状态");
            var healthStatus = daemonService.getHealthCheckStatus();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", healthStatus
            ));
        } catch (Exception e) {
            logger.severe("获取健康检查状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "获取健康检查状态失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 安装为系统服务
     */
    @PostMapping("/install")
    public ResponseEntity<?> install() {
        try {
            logger.info("安装为系统服务");
            boolean success = daemonService.installAsService();
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "系统服务安装成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "系统服务安装失败"
                ));
            }
        } catch (Exception e) {
            logger.severe("安装系统服务失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "安装系统服务失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 卸载系统服务
     */
    @PostMapping("/uninstall")
    public ResponseEntity<?> uninstall() {
        try {
            logger.info("卸载系统服务");
            boolean success = daemonService.uninstallService();
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "系统服务卸载成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "系统服务卸载失败"
                ));
            }
        } catch (Exception e) {
            logger.severe("卸载系统服务失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "卸载系统服务失败: " + e.getMessage()
            ));
        }
    }
}
