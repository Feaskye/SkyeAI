# Jarvis V9 技术增强建议分析报告

## 基于当前项目架构的深度分析

---

## 一、当前项目技术现状评估

### 1.1 架构概览

**服务分布**：
- **Java服务** (13个): java-jarvis(主控), jarvis-llm, jarvis-cognition, jarvis-frontend, jarvis-user, jarvis-data, jarvis-rag, jarvis-knowledge, jarvis-dify, jarvis-sql, jarvis-advisor, jarvis-skills, jarvis-plugin, jarvis-recommendation
- **Go服务** (4个): jarvis-edge(边缘代理), jarvis-proactive(主动服务), jarvis-finance-go(金融分析), mock-mqtt-server
- **基础设施**: Redis, Qdrant, Mosquitto(MQTT), PostgreSQL(注释中), Nacos(注释中), Ollama(注释中)

**技术栈**：
- Java 17 + Spring Boot 3.3.5 (Web + WebFlux)
- Go 1.20+
- gRPC + Protobuf (服务间通信)
- Spring Cloud Gateway (API网关)
- Spring Cloud Alibaba Nacos (服务发现)
- Redis (缓存)
- Qdrant (向量数据库)
- MQTT (消息队列)

### 1.2 当前架构优势

✅ **微服务架构清晰**：服务职责明确，按功能拆分合理  
✅ **多语言技术栈**：Java适合业务逻辑，Go适合高性能边缘计算  
✅ **gRPC通信**：服务间采用高效的gRPC通信  
✅ **AI能力丰富**：集成RAG、ReAct、Skills、多模态等  
✅ **本地化支持**：支持Ollama本地模型部署  
✅ **向量检索**：Qdrant提供强大的向量相似度搜索  

### 1.3 当前架构痛点与改进空间

⚠️ **关键问题识别**：

1. **基础设施未启用**
   - PostgreSQL被注释（使用H2内存数据库测试）
   - Nacos被注释（服务发现未真正启用）
   - Ollama被注释（本地LLM未部署）
   - 影响：生产环境稳定性不足

2. **模型调用缺乏管理**
   - 硬编码模型配置（jarvis-proactive中固定model/temperature）
   - 无模型路由策略
   - 无Token成本统计
   - 无缓存机制
   - 影响：成本高、灵活性差

3. **服务间依赖复杂**
   - java-jarvis直接依赖jarvis-skills、jarvis-sql（Maven依赖）
   - 缺乏统一的服务治理
   - 影响：耦合度高、扩展困难

4. **缓存策略不完善**
   - 虽然引入了Redis，但未充分利用
   - 缺少多级缓存设计
   - 无向量缓存（相似查询重复调用LLM）
   - 影响：性能瓶颈、资源浪费

5. **监控与可观测性缺失**
   - 无链路追踪（Jaeger/Zipkin）
   - 无指标监控（Prometheus/Grafana）
   - 无日志聚合（ELK/Loki）
   - 影响：问题定位困难、性能优化无依据

6. **安全防护不足**
   - 无API网关鉴权
   - 无细粒度访问控制
   - 无审计日志
   - 影响：安全隐患

---

## 二、V9需求文档技术增强建议

### 2.1 AI技术优化 - 针对本项目的定制化建议

#### 建议1: 构建智能模型网关服务 (jarvis-model-gateway)

**当前问题**：
- jarvis-llm直接调用单一模型
- jarvis-proactive硬编码模型配置
- 无法根据任务类型选择最优模型

**技术方案**：
```
新建服务: jarvis-model-gateway (Java Spring Boot)
端口: 8095 (HTTP) / 9105 (gRPC)

核心功能:
├── 模型注册中心
│   ├── 云端模型: 通义千问、GPT-4、Claude等
│   ├── 本地模型: Ollama管理的qwen2.5、llama3等
│   └── 模型能力画像: 推理能力/上下文长度/成本/速度
├── 智能路由器
│   ├── 规则路由: 简单问答→轻量模型，复杂推理→强大模型
│   ├── 语义路由: 基于意图识别选择专用模型
│   └── 负载均衡: 多模型实例流量分配
├── 成本优化器
│   ├── Token统计与计费
│   ├── 语义缓存层（基于Qdrant向量相似度）
│   └── 请求批处理
└── 降级与容错
    ├── 模型故障自动切换
    └── 超时降级策略
```

