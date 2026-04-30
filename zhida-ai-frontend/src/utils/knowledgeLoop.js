export function getDocumentStatusSummary(documents = []) {
  const total = documents.length
  const ready = documents.filter(doc => doc.status === 'READY').length
  const processing = documents.filter(doc => doc.status === 'PROCESSING').length
  const error = documents.filter(doc => doc.status === 'ERROR').length
  const readinessRate = total ? Math.round((ready / total) * 100) : 0

  return {
    total,
    ready,
    processing,
    error,
    readinessRate,
    label: resolveStatusLabel(total, ready, processing, error)
  }
}

export function buildRecommendedQuestions(documents = []) {
  const readyDocuments = documents.filter(doc => doc.status === 'READY')
  const blockedDocuments = documents.filter(doc => doc.status && doc.status !== 'READY')
  const questions = readyDocuments.slice(0, 2).map((doc, index) => {
    const title = doc.title || doc.fileName || doc.category || `文档 ${index + 1}`
    return index === 0
      ? `${title}的核心结论是什么？`
      : `${title}可以支撑哪些实现细节？`
  })

  if (blockedDocuments.length) {
    questions.push('当前知识库里有哪些资料还不能稳定用于回答？')
  }

  if (!questions.length) {
    return [
      '这个知识库目前适合回答哪些问题？',
      '请概括当前资料的关键主题。',
      '哪些文档状态会影响回答质量？'
    ]
  }

  return questions.slice(0, 3)
}

function resolveStatusLabel(total, ready, processing, error) {
  if (!total) return '等待接入文档'
  if (error > 0 && ready === 0) return '索引存在异常'
  if (ready === total) return '知识库可直接用于演示'
  if (processing > 0 && ready === 0) return '知识索引构建中'
  return '知识库部分可用'
}
