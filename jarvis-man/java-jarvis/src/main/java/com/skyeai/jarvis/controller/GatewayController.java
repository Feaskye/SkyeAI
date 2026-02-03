package com.skyeai.jarvis.controller;

import com.skyeai.jarvis.service.GatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/gateway")
public class GatewayController {

    private static final Logger logger = Logger.getLogger(GatewayController.class.getName());

    @Autowired
    private GatewayService gatewayService;

    /**
     * 初始化网关服务
     */
    @PostMapping("/initialize")
    public ResponseEntity<?> initialize() {
        try {
            logger.info("初始化网关服务");
            gatewayService.initialize();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "网关服务初始化成功"
            ));
        } catch (Exception e) {
            logger.severe("初始化网关服务失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "初始化网关服务失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 注册节点
     */
    @PostMapping("/nodes/register")
    public ResponseEntity<?> registerNode(@RequestBody GatewayService.NodeInfo nodeInfo) {
        try {
            if (nodeInfo.getNodeId() == null || nodeInfo.getNodeId().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "节点ID不能为空"
                ));
            }

            boolean success = gatewayService.registerNode(nodeInfo);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "节点注册成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "节点注册失败"
                ));
            }
        } catch (Exception e) {
            logger.severe("注册节点失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "注册节点失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 注销节点
     */
    @PostMapping("/nodes/unregister")
    public ResponseEntity<?> unregisterNode(@RequestBody Map<String, String> request) {
        try {
            String nodeId = request.get("nodeId");
            if (nodeId == null || nodeId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "节点ID不能为空"
                ));
            }

            boolean success = gatewayService.unregisterNode(nodeId);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "节点注销成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "节点注销失败"
                ));
            }
        } catch (Exception e) {
            logger.severe("注销节点失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "注销节点失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 更新节点心跳
     */
    @PostMapping("/nodes/heartbeat")
    public ResponseEntity<?> updateNodeHeartbeat(@RequestBody Map<String, String> request) {
        try {
            String nodeId = request.get("nodeId");
            if (nodeId == null || nodeId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "节点ID不能为空"
                ));
            }

            boolean success = gatewayService.updateNodeHeartbeat(nodeId);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "节点心跳更新成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "节点心跳更新失败，节点不存在"
                ));
            }
        } catch (Exception e) {
            logger.severe("更新节点心跳失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "更新节点心跳失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取节点列表
     */
    @GetMapping("/nodes")
    public ResponseEntity<?> getNodes() {
        try {
            var nodes = gatewayService.getAllNodes();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "nodes", nodes
            ));
        } catch (Exception e) {
            logger.severe("获取节点列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "获取节点列表失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 注册技能
     */
    @PostMapping("/skills/register")
    public ResponseEntity<?> registerSkill(@RequestBody GatewayService.SkillInfo skillInfo) {
        try {
            if (skillInfo.getSkillId() == null || skillInfo.getSkillId().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "技能ID不能为空"
                ));
            }

            boolean success = gatewayService.registerSkill(skillInfo);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "技能注册成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "技能注册失败"
                ));
            }
        } catch (Exception e) {
            logger.severe("注册技能失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "注册技能失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 注销技能
     */
    @PostMapping("/skills/unregister")
    public ResponseEntity<?> unregisterSkill(@RequestBody Map<String, String> request) {
        try {
            String skillId = request.get("skillId");
            if (skillId == null || skillId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "技能ID不能为空"
                ));
            }

            boolean success = gatewayService.unregisterSkill(skillId);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "技能注销成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "技能注销失败"
                ));
            }
        } catch (Exception e) {
            logger.severe("注销技能失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "注销技能失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取技能列表
     */
    @GetMapping("/skills")
    public ResponseEntity<?> getSkills() {
        try {
            var skills = gatewayService.getAllSkills();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "skills", skills
            ));
        } catch (Exception e) {
            logger.severe("获取技能列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "获取技能列表失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 提交任务
     */
    @PostMapping("/tasks/submit")
    public ResponseEntity<?> submitTask(@RequestBody Map<String, Object> request) {
        try {
            String taskType = (String) request.get("taskType");
            Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");

            if (taskType == null || taskType.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "任务类型不能为空"
                ));
            }

            CompletableFuture<GatewayService.TaskInfo> future = gatewayService.submitTask(taskType, parameters);
            GatewayService.TaskInfo taskInfo = future.join();

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "任务提交成功",
                    "task", taskInfo
            ));
        } catch (Exception e) {
            logger.severe("提交任务失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "提交任务失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取任务状态
     */
    @GetMapping("/tasks/status")
    public ResponseEntity<?> getTaskStatus(@RequestParam String taskId) {
        try {
            if (taskId == null || taskId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "任务ID不能为空"
                ));
            }

            var taskInfo = gatewayService.getTaskStatus(taskId);
            if (taskInfo != null) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "task", taskInfo
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "任务不存在"
                ));
            }
        } catch (Exception e) {
            logger.severe("获取任务状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "获取任务状态失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取所有任务
     */
    @GetMapping("/tasks")
    public ResponseEntity<?> getTasks() {
        try {
            var tasks = gatewayService.getAllTasks();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "tasks", tasks
            ));
        } catch (Exception e) {
            logger.severe("获取任务列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "获取任务列表失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取网关服务状态
     */
    @GetMapping("/status")
    public ResponseEntity<?> getGatewayStatus() {
        try {
            var status = gatewayService.getServiceStatus();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", status
            ));
        } catch (Exception e) {
            logger.severe("获取网关服务状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "获取网关服务状态失败: " + e.getMessage()
            ));
        }
    }
}
