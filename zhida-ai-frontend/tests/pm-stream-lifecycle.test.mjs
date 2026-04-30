import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

const content = readFileSync(new URL('../src/views/PmMode.vue', import.meta.url), 'utf8')

assert.match(content, /function\s+stopCurrentStream\s*\(/)
assert.match(content, /function\s+resetStreamingState\s*\(/)

const newSessionBody = content.match(/async function handleNewSession\(\) \{([\s\S]*?)\n\}/)?.[1] || ''
assert.match(newSessionBody, /stopCurrentStream\(\)/)
assert.match(newSessionBody, /resetStreamingState\(\)/)

const selectSessionBody = content.match(/async function handleSelectSession\(sessionId\) \{([\s\S]*?)\n\}/)?.[1] || ''
assert.match(selectSessionBody, /stopCurrentStream\(\)/)
assert.match(selectSessionBody, /resetStreamingState\(\)/)

assert.match(content, /finishStreaming\('', \[\], '生成失败，请重试。'\)/)
assert.doesNotMatch(content, /messages\.value\.push\(\{ role: 'ASSISTANT', content, sources \}\)/)
