import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

const pmMode = readFileSync(new URL('../src/views/PmMode.vue', import.meta.url), 'utf8')
const chatInput = readFileSync(new URL('../src/components/ChatInput.vue', import.meta.url), 'utf8')
const messageList = readFileSync(new URL('../src/components/MessageList.vue', import.meta.url), 'utf8')

const chatInputTag = pmMode.match(/<ChatInput\b[^>]*>/)?.[0] || ''

function cssBlock(content, selector) {
  const escapedSelector = selector.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return content.match(new RegExp(`${escapedSelector}\\s*\\{([^}]*)\\}`))?.[1] || ''
}

function hasDeclaration(block, property, valuePattern) {
  const escapedProperty = property.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return new RegExp(`(^|\\n)\\s*${escapedProperty}\\s*:\\s*${valuePattern}\\s*;`).test(block)
}

const pmPageBlock = cssBlock(pmMode, '.pm-page')
const pmShellBlock = cssBlock(pmMode, '.pm-shell')
const pmStageBlock = cssBlock(pmMode, '.pm-stage')
const messageListBlock = cssBlock(messageList, '.message-list')
const chatInputShellBlock = cssBlock(chatInput, '.chat-input-shell')

assert.ok(chatInputTag, 'PM mode must render ChatInput')
assert.doesNotMatch(chatInputTag, /\bv-if\b|\bv-show\b/, 'ChatInput must not be conditionally removed')
assert.match(chatInputTag, /:disabled="isStreaming"/, 'streaming should disable ChatInput instead of removing it')

assert.ok(hasDeclaration(pmPageBlock, 'height', '100vh'), 'PM page must lock to the viewport height')
assert.ok(hasDeclaration(pmPageBlock, 'overflow', 'hidden'), 'PM page itself must not scroll the input away')
assert.ok(hasDeclaration(pmShellBlock, 'height', 'calc\\(100vh - 48px\\)'), 'PM shell must provide a bounded flex height')
assert.ok(hasDeclaration(pmShellBlock, 'min-height', '0'), 'PM shell must allow its stage to shrink')
assert.ok(hasDeclaration(pmStageBlock, 'display', 'flex'))
assert.ok(hasDeclaration(pmStageBlock, 'flex-direction', 'column'))
assert.ok(hasDeclaration(pmStageBlock, 'min-height', '0'))
assert.ok(hasDeclaration(pmStageBlock, 'overflow', 'hidden'))

assert.ok(hasDeclaration(messageListBlock, 'flex', '1'))
assert.ok(hasDeclaration(messageListBlock, 'min-height', '0'))
assert.ok(hasDeclaration(messageListBlock, 'overflow-y', 'auto'))

assert.ok(hasDeclaration(chatInputShellBlock, 'flex-shrink', '0'))
