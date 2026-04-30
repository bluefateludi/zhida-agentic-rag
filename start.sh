#!/bin/bash
# 智答 AI 项目一键启动脚本（本地开发环境）
# 使用方式: bash start.sh

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"

# 检测是否在 worktree 中运行，并自动同步缺失的配置文件
MAIN_REPO_DIR="$(cd "$PROJECT_DIR" && git worktree list 2>/dev/null | head -1 | awk '{print $1}')"
if [ "$MAIN_REPO_DIR" != "$PROJECT_DIR" ]; then
    echo "检测到 worktree 环境，自动同步配置文件..."
    CONFIG_SYNC=false
    for f in .env src/main/resources/application-local.yml; do
        if [ ! -f "$PROJECT_DIR/$f" ] && [ -f "$MAIN_REPO_DIR/$f" ]; then
            cp "$MAIN_REPO_DIR/$f" "$PROJECT_DIR/$f"
            echo "  已复制: $f"
            CONFIG_SYNC=true
        fi
    done
    if [ "$CONFIG_SYNC" = false ]; then
        echo "  配置文件已是最新"
    fi
fi

echo "=== 智答 AI 启动 ==="

# 1. 启动 Docker Desktop
echo "[1/4] 启动 Docker Desktop..."
if ! docker info > /dev/null 2>&1; then
    start "" "C:/Program Files/Docker/Docker/Docker Desktop.exe" 2>/dev/null
    for i in $(seq 1 20); do
        docker info > /dev/null 2>&1 && echo "  Docker Desktop 已就绪" && break
        sleep 3
    done
else
    echo "  Docker Desktop 已在运行"
fi

# 2. 启动 PostgreSQL
echo "[2/4] 启动 PostgreSQL..."
if docker ps --format '{{.Names}}' | grep -q "zhida-postgres"; then
    echo "  zhida-postgres 已在运行"
else
    docker start zhida-postgres 2>/dev/null || docker run -d --name zhida-postgres \
        -e POSTGRES_USER=postgres \
        -e POSTGRES_PASSWORD='@Value123456' \
        -e POSTGRES_DB=postgres \
        -p 5432:5432 \
        pgvector/pgvector:pg16
    echo "  zhida-postgres 已启动"
fi
sleep 2

# 3. 启动后端
echo "[3/4] 启动后端 (Spring Boot)..."
cd "$PROJECT_DIR"
mvn spring-boot:run -Dmaven.test.skip=true > /tmp/spring-boot.log 2>&1 &
for i in $(seq 1 40); do
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:8123/api/chat/sessions 2>/dev/null | grep -q "200"; then
        echo "  后端已启动: http://localhost:8123/api"
        break
    fi
    sleep 3
done

# 4. 启动前端
echo "[4/4] 启动前端 (Vite)..."
cd "$PROJECT_DIR/zhida-ai-frontend"
npm run dev > /tmp/vite-frontend.log 2>&1 &
sleep 5
FRONTEND_PORT=$(cat /tmp/vite-frontend.log 2>/dev/null | grep -oE 'localhost:[0-9]+' | head -1 | cut -d: -f2)
if [ -n "$FRONTEND_PORT" ]; then
    echo "  前端已启动: http://localhost:$FRONTEND_PORT"
else
    echo "  前端启动中，请查看: cat /tmp/vite-frontend.log"
fi

echo ""
echo "=== 启动完成 ==="
echo "前端:   http://localhost:${FRONTEND_PORT:-3056}"
echo "后端:   http://localhost:8123/api"
echo "Swagger: http://localhost:8123/api/swagger-ui.html"
