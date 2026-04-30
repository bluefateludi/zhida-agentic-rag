# 智答 AI 部署文档

## 服务器要求

- 系统：Linux，推荐 Ubuntu 20.04+ 或 CentOS 7+
- 配置：2 核 4G 以上
- 软件：Docker 20.10+，Docker Compose 2.0+

## 快速部署

### 1. 安装 Docker 和 Docker Compose

```bash
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

docker --version
docker compose version
```

### 2. 上传项目到服务器

```bash
# 使用你的仓库地址
git clone git@github.com:bluefateludi/zhida-ai-agent.git
cd zhida-ai-agent

# 或者从本地上传
# scp -r ./zhida-ai-agent root@your-server-ip:/root/
```

如果本地目录已经叫 `zhida-ai-agent`，也可以直接进入现有目录运行部署命令；目录名不影响服务运行。

### 3. 配置环境变量

```bash
cp .env.example .env
vim .env
```

必需配置项：

```env
POSTGRES_PASSWORD=your_strong_password_here
DASHSCOPE_API_KEY=sk-your-api-key-here
DASHSCOPE_MODEL=qwen-plus
```

可选配置项：

```env
SEARCH_API_KEY=your_searchapi_key
SERVER_PORT=8123
SPRING_PROFILES_ACTIVE=prod
```

### 4. 启动服务

```bash
docker compose up -d --build
docker compose ps
docker compose logs -f
```

### 5. 验证部署

```bash
curl http://localhost:8123/api/actuator/health
```

访问地址：

```text
前端：http://your-server-ip
后端：http://your-server-ip:8123/api
接口文档：http://your-server-ip:8123/api/doc.html
```

## 服务管理

```bash
docker compose ps
docker compose logs -f backend
docker compose logs -f postgres
docker compose restart backend
docker compose down
```

停止并删除数据卷会清空数据库：

```bash
docker compose down -v
```

## 性能优化

### 限制容器资源

```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '1.5'
          memory: 2G
        reservations:
          cpus: '0.5'
          memory: 512M
```

### PostgreSQL 参数

```sql
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;
```

### JVM 参数

```yaml
backend:
  environment:
    JAVA_OPTS: >-
      -Xms512m
      -Xmx1536m
      -XX:+UseG1GC
      -XX:MaxGCPauseMillis=200
      -XX:+HeapDumpOnOutOfMemoryError
```

## 常见问题

### 容器启动失败

```bash
docker compose logs backend
netstat -tunlp | grep 8123
netstat -tunlp | grep 5432
```

### 数据库连接失败

```bash
docker compose exec postgres pg_isready -U postgres -d postgres
docker compose logs postgres
```

### 内存不足

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

### API Key 无效

- 检查 `.env` 中的 `DASHSCOPE_API_KEY`
- 确认模型服务额度和权限
- 查看后端日志：`docker compose logs backend`

## 安全建议

1. 修改 `.env` 中的默认数据库密码。
2. 不要提交真实 API Key。
3. 只开放必要端口。
4. 生产环境建议配置 HTTPS。
5. 定期备份数据库。

备份和恢复示例：

```bash
docker compose exec postgres pg_dump -U postgres postgres > backup.sql
docker compose exec -T postgres psql -U postgres postgres < backup.sql
```

## 更新部署

```bash
git pull
docker compose up -d --build
docker image prune -a
```

## 技术支持

请在当前项目仓库提交 Issue，或在项目文档中补充部署环境、日志和复现步骤。
