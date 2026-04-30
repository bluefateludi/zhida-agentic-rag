import assert from 'node:assert/strict'

import { appRoutes } from '../src/router/routes.js'

const paths = appRoutes.map(route => route.path)

assert.deepEqual(paths, ['/', '/chat', '/chat/documents', '/pm', '/documents'])

const legacyDocuments = appRoutes.find(route => route.path === '/documents')

assert.equal(legacyDocuments.redirect, '/chat/documents')

const homeRoute = appRoutes.find(route => route.path === '/')
const chatRoute = appRoutes.find(route => route.path === '/chat')
const pmRoute = appRoutes.find(route => route.path === '/pm')

assert.equal(homeRoute.name, 'LandingPage')
assert.equal(chatRoute.name, 'KnowledgeBase')
assert.equal(pmRoute.name, 'PmMode')
assert.notEqual(homeRoute.meta.title, chatRoute.meta.title)
