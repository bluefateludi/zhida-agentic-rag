import assert from 'node:assert/strict'

import { appRoutes } from '../src/router/routes.js'

const paths = appRoutes.map(route => route.path)

assert.deepEqual(paths, ['/', '/chat', '/chat/documents', '/pm', '/report', '/evaluation', '/traces', '/documents'])

const legacyDocuments = appRoutes.find(route => route.path === '/documents')

assert.equal(legacyDocuments.redirect, '/chat/documents')

const homeRoute = appRoutes.find(route => route.path === '/')
const chatRoute = appRoutes.find(route => route.path === '/chat')
const pmRoute = appRoutes.find(route => route.path === '/pm')
const reportRoute = appRoutes.find(route => route.path === '/report')
const evaluationRoute = appRoutes.find(route => route.path === '/evaluation')
const traceRoute = appRoutes.find(route => route.path === '/traces')

assert.equal(homeRoute.name, 'LandingPage')
assert.equal(chatRoute.name, 'KnowledgeBase')
assert.equal(pmRoute.name, 'PmMode')
assert.equal(reportRoute.name, 'ReportMode')
assert.equal(evaluationRoute.name, 'EvaluationDashboard')
assert.equal(traceRoute.name, 'TraceDashboard')
assert.notEqual(homeRoute.meta.title, chatRoute.meta.title)
