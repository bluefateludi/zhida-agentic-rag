import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

import { appRoutes } from '../src/router/routes.js'

const routePaths = appRoutes.map(route => route.path)
assert.ok(!routePaths.includes('/chat/graph'))
assert.ok(!routePaths.includes('/graph'))

const packageJson = JSON.parse(readFileSync(new URL('../package.json', import.meta.url), 'utf8'))
assert.ok(!packageJson.dependencies['3d-force-graph'])
assert.ok(!packageJson.dependencies.three)

const apiSource = readFileSync(new URL('../src/api/index.js', import.meta.url), 'utf8')
assert.ok(!apiSource.includes('getKnowledgeGraph'))
assert.ok(!apiSource.includes('/knowledge/graph'))

const knowledgeBase = readFileSync(new URL('../src/views/KnowledgeBase.vue', import.meta.url), 'utf8')
assert.ok(!knowledgeBase.includes('openKnowledgeGraph'))
assert.ok(!knowledgeBase.includes('showGraphActions'))

const sourceCard = readFileSync(new URL('../src/components/SourceCard.vue', import.meta.url), 'utf8')
assert.ok(!sourceCard.includes('点亮图谱节点'))
assert.ok(!sourceCard.includes('locate-graph'))
