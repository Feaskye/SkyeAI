# Jarvis AI项目总结

## 1. 项目概述

Jarvis AI是一个基于Stark Industries概念的智能助手系统，旨在提供全面的AI服务，包括语音交互、健康监测、IoT设备控制和自动化执行等功能。项目采用微服务架构，分为Java主控服务和Go边缘服务，支持跨平台部署。

## 2. 核心需求

根据`jarvis-ai.md`文档，项目实现了以下阶段需求：

### 2.1 阶段0：基础框架搭建
- ✅ 基于Java 17和Spring Boot 3.x构建主控服务
- ✅ Go语言实现边缘代理服务
- ✅ gRPC通信机制
- ✅ WebSocket实时通信

### 2.2 阶段1：语音交互与日程管理
- ✅ 语音识别（ASR）API
- ✅ 文本转语音（TTS）API
- ✅ 音频流处理接口
- ✅ 日程管理功能

### 2.3 阶段2：自动化执行引擎
- ✅ 工具注册表实现
- ✅ ReAct控制器
- ✅ 安全沙箱执行环境
- ✅ 浏览器自动化控制
- ✅ 多工具协作能力

### 2.4 阶段3：健康监测和IoT集成
- ✅ 健康数据模型设计
- ✅ 健康数据聚合与存储
- ✅ 异常检测服务
- ✅ MQTT协议支持
- ✅ IoT设备通信
- ✅ 实时数据可视化

## 3. 现有服务架构

项目采用Docker Compose部署，包含以下核心服务：

| 服务名称 | 技术栈 | 主要功能 | 端口 |
|---------|--------|---------|------|
| **java-jarvis** | Java 17 + Spring Boot 3.x | 主控服务、AI交互、健康数据管理 | 8080, 9090 |
| **edge-proxy** | Go 1.22 | 边缘服务、IoT控制、音频处理 | 8081, 9091 |
| **mqtt** | Go 1.22 | 模拟MQTT服务器 | 9003, 9004 |
| **ollama** | Docker | 本地LLM服务 | 11434 |
| **qdrant** | Docker | 向量数据库 | 6333, 6334 |

## 4. 核心功能实现

### 4.1 AI交互功能
- 基于Alibaba AI模型的对话系统
- 动态模型切换（qwen-turbo → qwen-max → qwen-plus → qwen-long-latest）
- WebSocket实时通信
- 系统提示词配置

### 4.2 健康监测功能
- 健康数据模型（心率、睡眠质量、步数、卡路里、血氧等）
- 数据聚合与统计分析
- 规则-based异常检测
- 健康数据可视化界面

### 4.3 IoT设备控制
- MQTT协议支持
- 设备命令发送与接收
- 设备状态监控
- 命令执行反馈

### 4.4 自动化执行引擎
- 工具注册表管理
- 沙箱环境代码执行
- 浏览器自动化控制（导航、截图、数据抓取）
- 多工具协同工作流

### 4.5 语音处理功能
- 语音识别（ASR）
- 文本转语音（TTS）
- 音频流WebSocket接口

## 5. 技术栈

### 5.1 后端技术
- **Java服务**：
  - Java 17
  - Spring Boot 3.3.5
  - Spring AI
  - gRPC
  - Eclipse Paho MQTT客户端
  - Jackson JSON处理

- **Go服务**：
  - Go 1.22
  - Gin Web框架
  - gRPC
  - Mochi MQTT库
  - 模拟传感器数据生成

### 5.2 通信协议
- **gRPC**：Java-Go服务间通信
- **MQTT**：IoT设备通信
- **WebSocket**：前端实时通信
- **HTTP/REST**：外部API接口

### 5.3 数据存储
- 健康数据：内存存储（可扩展至数据库）
- 向量数据：Qdrant向量数据库
- LLM模型：Ollama本地存储

### 5.4 部署技术
- **Docker Compose**：服务编排
- **Docker**：容器化部署
- **环境变量配置**：服务参数动态调整

## 6. 部署架构

### 6.1 Docker网络
```
┌─────────────────┐
│  jarvis-network │
└─────────┬───────┘
          │
┌─────────┼─────────────────────────────────────────────────┐
│         │                                                 │
▼         ▼                                                 ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│    java-jarvis  │ │    edge-proxy   │ │      ollama     │
└─────────────────┘ └─────────────────┘ └─────────────────┘
         │                  │                  │
         ▼                  ▼                  ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│      mqtt       │ │      qdrant     │ │   前端应用      │
└─────────────────┘ └─────────────────┘ └─────────────────┘
```

### 6.2 服务依赖关系
```
java-jarvis → ollama, edge-proxy, mqtt
edge-proxy → mqtt
前端应用 → java-jarvis
```

