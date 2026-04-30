# `/chat/send` 与 `/pm/send` 卡住不返回排障记录

## 1. 问题现象

- `/chat/sessions` 正常
- `/pm/sessions` 正常
- `/chat/send` 调用后长时间不返回
- `/pm/send` 调用后长时间不返回

本次排查要求是不先假设大模型本身异常，而是按调用链逐段验证：

1. QueryRewriter
2. ChatService / PmChatService
3. RAG
4. 会话记忆
5. 联网研究

## 2. 关键结论

根因定位在 **QueryRewriter 的同步查询重写调用**。

具体来说，`QueryRewriter.doQueryRewrite()` 内部直接调用：

```java
Query transformedQuery = queryTransformer.transform(query);
```

这一步是一个外部模型调用，但当前实现：

- 没有超时控制
- 没有异常回退
- 没有降级到原始问题

因此只要查询重写阶段阻塞，`/chat/send` 和 `/pm/send` 都会一起卡住，因为这两个接口都在主流程早期同步调用了 QueryRewriter。

## 3. 调用链验证结果

### 3.1 QueryRewriter

`ChatService` 在保存用户消息后，第一步公共外部调用就是：

```java
String rewrittenQuery = queryRewriter.doQueryRewrite(message);
```

当前 `QueryRewriter` 实现位于：

- `src/main/java/com/zhida/aiagent/rag/QueryRewriter.java`

实现特点：

- `RewriteQueryTransformer` 基于 `ChatModel`
- `doQueryRewrite()` 同步执行
- 无 timeout / fallback

这是本次公共阻塞点。

### 3.2 ChatService / PmChatService

`ChatService` 的同步路径是：

1. `saveMessage(sessionId, USER, ...)`
2. `queryRewriter.doQueryRewrite(message)`
3. `createRagAdvisor(...)`
4. `chatClient.prompt().call().chatResponse()`

排查时通过实际请求验证到：

- `POST /chat/send` 超时
- 该 session 的 history 里只写入了 `USER` 消息
- 没有 `ASSISTANT` 消息

这说明：

- 控制器没有卡住
- 数据库消息持久化已完成
- 阻塞发生在 `saveMessage()` 之后

而 `saveMessage()` 之后的第一个公共外部调用就是 QueryRewriter，因此可以把问题收敛到查询重写阶段。

`/pm/send` 在调试分支中的流程同样先调用 QueryRewriter，再走联网研究和主对话，所以它与 `/chat/send` 一样卡住，符合共享阻塞点特征。

说明：

- `/pm/send` 相关代码在本次调试时存在于备份 worktree 分支中
- 主工作区已按需求回退，不再保留该分支代码
- 备份 worktree 路径：`.worktrees/codex-demo-save-20260423`

### 3.3 RAG

RAG 不是本次首个阻塞点。

原因：

- `createRagAdvisor(...)` 发生在 QueryRewriter 之后
- 实际现象已经证明请求在更早阶段就挂起
- 因此 RAG 即使后续仍需优化，也不是这次“两个接口同时卡住”的首因

### 3.4 会话记忆

会话记忆不是本次根因。

依据：

- `/chat/sessions`、`/pm/sessions` 正常
- 发消息后 `USER` 消息已经成功落库
- `DatabaseChatMemoryRepository` 本身是数据库读写封装，不包含额外的外部网络调用

这说明会话记忆和消息存储链路能跑通，不是导致请求悬挂的第一现场。

### 3.5 联网研究

联网研究不是本次公共首因。

依据：

- `/chat/send` 本身不依赖联网研究，但同样卡住
- `/pm/send` 虽然依赖 `WebResearchService`，但其前面已经先调用 QueryRewriter
- 两个接口一起表现为相同的超时模式，更符合“共享前置步骤阻塞”，而不是 PM 独有的联网研究问题

## 4. 复现实证

排查时做过以下验证：

### 4.1 两个发送接口都稳定超时

以 20 秒超时调用：

- `POST /api/chat/send`
- `POST /api/pm/send`

两者都出现超时，不是单一路径问题。

### 4.2 历史消息中只看到 USER 消息

调用超时后查询 history，能看到本轮用户消息已经写入数据库，但没有 assistant 回复。

这说明阻塞发生在：

- 控制器入参解析之后
- `saveMessage(USER)` 之后
- `saveMessage(ASSISTANT)` 之前

### 4.3 共享调用链前缀完全一致

`/chat/send` 与 `/pm/send` 共享的前缀步骤里，唯一显式外部模型调用是 QueryRewriter，因此该点最符合“共同卡住”的事实。

## 5. 建议修复

建议保留 QueryRewriter，但必须补上超时与回退：

1. 给 `queryTransformer.transform(...)` 增加超时控制
2. 超时或异常时直接返回原始问题 `prompt`
3. 记录明确日志，例如：
   - 查询重写开始
   - 查询重写成功
   - 查询重写超时，已回退原问题
   - 查询重写异常，已回退原问题
4. 超时时间做成配置项，例如：
   - `chat.query-rewrite-timeout-ms=3000`

建议实现策略：

- 单独线程执行重写
- `Future#get(timeout, TimeUnit.MILLISECONDS)` 或等价机制做超时控制
- `TimeoutException` / `InterruptedException` / 运行时异常统一回退到原始问题

## 6. 推荐补充测试

至少补三类测试：

1. QueryRewriter 正常返回时，输出改写结果
2. QueryRewriter 抛异常时，回退原始问题
3. QueryRewriter 超时时，回退原始问题

另外建议补接口级回归测试：

1. `/chat/send` 在查询重写失败时仍能继续主流程
2. `/pm/send` 在查询重写失败时仍能继续主流程

## 7. 这次没有合入主工作区的原因

本次排障过程中已经在独立 worktree 中开始了修复和测试草稿，但你要求先回退主工作区到上一个提交用于演示，因此主工作区没有继续保留该修复。

相关备份已经保存在：

- worktree: `D:\\Program code\\java\\react-agent\\zhida-ai-agent\\.worktrees\\codex-demo-save-20260423`
- branch: `codex-demo-save-20260423`
- commit: `de89572`

后续如果要继续修复，建议直接基于该 worktree 接着做，而不是在当前回退后的主工作区上重头再来。
