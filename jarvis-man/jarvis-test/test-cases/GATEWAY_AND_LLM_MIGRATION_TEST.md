# 测试用例：网关服务和大模型迁移测试

## 测试目的
验证以下功能：
1. java-jarvis作为网关服务的路由功能
2. 大模型相关代码从cognition服务迁移到llm服务后的功能
3. 阿里大模型的集成功能
4. 多模态处理功能
5. 系统整体功能

## 测试环境
- 操作系统：Windows 10/11
- Docker和Docker Compose
- JDK 17
- Maven 3.9.6

## 测试准备
1. 确保所有服务都已构建并启动
2. 确保Nacos服务发现正常工作
3. 确保Ollama服务正常运行

## 测试用例

### 1. 网关服务路由测试

#### 测试步骤
1. 发送HTTP请求到网关服务的不同路由
2. 验证请求是否正确转发到对应服务
3. 验证响应是否正确

#### 测试命令
```bash
# 测试转发到llm服务
curl -X POST http://localhost:8080/api/llm/generate -H "Content-Type: application/json" -d '{"prompt": "Hello, world!"}'

# 测试转发到cognition服务
curl -X POST http://localhost:8080/api/cognition/react -H "Content-Type: application/json" -d '{"prompt": "What is the capital of France?"}'

# 测试转发到data服务
curl -X GET http://localhost:8080/api/data/health
```

#### 预期结果
- 所有请求都能正确转发到对应服务
- 所有响应都包含正确的结果

### 2. LLM服务文本生成测试

#### 测试步骤
1. 发送文本生成请求到llm服务
2. 验证响应是否正确

#### 测试命令
```bash
# 通过网关访问llm服务
curl -X POST http://localhost:8080/api/llm/generate -H "Content-Type: application/json" -d '{"prompt": "Hello, world!"}'

# 直接访问llm服务
curl -X POST http://localhost:8088/generate -H "Content-Type: application/json" -d '{"prompt": "Hello, world!"}'
```

#### 预期结果
- 两种访问方式都能返回正确的文本生成结果

### 3. 多模态处理测试

#### 测试步骤
1. 发送图像处理请求到llm服务
2. 发送语音处理请求到llm服务
3. 验证响应是否正确

#### 测试命令
```bash
# 测试图像处理
curl -X POST http://localhost:8080/api/llm/process-image -H "Content-Type: multipart/form-data" -F "image=@test-image.jpg" -F "type=jpg"

# 测试语音处理
curl -X POST http://localhost:8080/api/llm/process-speech -H "Content-Type: multipart/form-data" -F "audio=@test-audio.wav" -F "type=wav"
```

#### 预期结果
- 图像处理请求能返回正确的描述
- 语音处理请求能返回正确的转录

### 4. 阿里大模型集成测试

#### 测试步骤
1. 配置阿里大模型API密钥
2. 发送请求到llm服务，使用阿里大模型
3. 验证响应是否正确

#### 测试命令
```bash
# 测试阿里视觉模型
curl -X POST http://localhost:8080/api/llm/process-image -H "Content-Type: multipart/form-data" -F "image=@test-image.jpg" -F "type=jpg"

# 测试阿里语音模型
curl -X POST http://localhost:8080/api/llm/process-speech -H "Content-Type: multipart/form-data" -F "audio=@test-audio.wav" -F "type=wav"
```

#### 预期结果
- 当配置了阿里大模型API密钥时，llm服务应使用阿里大模型处理请求
- 当未配置阿里大模型API密钥时，llm服务应使用jarvis-edge或本地处理

### 5. 系统整体功能测试

#### 测试步骤
1. 启动所有服务
2. 发送复合请求到网关服务
3. 验证系统各部分是否协同工作

#### 测试命令
```bash
# 测试完整的对话流程
curl -X POST http://localhost:8080/api/chat -H "Content-Type: application/json" -d '{"message": "Hello, what can you do?"}'

# 测试多模态对话
curl -X POST http://localhost:8080/api/chat/multimodal -H "Content-Type: multipart/form-data" -F "message=What is in this image?" -F "image=@test-image.jpg"
```

#### 预期结果
- 系统能正确处理复合请求
- 各服务能协同工作，提供完整的功能

## 测试验证

### 验证网关服务
- [ ] 网关服务能正确路由请求到各微服务
- [ ] 网关服务能处理服务不可用的情况
- [ ] 网关服务能提供统一的API入口

### 验证大模型迁移
- [ ] llm服务能处理文本生成请求
- [ ] llm服务能处理图像识别请求
- [ ] llm服务能处理语音识别请求
- [ ] llm服务能集成阿里大模型
- [ ] llm服务能集成全模态模型

### 验证系统整体
- [ ] 所有服务能正常启动和运行
- [ ] 服务间能正常通信
- [ ] 系统能处理各种请求
- [ ] 系统性能满足要求

## 测试报告

### 测试结果
| 测试用例 | 预期结果 | 实际结果 | 状态 |
|---------|---------|---------|------|
| 网关服务路由测试 | 所有请求都能正确转发 | - | - |
| LLM服务文本生成测试 | 能返回正确的文本生成结果 | - | - |
| 多模态处理测试 | 能正确处理图像和语音 | - | - |
| 阿里大模型集成测试 | 能集成并使用阿里大模型 | - | - |
| 系统整体功能测试 | 系统各部分能协同工作 | - | - |

### 问题记录
| 问题描述 | 严重程度 | 解决方案 |
|---------|---------|---------|
| - | - | - |

### 测试结论
- [ ] 所有测试通过
- [ ] 部分测试通过，需要修复问题
- [ ] 测试失败，需要重大修复

## 测试时间
- 开始时间：YYYY-MM-DD HH:MM:SS
- 结束时间：YYYY-MM-DD HH:MM:SS
- 测试人员：[测试人员姓名]
