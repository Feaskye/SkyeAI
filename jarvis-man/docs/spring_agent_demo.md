这篇文章的核心技术点、实现思路、代码示例以及个人项目改进建议如下：

### 一、核心技术点

1.  **核心编排器 (AgentCore)**：作为系统的“大脑”，集成了意图识别、RAG检索、记忆管理、模型调用和工具执行的完整对话流程。
2.  **工具调用 (Function Calling)**：基于Spring AI构建了可插拔的工具注册机制，使LLM能够调用外部功能（如天气查询、知识检索、创建子代理等）。
3.  **检索增强生成 (RAG)**：实现了从文档加载、分块、向量化、存储到多路召回、重排的完整流水线，用于基于私有知识库的问答。
4.  **对话记忆管理 (ChatMemory)**：设计了三层上下文压缩策略（摘要压缩、Assistant消息裁剪、滑动窗口）来管理长对话，防止令牌溢出。
5.  **命令与技能系统 (Command & Skill)**：提供了两种基于Markdown的Prompt模板机制。Command由用户主动触发，Skill由LLM自主判断调用。
6.  **子代理 (SubAgent)**：支持创建拥有独立记忆和上下文的子代理，用于处理需要隔离的多轮任务。
7.  **模型上下文协议 (MCP)**：同时实现了MCP Client（连接外部工具）和MCP Server（对外暴露自身能力），实现了与外部服务的标准化连接。

### 二、实现思路与关键代码示例

#### 1. AgentCore 对话流程编排
*   **思路**：`AgentCore.chat()` 方法串联了整个处理链条。首先进行意图识别，决定是否需要RAG检索。然后管理对话记忆（自动触发压缩），最后调用大模型并处理可能的工具调用循环。
*   **代码示例**：
    ```java
    public String chat(String sessionId, String userInput) {
        ChatMemory memory = getOrCreateMemory(sessionId);
        // 1. 意图识别
        Intent intent = intentRecognizer.recognize(userInput);
        // 2. 如果是 RAG 意图，注入检索上下文
        if (intent == Intent.RAG && ragService.isKnowledgeLoaded()) {
            String ragContext = ragService.query(userInput);
            if (ragContext != null && !ragContext.isBlank()) {
                String enrichedInput = "以下是从知识库中检索到的相关参考资料，请结合这些资料回答用户的问题：\n\n" + ragContext + "\n\n用户问题：" + userInput;
                memory.addMessage(new UserMessage(enrichedInput));
            } else {
                memory.addMessage(new UserMessage(userInput));
            }
        } else {
            memory.addMessage(new UserMessage(userInput));
        }
        // 3. 构建消息并调用模型（记忆压缩在getMessages内部自动处理）
        List<Message> messages = memory.getMessages();
        Prompt prompt = new Prompt(messages, buildChatOptions());
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt(prompt);
        if (!toolCallbacks.isEmpty()) {
            requestSpec.toolCallbacks(toolCallbacks.toArray(new ToolCallback[0]));
        }
        String response = requestSpec.call().content();
        memory.addMessage(new AssistantMessage(response != null ? response : ""));
        return response != null ? response : "";
    }
    ```

#### 2. 三层对话记忆压缩 (ChatMemory)
*   **思路**：在 `getMessages()` 方法被调用时，自动检查并执行压缩逻辑，对调用方无感知。
*   **摘要压缩核心代码**：
    ```java
    private void compressIfNeeded() {
        if (chatClient == null || history.size() <= COMPRESS_THRESHOLD_MESSAGES) {
            return;
        }
        int compressEndIndex = history.size() - PRESERVE_RECENT_MESSAGES;
        // 保护TOOL消息的上下文完整性
        while (compressEndIndex < history.size() && history.get(compressEndIndex).getMessageType() == MessageType.TOOL) {
            compressEndIndex--;
        }
        if (compressEndIndex <= 0) return;
        List<Message> messagesToCompress = new ArrayList<>(history.subList(0, compressEndIndex));
        // 调用LLM进行摘要总结
        String newSummary = SummaryCompressor.compress(chatClient, messagesToCompress, summaryText);
        if (newSummary != null && !newSummary.isBlank()) {
            this.summaryText = newSummary;
            history.subList(0, compressEndIndex).clear(); // 清除已摘要的原始消息
        }
    }
    ```

#### 3. 可插拔工具注册
*   **思路**：定义统一的 `InnerTool` 接口，项目启动时Spring自动扫描并收集所有实现该接口的Bean所暴露的工具回调(`ToolCallback`)，实现开闭原则。
*   **接口与注册示例**：
    ```java
    public interface InnerTool {
        List<ToolCallback> loadToolCallbacks();
    }
    // 在某个具体工具类中实现
    @Component
    public class WeatherTool implements InnerTool {
        @Override
        public List<ToolCallback> loadToolCallbacks() {
            ToolCallback weatherTool = ToolCallback.builder()
                .name(“get_weather”)
                .description(“Get the current weather for a city”)
                .inputSchema(/* JSON Schema 定义参数 */)
                .function((city) -> fetchWeather(city)) // 实际执行函数
                .build();
            return List.of(weatherTool);
        }
    }
    ```