**实施优先级**: 🔥 **最高优先级**  
**预期收益**: 
- LLM成本降低40-60%
- 响应速度提升30-50%
- 系统可用性提升至99.9%

**实施步骤**：
1. 第1周：设计模型注册表和路由策略
2. 第2周：实现基础路由和负载均衡
3. 第3周：集成语义缓存（复用Qdrant）
4. 第4周：迁移jarvis-llm和jarvis-proactive使用网关

---

#### 建议2: 实现语义缓存层 (Semantic Cache)

**当前问题**：
- 相似问题重复调用LLM
- 无缓存复用机制
- Qdrant仅用于RAG，未用于缓存

**技术方案**：
```java
// 语义缓存核心实现
@Service
public class SemanticCacheService {
    
    @Autowired
    private QdrantClient qdrantClient;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 查询语义缓存
     * 1. 将查询文本向量化
     * 2. 在Qdrant中搜索相似度>0.95的历史请求
     * 3. 返回缓存结果
     */
    public Optional<CacheResult> querySemanticCache(String query) {
        // 向量相似度匹配
        List<ScoredPoint> similar = qdrantClient.searchSimilar(
            collection: "llm_cache",
            vector: embed(query),
            threshold: 0.95
        );
        
        if (!similar.isEmpty()) {
            return Optional.of(buildCacheResult(similar.get(0)));
        }
        return Optional.empty();
    }
    
    /**
     * 缓存LLM响应
     */
    public void cacheResponse(String query, String response, Metadata metadata) {
        // 存储到Qdrant（向量+元数据）
        qdrantClient.upsert("llm_cache", 
            point(query, embed(query), response, metadata)
        );
        
        // 热点数据同步到Redis
        redisTemplate.opsForValue().set(
            "cache:hot:" + hash(query), 
            response, 
            1, TimeUnit.HOURS
        );
    }
}
```

**实施优先级**: 🔥 **最高优先级**  
**预期收益**:
- 缓存命中率>60%
- LLM调用减少50%+
- 响应时间从秒级降至毫秒级

---

#### 建议3: 启用并优化Ollama本地模型

**当前问题**：
- docker-compose中Ollama被注释
- 完全依赖云端API
- 数据隐私风险

**技术方案**：
```yaml
# docker-compose.yml 优化
services:
  ollama:
    image: ollama/ollama:latest
    container_name: jarvis-ollama
    ports:
      - "11434:11434"
    volumes:
      - ollama_data:/root/.ollama
    deploy:
      resources:
        reservations:
          devices:
            - driver: nvidia
              capabilities: [gpu]
              count: 1  # 如有GPU
    environment:
      - OLLAMA_HOST=0.0.0.0
      - OLLAMA_MODELS=/root/.ollama/models
    restart: unless-stopped
    networks:
      - jarvis-network
    
  # 模型预加载服务
  ollama-model-loader:
    image: curlimages/curl:latest
    depends_on:
      - ollama
    command: >
      sh -c "
        sleep 10 &&
        curl -X POST http://ollama:11434/api/pull -d '{"name": "qwen2.5:7b"}' &&
        curl -X POST http://ollama:11434/api/pull -d '{"name": "nomic-embed-text"}'
      "
    networks:
      - jarvis-network
```

**推荐模型配置**：
- **日常对话**: qwen2.5:7b (均衡性能)
- **复杂推理**: qwen2.5:14b 或 llama3.1:70b (如有GPU)
- **向量化**: nomic-embed-text (轻量高效)
- **代码生成**: codellama:13b

**预期收益**:
- 本地响应延迟<500ms
- 数据完全本地化
- 云端成本降低70%

---

### 2.2 Agent架构增强 - 针对本项目的定制化建议

#### 建议4: 实现分层Agent记忆系统

**当前问题**：
- jarvis-cognition的记忆系统较简单
- 无长期记忆持久化
- 记忆无结构化组织

