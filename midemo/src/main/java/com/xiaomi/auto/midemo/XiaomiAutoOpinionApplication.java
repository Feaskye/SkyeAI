package com.xiaomi.auto.midemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.xiaomi.auto.midemo")
public class XiaomiAutoOpinionApplication {
    public static void main(String[] args) {
        SpringApplication.run(XiaomiAutoOpinionApplication.class, args);
    }
}