# 小米汽车舆情分析智能体

## 功能介绍
该智能体能够自动完成以下任务：
1. 接收用户指令"分析小米汽车的舆情"
2. 搜索相关新闻资讯
3. 调用大模型分析新闻情感倾向
4. 生成Markdown格式分析报告
5. 将报告发送至指定邮箱

## 技术栈
- **核心架构**: MCP (Model Control Plane) + LLM (Large Language Model) + Agent
- **后端框架**: Java 17, Spring Boot 3.2.0
- **AI组件**: 
  - LangChain4j (Agent能力)
  - Spring AI (MCP客户端)
  - OpenAI API (大语言模型)
- **数据获取**: NewsAPI (新闻搜索)
- **工具集成**: JavaMail (邮件发送), CommonMark (Markdown转HTML)

## 架构详解

### MCP+LLM+Agent三层架构

1. **MCP层（Model Control Plane）**
   - 通过`McpClientService`实现与模型控制平面的通信
   - 统一管理LLM调用，提供模型路由和负载均衡
   - 配置文件：`McpConfig.java`
   - 核心配置：
     ```properties
     spring.ai.mcp.client.server-url=http://localhost:8081
     spring.ai.mcp.client.api-key=YOUR_MCP_API_KEY
     spring.ai.mcp.client.timeout=30000
     ```

2. **LLM层（Large Language Model）**
   - 通过MCP层调用OpenAI模型进行自然语言处理
   - 负责情感分析、文本生成和任务决策
   - 配置文件：`application.properties`中的OpenAI相关配置

3. **Agent层**
   - 核心实现：`OpinionAnalysisAgent.java`
   - 功能：协调工具调用、管理对话状态、执行复杂任务
   - 三大工具：
     - 新闻搜索工具：调用`NewsSearchService`获取相关新闻
     - 情感分析工具：通过MCP调用LLM进行情感分析
     - 报告发送工具：生成分析报告并通过邮件发送
   - 记忆机制：使用`MessageWindowChatMemory`保存对话上下文

### 组件交互流程
1. 用户请求通过`OpinionAnalysisController`进入系统
2. Agent接收请求并解析用户意图
3. 根据需要依次调用：
   - 新闻搜索工具获取相关资讯
   - 情感分析工具对新闻内容进行情感评分
   - 报告生成工具汇总分析结果
   - 邮件发送工具将报告发送给指定邮箱
4. Agent整合所有结果并返回最终响应

## 环境配置