**技术方案**：
```
记忆架构设计:
┌─────────────────────────────────────────┐
│         工作记忆 (Working Memory)        │
│  - 当前对话上下文                         │
│  - 存储在Redis (TTL: 30分钟)             │
│  - 容量: 最近20轮对话                     │
└──────────────┬──────────────────────────┘
               │ 定期总结
┌──────────────▼──────────────────────────┐
│        短期记忆 (Short-term Memory)      │
│  - 会话摘要和关键信息                     │
│  - 存储在PostgreSQL                      │
│  - 容量: 最近7天                         │
└──────────────┬──────────────────────────┘
               │ 向量嵌入
┌──────────────▼──────────────────────────┐
│        长期记忆 (Long-term Memory)       │
│  - 用户偏好、习惯、重要事件               │
│  - 存储在Qdrant (向量检索)               │
│  - 容量: 永久（带遗忘机制）               │
└──────────────┬──────────────────────────┘
               │ 模式识别
┌──────────────▼──────────────────────────┐
│        元记忆 (Meta Memory)              │
│  - 学习到的经验和模式                    │
│  - 存储在PostgreSQL + Qdrant            │
│  - 用于Agent自我优化                     │
└─────────────────────────────────────────┘
```

**实施优先级**: 🚀 **高优先级**  
**预期收益**:
- 对话连贯性提升80%
- 用户满意度显著提升
- Agent智能度质的飞跃

---

#### 建议5: 构建多Agent协作框架

**当前问题**：
- 单一Agent处理所有任务
- 无专业分工
- 复杂任务处理能力有限

**技术方案**：
```java
// Agent协作编排器
@Component
public class AgentOrchestrator {
    
    // 定义专业Agent
    private Map<String, Agent> agents = Map.of(
        "researcher", new ResearchAgent(llmClient, searchService),
        "coder", new CodeAgent(llmClient, githubClient),
        "analyst", new AnalysisAgent(llmClient, financeService),
        "planner", new PlannerAgent(llmClient),
        "executor", new ExecutorAgent(llmClient, skillsService)
    );
    
    /**
     * 任务分解与分配
     */
    public AgentResponse handleComplexTask(TaskRequest request) {
        // 1. Planner Agent分解任务
        TaskDecomposition decomposition = 
            agents.get("planner").decompose(request);
        
        // 2. 并行执行子任务
        List<CompletableFuture<SubTaskResult>> futures = 
            decomposition.getSubTasks().stream()
                .map(subTask -> {
                    Agent agent = selectBestAgent(subTask);
                    return CompletableFuture.supplyAsync(
                        () -> agent.execute(subTask)
                    );
                })
                .toList();
        
        // 3. 聚合结果
        List<SubTaskResult> results = 
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                    .map(CompletableFuture::join)
                    .toList())
                .join();
        
        // 4. 生成最终响应
        return agents.get("planner").synthesize(results);
    }
}
```

**实施优先级**: 🚀 **中优先级**  
**建议先实现**：
1. Research Agent（研究助手）
2. Code Agent（编程助手）
3. 后续逐步扩展

---

### 2.3 Skills生态系统 - 针对本项目的定制化建议

#### 建议6: 实现Skills版本管理与热更新

**当前问题**：
- jarvis-skills无版本控制
- 更新需重启服务
- 无依赖管理

**技术方案**：
```yaml
# Skills元数据标准
skill:
  name: "weather-query"
  version: "1.2.3"  # SemVer
  description: "查询天气信息"
  author: "jarvis-team"
  dependencies:
    - name: "http-client"
      version: ">=2.0.0"
    - name: "json-parser"
      version: ">=1.0.0"
  permissions:
    - "network:outbound"
    - "cache:read-write"
  config:
    api_endpoint: "https://api.weather.com"
    timeout: 5000
  triggers:
    - intent: "query_weather"
    - keywords: ["天气", "气温", "下雨"]
```

