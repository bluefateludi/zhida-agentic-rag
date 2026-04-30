-- 初始化 pgvector 扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- ==========================================
-- 知识库文档管理
-- ==========================================

-- 文档元数据（每个上传文件一条记录）
CREATE TABLE IF NOT EXISTS kb_document (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    file_name VARCHAR(500) NOT NULL,
    file_type VARCHAR(20) NOT NULL,
    file_size BIGINT NOT NULL DEFAULT 0,
    file_path VARCHAR(1000),
    category VARCHAR(100) DEFAULT 'default',
    status VARCHAR(20) DEFAULT 'processing',
    chunk_count INT DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 文档分块 + 向量（Spring AI PgVectorStore 默认使用此表名）
CREATE TABLE IF NOT EXISTS vector_store (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    content TEXT NOT NULL,
    metadata JSONB,
    embedding vector(1536)
);

-- 向量相似度检索索引
CREATE INDEX IF NOT EXISTS vector_store_embedding_idx
    ON vector_store USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

-- metadata 字段索引（用于按 documentId 过滤删除）
CREATE INDEX IF NOT EXISTS vector_store_metadata_idx
    ON vector_store USING gin (metadata);

-- ==========================================
-- 聊天会话管理
-- ==========================================

-- 聊天会话
CREATE TABLE IF NOT EXISTS chat_session (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(200) DEFAULT '新对话',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 聊天消息（含来源追溯）
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL REFERENCES chat_session(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    sources TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS chat_message_session_idx
    ON chat_message(session_id, created_at);

-- ==========================================
-- 问答日志（管理后台统计用）
-- ==========================================

CREATE TABLE IF NOT EXISTS query_log (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(64),
    query TEXT NOT NULL,
    answer TEXT,
    sources TEXT,
    response_time_ms INT,
    model_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 授权
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;

COMMENT ON TABLE kb_document IS '知识库文档元数据';
COMMENT ON TABLE vector_store IS '文档分块向量存储';
COMMENT ON TABLE chat_session IS '聊天会话';
COMMENT ON TABLE chat_message IS '聊天消息（含来源追溯）';
COMMENT ON TABLE query_log IS '问答审计日志';
