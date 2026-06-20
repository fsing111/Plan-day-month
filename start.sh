#!/bin/bash
# 日/月计划和成果验收系统 - 一键启动脚本

set -e

echo "========================================"
echo "  日/月计划和成果验收系统 - 启动中..."
echo "========================================"

cd "$(dirname "$0")"

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker not found. Please install Docker first."
    exit 1
fi

echo "🐳 Starting services with Docker Compose..."
docker compose up -d --build

echo ""
echo "⏳ Waiting for services to be ready..."
sleep 10

echo ""
echo "✅ 系统启动完成！"
echo ""
echo "   前端地址:  http://localhost"
echo "   后端地址:  http://localhost:8080"
echo "   API 文档:  http://localhost:8080/doc.html"
echo ""
echo "   默认管理员账号: admin / admin123"
echo ""
echo "   查看日志:  docker compose logs -f"
echo "   停止服务:  ./stop.sh"
