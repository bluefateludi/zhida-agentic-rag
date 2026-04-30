# MCP 协议：AI 的 USB 接口，5 分钟搞懂

你有没有想过，为什么电脑插上任何 USB 设备都能用，但 AI 想调用一个工具却要写一堆集成代码？

Anthropic 提出了一个解决方案 — **MCP（Model Context Protocol）**，它要做的就是成为 AI 的 USB 接口。

---

## 一句话搞懂 MCP

**MCP 是一个开放协议，让 AI 模型能以标准化方式连接外部数据源和工具。**

就像 USB 统一了外设接口一样，MCP 统一了 AI 和外部工具的通信方式。

---

## 没有 MCP 的世界

假设你要让 AI 做三件事：搜索网页、查数据库、读文件。

没有 MCP：
```
AI 应用 → 自己写代码对接搜索 API
AI 应用 → 自己写代码对接数据库
AI 应用 → 自己写代码对接文件系统
```

每个工具都要单独写集成逻辑，换个项目又得重写一遍。

有了 MCP：
```
AI 应用 → MCP 客户端 → MCP 服务器（搜索）
                        MCP 服务器（数据库）
                        MCP 服务器（文件系统）
```

**一个协议搞定所有工具对接，工具还能复用。**

---

## MCP 的架构：只有 3 个角色

```
┌─────────────┐     MCP协议     ┌─────────────┐
│  AI 应用     │ ←────────────→ │  MCP Server  │ → 外部 API / 工具
│ (MCP Client) │    JSON-RPC    │ (工具提供者)  │
└─────────────┘                 └─────────────┘
```

- **Host（宿主）**：AI 应用，比如 Claude Desktop、你自己的 Spring AI 项目
- **Client（客户端）**：Host 内部和 MCP Server 通信的组件
- **Server（服务端）**：提供具体工具能力的独立服务，比如搜索、数据库查询

---

## MCP 能提供什么能力？

| 能力 | 说明 | 例子 |
|------|------|------|
| **Tools** | 让 AI 调用外部函数 | 搜索、计算、查询数据库 |
| **Resources** | 让 AI 读取外部数据 | 文件内容、数据库记录 |
| **Prompts** | 预定义的 Prompt 模板 | 标准化的任务模板 |

---

## 实战：用 Spring AI 写一个 MCP Server

光说不练假把式，我们用 Spring AI 写一个**图片搜索 MCP Server**，让任何 AI 应用都能通过 MCP 协议搜索图片。

### 第一步：加依赖

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

### 第二步：写工具逻辑

就两个注解 + 业务代码：

```java
@Service
@Tool("search image from web")  // 声明工具用途，AI 会看到这段描述
public class ImageSearchTool {

    public String searchImage(
        @ToolParam(description = "Search query keyword") String query
    ) {
        // 调用 Pexels API 搜索图片
        // 返回图片 URL 列表
        // 这里写你的业务逻辑
    }
}
```

### 第三步：注册给 MCP Server

```java
@Bean
public ToolCallbackProvider toolCallbackProvider(ImageSearchTool imageSearchTool) {
    return MethodToolCallbackProvider.builder()
            .toolObjects(imageSearchTool)
            .build();
}
```

### 第四步：配置传输方式

```yaml
# application.yml
server:
  port: 8127

spring:
  ai:
    mcp:
      server:
        type: SYNC
```

**就这么简单。** 启动服务后，任何 MCP 客户端都能发现并调用你的图片搜索工具。协议的通信、序列化、路由全由框架处理，你只管写业务逻辑。

---

## MCP 两种传输方式

| 方式 | 场景 | 说明 |
|------|------|------|
| **SSE**（HTTP 长连接） | 远程服务、微服务 | 适合部署在服务器上，通过网络调用 |
| **Stdio**（标准输入输出） | 本地进程通信 | 适合本地工具，像命令行一样使用 |

---

## MCP vs 直接调 API

有人可能会问：我直接调 API 不行吗？为什么要多一层 MCP？

| 对比 | 直接调 API | 通过 MCP |
|------|-----------|----------|
| 工具描述 | 写在代码里，AI 看不到 | 自动生成 schema，AI 能理解工具是干什么的 |
| 复用性 | 只能当前项目用 | 任何 MCP 客户端都能调用，写一次到处用 |
| 发现机制 | 手动配置 | 协议自动发现可用工具 |
| 生态 | 孤岛，每个项目自己造轮子 | 越来越多现成 MCP Server 可以直接用 |

**最大的价值是生态。** 当 MCP 标准普及后，你需要什么工具，直接找一个现成的 MCP Server 接上就行，不用自己写集成代码。

---

## MCP 的生态现状

- **Claude Desktop** 原生支持 MCP，可以直接连接 MCP Server
- **Spring AI** 内置 MCP 客户端和 Server 支持
- 社区已有大量开源 MCP Server：GitHub、Slack、数据库、搜索引擎等
- OpenAI 也在跟进兼容 MCP 协议

---

## 总结

| 你需要记住的 |
|-------------|
| MCP 是 AI 连接外部工具的标准化协议 |
| 就像 USB 统一了设备接口，MCP 统一了 AI 的工具接口 |
| 用 Spring AI 写 MCP Server 只需要两个注解 |
| 最大的价值是生态复用，写一次到处用 |

MCP 正在成为 Agent 生态的基础设施，越早了解越好。

---

> 本文示例代码来自智答 AI 项目中的 MCP 服务实践。