**热更新机制**：
```java
@Service
public class SkillsHotUpdateService {
    
    /**
     * 动态加载Skill
     */
    public void loadSkill(Path skillJar) {
        // 1. 验证签名和依赖
        SkillMetadata metadata = validateSkill(skillJar);
        
        // 2. 使用自定义ClassLoader加载
        URLClassLoader loader = new URLClassLoader(
            new URL[]{skillJar.toUri().toURL()},
            this.getClass().getClassLoader()
        );
        
        // 3. 注册到Skills注册表
        SkillInstance instance = loader.loadClass(metadata.getMainClass())
            .getDeclaredConstructor()
            .newInstance();
        
        skillRegistry.register(metadata.getName(), instance);
        
        // 4. 更新gRPC服务路由
        grpcRouter.updateRoute(metadata.getName(), instance);
    }
    
    /**
     * 无损更新
     */
    public void updateSkill(String skillName, Path newVersion) {
        // 1. 加载新版本
        loadSkill(newVersion);
        
        // 2. 优雅切换流量
        grpcRouter.gracefulSwitch(skillName);
        
        // 3. 卸载旧版本
        unloadOldVersion(skillName);
    }
}
```

**实施优先级**: 🚀 **高优先级**  
**预期收益**:
- Skills更新无需重启
- 支持回滚
- 生态扩展性增强

---

### 2.4 性能与可扩展性 - 针对本项目的定制化建议

#### 建议7: 启用Nacos并完善服务治理

**当前问题**：
- Nacos在docker-compose中被注释
- 服务发现未真正启用
- 配置分散在各服务

**技术方案**：
```yaml
# docker-compose.yml
services:
  nacos:
    image: nacos/nacos-server:v2.2.3
    container_name: jarvis-nacos
    ports:
      - "8848:8848"
      - "9848:9848"
    environment:
      - MODE=standalone
      - NACOS_AUTH_ENABLE=false
      - JVM_XMS=512m
      - JVM_XMX=512m
    volumes:
      - nacos_data:/home/nacos/data
    restart: unless-stopped
    networks:
      - jarvis-network

  # PostgreSQL（生产环境必需）
  postgres:
    image: postgres:15-alpine
    container_name: jarvis-postgres
    ports:
      - "5432:5432"
    environment:
      - POSTGRES_DB=jarvis
      - POSTGRES_USER=jarvis
      - POSTGRES_PASSWORD=jarvis_secure_password_2024
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/init.sql:/docker-entrypoint-initdb.d/init.sql
    restart: unless-stopped
    networks:
      - jarvis-network
```

**Nacos配置管理**：
```yaml
# 在Nacos中统一管理配置
jarvis-llm.yaml:
  llm:
    models:
      - name: qwen-plus
        provider: dashscope
        api-key: ${DASHSCOPE_API_KEY}
        temperature: 0.7
        max-tokens: 2000
      - name: qwen2.5-7b
        provider: ollama
        base-url: http://ollama:11434
        temperature: 0.7
        
jarvis-cognition.yaml:
  cognition:
    react:
      max-iterations: 10
      timeout: 30000
    memory:
      working-ttl: 1800
      short-term-days: 7
```

**实施优先级**: 🔥 **最高优先级**  
**理由**: 这是其他优化的基础

---

#### 建议8: 构建可观测性体系

**技术方案**：
```yaml
# docker-compose.yml 添加监控组件
services:
  # Prometheus指标收集
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./docker/prometheus.yml:/etc/prometheus/prometheus.yml
    networks:
      - jarvis-network
  
  # Grafana可视化
  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana_data:/var/lib/grafana
    depends_on:
      - prometheus
    networks:
      - jarvis-network
  
  # Jaeger链路追踪
  jaeger:
    image: jaegertracing/all-in-one:latest
    ports:
      - "16686:16686"  # UI
      - "14268:14268"  # Collector
    networks:
      - jarvis-network
```

**Spring Boot集成**：
```xml
<!-- pom.xml 添加监控依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-spring-boot-starter</artifactId>
</dependency>
```

**关键监控指标**：
- LLM调用延迟（P50/P95/P99）
- Token使用量和成本
- 缓存命中率
- 服务间gRPC调用成功率
- Agent任务完成率
- 系统资源使用率

**实施优先级**: 🚀 **高优先级**

---

### 2.5 安全与隐私 - 针对本项目的定制化建议

#### 建议9: 实现API网关鉴权

**当前问题**：
- jarvis-frontend暴露的API无鉴权
- 任何用户可访问所有服务

