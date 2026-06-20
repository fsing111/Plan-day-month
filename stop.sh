#!/bin/bash
# 日/月计划和成果验收系统 - 停止脚本

cd "$(dirname "$0")"

echo "🛑 Stopping all services..."
docker compose down

echo ""
echo "✅ 所有服务已停止"
echo ""
echo "   如需同时清理数据卷（删除数据库数据），请运行："
echo "   docker compose down -v"
