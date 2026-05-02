import assert from 'node:assert/strict'

import { appRoutes } from '../src/router/routes.js'

const paths = appRoutes.map(route => route.path)

assert.deepEqual(paths, ['/', '/chat', '/chat/documents', '/pm', '/report', '/documents'])

const legacyDocuments = appRoutes.find(route => route.path === '/documents')

assert.equal(legacyDocuments.redirect, '/chat/documents')

const homeRoute = appRoutes.find(route => route.path === '/')
const chatRoute = appRoutes.find(route => route.path === '/chat')
const pmRoute = appRoutes.find(route => route.path === '/pm')
const reportRoute = appRoutes.find(route => route.path === '/report')

assert.equal(homeRoute.name, 'LandingPage')
assert.equal(chatRoute.name, 'KnowledgeBase')
assert.equal(pmRoute.name, 'PmMode')
assert.equal(reportRoute.name, 'ReportMode')
assert.notEqual(homeRoute.meta.title, chatRoute.meta.title)
