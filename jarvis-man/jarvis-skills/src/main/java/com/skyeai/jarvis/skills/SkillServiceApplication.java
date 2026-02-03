package com.skyeai.jarvis.skills;

import com.skyeai.jarvis.skills.registry.SkillRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Skill服务应用程序
 * 服务的入口点
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class SkillServiceApplication implements CommandLineRunner {

    @Autowired
    private SkillRegistry skillRegistry;

    public static void main(String[] args) {
        SpringApplication.run(SkillServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 初始化Skill注册表缓存
        skillRegistry.initializeCache();
        System.out.println("Skill Service Application initialized successfully");
    }

}
