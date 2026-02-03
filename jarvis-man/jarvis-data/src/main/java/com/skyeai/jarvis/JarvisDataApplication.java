package com.skyeai.jarvis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 数据服务主类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaRepositories("com.skyeai.jarvis.repository")
public class JarvisDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(JarvisDataApplication.class, args);
    }

}