### 前提条件
- JDK 17+ 
- Maven 3.6+
- 有效的API密钥：
  - NewsAPI密钥 (获取地址: https://newsapi.org/)
  - OpenAI API密钥 (获取地址: https://platform.openai.com/)
  - MCP服务API密钥
- 邮件服务配置 (支持SMTP的邮箱账号)

### 配置步骤
1. 克隆项目到本地
2. 修改配置文件 `src/main/resources/application.properties`，替换以下占位符：
```properties
# OpenAI 配置
openai.api.key=YOUR_OPENAI_API_KEY
openai.model=gpt-3.5-turbo
openai.temperature=0.7
openai.max-tokens=1000

# MCP 配置
spring.ai.mcp.client.server-url=http://localhost:8081
spring.ai.mcp.client.api-key=YOUR_MCP_API_KEY
spring.ai.mcp.client.timeout=30000

# 新闻API配置
news.api.endpoint=https://newsapi.org/v2/everything
news.api.key=YOUR_NEWS_API_KEY
news.search.keyword=小米汽车

# 邮件配置
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=your-email@example.com
spring.mail.password=your-email-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
email.sender=舆情分析系统 <your-email@example.com>
```

## 项目结构
```
midemo/
├── src/
│   ├── main/
│   │   ├── java/com/xiaomi/auto/midemo/
│   │   │   ├── controller/       # API控制器
│   │   │   ├── service/          # 业务逻辑服务
│   │   │   ├── dto/              # 数据传输对象
│   │   │   └── XiaomiAutoOpinionApplication.java  # 应用入口
│   │   └── resources/
│   │       └── application.properties  # 配置文件
│   └── test/                     # 测试代码
├── pom.xml                       # Maven依赖
└── README.md                     # 项目说明
```

## 运行方法
### 本地运行
1. 构建项目
```bash
mvn clean package
```

2. 运行应用
```bash
java -jar target/midemo-1.0-SNAPSHOT.jar
```

3. 发送请求触发分析
使用POST请求调用API：
```bash
curl -X POST "http://localhost:8080/api/opinion/analyze" \
  -d "userInput=分析小米汽车的舆情" \
  -d "recipientEmail=target@example.com"
```

## CI/CD部署指南
### 基于Jenkins+Docker+K8s的自动化部署

### 环境要求
- Jenkins 2.303+（安装插件：Kubernetes、Docker Pipeline、Maven Integration）
- Docker 20.10+及Docker Registry
- Kubernetes集群 1.21+
- JDK 17+、Maven 3.8+

### 部署架构
1. 开发者提交代码到Git仓库
2. Jenkins触发自动构建流程
3. 构建Docker镜像并推送到镜像仓库
4. Kubernetes部署更新

### 部署配置文件说明
项目已包含以下CI/CD配置文件：
- **Dockerfile**: 应用容器化配置
- **Jenkinsfile**: CI/CD流水线定义
- **k8s/deployment.yaml**: Kubernetes部署配置
- **k8s/service.yaml**: Kubernetes服务配置

### 部署步骤

#### 1. 环境准备
##### 1.1 Jenkins配置
```bash
docker run -d -p 8080:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --name jenkins jenkins/jenkins:lts-jdk17
```

在Jenkins中配置：
- Docker Registry凭证（ID: docker-registry-token）
- Kubernetes配置（ID: kubeconfig）
- Maven安装路径

##### 1.2 Kubernetes集群准备
```bash
kubectl create namespace midemo
```

#### 2. 自动化部署流程
1. 代码推送到Git仓库触发Jenkins流水线
2. 流水线自动执行：代码检出→编译构建→单元测试→Docker镜像构建推送→K8s部署更新

#### 3. 手动部署方式（可选）
```bash
docker build -t midemo:latest .
kubectl apply -f k8s/deployment.yaml -f k8s/service.yaml
kubectl get pods -n midemo
```

### 优化建议

#### 1. Jenkins优化
启用Maven依赖缓存加速构建：
```groovy
stage('编译构建') {
  steps {
    cache(path: '~/.m2/repository', key: 'maven-deps') {
      sh './mvnw clean package -DskipTests'
    }
  }
}
```

#### 2. Docker优化
- 使用多阶段构建减小镜像体积（已实现）
- 配置镜像层缓存：
```dockerfile
COPY pom.xml .
RUN ./mvnw dependency:go-offline
COPY src src
```

#### 3. Kubernetes优化
- 配置资源限制（已实现）
- 启用自动扩缩容：
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: midemo-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: midemo-deployment
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

### 常见问题
- **镜像推送失败**：检查Docker Registry凭证和网络
- **K8s部署超时**：验证集群资源是否充足
- **Jenkins权限问题**：确保Jenkins用户有Docker和K8s操作权限

## 注意事项
- API密钥安全：请勿将包含密钥的配置文件提交到版本控制系统
- 新闻API限制：部分免费API有调用频率限制，可能影响分析结果
- 邮件服务配置：不同邮箱提供商的SMTP设置可能不同，需参考对应邮箱的帮助文档
- 大模型费用：OpenAI API调用会产生费用，请关注使用量

## 故障排除
- 新闻获取失败：检查NewsAPI密钥有效性和网络连接
- 情感分析错误：检查OpenAI API密钥和余额
- 邮件发送失败：验证SMTP配置和邮箱账号权限
- 应用启动失败：检查Java版本和依赖是否正确