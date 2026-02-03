package com.skyeai.jarvis.controller;

import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.PortBinding;
import com.skyeai.jarvis.service.EnhancedAutomationService;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1ServiceList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 增强自动化执行控制器，用于处理Docker容器和Kubernetes资源管理的HTTP请求
 */
@RestController
@RequestMapping("/api/automation")
public class EnhancedAutomationController {
    
    @Autowired
    private EnhancedAutomationService enhancedAutomationService;
    
    // Docker相关接口
    
    /**
     * 运行Docker容器
     * @param image 镜像名称
     * @param containerName 容器名称
     * @param envVars 环境变量
     * @param ports 端口映射
     * @return 运行结果
     */
    @PostMapping("/docker/containers/run")
    public ResponseEntity<Map<String, Object>> runDockerContainer(
            @RequestParam("image") String image,
            @RequestParam("containerName") String containerName,
            @RequestParam(value = "envVars", required = false) List<String> envVars,
            @RequestParam(value = "ports", required = false) List<PortBinding> ports) {
        try {
            String containerId = enhancedAutomationService.runDockerContainer(image, containerName, envVars, ports);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Docker容器运行成功",
                    "containerId", containerId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Docker容器运行失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 停止Docker容器
     * @param containerId 容器ID
     * @return 操作结果
     */
    @PostMapping("/docker/containers/{containerId}/stop")
    public ResponseEntity<Map<String, Object>> stopDockerContainer(@PathVariable String containerId) {
        try {
            enhancedAutomationService.stopDockerContainer(containerId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Docker容器停止成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Docker容器停止失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 删除Docker容器
     * @param containerId 容器ID
     * @return 操作结果
     */
    @DeleteMapping("/docker/containers/{containerId}")
    public ResponseEntity<Map<String, Object>> deleteDockerContainer(@PathVariable String containerId) {
        try {
            enhancedAutomationService.deleteDockerContainer(containerId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Docker容器删除成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Docker容器删除失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取所有Docker容器
     * @return 容器列表
     */
    @GetMapping("/docker/containers")
    public List<Container> getDockerContainers() {
        return enhancedAutomationService.getDockerContainers();
    }
    
    // Kubernetes相关接口
    
    /**
     * 创建Kubernetes部署
     * @param namespace 命名空间
     * @param deploymentName 部署名称
     * @param image 镜像名称
     * @param replicas 副本数
     * @param containerPort 容器端口
     * @param envVars 环境变量
     * @return 操作结果
     */
    @PostMapping("/kubernetes/deployments")
    public ResponseEntity<Map<String, Object>> createKubernetesDeployment(
            @RequestParam("namespace") String namespace,
            @RequestParam("deploymentName") String deploymentName,
            @RequestParam("image") String image,
            @RequestParam("replicas") int replicas,
            @RequestParam("containerPort") int containerPort,
            @RequestParam(value = "envVars", required = false) List<V1EnvVar> envVars) {
        try {
            enhancedAutomationService.createKubernetesDeployment(namespace, deploymentName, image, replicas, containerPort, envVars);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Kubernetes部署创建成功"
            ));
        } catch (ApiException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Kubernetes部署创建失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 创建Kubernetes服务
     * @param namespace 命名空间
     * @param serviceName 服务名称
     * @param deploymentName 部署名称
     * @param port 服务端口
     * @param targetPort 目标端口
     * @return 操作结果
     */
    @PostMapping("/kubernetes/services")
    public ResponseEntity<Map<String, Object>> createKubernetesService(
            @RequestParam("namespace") String namespace,
            @RequestParam("serviceName") String serviceName,
            @RequestParam("deploymentName") String deploymentName,
            @RequestParam("port") int port,
            @RequestParam("targetPort") int targetPort) {
        try {
            enhancedAutomationService.createKubernetesService(namespace, serviceName, deploymentName, port, targetPort);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Kubernetes服务创建成功"
            ));
        } catch (ApiException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Kubernetes服务创建失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 删除Kubernetes部署
     * @param namespace 命名空间
     * @param deploymentName 部署名称
     * @return 操作结果
     */
    @DeleteMapping("/kubernetes/deployments/{namespace}/{deploymentName}")
    public ResponseEntity<Map<String, Object>> deleteKubernetesDeployment(
            @PathVariable String namespace,
            @PathVariable String deploymentName) {
        try {
            enhancedAutomationService.deleteKubernetesDeployment(namespace, deploymentName);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Kubernetes部署删除成功"
            ));
        } catch (ApiException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Kubernetes部署删除失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 删除Kubernetes服务
     * @param namespace 命名空间
     * @param serviceName 服务名称
     * @return 操作结果
     */
    @DeleteMapping("/kubernetes/services/{namespace}/{serviceName}")
    public ResponseEntity<Map<String, Object>> deleteKubernetesService(
            @PathVariable String namespace,
            @PathVariable String serviceName) {
        try {
            enhancedAutomationService.deleteKubernetesService(namespace, serviceName);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Kubernetes服务删除成功"
            ));
        } catch (ApiException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Kubernetes服务删除失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取所有Kubernetes部署
     * @param namespace 命名空间
     * @return 部署列表
     */
    @GetMapping("/kubernetes/deployments/{namespace}")
    public ResponseEntity<Map<String, Object>> getKubernetesDeployments(@PathVariable String namespace) {
        try {
            V1DeploymentList deployments = enhancedAutomationService.getKubernetesDeployments(namespace);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "deployments", deployments
            ));
        } catch (ApiException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "获取Kubernetes部署失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取所有Kubernetes服务
     * @param namespace 命名空间
     * @return 服务列表
     */
    @GetMapping("/kubernetes/services/{namespace}")
    public ResponseEntity<Map<String, Object>> getKubernetesServices(@PathVariable String namespace) {
        try {
            V1ServiceList services = enhancedAutomationService.getKubernetesServices(namespace);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "services", services
            ));
        } catch (ApiException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "获取Kubernetes服务失败: " + e.getMessage()
            ));
        }
    }
}
