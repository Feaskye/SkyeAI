#!/bin/bash

# 确保脚本在出错时退出
set -e

# 定义生成目录
GEN_DIR="./pkg/api"

# 确保生成目录存在
mkdir -p $GEN_DIR

# 定义proto文件路径
PROTO_FILE="../proto/edge.proto"

# 生成Go gRPC代码
echo "Generating Go gRPC code..."
protoc --go_out=$GEN_DIR --go_opt=paths=source_relative \
       --go-grpc_out=$GEN_DIR --go-grpc_opt=paths=source_relative \
       $PROTO_FILE

echo "Go gRPC code generation completed successfully!"
echo "Generated files are in $GEN_DIR"
