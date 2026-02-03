package com.skyeai.jarvis.grpc.resolver;

import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.Status;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class NacosNameResolver extends NameResolver {
    
    private final URI targetUri;
    private final String serviceName;
    private Listener listener;
    
    public NacosNameResolver(URI targetUri) {
        this.targetUri = targetUri;
        this.serviceName = targetUri.getHost();
    }
    
    @Override
    public String getServiceAuthority() {
        return serviceName;
    }
    
    @Override
    public void start(Listener listener) {
        this.listener = listener;
        resolve();
    }
    
    private void resolve() {
        try {
            // 从Nacos获取服务实例
            List<EquivalentAddressGroup> servers = new ArrayList<>();
            
            // 使用ApplicationContext获取DiscoveryClient
            org.springframework.context.ApplicationContext context = 
                ApplicationContextProvider.getApplicationContext();
            
            if (context != null) {
                org.springframework.cloud.client.discovery.DiscoveryClient discoveryClient = 
                    context.getBean(org.springframework.cloud.client.discovery.DiscoveryClient.class);
                
                List<org.springframework.cloud.client.ServiceInstance> instances = 
                    discoveryClient.getInstances(serviceName);
                
                for (org.springframework.cloud.client.ServiceInstance instance : instances) {
                    // 获取gRPC端口，如果元数据中有则使用，否则使用默认端口
                    String grpcPortStr = instance.getMetadata().get("gRPC_port");
                    int grpcPort = 9090; // 默认gRPC端口
                    
                    if (grpcPortStr != null && !grpcPortStr.isEmpty()) {
                        try {
                            grpcPort = Integer.parseInt(grpcPortStr);
                        } catch (NumberFormatException e) {
                            // 如果解析失败，使用默认端口
                            grpcPort = 9090;
                        }
                    }
                    
                    SocketAddress socketAddress = new InetSocketAddress(
                        instance.getHost(), 
                        grpcPort
                    );
                    servers.add(new EquivalentAddressGroup(socketAddress));
                }
            }
            
            if (!servers.isEmpty()) {
                listener.onAddresses(servers, io.grpc.Attributes.EMPTY);
            } else {
                listener.onError(Status.NOT_FOUND.withDescription("No instances found for service: " + serviceName));
            }
        } catch (Exception e) {
            listener.onError(Status.INTERNAL.withCause(e));
        }
    }
    
    @Override
    public void shutdown() {
    }
}