#### 4. 多路召回与融合 (RAG)
*   **思路**：结合语义向量检索、关键词检索(BM25)和查询改写召回，使用RRF (Reciprocal Rank Fusion) 算法融合不同检索器的结果，提升召回覆盖面。
*   **RRF融合核心逻辑**：
    ```java
    private void accumulateRrfScores(List<Document> results, Map<String, Double> rrfScores, Map<String, Document> keyToDocument) {
        for (int rank = 0; rank < results.size(); rank++) {
            Document doc = results.get(rank);
            String key = doc.getId(); // 假设Document有唯一ID
            keyToDocument.putIfAbsent(key, doc);
            // RRF公式: score(d) += 1.0 / (k + rank)
            double score = 1.0 / (RRF_CONSTANT_K + rank + 1);
            rrfScores.merge(key, score, Double::sum);
        }
    }
    ```

#### 5. SubAgent 独立记忆
*   **思路**：每个SubAgent持有自己独立的`ChatMemory`实例，与主代理隔离。通过工具调用（`create_sub_agent`, `chat_with_sub_agent`）由主LLM决策其生命周期。
*   **创建示例**：
    ```java
    public SubAgent(String id, String name, String systemPrompt, ChatClient chatClient) {
        this.memory = ChatMemory.forSubAgent(); // 关键：创建独立记忆实例
        this.memory.setSystemPrompt(systemPrompt);
        this.chatClient = chatClient;
        // ... 其他初始化
    }
    ```

### 三、个人项目改进建议

基于此Demo项目的设计，可以针对个人学习或生产化进行如下改进：

1.  **记忆持久化**：
    *   **现状**：`ChatMemory`目前基于内存，应用重启后对话历史丢失。
    *   **改进**：将对话历史、压缩后的摘要持久化到数据库（如Redis、PostgreSQL）。可以将会话(`sessionId`)、消息、摘要等模型化存储。

2.  **向量数据库集成**：
    *   **现状**：使用内存`VectorStore`，不适合大规模知识库。
    *   **改进**：集成专业的向量数据库，如Milvus、Pinecone、PgVector或Qdrant。实现`VectorStore`接口的对应实现，支持增量更新、元数据过滤等高级功能。

3.  **工具调用与RAG性能优化**：
    *   **异步工具调用**：对于网络IO密集型的工具（如调用外部API），可以改为异步非阻塞调用，提升Agent整体响应速度。
    *   **RAG缓存**：为频繁查询的问题建立缓存（向量结果或最终答案），减少不必要的向量检索和LLM调用，降低成本与延迟。

4.  **更复杂的Agent协作模式**：
    *   **现状**：主要为“主Agent + 子Agent”模式。
    *   **改进**：实现`工作流(Workflow)`或`智能体群(Agent Swarm)`。例如，可以设计一个“规划Agent”先拆解复杂任务，然后由“执行Agent”调用工具，最后“校验Agent”审核结果，形成协作链。

5.  **技能(Skill)系统的增强**：
    *   **动态加载**：支持在运行时（无需重启）动态添加、更新或删除Markdown技能文件。
    *   **技能组合**：允许LLM在一个回合内顺序或并行调用多个技能，并组合其结果。

6.  **可观测性与评估**：
    *   **日志与监控**：增强关键步骤的日志（意图识别结果、调用的工具、RAG检索的文档、令牌消耗等），并集成到APM系统（如SkyWalking, Prometheus）。
    *   **效果评估**：构建一个简单的测试框架，用一组标准问题测试Agent回答的准确性、工具调用的正确性，便于迭代优化。

7.  **前端与用户体验**：
    *   **现状**：提供了一个基础的Web聊天界面。
    *   **改进**：
        *   **可视化工具调用链**：在UI上展示Agent的思考过程，如“识别意图 -> 调用工具A -> 获得结果 -> 生成回复”。
        *   **知识库管理界面**：提供上传文档、查看已索引文档、触发重新构建向量库等功能的前端界面。

8.  **安全性增强**：
    *   **工具调用沙箱**：对于执行代码、文件操作等高风险工具，应在沙箱环境中运行，限制其权限。
    *   **输入/输出过滤**：对用户输入和LLM输出进行内容安全过滤，防止注入攻击或不当内容。

这个项目提供了一个非常扎实的、模块清晰的AI Agent工程化实践样板。上述改进点可以作为一个路线图，帮助你逐步将其扩展成一个功能更完备、更健壮、更实用的AI应用系统。