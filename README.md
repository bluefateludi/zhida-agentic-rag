# 智答 Agentic RAG

智答 Agentic RAG 是一个面向企业知识库、项目文档和专业资料问答场景的智能检索增强生成系统。项目围绕“文档接入、向量检索、流式问答、引用追溯、产品分析和评测闭环”构建，提供从知识入库到问答验证的一体化能力。

系统采用 Spring Boot + Spring AI + pgvector 构建后端知识检索与智能体编排能力，使用 Vue 3 + Vite 构建前端工作台，适合用于个人知识库、企业内部文档助手、研发资料问答、产品需求分析和智能体应用原型。

## 项目亮点

- 文档知识库：支持 PDF、DOCX、TXT、Markdown 等文件上传、解析、切片、向量化和状态管理。
- RAG 问答链路：基于 pgvector 进行语义检索，将知识片段注入大模型上下文，降低无依据回答风险。
- 流式对话体验：通过 SSE 返回模型生成过程，前端支持连续对话、会话历史和工作台式交互。
- 引用来源追溯：回答可关联文档来源、片段内容和元信息，便于核验答案依据。
- 产品分析模式：面向需求评审、方案判断和竞品分析，结合知识库与联网研究输出结构化分析。
- 评测闭环：内置评测用例与运行接口，可用于验证问答效果、引用质量和检索链路稳定性。
- 工具扩展能力：保留联网搜索、网页抓取、资源下载、文件操作、终端调用和 PDF 生成等智能体工具。
- MCP 服务示例：包含图片搜索 MCP Server，可作为外部工具接入智能体流程。

## 技术栈

**后端**

- Java 21
- Spring Boot 3.4
- Spring AI / Spring AI Alibaba
- LangChain4j
- PostgreSQL 16 + pgvector
- JPA / JDBC
- Knife4j / OpenAPI
- PDFBox / Apache POI / iText / Jsoup

**前端**

- Vue 3
- Vite
- Vue Router
- Axios
- Marked / Highlight.js / DOMPurify
- SSE 流式渲染

**部署**

- Docker
- Docker Compose
- Nginx

## 项目结构

```text
.
├── src/                              # Spring Boot 后端服务
├── zhida-ai-frontend/                # Vue 3 前端工作台
├── zhida-image-search-mcp-server/    # 图片搜索 MCP 服务示例
├── docs/                             # 设计文档、排障记录和开发说明
├── repository/                       # 本地知识库与运行数据目录
├── docker-compose.yml                # 容器化部署编排
├── Dockerfile                        # 后端镜像构建文件
├── init.sql                          # PostgreSQL / pgvector 初始化脚本
├── DEPLOY.md                         # 部署说明
└── DESIGN.md                         # 系统设计说明
```

## 快速开始

### 环境要求

- JDK 21
- Maven 3.9+
- Node.js 16+
- PostgreSQL 16 + pgvector
- Docker / Docker Compose，可选

### 配置环境变量

复制环境变量模板：

```bash
cp .env.example .env
```

按需配置以下变量：

```env
POSTGRES_PASSWORD=your_database_password
DASHSCOPE_API_KEY=your_dashscope_api_key
DASHSCOPE_MODEL=qwen-plus
SEARCH_API_KEY=your_search_api_key
SERVER_PORT=8123
SPRING_PROFILES_ACTIVE=prod
```

生产环境请通过环境变量或密钥管理服务注入真实密钥，避免将 `.env`、数据库密码或第三方服务 Key 提交到仓库。

### 启动后端

```bash
mvn spring-boot:run
```

默认服务地址：

```text
http://localhost:8123/api
```

接口文档地址：

```text
http://localhost:8123/api/doc.html
http://localhost:8123/api/swagger-ui.html
```

### 启动前端

```bash
cd zhida-ai-frontend
npm install
npm run dev
```

前端开发服务默认由 Vite 启动，实际端口以终端输出为准。

### Docker Compose 部署

```bash
docker compose up -d --build
```

更多部署细节见 [DEPLOY.md](DEPLOY.md)。

## 核心接口

接口统一挂载在 `/api` 前缀下。

| 模块 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 健康检查 | `GET` | `/api/health` | 服务健康状态 |
| 文档管理 | `POST` | `/api/knowledge/document/upload` | 上传知识库文档 |
| 文档管理 | `GET` | `/api/knowledge/document/list` | 查询文档列表 |
| 文档管理 | `GET` | `/api/knowledge/document/{id}` | 查询文档详情 |
| 知识问答 | `POST` | `/api/chat/session` | 创建问答会话 |
| 知识问答 | `GET` | `/api/chat/sessions` | 查询会话列表 |
| 知识问答 | `POST` | `/api/chat/send` | 发送非流式消息 |
| 知识问答 | `GET` | `/api/chat/stream` | 流式知识问答 |
| 知识问答 | `GET` | `/api/chat/history/{sessionId}` | 查询会话历史 |
| 产品分析 | `POST` | `/api/pm/session` | 创建产品分析会话 |
| 产品分析 | `GET` | `/api/pm/stream` | 流式产品分析 |
| 智能体 | `GET` | `/api/ai/agent/chat` | 智能体对话入口 |
| 评测 | `GET` | `/api/eval/cases` | 查询评测用例 |
| 评测 | `POST` | `/api/eval/run` | 运行问答评测 |

## 前端功能

- 项目首页与能力展示
- 知识库文档上传和管理
- RAG 聊天工作台
- SSE 流式回答展示
- 引用来源卡片
- 产品分析工作区
- 评测结果展示

## 配置文件

- `src/main/resources/application.yml`：通用服务、模型、OpenAPI 和日志配置。
- `src/main/resources/application-local.yml`：本地数据库与向量库配置。
- `.env.example`：容器化部署环境变量模板。
- `docker-compose.yml`：数据库、后端和前端容器编排。

## 本地验证

后端测试：

```bash
mvn test
```

前端构建：

```bash
cd zhida-ai-frontend
npm run build
```

## 开发规划

- 完善文档解析队列和失败重试机制。
- 增加更多检索评测指标和可视化报告。
- 优化长文档引用定位与片段高亮体验。
- 扩展 MCP 工具接入与智能体任务编排能力。
- 增加生产环境权限控制、租户隔离和审计日志。

## License

本项目当前暂未声明开源许可证。正式公开发布前建议补充 `LICENSE` 文件，明确代码使用、分发和二次开发规则。
