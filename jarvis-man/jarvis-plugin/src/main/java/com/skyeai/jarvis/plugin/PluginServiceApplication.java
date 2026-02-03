package com.skyeai.jarvis.plugin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PluginServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PluginServiceApplication.class, args);
    }
}