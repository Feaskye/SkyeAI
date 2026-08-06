# 贾维斯助手 v10 需求与优化整改方案

> **版本**：v10
> **日期**：2026-08-06
> **范围**：基于 v9-3 现状盘点，将 Spring AI 2.0.0 (GA) + Spring AI Alibaba 2.0.0-M1.1 + Spring Boot 4.0.x + Milvus 向量库 + Spring AI 原生工具调用（function-call）+ Text-to-SQL（DataAgent / SuperSQL 思路）+ Advisor 日志/敏感词拦截器 + 系统/用户提示词模板 + ChatMemory 落库向量库及历史记忆检索等技术集成进现有代码；对已存在同类能力（自研 InnerTool、Qdrant、AgentCore）以开关切换方式保留并存。
> **技术基线**：Spring Boot 4.0.x + Spring Framework 7.0 + Java 17（最低）/21+（推荐）+ Jakarta EE 11
> **目的**：在不大改 v9 架构的前提下，把"占位实现 / 模拟数据 / 硬编码"替换为 Spring AI 2.0 + SAA 2.0 体系下的真实可用能力，并利用 SAA 生态组件（ReactAgent / DataAgent / Graph / Studio / Nacos 热更新）实现质的飞跃，同时提供多向量库、多工具协议、多提示词来源、多 Agent 框架的开关切换能力。

---

## 目录

