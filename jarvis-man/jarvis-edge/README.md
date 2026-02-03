# Jarvis Edge Service

边缘服务组件，提供gRPC和HTTP接口，用于音频处理、状态管理等功能。

## 依赖要求

- Go 1.21+
- Protobuf编译器 (protoc) 3.20+
- protoc-gen-go 和 protoc-gen-go-grpc 插件

## 安装依赖

### 1. 安装Go依赖

```bash
go mod tidy
```

### 2. 安装Protobuf编译器

#### Windows
```bash
# 使用Chocolatey安装
choco install protoc

# 或者从GitHub下载预编译二进制文件
# https://github.com/protocolbuffers/protobuf/releases
```

#### Linux
```bash
# Ubuntu/Debian
sudo apt-get install -y protobuf-compiler

# CentOS/RHEL
sudo yum install -y protobuf-compiler
```

#### macOS
```bash
# 使用Homebrew安装
brew install protobuf
```

### 3. 安装Go gRPC插件

```bash
go install google.golang.org/protobuf/cmd/protoc-gen-go@v1.34
go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@v1.5
```

## 生成gRPC代码

在项目根目录下运行：

```bash
protoc --go_out=. --go-grpc_out=. pkg/api/edge.proto
```

这将生成以下文件：
- `pkg/api/edge.pb.go` - Protobuf消息定义
- `pkg/api/edge_grpc.pb.go` - gRPC服务定义

## 编译和运行

### 编译

```bash
go build -o jarvis-edge.exe ./cmd/main.go
```

### 运行

```bash
./jarvis-edge.exe
```

服务将启动两个端口：
- gRPC: 9091
- HTTP: 8081

## API接口

### gRPC接口

- `HealthCheck` - 健康检查
- `GetStatus` - 获取服务状态
- `StreamAudio` - 音频流处理

### HTTP接口

- `GET /health` - 健康检查
- `GET /status` - 获取服务状态
- `POST /api/asr` - 语音识别（模拟实现）
- `POST /api/tts` - 文本转语音（模拟实现）
- `GET /ws/audio` - 音频流WebSocket（未实现）

## 开发说明

- 所有gRPC接口定义在 `pkg/api/edge.proto` 文件中
- 实现代码在 `pkg/api/edge.go` 文件中
- 主程序入口在 `cmd/main.go` 文件中
- 使用Gin框架提供HTTP接口
- 使用gRPC框架提供gRPC接口
