package com.skyeai.jarvis;

import com.skyeai.jarvis.grpc.resolver.ApplicationContextProvider;
import com.skyeai.jarvis.grpc.resolver.NacosNameResolverProvider;
import io.grpc.NameResolverProvider;
import io.grpc.NameResolverRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableDiscoveryClient
public class JarvisApplication {
    
    public static void main(String[] args) {
        // 直接注册NacosNameResolverProvider
        NacosNameResolverProvider provider = new NacosNameResolverProvider();
        NameResolverRegistry.getDefaultRegistry().register(provider);
        
        SpringApplication.run(JarvisApplication.class, args);
    }
    
    @Configuration
    public static class GrpcClientNacosConfig {
        @Bean
        public ApplicationContextInitializer applicationContextInitializer() {
            return new ApplicationContextInitializer<ConfigurableApplicationContext>() {
                @Override
                public void initialize(ConfigurableApplicationContext context) {
                    ApplicationContextProvider.setApplicationContext(context);
                }
            };
        }
    }
}
