import axios from 'axios'

const API_BASE_URL = import.meta.env.PROD
  ? '/api'
  : 'http://localhost:8123/api'

const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000
})

// ==================== 文档管理 ====================

export const uploadDocument = (file, title, category) => {
  const formData = new FormData()
  formData.append('file', file)
  if (title) formData.append('title', title)
  if (category) formData.append('category', category)
  return request.post('/knowledge/document/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const listDocuments = (category) => {
  return request.get('/knowledge/document/list', {
    params: category ? { category } : {}
  })
}

export const deleteDocument = (id) => {
  return request.delete(`/knowledge/document/${id}`)
}

export const getDocument = (id) => {
  return request.get(`/knowledge/document/${id}`)
}

// ==================== 会话管理 ====================

export const createSession = (title) => {
  return request.post('/chat/session', title ? { title } : {})
}

export const listSessions = () => {
  return request.get('/chat/sessions')
}

export const renameSession = (sessionId, title) => {
  return request.put(`/chat/session/${sessionId}`, { title })
}

export const deleteSession = (sessionId) => {
  return request.delete(`/chat/session/${sessionId}`)
}

// ==================== 聊天 ====================

export const chatSend = (message, sessionId, category) => {
  return request.post('/chat/send', { message, sessionId, category })
}

/**
 * SSE 流式聊天
 */
export const chatStream = (message, sessionId, category, onMessage, onComplete, onError) => {
  const params = new URLSearchParams({ message, sessionId })
  if (category) params.append('category', category)

  const fullUrl = `${API_BASE_URL}/chat/stream?${params.toString()}`
  const eventSource = new EventSource(fullUrl)

  eventSource.onmessage = (event) => {
    if (onMessage) onMessage(event.data)
  }

  eventSource.addEventListener('complete', (event) => {
    if (!onComplete) return
    try {
      onComplete(JSON.parse(event.data))
    } catch {
      onComplete({ content: '', sources: [] })
    }
  })

  eventSource.onerror = (error) => {
    if (onError) onError(error)
    eventSource.close()
  }

  return eventSource
}

export const getChatHistory = (sessionId) => {
  return request.get(`/chat/history/${sessionId}`)
}

// ==================== PM 模式 ====================

export const createPmSession = (title) => {
  return request.post('/pm/session', title ? { title } : {})
}

export const listPmSessions = () => {
  return request.get('/pm/sessions')
}

export const deletePmSession = (sessionId) => {
  return request.delete(`/pm/session/${sessionId}`)
}

export const getPmHistory = (sessionId) => {
  return request.get(`/pm/history/${sessionId}`)
}

export const pmChatSend = (message, sessionId, category) => {
  return request.post('/pm/send', { message, sessionId, category })
}

export const pmChatStream = (message, sessionId, category, onMessage, onComplete, onError) => {
  const params = new URLSearchParams({ message, sessionId })
  if (category) params.append('category', category)

  const fullUrl = `${API_BASE_URL}/pm/stream?${params.toString()}`
  const eventSource = new EventSource(fullUrl)

  eventSource.onmessage = (event) => {
    if (onMessage) onMessage(event.data)
  }

  eventSource.addEventListener('complete', (event) => {
    if (!onComplete) return
    try {
      onComplete(JSON.parse(event.data))
    } catch {
      onComplete({ content: '', sources: [] })
    }
  })

  eventSource.onerror = (error) => {
    if (onError) onError(error)
    eventSource.close()
  }

  return eventSource
}

// ==================== 研究报告模式 ====================

export const generateReport = (question, category) => {
  return request.post('/report/generate', {
    question,
    category: category || null
  })
}

// ==================== RAG 评测 ====================

export const listEvalCases = () => {
  return request.get('/eval/cases')
}

export const runRagEvaluation = (caseIds = []) => {
  return request.post('/eval/run', { caseIds })
}

// ==================== Trace Dashboard ====================

export const listRecentTraces = (limit = 20) => {
  return request.get('/traces/recent', { params: { limit } })
}

export const getTraceDetail = (traceId) => {
  return request.get(`/traces/${traceId}`)
}

// ==================== Agent ====================

export const chatWithAgent = (message) => {
  const fullUrl = `${API_BASE_URL}/ai/agent/chat?message=${encodeURIComponent(message)}`
  const eventSource = new EventSource(fullUrl)

  return eventSource
}
