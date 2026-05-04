# 智答 AI Agentic RAG

智答 AI Agentic RAG 是一个面向企业知识库、项目资料和专业文档问答场景的智能检索增强生成系统。项目以 Spring Boot 后端和 Vue 3 前端为主体，围绕知识库入库、RAG 问答、产品分析、研究报告生成、评测闭环和 Trace 可观测性构建了一套完整的 Agentic RAG 应用原型。

这个项目的重点不是只做一次问答，而是把“问题改写、知识库检索、联网研究、Agent 写作、来源追溯、失败诊断”串成可展示、可追踪、可调试的链路，适合用于课程设计、项目答辩、面试作品集和企业知识助手原型。

## 核心能力

- 知识库管理：支持上传和管理 PDF、DOCX、TXT、Markdown 等资料，后端完成解析、切片、向量化和入库。
- RAG 问答工作台：基于 pgvector 召回知识片段，将证据注入大模型上下文，支持普通问答和 SSE 流式回答。
- 来源追溯：回答结果关联文档来源、片段内容、分数和元信息，方便核验答案依据。
- 产品分析模式：面向需求评审、方案分析和产品判断，结合知识库资料与联网研究生成结构化分析。
- 研究报告模式：输入研究问题后，系统生成 ResearchBrief、知识库证据、网络证据、信息缺口和 Markdown 报告。
- RAG 评测闭环：提供评测用例列表和运行接口，用于观察回答命中、来源数量和失败情况。
- Trace Dashboard：记录 CHAT、PM、REPORT、EVAL 等链路的 trace，可按模式筛选并查看每次执行详情。
- Trace Timeline：持久化 Query Rewrite、Retrieval、Web Research、Writer Agent、Report Output 等阶段及耗时。
- 失败分析视图：对失败 trace 做前端归因，区分空证据、超时、写作失败和系统异常，并给出排查建议。

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
- Server-Sent Events

**部署**

- Docker
- Docker Compose
- Nginx

## 项目结构

```text
.
├── src/                              # Spring Boot 后端服务
├── zhida-ai-frontend/                # Vue 3 前端工作台
├── zhida-image-search-mcp-server/    # 图片搜索 MCP Server 示例
├── docs/                             # 设计文档、排障记录和开发计划
├── repository/                       # 本地知识库和运行数据目录
├── docker-compose.yml                # 容器化部署编排
├── Dockerfile                        # 后端镜像构建文件
├── init.sql                          # PostgreSQL / pgvector 初始化脚本
├── DEPLOY.md                         # 部署说明
└── DESIGN.md                         # 系统设计说明
```

## 页面入口

| 页面 | 路径 | 说明 |
| --- | --- | --- |
| 首页 | `/` | 项目能力展示和入口导航 |
| 知识库工作台 | `/chat` | 文档问答、来源引用、会话工作台 |
| 文档管理 | `/chat/documents` | 上传和管理知识库文档 |
| 产品分析模式 | `/pm` | 面向产品和需求分析的 Agentic RAG |
| 研究报告模式 | `/report` | 生成 ResearchBrief 和 Markdown 研究报告 |
| RAG 评测 | `/evaluation` | 运行评测用例并查看结果 |
| Trace Dashboard | `/traces` | 查看 trace、timeline、来源和失败分析 |

## 快速开始

### 环境要求

- JDK 21
- Maven 3.9+
- Node.js 16+
- PostgreSQL 16 + pgvector
- Docker / Docker Compose，可选

### 1. 配置环境变量

复制环境变量模板：

```bash
cp .env.example .env
```

按需配置：

```env
POSTGRES_PASSWORD=your_database_password
DASHSCOPE_API_KEY=your_dashscope_api_key
DASHSCOPE_MODEL=qwen-plus
SEARCH_API_KEY=your_search_api_key
SERVER_PORT=8123
SPRING_PROFILES_ACTIVE=prod
```

生产环境请通过环境变量或密钥管理服务注入真实密钥，避免将 `.env`、数据库密码或第三方服务 Key 提交到仓库。

### 2. 启动数据库

