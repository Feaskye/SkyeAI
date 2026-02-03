package com.skyeai.jarvis.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.custom.IntOrString;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * 增强自动化执行服务，用于管理Docker容器和Kubernetes资源
 */
@Service
public class EnhancedAutomationService {
    
    private DockerClient dockerClient;
    private ApiClient kubernetesClient;
    private CoreV1Api coreV1Api;
    private AppsV1Api appsV1Api;
    
    /**
     * 初始化Docker客户端
     */
    public void initializeDockerClient() {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        dockerClient = DockerClientBuilder.getInstance(config).build();
        System.out.println("Docker客户端初始化完成");
    }
    
    /**
     * 初始化Kubernetes客户端
     */
    public void initializeKubernetesClient() throws IOException {        
        kubernetesClient = Config.defaultClient();
        Configuration.setDefaultApiClient(kubernetesClient);
        coreV1Api = new CoreV1Api();
        appsV1Api = new AppsV1Api();
        System.out.println("Kubernetes客户端初始化完成");
    }
    
    /**
     * 运行Docker容器
     * @param image 镜像名称
     * @param containerName 容器名称
     * @param envVars 环境变量
     * @param ports 端口映射
     * @return 容器ID
     */
    public String runDockerContainer(String image, String containerName, List<String> envVars, List<PortBinding> ports) {
        if (dockerClient == null) {
            initializeDockerClient();
        }
        
        // 拉取镜像
        dockerClient.pullImageCmd(image).exec(new com.github.dockerjava.api.async.ResultCallback.Adapter<>());
        
        // 创建容器
        CreateContainerResponse container = dockerClient.createContainerCmd(image)
                .withName(containerName)
                .withEnv(envVars)
                .withPortBindings(ports)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withTty(true)
                .exec();
        
        // 启动容器
        dockerClient.startContainerCmd(container.getId()).exec();
        
        System.out.println("Docker容器启动成功: " + containerName + " (ID: " + container.getId() + ")");
        return container.getId();
    }
    
    /**
     * 停止Docker容器
     * @param containerId 容器ID
     */
    public void stopDockerContainer(String containerId) {
        if (dockerClient == null) {
            initializeDockerClient();
        }
        
        dockerClient.stopContainerCmd(containerId).exec();
        System.out.println("Docker容器停止成功: " + containerId);
    }
    
    /**
     * 删除Docker容器
     * @param containerId 容器ID
     */
    public void deleteDockerContainer(String containerId) {
        if (dockerClient == null) {
            initializeDockerClient();
        }
        
        dockerClient.removeContainerCmd(containerId).withForce(true).exec();
        System.out.println("Docker容器删除成功: " + containerId);
    }
    
    /**
     * 获取所有Docker容器
     * @return 容器列表
     */
    public List<Container> getDockerContainers() {
        if (dockerClient == null) {
            initializeDockerClient();
        }
        
        return dockerClient.listContainersCmd().withShowAll(true).exec();
    }
    
    /**
     * 创建Kubernetes部署
     * @param namespace 命名空间
     * @param deploymentName 部署名称
     * @param image 镜像名称
     * @param replicas 副本数
     * @param containerPort 容器端口
     * @param envVars 环境变量
     */
    public void createKubernetesDeployment(String namespace, String deploymentName, String image, int replicas, int containerPort, List<V1EnvVar> envVars) throws ApiException {
        if (kubernetesClient == null) {
            try {
                initializeKubernetesClient();
            } catch (IOException e) {
                throw new RuntimeException("Kubernetes客户端初始化失败: " + e.getMessage(), e);
            }
        }
        
        // 创建容器
        V1Container container = new V1Container()
                .name(deploymentName)
                .image(image)
                .ports(Collections.singletonList(new V1ContainerPort().containerPort(containerPort)))
                .env(envVars);
        
        // 创建容器规格
        V1PodSpec podSpec = new V1PodSpec()
                .containers(Collections.singletonList(container));
        
        // 创建Pod模板
        V1PodTemplateSpec podTemplateSpec = new V1PodTemplateSpec()
                .spec(podSpec)
                .metadata(new V1ObjectMeta().name(deploymentName));
        
        // 创建部署规格
        V1DeploymentSpec deploymentSpec = new V1DeploymentSpec()
                .replicas(replicas)
                .template(podTemplateSpec)
                .selector(new V1LabelSelector()
                        .matchLabels(Collections.singletonMap("app", deploymentName)));
        
        // 创建部署
        V1Deployment deployment = new V1Deployment()
                .metadata(new V1ObjectMeta().name(deploymentName))
                .spec(deploymentSpec);
        
        // 部署到Kubernetes
        appsV1Api.createNamespacedDeployment(namespace, deployment, null, null, null, null);
        System.out.println("Kubernetes部署创建成功: " + deploymentName + " (命名空间: " + namespace + ")");
    }
    
