package com.skyeai.jarvis.sql.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FunctionCallService {

    @Value("${function-call.max-params}")
    private int maxParams;

    @Value("${function-call.max-parameter-length}")
    private int maxParameterLength;

    @Value("${function-call.enable-auto-discovery}")
    private boolean enableAutoDiscovery;

    @Value("${tools.discovery.scan-packages}")
    private List<String> scanPackages;

    // 使用Map存储工具，支持版本管理
    private final Map<String, Map<String, ToolDefinition>> toolsByVersion = new HashMap<>();
    // 默认版本
    private static final String DEFAULT_VERSION = "1.0";

    @PostConstruct
    public void init() {
        System.out.println("FunctionCallService initialized successfully");
        System.out.println("Enable auto discovery: " + enableAutoDiscovery);
        System.out.println("Scan packages: " + scanPackages);

        if (enableAutoDiscovery) {
            discoverTools();
        }
    }

    /**
     * 自动发现工具
     */
    private void discoverTools() {
        try {
            for (String packageName : scanPackages) {
                String path = packageName.replace('.', '/');
                Enumeration<URL> resources = getClass().getClassLoader().getResources(path);
                
                while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();
                    File directory = new File(resource.getFile());
                    if (directory.exists()) {
                        scanDirectory(packageName, directory);
                    }
                }
            }
            
            // 统计工具数量
            int totalTools = 0;
            for (Map<String, ToolDefinition> versionMap : toolsByVersion.values()) {
                totalTools += versionMap.size();
            }
            System.out.println("Discovered " + totalTools + " tools");
        } catch (Exception e) {
            System.err.println("Failed to discover tools: " + e.getMessage());
        }
    }

    /**
     * 扫描目录查找工具类
     * @param packageName 包名
     * @param directory 目录
     */
    private void scanDirectory(String packageName, File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(packageName + "." + file.getName(), file);
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (clazz.isAnnotationPresent(Tool.class)) {
                            Tool toolAnnotation = clazz.getAnnotation(Tool.class);
                            ToolDefinition tool = new ToolDefinition();
                            tool.setName(toolAnnotation.name());
                            tool.setDescription(toolAnnotation.description());
                            tool.setClassName(className);
                            tool.setVersion(DEFAULT_VERSION);
                            
                            // 处理参数
                            String[] params = toolAnnotation.parameters();
                            if (params != null && params.length > 0) {
                                List<String> parameterList = new ArrayList<>();
                                for (String param : params) {
                                    parameterList.add(param);
                                }
                                tool.setParameters(parameterList);
                            }
                            
                            tool.setReturnType(toolAnnotation.returnType());
                            registerTool(tool);
                            System.out.println("Discovered tool: " + tool.getName() + " v" + tool.getVersion() + " - " + tool.getDescription());
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to load class: " + className + ", error: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 获取所有工具
     * @return 工具列表
     */
    public List<ToolDefinition> getAllTools() {
        List<ToolDefinition> allTools = new ArrayList<>();
        for (Map<String, ToolDefinition> versionMap : toolsByVersion.values()) {
            allTools.addAll(versionMap.values());
        }
        return allTools;
    }

    /**
     * 根据名称获取工具（默认版本）
     * @param name 工具名称
     * @return 工具定义
     */
    public ToolDefinition getToolByName(String name) {
        return getToolByName(name, DEFAULT_VERSION);
    }

    /**
     * 根据名称和版本获取工具
     * @param name 工具名称
     * @param version 工具版本
     * @return 工具定义
     */
    public ToolDefinition getToolByName(String name, String version) {
        Map<String, ToolDefinition> versionMap = toolsByVersion.get(version);
        if (versionMap != null) {
            return versionMap.get(name);
        }
        return null;
    }

    /**
     * 注册工具
     * @param tool 工具定义
     */
    public void registerTool(ToolDefinition tool) {
        String version = tool.getVersion() != null ? tool.getVersion() : DEFAULT_VERSION;
        toolsByVersion.computeIfAbsent(version, k -> new HashMap<>()).put(tool.getName(), tool);
        System.out.println("Registered tool: " + tool.getName() + " v" + version);
    }

    /**
     * 注销工具
     * @param name 工具名称
     * @param version 工具版本
     */
    public void unregisterTool(String name, String version) {
        Map<String, ToolDefinition> versionMap = toolsByVersion.get(version);
        if (versionMap != null) {
            versionMap.remove(name);
            System.out.println("Unregistered tool: " + name + " v" + version);
        }
    }

    /**
     * 获取指定版本的所有工具
     * @param version 工具版本
     * @return 工具列表
     */
    public List<ToolDefinition> getToolsByVersion(String version) {
        Map<String, ToolDefinition> versionMap = toolsByVersion.get(version);
        if (versionMap != null) {
            return new ArrayList<>(versionMap.values());
        }
        return new ArrayList<>();
    }

    /**
     * 工具注解
     */
    public @interface Tool {
        String name();
        String description();
        String[] parameters() default {};
        String returnType() default "string";
        String version() default "1.0";
    }

    /**
     * 工具定义类
     */
    public static class ToolDefinition {
        private String name;
        private String description;
        private String className;
        private List<String> parameters;
        private String returnType;
        private String version;
        private List<String> dependencies;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public List<String> getParameters() {
            return parameters;
        }

        public void setParameters(List<String> parameters) {
            this.parameters = parameters;
        }

        public String getReturnType() {
            return returnType;
        }

        public void setReturnType(String returnType) {
            this.returnType = returnType;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public List<String> getDependencies() {
            return dependencies;
        }

        public void setDependencies(List<String> dependencies) {
            this.dependencies = dependencies;
        }
    }
}