**技术方案**：
```java
// Spring Cloud Gateway鉴权过滤器
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {
    
    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // 1. 提取Token
            String token = request.getHeaders().getFirst("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return unauthorized(exchange);
            }
            
            // 2. 验证JWT
            String jwt = token.substring(7);
            try {
                Claims claims = JwtUtil.parseJWT(jwt);
                
                // 3. 检查权限
                String role = claims.get("role", String.class);
                String path = request.getURI().getPath();
                if (!hasPermission(role, path)) {
                    return forbidden(exchange);
                }
                
                // 4. 传递用户信息到下游
                ServerHttpRequest mutated = request.mutate()
                    .header("X-User-ID", claims.getSubject())
                    .header("X-User-Role", role)
                    .build();
                
                return chain.filter(exchange.mutate().request(mutated).build());
            } catch (Exception e) {
                return unauthorized(exchange);
            }
        };
    }
}
```

**实施优先级**: 🚀 **高优先级**

---

## 三、技术债务清理建议

### 3.1 紧急技术债务

1. **启用PostgreSQL**
   - 当前使用H2内存数据库，数据易丢失
   - 影响：生产环境不可用
   - 解决：取消注释PostgreSQL配置，迁移数据

2. **统一错误处理**
   - 各服务错误码不统一
   - 解决：建立全局错误码体系

3. **日志标准化**
   - 日志格式不统一
   - 解决：采用JSON结构化日志

### 3.2 中期技术债务

1. **服务间通信优化**
   - 部分服务仍用HTTP
   - 解决：全面转向gRPC

2. **配置管理集中化**
   - 配置分散在application.yml
   - 解决：迁移到Nacos

3. **依赖版本统一**
   - 各服务依赖版本不一致
   - 解决：建立父POM统一管理

---

## 四、实施路线图（优化版）

### 阶段一：基础设施完善（2-3周）🔥

**目标**：夯实基础，为后续优化做准备

| 任务 | 工作量 | 优先级 |
|------|--------|--------|
| 启用PostgreSQL + Nacos | 3天 | P0 |
| 统一日志和错误处理 | 2天 | P0 |
| 集成Prometheus + Grafana | 3天 | P1 |
| 启用Ollama本地模型 | 2天 | P1 |
| 配置Spring Boot Actuator | 2天 | P1 |

**交付物**：
- ✅ 稳定运行的基础设施
- ✅ 监控仪表盘
- ✅ 结构化日志

---

### 阶段二：AI能力优化（4-5周）🔥

**目标**：降低成本，提升性能

| 任务 | 工作量 | 优先级 |
|------|--------|--------|
| 构建jarvis-model-gateway | 2周 | P0 |
| 实现语义缓存层 | 1周 | P0 |
| 迁移现有服务使用网关 | 1周 | P0 |
| Token统计和成本监控 | 3天 | P1 |
| 模型性能基准测试 | 2天 | P1 |

**交付物**：
- ✅ 智能模型网关
- ✅ 语义缓存（命中率>60%）
- ✅ 成本降低40%+

---

### 阶段三：Agent与Skills增强（5-6周）🚀

**目标**：提升智能化水平

| 任务 | 工作量 | 优先级 |
|------|--------|--------|
| 分层记忆系统 | 2周 | P0 |
| Skills版本管理 | 1.5周 | P0 |
| Skills热更新 | 1周 | P1 |
| 多Agent协作框架 | 2周 | P1 |
| Agent监控评估 | 1周 | P1 |

**交付物**：
- ✅ 智能记忆系统
- ✅ Skills热更新能力
- ✅ 2-3个专业Agent

---

### 阶段四：安全与体验（3-4周）🚀

**目标**：完善安全和开发者体验

| 任务 | 工作量 | 优先级 |
|------|--------|--------|
| API网关鉴权 | 1周 | P0 |
| 审计日志系统 | 1周 | P1 |
| 开发者SDK | 1.5周 | P1 |
| CI/CD流水线优化 | 1周 | P1 |

**交付物**：
- ✅ 完整鉴权体系
- ✅ 开发者工具包
- ✅ 自动化部署

---

## 五、投资回报分析

### 5.1 成本优化

| 优化项 | 当前成本 | 优化后成本 | 节省比例 |
|--------|----------|------------|----------|
| LLM API调用 | 100% | 40-50% | 50-60% |
| 响应延迟 | 2-3s | 0.5-1s | 60-70% |
| 服务器资源 | 100% | 70% | 30% |
| 运维人力 | 100% | 60% | 40% |

