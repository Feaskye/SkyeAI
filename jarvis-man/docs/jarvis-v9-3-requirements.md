# Jarvis v9-3 需求与实现状态报告

> **版本**: v9.3  
> **日期**: 2026-07-08  
> **范围**: 基于 `jarvis-v9-requirements.md`、`jarvis-v9-technical-enhancement-analysis.md`、`Jarvis_v9_Optimization_Requirements_73b3219d.md` 三份 v9 文档，结合当前实际代码实现进行逐项核查  
> **目的**: 盘点已有实现、发现代码级改进点、给出下一阶段实施建议

---

## 目录

1. [执行摘要](#1-执行摘要)
2. [v9 需求逐项实现状态](#2-v9-需求逐项实现状态)
3. [技术增强建议评估（v9-analysis）](#3-技术增强建议评估v9-analysis)
4. [Qoder v9 Plan 覆盖情况](#4-qoder-v9-plan-覆盖情况)
5. [代码级改进建议](#5-代码级改进建议)
6. [基础设施债务清理](#6-基础设施债务清理)
7. [v9-3 实施路线图](#7-v9-3-实施路线图)

---

## 1. 执行摘要

### 1.1 整体评分

| 维度 | 评分 | 说明 |
|------|------|------|
| v9 核心架构实现 | ⭐⭐⭐⭐☆ 85% | AgentCore、InnerTool、三层记忆压缩、多路召回+RRF、MCP骨架已全部到位 |
| 端到端可用程度 | ⭐⭐☆☆☆ 45% | 大量模块使用模拟/stub数据，缺乏真实API集成 |
| 基础设施就绪度 | ⭐☆☆☆☆ 20% | PostgreSQL/Nacos/Ollama 仍被注释，无法形成完整链路 |
| 质量改进空间 | ⭐⭐⭐⭐☆ | 接口已存在，但需要连接各服务、接入真实API、补齐缺失层 |

### 1.2 关键事实

**已完成的核心架构组件（`java-jarvis/src/main/java/com/skyeai/jarvis/agent/`）**：

| 文件 | 功能 | 状态 |
|------|------|------|
| `AgentCore.java` | 统一对话编排器 | ✅ 架构完整，流程正确 |
| `Intent.java` + `IntentRecognizer.java` | 意图识别（CHAT/RAG/TOOL_CALL/SUB_AGENT/SUMMARY） | ✅ 关键词匹配，可扩展为LLM驱动 |
| `ChatMemory.java` | 双层压缩（摘要+滑动窗口），缺Assistant裁剪层 | ⚠️ 缺一层 |
| `ChatMemoryManager.java` | 会话记忆管理 | ✅ |
| `PersistentChatMemory.java` | Redis持久化，含异步写入 | ✅ 可运行 |
| `SummaryCompressor.java` | 调用LLM做摘要 | ⚠️ 模拟实现，未接入真实LLM |
| `SubAgent.java` + `SubAgentManager.java` | 子代理+独立记忆 | ✅ 骨架完整，响应生成用模拟 |
| `InnerTool.java` + `ToolRegistry.java` | 可插拔工具系统，Spring自动扫描注册 | ✅ 架构优秀 |
| `WeatherTool.java` + `StockTool.java` | 示例工具实现 | ⚠️ 模拟数据，需接真实API |
| `McpClientManager.java` + `McpServer.java` | MCP协议骨架 | ⚠️ 无传输层实现 |
| `AsyncToolExecutor.java` | 异步并行工具调用，带超时控制 | ✅ 完整 |
| `RagCache.java` | 本地+Redis双层RAG缓存 | ✅ 完整 |
| `monitor/AgentMonitoringAspect.java` | AOP监控指标采集 | ⚠️ 仅内存计数，未接Prometheus |
| `security/ContentSecurityFilter.java` | 内容安全过滤 | ⚠️ 骨架类 |
| `client/LlmClient.java` | 调用jarvis-llm服务的HTTP客户端 | ✅ 架构正确 |

**RAG多路召回（`jarvis-rag/src/`）**：

| 文件 | 功能 | 状态 |
|------|------|------|
| `MultiRecallRagService.java` | 语义向量+关键词+查询改写多路召回 | ✅ 流程完整 |
| `RrfFusion.java` | RRF融合算法 | ✅ 完整实现，支持泛型 |
| `QueryRewriter.java` | 同义词扩展+泛化+具体化 | ✅ 基础版，可扩展为LLM改写 |
| `KeywordStore.java` | 关键词存储（内存） | ⚠️ 纯内存，无持久化 |
| `Reranker.java` | 交叉编码器重排 | ⚠️ 骨架类 |
| `generateEmbedding()` | 文本向量化 | ❌ 返回随机向量，未接嵌入模型 |

**jarvis-llm服务能力**：

| 组件 | 功能 | 状态 |
|------|------|------|
| `AgentController.java` | Agent专用REST接口（/api/llm/agent/*） | ✅ 4个端点全实现 |
| `OpenAiLlmServiceImpl.java` | 模型调用（阿里通义+OpenAI兼容） | ✅ 支持模型路由fallback |
| `LlmService.java` | Agent接口定义（chat/chatWithMemory/chatStream等） | ✅ 接口完善 |

### 1.3 核心差距总结

```
┌─────────────────┬───────────┬───────────────────────────────┐
│     类别         │ 完成度    │        主要差距                │
├─────────────────┼───────────┼───────────────────────────────┤
│ 核心架构层       │ 85%       │ SummaryCompressor未接真实LLM   │
│ 工具系统         │ 70%       │ Weather/Stock工具为模拟数据    │
│ 记忆系统         │ 65%       │ Assistant消息裁剪层缺失        │
│ RAG检索          │ 55%       │ 向量化为随机数，KeywordStore   │
│                  │           │ 无持久化                       │
│ MCP协议          │ 25%       │ 仅有骨架，无JSON-RPC传输       │
│ 异步执行         │ 90%       │ 基本可用                       │
│ RAG缓存          │ 90%       │ 基本可用                       │
│ 安全过滤         │ 15%       │ ContentSecurityFilter为空      │
│ 监控             │ 30%       │ 指标在内存，未暴露Prometheus   │
│ 基础设施         │ 20%       │ PostgreSQL/Nacos/Ollama均未启  │
└─────────────────┴───────────┴───────────────────────────────┘
```

---

## 2. v9 需求逐项实现状态

### 2.1 AgentCore 核心编排器 ✅ 已完成

**对应原文档**: §3.2.1

**实际实现**: `AgentCore.java` + `IntentRecognizer.java` + `LlmClient.java`

**分析**:

架构图景已完全构建：

```
用户输入 → IntentRecognizer(意图识别) → chat|RAG|TOOL_CALL|SUB_AGENT
    ↓
chat分支: Memory.getOrCreate() → addMessage → callModel(llmClient)
              ↓ 每20条触发compressIfNeeded() → SummaryCompressor.compress()
              ↓ LLM返回后 → addMessage(assistant)
              
RAG分支: queryRag(userInput) → 注入知识库上下文 → 走chat流程

TOOL_CALL分支: chatWithTools(messages, ToolCallback[]) → callModelWithTools()
              ↓ LLM返回tool_call → handleToolCall() → 工具执行 → 结果回传给LLM

SUB_AGENT分支: SubAgentManager.createTempAgent() → agent.chat() → destroy()
```

**优点**:
- 意图识别分层清晰（SUMMARY > SUB_AGENT > TOOL_CALL > RAG > CHAT）
- 工具回调传递路径正确（AgentCore → LlmClient → jarvis-llm → OpenAiLlmServiceImpl）
- LlmClient的AgentResponse正确携带toolCall标志和toolCallData字段

**问题**:
- `queryRag()` 方法仍是模拟实现，未调用真正的RAG服务或MultiRecallRagService
- `callModelWithTools()` 中的工具参数转换过于简化——`convertToLlmTools()` 的parameters字段传空Map，LLM端无法获取工具的inputSchema
- `handleToolCall()` 是模拟逻辑，没有解析toolCallData里的实际参数，直接遍历所有工具执行了全部而不是只执行被调用的那个

### 2.2 可插拔工具系统 ✅ 已完成

**对应原文档**: §3.2.2

**实际实现**: `InnerTool.java` + `ToolRegistry.java` + `WeatherTool.java` + `StockTool.java` + `ToolCallback.java`

**分析**:

这是项目中设计最好的模块之一：

```
@PostConstruct
  └→ ToolRegistry.init() 
       └→ @Autowired List<InnerTool> innerTools (Spring自动注入所有实现)
            └→ registerInnerTool(tool)
                 └→ tool.loadToolCallbacks() → ToolCallback.builder().build()
                      └→ registerTool(callback) → toolCache.put(name, callback) + typeIndex[type].add(callback)
```

**优点**:
- Spring自动扫描机制优雅，新增工具只需声明@Component+实现InnerTool
- 类型索引(typeIndex)支持按类型查询工具
- 支持运行时动态unregisterTool

**问题**:
- WeatherTool和StockTool的fetch*方法是模拟数据映射，生产环境应替换为真实API调用（如和风天气API、东方财富/新浪财经API）
- InnerTool接口的loadToolCallbacks()返回的是静态注册的固定工具列表，不支持运行时动态增减不同数量的ToolCallback
- ToolCallback的Builder中parameters/inputSchema的传递链断裂——`convertToLlmTools()`忽略ToolCallback自己的schema

### 2.3 三层记忆压缩机制 ⚠️ 完成约65%

**对应原文档**: §3.2.3

**实际实现**: `ChatMemory.java` + `SummaryCompressor.java`

**当前实现的两层**:

| 层级 | 实现 | 状态 |
|------|------|------|
| 第1层：摘要压缩 | `SummaryCompressor.compress()` → 调用LLM | ⚠️ 模拟实现 |
| 第2层：Assistant裁剪 | ❌ 未实现 | ❌ 缺失 |
| 第3层：滑动窗口 | `ChatMemory.addMessage()` 中 `while(size > maxMessages)` | ✅ 完整 |

**缺失——Assistant消息裁剪层**:

v9要求"保留最近的Assistant消息"作为一层独立的压缩策略。当前实现只在滑动窗口层面粗暴地按位置删除最早的消息，没有区分user/assistant/tool/tool_result消息类型。

建议在`compressIfNeeded()`中添加专门的Assistant消息裁剪逻辑：

```java
// 伪代码思路：
// 当messages数量超过阈值时：
// 1. 先裁剪掉旧的Assistant消息（这些已被摘要压缩）
// 2. 保留最近N轮完整的user+assistant+tool交互
// 3. 最后滑动窗口兜底
```

**问题**:
- `SummaryCompressor.generateSummary()` 只是简单截取前150字符，没有真正调用LLM
- 需要在jarvis-llm侧提供专门的 `/api/llm/agent/memory-summary` 端点来驱动摘要压缩（该端点已在AgentController中存在，但SummaryCompressor没有通过LlmClient调用它）

### 2.4 多路RAG召回与RRF融合 ✅ 架构完整，数据层待完善

**对应原文档**: §3.2.4

**实际实现**: `MultiRecallRagService.java` + `RrfFusion.java` + `QueryRewriter.java`

**架构流程**:

```
query → QueryRewriter.rewrite()  → [原始, 同义词扩展, 泛化, 具体化]
         ↓
      MultiRecallRagService.query()
         ├─ 向量检索: Qdrant REST API + embedding(query) ← 目前是随机向量！
         ├─ 关键词检索: KeywordStore.search(每个rewrittenQuery)
         └─ RRF融合: rrfScores.merge(key, 1/(60+rank))
              ↓
         Reranker.rerank() ← 骨架类
              ↓
         buildContext() → 【文档n】格式文本
```

**优点**:
- RRF算法实现标准（K=60常数，score+=1/(k+rank)，多路累加）
- QueryRewriter的四路改写策略（同义词/泛化/具体化）覆盖了基本场景
- 多路结果去重合并做得好

**关键问题**:
1. **`generateEmbedding()` 返回随机向量**（1536维），这意味着向量检索永远无效
2. **`jarvis-llm/service/LlmService.embedText()` 返回空列表**——嵌入模型未被实现
3. **`KeywordStore` 纯内存实现**，重启即丢失，且初始化用的是硬编码示例数据
4. **`RagService.java`(agent包内)** 仍然是模拟数据分支匹配，与 `MultiRecallRagService` 没有整合
5. **`AgentCore.queryRag()`** 也没有调用MultiRecallRagService，仍然返回固定模拟字符串

### 2.5 子代理协作系统 ✅ 架构完整

**对应原文文档**: §3.2.5

**实际实现**: `SubAgent.java` + `SubAgentManager.java`

**架构**:

```
SubAgentManager {
    subAgents: ConcurrentHashMap<String, SubAgent>
    ├─ createSubAgent(id, name, systemPrompt)
    │    └→ SubAgent{memory=ChatMemory.forSubAgent(), state=IDLE}
    ├─ chatWithSubAgent(agentId, message)
    │    └→ subAgent.chat(message) → generateResponse()
    └─ destroySubAgent(agentId)
}
```

**优点**:
- 子代理拥有独立的ChatMemory实例
- AgentState生命周期管理（IDLE→WORKING→COMPLETED/ERROR）
- lastActiveTime追踪便于空闲回收

**问题**:
- `SubAgent.generateResponse()` 是纯模拟实现——回复 "[子代理: X] 收到您的请求..."
- 应接入jarvis-llm服务，像主Agent一样调用真实的LLM
- 缺少规划-执行-校验(P-E-C)协作模式
- 缺少Agent Swarm并行处理机制

### 2.6 MCP协议集成 ⚠️ 骨架阶段

**对应原文档**: §3.2.6

**实际实现**: `McpClientManager.java` + `McpServer.java`

**现状**:
- McpClient内部类定义了connect()/discoverTools()/callTool()接口
- 但connect()只做布尔标记，discoverTools()返回空列表，callTool()返回固定字符串"MCP工具响应"
- McpServer只有getAvailableTools()方法，没有实际的HTTP/JSON-RPC服务器启动逻辑

**需要补充的工作量很大**——要实现标准的MCP传输层：
1. HTTP+SSE或WebSocket作为传输协议
2. JSON-RPC 2.0消息格式
3. tools/list、tools/call、resources/read、prompts/list等标准方法

### 2.7 记忆持久化系统 ✅ 已完成

**对应原文档**: §3.2.7

**实际实现**: `PersistentChatMemory.java`

**架构**:

```
PersistentChatMemory {
    MEMORY_PREFIX = "chat:memory:"  → Redis list rightPush
    SUMMARY_PREFIX  = "chat:summary:" → Redis value set
    expireHours = 24 (可配置)
    
    addMessage(sessionId, message) → CompletableFuture.runAsync(() → redisList.push)
    getMessages(sessionId) → redisTemplate.opsForList().range()
    saveSummary(summary) → redisTemplate.opsForValue().set(key, summary, TTL)
    loadSummary(sessionId) → redisTemplate.opsForValue().get(key)
}
```

**优点**:
- 异步持久化不阻塞主线程
- 读写双重路径（内存优先→Redis回退）
- 支持过期时间自动清理

**问题**:
- `inMemoryMemory`始终为null（没有地方调用`setInMemoryMemory()`），所以addMessage实际上只做持久化不做内存操作
- Message对象必须可序列化才能存入Redis，当前缺少Serializable标记

### 2.8 异步工具调用 ✅ 已完成

**对应原文档**: §3.3.1

**实际实现**: `AsyncToolExecutor.java`

**功能**:
- `executeAsync(tool, params)` → CompletableFuture
- `executeAsync(tool, params, timeout)` → 带超时控制的CompletableFuture
- `executeParallel(List<ToolCall>)` → 并行执行，收集Map<toolName, result>
- ExecutorService固定10线程池，支持优雅关闭

### 2.9 RAG缓存机制 ✅ 已完成

**对应原文档**: §3.3.2

**实际实现**: `RagCache.java`

**架构**:

```
RagCache {
    localCache: ConcurrentHashMap  // 热数据加速
    Redis: rag:answer:* / rag:doc:* 
    
    getCachedAnswer(query) → 查local → 查Redis → 冷写热
    cacheAnswer(query, answer) → 双写local+Redis
    
    CacheStats { localCacheSize, redisAnswerCacheSize, redisDocumentCacheSize }
}
```

### 2.10 安全性增强 ⚠️ 骨架

**对应原文档**: §3.4

**实际实现**: `security/ContentSecurityFilter.java` — 仅骨架类

需要实现：
- 敏感词过滤（正则匹配+语义模型）
- 输入/输出双向安全检查
- 工具调用沙箱（高风险操作隔离）

### 2.11 可观测性 ⚠️ 中等完成度

**对应原文档**: §3.5

**实际实现**: `monitor/AgentMonitoringAspect.java`

**已有**:
- AOP切面监控AgentCore.chat()和chatWithTools()
- 内存计数器：调用次数、错误次数、总耗时
- 平均值计算和错误率

**缺失**:
- 未集成Micrometer/Prometheus暴露指标
- 无Grafana面板
- 无Jaeger链路追踪
- 日志非结构化JSON格式

---

## 3. 技术增强建议评估（v9-analysis）

原文档提出了9项技术增强建议，逐项评估：

| # | 建议项 | 优先级 | 实现状态 | 分析 |
|---|--------|--------|----------|------|
| 1 | **智能模型网关** (jarvis-model-gateway) | 🔥最高 | ⚠️部分 | OpenAiLlmServiceImpl已有模型列表和阿里fallback逻辑，但缺乏显式的路由决策层 |
| 2 | **语义缓存层** (Semantic Cache) | 🔥最高 | ❌未实现 | RagCache是基于精确key的哈希匹配，不是基于向量相似度的语义缓存 |
| 3 | 启用Ollama本地模型 | 🟡高 | ❌ docker-compose中注释 | 已在docker-compose.yml中注释 |
| 4 | 分层Agent记忆 | 🟡高 | ⚠️部分 | 当前只有工作记忆(内存)+长期(Redis)，缺短期(PostgreSQL)+元记忆(Qdrant) |
| 5 | 多Agent协作框架 | 🟡中 | ⚠️骨架 | SubAgent系统已搭建，但缺Planner/Reasoner专业分工 |
| 6 | Skills版本管理+热更新 | 🟡高 | ⚠️部分 | SkillRegistry有version字段但无SemVer规范和热加载ClassLoader |
| 7 | 启用Nacos | 🔥最高 | ❌ docker-compose中注释 | 服务间仍用固定URL硬编码 |
| 8 | 可观测性体系(P+G+J) | 🟡高 | ⚠️骨架 | 有AOP监控但无Prometheus/Grafana/Jaeger |
| 9 | API网关鉴权 | 🟡高 | ❌未实现 | 无任何认证中间件 |

---

## 4. Qoder v9 Plan 覆盖情况

原文档规划的6大支柱：

| 支柱 | 描述 | 完成度 | 关键缺失 |
|------|------|--------|----------|
| AI技术优化 | 多模型编排、微调、边缘AI、成本优化 | 30% | 无Token成本统计、无边缘部署 |
| Agent架构增强 | 多Agent协作、记忆学习、自主决策 | 50% | 缺ReAct自主循环、缺自我评估 |
| Skills生态系统 | 市场、版本、测试、社区 | 25% | Skills服务只有CRUD，无市场/测试框架 |
| 性能和可扩展性 | 延迟优化、缓存、负载均衡 | 45% | 无负载均衡、缓存效率低 |
| 安全和隐私 | 加密、访问控制、审计、隐私 | 15% | 无加密、无审计日志 |
| 开发者体验 | SDK、文档、测试、部署自动化 | 10% | 无SDK、文档分散 |

---

## 5. 代码级改进建议

以下建议针对已存在的代码做质量提升，不涉及新功能开发。

### 5.1 紧急修复

#### Fix-1: SummaryCompressor接入真实LLM

**影响**: 三层压缩的第一层形同虚设，长对话会浪费token  
**方案**: `SummaryCompressor` 改为通过 `LlmClient.generateMemorySummary()` 调用jarvis-llm

```java
// 修改前（模拟实现）
private static String generateSummary(String prompt) { ... }

// 修改后（接入LLM）
@Autowired private LlmClient llmClient;
public String compress(List<Message> messages, String existingSummary) {
    // ... 构建prompt ...
    return llmClient.generateMemorySummary(convert(messages), maxLength);
}
```

#### Fix-2: ChatMemory.generateEmbedding()替换为真实嵌入

**影响**: RAG多路召回中的向量检索部分永无结果  
**方案**: 通过jarvis-llm的embedText()或使用外部嵌入模型（如sentence-transformers）

```java
@Autowired private LlmClient llmClient;

private List<Float> generateEmbedding(String text) {
    List<Double> result = llmClient.embedText(text);
    return result.stream().map(d -> d.floatValue()).collect(Collectors.toList());
}
```

#### Fix-3: AgentCore.queryRag()调用MultiRecallRagService

**影响**: RAG检索永远返回模拟字符串，知识库毫无意义  
**方案**: 注入MultiRecallRagService并调用其query()方法

```java
@Autowired private MultiRecallRagService multiRecallRagService;

private String queryRag(String query) {
    String collection = "default";
    return multiRecallRagService.query(query, collection, 5);
}
```

#### Fix-4: AgentCore.handleToolCall()正确解析并执行被调用的工具

**影响**: 当前会遍历执行所有工具，而不是只执行LLM指定的工具  
**方案**: 从toolCallData中提取tool_name和arguments，只调用匹配的那个

```java
private String handleToolCall(Map<String, Object> toolCallData, List<ToolCallback> toolCallbacks) {
    String toolName = (String) toolCallData.get("toolName");
    Map<String, Object> args = (Map<String, Object>) toolCallData.get("arguments");
    
    for (ToolCallback cb : toolCallbacks) {
        if (cb.getName().equals(toolName)) {
            return cb.getFunction().apply(args != null ? args : Map.of());
        }
    }
    return "未找到工具: " + toolName;
}
```

#### Fix-5: ContentSecurityFilter填充实现

**影响**: 安全过滤层不存在，任何非法输入都可进入系统  
**方案**: 至少实现基础的敏感词正则过滤

```java
public SecurityResult filterInput(String input) {
    if (input == null || input.isBlank()) return SecurityResult.safe();
    for (Pattern p : sensitivePatterns) {
        if (p.matcher(input).find()) {
            return SecurityResult.blocked("包含敏感内容");
        }
    }
    return SecurityResult.safe();
}
```

### 5.2 中期优化

#### Opt-1: 添加Assistant消息裁剪层（记忆压缩第2层）

#### Opt-2: KeywordStore改为PostgreSQL/SQLite持久化

#### Opt-3: Monitor暴露Prometheus指标端点

#### Opt-4: RagCache增加语义相似度匹配（而非精确hash）

#### Opt-5: SubAgent.generateResponse()接入LlmClient

#### Opt-6: WeatherTool/StockTool对接真实外部API

---

## 6. 基础设施债务清理

### 6.1 当前docker-compose.yml状态

```yaml
# 活跃服务（正常运行）
redis:        ✅ 运行
qdrant:       ✅ 运行
mqtt:         ✅ 运行
edge-proxy:   ✅ 运行（Go服务）
jarvis-data:  ⚠️ 依赖PostgreSQL（被注释），实际跑不通
jarvis-rag:   ⚠️ 依赖Nacos（被注释）
... (其他13个Java服务同样依赖Nacos/PostgreSQL)

# 被注释的服务
postgres:     ❌ 被注释 → 所有服务用H2内存库，重启丢失
nacos:        ❌ 被注释 → 服务发现失效，URL硬编码
ollama:       ❌ 被注释 → 完全依赖云端API，无可控成本
```

### 6.2 清理建议（按优先级）

| 优先级 | 服务 | 动作 | 影响 |
|--------|------|------|------|
| P0 | PostgreSQL | 取消注释，创建数据库schema | 数据持久化，记忆系统落地 |
| P0 | Nacos | 取消注释，服务注册发现 | 动态服务发现，配置集中管理 |
| P1 | Ollama | 取消注释，拉取qwen2.5:7b | 本地低成本推理，断网可用 |
| P2 | Prometheus | 新增容器，所有Java服务加micrometer | 可视化监控 |
| P2 | Jaeger | 新增容器 | 全链路追踪 |
| P3 | Grafana | 新增容器 | 可视化仪表盘 |

### 6.3 Docker Compose整改方案

```yaml
# P0: 启用PostgreSQL + Nacos
services:
  postgres:
    image: postgres:15-alpine
    container_name: jarvis-postgres
    ports: ["5432:5432"]
    environment:
      - POSTGRES_DB=jarvis
      - POSTGRES_USER=jarvis
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/init.sql:/docker-entrypoint-initdb.d/init.sql

  nacos:
    image: nacos/nacos-server:v2.2.3
    container_name: jarvis-nacos
    ports: ["8848:8848", "9848:9848"]
    environment:
      - MODE=standalone
    volumes:
      - nacos_data:/home/nacos/data

# P2: 可观测性堆栈
  prometheus:
    image: prom/prometheus:latest
    ports: ["9090:9090"]
    volumes:
      - ./docker/prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana:latest
    ports: ["3000:3000"]
    depends_on: [prometheus]

  jaeger:
    image: jaegertracing/all-in-one:latest
    ports: ["16686:16686"]
```

---

## 7. v9-3 实施路线图

### Phase 1: 基础设施打通（1-2周）

**目标**: 让所有现有代码能跑起来

| 任务 | 工作量 | 产出 |
|------|--------|------|
| 1.1 启用PostgreSQL+Nacos | 1天 | docker-compose up成功，所有服务注册到Nacos |
| 1.2 编写init.sql（建表DDL） | 1天 | 用户表、对话表、知识文档表、Skill表 |
| 1.3 jarvis-data迁移至PostgreSQL | 1天 | H2→PG数据迁移脚本 |
| 1.4 安装Ollama并拉取qwen2.5:7b | 半天 | 本地模型可用 |
| 1.5 配置Nacos作为配置中心 | 1天 | 所有应用YAML迁移到Nacos |

### Phase 2: 核心链路接通（2-3周）

**目标**: AgentCore端到端可用

| 任务 | 工作量 | 产出 |
|------|--------|------|
| 2.1 Fix-1: SummaryCompressor接LlmClient | 1天 | 摘要压缩真正调用LLM |
| 2.2 Fix-2: generateEmbedding接real model | 1天 | RAG向量检索有效 |
| 2.3 Fix-3: AgentCore.queryRag接MultiRecallRagService | 1天 | RAG检索返回真实知识 |
| 2.4 Fix-4: handleToolCall正确解析参数 | 1天 | 工具调用只执行指定的工具 |
| 2.5 Opt-1: 添加Assistant消息裁剪层 | 1天 | 三层压缩全部就位 |
| 2.6 Opt-5: SubAgent接入LlmClient | 1天 | 子代理有真实能力 |
| 2.7 WeatherTool/StockTool接真实API | 2天 | 工具返回真实数据 |

### Phase 3: 安全与可观测性（1-2周）

**目标**: 生产可用级别

| 任务 | 工作量 | 产出 |
|------|--------|------|
| 3.1 Fix-5: ContentSecurityFilter实现 | 1天 | 输入输出安全过滤 |
| 3.2 Opt-3: Monitor接Prometheus+Grafana | 2天 | 可可视化监控 |
| 3.3 Opt-4: RagCache语义相似度匹配 | 1天 | 缓存命中率提升 |
| 3.4 MCP传输层骨架实现（HTTP+SSE） | 2天 | MCP Client可实际连接 |
| 3.5 日志JSON标准化 | 1天 | ELK/Kibana可读 |

### Phase 4: 深度增强（2-3周）

**目标**: 达到v9最终愿景

| 任务 | 工作量 | 产出 |
|------|--------|------|
| 4.1 Semantic Cache（Qdrant向量相似度缓存） | 2天 | 相似问题秒级返回 |
| 4.2 Model Gateway（显式路由决策层） | 3天 | 简单→轻量模型，复杂→强大模型 |
| 4.3 Skills SemVer版本管理 | 2天 | 版本号规范+回滚 |
| 4.4 Skills热更新ClassLoader | 2天 | 热加载无需重启 |
| 4.5 ReAct自主决策循环 | 2天 | Agent可自主规划执行 |
| 4.6 API网关鉴权（JWT） | 2天 | 安全认证入口 |

### Phase 5: 打磨与测试（1-2周）

**目标**: 全面验收

| 任务 | 工作量 | 产出 |
|------|--------|------|
| 5.1 全链路集成测试 | 2天 | 冒烟测试通过 |
| 5.2 性能基准测试（P95延迟、吞吐量） | 1天 | 性能达标报告 |
| 5.3 文档完善（API文档、部署指南） | 1天 | README更新 |
| 5.4 Bug修复与优化 | 2天 | 零P0/P1 bug |

---

## 附录

### A. 关键文件索引

**核心架构（java-jarvis/agent/）**:
```
java-jarvis/src/main/java/com/skyeai/jarvis/agent/
├── AgentCore.java              # 核心编排器（83行chat主流程）
├── ChatMemory.java             # 对话记忆（三层压缩，两实一缺）
├── ChatMemoryManager.java      # 记忆管理器
├── PersistentChatMemory.java   # Redis持久化
├── SummaryCompressor.java      # 摘要压缩（需接LLM）
├── Intent.java                 # 意图枚举
├── IntentRecognizer.java       # 关键词意图识别
├── Message.java                # 消息模型
├── RagCache.java               # RAG双层缓存
├── RagService.java             # RAG服务（模拟数据，待整合）
├── AsyncToolExecutor.java      # 异步并行工具执行
├── client/LlmClient.java       # LLM服务HTTP客户端
├── tool/InnerTool.java         # 工具接口
├── tool/ToolCallback.java      # 工具回调包装
├── tool/ToolRegistry.java      # 工具注册中心
├── tool/impl/WeatherTool.java  # 天气工具（模拟数据）
├── tool/impl/StockTool.java    # 股票工具（模拟数据）
├── subagent/SubAgent.java      # 子代理（响应为模拟）
├── subagent/SubAgentManager.java  # 子代理管理
├── mcp/McpClientManager.java   # MCP客户端（骨架）
├── mcp/McpServer.java          # MCP服务端（骨架）
├── monitor/AgentMonitoringAspect.java  # AOP监控
└── security/ContentSecurityFilter.java # 安全过滤（骨架）
```

**RAG多路召回（jarvis-rag）**:
```
jarvis-rag/src/main/java/com/skyeai/jarvis/rag/
├── MultiRecallRagService.java  # 多路召回主服务
├── RrfFusion.java              # RRF融合算法
├── QueryRewriter.java          # 查询改写
├── KeywordStore.java           # 关键词存储（纯内存）
├── Reranker.java               # 重排（骨架）
└── RagService.java             # RAG接口
```

**LLM服务（jarvis-llm）**:
```
jarvis-llm/src/main/java/com/skyeai/jarvis/llm/
├── controller/AgentController.java  # Agent REST端点
├── service/LlmService.java          # LLM接口定义
└── service/impl/OpenAiLlmServiceImpl.java  # 实现（阿里通义+OpenAI兼容）
```

### B. v9-3 vs v9 vs v9-analysis 三文档关系

```
jarvis-v9-requirements.md
  ├── v9核心需求：AgentCore、InnerTool、三层压缩、多路RAG、MCP、持久化、异步、缓存、安全、监控
  └── 6个实施阶段，每阶段有验收标准

jarvis-v9-technical-enhancement-analysis.md
  ├── 对当前项目的深度技术分析
  ├── 9项定制化增强建议（模型网关、语义缓存、Ollama、分层记忆等）
  ├── 投资回报分析（成本降40-60%，P95从2-3s降到<500ms）
  └── 16周实施路线图

.qoder/plans/Jarvis_v9_Optimization_Requirements_73b3219d.md
  ├── 6大支柱框架（AI技术/Agent架构/Skills生态/性能/安全/开发体验）
  └── 完成了v1-v8的需求回顾
```

**本v9-3文档的作用**: 将三份文档中的需求与建议，与当前代码逐一对照，给出"差什么、怎么补"的可执行清单。

### C. 术语表

| 术语 | 全称 | 说明 |
|------|------|------|
| AgentCore | Agent Orchestrator | 统一对话流程编排器 |
| InnerTool | Internal Tool Interface | 可插拔工具接口 |
| RRF | Reciprocal Rank Fusion | 互反秩融合算法，多路召回排序 |
| LlmClient | LLM Service Client | 调用jarvis-llm服务的HTTP客户端 |
| RAG | Retrieval-Augmented Generation | 检索增强生成 |
| MCP | Model Context Protocol | AI工具通信标准协议 |
| AOP | Aspect-Oriented Programming | 面向切面编程，用于监控 |
| ReAct | Reasoning + Acting | 推理-行动循环范式 |
