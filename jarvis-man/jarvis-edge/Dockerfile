# 使用官方Go镜像构建应用
FROM golang:1.21-alpine AS builder

# 设置工作目录
WORKDIR /app

# 复制go.mod和go.sum文件
COPY go.mod go.sum ./

# 下载依赖
RUN go mod download

# 复制源代码
COPY . .

# 生成gRPC代码
RUN apk add --no-cache protobuf-dev protoc
RUN go install google.golang.org/protobuf/cmd/protoc-gen-go@v1.33.0
RUN go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@v1.5.1
RUN protoc --go_out=./pkg/api --go_opt=paths=source_relative \
           --go-grpc_out=./pkg/api --go-grpc_opt=paths=source_relative \
           ../proto/edge.proto

# 构建应用
RUN go build -o edge-proxy ./cmd

# 使用alpine作为基础镜像运行应用
FROM alpine:3.19

# 设置工作目录
WORKDIR /app

# 从构建阶段复制二进制文件
COPY --from=builder /app/edge-proxy .

# 暴露端口
EXPOSE 8081 9091

# 运行应用
CMD ["./edge-proxy"]
