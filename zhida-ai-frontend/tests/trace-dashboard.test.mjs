import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

import { appRoutes } from '../src/router/routes.js'

const apiSource = readFileSync(new URL('../src/api/index.js', import.meta.url), 'utf8')
const landingPageSource = readFileSync(new URL('../src/views/LandingPage.vue', import.meta.url), 'utf8')
const knowledgeBaseSource = readFileSync(new URL('../src/views/KnowledgeBase.vue', import.meta.url), 'utf8')
const reportSource = readFileSync(new URL('../src/views/ReportMode.vue', import.meta.url), 'utf8')
const evaluationSource = readFileSync(new URL('../src/views/EvaluationDashboard.vue', import.meta.url), 'utf8')
const traceSource = readFileSync(new URL('../src/views/TraceDashboard.vue', import.meta.url), 'utf8')

const traceRoute = appRoutes.find(route => route.path === '/traces')

assert.ok(traceRoute, 'trace dashboard route should exist')
assert.equal(traceRoute.name, 'TraceDashboard')
assert.match(traceRoute.meta.title, /Trace Dashboard|Trace 仪表盘/)

assert.match(apiSource, /export const listRecentTraces =/)
assert.match(apiSource, /export const getTraceDetail =/)
assert.match(apiSource, /request\.get\('\/traces\/recent'/)
assert.match(apiSource, /request\.get\(`\/traces\/\$\{traceId\}`/)

assert.match(landingPageSource, /to="\/traces"/, 'landing page should link to trace dashboard')
assert.match(knowledgeBaseSource, /to="\/traces"/, 'workspace should link to trace dashboard')
assert.match(reportSource, /to="\/traces"/, 'report mode should link to trace dashboard')
assert.match(evaluationSource, /to="\/traces"/, 'evaluation dashboard should link to trace dashboard')

assert.match(traceSource, /listRecentTraces/, 'dashboard should load recent traces')
assert.match(traceSource, /getTraceDetail/, 'dashboard should load selected trace detail')
assert.match(traceSource, /useRoute/, 'dashboard should read route query parameters')
assert.match(traceSource, /route\.query\.traceId/, 'dashboard should support opening a requested trace id')
assert.match(traceSource, /requestedTraceId/, 'dashboard should prioritize the trace id from the report page')
assert.match(traceSource, /recentTraces/, 'dashboard should keep recent trace state')
assert.match(traceSource, /modeOptions/, 'dashboard should define mode filter options')
assert.match(traceSource, /selectedMode/, 'dashboard should keep selected mode state')
assert.match(traceSource, /filteredTraces/, 'dashboard should filter traces by selected mode')
assert.match(traceSource, /v-for="trace in filteredTraces"/, 'dashboard list should render filtered traces')
assert.match(traceSource, /\.trace-row\.report/, 'dashboard should visually highlight report traces')
assert.match(traceSource, /selectedTrace\?\.mode === 'REPORT'/, 'dashboard should expose report-specific detail styling')
assert.match(traceSource, /Trace Timeline/, 'dashboard should render a trace timeline section')
assert.match(traceSource, /traceTimeline/, 'dashboard should derive timeline steps for the selected trace')
assert.match(traceSource, /v-for="step in traceTimeline"/, 'dashboard should render every timeline step')
assert.match(traceSource, /Query Rewrite/, 'timeline should include query rewrite stage')
assert.match(traceSource, /KB Retrieval/, 'report timeline should include kb retrieval stage')
assert.match(traceSource, /Web Research/, 'report timeline should include web research stage')
assert.match(traceSource, /Writer Agent/, 'report timeline should include writer agent stage')
assert.match(traceSource, /Report Output/, 'report timeline should include report output stage')
assert.match(traceSource, /Retrieval/, 'chat and pm timeline should include retrieval stage')
assert.match(traceSource, /Answer/, 'chat and pm timeline should include answer stage')
assert.match(traceSource, /const status = selectedTrace\.value\?\.success === false \? 'blocked' : 'done'/, 'timeline should expose a status for each stage')
assert.match(traceSource, /evidenceCount/, 'timeline should expose evidence counts where available')
assert.match(traceSource, /durationLabel/, 'timeline should expose duration or duration placeholder metadata')
assert.match(traceSource, /selectedTrace/, 'dashboard should keep selected trace state')
assert.match(traceSource, /originalQuery/, 'dashboard should render original query')
assert.match(traceSource, /rewrittenQuery/, 'dashboard should render rewritten query')
assert.match(traceSource, /latencyMs/, 'dashboard should render latency')
assert.match(traceSource, /retrievalCount/, 'dashboard should render retrieval count')
assert.match(traceSource, /sourcesJson/, 'dashboard should parse persisted sources')
assert.match(
  traceSource,
  /运行 `\/chat`、`\/pm`、`\/report` 或 `\/evaluation` 后/,
  'dashboard empty state should tell users report mode also produces traces'
)
assert.match(traceSource, /\.trace-page\s*\{\s*\n\s*height:\s*100vh/s, 'trace page should create a viewport scroll container')
assert.match(traceSource, /\.trace-page\s*\{[^}]*overflow-y:\s*auto/s, 'trace page should allow vertical scrolling')
