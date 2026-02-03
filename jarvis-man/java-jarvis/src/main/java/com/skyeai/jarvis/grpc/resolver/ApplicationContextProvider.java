package com.skyeai.jarvis.grpc.resolver;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

public class ApplicationContextProvider {
    private static ApplicationContext applicationContext;
    
    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }
    
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
