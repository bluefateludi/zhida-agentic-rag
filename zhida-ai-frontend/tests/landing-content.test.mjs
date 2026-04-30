import assert from 'node:assert/strict'

import { landingContent } from '../src/content/landingContent.js'

assert.ok(landingContent.positioning)
assert.equal(landingContent.capabilities.length, 3)
assert.equal(landingContent.demoFlow.length, 5)
assert.ok(landingContent.traceability)

assert.equal(landingContent.hero.primaryAction.href, '/chat')
assert.match(landingContent.hero.primaryAction.label, /开始体验/)

assert.match(landingContent.traceability.title, /来源|RAG|文档状态/)
assert.ok(
  landingContent.traceability.points.some(point => /RAG|检索/.test(point.title))
)
assert.ok(
  landingContent.traceability.points.some(point => /来源|证据/.test(point.title))
)
assert.ok(
  landingContent.traceability.points.some(point => /状态|文档/.test(point.title))
)
