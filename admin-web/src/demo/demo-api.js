// Installs an in-browser mock backend so the admin console can be previewed
// without a server (GitHub Pages / tunnel demo). Enabled by VITE_DEMO=1 only.
import {
  audits,
  builtinTools,
  community,
  dashboard,
  dataSources,
  dynamicTools,
  prompts,
  requestConfigs,
  resourceMarkdown,
  resources,
  roles,
  skillMarkdown,
  skills,
  tokens,
  users
} from './demo-fixtures.js'

const json = (data) =>
  new Response(JSON.stringify({ code: 200, data }), {
    status: 200,
    headers: { 'Content-Type': 'application/json; charset=utf-8' }
  })

const text = (body) =>
  new Response(body, { status: 200, headers: { 'Content-Type': 'text/markdown; charset=utf-8' } })

const GET_ROUTES = {
  '/api/admin/dashboard': () => dashboard,
  '/api/admin/users': () => users,
  '/api/admin/roles': () => roles,
  '/api/admin/users/role-relations': () => [],
  '/api/admin/tokens': () => tokens,
  '/api/admin/token-selections': () => [],
  '/api/admin/token-prompt-selections': () => [],
  '/api/admin/token-resource-selections': () => [],
  '/api/admin/role-tools': () => [],
  '/api/admin/role-prompts': () => [],
  '/api/admin/role-resources': () => [],
  '/api/admin/request-configs': () => requestConfigs,
  '/api/admin/data-sources': () => dataSources,
  '/api/admin/resources': () => resources,
  '/api/admin/dynamic-tools': () => dynamicTools,
  '/api/admin/prompt-templates': () => prompts,
  '/api/admin/skills': () => skills,
  '/api/admin/audits': () => audits,
  '/api/admin/audit-logs': () => audits,
  '/api/share/tokens': () => tokens,
  '/api/share/community/skills': () => community.skills,
  '/api/share/community/tools': () => community.tools,
  '/api/share/community/builtin-tools': () => builtinTools,
  '/api/share/community/feishu-tools': () => [],
  '/api/share/community/prompts': () => community.prompts,
  '/api/share/community/resources': () => community.resources,
  '/api/share/community/apis': () => community.apis,
  '/api/share/studio/apis': () => requestConfigs,
  '/api/share/studio/skills': () => skills,
  '/api/share/studio/tools': () => dynamicTools,
  '/api/share/studio/prompts': () => prompts,
  '/api/share/studio/resources': () => resources
}

const PRESIGN_SKILL = /^\/api\/share\/studio\/skills\/[^/]+\/presign-download$/
const PRESIGN_RESOURCE = /^\/api\/share\/studio\/resources\/[^/]+\/presign-download$/
const DEMO_FILE = /^demo:\/\/(skill|resource)$/

export function installDemoApi() {
  const originalFetch = window.fetch.bind(window)

  window.fetch = async (input, init = {}) => {
    const raw = typeof input === 'string' ? input : input?.url || ''
    const url = new URL(raw, window.location.origin)
    const path = url.pathname
    const method = (init.method || (typeof input === 'object' && input?.method) || 'GET').toUpperCase()

    if (DEMO_FILE.test(raw)) {
      return text(raw.endsWith('skill') ? skillMarkdown : resourceMarkdown)
    }

    if (!path.startsWith('/api/')) {
      return originalFetch(input, init)
    }

    if (path === '/api/admin/auth/login' && method === 'POST') {
      return json({ token: 'demo-token', user: users[0] })
    }

    if (PRESIGN_SKILL.test(path)) {
      return json({ downloadUrl: 'demo://skill', fileName: 'SKILL.md' })
    }

    if (PRESIGN_RESOURCE.test(path)) {
      return json({ downloadUrl: 'demo://resource', fileName: 'resource.md' })
    }

    if (method === 'GET') {
      const route = GET_ROUTES[path]
      if (route) {
        return json(route())
      }
      return json([])
    }

    // Uploads and mutations are accepted so buttons behave; nothing is persisted.
    return json(true)
  }

  // eslint-disable-next-line no-console
  console.info('[mcpforge] demo mode: API responses are mocked in the browser')
}