    /**
     * 创建Kubernetes服务
     * @param namespace 命名空间
     * @param serviceName 服务名称
     * @param deploymentName 部署名称
     * @param port 服务端口
     * @param targetPort 目标端口
     */
    public void createKubernetesService(String namespace, String serviceName, String deploymentName, int port, int targetPort) throws ApiException {
        if (kubernetesClient == null) {
            try {
                initializeKubernetesClient();
            } catch (IOException e) {
                throw new RuntimeException("Kubernetes客户端初始化失败: " + e.getMessage(), e);
            }
        }
        
        // 创建服务规格
        V1ServiceSpec serviceSpec = new V1ServiceSpec()
                .selector(Collections.singletonMap("app", deploymentName))
                .ports(Collections.singletonList(new V1ServicePort()
                        .port(port)
                        .targetPort(new IntOrString(targetPort))))
                .type("ClusterIP");
        
        // 创建服务
        V1Service service = new V1Service()
                .metadata(new V1ObjectMeta().name(serviceName))
                .spec(serviceSpec);
        
        // 部署到Kubernetes
        coreV1Api.createNamespacedService(namespace, service, null, null, null, null);
        System.out.println("Kubernetes服务创建成功: " + serviceName + " (命名空间: " + namespace + ")");
    }
    
    /**
     * 删除Kubernetes部署
     * @param namespace 命名空间
     * @param deploymentName 部署名称
     */
    public void deleteKubernetesDeployment(String namespace, String deploymentName) throws ApiException {
        if (kubernetesClient == null) {
            try {
                initializeKubernetesClient();
            } catch (IOException e) {
                throw new RuntimeException("Kubernetes客户端初始化失败: " + e.getMessage(), e);
            }
        }
        
        appsV1Api.deleteNamespacedDeployment(deploymentName, namespace, null, null, null, null, null, null);
        System.out.println("Kubernetes部署删除成功: " + deploymentName + " (命名空间: " + namespace + ")");
    }
    
    /**
     * 删除Kubernetes服务
     * @param namespace 命名空间
     * @param serviceName 服务名称
     */
    public void deleteKubernetesService(String namespace, String serviceName) throws ApiException {
        if (kubernetesClient == null) {
            try {
                initializeKubernetesClient();
            } catch (IOException e) {
                throw new RuntimeException("Kubernetes客户端初始化失败: " + e.getMessage(), e);
            }
        }
        
        coreV1Api.deleteNamespacedService(serviceName, namespace, null, null, null, null, null, null);
        System.out.println("Kubernetes服务删除成功: " + serviceName + " (命名空间: " + namespace + ")");
    }
    
    /**
     * 获取所有Kubernetes部署
     * @param namespace 命名空间
     * @return 部署列表
     */
    public V1DeploymentList getKubernetesDeployments(String namespace) throws ApiException {
        if (kubernetesClient == null) {
            try {
                initializeKubernetesClient();
            } catch (IOException e) {
                throw new RuntimeException("Kubernetes客户端初始化失败: " + e.getMessage(), e);
            }
        }
        
        return appsV1Api.listNamespacedDeployment(namespace, null, null, null, null, null, null, null, null, null, null);
    }
    
    /**
     * 获取所有Kubernetes服务
     * @param namespace 命名空间
     * @return 服务列表
     */
    public V1ServiceList getKubernetesServices(String namespace) throws ApiException {
        if (kubernetesClient == null) {
            try {
                initializeKubernetesClient();
            } catch (IOException e) {
                throw new RuntimeException("Kubernetes客户端初始化失败: " + e.getMessage(), e);
            }
        }
        
        return coreV1Api.listNamespacedService(namespace, null, null, null, null, null, null, null, null, null, null);
    }
}