如果本地已有 PostgreSQL + pgvector，可按 `application-local.yml` 配置连接信息。

也可以直接使用 Docker Compose 启动完整环境：

```bash
docker compose up -d --build
```

### 3. 启动后端

```bash
mvn spring-boot:run
```

默认后端地址：

```text
http://localhost:8123/api
```

接口文档：

```text
http://localhost:8123/api/doc.html
http://localhost:8123/api/swagger-ui.html
```

### 4. 启动前端

```bash
cd zhida-ai-frontend
npm install
npm run dev
```

Vite 会输出前端访问地址，通常是：

```text
http://localhost:5173
```

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
| 产品分析 | `POST` | `/api/pm/send` | 发送非流式产品分析消息 |
| 产品分析 | `GET` | `/api/pm/stream` | 流式产品分析 |
| 产品分析 | `GET` | `/api/pm/history/{sessionId}` | 查询产品分析历史 |
| 研究报告 | `POST` | `/api/report/generate` | 生成 ResearchBrief、Markdown 报告和 traceId |
| Trace | `GET` | `/api/traces/recent` | 查询最近 trace |
| Trace | `GET` | `/api/traces/{traceId}` | 查询单次 trace 详情 |
| 评测 | `GET` | `/api/eval/cases` | 查询评测用例 |
| 评测 | `POST` | `/api/eval/run` | 运行 RAG 评测 |
| 智能体 | `GET` | `/api/ai/agent/chat` | 通用智能体对话入口 |

## 推荐联调流程

1. 启动数据库、后端和前端。
2. 打开 `/chat/documents` 上传测试文档，确认文档状态和分类。
3. 打开 `/chat` 提问，检查回答、来源卡片和 traceId。
4. 打开 `/pm` 测试产品分析链路。
5. 打开 `/report` 生成研究报告，检查 Markdown、ResearchBrief、kbEvidence、webEvidence 和 informationGaps。
6. 在报告页点击“查看本次 Trace”，跳转 `/traces?traceId=...`。
7. 在 Trace Dashboard 中查看 mode 筛选、Timeline 阶段耗时、slow 阶段高亮、来源片段和失败分析。
8. 打开 `/evaluation` 运行评测用例，观察评测结果和对应 trace。

## Trace 可观测性

系统会记录不同模式下的执行链路：

- `CHAT` / `CHAT_STREAM`：Query Rewrite、Retrieval、Answer。
- `PM` / `PM_STREAM`：Query Rewrite、Retrieval、Answer。
- `REPORT`：Query Rewrite、KB Retrieval、Web Research、Writer Agent、Report Output。
- `EVAL`：通过评测链路产生的问答记录。

Trace Dashboard 支持：

- 按 mode 筛选：ALL / CHAT / PM / REPORT / EVAL。
- 查看原始问题、改写问题、category、latencyMs、retrievalCount、sessionId。
- 优先读取后端持久化的 `traceJson.timeline`。
- 展示每个阶段的 `durationMs`，并高亮耗时最长的 slow 阶段。
- 展示 Sources 列表，便于核验召回证据。
- 对失败 trace 展示 Failure Analysis，归因为空证据、超时、写作失败或系统异常。

## 本地验证

后端测试：

```bash
mvn test
```

前端 Trace Dashboard 测试：

```bash
cd zhida-ai-frontend
node tests\trace-dashboard.test.mjs
```

前端构建：

```bash
cd zhida-ai-frontend
npm run build
```

## 配置文件

- `src/main/resources/application.yml`：通用服务、模型、OpenAPI 和日志配置。
- `src/main/resources/application-local.yml`：本地数据库与向量库配置。
- `.env.example`：容器化部署环境变量模板。
- `docker-compose.yml`：PostgreSQL、后端和前端容器编排。
- `zhida-ai-frontend/vite.config.js`：前端开发和构建配置。

## 说明

本仓库对应项目：

```text
https://github.com/bluefateludi/zhida-agentic-rag
```

本项目当前暂未声明开源许可证。正式公开发布前建议补充 `LICENSE` 文件，明确代码使用、分发和二次开发规则。
