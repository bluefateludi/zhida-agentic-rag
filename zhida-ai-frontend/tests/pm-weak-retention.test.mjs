import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

import { appRoutes } from '../src/router/routes.js'

const pmRoute = appRoutes.find(route => route.path === '/pm')
const uiFiles = [
  'src/views/PmMode.vue',
  'src/views/LandingPage.vue',
  'src/components/Sidebar.vue'
]

assert.equal(pmRoute.name, 'PmMode')
assert.match(pmRoute.meta.title, /产品/)
assert.doesNotMatch(pmRoute.meta.title, /张一鸣/)
assert.doesNotMatch(pmRoute.meta.description, /独立.*人格|张一鸣/)

for (const file of uiFiles) {
  const content = readFileSync(new URL(`../${file}`, import.meta.url), 'utf8')
  assert.doesNotMatch(content, /张一鸣|独立人格/)
}
