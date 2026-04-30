import assert from 'node:assert/strict'

import {
  buildRecommendedQuestions,
  getDocumentStatusSummary
} from '../src/utils/knowledgeLoop.js'

const documents = [
  { id: 1, title: 'RAG 架构说明', category: 'rag', status: 'READY' },
  { id: 2, title: 'Agent 设计草稿', category: 'agent', status: 'PROCESSING' },
  { id: 3, title: 'MCP 接入指南', category: 'rag', status: 'READY' }
]

assert.deepEqual(getDocumentStatusSummary(documents), {
  total: 3,
  ready: 2,
  processing: 1,
  error: 0,
  readinessRate: 67,
  label: '知识库部分可用'
})

assert.deepEqual(buildRecommendedQuestions(documents), [
  'RAG 架构说明的核心结论是什么？',
  'MCP 接入指南可以支撑哪些实现细节？',
  '当前知识库里有哪些资料还不能稳定用于回答？'
])
