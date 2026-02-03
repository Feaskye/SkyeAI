package com.skyeai.jarvis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Service
public class GatewayService {

    private static final Logger logger = Logger.getLogger(GatewayService.class.getName());

    @Value("${gateway.port}")
    private int gatewayPort;

    @Value("${gateway.host}")
    private String gatewayHost;

    // 节点注册表
    private final Map<String, NodeInfo> nodes = new HashMap<>();

    // 技能注册表
    private final Map<String, SkillInfo> skills = new HashMap<>();

    // 任务队列
    private final Map<String, TaskInfo> tasks = new HashMap<>();

    /**
     * 节点信息实体类
     */
    public static class NodeInfo {
        private String nodeId;
        private String host;
        private int port;
        private String type;
        private boolean online;
        private long lastHeartbeat;
        private Map<String, Object> resources;

        // Getters and Setters
        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isOnline() {
            return online;
        }

        public void setOnline(boolean online) {
            this.online = online;
        }

        public long getLastHeartbeat() {
            return lastHeartbeat;
        }

        public void setLastHeartbeat(long lastHeartbeat) {
            this.lastHeartbeat = lastHeartbeat;
        }

        public Map<String, Object> getResources() {
            return resources;
        }

        public void setResources(Map<String, Object> resources) {
            this.resources = resources;
        }
    }

    /**
     * 技能信息实体类
     */
    public static class SkillInfo {
        private String skillId;
        private String name;
        private String description;
        private String type;
        private String endpoint;
        private Map<String, Object> parameters;
        private boolean enabled;

        // Getters and Setters
        public String getSkillId() {
            return skillId;
        }

        public void setSkillId(String skillId) {
            this.skillId = skillId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * 任务信息实体类
     */
    public static class TaskInfo {
        private String taskId;
        private String type;
        private String status;
        private Map<String, Object> parameters;
        private String nodeId;
        private long startTime;
        private long endTime;
        private Object result;

        // Getters and Setters
        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }
    }

    /**
     * 初始化网关服务
     */
    public void initialize() {
        logger.info("初始化网关服务，监听地址: " + gatewayHost + ":" + gatewayPort);
        
        // 启动心跳检测
        startHeartbeatMonitor();
        
        // 加载默认技能
        loadDefaultSkills();
    }

    /**
     * 加载默认技能
     */
    private void loadDefaultSkills() {
        // 添加默认技能
        SkillInfo browserSkill = new SkillInfo();
        browserSkill.setSkillId("browser-1");
        browserSkill.setName("browser");
        browserSkill.setDescription("控制无头浏览器，抓取/操作网页");
        browserSkill.setType("system");
        browserSkill.setEndpoint("http://localhost:8081/api/browser");
        browserSkill.setEnabled(true);
        skills.put(browserSkill.getSkillId(), browserSkill);
        
        SkillInfo sandboxSkill = new SkillInfo();
        sandboxSkill.setSkillId("sandbox-1");
        sandboxSkill.setName("sandbox");
        sandboxSkill.setDescription("在安全沙箱中执行代码");
        sandboxSkill.setType("system");
        sandboxSkill.setEndpoint("http://localhost:8081/api/sandbox");
        sandboxSkill.setEnabled(true);
        skills.put(sandboxSkill.getSkillId(), sandboxSkill);
        
        logger.info("加载默认技能: " + skills.keySet());
    }

