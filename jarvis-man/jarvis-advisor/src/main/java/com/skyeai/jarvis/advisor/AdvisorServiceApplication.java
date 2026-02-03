package com.skyeai.jarvis.advisor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AdvisorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdvisorServiceApplication.class, args);
    }
}