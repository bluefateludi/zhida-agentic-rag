import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

import { appRoutes } from '../src/router/routes.js'

const apiSource = readFileSync(new URL('../src/api/index.js', import.meta.url), 'utf8')
const landingPageSource = readFileSync(new URL('../src/views/LandingPage.vue', import.meta.url), 'utf8')
const knowledgeBaseSource = readFileSync(new URL('../src/views/KnowledgeBase.vue', import.meta.url), 'utf8')
const evaluationSource = readFileSync(new URL('../src/views/EvaluationDashboard.vue', import.meta.url), 'utf8')

const evaluationRoute = appRoutes.find(route => route.path === '/evaluation')

assert.ok(evaluationRoute, 'evaluation dashboard route should exist')
assert.equal(evaluationRoute.name, 'EvaluationDashboard')
assert.match(evaluationRoute.meta.title, /RAG 评测/)

assert.match(landingPageSource, /to="\/evaluation"/, 'landing page should link to evaluation dashboard')
assert.match(knowledgeBaseSource, /to="\/evaluation"/, 'workspace should link to evaluation dashboard')

assert.match(apiSource, /export const listEvalCases =/)
assert.match(apiSource, /export const runRagEvaluation =/)
assert.match(apiSource, /request\.get\('\/eval\/cases'/)
assert.match(apiSource, /request\.post\('\/eval\/run'/)

assert.match(evaluationSource, /listEvalCases/, 'dashboard should load golden-set cases')
assert.match(evaluationSource, /runRagEvaluation/, 'dashboard should run evaluation')
assert.match(evaluationSource, /caseCount/, 'dashboard should show case count')
assert.match(evaluationSource, /passCount/, 'dashboard should show pass count')
assert.match(evaluationSource, /failedResults/, 'dashboard should expose failed cases')
assert.match(evaluationSource, /actualAnswer/, 'dashboard should render answer preview')
assert.match(evaluationSource, /sourceCount/, 'dashboard should show source count')
