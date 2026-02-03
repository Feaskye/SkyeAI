FROM golang:1.20-alpine AS builder

WORKDIR /app

COPY go.mod go.sum ./
RUN go mod download

COPY . .

RUN go build -o proactive-server ./cmd/main.go

FROM alpine:latest

WORKDIR /app

COPY --from=builder /app/proactive-server .
COPY config.toml .

EXPOSE 8080 9090

CMD ["./proactive-server"]
