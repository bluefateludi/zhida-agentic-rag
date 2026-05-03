import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

import { appRoutes } from '../src/router/routes.js'

const apiSource = readFileSync(new URL('../src/api/index.js', import.meta.url), 'utf8')
const landingPageSource = readFileSync(new URL('../src/views/LandingPage.vue', import.meta.url), 'utf8')
const reportModeSource = readFileSync(new URL('../src/views/ReportMode.vue', import.meta.url), 'utf8')

const reportRoute = appRoutes.find(route => route.path === '/report')

assert.ok(reportRoute, 'report mode route should exist')
assert.equal(reportRoute.name, 'ReportMode')
assert.match(reportRoute.meta.title, /研究报告模式/)

assert.match(landingPageSource, /to="\/report"/, 'landing page should link to report mode')
assert.match(landingPageSource, /研究报告模式/, 'landing page should expose a report mode entry')

assert.match(apiSource, /export const generateReport =/)
assert.match(apiSource, /request\.post\('\/report\/generate'/)

assert.match(reportModeSource, /generateReport/, 'report mode should call the report API')
assert.match(reportModeSource, /reportMarkdown/, 'report mode should render markdown result')
assert.match(reportModeSource, /rewrittenQuestion/, 'report mode should expose rewritten question')
assert.match(reportModeSource, /kbEvidence/, 'report mode should expose kb evidence')
assert.match(reportModeSource, /webEvidence/, 'report mode should expose web evidence')
assert.match(reportModeSource, /informationGaps/, 'report mode should expose information gaps')
assert.match(reportModeSource, /traceId/, 'report mode should keep the generated trace id')
assert.match(reportModeSource, /res\.data\?\.traceId/, 'report mode should read trace id from API response')
assert.match(reportModeSource, /查看本次 Trace/, 'report mode should expose a direct trace navigation action')
assert.match(reportModeSource, /query:\s*\{\s*traceId\s*\}/s, 'report mode trace link should target the generated trace id')
assert.match(reportModeSource, /发送并生成报告/, 'report form should expose a clear send action')
assert.match(reportModeSource, /\.report-page\s*\{[^}]*height:\s*100vh/s, 'report page should create a viewport scroll container')
assert.match(reportModeSource, /\.report-page\s*\{[^}]*overflow-y:\s*auto/s, 'report page should allow vertical scrolling')
assert.match(reportModeSource, /\.button-row\s*\{[^}]*position:\s*sticky/s, 'report action row should remain reachable while scrolling')
