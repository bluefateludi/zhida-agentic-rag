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