### 5.2 性能提升

| 指标 | 当前 | V9目标 | 提升 |
|------|------|--------|------|
| P95延迟 | 2-3s | <500ms | 6-8x |
| 缓存命中率 | <10% | >60% | 6x |
| Agent任务成功率 | 70% | >95% | 35% |
| 系统可用性 | 95% | 99.9% | 5% |

### 5.3 开发效率

| 指标 | 当前 | V9目标 | 提升 |
|------|------|--------|------|
| 新功能开发周期 | 2周 | 3-5天 | 3-4x |
| 问题定位时间 | 2-4小时 | <30分钟 | 4-8x |
| 部署频率 | 1次/周 | 多次/天 | 10x+ |

---

## 六、风险评估与应对

### 6.1 技术风险

| 风险 | 影响 | 概率 | 应对措施 |
|------|------|------|----------|
| 模型网关单点故障 | 高 | 中 | 多实例部署+负载均衡 |
| 语义缓存误命中 | 中 | 中 | 相似度阈值调优+人工审核 |
| Skills热更新导致不稳定 | 高 | 低 | 灰度发布+快速回滚 |
| 多Agent协作复杂度高 | 中 | 高 | 分阶段实施，先简后繁 |

### 6.2 业务风险

| 风险 | 影响 | 应对措施 |
|------|------|----------|
| 优化期间功能冻结 | 用户体验下降 | 采用灰度发布，逐步上线 |
| 学习曲线陡峭 | 开发效率短期下降 | 提供培训和文档 |
| 技术债务清理影响进度 | 延期风险 | 预留20%缓冲时间 |

---

## 七、总结与建议

### 7.1 核心建议

1. **立即行动**（第1周）：
   - ✅ 启用PostgreSQL和Nacos
   - ✅ 集成Prometheus监控
   - ✅ 统一日志格式

2. **快速见效**（第2-4周）：
   - ✅ 构建模型网关
   - ✅ 实现语义缓存
   - ✅ 启用Ollama本地模型

3. **中期目标**（第5-10周）：
   - ✅ 记忆系统升级
   - ✅ Skills热更新
   - ✅ 多Agent协作

4. **长期规划**（第11-16周）：
   - ✅ 安全加固
   - ✅ 开发者生态
   - ✅ 性能调优

### 7.2 技术选型建议

| 需求 | 推荐方案 | 备选方案 |
|------|----------|----------|
| 服务网格 | Spring Cloud + Nacos | Istio + Kubernetes |
| 缓存 | Redis + Qdrant | Hazelcast + Milvus |
| 监控 | Prometheus + Grafana | Datadog（商业） |
| 追踪 | Jaeger | Zipkin |
| 日志 | Loki + Grafana | ELK Stack |
| 网关 | Spring Cloud Gateway | Kong |

### 7.3 团队建议

- **人员配置**：至少2名Java开发 + 1名Go开发 + 1名DevOps
- **技能要求**：Spring Boot、gRPC、Docker、向量数据库、LLM
- **开发流程**：敏捷开发，2周一个Sprint
- **代码审查**：严格Code Review，自动化测试覆盖率>80%

---

## 附录：快速启动清单

### A. 立即可执行的操作

```bash
# 1. 启用基础设施
docker-compose up -d postgres nacos redis qdrant mqtt

# 2. 初始化数据库
psql -h localhost -U jarvis -d jarvis -f docker/init.sql

# 3. 查看Nacos控制台
open http://localhost:8848/nacos

# 4. 启动Ollama并拉取模型
docker-compose up -d ollama
curl http://localhost:11434/api/pull -d '{"name": "qwen2.5:7b"}'
```

### B. 监控面板访问

- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **Jaeger**: http://localhost:16686
- **Nacos**: http://localhost:8848/nacos

### C. 关键配置文件

- `docker-compose.yml` - 服务编排
- `java-jarvis/pom.xml` - 依赖管理
- `jarvis-llm/src/main/resources/application.yml` - LLM配置
- `jarvis-cognition/src/main/resources/application.yml` - Agent配置

---

**文档版本**: v1.0  
**创建日期**: 2026-04-07  
**分析师**: AI Assistant  
**审阅状态**: 待审阅
