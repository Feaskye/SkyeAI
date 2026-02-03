package com.skyeai.jarvis.dify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DifyIntegrationApplication {
    public static void main(String[] args) {
        SpringApplication.run(DifyIntegrationApplication.class, args);
    }
}