    /**
     * 注册节点
     */
    public boolean registerNode(NodeInfo nodeInfo) {
        try {
            logger.info("注册节点: " + nodeInfo.getNodeId() + " (" + nodeInfo.getHost() + ":" + nodeInfo.getPort() + ")");
            
            nodeInfo.setOnline(true);
            nodeInfo.setLastHeartbeat(System.currentTimeMillis());
            
            nodes.put(nodeInfo.getNodeId(), nodeInfo);
            
            return true;
        } catch (Exception e) {
            logger.severe("注册节点失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 注销节点
     */
    public boolean unregisterNode(String nodeId) {
        try {
            logger.info("注销节点: " + nodeId);
            
            nodes.remove(nodeId);
            
            return true;
        } catch (Exception e) {
            logger.severe("注销节点失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 更新节点心跳
     */
    public boolean updateNodeHeartbeat(String nodeId) {
        try {
            if (nodes.containsKey(nodeId)) {
                NodeInfo nodeInfo = nodes.get(nodeId);
                nodeInfo.setLastHeartbeat(System.currentTimeMillis());
                nodeInfo.setOnline(true);
                logger.fine("更新节点心跳: " + nodeId);
                return true;
            } else {
                logger.warning("节点不存在: " + nodeId);
                return false;
            }
        } catch (Exception e) {
            logger.severe("更新节点心跳失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 注册技能
     */
    public boolean registerSkill(SkillInfo skillInfo) {
        try {
            logger.info("注册技能: " + skillInfo.getSkillId() + " (" + skillInfo.getName() + ")");
            
            skills.put(skillInfo.getSkillId(), skillInfo);
            
            return true;
        } catch (Exception e) {
            logger.severe("注册技能失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 注销技能
     */
    public boolean unregisterSkill(String skillId) {
        try {
            logger.info("注销技能: " + skillId);
            
            skills.remove(skillId);
            
            return true;
        } catch (Exception e) {
            logger.severe("注销技能失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 提交任务
     */
    public CompletableFuture<TaskInfo> submitTask(String taskType, Map<String, Object> parameters) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String taskId = "task-" + System.currentTimeMillis();
                logger.info("提交任务: " + taskId + " (" + taskType + ")");
                
                // 创建任务信息
                TaskInfo taskInfo = new TaskInfo();
                taskInfo.setTaskId(taskId);
                taskInfo.setType(taskType);
                taskInfo.setStatus("pending");
                taskInfo.setParameters(parameters);
                taskInfo.setStartTime(System.currentTimeMillis());
                
                // 选择节点
                String nodeId = selectNodeForTask(taskType, parameters);
                if (nodeId == null) {
                    taskInfo.setStatus("failed");
                    taskInfo.setEndTime(System.currentTimeMillis());
                    throw new RuntimeException("没有可用的节点");
                }
                
                taskInfo.setNodeId(nodeId);
                taskInfo.setStatus("running");
                
                // 保存任务
                tasks.put(taskId, taskInfo);
                
                // 执行任务
                Object result = executeTaskOnNode(nodeId, taskType, parameters);
                
                // 更新任务状态
                taskInfo.setStatus("completed");
                taskInfo.setResult(result);
                taskInfo.setEndTime(System.currentTimeMillis());
                
                logger.info("任务完成: " + taskId);
                
                return taskInfo;
            } catch (Exception e) {
                logger.severe("提交任务失败: " + e.getMessage());
                throw new RuntimeException("提交任务失败: " + e.getMessage());
            }
        });
    }

    /**
     * 选择节点执行任务（负载均衡）
     */
    private String selectNodeForTask(String taskType, Map<String, Object> parameters) {
        // 实现基于资源利用率的负载均衡算法
        String bestNodeId = null;
        double bestScore = Double.MAX_VALUE;
        
        for (String nodeId : nodes.keySet()) {
            NodeInfo nodeInfo = nodes.get(nodeId);
            if (nodeInfo.isOnline()) {
                // 计算节点得分（资源利用率越低得分越好）
                double score = calculateNodeScore(nodeInfo);
                if (score < bestScore) {
                    bestScore = score;
                    bestNodeId = nodeId;
                }
            }
        }
        return bestNodeId;
    }

    /**
     * 计算节点得分
     */
    private double calculateNodeScore(NodeInfo nodeInfo) {
        Map<String, Object> resources = nodeInfo.getResources();
        if (resources == null) {
            return 100.0; // 默认得分
        }
        
        // 计算CPU、内存、网络等资源的综合利用率
        double cpuUsage = resources.containsKey("cpuUsage") ? ((Number) resources.get("cpuUsage")).doubleValue() : 0;
        double memoryUsage = resources.containsKey("memoryUsage") ? ((Number) resources.get("memoryUsage")).doubleValue() : 0;
        double networkUsage = resources.containsKey("networkUsage") ? ((Number) resources.get("networkUsage")).doubleValue() : 0;
        
        // 加权平均
        return (cpuUsage * 0.4) + (memoryUsage * 0.4) + (networkUsage * 0.2);
    }

    /**
     * 在节点上执行任务（带故障转移）
     */
    private Object executeTaskOnNode(String nodeId, String taskType, Map<String, Object> parameters) {
        // 实现故障转移机制
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                logger.info("在节点 " + nodeId + " 上执行任务: " + taskType);
                
                // 模拟任务执行
                try {
                    Thread.sleep(1000); // 模拟执行时间
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // 模拟节点故障（10%概率）
                if (Math.random() < 0.1 && retryCount == 0) {
                    throw new RuntimeException("节点 " + nodeId + " 执行任务失败");
                }
                
                return Map.of(
                        "status", "success",
                        "message", "任务执行成功",
                        "data", parameters,
                        "nodeId", nodeId
                );
            } catch (Exception e) {
                retryCount++;
                logger.warning("节点 " + nodeId + " 执行任务失败，尝试第 " + retryCount + " 次重试: " + e.getMessage());
                
                // 标记节点为离线
                if (nodes.containsKey(nodeId)) {
                    NodeInfo nodeInfo = nodes.get(nodeId);
                    nodeInfo.setOnline(false);
                    logger.warning("节点 " + nodeId + " 已标记为离线");
                }
                
                // 选择新节点
                String newNodeId = selectNodeForTask(taskType, parameters);
                if (newNodeId == null) {
                    if (retryCount >= maxRetries) {
                        throw new RuntimeException("没有可用的节点执行任务");
                    }
                } else {
                    nodeId = newNodeId;
                    logger.info("故障转移到节点 " + nodeId);
                }
            }
        }
        
        throw new RuntimeException("任务执行失败，已达到最大重试次数");
    }

    /**
     * 启动心跳检测
     */
    private void startHeartbeatMonitor() {
        // 实现定时任务，检测节点的心跳
        logger.info("启动节点心跳检测");
        
        // 每30秒检查一次节点状态
        CompletableFuture.runAsync(() -> {
            while (true) {
                try {
                    Thread.sleep(30000);
                    checkNodeHeartbeats();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    /**
     * 检查节点心跳
     */
    private void checkNodeHeartbeats() {
        long currentTime = System.currentTimeMillis();
        long heartbeatTimeout = 60000; // 60秒超时
        
        for (String nodeId : nodes.keySet()) {
            NodeInfo nodeInfo = nodes.get(nodeId);
            if (nodeInfo.isOnline() && (currentTime - nodeInfo.getLastHeartbeat() > heartbeatTimeout)) {
                logger.warning("节点 " + nodeId + " 心跳超时，标记为离线");
                nodeInfo.setOnline(false);
            }
        }
    }

    /**
     * 获取任务状态
     */
    public TaskInfo getTaskStatus(String taskId) {
        return tasks.get(taskId);
    }

    /**
     * 获取所有节点
     */
    public Map<String, NodeInfo> getAllNodes() {
        return nodes;
    }

    /**
     * 获取所有技能
     */
    public Map<String, SkillInfo> getAllSkills() {
        return skills;
    }

    /**
     * 获取所有任务
     */
    public Map<String, TaskInfo> getAllTasks() {
        return tasks;
    }

    /**
     * 获取服务状态
     */
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        
        int onlineNodes = 0;
        for (NodeInfo nodeInfo : nodes.values()) {
            if (nodeInfo.isOnline()) {
                onlineNodes++;
            }
        }
        
        int enabledSkills = 0;
        for (SkillInfo skillInfo : skills.values()) {
            if (skillInfo.isEnabled()) {
                enabledSkills++;
            }
        }
        
        int runningTasks = 0;
        for (TaskInfo taskInfo : tasks.values()) {
            if ("running".equals(taskInfo.getStatus())) {
                runningTasks++;
            }
        }
        
        status.put("onlineNodes", onlineNodes);
        status.put("totalNodes", nodes.size());
        status.put("enabledSkills", enabledSkills);
        status.put("totalSkills", skills.size());
        status.put("runningTasks", runningTasks);
        status.put("totalTasks", tasks.size());
        status.put("gatewayAddress", gatewayHost + ":" + gatewayPort);
        
        return status;
    }
}