### 6.3 部署命令
```bash
docker-compose up -d      # 启动所有服务
docker-compose down       # 停止所有服务
docker-compose build      # 重建服务镜像
```

## 7. 配置管理

### 7.1 Java服务配置
- 文件：`application.properties`
- 主要配置项：
  - AI模型配置（阿里云API Key、模型列表）
  - gRPC服务配置
  - MQTT连接参数
  - 系统提示词

### 7.2 Go服务配置
- 硬编码配置（可扩展为配置文件）
- 主要配置项：
  - MQTT连接参数
  - 传感器配置
  - HTTP服务端口

### 7.3 环境变量
- `MQTT_BROKER_URL`：MQTT服务器地址
- `SPRING_AI_OLLAMA_BASE_URL`：Ollama服务地址
- `GRPC_CLIENT_EDGE_ADDRESS`：边缘服务gRPC地址

## 8. 关键模块实现

### 8.1 健康数据服务
- **文件**：`HealthDataService.java`
- **功能**：数据聚合、存储、统计分析
- **异常检测**：规则-based检测，支持多种健康指标

### 8.2 MQTT消息处理
- **文件**：`MqttMessageHandler.java`（Java）、`client.go`（Go）
- **功能**：MQTT连接管理、消息订阅与发布、命令处理

### 8.3 工具服务
- **文件**：`ToolService.java`
- **功能**：工具注册表管理、默认工具加载、工具执行调度

### 8.4 模拟MQTT服务器
- **文件**：`main.go`（mock-mqtt-server）
- **功能**：MQTT协议支持、事件处理、日志记录

## 9. 前端界面

### 9.1 主要功能页面
- **聊天界面**：AI交互、语音输入/输出
- **健康监测**：健康数据可视化、异常告警
- **IoT控制**：设备状态监控、命令发送
- **自动化任务**：工具调用、工作流管理

### 9.2 技术栈
- HTML5 + CSS3 + JavaScript
- WebSocket实时通信
- 响应式设计

## 10. 监控与日志

### 10.1 日志系统
- Java服务：SLF4J + Logback
- Go服务：标准库日志
- MQTT服务器：事件日志记录

### 10.2 监控指标
- 服务健康状态
- 连接数统计
- 消息吞吐量
- 错误率监控

## 11. 未来扩展方向

1. **AI模型增强**：
   - 集成更多AI模型
   - 模型微调支持
   - 本地模型优化

2. **IoT生态扩展**：
   - 支持更多IoT协议（CoAP、LoRaWAN）
   - 设备管理平台
   - 规则引擎

3. **健康功能增强**：
   - 机器学习异常检测
   - 健康报告生成
   - 健康建议系统

4. **自动化能力提升**：
   - 自然语言任务规划
   - 多工具自动协作
   - 学习型自动化

5. **部署优化**：
   - Kubernetes支持
   - 服务网格集成
   - 边缘计算增强

## 12. 快速开始

### 12.1 环境准备
- Docker 20.10+
- Docker Compose 1.29+
- Git

### 12.2 部署步骤

1. **克隆代码仓库**：
   ```bash
   git clone <repository-url>
   cd jarvis-man
   ```

2. **启动服务**：
   ```bash
   docker-compose up -d
   ```

3. **访问应用**：
   - Web界面：http://localhost:8080
   - Java API文档：http://localhost:8080/swagger-ui.html
   - Go API文档：http://localhost:8081/swagger/index.html

4. **测试MQTT服务**：
   - MQTT TCP连接：tcp://localhost:9003
   - MQTT WebSocket：ws://localhost:9004

## 13. 项目结构

```
jarvis-man/
├── java-jarvis/           # Java主控服务
│   ├── src/main/java/     # Java源代码
│   ├── src/main/resources/# 配置文件
│   ├── pom.xml            # Maven配置
│   └── Dockerfile         # Docker构建文件
├── go-edge/               # Go边缘服务
│   ├── cmd/               # 主程序入口
│   ├── pkg/               # 核心包
│   ├── go.mod             # Go模块配置
│   └── Dockerfile         # Docker构建文件
├── mock-mqtt-server/      # 模拟MQTT服务器
│   ├── main.go            # 主程序
│   ├── go.mod             # Go模块配置
│   └── Dockerfile         # Docker构建文件
├── docs/                  # 文档目录
├── docker-compose.yml     # Docker Compose配置
└── jarvis-ai.md           # 项目设计文档
```

## 14. 联系方式

- 项目维护：SkyeAI团队
- 技术支持：<support@skyeai.com>
- 文档地址：https://skyeai.com/jarvis/docs

---

**版本**: 1.0.0  
**更新日期**: 2026-01-18  
**版权**: © 2026 SkyeAI. All rights reserved.