1. [现状评估（基于代码证据）](#1-现状评估基于代码证据)
2. [v10 技术清单与差距矩阵](#2-v10-技术清单与差距矩阵)
3. [版本升级基线与兼容性](#3-版本升级基线与兼容性)
4. [核心集成方案](#4-核心集成方案)
5. [全局开关设计总表](#5-全局开关设计总表)
6. [实施路线图](#6-实施路线图)
7. [风险与验收](#7-风险与验收)

---

## 1. 现状评估（基于代码证据）

### 1.1 Spring AI / Spring AI Alibaba 依赖情况

- 14 个模块 `pom.xml` 中 **零 `spring-ai-*` / `spring-ai-alibaba-*` 依赖**（全仓库 `grep spring-ai` 无命中）。
- 全部模块 Spring Boot **3.3.5 / Java 17**（基于 Spring Framework 6 / Jakarta EE 10）。v10 需升级至 **Spring Boot 4.0.x / Spring Framework 7 / Jakarta EE 11**，Java 基线保持 17（最低）或升至 21+（推荐，适配虚拟线程）。
- `java-jarvis/src/main/resources/application.properties:15-17` 已配置 `spring.ai.ollama.*` 三项，但 `java-jarvis/pom.xml` 无 spring-ai 依赖，**配置与依赖不一致**（属于历史遗留死配置）。
- LLM 调用走 `dashscope-sdk-java` + Apache HttpClient5 直连阿里通义（`jarvis-llm/src/main/java/com/skyeai/jarvis/llm/service/impl/OpenAiLlmServiceImpl.java`），未走 Spring AI `ChatModel` 抽象。
- `Message` 类全部是自定义：`com.skyeai.jarvis.agent.Message`、`com.alibaba.dashscope.common.Message`，**未使用 `org.springframework.ai.chat.messages.Message`**。

### 1.2 向量库使用情况

- 仅使用 **Qdrant**，无 Milvus。
- `jarvis-rag/MultiRecallRagService.java:65-68,173-179` 通过 `WebClient` 直连 Qdrant REST `/collections/{c}/points/search`，未走 Spring AI `VectorStore` 抽象。
- `jarvis-data/src/main/java/com/skyeai/jarvis/config/VectorService.java:13-22` 是**内存 HashMap 占位**（注释自述"作为 Qdrant 占位符实现"），`searchSimilarChatHistory` 完全忽略 `queryVector` 参数（VectorService.java:85-121）。
- `jarvis-rag/pom.xml:79-85` 中 `qdrant-client` 依赖被注释掉，改为 REST 调用。
- **`MultiRecallRagService` 无任何业务调用方**（grep 仅命中类定义本身），`RagService.retrieveDocuments` 直接返回空 List（RagService.java:96-98）。
- collection 现状：VectorService 内存常量 2 个（`chat_history` / `user_preference`，均为占位）；Qdrant 真实 collection 数 = 0（无 createCollection 代码）。

### 1.3 工具调用（function-call）

- 自研体系：`InnerTool` 接口 + `ToolCallback` POJO（非 `org.springframework.ai.tool.ToolCallback`）+ `ToolRegistry`（Spring 自动扫描注册）。
- 实现**仅 2 个**：`WeatherTool`、`StockTool`（`java-jarvis/.../agent/tool/impl/`，支持真实 API + 模拟降级）。
- `AgentCore.java:303-334 convertToLlmTools` 手写 OpenAI function-calling JSON Schema 构造；`:340-381 handleToolCall` 手写解析 toolCallData 执行工具。
- **缺陷**：`handleToolCall` 执行一次后直接 return，**无多轮 ReAct 循环**，未把工具结果回传 LLM 做总结（虽然 `LlmClient.summarizeToolResults` 已存在但未被调用）。
- MCP 集成（`McpClientManager.java:140,146` / `McpServer.java:44-51`）均为桩实现：`discoverTools()` 返回 `List.of()`，`callTool()` 返回固定字符串 `"MCP工具响应"`。
- `@Tool` / `@ToolParam` / `MethodToolCallback` / `ToolCallbacks`（Spring AI 原生）**全仓库零命中**。

### 1.4 Text-to-SQL

- `jarvis-sql/src/main/java/com/skyeai/jarvis/sql/service/TextToSqlService.java:43-60` 是**关键字硬编码 SQL 模板**（`if query.contains("所有用户") return "SELECT * FROM users"`），注释自述"实际应用中应该使用AI模型生成SQL"。
- `FunctionCallService.java:194-200` 自定义 `@Tool` 注解（非 Spring AI），通过反射扫描 `.class` 文件，但**只有注册/查询/注销方法，无 execute/invoke**，`ToolDefinition.implementationClass` 字段从未被反射加载。
- `TextToSqlController.java:39-42 POST /execute` 直接执行用户传入 SQL，**存在 SQL 注入风险**（无白名单、无参数化）。
- `application.yml:103-109` 配置 `tools.discovery.scan-packages: [com.skyeai.jarvis.sql.tool, com.skyeai.jarvis.tool]`，**这两个包在源码中不存在**，自动扫描实际发现不到任何工具。
- 无 DB-GPT / Vanna / SuperSQL / DataAgent 等第三方 Text2SQL 框架依赖。

### 1.5 Advisor / 日志 / 敏感词拦截

- `jarvis-advisor` 模块是业务"溯源/融合"模块，**非 Spring AI Advisor**：`AdvisorService.java:12` 无 `implements Advisor`，pom 无 spring-ai 依赖。
- `org.springframework.ai.chat.client.advisor.Advisor` / `QuestionAnswerAdvisor` / `MessageChatMemoryAdvisor` / `SafeGuardAdvisor` **全仓库零命中**。
- `java-jarvis/.../agent/security/ContentSecurityFilter.java:46-48` 敏感词为占位符（`"敏感词1","敏感词2","敏感词3"`），正则含"政治""敏感"等过宽词；**该 Bean 从未被主链路调用**（grep 调用方为 0）。
- `monitor/AgentMonitoringAspect.java:53` 仅 `@Around` 拦截 `AgentCore.chat` / `chatWithTools` 两个方法，`:76-77` 输出纯文本日志，`:115-118 recordMetric` 仅 `log.debug`，未接 Prometheus。
- `java-jarvis/pom.xml` **无 `micrometer-prometheus-registry` 依赖**；`jarvis-advisor/application.yml:128-148` 配置了 prometheus 端点但 pom 同样缺依赖（端点不可用）。
- 全部日志为传统 `log.info("文本 {}", args)` 格式，**未使用结构化 JSON 编码器**。
- 网关层（`GatewayService` / `GatewayController` / Spring Cloud Gateway 路由）**无任何鉴权 Filter**。

### 1.6 系统/用户提示词

- 未使用 `org.springframework.ai.chat.prompt.Prompt` / `PromptTemplate`，无 `resources/prompts/*.st` 模板文件。
- 系统提示词分散在 **10+ 处硬编码**：
  - `AgentCore.java:89-90`（`@Value` 字段版）+ `:229`（方法内局部硬编码版，**两套不一致**）。
  - `ChatController.java:256,262,310`、`AliyunAIServiceImpl.java:36-37`、`OpenAiLlmServiceImpl.java:149-150`、`LlmController.java:25`、`AgentController.java:26,39`、`ReactServiceImpl.java:251`、`MultimodalFusionServiceImpl.java:321`、`SubAgent.java:34,77-82`。
- `jarvis-llm/.../service/impl/PromptServiceImpl.java:14-23` 系统提示存内存 `HashMap`，硬编码 5 种类型（general/expert/creative/technical/friendly），模板渲染是自实现正则替换（`:92-106`），未用 Spring AI `PromptTemplate`。
- `PromptEngineer` 在 jarvis-llm 模块无 `@Service` 注解（`PromptEngineer.java:9`），可能未被 Spring 托管。
- `application.properties:27` 与 `jarvis-llm/application.yml:81-86` 重复定义同一 J.A.R.V.I.S. 文案（`base.system.prompt` 与 `ai.system.prompt`）。

### 1.7 ChatMemory 与历史记忆检索

- `ChatMemory.java:22` 是自定义 `@Component`，**未实现 `org.springframework.ai.chat.memory.ChatMemory` 接口**。
- 三层压缩完整可用：Assistant 裁剪（`:174-176,219-286`）+ LLM 摘要（`:180-211`）+ 滑动窗口（`:139-142`）。
- 存储纯内存 `ArrayList`（`:73 history`）；`PersistentChatMemory.java:61-70,115-118` 落 Redis（List 存 Message 对象 + String 存摘要），**无向量化**。
- `jarvis-data/.../service/TextEmbeddingService.java:16-31` 用 `java.util.Random` 生成 768 维随机向量（每次结果不同，注释自述"实际应用中应使用真实嵌入模型"）。
- `OpenAiLlmServiceImpl.java:350-354 embedText` 返回空 List。
- `MultiRecallRagService.java:286-307 generateFallbackEmbedding` 基于 `text.hashCode() + Math.sin` 生成 1536 维伪向量（确定性但无语义）。
- **历史记忆检索完全缺失**：
  - `VectorService.searchSimilarChatHistory` 定义但无人调用。
  - gRPC proto 定义了 `searchSimilarChatHistory` / `searchSimilarUserPreferences`，但 `DataServiceImpl.java` **未 `@Override` 实现**这两个方法。
  - `LongTermMemoryServiceImpl` 通过 gRPC 调 dataServiceStub，但只调 save/get/analyze，无 searchSimilar。
  - grep `历史记忆|记忆检索|recallMemory` 全部 No matches found。

### 1.8 现状总结矩阵

| 维度 | 现状 | 完成度 | v10 方向 |
|------|------|--------|----------|
| Spring AI 依赖 | 零依赖，配置死代码 | 0% | 引入 spring-ai-bom 2.0.0 + spring-ai-alibaba 2.0.0-M1.1 |
| Spring Boot 基线 | 3.3.5 / Framework 6 / Jakarta EE 10 | — | 升级至 4.0.x / Framework 7 / Jakarta EE 11 |
| 向量库 | 仅 Qdrant REST，部分内存占位 | 30% | Milvus + Qdrant 开关切换，多 collection 分工 |
| 工具调用 | 自研 InnerTool，2 个实现，无 ReAct 循环 | 40% | Spring AI 2.0 @Tool + SAA ReactAgent + 自研兼容开关 + ReAct 循环 |
| Text-to-SQL | 关键字硬编码，无 LLM，SQL 注入风险 | 10% | SAA DataAgent（首选）+ SuperSQL 思路（备选）+ 白名单 |
| Advisor | 业务模块同名，无 Spring AI Advisor | 0% | Spring AI 2.0 Advisor 链 + SAA Graph Observation |
| 敏感词拦截 | ContentSecurityFilter 未接入主链路 | 5% | 适配为 Advisor 接入主链路 + 词库外置 |
| 日志监控 | 纯文本日志，未接 Prometheus | 20% | 结构化 JSON + Micrometer + Prometheus + SAA Studio |
| 提示词 | 10+ 处硬编码，无模板 | 10% | PromptTemplate + .st 文件 + SAA Nacos 模型热更新 |
| ChatMemory 落库 | 纯内存 + Redis，无向量化 | 35% | Spring AI 2.0 ChatMemory 接口 + 向量库长期记忆 |
| 历史记忆检索 | 完全缺失 | 0% | 用户问题向量化 → 检索 chat_memory collection → 注入 Prompt |
| Agent 框架 | 自研 AgentCore，无多智能体编排 | 40% | SAA Agent Framework（ReactAgent + Graph + Skills）+ 自研开关 |
| spring-ai-alibaba | 未引入 | 0% | 统一引入 2.0.0-M1.1（dashscope + agent-framework + graph + studio） |

---

## 2. v10 技术清单与差距矩阵

### 2.1 用户提出的技术清单映射

| 用户提出的技术 | 现状 | 是否已有同类 | v10 处理方式 |
|---------------|------|-------------|-------------|
| Spring AI 最新版本 | 零依赖 | 否 | **全新引入** spring-ai-bom 2.0.0 (GA) |
| 向量库 Milvus | 仅 Qdrant（且部分占位） | 是（Qdrant） | **开关切换** `vector-store.type=milvus\|qdrant` |
| 内部工具调用 tools（function-call） | 自研 InnerTool + AgentCore 手写 function-calling | 是 | **开关切换** `tool.mode=spring-ai\|custom\|hybrid`，新增 SAA ReactAgent ReAct 循环 |
| SuperSQL（text-to-sql） | 关键字硬编码 | 否 | **首选 SAA DataAgent**（官方生态），**备选 SuperSQL 思路**自研，开关切换 |
| advise 日志或敏感词拦截器 | ContentSecurityFilter 未接入 + AgentMonitoringAspect 纯文本 | 部分 | **全新引入** Spring AI 2.0 Advisor 体系 + SAA Graph Observation |
| 系统/用户提示词 | 10+ 处硬编码 | 否 | **全新引入** PromptTemplate + .st 模板 + SAA Nacos 热更新 |
| chatmemory 落库向量库及检索历史记忆 | 纯内存 + Redis，无向量化，无检索 | 部分（压缩层可用） | **改造** 实现 Spring AI 2.0 ChatMemory + 向量库长期记忆 |
| spring-ai-alibaba 依赖 | 未引入 | 否 | **全新引入** spring-ai-alibaba 2.0.0-M1.1 全套（dashscope + agent-framework + graph + studio + nacos） |

### 2.2 关键依赖版本选型（v10 升级版）

| 依赖 | 版本 | 用途 |
|------|------|------|
| `spring-boot-starter-parent` | `4.0.x` | Spring Boot 4.0 主基线 |
| `spring-ai-bom` | `2.0.0` | Spring AI 2.0 GA BOM 统一版本管理 |
| `spring-ai-alibaba-bom` | `2.0.0-M1.1` | Spring AI Alibaba BOM（里程碑版） |
| `spring-ai-alibaba-extensions-bom` | 跟随 SAA BOM | Spring AI Extensions（DashScopeChatModel 等） |
| `spring-ai-alibaba-starter-dashscope` | 跟随 SAA BOM | 阿里通义 ChatModel + EmbeddingModel |
| `spring-ai-alibaba-agent-framework` | 跟随 SAA BOM | ReactAgent、多智能体编排、Hooks、Skills |
| `spring-ai-alibaba-graph-core` | 跟随 SAA BOM | 图工作流运行时、持久化、流式、MCP 节点 |
| `spring-ai-alibaba-studio` | 跟随 SAA BOM | 嵌入式 Agent 调试与可视化 UI |
| `spring-ai-alibaba-starter-config-nacos` | 跟随 SAA BOM | 基于 Nacos 的动态配置与模型热更新 |
| `spring-ai-alibaba-starter-graph-observation` | 跟随 SAA BOM | Graph 可观测性（Micrometer/OpenTelemetry） |
| `spring-ai-milvus-store-spring-boot-starter` | 跟随 spring-ai-bom | Milvus 向量存储 |
| `spring-ai-qdrant-store-spring-boot-starter` | 跟随 spring-ai-bom | Qdrant 向量存储（保留兼容） |
| `milvus-sdk-java` | `2.4.x`+ | Milvus Java SDK（starter 间接引入） |
| `micrometer-registry-prometheus` | `1.14.x`+ | Prometheus 指标暴露（Spring Boot 4 配套） |
| `logstash-logback-encoder` | `8.x` | 结构化 JSON 日志（适配 Logback 1.5+） |
| **DataAgent**（SAA 生态项目） | 跟随 SAA | 自然语言转 SQL 智能体（替代 SuperSQL） |

> **注意**：spring-ai-alibaba 2.0.0-M1.1 为里程碑版（M1），API 可能存在调整。需通过 `spring-ai-alibaba-bom` + `spring-ai-bom` + `spring-ai-alibaba-extensions-bom` 三 BOM 统一管理，避免版本冲突。Spring Boot 4.0 对应 Spring Cloud 2025.x，需同步升级 spring-cloud-alibaba。

---

## 3. 版本升级基线与兼容性

### 3.1 版本对应关系

```
Spring Boot 4.0.x
  └── Spring Framework 7.0
  └── Java 17（最低）/ 21+（推荐，虚拟线程）
  └── Jakarta EE 11（Servlet 6.1 / JPA 3.2）
  └── Spring Cloud 2025.x
       └── Spring Cloud Alibaba 2025.x（配套 Nacos 客户端）

Spring AI 2.0.0 (GA)
  └── 强制依赖 Spring Boot 4.0 + Spring Framework 7
  └── 不兼容 Spring Boot 3.x / Spring Framework 6

Spring AI Alibaba 2.0.0-M1.1
  └── 依赖 Spring AI 2.0.0
  └── 依赖 Spring Boot 4.0.x
  └── 里程碑版（M1），API 可能调整，生产需评估稳定性
```

### 3.2 升级影响清单

| 维度 | v9 现状 | v10 目标 | 影响与处理 |
|------|---------|---------|-----------|
| Java 版本 | 17 | 17（最低）/ 21+（推荐） | 代码无需改；JDK 升级到 21 可用虚拟线程、record 模式 |
| Spring Boot | 3.3.5 | 4.0.x | parent 版本升级；`javax.*` → `jakarta.*`（项目已用 Jakarta，影响小） |
| Spring Framework | 6.x | 7.0 | AOP/Core 兼容；`@HttpExchange` 原生支持可替代 Feign |
| Jakarta EE | 10 | 11（Servlet 6.1） | **Undertow 不兼容**（未适配 Servlet 6.1），需切换 Tomcat/Jetty |
| Spring Cloud | — | 2025.x | spring-cloud-gateway / spring-cloud-alibaba 需配套升级 |
| Nacos 客户端 | 2.x | 2025.x 配套版 | 验证 Nacos SDK 对 Jakarta EE 11 兼容性 |
| dashscope-sdk-java | 独立引入 | 可移除（由 spring-ai-alibaba-starter-dashscope 替代） | 保留 legacy 开关时仍需 |
| IDE | — | IDEA 2025+ | Spring Boot 4 需要 IDEA 2025+ 支持识别 |
| GraalVM | 未使用 | 可选支持 | Spring Boot 4 原生镜像支持增强，可选启用 |

### 3.3 关键兼容性风险

| 风险项 | 概率 | 影响 | 应对 |
|--------|------|------|------|
| Spring Boot 3→4 跨大版本升级 | 高 | 编译/运行时错误 | 逐模块升级，先验证空项目启动，再迁移业务代码 |
| SAA 2.0.0-M1.1 里程碑版 API 不稳定 | 中 | Bean 装配失败 / API 变更 | 锁定 M1.1 版本；关键路径保留自研 legacy 开关回退 |
| Undertow 不兼容 Servlet 6.1 | 高 | 启动失败 | 排查各模块是否用 Undertow；统一切换 `spring-boot-starter-tomcat` |
| Spring Cloud Alibaba 版本不齐 | 中 | Nacos 注册/配置失效 | 等待 2025.x 配套版；或暂时用 Eureka/本地配置 |
| 第三方 SDK Jakarta EE 11 兼容 | 中 | 连接失败 | 逐个验证 Nacos/Redis/Milvus/Qdrant/gRPC SDK |
| spring-ai 2.0 API 变更（相比 1.0） | 中 | 编译错误 | 参照 Spring AI 2.0 迁移指南调整 |

---

## 4. 核心集成方案

### 4.1 Spring AI 2.0 + Spring AI Alibaba 2.0 依赖统一引入

#### 4.1.1 三 BOM 引入

在 `java-jarvis/pom.xml`、`jarvis-llm/pom.xml`、`jarvis-rag/pom.xml`、`jarvis-data/pom.xml`、`jarvis-sql/pom.xml`、`jarvis-advisor/pom.xml`、`jarvis-knowledge/pom.xml` 七个核心模块统一引入：

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.x</version>
</parent>

<dependencyManagement>
    <dependencies>
        <!-- Spring AI BOM 2.0.0 GA -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>2.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        <!-- Spring AI Alibaba BOM 2.0.0-M1.1 -->
        <dependency>
            <groupId>com.alibaba.cloud.ai</groupId>
            <artifactId>spring-ai-alibaba-bom</artifactId>
            <version>2.0.0-M1.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        <!-- Spring AI Extensions BOM -->
        <dependency>
            <groupId>com.alibaba.cloud.ai</groupId>
            <artifactId>spring-ai-alibaba-extensions-bom</artifactId>
            <version>2.0.0-M1.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<properties>
    <java.version>17</java.version>
</properties>
```

#### 4.1.2 模块依赖分配

| 模块 | 新增依赖 | 用途 |
|------|---------|------|
| `jarvis-llm` | `spring-ai-alibaba-starter-dashscope` | 提供 `DashScopeChatModel` + `DashScopeEmbeddingModel`，替换手写 HttpClient 调用 |
| `jarvis-rag` | `spring-ai-alibaba-starter-dashscope` + `spring-ai-milvus-store-spring-boot-starter` + `spring-ai-qdrant-store-spring-boot-starter` | EmbeddingModel + 双 VectorStore |
| `jarvis-data` | `spring-ai-alibaba-starter-dashscope` + `spring-ai-milvus-store-spring-boot-starter` | ChatHistory 向量化落库 |
| `java-jarvis` | `spring-ai-alibaba-starter-dashscope` + `spring-ai-alibaba-agent-framework` + `spring-ai-alibaba-graph-core` + `spring-ai-alibaba-studio` + `spring-ai-alibaba-starter-config-nacos` + `spring-ai-alibaba-starter-graph-observation` | AgentCore 走 ReactAgent，工具调用走原生 ToolCallback，Graph 工作流，Studio 调试，Nacos 热更新 |
| `jarvis-sql` | `spring-ai-alibaba-starter-dashscope` + DataAgent（SAA 生态） | Text2SQL 走 DataAgent 智能体 |
| `jarvis-advisor` | `spring-ai-alibaba-starter-dashscope` | 改造为 Spring AI Advisor 实现 |

#### 4.1.3 jarvis-llm 改造：DashScopeChatModel 替换手写 HttpClient

`OpenAiLlmServiceImpl.java:350-354 embedText` 返回空的问题，通过 `DashScopeEmbeddingModel` 直接修复：

```java
@Service
@ConditionalOnProperty(name = "jarvis.llm.impl", havingValue = "spring-ai", matchIfMissing = true)
public class SpringAiLlmServiceImpl implements LlmService {

    private final DashScopeChatModel chatModel;
    private final DashScopeEmbeddingModel embeddingModel;

    @Autowired
    public SpringAiLlmServiceImpl(
            DashScopeChatModel chatModel,
            DashScopeEmbeddingModel embeddingModel) {
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
    }

    @Override
    public List<Double> embedText(String text) {
        if (text == null || text.isBlank()) {
            // 缺失配置显式报错，不静默返回空
            throw new IllegalArgumentException("embedText 输入不能为空");
        }
        EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));
        if (response.getResults().isEmpty()) {
            throw new IllegalStateException("嵌入模型返回空结果，请检查 DASHSCOPE_API_KEY 配置");
        }
        return response.getResults().get(0).getOutput().stream()
                .map(Double::valueOf)
                .collect(Collectors.toList());
    }
}
```

配置开关：

```yaml
jarvis:
  llm:
    impl: spring-ai   # spring-ai | legacy（保留原 dashscope-sdk + HttpClient5）
```

通过 `@ConditionalOnProperty` 切换，`legacy` 时激活原 `OpenAiLlmServiceImpl`。

---

### 4.2 向量库：Milvus + Qdrant 双库开关切换 + 多 collection 分工

#### 4.2.1 设计原则

- **统一抽象**：所有向量读写走 Spring AI 2.0 `VectorStore` 接口，不再直接调 Qdrant REST。
- **多库共存**：Milvus 与 Qdrant 可同时配置，按用途路由到不同库。
- **多 collection 分工**：明确每个 collection 存什么数据，避免混存。

#### 4.2.2 多 collection 分工设计

| collection 名 | 用途 | 维度 | 距离度量 | 存放库 | 写入方 | 读取方 |
|--------------|------|------|---------|--------|--------|--------|
| `jarvis_knowledge` | RAG 知识文档（资料） | 1536（text-embedding-v3） | Cosine | Milvus | jarvis-rag 文档导入接口 | jarvis-rag `MultiRecallRagService` |
| `jarvis_chat_memory` | 对话记忆向量化（长期记忆） | 1536 | Cosine | Milvus | jarvis-data `ChatHistoryService` | java-jarvis `MemoryRetrievalService` |
| `jarvis_user_profile` | 用户画像/偏好 | 1536 | Cosine | Milvus | jarvis-user `UserPreferenceService` | java-jarvis 个性化提示词注入 |
| `jarvis_skills_meta` | 技能元数据（语义检索技能） | 1536 | Cosine | Qdrant（保留） | jarvis-skills `SkillRegistry` | jarvis-skills `SkillService.search` |

> **设计要点**：记忆/资料/画像走 Milvus（主库，性能与规模更好），技能元数据走 Qdrant（保留现有依赖，验证双库共存）。可通过 `jarvis.vector-store.routing.*` 配置灵活调整。

#### 4.2.3 VectorStore 配置

```yaml
spring:
  ai:
    vectorstore:
      milvus:
        client:
          host: ${MILVUS_HOST:localhost}
          port: ${MILVUS_PORT:19530}
          username: ${MILVUS_USERNAME:root}
          password: ${MILVUS_PASSWORD:Milvus}
        database-name: default
        collection-name: jarvis_knowledge   # 默认 collection
        embedding-dimension: 1536
        index-type: IVF_FLAT
        metric-type: COSINE
      qdrant:
        host: ${QDRANT_HOST:localhost}
        port: ${QDRANT_PORT:6333}
        use-tls: false
        collection-name: jarvis_skills_meta

jarvis:
  vector-store:
    type: milvus                          # milvus | qdrant | both
    routing:
      knowledge: milvus                   # RAG 知识走 milvus
      chat-memory: milvus                 # 对话记忆走 milvus
      user-profile: milvus                # 用户画像走 milvus
      skills-meta: qdrant                 # 技能元数据走 qdrant
```

#### 4.2.4 多 VectorStore Bean 装配

```java
@Configuration
public class VectorStoreConfig {

    @Bean
    @ConditionalOnProperty(name = "jarvis.vector-store.routing.knowledge", havingValue = "milvus",
                           matchIfMissing = true)
    @Qualifier("knowledgeVectorStore")
    public VectorStore knowledgeVectorStore(MilvusVectorStore milvusVectorStore) {
        return milvusVectorStore;
    }

    @Bean
    @Qualifier("chatMemoryVectorStore")
    public VectorStore chatMemoryVectorStore(
            @Value("${jarvis.vector-store.routing.chat-memory:milvus}") String routing,
            MilvusVectorStore milvusVectorStore,
            @Autowired(required = false) QdrantVectorStore qdrantVectorStore) {
        return resolveStore(routing, milvusVectorStore, qdrantVectorStore);
    }

    @Bean
    @Qualifier("userProfileVectorStore")
    public VectorStore userProfileVectorStore(/* 同上 */) { ... }

    @Bean
    @Qualifier("skillsMetaVectorStore")
    public VectorStore skillsMetaVectorStore(/* 走 qdrant */) { ... }
}
```

#### 4.2.5 现有代码改造点

- `jarvis-data/VectorService.java`：标记 `@Deprecated`，内部委托给 `@Qualifier("chatMemoryVectorStore") VectorStore`，保持调用方兼容。删除 `Math.random` 占位逻辑。
- `jarvis-rag/MultiRecallRagService.java:62-69,159-202`：删除 `WebClient` 直连 Qdrant REST 的代码，改为注入 `@Qualifier("knowledgeVectorStore") VectorStore`，调用 `vectorStore.similaritySearch(SearchRequest.query(query).withTopK(10))`。
- `jarvis-rag/MultiRecallRagService.java:246-307 generateEmbedding/generateFallbackEmbedding`：删除，改用 `DashScopeEmbeddingModel`（VectorStore 内部自动调用）。
- `jarvis-data/TextEmbeddingService.java:16-31`：删除 `Random` 占位，改为委托 `DashScopeEmbeddingModel`。
- `jarvis-data/grpc/DataServiceImpl.java`：补齐 `searchSimilarChatHistory` / `searchSimilarUserPreferences` 的 `@Override` 实现，委托给 VectorStore。
- `jarvis-rag/pom.xml:79-85`：恢复或替换为 `spring-ai-qdrant-store-spring-boot-starter`。

---

### 4.3 工具调用：Spring AI 2.0 @Tool + SAA ReactAgent + 自研兼容开关

#### 4.3.1 三模式设计

```yaml
jarvis:
  tool:
    mode: hybrid                       # spring-ai | custom | hybrid
    react-loop:
      enabled: true
      max-iterations: 5
  agent:
    framework: saa-react               # saa-react | custom（自研 AgentCore）
```

- `spring-ai`：工具用 `@Tool` 注解声明，通过 `ToolCallbacks.from(toolBean)` 自动生成 `ToolCallback`，交给 `ChatClient.prompt().toolCallbacks(...)`。
- `custom`：保留现有 `InnerTool` + `ToolRegistry` + `AgentCore.convertToLlmTools/handleToolCall`（v9 实现）。
- `hybrid`（推荐默认）：自研 `InnerTool` 适配为 Spring AI `ToolCallback`，统一走 `ChatClient` 调用链。
- `agent.framework=saa-react`：使用 SAA `spring-ai-alibaba-agent-framework` 的 `ReactAgent` 替代自研 AgentCore 编排，获得原生 ReAct 循环、Hooks、Skills 能力。
- `agent.framework=custom`：保留自研 AgentCore（兼容回退）。

#### 4.3.2 Spring AI 2.0 @Tool 工具示例（新增）

```java
@Component
public class WeatherSpringAiTool {

    @Tool(description = "获取指定城市的当前天气信息")
    public String getWeather(@ToolParam(description = "城市名称") String city) {
        // 复用 WeatherTool.fetchWeatherFromApi 逻辑
        return fetchWeatherFromApi(city);
    }
}
```

#### 4.3.3 自研 InnerTool → Spring AI ToolCallback 适配器

```java
@Component
public class InnerToolAdapter {

    @Autowired
    private ToolRegistry toolRegistry;

    /**
     * 把自研 InnerTool 适配为 Spring AI 2.0 ToolCallback 列表。
     */
    public ToolCallback[] toSpringAiToolCallbacks() {
        List<ToolCallback> result = new ArrayList<>();
        for (var inner : toolRegistry.getAllTools()) {
            // 用 MethodToolCallback 包装：把 inner.getFunction() 反射为 Method
            ToolCallback callback = ToolCallbacks.fromToolMethod(new InnerToolInvocation(inner));
            result.add(callback);
        }
        return result.toArray(new ToolCallback[0]);
    }
}
```

> 适配细节：`InnerToolInvocation` 通过 `java.lang.reflect.Proxy` 动态生成带 `@Tool` 注解的方法代理，使 `ToolCallbacks.fromToolMethod` 能识别。

#### 4.3.4 SAA ReactAgent 集成（质的飞跃）

`spring-ai-alibaba-agent-framework` 提供 `ReactAgent`，具备原生 ReAct 循环、Hooks、Skills，可替代自研 AgentCore 的编排逻辑：

```java
@Configuration
@ConditionalOnProperty(name = "jarvis.agent.framework", havingValue = "saa-react",
                       matchIfMissing = true)
public class ReactAgentConfig {

    @Bean
    public ReactAgent jarvisReactAgent(
            ChatClient chatClient,
            List<Object> toolBeans,           // 所有 @Tool 注解的 Bean
            ToolRegistry toolRegistry,        // 自研工具注册中心
            InnerToolAdapter innerToolAdapter) {

        // 合并 Spring AI @Tool 工具 + 自研 InnerTool 适配工具
        ToolCallback[] springAiTools = ToolCallbacks.from(toolBeans.toArray());
        ToolCallback[] customTools = innerToolAdapter.toSpringAiToolCallbacks();
        ToolCallback[] allTools = Stream.concat(
                Arrays.stream(springAiTools), Arrays.stream(customTools))
                .distinct().toArray(ToolCallback[]::new);

        return ReactAgent.builder()
                .name("Jarvis")
                .chatClient(chatClient)
                .tools((Object[]) allTools)          // 工具自动注册
                .maxIterations(5)                     // ReAct 最大循环次数
                .build();
    }
}
```

> **关键收益**：SAA ReactAgent 自带多轮 ReAct 循环（模型返回 tool_call → 执行工具 → 结果回传模型 → 直至返回最终文本），修复了 v9 中 `handleToolCall` 单次执行不回传的缺陷（`AgentCore.java:340-381`）。同时获得 Hooks（前置/后置/错误回调）和 Skills（技能动态加载）能力。

#### 4.3.5 AgentCore 改造：双框架开关

`AgentCore.java:164-178 chatWithTools` 改造为根据开关走 ReactAgent 或自研逻辑：

```java
@Component
public class AgentCore {

    @Autowired(required = false)
    @Qualifier("jarvisReactAgent")
    private ReactAgent reactAgent;        // SAA ReactAgent（可选）

    @Value("${jarvis.agent.framework:custom}")
    private String agentFramework;

    @Value("${jarvis.tool.mode:hybrid}")
    private String toolMode;

    public String chatWithTools(String sessionId, String userInput,
                                 List<com.skyeai.jarvis.agent.tool.ToolCallback> legacyCallbacks) {
        // 开关：优先走 SAA ReactAgent
        if ("saa-react".equals(agentFramework) && reactAgent != null) {
            log.info("使用 SAA ReactAgent 处理对话 - sessionId: {}", sessionId);
            String response = reactAgent.chat(userInput);
            return response != null ? response : "";
        }

        // 回退：自研 AgentCore 逻辑（v9 实现）
        log.info("使用自研 AgentCore 处理对话 - sessionId: {}", sessionId);
        return chatWithToolsLegacy(sessionId, userInput, legacyCallbacks);
    }

    // 原有自研逻辑保留，重命名为 chatWithToolsLegacy
    private String chatWithToolsLegacy(String sessionId, String userInput,
                                        List<com.skyeai.jarvis.agent.tool.ToolCallback> legacyCallbacks) {
        // ... v9 的 AgentCore.chatWithTools 原逻辑 ...
    }
}
```

#### 4.3.6 MCP 改造

`McpClientManager.java:140,146` / `McpServer.java:44-51` 桩实现替换为 Spring AI 2.0 的 MCP 客户端支持：

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mcp-client-spring-boot-starter</artifactId>
</dependency>
```

通过 `spring.ai.mcp.client.servers.*` 配置外部 MCP server，自动发现工具并注册为 Spring AI `ToolCallback`。SAA 的 `spring-ai-alibaba-graph-core` 也支持 MCP 节点。

---

### 4.4 Text-to-SQL：SAA DataAgent（首选）+ SuperSQL 思路（备选）

#### 4.4.1 现状问题

- `TextToSqlService.java:43-60` 关键字硬编码。
- `TextToSqlController.java:39-42 POST /execute` SQL 注入风险。
- `FunctionCallService.java:194-200` 自定义 `@Tool` 注解无 execute 能力。
- `application.yml:103-109` scan-packages 指向不存在的包。

#### 4.4.2 SAA DataAgent 集成（首选方案，质的飞跃）

> **DataAgent** 是 Spring AI Alibaba 官方生态项目（[github.com/spring-ai-alibaba/dataagent](https://github.com/spring-ai-alibaba/dataagent)），基于 SAA 构建的虚拟 AI 数据分析师。它能像专家一样思考、规划、纠错，输出带图表、带逻辑的分析结果，远超传统 Text-to-SQL 的指令翻译器模式。

DataAgent 核心能力：
- **Schema 感知**：自动拉取数据库表结构作为上下文
- **智能体架构**：确定性流程 + 模型推理结合，支持多步纠错
- **结果可视化**：输出带图表的分析报告

```yaml
jarvis:
  text-to-sql:
    engine: dataagent               # dataagent | supersql | legacy
    dataagent:
      datasource:
        url: ${DB_URL:jdbc:postgresql://localhost:5432/jarvis}
        username: ${DB_USERNAME:jarvis}
        password: ${DB_PASSWORD:jarvis}
      safety:
        select-only: true           # 强制只读
        max-rows: 1000              # 结果集上限
```

集成方式（引入 DataAgent 依赖后配置数据源即可）：

```java
@Configuration
@ConditionalOnProperty(name = "jarvis.text-to-sql.engine", havingValue = "dataagent",
                       matchIfMissing = true)
public class DataAgentConfig {

    @Bean
    public DataAgent jarvisDataAgent(ChatClient chatClient, DataSource dataSource) {
        return DataAgent.builder()
                .chatClient(chatClient)
                .dataSource(dataSource)
                .selectOnly(true)                    // 强制只读安全
                .maxRows(1000)                       // 结果集上限
                .build();
    }
}
```

#### 4.4.3 SuperSQL 思路备选方案

> 若 DataAgent 在 M1.1 版本不稳定，可回退至 SuperSQL 思路自研实现。**SuperSQL** 是腾讯开源的 Text-to-SQL 引擎（GitHub: Tencent/supersql），支持 Schema 感知、多方言、SQL 校验。

核心思路：
1. **Schema 感知**：从 `information_schema` 拉取表结构（复用 `TextToSqlService.java:114-131 getDatabaseSchema`），组装为 Prompt 上下文。
2. **LLM 生成 SQL**：通过 Spring AI 2.0 `ChatClient` + function call。
3. **SQL 校验白名单**：执行前用 `JSqlParser` 解析，只允许 `SELECT`，禁止 DDL/DML。
4. **参数化执行**：用 `NamedParameterJdbcTemplate` 而非字符串拼接。

```java
@Service
@ConditionalOnProperty(name = "jarvis.text-to-sql.engine", havingValue = "supersql")
public class SuperSqlTextToSqlService {

    private final ChatClient chatClient;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DatabaseSchemaProvider schemaProvider;

    @Tool(description = "根据自然语言生成 SQL 并执行查询，仅支持只读 SELECT 语句")
    public SqlExecutionResult naturalLanguageQuery(
            @ToolParam(description = "用户的自然语言查询") String question) {
        // 1. 拉取 schema
        String schema = schemaProvider.getSchemaAsMarkdown();

        // 2. 调 LLM 生成 SQL（function call）
        String sql = chatClient.prompt()
                .system(buildTextToSqlSystemPrompt(schema))
                .user(question)
                .call()
                .content();

        // 3. 安全校验：只允许 SELECT
        if (!SqlSafetyValidator.isSelectOnly(sql)) {
            throw new IllegalStateException("仅允许执行 SELECT 语句，拒绝执行: " + sql);
        }

        // 4. 参数化执行
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, new HashMap<>());
        return new SqlExecutionResult(sql, rows);
    }
}
```

#### 4.4.4 SQL 安全校验器（新增，通用）

```java
public class SqlSafetyValidator {
    private static final Pattern FORBIDDEN = Pattern.compile(
        "\\b(INSERT|UPDATE|DELETE|DROP|ALTER|CREATE|TRUNCATE|GRANT|REVOKE|MERGE)\\b",
        Pattern.CASE_INSENSITIVE);

    public static boolean isSelectOnly(String sql) {
        if (sql == null || sql.isBlank()) return false;
        if (FORBIDDEN.matcher(sql).find()) return false;
        // 用 JSqlParser 解析确认首语句为 SELECT
        try {
            Statement stmt = CCJSqlParserUtil.parse(sql);
            return stmt instanceof Select;
        } catch (Exception e) {
            return false;
        }
    }
}
```

#### 4.4.5 配置开关

```yaml
jarvis:
  text-to-sql:
    engine: dataagent       # dataagent | supersql | legacy（保留关键字硬编码）
    safety:
      select-only: true     # 强制只读
      max-rows: 1000        # 结果集上限
```

`TextToSqlController.java:39-42 POST /execute` 必须改为只接受自然语言，**移除直接执行 SQL 的端点**（或加管理员鉴权 + 强制走 `SqlSafetyValidator`）。

---

### 4.5 Advisor：日志/敏感词拦截器 + SAA Graph Observation

#### 4.5.1 Advisor 链设计

```yaml
jarvis:
  advisor:
    order:
      logging: 1          # 最外层记录全链路
      sensitive-input: 2  # 输入敏感词拦截
      memory: 3           # ChatMemory 注入
      rag: 4              # RAG 上下文注入（QuestionAnswerAdvisor）
      sensitive-output: 5 # 输出敏感词拦截
    enable:
      logging: true
      sensitive-input: true
      memory: true
      rag: true
      sensitive-output: true
      safeguard: false     # SafeGuardAdvisor 可选，与 sensitive-input 互补
  observation:
    graph-enabled: true    # SAA Graph 可观测性（Micrometer/OpenTelemetry）
```

#### 4.5.2 LoggingAdvisor（新增）

```java
@Component
public class LoggingAdvisor implements CallAroundAdvisor {

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        long start = System.currentTimeMillis();
        String sessionId = advisedRequest.chatParams() == null ? "-" :
                advisedRequest.chatParams().getOrDefault("sessionId", "-").toString();

        try {
            AdvisedResponse response = chain.nextAroundCall(advisedRequest);
            long duration = System.currentTimeMillis() - start;
            // 结构化 JSON 日志输出到 stdout
            log.info("{\"event\":\"chat\",\"session\":\"{}\",\"duration\":{},\"tokens\":{}}",
                    sessionId, duration, response.response().getMetadata().getUsage());
            return response;
        } catch (Exception e) {
            log.error("{\"event\":\"chat_error\",\"session\":\"{}\",\"error\":\"{}\"}",
                    sessionId, e.getMessage());
            throw e;
        }
    }

    @Override
    public String getName() { return "LoggingAdvisor"; }

    @Override
    public int getOrder() { return 1; }
}
```

#### 4.5.3 SensitiveWordAdvisor（新增，适配现有 ContentSecurityFilter）

把 `ContentSecurityFilter.java:46-48` 占位敏感词改为外置词库，并接入 Advisor 链：

```java
@Component
public class SensitiveWordAdvisor implements CallAroundAdvisor {

    private final ContentSecurityFilter filter;  // 复用现有类

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        // 输入拦截
        SecurityResult inputCheck = filter.filterInput(advisedRequest.userText());
        if (inputCheck.isBlocked()) {
            throw new ContentBlockedException("输入被拦截: " + inputCheck.getReason());
        }

        AdvisedResponse response = chain.nextAroundCall(advisedRequest);

        // 输出拦截
        String outputText = response.response().getResult().getOutput().getText();
        SecurityResult outputCheck = filter.filterOutput(outputText);
        if (outputCheck.isBlocked()) {
            // 替换为脱敏文本而非抛异常，避免用户体验断裂
            return replaceOutput(response, filter.maskWord(outputText));
        }
        return response;
    }
}
```

#### 4.5.4 ContentSecurityFilter 改造

- `:46-48` 占位敏感词改为从 `classpath:sensitive-words.txt` 加载（每行一个词，支持注释）。
- `:28-31` 过宽正则"政治""敏感"等词移除或改为精确匹配。
- 新增 `@PostConstruct` 加载词库，词库不存在时**抛异常而非静默空集**（符合用户偏好：缺失配置显式报错）。

#### 4.5.5 Spring AI 2.0 原生 SafeGuardAdvisor

可选引入 Spring AI 2.0 内置的 `SafeGuardAdvisor`，与自研 `SensitiveWordAdvisor` 互补：

```java
@Bean
@ConditionalOnProperty(name = "jarvis.advisor.enable.safeguard", havingValue = true)
public SafeGuardAdvisor safeGuardAdvisor() {
    return new SafeGuardAdvisor(List.of("敏感词A", "敏感词B"));
}
```

#### 4.5.6 QuestionAnswerAdvisor（RAG 注入）

```java
@Bean
public QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore knowledgeVectorStore) {
    return new QuestionAnswerAdvisor(
            chatClient,
            SearchRequest.query("").withTopK(5),
            """
            以下是从知识库检索到的参考资料：
            {question_answer_context}
            请结合参考资料回答用户问题。
            """);
}
```

#### 4.5.7 MessageChatMemoryAdvisor（记忆注入）

```java
@Bean
public MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory springAiChatMemory) {
    return new MessageChatMemoryAdvisor(springAiChatMemory);
}
```

> 注意：Spring AI 2.0 的 `ChatMemory` 接口与项目自研 `com.skyeai.jarvis.agent.ChatMemory` 同名，需通过包名区分。建议把自研类重命名为 `JarvisChatMemory`，避免混淆。

#### 4.5.8 SAA Graph Observation（可观测性增强）

引入 `spring-ai-alibaba-starter-graph-observation`，自动为 Graph 工作流注入 Micrometer/OpenTelemetry 链路追踪：

```yaml
spring:
  ai:
    alibaba:
      graph:
        observation:
          enabled: true    # 启用 Graph 节点级追踪
```

#### 4.5.9 AOP 监控改造

`AgentMonitoringAspect.java:76-77` 纯文本日志改为结构化 JSON，`:115-118 recordMetric` 改为 `MeterRegistry`：

```java
@Autowired private MeterRegistry meterRegistry;

private void recordMetric(String metricName, long value) {
    meterRegistry.timer("jarvis.agent.chat.duration").record(value, TimeUnit.MILLISECONDS);
    meterRegistry.counter("jarvis.agent.chat.calls").increment();
}
```

`java-jarvis/pom.xml` 新增 `micrometer-registry-prometheus`，`application.properties` 新增：

```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.metrics.export.prometheus.enabled=true
```

---

### 4.6 系统/用户提示词：PromptTemplate + SAA Nacos 热更新

#### 4.6.1 模板文件目录

```
java-jarvis/src/main/resources/prompts/
├── system/
│   ├── jarvis.st              # 默认 J.A.R.V.I.S. 人设
│   ├── expert.st              # 专家模式
│   ├── creative.st            # 创意模式
│   ├── technical.st           # 技术模式
│   └── subagent-default.st    # 子代理默认提示
├── user/
│   ├── rag-enhanced.st        # RAG 上下文增强模板
│   ├── memory-enhanced.st     # 历史记忆注入模板
│   └── tool-summary.st        # 工具结果总结模板
└── text-to-sql/
    └── system.st              # Text2SQL 系统提示（含 schema 占位符）
```

#### 4.6.2 模板示例 `prompts/system/jarvis.st`

```st
你是由 Stark Industries 开发的高级人工智能助手 J.A.R.V.I.S.（Just A Rather Very Intelligent System）。
你具备以下能力：
- 调用内部工具获取实时数据（天气、股票、搜索等）
- 检索知识库回答专业问题
- 记忆历史对话上下文
请用中文回答，保持简洁专业。
```

#### 4.6.3 PromptService 改造（三来源开关）

`PromptServiceImpl.java:14-23` 内存 HashMap 硬编码改为基于 `PromptTemplate` 加载，支持 file / nacos / legacy 三种来源：

```java
@Service
public class PromptServiceImpl implements PromptService {

    private final ResourceLoader resourceLoader;
    private final Map<String, PromptTemplate> systemTemplates = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        loadTemplate("jarvis", "classpath:prompts/system/jarvis.st");
        loadTemplate("expert", "classpath:prompts/system/expert.st");
        // ... 其他模板
    }

    private void loadTemplate(String name, String location) {
        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            // 缺失配置显式报错
            throw new IllegalStateException("提示词模板不存在: " + location);
        }
        try {
            systemTemplates.put(name, new PromptTemplate(resource));
        } catch (IOException e) {
            throw new IllegalStateException("加载提示词模板失败: " + location, e);
        }
    }

    @Override
    public String getSystemPrompt(String type) {
        PromptTemplate template = systemTemplates.get(type);
        if (template == null) {
            throw new IllegalArgumentException("未知的提示词类型: " + type);
        }
        return template.getRenderer().apply(Map.of());
    }

    @Override
    public String renderUserPrompt(String templateName, Map<String, Object> variables) {
        PromptTemplate template = userTemplates.get(templateName);
        return template.render(variables);
    }
}
```

#### 4.6.4 SAA Nacos 模型热更新

引入 `spring-ai-alibaba-starter-config-nacos`，实现模型配置和提示词的动态热更新（无需重启）：

```yaml
spring:
  ai:
    alibaba:
      config:
        nacos:
          server-addr: ${NACOS_ADDR:localhost:8848}
          data-id: jarvis-ai-config
          group: JARVIS_GROUP
          # 支持模型参数热更新（model/temperature/max-tokens 等）
          # 支持提示词内容热更新
```

#### 4.6.5 配置开关

```yaml
jarvis:
  prompt:
    source: file                # file | nacos | legacy
    nacos:
      data-id: jarvis-prompts
      group: JARVIS_GROUP
    default-system-prompt: jarvis
```

- `file`：从 classpath 加载 .st 文件。
- `nacos`：从 Nacos 配置中心动态加载（支持热更新，配合 SAA starter-config-nacos）。
- `legacy`：保留原 `@Value("${ai.system.prompt}")` 硬编码方式（兼容老配置）。

#### 4.6.6 现有硬编码改造点

| 文件:行号 | 改造 |
|----------|------|
| `AgentCore.java:89-90` | 改为注入 `PromptService`，按 `jarvis.prompt.default-system-prompt` 选择模板 |
| `AgentCore.java:229` | 删除方法内局部硬编码，统一用字段 |
| `ChatController.java:256,262,310` | 改为 `PromptService.getSystemPrompt("jarvis")` |
| `AliyunAIServiceImpl.java:36-37` / `OpenAiLlmServiceImpl.java:149-150` | 同上 |
| `LlmController.java:25` / `AgentController.java:26,39` | 同上 |
| `ReactServiceImpl.java:251` / `MultimodalFusionServiceImpl.java:321` | 同上 |
| `SubAgent.java:34,77-82` | 改为注入 PromptService，按子代理类型选择模板 |
| `application.properties:27` + `jarvis-llm/application.yml:81-86` | 删除重复定义，统一到 `prompts/system/jarvis.st` |

---

### 4.7 ChatMemory 落库向量库及历史记忆检索

#### 4.7.1 三级存储架构

| 层级 | 存储 | 用途 | TTL | 实现 |
|------|------|------|-----|------|
| 工作记忆 | 内存（最近 N 轮） | 当前对话上下文 | 会话级 | 现有 `ChatMemory.history`（保留） |
| 短期记忆 | Redis | 会话历史持久化、跨实例恢复 | 24h | 现有 `PersistentChatMemory`（保留） |
| 长期记忆 | Milvus `jarvis_chat_memory` collection | 跨会话历史记忆检索 | 永久 | **新增** `VectorMemoryStore` |

#### 4.7.2 实现 Spring AI 2.0 ChatMemory 接口（重命名）

把 `com.skyeai.jarvis.agent.ChatMemory` 重命名为 `JarvisChatMemory`，并实现 Spring AI 2.0 接口：

```java
@Component
public class SpringAiChatMemory implements org.springframework.ai.chat.memory.ChatMemory {

    private final JarvisChatMemory jarvisMemory;          // 现有三层压缩逻辑
    private final PersistentChatMemory persistentMemory;  // Redis 持久化
    private final VectorMemoryStore vectorMemoryStore;    // 新增：向量库长期记忆

    @Override
    public void add(String conversationId, List<Message> messages) {
        // 1. 写工作记忆（触发三层压缩）
        messages.forEach(msg -> jarvisMemory.addMessage(convertMsg(msg)));

        // 2. 写 Redis 短期记忆
        messages.forEach(msg -> persistentMemory.addMessage(conversationId, convertMsg(msg)));

        // 3. 写向量库长期记忆（每条消息向量化后存入）
        for (Message msg : messages) {
            if (msg instanceof AssistantMessage || msg instanceof UserMessage) {
                vectorMemoryStore.store(conversationId, msg);
            }
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        // 优先从工作记忆取，回退 Redis
        return jarvisMemory.getMessages().stream()
                .map(this::toSpringAiMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        jarvisMemory.clear();
        persistentMemory.clear(conversationId);
        // 注意：向量库长期记忆不随会话清除，保留为永久记忆
    }
}
```

#### 4.7.3 VectorMemoryStore（新增）

```java
@Component
public class VectorMemoryStore {

    @Qualifier("chatMemoryVectorStore")
    private final VectorStore vectorStore;

    public void store(String sessionId, Message message) {
        String text = message.getText();
        if (text == null || text.isBlank()) return;

        Document doc = Document.builder()
                .id(UUID.randomUUID().toString())
                .text(text)
                .metadata(Map.of(
                        "sessionId", sessionId,
                        "role", message.getMessageType().name(),
                        "timestamp", System.currentTimeMillis()
                ))
                .build();
        vectorStore.add(List.of(doc));   // VectorStore 内部自动调 EmbeddingModel 向量化
    }

    public List<Document> retrieveRelevant(String sessionId, String query, int topK) {
        SearchRequest request = SearchRequest.query(query)
                .withTopK(topK)
                .withFilterExpression("sessionId == '" + sessionId + "'");  // 可选：限定当前会话
        return vectorStore.similaritySearch(request);
    }
}
```

#### 4.7.4 历史记忆检索：注入当前 Prompt

在 `AgentCore.chat` 流程中，每次用户提问时检索相关历史记忆：

```java
public String chat(String sessionId, String userInput) {
    JarvisChatMemory memory = memoryManager.getOrCreateMemory(sessionId);

    // 历史记忆检索（新增）
    if (memoryRetrievalEnabled) {
        List<Document> historyDocs = vectorMemoryStore.retrieveRelevant(sessionId, userInput, 5);
        if (!historyDocs.isEmpty()) {
            String historyContext = formatHistoryContext(historyDocs);
            String enrichedInput = promptService.renderUserPrompt("memory-enhanced",
                    Map.of("userInput", userInput, "historyContext", historyContext));
            memory.addMessage(new UserMessage(enrichedInput));
        } else {
            memory.addMessage(new UserMessage(userInput));
        }
    } else {
        memory.addMessage(new UserMessage(userInput));
    }

    // ... 后续流程
}
```

#### 4.7.5 配置开关

```yaml
jarvis:
  memory:
    mode: hybrid                 # memory | redis | vector | hybrid
    retrieval:
      enabled: true              # 是否启用历史记忆检索
      top-k: 5                   # 检索条数
      score-threshold: 0.7       # 相似度阈值
      session-scope: false       # true=仅当前会话，false=跨会话全局检索
    compress:
      enabled: true              # 保留三层压缩
      threshold-messages: 20
      preserve-recent-messages: 5
      max-messages: 50
```

- `memory`：仅内存（v9 行为，兼容）。
- `redis`：内存 + Redis（v9-3 已实现）。
- `vector`：内存 + 向量库（新增）。
- `hybrid`（推荐）：内存 + Redis + 向量库三级联动。

#### 4.7.6 现有代码改造点

- `jarvis-data/ChatHistoryService.java:48-50,56-75 saveToVectorDatabase`：删除调用 `vectorService.addChatHistoryVector`（内存占位），改为通过 gRPC 调用 `SpringAiChatMemory.add()`，由 java-jarvis 统一写向量库。
- `jarvis-data/TextEmbeddingService.java:16-31`：删除 `Random` 占位，改为委托 `DashScopeEmbeddingModel`（通过 gRPC 或直接注入）。
- `jarvis-data/grpc/DataServiceImpl.java`：补齐 `searchSimilarChatHistory` 实现，委托给 `VectorMemoryStore.retrieveRelevant`。

---

### 4.8 SAA Studio 调试 UI（附加能力）

引入 `spring-ai-alibaba-studio`，获得嵌入式 Agent 调试与可视化 UI，便于开发期排查问题：

```yaml
spring:
  ai:
    alibaba:
      studio:
        enabled: true           # 开发环境启用，生产关闭
        port: 8088              # Studio UI 端口
```

> Studio 提供：Agent 执行链路可视化、工具调用追踪、Prompt 调试、Memory 查看。与 SAA Graph Observation 配合，实现全链路可观测。

---

### 4.9 配置开关与回退策略

#### 4.9.1 全局开关总表

| 开关路径 | 可选值 | 默认值 | 失败回退 |
|---------|--------|--------|----------|
| `jarvis.llm.impl` | `spring-ai` / `legacy` | `spring-ai` | spring-ai 失败回退 legacy |
| `jarvis.vector-store.type` | `milvus` / `qdrant` / `both` | `milvus` | 主库失败回退备库 |
| `jarvis.vector-store.routing.*` | `milvus` / `qdrant` | 见 4.2.2 | 不回退，显式报错 |
| `jarvis.tool.mode` | `spring-ai` / `custom` / `hybrid` | `hybrid` | spring-ai 工具加载失败回退 custom |
| `jarvis.tool.react-loop.enabled` | `true` / `false` | `true` | 关闭后退化为单次工具调用 |
| `jarvis.agent.framework` | `saa-react` / `custom` | `saa-react` | ReactAgent 不可用回退自研 AgentCore |
| `jarvis.text-to-sql.engine` | `dataagent` / `supersql` / `legacy` | `dataagent` | DataAgent 不可用回退 supersql |
| `jarvis.text-to-sql.safety.select-only` | `true` / `false` | `true` | 校验失败抛异常不执行 |
| `jarvis.advisor.enable.*` | `true` / `false` | 全 `true` | 单个 Advisor 失败跳过该层 |
| `jarvis.observation.graph-enabled` | `true` / `false` | `true` | 失败降级为普通 Micrometer |
| `jarvis.prompt.source` | `file` / `nacos` / `legacy` | `file` | file 不存在抛异常；nacos 不可达回退 file |
| `jarvis.memory.mode` | `memory` / `redis` / `vector` / `hybrid` | `hybrid` | 向量库不可达回退 redis |
| `jarvis.memory.retrieval.enabled` | `true` / `false` | `true` | 检索失败降级为不注入历史 |
| `jarvis.memory.retrieval.session-scope` | `true` / `false` | `false` | 跨会话检索 |
| `spring.ai.alibaba.studio.enabled` | `true` / `false` | `false`（生产） | 不影响业务 |

#### 4.9.2 错误处理原则（遵循用户偏好）

- **缺失配置显式报错**：向量库连接失败、提示词模板缺失、敏感词词库不存在、API Key 缺失等情况，**抛异常 + ERROR 日志**，不静默降级为空实现。
- **降级需显式配置**：仅在开关中明确配置了回退策略时才降级（如 `jarvis.llm.impl=legacy` 时才允许 spring-ai 失败后回退）。
- **日志可见性**：所有开关切换、降级触发、Advisor 拦截、向量库路由决策均输出 `INFO` 级日志到 stdout（符合用户偏好：服务器环境可见）。

---

## 5. 全局开关设计总表

### 5.1 完整 application.yml 模板

```yaml
jarvis:
  # ============ LLM 实现切换 ============
  llm:
    impl: spring-ai                    # spring-ai | legacy

  # ============ 向量库切换与路由 ============
  vector-store:
    type: milvus                       # milvus | qdrant | both
    routing:
      knowledge: milvus
      chat-memory: milvus
      user-profile: milvus
      skills-meta: qdrant

  # ============ 工具调用切换 ============
  tool:
    mode: hybrid                       # spring-ai | custom | hybrid
    react-loop:
      enabled: true
      max-iterations: 5

  # ============ Agent 框架切换 ============
  agent:
    framework: saa-react               # saa-react | custom

  # ============ Text-to-SQL 切换 ============
  text-to-sql:
    engine: dataagent                  # dataagent | supersql | legacy
    safety:
      select-only: true
      max-rows: 1000

  # ============ Advisor 链 ============
  advisor:
    enable:
      logging: true
      sensitive-input: true
      memory: true
      rag: true
      sensitive-output: true
      safeguard: false
    order:
      logging: 1
      sensitive-input: 2
      memory: 3
      rag: 4
      sensitive-output: 5

  # ============ 可观测性 ============
  observation:
    graph-enabled: true                # SAA Graph Observation

  # ============ 提示词管理 ============
  prompt:
    source: file                       # file | nacos | legacy
    nacos:
      data-id: jarvis-prompts
      group: JARVIS_GROUP
    default-system-prompt: jarvis

  # ============ 记忆系统 ============
  memory:
    mode: hybrid                       # memory | redis | vector | hybrid
    retrieval:
      enabled: true
      top-k: 5
      score-threshold: 0.7
      session-scope: false
    compress:
      enabled: true
      threshold-messages: 20
      preserve-recent-messages: 5
      max-messages: 50

spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
      chat:
        options:
          model: qwen-max
          temperature: 0.7
      embedding:
        options:
          model: text-embedding-v3
    vectorstore:
      milvus:
        client:
          host: ${MILVUS_HOST:localhost}
          port: ${MILVUS_PORT:19530}
        collection-name: jarvis_knowledge
        embedding-dimension: 1536
        metric-type: COSINE
      qdrant:
        host: ${QDRANT_HOST:localhost}
        port: ${QDRANT_PORT:6333}
        collection-name: jarvis_skills_meta
    alibaba:
      studio:
        enabled: false                 # 生产关闭，开发开启
        port: 8088
      config:
        nacos:
          server-addr: ${NACOS_ADDR:localhost:8848}
          data-id: jarvis-ai-config
          group: JARVIS_GROUP
      graph:
        observation:
          enabled: true
```

### 5.2 开关兼容性矩阵

| 现有组件 | v10 保留方式 | 默认开关 | 备注 |
|---------|-------------|---------|------|
| 自研 `InnerTool` + `ToolRegistry` | 适配为 Spring AI ToolCallback | `jarvis.tool.mode=hybrid` | 代码不删除，加适配器 |
| 自研 `AgentCore` | 与 SAA ReactAgent 并存 | `jarvis.agent.framework=custom` | saa-react 不可用时回退 |
| 自研 `ChatMemory`（重命名为 `JarvisChatMemory`） | 作为 `SpringAiChatMemory` 的内层 | `jarvis.memory.mode=hybrid` | 三层压缩逻辑保留 |
| `PersistentChatMemory`（Redis） | 作为 `SpringAiChatMemory` 的持久化层 | `jarvis.memory.mode=hybrid` | 不重写 |
| Qdrant VectorStore | 与 Milvus 并存 | `jarvis.vector-store.routing.skills-meta=qdrant` | 双库共存 |
| `ContentSecurityFilter` | 适配为 `SensitiveWordAdvisor` 内部依赖 | `jarvis.advisor.enable.sensitive-input=true` | 词库外置 |
| `AgentMonitoringAspect` | 保留 AOP，新增 Micrometer | 始终启用 | 与 LoggingAdvisor 互补 |
| `OpenAiLlmServiceImpl`（legacy） | 通过 `@ConditionalOnProperty` 保留 | `jarvis.llm.impl=legacy` 时激活 | 兼容老配置 |
| `TextToSqlService`（legacy 关键字） | 通过 `@ConditionalOnProperty` 保留 | `jarvis.text-to-sql.engine=legacy` 时激活 | 兼容老配置 |
| `PromptServiceImpl`（legacy HashMap） | 通过 `@ConditionalOnProperty` 保留 | `jarvis.prompt.source=legacy` 时激活 | 兼容老配置 |

---

## 6. 实施路线图

### Phase 0：Spring Boot 4.0 基线升级（1-2 周）

**目标**：完成 Spring Boot 3.3.5 → 4.0.x 大版本升级，代码能编译启动。

| 任务 | 模块 | 产出 |
|------|------|------|
| 0.1 升级 `spring-boot-starter-parent` 至 4.0.x | 所有模块 pom.xml | Spring Boot 4 基线 |
| 0.2 Java 版本确认（17 最低 / 21 推荐） | 所有模块 | JDK 版本统一 |
| 0.3 排查 Undertow 依赖，切换 Tomcat | 全局 | Servlet 6.1 兼容 |
| 0.4 `javax.*` → `jakarta.*` 全量排查替换 | 全局 | Jakarta EE 11 兼容 |
| 0.5 Spring Cloud 升级至 2025.x | java-jarvis 等 | Gateway/Nacos 兼容 |
| 0.6 验证 Nacos/Redis/gRPC SDK 兼容性 | 全局 | 第三方依赖可用 |
| 0.7 清理 `application.properties:15-17` 死配置 | java-jarvis | 配置与依赖一致 |

**验收**：`mvn compile` 通过；所有模块能启动（即使部分功能未接通）；无 Undertow/Servlet 报错。

### Phase 1：Spring AI 2.0 + SAA 2.0 依赖引入（1 周）

**目标**：引入 Spring AI 2.0 + Spring AI Alibaba 2.0.0-M1.1 依赖，DashScope 可用。

| 任务 | 模块 | 产出 |
|------|------|------|
| 1.1 引入 spring-ai-bom 2.0.0 + spring-ai-alibaba-bom 2.0.0-M1.1 + extensions-bom | 7 个核心模块 pom.xml | 依赖统一管理 |
| 1.2 引入 spring-ai-alibaba-starter-dashscope | jarvis-llm | DashScopeChatModel + EmbeddingModel 可用 |
| 1.3 引入 spring-ai-alibaba-agent-framework + graph-core + studio | java-jarvis | ReactAgent + Graph + Studio 就位 |
| 1.4 引入 spring-ai-milvus-store + micrometer-prometheus + logstash-encoder | jarvis-rag, jarvis-data, java-jarvis | Milvus + Prometheus + JSON 日志依赖就位 |
| 1.5 引入 spring-ai-alibaba-starter-config-nacos + graph-observation | java-jarvis | Nacos 热更新 + 可观测性就位 |
| 1.6 补齐 jarvis-advisor/pom.xml micrometer 依赖 | jarvis-advisor | prometheus 端点可用 |

**验收**：`mvn compile` 通过；`DashScopeChatModel` Bean 可注入；`DashScopeEmbeddingModel` 可生成嵌入。

### Phase 2：向量库与嵌入打通（1 周）

**目标**：Milvus + Qdrant 双库可用，嵌入真实可用。

| 任务 | 模块 | 产出 |
|------|------|------|
| 2.1 配置 Milvus docker-compose 服务 | docker-compose.yml | Milvus 19530 端口可用 |
| 2.2 实现 `VectorStoreConfig` 多 Bean 装配 | java-jarvis | 4 个限定名 VectorStore Bean |
| 2.3 创建 4 个 collection（DDL 脚本） | docker/init.sql 或代码 `init()` | knowledge/chat_memory/user_profile/skills_meta |
| 2.4 `TextEmbeddingService` 改为委托 DashScopeEmbeddingModel | jarvis-data | 嵌入真实可用 |
| 2.5 `MultiRecallRagService` 改为注入 VectorStore | jarvis-rag | 删除 WebClient 直连 |
| 2.6 `VectorService` 标记 @Deprecated 并委托 | jarvis-data | 兼容老调用方 |
| 2.7 `DataServiceImpl` 补齐 searchSimilar* 方法 | jarvis-data | gRPC 检索可用 |

**验收**：写入文档 → Milvus 可检索；嵌入维度 1536；`MultiRecallRagService.query` 返回真实结果。

### Phase 3：工具调用 + ReactAgent + Text-to-SQL（1-2 周）

**目标**：Spring AI @Tool + SAA ReactAgent + DataAgent 可用。

| 任务 | 模块 | 产出 |
|------|------|------|
| 3.1 `ReactAgentConfig` 实现 SAA ReactAgent Bean | java-jarvis | 原生 ReAct 多轮循环 |
| 3.2 `AgentCore` 改造为双框架开关 | java-jarvis | saa-react / custom 切换 |
| 3.3 `InnerToolAdapter` 实现 | java-jarvis | 自研工具适配为 Spring AI ToolCallback |
| 3.4 新增 `@Tool` 工具（搜索、计算器、日期等） | java-jarvis | 工具数量扩充 |
| 3.5 DataAgent 集成（首选） | jarvis-sql | 官方 text-to-sql 智能体 |
| 3.6 `SuperSqlTextToSqlService` 备选实现 | jarvis-sql | SuperSQL 思路 + function call |
| 3.7 `SqlSafetyValidator` 实现 | jarvis-sql | 只读 SELECT 强制校验 |
| 3.8 `TextToSqlController` 移除直接执行端点 | jarvis-sql | 消除 SQL 注入风险 |
| 3.9 MCP 客户端接入 spring-ai-mcp-client | java-jarvis | 替换桩实现 |

**验收**：ReactAgent 支持多轮 ReAct；DataAgent 可自然语言查库；Text2SQL 仅执行 SELECT；工具数量 ≥ 5。

### Phase 4：Advisor + 提示词 + 可观测性（1 周）

**目标**：Advisor 链就位，提示词集中管理，Prometheus 可用。

| 任务 | 模块 | 产出 |
|------|------|------|
| 4.1 `LoggingAdvisor` 实现 | java-jarvis | 结构化 JSON 日志 |
| 4.2 `SensitiveWordAdvisor` 实现 + ContentSecurityFilter 改造 | java-jarvis | 词库外置，接入主链路 |
| 4.3 `QuestionAnswerAdvisor` + `MessageChatMemoryAdvisor` 装配 | java-jarvis | RAG + 记忆自动注入 |
| 4.4 SAA Graph Observation 启用 | java-jarvis | Graph 节点级追踪 |
| 4.5 `prompts/` 目录 + .st 模板文件 | java-jarvis | 集中管理 |
| 4.6 `PromptServiceImpl` 改造为 PromptTemplate + Nacos 热更新 | jarvis-llm | 支持 file/nacos/legacy |
| 4.7 10+ 处硬编码系统提示词迁移 | 多模块 | 统一到 PromptService |
| 4.8 `AgentMonitoringAspect` 接入 Micrometer | java-jarvis | Prometheus 端点可用 |
| 4.9 SAA Studio 调试 UI 启用（开发环境） | java-jarvis | Agent 执行链路可视化 |

**验收**：Advisor 链按 order 执行；敏感词命中即拦截；Prometheus 端点 `/actuator/prometheus` 可访问；提示词修改无需改代码；Studio 可查看 Agent 链路。

### Phase 5：ChatMemory 落库与历史记忆检索（1 周）

**目标**：长期记忆向量化，历史检索可用。

| 任务 | 模块 | 产出 |
|------|------|------|
| 5.1 自研 `ChatMemory` 重命名为 `JarvisChatMemory` | java-jarvis | 避免与 Spring AI 同名混淆 |
| 5.2 `SpringAiChatMemory` 实现 Spring AI 2.0 接口 | java-jarvis | 三级存储联动 |
| 5.3 `VectorMemoryStore` 实现 | java-jarvis | 向量库长期记忆写入 |
| 5.4 `AgentCore.chat` 接入历史记忆检索 | java-jarvis | 用户问题向量化检索历史 |
| 5.5 `memory-enhanced.st` 提示词模板 | java-jarvis | 历史上下文注入模板 |
| 5.6 `ChatHistoryService` gRPC 调用统一写向量库 | jarvis-data | 删除内存占位 |

**验收**：跨会话提问能召回历史上下文；相似度阈值可配；`jarvis.memory.mode=memory` 可回退到纯内存。

### Phase 6：端到端集成与验收（1 周）

**目标**：全链路可用，开关切换验证。

| 任务 | 产出 |
|------|------|
| 6.1 全链路冒烟测试（对话 + RAG + 工具 + 记忆 + Text2SQL） | 端到端通过 |
| 6.2 开关切换测试（每个开关的所有值组合） | 切换无异常 |
| 6.3 性能基准（P95 延迟、向量检索延迟） | 性能报告 |
| 6.4 SAA Studio 全链路调试验证 | 可视化追踪 |
| 6.5 文档更新（README、API 文档、部署指南） | 文档完善 |
| 6.6 Bug 修复与优化 | 零 P0/P1 bug |

---

## 7. 风险与验收

### 7.1 技术风险

| 风险 | 概率 | 影响 | 应对 |
|------|------|------|------|
| **Spring Boot 3→4 跨大版本升级** | 高 | 编译/运行时错误 | Phase 0 单独验证；逐模块升级；先空项目启动再迁移业务 |
| **SAA 2.0.0-M1.1 里程碑版 API 不稳定** | 中 | Bean 装配失败 / API 变更 | 锁定 M1.1 版本；关键路径保留自研 legacy 开关回退；关注 SAA Release Notes |
| **Spring AI 2.0 不兼容 Spring Boot 3** | 高 | 无法降级 | 严格遵循 Spring Boot 4 + Spring AI 2.0 + SAA 2.0 三件套 |
| **Undertow 不兼容 Servlet 6.1** | 高 | 启动失败 | 排查各模块；统一切换 `spring-boot-starter-tomcat` |
| **Spring Cloud Alibaba 2025.x 未发布或不齐** | 中 | Nacos 注册/配置失效 | 等待配套版；或暂时用 Eureka/本地配置降级 |
| **第三方 SDK Jakarta EE 11 兼容** | 中 | 连接失败 | 逐个验证 Nacos/Redis/Milvus/Qdrant/gRPC SDK |
| **Milvus 2.4.x 与 spring-ai-milvus-store 兼容性** | 中 | 连接失败 | docker-compose 固定 Milvus 版本，先验证再集成 |
| 自研 ToolCallback 与 Spring AI ToolCallback 同名冲突 | 高 | 编译错误 | 重命名自研类为 `JarvisToolCallback` |
| 自研 ChatMemory 与 Spring AI ChatMemory 同名冲突 | 高 | 编译错误 | 重命名为 `JarvisChatMemory` |
| DataAgent 在 M1.1 版本不可用或不稳定 | 中 | Text2SQL 失效 | 回退至 `supersql` 思路自研实现 |
| ReactAgent API 在 M1.1 版本变化 | 中 | Agent 编排失败 | 回退至 `jarvis.agent.framework=custom` 自研 AgentCore |
| 多 Advisor order 冲突 | 低 | 链路顺序错乱 | 通过 `getOrder()` 显式声明，单元测试覆盖 |
| 向量库检索延迟过高 | 中 | 对话响应慢 | 设 topK ≤ 5、score-threshold ≥ 0.7；可异步预检索 |
| **IDEA 版本过低** | 中 | 开发环境报错 | 升级至 IDEA 2025+ |

### 7.2 实施风险

| 风险 | 应对 |
|------|------|
| Spring Boot 4 大版本升级引入大量兼容问题 | Phase 0 单独隔离验证；保留 v9 分支可随时回退 |
| SAA M1.1 里程碑版生产稳定性存疑 | 关键路径全部保留 legacy 开关；M1.1 仅用于新功能，核心链路可回退 |
| 大量现有代码改造引入 bug | 每阶段保留 legacy 开关，可随时回退 |
| 自研 ChatMemory 重命名涉及面广 | 全局批量替换 + 编译验证 |
| 提示词迁移可能丢失原语义 | 逐个对比迁移前后输出，保留 legacy 模式对照 |
| Milvus 引入增加运维成本 | docker-compose 一键启动；提供 `jarvis.vector-store.type=qdrant` 回退 |

### 7.3 验收标准

| 维度 | 验收标准 |
|------|---------|
| **基线升级** | Spring Boot 4.0.x 启动无 Undertow/Servlet 报错；所有模块 `mvn compile` 通过 |
| 依赖 | 7 个核心模块 pom.xml 含 spring-ai-bom 2.0.0 + spring-ai-alibaba 2.0.0-M1.1 依赖 |
| 向量库 | Milvus 与 Qdrant 双库均可读写；4 个 collection 数据可查；切换开关后路由正确 |
| 嵌入 | `TextEmbeddingService.embedText` 返回真实 1536 维向量，维度一致、确定性可复现 |
| 工具调用 | `@Tool` 注解工具 ≥ 5 个；ReactAgent 支持 ReAct 多轮循环；`jarvis.tool.mode=custom` 可回退 |
| Agent 框架 | SAA ReactAgent 可正常编排；`jarvis.agent.framework=custom` 可回退自研 AgentCore |
| Text-to-SQL | DataAgent 可自然语言查库；`SqlSafetyValidator` 拦截所有非 SELECT；`engine=supersql` 可回退 |
| Advisor | 5 个 Advisor 按 order 执行；敏感词命中即拦截；日志为 JSON 格式 |
| 可观测性 | `/actuator/prometheus` 可访问；SAA Graph Observation 链路追踪可见；Studio UI 可调试 |
| 提示词 | `prompts/` 目录含 ≥ 5 个 .st 模板；Nacos 热更新生效；`legacy` 模式可回退 |
| ChatMemory | 跨会话提问能召回历史；`jarvis.memory.retrieval.enabled=false` 可关闭 |
| 开关 | 所有 15 个开关的值组合测试通过；失败回退符合预期 |
| 性能 | 对话 P95 延迟 < 2s；向量检索延迟 < 200ms；工具调用响应 < 1.5s |

---

## 附录

### A. v10 与 v9 / v9-3 的关系

```
jarvis-v9-requirements.md
  └── v9 愿景：AgentCore、InnerTool、三层压缩、多路 RAG、MCP、持久化、异步、缓存、安全、监控

jarvis-v9-3-requirements.md
  └── v9-3 现状盘点：架构 85%、端到端 45%、基础设施 20%；列出 Fix-1~5、Opt-1~6

jarvis-v10-requirements.md（本文档）
  └── v10 升级路径（质的飞跃）：
      1. Spring Boot 3.3.5 → 4.0.x（Framework 7 / Jakarta EE 11）
      2. 引入 Spring AI 2.0.0 GA + Spring AI Alibaba 2.0.0-M1.1
      3. SAA 生态组件：ReactAgent / DataAgent / Graph / Studio / Nacos 热更新
      4. 把"占位/模拟/硬编码"替换为"真实可用 + 开关切换"
      5. 补齐 v9-3 中未落地的嵌入、向量检索、历史记忆、Advisor、提示词模板
```

### B. 版本升级关键决策

| 决策点 | 选择 | 理由 |
|--------|------|------|
| Spring Boot 版本 | 4.0.x | Spring AI 2.0 强制要求；Framework 7 带来虚拟线程/GraalVM 增强 |
| Java 版本 | 17（最低）/ 21+（推荐） | Spring Boot 4 最低 17；21 支持虚拟线程 |
| SAA 版本 | 2.0.0-M1.1 | 用户指定；里程碑版但带来 ReactAgent/DataAgent/Graph 等质的飞跃 |
| Text2SQL 引擎 | DataAgent（首选） | SAA 官方生态，比 SuperSQL 更贴合 Spring AI 体系 |
| Agent 框架 | SAA ReactAgent（首选） | 原生 ReAct 循环 + Hooks + Skills，优于自研 |
| 向量库 | Milvus（主）+ Qdrant（副） | Milvus 性能规模更优；Qdrant 保留兼容验证双库 |
| Servlet 容器 | Tomcat | Undertow 不兼容 Servlet 6.1 |

### C. 关键改造文件索引

| 模块 | 文件 | 改造类型 |
|------|------|---------|
| 全局 | 所有 `pom.xml` | Spring Boot 4.0 + Spring AI 2.0 + SAA 2.0 依赖升级 |
| java-jarvis | `agent/ChatMemory.java` | 重命名为 `JarvisChatMemory` |
| java-jarvis | `agent/AgentCore.java` | 改造为双框架开关（ReactAgent / 自研）+ 历史检索 |
| java-jarvis | `agent/tool/ToolCallback.java` | 重命名为 `JarvisToolCallback` 避免冲突 |
| java-jarvis | `agent/tool/InnerToolAdapter.java` | 新增：自研 → Spring AI 适配 |
| java-jarvis | `config/ReactAgentConfig.java` | 新增：SAA ReactAgent Bean |
| java-jarvis | `agent/SpringAiChatMemory.java` | 新增：实现 Spring AI 2.0 接口 |
| java-jarvis | `agent/VectorMemoryStore.java` | 新增：向量库长期记忆 |
| java-jarvis | `config/VectorStoreConfig.java` | 新增：多 VectorStore Bean |
| java-jarvis | `advisor/LoggingAdvisor.java` | 新增 |
| java-jarvis | `advisor/SensitiveWordAdvisor.java` | 新增 |
| java-jarvis | `agent/security/ContentSecurityFilter.java` | 改造：词库外置 |
| java-jarvis | `agent/monitor/AgentMonitoringAspect.java` | 改造：Micrometer + JSON 日志 |
| java-jarvis | `resources/prompts/**/*.st` | 新增：模板文件 |
| jarvis-llm | `service/impl/SpringAiLlmServiceImpl.java` | 新增 |
| jarvis-llm | `service/impl/PromptServiceImpl.java` | 改造：PromptTemplate + Nacos |
| jarvis-rag | `service/MultiRecallRagService.java` | 改造：注入 VectorStore |
| jarvis-data | `service/TextEmbeddingService.java` | 改造：委托 DashScope |
| jarvis-data | `config/VectorService.java` | 标记 @Deprecated |
| jarvis-data | `grpc/DataServiceImpl.java` | 补齐 searchSimilar* |
| jarvis-sql | `config/DataAgentConfig.java` | 新增：SAA DataAgent |
| jarvis-sql | `service/SuperSqlTextToSqlService.java` | 新增：备选方案 |
| jarvis-sql | `service/SqlSafetyValidator.java` | 新增 |
| jarvis-sql | `controller/TextToSqlController.java` | 移除直接执行端点 |
| docker-compose | `docker-compose.yml` | 新增 Milvus 服务 |

### D. 术语表

| 术语 | 说明 |
|------|------|
| Spring AI 2.0 | Spring 官方 AI 应用框架 GA 版，提供 ChatClient/ChatModel/VectorStore/ToolCallback/Advisor/ChatMemory 抽象，强制 Spring Boot 4 |
| Spring AI Alibaba (SAA) 2.0 | 阿里基于 Spring AI 2.0 的扩展，提供 DashScope（通义千问）ChatModel/EmbeddingModel + Agent Framework + Graph + Studio |
| ReactAgent | SAA Agent Framework 提供的 ReAct 模式智能体，自带多轮工具调用循环、Hooks、Skills |
| DataAgent | SAA 生态项目，基于自然语言转 SQL 的智能体，支持 Schema 感知、多步纠错、结果可视化 |
| Graph Core | SAA 图工作流运行时，支持持久化、流式、MCP 节点 |
| Studio | SAA 嵌入式 Agent 调试与可视化 UI |
| Spring Boot 4.0 | 基于 Spring Framework 7.0 / Jakarta EE 11 / Java 17+ 的新一代基线 |
| Milvus | 开源向量数据库，支持大规模向量相似度检索 |
| Qdrant | 开源向量数据库，本项目 v9 已引入 |
| VectorStore | Spring AI 的向量存储抽象接口，屏蔽底层 Milvus/Qdrant 差异 |
| ToolCallback | Spring AI 2.0 的工具回调接口，支持 `@Tool` 注解或编程式注册 |
| Advisor | Spring AI 2.0 的拦截器机制，类似 AOP，用于日志/安全/记忆/RAG 注入 |
| PromptTemplate | Spring AI 的提示词模板，支持 `.st` 文件 + 变量渲染 |
| ChatMemory | Spring AI 2.0 的对话记忆接口，支持 add/get/clear |
| ReAct | Reasoning + Acting，模型多轮调用工具直至返回最终答案 |
| SuperSQL | 腾讯开源 Text-to-SQL 引擎，v10 作为 DataAgent 的备选方案 |
| Jakarta EE 11 | Java 企业版规范（Servlet 6.1 / JPA 3.2），Spring Boot 4 基线 |
| RRF | Reciprocal Rank Fusion，多路召回结果融合排序算法 |
