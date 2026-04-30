import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

const styleSource = readFileSync(new URL('../src/style.css', import.meta.url), 'utf8')
const indexHtml = readFileSync(new URL('../index.html', import.meta.url), 'utf8')
const logoUrl = 'https://econgencode-1379443208.cos.ap-shanghai.myqcloud.com/zhidalogo.png'
const componentFiles = [
  '../src/components/ChatRoom.vue',
  '../src/components/Sidebar.vue',
  '../src/components/MessageList.vue',
  '../src/components/ChatInput.vue',
  '../src/views/KnowledgeBase.vue',
  '../src/views/PmMode.vue',
  '../src/views/DocumentManager.vue',
  '../src/views/LandingPage.vue'
]

assert.match(styleSource, /--bg-console:/)
assert.match(styleSource, /--accent-evidence:/)
assert.match(styleSource, /--radius-panel:\s*12px/)
assert.doesNotMatch(styleSource, /radial-gradient\(circle at top,\s*rgba\(113,\s*112,\s*255/)
assert.ok(indexHtml.includes(`rel="icon" href="${logoUrl}"`))

const deprecatedStyleTokens = [
  '#007bff',
  '#0069d9',
  '#f5f5f5',
  '#e9e9eb',
  '#ddd',
  '#e0e0e0'
]

for (const file of componentFiles) {
  const source = readFileSync(new URL(file, import.meta.url), 'utf8')
  if (/LandingPage|Sidebar|PmMode/.test(file)) {
    assert.ok(source.includes(`src="${logoUrl}"`), `${file} should use the official logo image`)
  }
  for (const token of deprecatedStyleTokens) {
    assert.ok(
      !source.includes(token),
      `${file} should not use deprecated Bootstrap-era token ${token}`
    )
  }
}
