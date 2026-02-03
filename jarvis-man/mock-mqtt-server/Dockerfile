# 使用官方Go镜像作为构建环境
FROM golang:1.22-alpine AS builder

# 设置工作目录
WORKDIR /app

# 复制go.mod和go.sum文件
COPY go.mod go.sum ./

# 下载依赖
RUN go mod download

# 复制源代码
COPY . .

# 构建应用
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o mqtt-server .

# 使用alpine作为运行环境
FROM alpine:latest

# 设置工作目录
WORKDIR /app

# 从构建环境复制二进制文件
COPY --from=builder /app/mqtt-server .

# 暴露MQTT和WebSocket端口
EXPOSE 9003 9004

# 运行应用
CMD ["./mqtt-server"]
