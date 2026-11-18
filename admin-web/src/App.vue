<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { ADMIN_TOKEN_KEY, UNAUTHORIZED_EVENT, api } from './api'
import LoginView from './LoginView.vue'
import Icon from './ui/Icon.vue'
import ToastHost from './ui/ToastHost.vue'
import { toast } from './ui/toast'

const page = ref(pageFromPath())
const loggedIn = ref(Boolean(localStorage.getItem(ADMIN_TOKEN_KEY)))
const loading = ref(false)
const sidebarCollapsed = ref(localStorage.getItem('bear_mcp_admin_sidebar') === 'collapsed')
const theme = ref(localStorage.getItem('bear_mcp_admin_theme') || 'dark')
const navQuery = ref('')

function applyTheme() {
  document.documentElement.classList.toggle('dark', theme.value === 'dark')
  localStorage.setItem('bear_mcp_admin_theme', theme.value)
}

function toggleTheme() {
  theme.value = theme.value === 'dark' ? 'light' : 'dark'
}

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
  localStorage.setItem('bear_mcp_admin_sidebar', sidebarCollapsed.value ? 'collapsed' : 'expanded')
}

watch(theme, applyTheme, { immediate: true })
const rows = ref([])
const dashboard = ref({})
const roles = ref([])
const roleTools = ref([])
const rolePrompts = ref([])
const roleResources = ref([])
const userRoles = ref([])
const tokenSelections = ref([])
const tokenPromptSelections = ref([])
const tokenResourceSelections = ref([])
const dynamicToolOptions = ref([])
const feishuToolOptions = ref([])
const promptOptions = ref([])
const resourceOptions = ref([])
const toolPermissionOpen = ref(false)
const promptPermissionOpen = ref(false)
const resourcePermissionOpen = ref(false)
const tokenSelectionOpen = ref(false)
const tokenPromptSelectionOpen = ref(false)
const tokenResourceSelectionOpen = ref(false)
const userRoleOpen = ref(false)
const activeUser = ref(null)
const activeRole = ref(null)
const activeToken = ref(null)
const selectedUserRoles = ref([])
const selectedRoleTools = ref([])
const selectedRolePrompts = ref([])
const selectedRoleResources = ref([])
const selectedTokenTools = ref([])
const selectedTokenPrompts = ref([])
const selectedTokenResources = ref([])
const users = ref([])
const requestConfigs = ref([])
const dataSourceConfigs = ref([])
const drawer = ref(false)
const model = ref({})
const rawToken = ref('')
const oneTimeTokenOpen = ref(false)
const oneTimeTokenName = ref('')
const dynamicToolDetailOpen = ref(false)
const activeDynamicTool = ref(null)
const communityDetailOpen = ref(false)
const activeCommunityItem = ref(null)
const communityResourceContent = ref('')
const communityResourceContentLoading = ref(false)
const communityResourceContentError = ref('')
const auditDetailOpen = ref(false)
const activeAuditLog = ref(null)
const apiDebugOpen = ref(false)
const activeStudioApi = ref(null)
const debugParams = ref('{}')
const debugResult = ref(null)
const debugLoading = ref(false)
const apiSaving = ref(false)
const dataSourceTesting = ref(false)
const apiEditorTab = ref('body')
const apiEditorDebugParams = ref('{}')
const apiKeyword = ref('')
const apiStatusFilter = ref('all')
const skillKeyword = ref('')
const skillStatusFilter = ref('all')
const skillPreviewText = ref('')
const skillPreviewLoading = ref(false)
const skillPreviewHtml = computed(() => renderMarkdown(skillPreviewText.value))
const skillSaving = ref(false)
const skillSelectedFile = ref(null)
const toolKeyword = ref('')
const toolStatusFilter = ref('all')
const promptKeyword = ref('')
const promptStatusFilter = ref('all')
const resourceKeyword = ref('')
const resourceStatusFilter = ref('all')
const resourcePreviewText = ref('')
const resourcePreviewLoading = ref(false)
const resourcePreviewHtml = computed(() => renderMarkdown(resourcePreviewText.value))
const promptPreviewArgs = ref('{}')
const promptPreviewResult = ref(null)
const promptPreviewLoading = ref(false)
const promptSaving = ref(false)
const resourceSaving = ref(false)
const resourceSelectedFile = ref(null)
const promptArgumentItems = ref([])
const toolDebugParams = ref('{\n  "pageNum": 1,\n  "pageSize": 10\n}')
const toolDebugResult = ref(null)
const toolDebugLoading = ref(false)
const toolSaving = ref(false)
const lastAutoToolScript = ref('')
const lastAutoToolSchema = ref('')
const communityTools = ref([])
const communityBuiltinTools = ref([])
const communityApis = ref([])
const communitySkills = ref([])
const communityPrompts = ref([])
const communityResources = ref([])
const communityKeyword = ref('')
const communityToolTypeFilter = ref('all')
const activeConfigTokenId = ref(null)
const builtinToolFallback = [
  { name: 'hello', description: '问候工具' },
  { name: 'current_time', description: '当前时间' },
  { name: 'system_info', description: '系统信息' },
  { name: 'calculate', description: '数学计算' },
  { name: 'create_request_config', description: '创建 API 请求配置' },
  { name: 'list_request_configs', description: '查询 API 请求配置' },
  { name: 'create_dynamic_tool', description: '创建动态 Tool 草稿' },
  { name: 'list_dynamic_tools', description: '查询动态 Tool 配置' },
  { name: 'update_dynamic_tool_script', description: '更新动态 Tool 脚本' },
  { name: 'create_resource', description: '直接创建或更新 Markdown Resource' },
  { name: 'list_mcp_prompts', description: '列出当前 Token 可见的 MCP Prompts' },
  { name: 'list_mcp_resources', description: '列出当前 Token 可见的 MCP Resources' },
  { name: 'read_mcp_resource', description: '读取指定 MCP Resource 正文' },
  { name: 'create_prompt', description: '直接创建或更新 MCP Prompt 模板' },
  { name: 'render_prompt', description: '渲染已授权 MCP Prompt' },
  { name: 'create_skill', description: '直接创建或更新 Bear Skill' },
  { name: 'list_data_sources', description: '查询已发布数据源' },
  { name: 'query_data_source', description: '只读查询数据源' },
  { name: 'get_skill', description: '按 Skill ID 获取可安装的 Cursor Skill 文件' }
]
const builtinTools = ref([...builtinToolFallback])
const tokenPermissionOptions = [
  { label: '工具列表 + 工具调用', value: '["mcp:tools:read","mcp:tools:call"]' },
  { label: '仅查看工具列表', value: '["mcp:tools:read"]' }
]
const activeStatusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]
const requestTypeOptions = [
  { label: 'HTTP', value: 'HTTP' },
  { label: 'MOCK', value: 'MOCK' }
]
const httpMethodOptions = [
  { label: 'GET', value: 'GET' },
  { label: 'POST', value: 'POST' },
  { label: 'PUT', value: 'PUT' },
  { label: 'DELETE', value: 'DELETE' }
]
const publishStatusOptions = [
  { label: '草稿', value: 0 },
  { label: '上架不公开', value: 1 },
  { label: '公开', value: 2 }
]
const dataSourceDbTypeOptions = [
  { label: 'MYSQL', value: 'MYSQL' },
  { label: 'TIDB', value: 'TIDB' }
]
const dataSourcePublishStatusOptions = [
  { label: '草稿', value: 0 },
  { label: '已发布', value: 1 }
]
const menu = [
  ['dashboard', '概览', 'dashboard'], ['users', '用户', 'users'], ['roles', '角色与能力权限', 'shield'], ['tokens', 'Token 与能力选择', 'key'],
  ['requests', '请求配置', 'settings'], ['dataSources', '数据源', 'database'], ['resources', '资源', 'layers'], ['tools', '动态工具', 'zap'], ['audits', '审计日志', 'activity']
]
const communityPages = ['shareHome', 'shareSkills', 'shareTools', 'sharePrompts', 'shareResources', 'shareApis', 'shareConfig', 'shareTokens']
const studioPages = ['studioHome', 'studioSkills', 'studioSkillEdit', 'studioTools', 'studioToolEdit', 'studioPrompts', 'studioPromptEdit', 'studioResources', 'studioResourceEdit', 'studioApis', 'studioApiEdit']
const sharePages = [...communityPages, ...studioPages]
const isSharePage = computed(() => sharePages.includes(page.value))
const isCommunityPage = computed(() => communityPages.includes(page.value))
const navSections = { dashboard: '概览', users: '系统治理', roles: '系统治理', tokens: '访问控制', requests: '能力展示', dataSources: '能力展示', resources: '资源', tools: '能力展示', audits: '运行观测', shareHome: '首页', shareSkills: 'Skills 社区', shareTools: 'MCP Tools', sharePrompts: 'MCP Prompts', shareResources: 'MCP Resources', shareApis: 'API 能力', shareConfig: '我的MCP配置', shareTokens: 'Token 管理', studioHome: '首页', studioSkills: 'Skills 创作', studioTools: 'Tools 创作', studioPrompts: 'Prompts 创作', studioResources: 'Resources 创作', studioApis: 'API 创作' }
const title = computed(() => ({ dashboard:'运行概览', users:'用户', roles:'角色与能力权限', tokens:'Token 与能力选择', requests:'请求配置', dataSources:'数据源', resources:'资源', tools:'动态工具', audits:'调用审计', shareHome:'发现优质 AI 能力', shareSkills:'Skills 社区', shareTools:'MCP Tools', sharePrompts:'MCP Prompts', shareResources:'MCP Resources', shareApis:'API 能力', shareConfig:'我的MCP配置', shareTokens:'Token 管理', studioHome:'Bear 创作空间', studioSkills:'Skills 创作', studioSkillEdit:'新建 Skill', studioTools:'Tools 创作', studioToolEdit:'新建 Tool', studioPrompts:'Prompts 创作', studioPromptEdit:'新建 Prompt', studioResources:'Resources 创作', studioResourceEdit:'新建 Resource', studioApis:'API 创作', studioApiEdit:'新建 API' })[page.value])
const desc = computed(() => ({ dashboard:'当前数据库中的 MCP 治理状态', users:'角色是工具和 Prompt 权限上限，用户通过角色获得资格', roles:'角色决定资格上限，Tool、Prompt、Resource 都在这里授权', tokens:'每把 Token 单独选择要暴露和实际允许使用的 Tool / Prompt / Resource', requests:'展示动态工具可引用的企业请求配置；完整创作在创作空间完成', dataSources:'维护外部数据库连接配置，为后续 query_data_source 和动态 Tool runSql 提供受控数据入口', resources:'展示已创建的 MCP Resource，发布并授权后可通过 resources/list 和 resources/read 读取', tools:'这里只展示已发布的动态工具；创建与编辑在创作空间完成', audits:'保留每一次 MCP Tool / Prompt / Resource 调用的结果摘要与耗时', shareHome:'公开资源会先进入社区，被团队发现、复用，再进入 Token 配置链路。', shareSkills:'浏览已公开的 Bear Skill，Agent 可通过 get_skill 安装使用。', shareTools:'浏览已公开的 MCP Tool。能否调用仍由角色权限和 Token 工具选择决定。', sharePrompts:'浏览已公开的 MCP Prompt 模板。能否获取仍由角色权限和 Token Prompt 选择决定。', shareResources:'浏览已公开的 MCP Resource。能否读取仍由角色权限和 Token Resource 选择决定。', shareApis:'浏览已公开的 API 配置，它们是动态 Tool 编排时可复用的基础能力。', shareConfig:'选择一把 Token，配置它实际暴露的 Tool、Prompt 和 Resource，并一键安装到 Cursor。', shareTokens:'每个人可以创建多把自己的 MCP Token，用于不同客户端、项目或环境。', studioHome:'创作 Skills、Tools、Prompts、Resources、API，分享到社区', studioSkills:'上传 Markdown Skill，让 Agent 通过 get_skill 安装到 Cursor 本地', studioSkillEdit:'上传 SKILL.md 并维护 Skill ID、名称、描述和发布状态', studioTools:'把已接入的 API 配置包装成 AI Agent 可见和可调用的 MCP Tool', studioToolEdit:'编写工具描述、入参 Schema 和 Groovy 脚本，调试通过后发布上线', studioPrompts:'沉淀企业工作流模板，指导 Agent 按标准流程使用工具', studioPromptEdit:'编写 Prompt 模板、参数和建议工具，预览渲染后发布', studioResources:'上传 Markdown 资源，让 Agent 通过 resources/list 和 resources/read 读取企业知识片段', studioResourceEdit:'上传 Markdown 文件并维护 Resource URI、名称、描述和发布状态', studioApis:'创建外部 HTTP API 配置，调试通过后发布给后续动态工具使用', studioApiEdit:'配置外部 HTTP API，保存并调试真实响应' })[page.value])
const studioApiStats = computed(() => {
  const all = rows.value.length
  const online = rows.value.filter(item => Number(item.publishStatus) !== 0).length
  const draft = rows.value.filter(item => Number(item.publishStatus) === 0).length

  return { all, online, draft }
})
const debugSummary = computed(() => {
  if (!debugResult.value) {
    return null
  }

  return {
    success: Boolean(debugResult.value.success),
    status: debugResult.value.result?.status,
    durationMs: debugResult.value.durationMs,
    errorMessage: debugResult.value.errorMessage
  }
})
const debugBodyText = computed(() => {
  if (!debugResult.value) {
    return '点击「发送」后显示响应体'
  }
  if (!debugResult.value.success) {
    return debugResult.value.errorMessage || '调试失败，请检查请求配置'
  }

  const body = debugResult.value.result?.body
  if (body == null || body === '') {
    return '响应体为空'
  }

  return prettyJsonText(body)
})
const filteredStudioApis = computed(() => {
  const keyword = apiKeyword.value.trim().toLowerCase()

  return rows.value.filter(item => {
    const publishStatus = Number(item.publishStatus)
    const matchStatus =
      apiStatusFilter.value === 'all'
      || (apiStatusFilter.value === 'online' && publishStatus !== 0)
      || (apiStatusFilter.value === 'draft' && publishStatus === 0)

    const searchText = [
      item.requestId,
      item.configKey,
      item.name,
      item.url,
      item.description
    ].filter(Boolean).join(' ').toLowerCase()

    return matchStatus && (!keyword || searchText.includes(keyword))
  })
})
const filteredStudioTools = computed(() => {
  const keyword = toolKeyword.value.trim().toLowerCase()

  return rows.value.filter(item => {
    const publishStatus = Number(item.publishStatus)
    const matchStatus =
      toolStatusFilter.value === 'all'
      || (toolStatusFilter.value === 'online' && publishStatus !== 0)
      || (toolStatusFilter.value === 'draft' && publishStatus === 0)

    const searchText = [
      item.toolName,
      item.toolDescription,
      item.linkedRequestKeys,
      item.groovyScript
    ].filter(Boolean).join(' ').toLowerCase()

    return matchStatus && (!keyword || searchText.includes(keyword))
  })
})
const filteredStudioPrompts = computed(() => {
  const keyword = promptKeyword.value.trim().toLowerCase()

  return rows.value.filter(item => {
    const publishStatus = Number(item.publishStatus)
    const matchStatus =
      promptStatusFilter.value === 'all'
      || (promptStatusFilter.value === 'online' && publishStatus !== 0)
      || (promptStatusFilter.value === 'draft' && publishStatus === 0)

    const searchText = [
      item.promptName,
      item.title,
      item.description,
      item.templateContent,
      item.linkedToolNames
    ].filter(Boolean).join(' ').toLowerCase()

    return matchStatus && (!keyword || searchText.includes(keyword))
  })
})
const filteredStudioSkills = computed(() => {
  const keyword = skillKeyword.value.trim().toLowerCase()

  return rows.value.filter(item => {
    const publishStatus = Number(item.publishStatus)
    const matchStatus =
      skillStatusFilter.value === 'all'
      || (skillStatusFilter.value === 'online' && publishStatus !== 0)
      || (skillStatusFilter.value === 'draft' && publishStatus === 0)

    const searchText = [
      item.skillCode,
      item.name,
      item.description,
      item.category,
      item.fileName,
      item.objectKey
    ].filter(Boolean).join(' ').toLowerCase()

    return matchStatus && (!keyword || searchText.includes(keyword))
  })
})
const filteredStudioResources = computed(() => {
  const keyword = resourceKeyword.value.trim().toLowerCase()

  return rows.value.filter(item => {
    const publishStatus = Number(item.publishStatus)
    const matchStatus =
      resourceStatusFilter.value === 'all'
      || (resourceStatusFilter.value === 'online' && publishStatus !== 0)
      || (resourceStatusFilter.value === 'draft' && publishStatus === 0)

    const searchText = [
      item.resourceUri,
      item.name,
      item.description,
      item.fileName,
      item.objectKey
    ].filter(Boolean).join(' ').toLowerCase()

    return matchStatus && (!keyword || searchText.includes(keyword))
  })
})
const communityDynamicToolSource = computed(() => {
  const source = page.value === 'shareTools' ? rows.value : communityTools.value
  return source.map(item => ({
    ...item,
    communityType: 'dynamic',
    displayName: item.toolName,
    displayDescription: item.toolDescription,
    displayCategory: '动态工具',
    scriptLanguage: item.scriptLanguage || 'Groovy'
  }))
})
const communityBuiltinToolSource = computed(() => {
  return communityBuiltinTools.value.map(item => ({
    ...item,
    linkedRequestKeys: '[]',
    communityType: 'builtin',
    displayName: item.toolName,
    displayDescription: item.toolDescription,
    displayCategory: item.category,
    scriptLanguage: item.scriptLanguage || 'Java @Tool'
  }))
})
const filteredCommunityTools = computed(() => {
  const source = [
    ...communityDynamicToolSource.value,
    ...communityBuiltinToolSource.value
  ].filter(item => {
    return communityToolTypeFilter.value === 'all' || item.communityType === communityToolTypeFilter.value
  })
  const keyword = communityKeyword.value.trim().toLowerCase()

  return source.filter(item => {
    const searchText = [
      item.displayName,
      item.displayDescription,
      item.displayCategory,
      item.linkedRequestKeys,
      item.inputSchema
    ].filter(Boolean).join(' ').toLowerCase()

    return !keyword || searchText.includes(keyword)
  })
})
const filteredCommunityApis = computed(() => {
  const source = page.value === 'shareApis' ? rows.value : communityApis.value
  const keyword = communityKeyword.value.trim().toLowerCase()

  return source.filter(item => {
    const searchText = [
      item.requestId,
      item.configKey,
      item.name,
      item.description,
      item.category,
      item.method
    ].filter(Boolean).join(' ').toLowerCase()

    return !keyword || searchText.includes(keyword)
  })
})
const filteredCommunitySkills = computed(() => {
  const source = page.value === 'shareSkills' ? rows.value : communitySkills.value
  const keyword = communityKeyword.value.trim().toLowerCase()

  return source.filter(item => {
    const searchText = [
      item.skillCode,
      item.name,
      item.description,
      item.category,
      item.fileName
    ].filter(Boolean).join(' ').toLowerCase()

    return !keyword || searchText.includes(keyword)
  })
})
const filteredCommunityPrompts = computed(() => {
  const source = page.value === 'sharePrompts' ? rows.value : communityPrompts.value
  const keyword = communityKeyword.value.trim().toLowerCase()

  return source.filter(item => {
    const searchText = [
      item.promptName,
      item.title,
      item.description,
      item.argumentsSchema,
      item.templateContent,
      item.linkedToolNames
    ].filter(Boolean).join(' ').toLowerCase()

    return !keyword || searchText.includes(keyword)
  })
})
const filteredCommunityResources = computed(() => {
  const source = page.value === 'shareResources' ? rows.value : communityResources.value
  const keyword = communityKeyword.value.trim().toLowerCase()

  return source.filter(item => {
    const searchText = [
      item.resourceUri,
      item.name,
      item.description,
      item.mimeType,
      item.fileName
    ].filter(Boolean).join(' ').toLowerCase()

    return !keyword || searchText.includes(keyword)
  })
})
const rankedCommunitySkills = computed(() => rankCommunityItems(filteredCommunitySkills.value))
const rankedCommunityTools = computed(() => rankCommunityItems(filteredCommunityTools.value))
const rankedCommunityPrompts = computed(() => rankCommunityItems(filteredCommunityPrompts.value))
const rankedCommunityResources = computed(() => rankCommunityItems(filteredCommunityResources.value))
const communityRankSections = computed(() => [
  { key: 'skills', title: 'Skill 点赞榜', page: 'shareSkills', items: rankedCommunitySkills.value.slice(0, 5), name: item => item.name || item.skillCode || '未命名 Skill', code: item => item.skillCode || communityCode('SKILL', item.id), open: openCommunitySkillDetail },
  { key: 'tools', title: 'Tool 点赞榜', page: 'shareTools', items: rankedCommunityTools.value.slice(0, 5), name: item => item.displayName || item.toolName || '未命名 Tool', code: item => communityToolCode(item), open: openCommunityToolDetail },
  { key: 'prompts', title: 'Prompt 点赞榜', page: 'sharePrompts', items: rankedCommunityPrompts.value.slice(0, 5), name: item => item.title || item.promptName || '未命名 Prompt', code: item => item.promptName || communityCode('PROMPT', item.id), open: openCommunityPromptDetail },
  { key: 'resources', title: 'Resource 点赞榜', page: 'shareResources', items: rankedCommunityResources.value.slice(0, 5), name: item => item.name || item.resourceUri || '未命名 Resource', code: item => item.resourceUri || communityCode('RES', item.id), open: openCommunityResourceDetail }
])
const communityDetailTitle = computed(() => {
  if (!activeCommunityItem.value) {
    return '能力详情'
  }
  if (activeCommunityItem.value.communityKind === 'skill') {
    return `Skill 详情 · ${activeCommunityItem.value.name || activeCommunityItem.value.skillCode || ''}`
  }
  if (activeCommunityItem.value.communityKind === 'api') {
    return `API 能力详情 · ${activeCommunityItem.value.configKey || activeCommunityItem.value.name || ''}`
  }
  if (activeCommunityItem.value.communityKind === 'prompt') {
    return `MCP Prompt 详情 · ${activeCommunityItem.value.title || activeCommunityItem.value.promptName || ''}`
  }
  if (activeCommunityItem.value.communityKind === 'resource') {
    return `MCP Resource 详情 · ${activeCommunityItem.value.name || activeCommunityItem.value.resourceUri || ''}`
  }
  return `MCP Tool 详情 · ${activeCommunityItem.value.displayName || activeCommunityItem.value.toolName || ''}`
})
const selectedToolRequestKeys = computed({
  get() {
    return parseJsonArray(model.value.linkedRequestKeys)
  },
  set(value) {
    model.value.linkedRequestKeys = JSON.stringify(value, null, 2)
  }
})
const availableToolApis = computed(() => {
  return requestConfigs.value.filter(item => Number(item.publishStatus) !== 0)
})
const toolApiOptions = computed(() => {
  return availableToolApis.value.map(item => ({
    label: `${item.configKey} ${item.name || ''}`,
    value: item.configKey,
    title: item.url || item.description || item.name || item.configKey
  }))
})
const selectedToolDataSourceIds = computed({
  get() {
    return parseJsonArray(model.value.linkedDataSourceIds).map(Number).filter(Number.isFinite)
  },
  set(value) {
    model.value.linkedDataSourceIds = JSON.stringify(value.map(Number), null, 2)
  }
})
const availableToolDataSources = computed(() => {
  return dataSourceConfigs.value.filter(item => Number(item.publishStatus) === 1)
})
const toolDataSourceOptions = computed(() => {
  return availableToolDataSources.value.map(item => ({
    label: `${item.id} ${item.name || item.datasourceKey || ''}`,
    value: item.id,
    title: item.description || item.jdbcUrl || item.name || String(item.id)
  }))
})
const selectedPromptToolNames = computed({
  get() {
    return parseJsonArray(model.value.linkedToolNames)
  },
  set(value) {
    model.value.linkedToolNames = JSON.stringify(value, null, 2)
  }
})
const promptToolOptions = computed(() => {
  const dynamicOptions = dynamicToolOptions.value.map(item => ({
    label: `${item.toolName} 动态工具`,
    value: item.toolName,
    title: item.toolDescription || item.toolName
  }))
  const builtinOptions = builtinTools.value.map(item => ({
    label: `${item.name} 内置工具`,
    value: item.name,
    title: item.description
  }))
  return [...dynamicOptions, ...builtinOptions]
})
const toolDebugText = computed(() => {
  if (!toolDebugResult.value) {
    return '点击「运行调试」后显示脚本返回结果'
  }

  return JSON.stringify(normalizeDebugResult(toolDebugResult.value), null, 2)
})
const drawerTitle = computed(() => {
  if (page.value === 'studioApis') {
    return model.value.id ? '编辑 HTTP API' : '新建 HTTP API'
  }
  return `${model.value.id ? '编辑' : '新建'}${title.value.replace('与工具权限','').replace('与工具选择','').replace('与能力权限','').replace('与能力选择','')}`
})
const userOptions = computed(() => users.value.map(user => ({
  label: `${user.displayName || user.username}（${user.username}）`,
  value: user.id
})))
const columns = computed(() => ({
 users:[['username','用户名'],['displayName','展示名称'],['roleCodes','角色'],['isEnabled','状态']],
 tokens:[['tokenName','Token 名称'],['userId','所属用户'],['permissions','访问范围'],['tokenPrefix','展示前缀'],['isActive','状态'],['expireTime','过期时间']],
 requests:[['requestId','接口 ID'],['configKey','配置 Key'],['name','名称'],['type','协议'],['publishStatus','发布'],['isEnabled','状态']],
 dataSources:[['name','名称'],['datasourceKey','数据源 Key'],['dbType','类型'],['jdbcUrl','JDBC URL'],['username','用户名'],['passwordSet','密码'],['publishStatus','发布']],
 resources:[['resourceUri','Resource URI'],['name','名称'],['description','描述'],['mimeType','MIME 类型'],['publishStatus','发布'],['enabled','状态']],
 tools:[['toolName','工具名'],['toolDescription','工具描述'],['linkedRequestKeys','API 白名单'],['linkedDataSourceIds','数据源白名单'],['publishStatus','发布'],['inputSchema','入参 Schema'],['groovyScript','脚本摘要'],['enabled','状态']],
 audits:[['createTime','调用时间'],['userName','用户'],['capabilityType','类型'],['capabilityName','名称/标识'],['capabilityAction','动作'],['status','状态'],['durationMs','耗时(ms)']],
 roles:[['roleCode','角色编码'],['roleName','角色名称'],['isEnabled','状态']]
})[page.value] || [])
const dataColumns = computed(() => columns.value.map(([dataIndex,title]) => ({
  title,
  dataIndex,
  ellipsis:true,
  customRender: ({ text, record }) => formatTableCell(dataIndex, text, record)
})))

const tableQuery = ref('')
const tableRows = computed(() => {
  const keyword = tableQuery.value.trim().toLowerCase()
  if (!keyword) {
    return rows.value
  }
  return rows.value.filter(row => String(formatTableCell(columns.value[0]?.[0], row[columns.value[0]?.[0]], row) || '')
    .toLowerCase()
    .includes(keyword) || Object.values(row).some(value => String(value ?? '').toLowerCase().includes(keyword)))
})

function pageFromPath() {
  const path = window.location.pathname
  if (path === '/share/studio/skills') {
    return 'studioSkills'
  }
  if (path === '/share/studio/skills/edit') {
    return 'studioSkillEdit'
  }
  if (path === '/share/studio/tools') {
    return 'studioTools'
  }
  if (path === '/share/studio/tools/edit') {
    return 'studioToolEdit'
  }
  if (path === '/share/studio/prompts') {
    return 'studioPrompts'
  }
  if (path === '/share/studio/prompts/edit') {
    return 'studioPromptEdit'
  }
  if (path === '/share/studio/resources') {
    return 'studioResources'
  }
  if (path === '/share/studio/resources/edit') {
    return 'studioResourceEdit'
  }
  if (path === '/share/studio/apis') {
    return 'studioApis'
  }
  if (path === '/share/studio/apis/edit') {
    return 'studioApiEdit'
  }
  if (path === '/share/tools') {
    return 'shareTools'
  }
  if (path === '/share/skills') {
    return 'shareSkills'
  }
  if (path === '/share/prompts') {
    return 'sharePrompts'
  }
  if (path === '/share/resources') {
    return 'shareResources'
  }
  if (path === '/share/apis') {
    return 'shareApis'
  }
  if (path === '/share/config') {
    return 'shareConfig'
  }
  if (path === '/share/tokens') {
    return 'shareTokens'
  }
  if (path === '/share/studio') {
    return 'studioHome'
  }
  if (path === '/share') {
    return 'shareHome'
  }
  return 'dashboard'
}

function pathForPage(key) {
  return {
    shareHome: '/share',
    shareSkills: '/share/skills',
    shareTools: '/share/tools',
    sharePrompts: '/share/prompts',
    shareResources: '/share/resources',
    shareApis: '/share/apis',
    shareConfig: '/share/config',
    shareTokens: '/share/tokens',
    studioHome: '/share/studio',
    studioSkills: '/share/studio/skills',
    studioSkillEdit: '/share/studio/skills/edit',
    studioTools: '/share/studio/tools',
    studioToolEdit: '/share/studio/tools/edit',
    studioPrompts: '/share/studio/prompts',
    studioPromptEdit: '/share/studio/prompts/edit',
    studioResources: '/share/studio/resources',
    studioResourceEdit: '/share/studio/resources/edit',
    studioApis: '/share/studio/apis',
    studioApiEdit: '/share/studio/apis/edit',
    dashboard: '/admin'
  }[key] || '/admin'
}

function formatTableCell(dataIndex, text, record) {
  if (dataIndex === 'roleCodes' && page.value === 'users') {
    return userRoleLabel(record.id)
  }
  if (dataIndex === 'userId' && page.value === 'tokens') {
    return userLabel(text)
  }
  if (dataIndex === 'permissions' && page.value === 'tokens') {
    return permissionLabel(text)
  }
  if (dataIndex === 'linkedRequestKeys' && page.value === 'tools') {
    return linkedRequestLabel(text)
  }
  if (dataIndex === 'linkedDataSourceIds' && page.value === 'tools') {
    return linkedDataSourceLabel(text)
  }
  if (dataIndex === 'inputSchema' && page.value === 'tools') {
    return compactText(text, 42)
  }
  if (dataIndex === 'groovyScript' && page.value === 'tools') {
    return compactText(text, 48)
  }
  if (dataIndex === 'jdbcUrl' && page.value === 'dataSources') {
    return compactText(text, 56)
  }
  if (dataIndex === 'passwordSet' && page.value === 'dataSources') {
    return text ? '已设置' : '未设置'
  }
  if (dataIndex === 'capabilityType' && page.value === 'audits') {
    return capabilityTypeLabel(text)
  }
  if (dataIndex === 'capabilityAction' && page.value === 'audits') {
    return capabilityActionLabel(text)
  }
  if (dataIndex === 'capabilityName' && page.value === 'audits') {
    return compactText(text, 48)
  }
  if (dataIndex === 'publishStatus' && page.value === 'resources') {
    return publishLabel(text)
  }
  if (dataIndex === 'enabled' && page.value === 'resources') {
    return Number(text) === 1 ? '启用' : '禁用'
  }
  if (dataIndex === 'isEnabled' || dataIndex === 'isActive' || dataIndex === 'enabled') {
    return Number(text) === 1 ? '启用' : '禁用'
  }
  if (dataIndex === 'publishStatus') {
    if (page.value === 'dataSources') {
      return Number(text) === 1 ? '已发布' : '草稿'
    }
    return ['草稿','上架不公开','公开'][Number(text)] || '-'
  }
  return text ?? '-'
}

function capabilityTypeLabel(type) {
  return ({ TOOL: 'Tool', PROMPT: 'Prompt', RESOURCE: 'Resource' })[type] || type || '-'
}

function capabilityActionLabel(action) {
  return ({ CALL: '调用', LIST: '列表', GET: '获取', READ: '读取' })[action] || action || '-'
}

function userLabel(userId) {
  const user = users.value.find(item => item.id === userId)
  if (!user) {
    return userId ?? '-'
  }
  return `${user.displayName || user.username}（${user.username}）`
}

function permissionLabel(value) {
  const normalized = normalizePermissions(value)
  const option = tokenPermissionOptions.find(item => item.value === normalized)
  return option?.label || '未配置'
}

function linkedRequestLabel(value) {
  const keys = parseJsonArray(value)
  if (!keys.length) {
    return '未绑定'
  }

  return keys.map(key => {
    const config = requestConfigs.value.find(item => item.configKey === key)
    return config ? `${key}（${config.name}）` : key
  }).join('，')
}

function linkedDataSourceLabel(value) {
  const ids = parseJsonArray(value).map(Number).filter(Number.isFinite)
  if (!ids.length) {
    return '未绑定'
  }

  return ids.map(id => {
    const dataSource = dataSourceConfigs.value.find(item => Number(item.id) === id)
    return dataSource ? `${id}（${dataSource.name || dataSource.datasourceKey}）` : String(id)
  }).join('，')
}

function parseJsonArray(value) {
  if (!value) {
    return []
  }
  if (Array.isArray(value)) {
    return value
  }
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed : []
  } catch (error) {
    return String(value).split(',').map(item => item.trim()).filter(Boolean)
  }
}

function compactText(value, maxLength) {
  if (!value) {
    return '-'
  }
  const text = String(value).replace(/\s+/g, ' ').trim()
  return text.length > maxLength ? text.slice(0, maxLength) + '...' : text
}

function communityCode(prefix, id) {
  const value = id == null ? 0 : Number(id)
  return `${prefix}${String(value).padStart(10, '0')}`
}

function communityToolCode(item) {
  return item.communityType === 'builtin'
    ? item.toolName
    : communityCode('TOOL', item.id)
}

function communityToolCountLabel(item) {
  if (item.communityType === 'builtin') {
    return item.displayCategory || '内置工具'
  }
  return `${parseJsonArray(item.linkedRequestKeys).length} 个 API · ${parseJsonArray(item.linkedDataSourceIds).length} 个数据源`
}

function rankCommunityItems(items) {
  return [...items].sort((left, right) => {
    const likeDiff = Number(right.likeCount || 0) - Number(left.likeCount || 0)
    if (likeDiff !== 0) return likeDiff
    return Number(right.id || 0) - Number(left.id || 0)
  })
}

function likeLabel(item) {
  return `${item?.liked ? '♥' : '♡'} ${Number(item?.likeCount || 0)}`
}

async function toggleCommunityLike(item) {
  if (!item?.likeTargetType || !item?.likeTargetKey) {
    toast.warning('这个能力暂时不能点赞')
    return
  }
  try {
    const result = await api('/api/share/community/likes/toggle', {
      method: 'POST',
      body: {
        targetType: item.likeTargetType,
        targetKey: item.likeTargetKey
      }
    })
    applyCommunityLikeResult(result)
  } catch (error) {
    toast.error(error.message || '点赞失败')
  }
}

function applyCommunityLikeResult(result) {
  const patchItem = item => {
    if (item?.likeTargetType === result.targetType && item?.likeTargetKey === result.targetKey) {
      item.likeCount = result.likeCount
      item.liked = result.liked
    }
  }
  ;[rows.value, communitySkills.value, communityTools.value, communityBuiltinTools.value, communityPrompts.value, communityResources.value].forEach(list => {
    list.forEach(patchItem)
  })
  patchItem(activeCommunityItem.value)
}

function applyBuiltinTools(items) {
  if (!Array.isArray(items) || !items.length) {
    return
  }
  builtinTools.value = items.map(item => ({
    name: item.toolName || item.name,
    description: item.toolDescription || item.description || item.name || item.toolName
  })).filter(item => item.name)
}

async function loadCommunityHomeData() {
  const requests = [
    ['skills', api('/api/share/community/skills')],
    ['tools', api('/api/share/community/tools')],
    ['builtinTools', api('/api/share/community/builtin-tools')],
    ['prompts', api('/api/share/community/prompts')],
    ['resources', api('/api/share/community/resources')],
    ['apis', api('/api/share/community/apis')]
  ]
  const result = await Promise.allSettled(requests.map(([, request]) => request))
  const empty = []
  communitySkills.value = result[0].status === 'fulfilled' ? result[0].value : empty
  communityTools.value = result[1].status === 'fulfilled' ? result[1].value : empty
  communityBuiltinTools.value = result[2].status === 'fulfilled' ? result[2].value : empty
  applyBuiltinTools(communityBuiltinTools.value)
  communityPrompts.value = result[3].status === 'fulfilled' ? result[3].value : empty
  communityResources.value = result[4].status === 'fulfilled' ? result[4].value : empty
  communityApis.value = result[5].status === 'fulfilled' ? result[5].value : empty

  const failed = result
    .map((item, index) => item.status === 'rejected' ? requests[index][0] : null)
    .filter(Boolean)
  if (failed.length) {
    toast.warning(`部分社区数据加载失败：${failed.join('、')}`)
  }
}

async function loadShareConfigData() {
  const [tokens, selections, promptSelections, resourceSelections, dynamicTools, builtinToolList, feishuTools, prompts, resources] = await Promise.all([
    api('/api/share/tokens'),
    api('/api/share/tokens/selections'),
    api('/api/share/tokens/prompt-selections'),
    api('/api/share/tokens/resource-selections'),
    api('/api/share/community/tools'),
    api('/api/share/community/builtin-tools'),
    api('/api/share/community/feishu-tools'),
    api('/api/share/community/prompts'),
    api('/api/share/community/resources')
  ])
  rows.value = tokens
  tokenSelections.value = selections
  tokenPromptSelections.value = promptSelections
  tokenResourceSelections.value = resourceSelections
  dynamicToolOptions.value = dynamicTools.map(item => ({ ...item, enabled: 1 }))
  communityBuiltinTools.value = builtinToolList
  applyBuiltinTools(builtinToolList)
  feishuToolOptions.value = feishuTools
  promptOptions.value = prompts
  resourceOptions.value = resources
  if (!activeConfigTokenId.value && rows.value.length) {
    activeConfigTokenId.value = rows.value[0].id
  }
  applyConfigTokenSelections()
}

function applyConfigTokenSelections() {
  const tokenId = activeConfigTokenId.value
  selectedTokenTools.value = tokenSelections.value
    .filter(item => item.tokenId === tokenId && item.enabled === 1)
    .map(item => item.toolName)
  selectedTokenPrompts.value = tokenPromptSelections.value
    .filter(item => item.tokenId === tokenId && item.enabled === 1)
    .map(item => item.promptName)
  selectedTokenResources.value = tokenResourceSelections.value
    .filter(item => item.tokenId === tokenId && item.enabled === 1)
    .map(item => item.resourceUri)
}

async function saveShareMcpConfig() {
  if (!activeConfigTokenId.value) {
    toast.warning('请先选择 Token')
    return
  }
  const tools = selectedTokenTools.value.map(toolName => ({
    toolName,
    toolType: toolTypeOf(toolName),
    enabled: 1
  }))
  const prompts = selectedTokenPrompts.value.map(promptName => ({ promptName, enabled: 1 }))
  const resources = selectedTokenResources.value.map(resourceUri => ({ resourceUri, enabled: 1 }))
  await Promise.all([
    api(`/api/share/tokens/${activeConfigTokenId.value}/selections`, { method: 'PUT', body: { tools } }),
    api(`/api/share/tokens/${activeConfigTokenId.value}/prompt-selections`, { method: 'PUT', body: { prompts } }),
    api(`/api/share/tokens/${activeConfigTokenId.value}/resource-selections`, { method: 'PUT', body: { resources } })
  ])
  toast.success('MCP 配置已保存')
  await loadShareConfigData()
}

async function installShareConfigToCursor() {
  if (!activeConfigTokenId.value) {
    toast.warning('请先选择 Token')
    return
  }
  await saveShareMcpConfig()
  const baseUrl = window.location.origin
  const result = await api(`/api/share/tokens/${activeConfigTokenId.value}/cursor-deeplink?baseUrl=${encodeURIComponent(baseUrl)}`)
  window.location.href = result.deeplink
}

function openCommunityToolDetail(item) {
  activeCommunityItem.value = {
    ...item,
    communityKind: 'tool'
  }
  communityDetailOpen.value = true
}

function openCommunityApiDetail(item) {
  activeCommunityItem.value = {
    ...item,
    communityKind: 'api'
  }
  communityDetailOpen.value = true
}

function openCommunitySkillDetail(item) {
  activeCommunityItem.value = {
    ...item,
    communityKind: 'skill'
  }
  communityDetailOpen.value = true
}

function openCommunityPromptDetail(item) {
  activeCommunityItem.value = {
    ...item,
    communityKind: 'prompt'
  }
  communityDetailOpen.value = true
}

async function openCommunityResourceDetail(item) {
  activeCommunityItem.value = {
    ...item,
    communityKind: 'resource'
  }
  communityDetailOpen.value = true
  communityResourceContent.value = ''
  communityResourceContentError.value = ''
  communityResourceContentLoading.value = true
  try {
    communityResourceContent.value = await api(`/api/share/community/resources/${item.id}/content`) || ''
    if (!communityResourceContent.value) {
      communityResourceContentError.value = '未读取到 Resource 内容，可能文件为空或对象存储暂不可用。'
    }
  } catch (error) {
    communityResourceContentError.value = error.message || 'Resource 内容读取失败'
  } finally {
    communityResourceContentLoading.value = false
  }
}

function prettyJsonText(value) {
  if (typeof value !== 'string') {
    return JSON.stringify(value, null, 2)
  }

  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch (error) {
    return value
  }
}

function normalizeDebugResult(value) {
  if (Array.isArray(value)) {
    return value.map(item => normalizeDebugResult(item))
  }
  if (value && typeof value === 'object') {
    const result = {}
    Object.entries(value).forEach(([key, item]) => {
      if (key === 'body' && typeof item === 'string') {
        result[key] = parseMaybeJson(item)
      } else {
        result[key] = normalizeDebugResult(item)
      }
    })
    return result
  }
  if (typeof value === 'string') {
    return parseMaybeJson(value)
  }
  return value
}

function parseMaybeJson(value) {
  const text = value.trim()
  if (!text) {
    return value
  }
  if (!['{', '['].includes(text[0])) {
    return value
  }

  try {
    return normalizeDebugResult(JSON.parse(text))
  } catch (error) {
    return value
  }
}

function userRoleLabel(userId) {
  const codes = userRoles.value
    .filter(item => item.userId === userId)
    .map(item => item.roleCode)

  if (!codes.length) {
    return '未分配'
  }

  return codes.map(code => {
    const role = roles.value.find(item => item.roleCode === code)
    return role ? `${role.roleName}（${code}）` : code
  }).join('，')
}

async function load() {
  loading.value = true
  rows.value = []
  try {
    if (page.value === 'shareHome') {
      rows.value = []
      await loadCommunityHomeData()
    }
    else if (page.value === 'shareSkills') {
      rows.value = await api('/api/share/community/skills')
    }
    else if (page.value === 'shareTools') {
      communityBuiltinTools.value = []
      ;[rows.value, communityBuiltinTools.value] = await Promise.all([
        api('/api/share/community/tools'),
        api('/api/share/community/builtin-tools')
      ])
    }
    else if (page.value === 'sharePrompts') {
      rows.value = await api('/api/share/community/prompts')
    }
    else if (page.value === 'shareResources') {
      rows.value = await api('/api/share/community/resources')
    }
    else if (page.value === 'shareApis') {
      rows.value = await api('/api/share/community/apis')
    }
    else if (page.value === 'shareConfig') {
      await loadShareConfigData()
    }
    else if (page.value === 'shareTokens') {
      rows.value = await api('/api/share/tokens')
    }
    else if (page.value === 'studioHome') rows.value = await api('/api/share/studio/apis')
    else if (page.value === 'studioSkills') {
      rows.value = await api('/api/share/studio/skills')
    }
    else if (page.value === 'studioSkillEdit') {
      if (!model.value.skillCode) {
        model.value = emptySkillModel()
      }
    }
    else if (page.value === 'dashboard') { dashboard.value = await api('/dashboard'); rows.value = dashboard.value.recentAudits || [] }
    else if (page.value === 'users') {
      [rows.value, roles.value, userRoles.value] = await Promise.all([
        api('/users'),
        api('/roles'),
        api('/users/role-relations')
      ])
      users.value = rows.value
    }
    else if (page.value === 'roles') {
      const [roleRows, roleToolRows, rolePromptRows, roleResourceRows, dynamicTools, prompts, resources, builtinToolList, feishuTools] = await Promise.all([
        api('/roles'),
        api('/role-tools'),
        api('/role-prompts'),
        api('/role-resources'),
        api('/dynamic-tools'),
        api('/prompt-templates'),
        api('/resources'),
        api('/api/share/community/builtin-tools'),
        api('/api/share/community/feishu-tools')
      ])
      rows.value = roleRows
      roleTools.value = roleToolRows
      rolePrompts.value = rolePromptRows
      roleResources.value = roleResourceRows
      dynamicToolOptions.value = dynamicTools
      promptOptions.value = prompts
      resourceOptions.value = resources
      communityBuiltinTools.value = builtinToolList
      applyBuiltinTools(builtinToolList)
      feishuToolOptions.value = feishuTools
    }
    else if (page.value === 'tokens') {
      const [tokenRows, userRows, selections, promptSelections, resourceSelections, dynamicTools, prompts, resources, builtinToolList, feishuTools] = await Promise.all([
        api('/tokens'),
        api('/users'),
        api('/token-selections'),
        api('/token-prompt-selections'),
        api('/token-resource-selections'),
        api('/dynamic-tools'),
        api('/prompt-templates'),
        api('/resources'),
        api('/api/share/community/builtin-tools'),
        api('/api/share/community/feishu-tools')
      ])
      rows.value = tokenRows
      users.value = userRows
      tokenSelections.value = selections
      tokenPromptSelections.value = promptSelections
      tokenResourceSelections.value = resourceSelections
      dynamicToolOptions.value = dynamicTools
      promptOptions.value = prompts
      resourceOptions.value = resources
      communityBuiltinTools.value = builtinToolList
      applyBuiltinTools(builtinToolList)
      feishuToolOptions.value = feishuTools
    }
    else if (page.value === 'requests') rows.value = await api('/request-configs')
    else if (page.value === 'dataSources') rows.value = await api('/data-sources')
    else if (page.value === 'resources') rows.value = await api('/resources')
    else if (page.value === 'studioTools') {
      [rows.value, requestConfigs.value, dataSourceConfigs.value] = await Promise.all([
        api('/api/share/studio/tools'),
        api('/api/share/studio/apis'),
        api('/data-sources')
      ])
    }
    else if (page.value === 'studioToolEdit') {
      [requestConfigs.value, dataSourceConfigs.value] = await Promise.all([
        api('/api/share/studio/apis'),
        api('/data-sources')
      ])
      if (!model.value.toolName) {
        model.value = emptyToolModel()
      }
    }
    else if (page.value === 'studioPrompts') {
      [rows.value, dynamicToolOptions.value] = await Promise.all([
        api('/api/share/studio/prompts'),
        api('/dynamic-tools')
      ])
    }
    else if (page.value === 'studioPromptEdit') {
      dynamicToolOptions.value = await api('/dynamic-tools')
      if (!model.value.promptName) {
        model.value = emptyPromptModel()
        syncPromptArgumentsFromModel()
        promptPreviewArgs.value = buildPromptPreviewArgs()
      }
    }
    else if (page.value === 'studioResources') {
      rows.value = await api('/api/share/studio/resources')
    }
    else if (page.value === 'studioResourceEdit') {
      if (!model.value.resourceUri) {
        model.value = emptyResourceModel()
      }
    }
    else if (page.value === 'studioApis') rows.value = await api('/api/share/studio/apis')
    else if (page.value === 'studioApiEdit') {
      if (!model.value.requestId) {
        model.value = emptyApiModel()
        apiEditorDebugParams.value = model.value.paramsDefault || '{}'
      }
    }
    else if (page.value === 'tools') {
      [rows.value, requestConfigs.value, dataSourceConfigs.value] = await Promise.all([
        api('/dynamic-tools'),
        api('/request-configs'),
        api('/data-sources')
      ])
    }
    else rows.value = await api('/audit-logs')
  } catch (error) {
    if (loggedIn.value) {
      console.error(error)
      toast.error(`${title.value}加载失败：${error.message || '请求失败'}`)
    }
  } finally { loading.value = false }
}
function changePage(key) {
  page.value = key
  rawToken.value = ''
  const nextPath = pathForPage(key)
  if (window.location.pathname !== nextPath) {
    window.history.pushState({}, '', nextPath)
  }
  load()
}
function syncPageFromLocation() {
  page.value = pageFromPath()
  load()
}
function emptyModel() {
  if (page.value === 'users') return { username:'', displayName:'', password:'', isEnabled:1 }
  if (page.value === 'roles') return { roleCode:'', roleName:'', description:'', isEnabled:1 }
  if (page.value === 'tokens') return { userId: users.value[0]?.id, tokenName:'新建 Token', permissions:'["mcp:tools:read","mcp:tools:call"]', isActive:1 }
  if (page.value === 'shareTokens') return { tokenName:'新建 Token', permissions:'["mcp:tools:read","mcp:tools:call"]', isActive:1 }
  if (page.value === 'requests' || page.value === 'studioApis' || page.value === 'studioApiEdit') return emptyApiModel()
  if (page.value === 'dataSources') return emptyDataSourceModel()
  if (page.value === 'tools') return { toolName:'', toolDescription:'', inputSchema:'{"type":"object","properties":{}}', groovyScript:'return [message: params.message]', linkedRequestKeys:'[]', linkedDataSourceIds:'[]', enabled:1 }
  if (page.value === 'studioSkills' || page.value === 'studioSkillEdit') return emptySkillModel()
  if (page.value === 'studioPrompts' || page.value === 'studioPromptEdit') return emptyPromptModel()
  if (page.value === 'studioResources' || page.value === 'studioResourceEdit') return emptyResourceModel()
  return {}
}
function emptyApiModel() {
  return { requestId:'API' + Date.now(), configKey:'', name:'', type:'HTTP', method:'GET', url:'', headers:'{}', bodyTemplate:'', paramsDefault:'{}', connectTimeoutMs:5000, readTimeoutMs:15000, serviceName:'', methodName:'', argsSchema:'{}', creatorId: users.value[0]?.id, isEnabled:1, rateLimitPerMinute:0, publishStatus:0, description:'', category:'' }
}
function emptyDataSourceModel() {
  return { name:'', datasourceKey:'', dbType:'MYSQL', jdbcUrl:'jdbc:mysql://localhost:3306/demo?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai', username:'', password:'', extraJdbcProps:'{}', description:'', publishStatus:0 }
}
function emptySkillModel() {
  return {
    skillCode: '',
    name: '',
    description: '',
    category: '',
    objectKey: '',
    fileName: '',
    fileSize: null,
    enabled: 0,
    publishStatus: 0
  }
}
function emptyToolModel() {
  const script = defaultToolScript()
  const schema = defaultToolSchema()
  lastAutoToolScript.value = script
  lastAutoToolSchema.value = schema

  return {
    toolName: '',
    toolDescription: '',
    inputSchema: schema,
    groovyScript: script,
    linkedRequestKeys: '[]',
    linkedDataSourceIds: '[]',
    enabled: 0,
    publishStatus: 0
  }
}
function emptyPromptModel() {
  return {
    promptName: '',
    title: '',
    description: '',
    argumentsSchema: '[]',
    templateContent: defaultPromptTemplate(),
    linkedToolNames: '[]',
    enabled: 0,
    publishStatus: 0
  }
}
function emptyResourceModel() {
  return {
    resourceUri: '',
    name: '',
    description: '',
    mimeType: 'text/markdown',
    objectKey: '',
    fileName: '',
    fileSize: null,
    enabled: 0,
    publishStatus: 0
  }
}
function openCreate() {
  if (page.value === 'studioSkills') {
    openSkillEditor()
    return
  }
  if (page.value === 'studioApis') {
    openApiEditor()
    return
  }
  if (page.value === 'studioTools') {
    openToolEditor()
    return
  }
  if (page.value === 'studioPrompts') {
    openPromptEditor()
    return
  }
  if (page.value === 'studioResources') {
    openResourceEditor()
    return
  }
  model.value = emptyModel(); drawer.value=true
}
function openSkillEditor(row) {
  model.value = row ? { ...row } : emptySkillModel()
  skillSelectedFile.value = null
  skillPreviewText.value = ''
  page.value = 'studioSkillEdit'
  window.history.pushState({}, '', pathForPage('studioSkillEdit'))
  if (row?.id && row?.objectKey) {
    loadSkillPreview(row.id)
  }
}
function openToolEditor(row) {
  model.value = row ? { ...row } : emptyToolModel()
  if (!model.value.linkedDataSourceIds) {
    model.value.linkedDataSourceIds = '[]'
  }
  lastAutoToolScript.value = row ? '' : model.value.groovyScript
  lastAutoToolSchema.value = row ? '' : model.value.inputSchema
  toolDebugResult.value = null
  toolDebugParams.value = '{}'
  page.value = 'studioToolEdit'
  window.history.pushState({}, '', pathForPage('studioToolEdit'))
}
function openApiEditor(row) {
  model.value = row ? { ...row } : emptyApiModel()
  apiEditorDebugParams.value = model.value.paramsDefault || '{}'
  debugResult.value = null
  apiEditorTab.value = 'body'
  page.value = 'studioApiEdit'
  window.history.pushState({}, '', pathForPage('studioApiEdit'))
}
function openPromptEditor(row) {
  model.value = row ? { ...row } : emptyPromptModel()
  if (!model.value.linkedToolNames) {
    model.value.linkedToolNames = '[]'
  }
  if (!model.value.argumentsSchema) {
    model.value.argumentsSchema = '[]'
  }
  syncPromptArgumentsFromModel()
  promptPreviewResult.value = null
  promptPreviewArgs.value = buildPromptPreviewArgs()
  page.value = 'studioPromptEdit'
  window.history.pushState({}, '', pathForPage('studioPromptEdit'))
}
function openResourceEditor(row) {
  model.value = row ? { ...row } : emptyResourceModel()
  resourceSelectedFile.value = null
  resourcePreviewText.value = ''
  page.value = 'studioResourceEdit'
  window.history.pushState({}, '', pathForPage('studioResourceEdit'))
  if (row?.id && row?.objectKey) {
    loadResourcePreview(row.id)
  }
}
function edit(row) {
  model.value = { ...row }
  if (page.value === 'tokens' || page.value === 'shareTokens') {
    model.value.permissions = normalizePermissions(model.value.permissions)
  }
  if (page.value === 'studioApis') {
    openApiEditor(row)
    return
  }
  if (page.value === 'studioSkills') {
    openSkillEditor(row)
    return
  }
  if (page.value === 'studioTools') {
    openToolEditor(row)
    return
  }
  if (page.value === 'studioPrompts') {
    openPromptEditor(row)
    return
  }
  if (page.value === 'studioResources') {
    openResourceEditor(row)
    return
  }
  drawer.value=true
}
async function save() {
  const base = { users:'/users', roles:'/roles', tokens:'/tokens', shareTokens:'/api/share/tokens', requests:'/request-configs', dataSources:'/data-sources', tools:'/dynamic-tools', studioApis:'/api/share/studio/apis' }[page.value]
  const method = model.value.id ? 'PUT' : 'POST'
  const body = buildSaveBody()
  const result = await api(model.value.id ? `${base}/${model.value.id}` : base, { method, body })
  if ((page.value === 'tokens' || page.value === 'shareTokens') && result?.rawToken) {
    rawToken.value = result.rawToken
    oneTimeTokenName.value = result.token?.tokenName || body.tokenName || '新建 Token'
    oneTimeTokenOpen.value = true
  }
  drawer.value = false; await load()
}

async function copyRawToken() {
  if (!rawToken.value) {
    return
  }
  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(rawToken.value)
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = rawToken.value
      textarea.setAttribute('readonly', 'readonly')
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
    }
    toast.success('Token 已复制')
  } catch (error) {
    toast.error('复制失败，请手动选中 Token 复制')
  }
}

function closeOneTimeTokenModal() {
  oneTimeTokenOpen.value = false
  rawToken.value = ''
  oneTimeTokenName.value = ''
}
async function removeRow(record) {
  if (page.value !== 'dataSources') {
    return
  }
  if (!window.confirm(`确定删除数据源「${record.name || record.datasourceKey}」？`)) {
    return
  }
  await api(`/data-sources/${record.id}`, { method: 'DELETE' })
  toast.success('已删除数据源')
  await load()
}
async function testDataSourceConnection() {
  if (page.value !== 'dataSources') {
    return
  }
  dataSourceTesting.value = true
  try {
    const body = buildSaveBody()
    const url = model.value.id ? `/data-sources/${model.value.id}/test-connection` : '/data-sources/test-connection'
    await api(url, { method: 'POST', body })
    toast.success('数据源连接成功')
  } finally {
    dataSourceTesting.value = false
  }
}
function buildSaveBody() {
  const body = { ...model.value }
  if ((page.value === 'tokens' || page.value === 'shareTokens') && !body.expireTime) {
    delete body.expireTime
  }
  if (page.value === 'tokens' || page.value === 'shareTokens') {
    body.permissions = normalizePermissions(body.permissions)
  }
  if (page.value === 'shareTokens') {
    delete body.userId
  }
  if (page.value === 'users' && body.id) {
    delete body.password
  }
  if (page.value === 'dataSources') {
    delete body.passwordSet
    delete body.lastOperatorId
    if (body.id && !body.password) {
      delete body.password
    }
  }
  if (page.value === 'studioApis' || page.value === 'studioApiEdit') {
    body.type = 'HTTP'
    delete body.creatorId
    delete body.serviceName
    delete body.methodName
    delete body.argsSchema
    delete body.publishStatus
  }
  if (page.value === 'studioTools' || page.value === 'studioToolEdit') {
    body.publishStatus = Number(body.publishStatus || 0)
    body.enabled = body.publishStatus === 0 ? 0 : 1
    body.linkedDataSourceIds = body.linkedDataSourceIds || '[]'
  }
  if (page.value === 'studioSkills' || page.value === 'studioSkillEdit') {
    body.publishStatus = Number(body.publishStatus || 0)
    body.enabled = body.publishStatus === 0 ? 0 : 1
  }
  if (page.value === 'studioPrompts' || page.value === 'studioPromptEdit') {
    body.publishStatus = Number(body.publishStatus || 0)
    body.enabled = body.publishStatus === 0 ? 0 : 1
    syncPromptArgumentsToModel()
    body.argumentsSchema = model.value.argumentsSchema || '[]'
    body.linkedToolNames = body.linkedToolNames || '[]'
  }
  if (page.value === 'studioResources' || page.value === 'studioResourceEdit') {
    body.publishStatus = Number(body.publishStatus || 0)
    body.enabled = body.publishStatus === 0 ? 0 : 1
    body.mimeType = 'text/markdown'
  }
  return body
}

function defaultToolScript() {
  return 'def result = runRequest.runRequest("api_config_key", params)\n\nreturn [\n    message: "API调用完成",\n    requestParams: params,\n    result: result\n]'
}

function defaultPromptTemplate() {
  return '你是企业 AI 工作流助手。\n\n任务：围绕 {{topic}} 完成分析。\n\n建议流程：\n1. 先理解用户目标，确认缺失参数。\n2. 优先使用下方建议工具完成事实查询。\n3. 如果工具返回 rows，不要原样堆砌，先提炼关键结论。\n4. 最终输出：结论、依据、下一步建议。\n\n建议使用的工具：\n{{tools}}'
}

function renderMarkdown(markdown) {
  if (!markdown) {
    return '<p class="md-empty">选择 Markdown 文件后显示预览内容。</p>'
  }
  const lines = String(markdown).replace(/\r\n/g, '\n').split('\n')
  const html = []
  let paragraph = []
  let listType = ''
  let codeLines = []
  let inCode = false

  const flushParagraph = () => {
    if (!paragraph.length) return
    html.push(`<p>${renderInline(paragraph.join(' '))}</p>`)
    paragraph = []
  }
  const closeList = () => {
    if (!listType) return
    html.push(`</${listType}>`)
    listType = ''
  }

  lines.forEach(line => {
    const raw = line
    const trimmed = raw.trim()
    if (trimmed.startsWith('```')) {
      if (inCode) {
        html.push(`<pre><code>${escapeHtml(codeLines.join('\n'))}</code></pre>`)
        codeLines = []
        inCode = false
      } else {
        flushParagraph()
        closeList()
        inCode = true
      }
      return
    }
    if (inCode) {
      codeLines.push(raw)
      return
    }
    if (!trimmed) {
      flushParagraph()
      closeList()
      return
    }
    const heading = trimmed.match(/^(#{1,6})\s+(.+)$/)
    if (heading) {
      flushParagraph()
      closeList()
      const level = heading[1].length
      html.push(`<h${level}>${renderInline(heading[2])}</h${level}>`)
      return
    }
    const unordered = trimmed.match(/^[-*]\s+(.+)$/)
    if (unordered) {
      flushParagraph()
      if (listType !== 'ul') {
        closeList()
        listType = 'ul'
        html.push('<ul>')
      }
      html.push(`<li>${renderInline(unordered[1])}</li>`)
      return
    }
    const ordered = trimmed.match(/^\d+\.\s+(.+)$/)
    if (ordered) {
      flushParagraph()
      if (listType !== 'ol') {
        closeList()
        listType = 'ol'
        html.push('<ol>')
      }
      html.push(`<li>${renderInline(ordered[1])}</li>`)
      return
    }
    paragraph.push(trimmed)
  })
  if (inCode) {
    html.push(`<pre><code>${escapeHtml(codeLines.join('\n'))}</code></pre>`)
  }
  flushParagraph()
  closeList()
  return html.join('\n')
}

function renderInline(value) {
  return escapeHtml(value)
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
}

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function defaultToolSchema() {
  return '{\n  "type": "object",\n  "properties": {\n    "pageNum": {\n      "type": "integer",\n      "description": "页码"\n    },\n    "pageSize": {\n      "type": "integer",\n      "description": "每页条数"\n    }\n  }\n}'
}

function promptArguments() {
  return promptArgumentItems.value
}

function promptArgumentKey() {
  return `${Date.now()}-${Math.random().toString(36).slice(2)}`
}

function normalizePromptArgument(item, index) {
  return {
    _key: item?._key || promptArgumentKey(),
    name: item?.name || '',
    description: item?.description || `参数 ${index + 1}`,
    required: Boolean(item?.required),
    defaultValue: item?.defaultValue ?? ''
  }
}

function serializablePromptArguments() {
  return promptArgumentItems.value
    .filter(item => item && item.name)
    .map(({ name, description, required, defaultValue }) => ({
      name,
      description,
      required,
      defaultValue
    }))
}

function syncPromptArgumentsFromModel() {
  promptArgumentItems.value = parseJsonArray(model.value.argumentsSchema)
    .map((item, index) => normalizePromptArgument(item, index))
}

function syncPromptArgumentsToModel() {
  model.value.argumentsSchema = JSON.stringify(serializablePromptArguments(), null, 2)
}

function addPromptArgument() {
  const nextIndex = promptArgumentItems.value.length + 1
  promptArgumentItems.value.push({
    _key: promptArgumentKey(),
    name: `param${nextIndex}`,
    description: `参数 ${nextIndex}`,
    required: false,
    defaultValue: ''
  })
  syncPromptArgumentsToModel()
  promptPreviewArgs.value = buildPromptPreviewArgs()
}

function removePromptArgument(index) {
  promptArgumentItems.value.splice(index, 1)
  syncPromptArgumentsToModel()
  promptPreviewArgs.value = buildPromptPreviewArgs()
}

function updatePromptArgument(index, field, value) {
  if (!promptArgumentItems.value[index]) {
    return
  }
  promptArgumentItems.value[index][field] = value
  syncPromptArgumentsToModel()
  if (field === 'name' || field === 'defaultValue') {
    promptPreviewArgs.value = buildPromptPreviewArgs()
  }
}

function updatePromptArguments(args) {
  promptArgumentItems.value = args.map((item, index) => normalizePromptArgument(item, index))
  syncPromptArgumentsToModel()
}

function extractPromptArgumentsFromTemplate() {
  const template = model.value.templateContent || ''
  const args = promptArguments()
  const names = args.map(item => item.name)
  const pattern = /\{\{\s*([A-Za-z_][A-Za-z0-9_.-]*)\s*}}/g
  let match = pattern.exec(template)
  while (match) {
    const name = match[1]
    if (name !== 'tools' && !names.includes(name)) {
      args.push({
        name,
        description: name,
        required: true,
        defaultValue: ''
      })
      names.push(name)
    }
    match = pattern.exec(template)
  }
  updatePromptArguments(args)
  promptPreviewArgs.value = buildPromptPreviewArgs()
  toast.success('已从模板提取参数')
}

function buildPromptPreviewArgs() {
  const params = {}
  serializablePromptArguments().forEach(item => {
    params[item.name] = item.defaultValue ?? ''
  })
  return JSON.stringify(params, null, 2)
}

function promptToolsText() {
  const names = selectedPromptToolNames.value
  if (!names.length) {
    return '{{tools}}'
  }
  return names.map(name => `- ${name}`).join('\n')
}

function promptToolsBlock() {
  return `建议使用的工具：\n${promptToolsText()}`
}

function syncPromptToolsIntoTemplate() {
  if (page.value !== 'studioPromptEdit') {
    return
  }

  const template = model.value.templateContent || ''
  const block = promptToolsBlock()
  const managedBlockPattern = /建议使用的工具：\n(?:- .*(?:\n|$)|\{\{tools}}(?:\n|$))*/m
  if (template.includes('{{tools}}')) {
    model.value.templateContent = template.replace(/\{\{tools}}/g, promptToolsText())
    return
  }
  if (managedBlockPattern.test(template)) {
    model.value.templateContent = template.replace(managedBlockPattern, block)
    return
  }
  if (selectedPromptToolNames.value.length) {
    model.value.templateContent = `${template.trimEnd()}\n\n${block}`
  }
}

function insertPromptToolsPlaceholder() {
  syncPromptToolsIntoTemplate()
}

function tryAutoGenerateToolAssets() {
  if (page.value !== 'studioToolEdit') {
    return
  }

  const script = model.value.groovyScript || ''
  if (!script.trim() || script === lastAutoToolScript.value || script === defaultToolScript()) {
    if (selectedToolRequestKeys.value.length) {
      generateToolScriptFromSelection()
    } else if (selectedToolDataSourceIds.value.length) {
      generateToolScriptFromDataSourceSelection()
    }
  }

  const schema = model.value.inputSchema || ''
  if (!schema.trim() || schema === lastAutoToolSchema.value || schema === defaultToolSchema()) {
    if (selectedToolRequestKeys.value.length) {
      generateToolSchemaFromSelection()
    } else if (selectedToolDataSourceIds.value.length) {
      generateToolSchemaFromScript()
    }
  }
}

function generateToolScriptFromSelection() {
  const keys = selectedToolRequestKeys.value
  if (!keys.length) {
    return
  }

  const apiConfigs = keys.map(key => {
    return requestConfigs.value.find(item => item.configKey === key) || { configKey: key, name: key, paramsDefault: '{}' }
  })
  const script = buildToolScript(apiConfigs)
  model.value.groovyScript = script
  lastAutoToolScript.value = script
}

function generateToolSchemaFromSelection() {
  const keys = selectedToolRequestKeys.value
  if (!keys.length) {
    return
  }

  const apiConfigs = keys.map(key => {
    return requestConfigs.value.find(item => item.configKey === key) || { configKey: key, paramsDefault: '{}' }
  })
  const schema = buildToolSchema(apiConfigs)
  model.value.inputSchema = schema
  lastAutoToolSchema.value = schema
}

function generateToolAssetsFromSelection() {
  generateToolSchemaFromSelection()
  generateToolScriptFromSelection()
  generateToolDebugParamsFromSelection()
}

function generateToolScriptFromDataSourceSelection() {
  const ids = selectedToolDataSourceIds.value
  if (!ids.length) {
    return
  }

  const script = buildDataSourceToolScript(ids)
  model.value.groovyScript = script
  lastAutoToolScript.value = script
}

function generateToolAssetsFromDataSourceSelection() {
  generateToolScriptFromDataSourceSelection()
  generateToolSchemaFromScript()
}

function generateToolSchemaFromScript() {
  const entries = collectScriptParamEntries(model.value.groovyScript)
  if (!entries.length) {
    toast.warning('没有识别到 params.xxx 参数')
    return
  }

  const currentSchema = parseJsonObject(model.value.inputSchema)
  const properties = {
    ...(currentSchema.properties && typeof currentSchema.properties === 'object' && !Array.isArray(currentSchema.properties)
      ? currentSchema.properties
      : {})
  }
  const required = new Set(Array.isArray(currentSchema.required) ? currentSchema.required : [])

  entries.forEach(item => {
    properties[item.key] = properties[item.key] || {
      type: item.type,
      description: item.description
    }
    if (item.required) {
      required.add(item.key)
    }
  })

  const nextSchema = {
    type: 'object',
    properties
  }
  if (required.size) {
    nextSchema.required = Array.from(required)
  }

  model.value.inputSchema = JSON.stringify(nextSchema, null, 2)
  toolDebugParams.value = JSON.stringify(buildScriptDebugParams(entries), null, 2)
  toast.success(`已生成 ${entries.length} 个入参`)
}

function generateToolDebugParamsFromSelection() {
  const keys = selectedToolRequestKeys.value
  if (!keys.length) {
    toolDebugParams.value = '{}'
    return
  }

  const apiConfigs = keys.map(key => {
    return requestConfigs.value.find(item => item.configKey === key) || { configKey: key, paramsDefault: '{}', bodyTemplate: '' }
  })
  toolDebugParams.value = buildToolDebugParams(apiConfigs)
}

function buildDataSourceToolScript(ids) {
  const firstId = ids[0]
  const dataSource = dataSourceConfigs.value.find(item => Number(item.id) === Number(firstId))
  const name = dataSource?.name || dataSource?.datasourceKey || `数据源 ${firstId}`
  const runSqlCall = ids.length === 1
    ? 'runSql.runSql(sql)'
    : `runSql.runSql(${Number(firstId)}L, sql)`

  return `def limit = Math.min((params["limit"] ?: 10) as Integer, 100)

def sql = """
select 1 as demo_value
limit \${limit}
"""

def result = ${runSqlCall}

return [
    message: "${name} 查询完成",
    requestParams: [
        "limit": limit
    ],
    rows: result.rows,
    rowCount: result.row_count,
    truncated: result.truncated,
    result: result
]`
}

function buildToolScript(apiConfigs) {
  if (apiConfigs.length === 1) {
    return buildSingleApiToolScript(apiConfigs[0])
  }

  const calls = apiConfigs.map((apiConfig, index) => {
    const configKey = apiConfig.configKey
    const entries = collectApiParamEntries(apiConfig)
    const resultName = `result${index + 1}`

    if (!entries.length) {
      return `def ${resultName} = runRequest.runRequest("${configKey}", params)`
    }

    const requestLines = entries.map(item => {
      return `    "${item.key}": params["${item.key}"] ?: ${groovyLiteral(item.defaultValue)}`
    }).join(',\n')

    return `def ${resultName} = runRequest.runRequest("${configKey}", [\n${requestLines}\n])`
  }).join('\n\n')

  const resultLines = apiConfigs.map((apiConfig, index) => {
    return `    "${apiConfig.configKey}": result${index + 1}`
  }).join(',\n')

  return `${calls}\n\nreturn [\n    message: "组合调用完成",\n${resultLines}\n]`
}

function buildSingleApiToolScript(apiConfig) {
  const configKey = apiConfig.configKey
  const apiName = apiConfig.name || configKey
  const entries = collectApiParamEntries(apiConfig)

  if (!entries.length) {
    return `def result = runRequest.runRequest("${configKey}", params)\n\nreturn [\n    message: "${apiName}调用完成",\n    requestParams: params,\n    result: result\n]`
  }

  const requestLines = entries.map(item => {
    return `    "${item.key}": params["${item.key}"] ?: ${groovyLiteral(item.defaultValue)}`
  }).join(',\n')

  const responseLines = entries.map(item => {
    return `        "${item.key}": params["${item.key}"] ?: ${groovyLiteral(item.defaultValue)}`
  }).join(',\n')

  return `def result = runRequest.runRequest("${configKey}", [\n${requestLines}\n])\n\nreturn [\n    message: "${apiName}调用完成",\n    requestParams: [\n${responseLines}\n    ],\n    result: result\n]`
}

function buildToolSchema(apiConfigs) {
  const properties = {}

  apiConfigs.forEach(apiConfig => {
    collectApiParamEntries(apiConfig).forEach(item => {
      if (!properties[item.key]) {
        properties[item.key] = {
          type: item.type,
          description: item.description
        }
      }
    })
  })

  return JSON.stringify({
    type: 'object',
    properties
  }, null, 2)
}

function buildToolDebugParams(apiConfigs) {
  const params = {}

  apiConfigs.forEach(apiConfig => {
    collectApiParamEntries(apiConfig).forEach(item => {
      if (params[item.key] == null) {
        params[item.key] = item.defaultValue
      }
    })
  })

  return JSON.stringify(params, null, 2)
}

function collectScriptParamEntries(script) {
  if (!script) {
    return []
  }

  const text = String(script)
  const paramMap = new Map()
  const patterns = [
    /params\.([A-Za-z_][A-Za-z0-9_]*)/g,
    /params\[['"]([A-Za-z_][A-Za-z0-9_]*)['"]\]/g
  ]

  patterns.forEach(pattern => {
    let match = pattern.exec(text)
    while (match) {
      const key = match[1]
      if (key && key !== 'class') {
        const after = text.slice(match.index + match[0].length, match.index + match[0].length + 20)
        const optional = /^\s*\?:/.test(after)
        const existing = paramMap.get(key)
        paramMap.set(key, {
          key,
          type: inferScriptParamType(key),
          description: paramDescription(key),
          required: existing ? existing.required || !optional : !optional
        })
      }
      match = pattern.exec(text)
    }
  })

  return Array.from(paramMap.values())
}

function inferScriptParamType(key) {
  if (/^(pageNum|pageSize|limit|offset|count|size|num|id)$/i.test(key) || /(Id|Count|Num|Size|Limit|Offset)$/.test(key)) {
    return 'integer'
  }
  if (/^(is|has|enable|enabled|active)/i.test(key)) {
    return 'boolean'
  }
  return 'string'
}

function paramDescription(key) {
  const labels = {
    keyword: '关键词',
    name: '名称',
    pageNum: '页码',
    pageSize: '每页条数',
    limit: '最大返回条数',
    offset: '偏移量'
  }
  return labels[key] || key
}

function buildScriptDebugParams(entries) {
  const params = {}
  entries.forEach(item => {
    if (item.type === 'integer') {
      params[item.key] = item.key === 'pageNum' ? 1 : 10
    } else if (item.type === 'number') {
      params[item.key] = 1
    } else if (item.type === 'boolean') {
      params[item.key] = true
    } else {
      params[item.key] = ''
    }
  })
  return params
}

function collectApiParamEntries(apiConfig) {
  const paramsDefault = parseJsonObject(apiConfig.paramsDefault)
  const paramMap = new Map()

  Object.entries(paramsDefault).forEach(([key, value]) => {
    paramMap.set(key, {
      key,
      defaultValue: value,
      type: inferJsonSchemaType(value),
      description: key
    })
  })

  extractTemplateParams(apiConfig.bodyTemplate).forEach(key => {
    if (!paramMap.has(key)) {
      paramMap.set(key, {
        key,
        defaultValue: '',
        type: 'string',
        description: key
      })
    }
  })

  return Array.from(paramMap.values())
}

function extractTemplateParams(template) {
  if (!template) {
    return []
  }

  const keys = []
  const pattern = /\{\{\s*([A-Za-z0-9_.-]+)\s*\}\}/g
  let match = pattern.exec(String(template))
  while (match) {
    if (!keys.includes(match[1])) {
      keys.push(match[1])
    }
    match = pattern.exec(String(template))
  }
  return keys
}

function inferJsonSchemaType(value) {
  if (Number.isInteger(value)) {
    return 'integer'
  }
  if (typeof value === 'number') {
    return 'number'
  }
  if (typeof value === 'boolean') {
    return 'boolean'
  }
  if (Array.isArray(value)) {
    return 'array'
  }
  if (value && typeof value === 'object') {
    return 'object'
  }
  return 'string'
}

function parseJsonObject(value) {
  try {
    const parsed = value ? JSON.parse(value) : {}
    return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {}
  } catch (error) {
    return {}
  }
}

function groovyLiteral(value) {
  if (typeof value === 'number' || typeof value === 'boolean') {
    return String(value)
  }
  if (value == null) {
    return 'null'
  }
  return JSON.stringify(String(value))
}

function normalizePermissions(value) {
  if (!value) {
    return '["mcp:tools:read","mcp:tools:call"]'
  }

  try {
    const permissions = Array.isArray(value) ? value : JSON.parse(value)
    if (permissions.includes('mcp:tools:call')) {
      return '["mcp:tools:read","mcp:tools:call"]'
    }
    if (permissions.includes('mcp:tools:read')) {
      return '["mcp:tools:read"]'
    }
  } catch (error) {
    console.warn('无法解析 Token 权限范围', value, error)
  }

  return String(value)
}
function uniqueValues(values) {
  return Array.from(new Set(values.filter(value => value !== undefined && value !== null && value !== '')))
}
function selectableToolNames() {
  const builtinNames = builtinTools.value.map(tool => tool.name)
  const dynamicNames = dynamicToolOptions.value
    .filter(tool => Number(tool.enabled) === 1)
    .map(tool => tool.toolName)
  const feishuNames = feishuToolOptions.value.map(tool => tool.name)
  return uniqueValues([...builtinNames, ...dynamicNames, ...feishuNames])
}
function toolTypeOf(toolName) {
  if (builtinTools.value.some(tool => tool.name === toolName)) return 'BUILTIN'
  if (feishuToolOptions.value.some(tool => tool.name === toolName)) return 'REMOTE'
  return 'DYNAMIC'
}
function toolNamesForKind(kind) {
  if (kind === 'builtin') return builtinTools.value.map(tool => tool.name)
  if (kind === 'dynamic') return dynamicToolOptions.value.filter(tool => Number(tool.enabled) === 1).map(tool => tool.toolName)
  return feishuToolOptions.value.map(tool => tool.name)
}
function selectRoleToolGroup(kind) { selectedRoleTools.value = uniqueValues([...selectedRoleTools.value, ...toolNamesForKind(kind)]) }
function clearRoleToolGroup(kind) { selectedRoleTools.value = selectedRoleTools.value.filter(name => !toolNamesForKind(kind).includes(name)) }
function selectTokenToolGroup(kind) { selectedTokenTools.value = uniqueValues([...selectedTokenTools.value, ...toolNamesForKind(kind)]) }
function clearTokenToolGroup(kind) { selectedTokenTools.value = selectedTokenTools.value.filter(name => !toolNamesForKind(kind).includes(name)) }
function selectShareToolGroup(kind) { selectedTokenTools.value = uniqueValues([...selectedTokenTools.value, ...toolNamesForKind(kind)]) }
function clearShareToolGroup(kind) { selectedTokenTools.value = selectedTokenTools.value.filter(name => !toolNamesForKind(kind).includes(name)) }
function selectablePromptNames() {
  return promptOptions.value
    .filter(prompt => Number(prompt.enabled) === 1 && Number(prompt.publishStatus) !== 0)
    .map(prompt => prompt.promptName)
}
function selectableResourceUris() {
  return resourceOptions.value
    .filter(resource => Number(resource.enabled) === 1 && Number(resource.publishStatus) !== 0)
    .map(resource => resource.resourceUri)
}
function selectAllRoleTools() {
  selectedRoleTools.value = selectableToolNames()
}
function selectAllTokenTools() {
  selectedTokenTools.value = selectableToolNames()
}
function selectAllRolePrompts() {
  selectedRolePrompts.value = selectablePromptNames()
}
function selectAllTokenPrompts() {
  selectedTokenPrompts.value = selectablePromptNames()
}
function selectAllRoleResources() {
  selectedRoleResources.value = selectableResourceUris()
}
function selectAllTokenResources() {
  selectedTokenResources.value = selectableResourceUris()
}
function clearRoleTools() {
  selectedRoleTools.value = []
}
function clearTokenTools() {
  selectedTokenTools.value = []
}
function clearRolePrompts() {
  selectedRolePrompts.value = []
}
function clearTokenPrompts() {
  selectedTokenPrompts.value = []
}
function clearRoleResources() {
  selectedRoleResources.value = []
}
function clearTokenResources() {
  selectedTokenResources.value = []
}
async function updateSelections(token) {
  activeToken.value = token
  selectedTokenTools.value = tokenSelections.value
    .filter(item => item.tokenId === token.id && item.enabled === 1)
    .map(item => item.toolName)
  tokenSelectionOpen.value = true
}
async function updateTokenPromptSelections(token) {
  activeToken.value = token
  selectedTokenPrompts.value = tokenPromptSelections.value
    .filter(item => item.tokenId === token.id && item.enabled === 1)
    .map(item => item.promptName)
  tokenPromptSelectionOpen.value = true
}
async function updateTokenResourceSelections(token) {
  activeToken.value = token
  selectedTokenResources.value = tokenResourceSelections.value
    .filter(item => item.tokenId === token.id && item.enabled === 1)
    .map(item => item.resourceUri)
  tokenResourceSelectionOpen.value = true
}
async function updateUserRoles(user) {
  activeUser.value = user
  selectedUserRoles.value = userRoles.value
    .filter(item => item.userId === user.id)
    .map(item => item.roleCode)
  userRoleOpen.value = true
}
async function saveUserRoles() {
  await api(`/users/${activeUser.value.id}/roles`, {
    method: 'PUT',
    body: { codes: selectedUserRoles.value }
  })

  userRoleOpen.value = false
  await load()
}
async function updateRoleTools(role) {
  activeRole.value = role
  selectedRoleTools.value = roleTools.value.filter(item => item.roleCode === role.roleCode).map(item => item.toolName)
  toolPermissionOpen.value = true
}
async function updateRolePrompts(role) {
  activeRole.value = role
  selectedRolePrompts.value = rolePrompts.value.filter(item => item.roleCode === role.roleCode).map(item => item.promptName)
  promptPermissionOpen.value = true
}
async function updateRoleResources(role) {
  activeRole.value = role
  selectedRoleResources.value = roleResources.value.filter(item => item.roleCode === role.roleCode).map(item => item.resourceUri)
  resourcePermissionOpen.value = true
}
async function saveRoleTools() {
  await api(`/roles/${activeRole.value.roleCode}/tools`, { method:'PUT', body:{ codes:selectedRoleTools.value } })
  toolPermissionOpen.value = false
  await load()
}
async function saveRolePrompts() {
  await api(`/roles/${activeRole.value.roleCode}/prompts`, { method:'PUT', body:{ codes:selectedRolePrompts.value } })
  promptPermissionOpen.value = false
  await load()
}
async function saveRoleResources() {
  await api(`/roles/${activeRole.value.roleCode}/resources`, { method:'PUT', body:{ codes:selectedRoleResources.value } })
  resourcePermissionOpen.value = false
  await load()
}
async function saveTokenSelections() {
  const tools = selectedTokenTools.value.map(toolName => ({
    toolName,
    toolType: toolTypeOf(toolName),
    enabled: 1
  }))

  await api(`/tokens/${activeToken.value.id}/selections`, {
    method: 'PUT',
    body: { tools }
  })

  tokenSelectionOpen.value = false
  await load()
}
async function saveTokenPromptSelections() {
  const prompts = selectedTokenPrompts.value.map(promptName => ({
    promptName,
    enabled: 1
  }))

  await api(`/tokens/${activeToken.value.id}/prompt-selections`, {
    method: 'PUT',
    body: { prompts }
  })

  tokenPromptSelectionOpen.value = false
  await load()
}
async function saveTokenResourceSelections() {
  const resources = selectedTokenResources.value.map(resourceUri => ({
    resourceUri,
    enabled: 1
  }))

  await api(`/tokens/${activeToken.value.id}/resource-selections`, {
    method: 'PUT',
    body: { resources }
  })

  tokenResourceSelectionOpen.value = false
  await load()
}
async function toggleResourceEnabled(resource) {
  const enabled = Number(resource.enabled) === 1
  await api(`/resources/${resource.id}/${enabled ? 'disable' : 'enable'}`, { method: 'POST' })
  toast.success(enabled ? 'Resource 已禁用' : 'Resource 已启用')
  await load()
}
function showDynamicToolDetail(tool) {
  activeDynamicTool.value = tool
  dynamicToolDetailOpen.value = true
}
function showAuditDetail(log) {
  activeAuditLog.value = log
  auditDetailOpen.value = true
}
function publishLabel(status) {
  return Number(status) === 0 ? '草稿' : '已上线'
}
function publishClass(status) {
  return Number(status) === 0 ? 'draft' : 'online'
}
function visibilityLabel(status) {
  return Number(status) === 2 ? '公开' : '不公开'
}
function visibilityClass(status) {
  return Number(status) === 2 ? 'public' : 'private'
}
function onlineActionLabel(status) {
  return Number(status) === 0 ? '上线' : '下线'
}
function visibilityActionLabel(status) {
  return Number(status) === 2 ? '不公开' : '公开'
}
function openApiDebug(apiConfig) {
  activeStudioApi.value = apiConfig
  debugParams.value = apiConfig.paramsDefault || '{}'
  debugResult.value = null
  apiDebugOpen.value = true
}
async function runApiDebug() {
  debugLoading.value = true
  try {
    const params = debugParams.value ? JSON.parse(debugParams.value) : {}
    debugResult.value = await api(`/api/share/studio/apis/${activeStudioApi.value.id}/debug`, {
      method: 'POST',
      body: { params }
    })
    toast.success('调试成功')
  } catch (error) {
    debugResult.value = error.data || { success: false, errorMessage: error.message }
    toast.error(debugResult.value.errorMessage || error.message || '调试失败')
  } finally {
    debugLoading.value = false
  }
}
async function saveApiEditor() {
  apiSaving.value = true
  try {
    const method = model.value.id ? 'PUT' : 'POST'
    const url = model.value.id ? `/api/share/studio/apis/${model.value.id}` : '/api/share/studio/apis'
    const result = await api(url, {
      method,
      body: buildSaveBody()
    })
    model.value = { ...model.value, ...result }
    toast.success(model.value.id ? 'API 已保存' : 'API 已创建')
    return result
  } catch (error) {
    toast.error(error.message || '保存失败')
    throw error
  } finally {
    apiSaving.value = false
  }
}
async function sendApiEditor() {
  debugLoading.value = true
  try {
    const params = apiEditorDebugParams.value ? JSON.parse(apiEditorDebugParams.value) : {}
    debugResult.value = await api('/api/share/studio/apis/debug', {
      method: 'POST',
      body: { ...buildSaveBody(), params }
    })
    toast.success('调试成功')
  } catch (error) {
    debugResult.value = error.data || { success: false, errorMessage: error.message }
    toast.error(debugResult.value.errorMessage || error.message || '调试失败')
  } finally {
    debugLoading.value = false
  }
}
async function saveToolEditor() {
  toolSaving.value = true
  try {
    const method = model.value.id ? 'PUT' : 'POST'
    const url = model.value.id ? `/api/share/studio/tools/${model.value.id}` : '/api/share/studio/tools'
    const result = await api(url, {
      method,
      body: buildSaveBody()
    })
    model.value = { ...model.value, ...result }
    toast.success(model.value.id ? 'Tool 已保存' : 'Tool 已创建')
    return result
  } catch (error) {
    toast.error(error.message || '保存失败')
    throw error
  } finally {
    toolSaving.value = false
  }
}
async function sendToolEditor() {
  toolDebugLoading.value = true
  try {
    const params = toolDebugParams.value ? JSON.parse(toolDebugParams.value) : {}
    toolDebugResult.value = await api('/api/share/studio/tools/debug', {
      method: 'POST',
      body: { params, tool: buildSaveBody() }
    })
    toast.success('调试成功')
  } catch (error) {
    toolDebugResult.value = error.data || { success: false, errorMessage: error.message }
    toast.error(toolDebugResult.value.errorMessage || error.message || '调试失败')
  } finally {
    toolDebugLoading.value = false
  }
}
async function savePromptEditor() {
  promptSaving.value = true
  try {
    const method = model.value.id ? 'PUT' : 'POST'
    const url = model.value.id ? `/api/share/studio/prompts/${model.value.id}` : '/api/share/studio/prompts'
    const result = await api(url, {
      method,
      body: buildSaveBody()
    })
    model.value = { ...model.value, ...result }
    toast.success(model.value.id ? 'Prompt 已保存' : 'Prompt 已创建')
    return result
  } catch (error) {
    toast.error(error.message || '保存失败')
    throw error
  } finally {
    promptSaving.value = false
  }
}

async function previewPromptEditor() {
  promptPreviewLoading.value = true
  try {
    const args = promptPreviewArgs.value ? JSON.parse(promptPreviewArgs.value) : {}
    promptPreviewResult.value = await api('/api/share/studio/prompts/debug', {
      method: 'POST',
      body: { arguments: args, prompt: buildSaveBody() }
    })
    toast.success('预览已生成')
  } catch (error) {
    promptPreviewResult.value = { success: false, errorMessage: error.message }
    toast.error(error.message || '预览失败')
  } finally {
    promptPreviewLoading.value = false
  }
}

async function publishPrompt(prompt) {
  const result = await api(`/api/share/studio/prompts/${prompt.id}/publish`, { method: 'POST' })
  if (model.value.id === prompt.id) {
    model.value = { ...model.value, ...result }
  }
  toast.success('Prompt 已公开发布')
  await load()
}

async function publishPrivatePrompt(prompt) {
  const result = await api(`/api/share/studio/prompts/${prompt.id}/publish-private`, { method: 'POST' })
  if (model.value.id === prompt.id) {
    model.value = { ...model.value, ...result }
  }
  toast.success('Prompt 已设为不公开')
  await load()
}

async function unpublishPrompt(prompt) {
  const result = await api(`/api/share/studio/prompts/${prompt.id}/unpublish`, { method: 'POST' })
  if (model.value.id === prompt.id) {
    model.value = { ...model.value, ...result }
  }
  toast.success('Prompt 已下线')
  await load()
}

async function toggleOnlinePrompt(prompt) {
  if (Number(prompt.publishStatus) !== 0) {
    await unpublishPrompt(prompt)
    return
  }
  await publishPrivatePrompt(prompt)
}

async function toggleVisibilityPrompt(prompt) {
  if (Number(prompt.publishStatus) === 0) {
    toast.warning('先上线，再设置公开范围')
    return
  }
  if (Number(prompt.publishStatus) === 2) {
    await publishPrivatePrompt(prompt)
    return
  }
  await publishPrompt(prompt)
}
async function onSkillFileChange(event) {
  const file = event.target.files?.[0]
  if (!file) {
    skillSelectedFile.value = null
    return
  }
  if (!file.name.toLowerCase().endsWith('.md')) {
    toast.warning('Skill 当前只支持 Markdown (.md) 文件')
    event.target.value = ''
    skillSelectedFile.value = null
    return
  }
  skillSelectedFile.value = file
  model.value.fileName = file.name
  model.value.fileSize = file.size
  if (!model.value.name) {
    model.value.name = file.name.replace(/\.md$/i, '')
  }
  skillPreviewText.value = await file.text()
  applySkillFrontmatterHint(skillPreviewText.value)
}

function applySkillFrontmatterHint(markdown) {
  const match = String(markdown || '').replace(/\r\n/g, '\n').match(/^---\s*\n([\s\S]*?)\n---/)
  if (!match) {
    return
  }
  const fields = {}
  match[1].split('\n').forEach(line => {
    const item = line.match(/^([a-zA-Z0-9_-]+):\s*(.*)$/)
    if (item) {
      fields[item[1]] = item[2].replace(/^['"]|['"]$/g, '').trim()
    }
  })
  if (fields.name && !model.value.name) {
    model.value.name = fields.name
  }
  if (fields.description && !model.value.description) {
    model.value.description = fields.description
  }
}

function skillInstallBaseUrl() {
  const origin = window.location.origin.replace(/\/$/, '')
  if (window.location.hostname === 'localhost' && window.location.port === '5173') {
    return 'http://localhost:8090'
  }
  return origin
}

function skillCliInstallCommand(skillCode) {
  return `bear-skill install ${skillCode} --base-url ${skillInstallBaseUrl()}`
}

function skillCodexInstallCommand(skillCode) {
  return `bear-skill install ${skillCode} --base-url ${skillInstallBaseUrl()} --codex`
}

function skillInstallScriptUrl() {
  return `${skillInstallBaseUrl()}/api/public/bear-skill/install.sh`
}

function buildCursorSkillInstallPrompt(skill) {
  const skillCode = skill?.skillCode || model.value.skillCode
  const cliCmd = skillCliInstallCommand(skillCode)
  return '请先检查是否已安装 bear-skill CLI（执行 which bear-skill 或 command -v bear-skill）。\n\n'
    + '若未安装，请执行以下命令安装（安装到 ~/.local/bin，无需 sudo）：\n'
    + `  curl -fsSL ${skillInstallScriptUrl()} | bash -s -- --cli-only\n\n`
    + `若已安装，则直接安装 ${skillCode} 技能：\n`
    + `  ${cliCmd}\n\n`
    + '安装完成后，请确认当前项目下存在 .cursor/skills/ 目录，并提示我重启或刷新 Cursor。'
}

function buildCodexSkillInstallCommand(skill) {
  const skillCode = skill?.skillCode || model.value.skillCode
  return `curl -fsSL ${skillInstallScriptUrl()} | bash -s -- --cli-only\n${skillCodexInstallCommand(skillCode)}`
}

function openCursorSkillInstall(skill) {
  const skillCode = skill?.skillCode || model.value.skillCode
  if (!skillCode || Number(skill?.publishStatus ?? model.value.publishStatus) === 0) {
    toast.warning('请先上线 Skill，再安装到 Cursor')
    return
  }
  const prompt = encodeURIComponent(buildCursorSkillInstallPrompt(skill))
  const deeplink = `cursor://anysphere.cursor-deeplink/prompt?text=${prompt}`
  try {
    window.location.href = deeplink
  } catch (error) {
    window.open(`https://cursor.com/link/prompt?text=${prompt}`, '_blank')
  }
}

async function copyCodexSkillInstall(skill) {
  const skillCode = skill?.skillCode || model.value.skillCode
  if (!skillCode || Number(skill?.publishStatus ?? model.value.publishStatus) === 0) {
    toast.warning('请先上线 Skill，再安装到 Codex')
    return
  }
  const command = buildCodexSkillInstallCommand(skill)
  try {
    await navigator.clipboard.writeText(command)
    toast.success('Codex 安装命令已复制到剪贴板，请在终端执行')
  } catch (error) {
    toast.info(command)
  }
}

function downloadSkillZip(skill) {
  const skillCode = skill?.skillCode || model.value.skillCode
  if (!skillCode || Number(skill?.publishStatus ?? model.value.publishStatus) === 0) {
    toast.warning('请先上线 Skill，再下载 ZIP')
    return
  }
  window.open(`${skillInstallBaseUrl()}/api/public/skills/${encodeURIComponent(skillCode)}/download`, '_blank')
}

async function loadSkillPreview(skillId = model.value.id) {
  if (!skillId) {
    skillPreviewText.value = ''
    return
  }
  skillPreviewLoading.value = true
  try {
    const presign = await api(`/api/share/studio/skills/${skillId}/presign-download`)
    const response = await fetch(presign.downloadUrl)
    if (!response.ok) {
      throw new Error(`Skill 读取失败：${response.status}`)
    }
    skillPreviewText.value = await response.text()
  } catch (error) {
    skillPreviewText.value = error.message || 'Skill 读取失败'
  } finally {
    skillPreviewLoading.value = false
  }
}

async function uploadSkillFileIfNeeded() {
  if (!skillSelectedFile.value) {
    return
  }
  const file = skillSelectedFile.value
  const presign = await api('/api/share/studio/skills/presign-upload', {
    method: 'POST',
    body: {
      skillCode: model.value.skillCode,
      fileName: file.name,
      fileSize: file.size
    }
  })
  const uploadResponse = await fetch(presign.uploadUrl, {
    method: 'PUT',
    body: file
  })
  if (!uploadResponse.ok) {
    throw new Error(`Skill 上传失败：${uploadResponse.status}`)
  }
  model.value.skillCode = presign.skillCode
  model.value.objectKey = presign.objectKey
  model.value.fileName = file.name
  model.value.fileSize = file.size
  skillSelectedFile.value = null
}

async function saveSkillEditor() {
  skillSaving.value = true
  try {
    await uploadSkillFileIfNeeded()
    const method = model.value.id ? 'PUT' : 'POST'
    const url = model.value.id ? `/api/share/studio/skills/${model.value.id}` : '/api/share/studio/skills'
    const result = await api(url, {
      method,
      body: buildSaveBody()
    })
    model.value = { ...model.value, ...result }
    await loadSkillPreview(result.id)
    toast.success(model.value.id ? 'Skill 已保存' : 'Skill 已创建')
    return result
  } catch (error) {
    toast.error(error.message || '保存失败')
    throw error
  } finally {
    skillSaving.value = false
  }
}

async function publishSkill(skill) {
  const result = await api(`/api/share/studio/skills/${skill.id}/publish`, { method: 'POST' })
  if (model.value.id === skill.id) {
    model.value = { ...model.value, ...result }
  }
  toast.success('Skill 已公开发布')
  await load()
}

async function publishPrivateSkill(skill) {
  const result = await api(`/api/share/studio/skills/${skill.id}/publish-private`, { method: 'POST' })
  if (model.value.id === skill.id) {
    model.value = { ...model.value, ...result }
  }
  toast.success('Skill 已设为不公开')
  await load()
}

async function unpublishSkill(skill) {
  const result = await api(`/api/share/studio/skills/${skill.id}/unpublish`, { method: 'POST' })
  if (model.value.id === skill.id) {
    model.value = { ...model.value, ...result }
  }
  toast.success('Skill 已下线')
  await load()
}

async function toggleOnlineSkill(skill) {
  if (Number(skill.publishStatus) !== 0) {
    await unpublishSkill(skill)
    return
  }
  await publishPrivateSkill(skill)
}

async function toggleVisibilitySkill(skill) {
  if (Number(skill.publishStatus) === 0) {
    toast.warning('先上线，再设置公开范围')
    return
  }
  if (Number(skill.publishStatus) === 2) {
    await publishPrivateSkill(skill)
    return
  }
  await publishSkill(skill)
}
async function onResourceFileChange(event) {
  const file = event.target.files?.[0]
  if (!file) {
    resourceSelectedFile.value = null
    return
  }
  if (!file.name.toLowerCase().endsWith('.md')) {
    toast.warning('Resource 当前只支持 Markdown (.md) 文件')
    event.target.value = ''
    resourceSelectedFile.value = null
    return
  }
  resourceSelectedFile.value = file
  model.value.fileName = file.name
  model.value.fileSize = file.size
  if (!model.value.name) {
    model.value.name = file.name.replace(/\.md$/i, '')
  }
  resourcePreviewText.value = await file.text()
}

async function loadResourcePreview(resourceId = model.value.id) {
  if (!resourceId) {
    resourcePreviewText.value = ''
    return
  }
  resourcePreviewLoading.value = true
  try {
    const presign = await api(`/api/share/studio/resources/${resourceId}/presign-download`)
    const response = await fetch(presign.downloadUrl)
    if (!response.ok) {
      throw new Error(`文档读取失败：${response.status}`)
    }
    resourcePreviewText.value = await response.text()
  } catch (error) {
    resourcePreviewText.value = error.message || '文档读取失败'
  } finally {
    resourcePreviewLoading.value = false
  }
}

async function uploadResourceFileIfNeeded() {
  if (!resourceSelectedFile.value) {
    return
  }
  if (!model.value.resourceUri) {
    throw new Error('请先填写 Resource URI')
  }
  const file = resourceSelectedFile.value
  const presign = await api('/api/share/studio/resources/presign-upload', {
    method: 'POST',
    body: {
      resourceUri: model.value.resourceUri,
      fileName: file.name,
      fileSize: file.size
    }
  })
  const uploadResponse = await fetch(presign.uploadUrl, {
    method: 'PUT',
    body: file
  })
  if (!uploadResponse.ok) {
    throw new Error(`Markdown 上传失败：${uploadResponse.status}`)
  }
  model.value.objectKey = presign.objectKey
  model.value.fileName = file.name
  model.value.fileSize = file.size
  resourceSelectedFile.value = null
}

async function saveResourceEditor() {
  resourceSaving.value = true
  try {
    await uploadResourceFileIfNeeded()
    const method = model.value.id ? 'PUT' : 'POST'
    const url = model.value.id ? `/api/share/studio/resources/${model.value.id}` : '/api/share/studio/resources'
    const result = await api(url, {
      method,
      body: buildSaveBody()
    })
    model.value = { ...model.value, ...result }
    await loadResourcePreview(result.id)
    toast.success(model.value.id ? 'Resource 已保存' : 'Resource 已创建')
    return result
  } catch (error) {
    toast.error(error.message || '保存失败')
    throw error
  } finally {
    resourceSaving.value = false
  }
}

async function publishResource(resource) {
  const result = await api(`/api/share/studio/resources/${resource.id}/publish`, { method: 'POST' })
  if (model.value.id === resource.id) {
    model.value = { ...model.value, ...result }
  }
  toast.success('Resource 已公开发布')
  await load()
}

async function publishPrivateResource(resource) {
  const result = await api(`/api/share/studio/resources/${resource.id}/publish-private`, { method: 'POST' })
  if (model.value.id === resource.id) {
    model.value = { ...model.value, ...result }
  }
  toast.success('Resource 已设为不公开')
  await load()
}

async function unpublishResource(resource) {
  const result = await api(`/api/share/studio/resources/${resource.id}/unpublish`, { method: 'POST' })
  if (model.value.id === resource.id) {
    model.value = { ...model.value, ...result }
  }
  toast.success('Resource 已下线')
  await load()
}

async function toggleOnlineResource(resource) {
  if (Number(resource.publishStatus) !== 0) {
    await unpublishResource(resource)
    return
  }
  await publishPrivateResource(resource)
}

async function toggleVisibilityResource(resource) {
  if (Number(resource.publishStatus) === 0) {
    toast.warning('先上线，再设置公开范围')
    return
  }
  if (Number(resource.publishStatus) === 2) {
    await publishPrivateResource(resource)
    return
  }
  await publishResource(resource)
}
async function publishTool(tool) {
  const result = await api(`/api/share/studio/tools/${tool.id}/publish`, { method: 'POST' })
  if (model.value.id === tool.id) {
    model.value = { ...model.value, ...result }
  }
  toast.success('Tool 已公开发布')
  await load()
}
async function publishPrivateTool(tool) {
  const result = await api(`/api/share/studio/tools/${tool.id}/publish-private`, { method: 'POST' })
  if (model.value.id === tool.id) {
    model.value = { ...model.value, ...result }
  }
  toast.success('Tool 已设为不公开')
  await load()
}
async function unpublishTool(tool) {
  const result = await api(`/api/share/studio/tools/${tool.id}/unpublish`, { method: 'POST' })
  if (model.value.id === tool.id) {
    model.value = { ...model.value, ...result }
  }
  toast.success('Tool 已下线')
  await load()
}
async function toggleOnlineTool(tool) {
  if (Number(tool.publishStatus) !== 0) {
    await unpublishTool(tool)
    return
  }

  await publishPrivateTool(tool)
}
async function toggleVisibilityTool(tool) {
  if (Number(tool.publishStatus) === 0) {
    toast.warning('先上线，再设置公开范围')
    return
  }
  if (Number(tool.publishStatus) === 2) {
    await publishPrivateTool(tool)
    return
  }

  await publishTool(tool)
}
async function publishApi(apiConfig) {
  await api(`/api/share/studio/apis/${apiConfig.id}/publish`, { method: 'POST' })
  toast.success('已公开发布')
  await load()
}
async function publishPrivateApi(apiConfig) {
  await api(`/api/share/studio/apis/${apiConfig.id}/publish-private`, { method: 'POST' })
  toast.success('已设为不公开')
  await load()
}
async function unpublishApi(apiConfig) {
  await api(`/api/share/studio/apis/${apiConfig.id}/unpublish`, { method: 'POST' })
  toast.success('已下架为草稿')
  await load()
}
async function toggleOnlineApi(apiConfig) {
  if (Number(apiConfig.publishStatus) === 0) {
    await publishPrivateApi(apiConfig)
    return
  }

  await unpublishApi(apiConfig)
}
async function toggleVisibilityApi(apiConfig) {
  if (Number(apiConfig.publishStatus) === 0) {
    toast.warning('先上线，再设置公开范围')
    return
  }
  if (Number(apiConfig.publishStatus) === 2) {
    await publishPrivateApi(apiConfig)
    return
  }

  await publishApi(apiConfig)
}
function showLogin() {
  loggedIn.value = false
  rows.value = []
  drawer.value = false
  userRoleOpen.value = false
  toolPermissionOpen.value = false
  promptPermissionOpen.value = false
  resourcePermissionOpen.value = false
  tokenSelectionOpen.value = false
  tokenPromptSelectionOpen.value = false
  tokenResourceSelectionOpen.value = false
  dynamicToolDetailOpen.value = false
  auditDetailOpen.value = false
  apiDebugOpen.value = false
  rawToken.value = ''
}

onMounted(() => {
  window.addEventListener(UNAUTHORIZED_EVENT, showLogin)
  window.addEventListener('popstate', syncPageFromLocation)

  if (loggedIn.value) {
    load()
  }
})

onUnmounted(() => {
  window.removeEventListener(UNAUTHORIZED_EVENT, showLogin)
  window.removeEventListener('popstate', syncPageFromLocation)
})

watch(selectedToolRequestKeys, () => {
  tryAutoGenerateToolAssets()
  generateToolDebugParamsFromSelection()
})

watch(selectedToolDataSourceIds, () => {
  tryAutoGenerateToolAssets()
})

watch(selectedPromptToolNames, () => {
  syncPromptToolsIntoTemplate()
})

function loginSuccess() {
  loggedIn.value = true
  load()
}
</script>
<template>
  <LoginView v-if="!loggedIn" @success="loginSuccess" />

  <div v-else :class="['app', { collapsed: sidebarCollapsed }]">
    <!-- ---------------------------------------------------------------- nav -->
    <aside class="sidebar">
      <div class="sidebar-head">
        <span class="brand-mark">B</span>
        <div class="brand-text">
          <b>Bear MCP</b>
          <small>管理控制台</small>
        </div>
        <button class="btn btn-ghost btn-icon" style="margin-left: auto" title="折叠菜单" @click="toggleSidebar">
          <Icon name="menu" />
        </button>
      </div>

      <nav class="sidebar-nav">
        <div class="nav-caption">治理</div>
        <button
          v-for="item in menu"
          :key="item[0]"
          :class="['nav-item', { active: page === item[0] }]"
          @click="changePage(item[0])"
        >
          <Icon :name="item[2]" :size="15" />
          <span class="nav-label">{{ item[1] }}</span>
        </button>

        <div class="nav-caption">创作空间</div>
        <button
          v-for="item in [['studioHome','首页','home'],['studioSkills','Skills','book'],['studioTools','Tools','zap'],['studioPrompts','Prompts','file'],['studioResources','Resources','layers'],['studioApis','API','globe']]"
          :key="item[0]"
          :class="['nav-item', { active: page === item[0] || page === item[0] + 'Edit' }]"
          @click="changePage(item[0])"
        >
          <Icon :name="item[2]" :size="15" />
          <span class="nav-label">{{ item[1] }}</span>
        </button>

        <div class="nav-caption">社区</div>
        <button
          v-for="item in [['shareHome','发现','sparkles'],['shareSkills','Skills','book'],['shareTools','Tools','zap'],['sharePrompts','Prompts','file'],['shareResources','Resources','layers'],['shareApis','API','globe'],['shareConfig','我的 MCP 配置','settings'],['shareTokens','Token 管理','key']]"
          :key="item[0]"
          :class="['nav-item', { active: page === item[0] }]"
          @click="changePage(item[0])"
        >
          <Icon :name="item[2]" :size="15" />
          <span class="nav-label">{{ item[1] }}</span>
        </button>
      </nav>

      <div class="sidebar-foot">
        <div class="status-line"><span class="status-dot"></span><span>MCP Server Online</span></div>
        <span>v0.1.0 · Admin Console</span>
      </div>
    </aside>

    <div class="main">
      <!-- ------------------------------------------------------------ topbar -->
      <header class="topbar">
        <div class="breadcrumb">
          <span>{{ navSections[page] || 'MCP 管理后台' }}</span>
          <span class="sep">/</span>
          <span class="strong" style="color: hsl(var(--foreground))">{{ title }}</span>
        </div>

        <label class="topbar-search">
          <Icon name="search" :size="14" />
          <input v-model="navQuery" placeholder="搜索页面、工具或配置" />
          <span class="kbd">⌘K</span>
        </label>

        <div class="topbar-actions">
          <button class="btn btn-ghost btn-sm" @click="changePage('shareHome')">
            <Icon name="sparkles" :size="14" /> 社区
          </button>
          <button class="btn btn-ghost btn-sm" @click="changePage('studioHome')">
            <Icon name="rocket" :size="14" /> 创作空间
          </button>
          <button class="btn btn-ghost btn-icon" :title="theme === 'dark' ? '切换浅色' : '切换深色'" @click="toggleTheme">
            <Icon :name="theme === 'dark' ? 'sun' : 'moon'" />
          </button>
          <div class="user-chip">
            <span class="avatar">D</span>
            <span>
              <b>demo-admin</b>
              <small>管理员</small>
            </span>
          </div>
          <button class="btn btn-ghost btn-icon" title="退出登录" @click="showLogin">
            <Icon name="logout" />
          </button>
        </div>
      </header>

      <div class="container">
        <!-- --------------------------------------------------------- page head -->
        <header class="page-head">
          <div>
            <div class="page-kicker">{{ navSections[page] || '控制台' }}</div>
            <h1 class="page-title">{{ title }}</h1>
            <p class="page-desc">{{ desc }}</p>
          </div>
          <div class="page-actions">
            <button
              v-if="['users','roles','tokens','requests','dataSources'].includes(page)"
              class="btn btn-primary"
              @click="openCreate"
            >
              <Icon name="plus" :size="15" />
              新建{{ page === 'users' ? '用户' : page === 'roles' ? '角色' : page === 'tokens' ? 'Token' : page === 'requests' ? '请求配置' : '数据源' }}
            </button>
          </div>
        </header>

        <!-- --------------------------------------------------------- dashboard -->
        <template v-if="page === 'dashboard'">
          <div class="stat-grid">
            <div
              v-for="item in [['用户','users','users'],['角色','roles','shield'],['有效 Token','activeTokens','key'],['启用请求','enabledRequests','settings'],['动态工具','enabledDynamicTools','zap'],['今日调用','todayCalls','activity']]"
              :key="item[1]"
              class="stat"
            >
              <div class="stat-top">
                <span class="stat-label">{{ item[0] }}</span>
                <span class="stat-icon"><Icon :name="item[2]" :size="14" /></span>
              </div>
              <div class="stat-value">{{ dashboard[item[1]] || 0 }}</div>
              <div class="stat-note"><span class="status-dot" style="width: 5px; height: 5px"></span> 数据库实时统计</div>
            </div>
          </div>

          <section class="card">
            <div class="card-head">
              <div>
                <div class="card-title">最近调用</div>
                <div class="card-desc">最新 100 条 MCP 能力调用记录</div>
              </div>
              <div class="card-tools">
                <button class="btn btn-outline btn-sm" :disabled="loading" @click="load">
                  <Icon name="refresh" :size="13" /> 刷新
                </button>
              </div>
            </div>
            <div class="table-wrap">
              <table class="dt">
                <thead>
                  <tr>
                    <th>时间</th>
                    <th>类型</th>
                    <th>名称 / 标识</th>
                    <th>动作</th>
                    <th>用户</th>
                    <th>状态</th>
                    <th style="text-align: right">耗时(ms)</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="row in rows" :key="row.id">
                    <td class="cell-mono">{{ row.createTime }}</td>
                    <td><span class="badge badge-outline">{{ capabilityTypeLabel(row.capabilityType) }}</span></td>
                    <td><span class="cell-truncate" :title="row.capabilityName">{{ compactText(row.capabilityName, 42) }}</span></td>
                    <td>{{ capabilityActionLabel(row.capabilityAction) }}</td>
                    <td>{{ row.userName || '-' }}</td>
                    <td>
                      <span :class="['badge', String(row.status).toUpperCase() === 'SUCCESS' ? 'badge-success' : 'badge-danger']">{{ row.status || '-' }}</span>
                    </td>
                    <td class="cell-mono" style="text-align: right">{{ row.durationMs ?? '-' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div v-if="!loading && !rows.length" class="empty">
              <span class="empty-icon"><Icon name="activity" :size="18" /></span>
              <div class="empty-title">还没有调用记录</div>
              <div class="empty-desc">通过 MCP Client 调用一次工具后，这里会出现审计流水。</div>
            </div>
          </section>
        </template>

        <!-- ------------------------------------------------------ admin lists -->
        <template v-else-if="['users','roles','tokens','requests','dataSources','resources','tools','audits'].includes(page)">
          <section class="card">
            <div class="card-head">
              <div>
                <div class="card-title">{{ title }}列表</div>
                <div class="card-desc">共 {{ rows.length }} 条记录<span v-if="page === 'tools' || page === 'resources'"> · 由创作空间发布</span></div>
              </div>
              <div class="card-tools">
                <label class="search-input">
                  <Icon name="search" :size="13" />
                  <input
                    v-model="tableQuery"
                    class="input"
                    :placeholder="page === 'audits' ? '搜索能力名称、类型或用户' : '搜索名称或编码'"
                  />
                </label>
                <button class="btn btn-outline btn-sm" :disabled="loading" @click="load">
                  <Icon name="refresh" :size="13" /> 刷新
                </button>
              </div>
            </div>

            <div class="table-wrap">
              <table class="dt">
                <thead>
                  <tr>
                    <th v-for="col in dataColumns" :key="col.dataIndex">{{ col.title }}</th>
                    <th style="text-align: right">操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="row in tableRows" :key="row.id">
                    <td v-for="col in dataColumns" :key="col.dataIndex">
                      <span class="cell-truncate" :title="String(formatTableCell(col.dataIndex, row[col.dataIndex], row) ?? '')">
                        {{ formatTableCell(col.dataIndex, row[col.dataIndex], row) }}
                      </span>
                    </td>
                    <td class="cell-actions">
                      <template v-if="page === 'users'">
                        <button class="btn btn-outline btn-sm" @click="updateUserRoles(row)">分配角色</button>
                        <button class="btn btn-ghost btn-sm" @click="edit(row)">编辑</button>
                      </template>
                      <template v-else-if="page === 'roles'">
                        <button class="btn btn-outline btn-sm" @click="updateRoleTools(row)">工具</button>
                        <button class="btn btn-outline btn-sm" @click="updateRolePrompts(row)">Prompt</button>
                        <button class="btn btn-outline btn-sm" @click="updateRoleResources(row)">Resource</button>
                        <button class="btn btn-ghost btn-sm" @click="edit(row)">编辑</button>
                      </template>
                      <template v-else-if="page === 'tokens'">
                        <button class="btn btn-outline btn-sm" @click="updateSelections(row)">工具</button>
                        <button class="btn btn-outline btn-sm" @click="updateTokenPromptSelections(row)">Prompt</button>
                        <button class="btn btn-outline btn-sm" @click="updateTokenResourceSelections(row)">Resource</button>
                        <button class="btn btn-ghost btn-sm" @click="edit(row)">编辑</button>
                      </template>
                      <template v-else-if="page === 'tools'">
                        <button class="btn btn-outline btn-sm" @click="showDynamicToolDetail(row)">查看详情</button>
                      </template>
                      <template v-else-if="page === 'resources'">
                        <button class="btn btn-outline btn-sm" @click="openResourceEditor(row)">查看资源</button>
                        <button
                          :class="['btn', 'btn-sm', Number(row.enabled) === 1 ? 'btn-danger' : 'btn-outline']"
                          @click="toggleResourceEnabled(row)"
                        >{{ Number(row.enabled) === 1 ? '禁用' : '启用' }}</button>
                      </template>
                      <template v-else-if="page === 'audits'">
                        <button class="btn btn-outline btn-sm" @click="showAuditDetail(row)">查看详情</button>
                      </template>
                      <template v-else>
                        <button class="btn btn-outline btn-sm" @click="edit(row)">编辑</button>
                        <button v-if="page === 'dataSources'" class="btn btn-danger btn-sm" @click="removeRow(row)">删除</button>
                      </template>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-if="!loading && !tableRows.length" class="empty">
              <span class="empty-icon"><Icon name="box" :size="18" /></span>
              <div class="empty-title">暂无{{ title }}数据</div>
              <div class="empty-desc">
                {{ page === 'tools' ? '动态工具在「创作空间 → Tools」里创建并发布后会出现在这里。' : '点击右上角新建，或先检查数据库初始化脚本是否已执行。' }}
              </div>
            </div>
            <div v-else-if="tableRows.length" class="table-foot">
              <span>显示 {{ tableRows.length }} / {{ rows.length }} 条</span>
              <span>搜索对当前页全部字段生效</span>
            </div>
          </section>
        </template>
        <!-- ------------------------------------------------------ share hub -->
        <template v-else-if="page === 'shareHome'">
          <div class="stat-grid four">
            <div v-for="item in [['Skills',communitySkills.length,'book'],['Tools',communityTools.length + communityBuiltinTools.length,'zap'],['Prompts',communityPrompts.length,'file'],['Resources',communityResources.length,'layers'],['API',communityApis.length,'globe']]" :key="item[0]" class="stat">
              <div class="stat-top"><span class="stat-label">{{ item[0] }}</span><span class="stat-icon"><Icon :name="item[2]" :size="14" /></span></div>
              <div class="stat-value">{{ item[1] }}</div>
              <div class="stat-note">社区已发布能力</div>
            </div>
          </div>

          <section class="card">
            <div class="card-head">
              <div>
                <div class="card-title">能力点赞榜</div>
                <div class="card-desc">按点赞数排序，点击任意条目查看详情</div>
              </div>
              <div class="card-tools">
                <button class="btn btn-outline btn-sm" :disabled="loading" @click="load"><Icon name="refresh" :size="13" /> 刷新</button>
              </div>
            </div>
            <div class="card-body">
              <div class="grid-cards four">
                <div v-for="section in communityRankSections" :key="section.key" class="panel">
                  <div class="panel-head">
                    <b>{{ section.title }}</b>
                    <button class="btn btn-ghost btn-sm" @click="changePage(section.page)">全部</button>
                  </div>
                  <div class="panel-body" style="padding: 6px">
                    <button v-for="item in section.items" :key="item.id" class="list-item" @click="section.open(item)">
                      <span class="truncate">{{ section.name(item) }}</span>
                      <span class="tiny muted">{{ likeLabel(item) }}</span>
                    </button>
                    <div v-if="!section.items.length" class="empty" style="padding: 24px 12px">
                      <div class="empty-desc">还没有数据</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </template>

        <!-- --------------------------------------------- share: skills etc. -->
        <template v-else-if="['shareSkills','shareTools','sharePrompts','shareResources','shareApis'].includes(page)">
          <section class="card">
            <div class="card-head">
              <div>
                <div class="card-title">{{ title }} 市场</div>
                <div class="card-desc">{{ desc }}</div>
              </div>
              <div class="card-tools">
                <label class="search-input">
                  <Icon name="search" :size="13" />
                  <input v-model="communityKeyword" class="input" placeholder="搜索名称、编码或描述" />
                </label>
                <div v-if="page === 'shareTools'" class="segmented">
                  <button :class="{ active: communityToolTypeFilter === 'all' }" @click="communityToolTypeFilter = 'all'">全部</button>
                  <button :class="{ active: communityToolTypeFilter === 'dynamic' }" @click="communityToolTypeFilter = 'dynamic'">动态</button>
                  <button :class="{ active: communityToolTypeFilter === 'builtin' }" @click="communityToolTypeFilter = 'builtin'">内置</button>
                </div>
                <button class="btn btn-outline btn-sm" :disabled="loading" @click="load"><Icon name="refresh" :size="13" /> 刷新</button>
              </div>
            </div>

            <div class="card-body">
              <div class="grid-cards">
                <!-- skills -->
                <template v-if="page === 'shareSkills'">
                  <article v-for="item in filteredCommunitySkills" :key="item.id" class="tile">
                    <div class="tile-head">
                      <span class="tile-kicker"><Icon name="book" :size="13" /> Skill</span>
                      <button class="btn btn-ghost btn-sm" @click="toggleCommunityLike(item)">{{ likeLabel(item) }}</button>
                    </div>
                    <div class="tile-title">{{ item.name || item.skillCode }}</div>
                    <div class="tile-desc">{{ item.description || '暂无描述' }}</div>
                    <div class="tile-meta">
                      <code class="inline">{{ item.skillCode || communityCode('SKILL', item.id) }}</code>
                      <span v-if="item.category" class="badge badge-outline">{{ item.category }}</span>
                    </div>
                    <div class="tile-foot">
                      <button class="btn btn-primary btn-sm" @click="openCommunitySkillDetail(item)">查看</button>
                      <button class="btn btn-outline btn-sm" @click="openCursorSkillInstall(item)">Cursor</button>
                      <button class="btn btn-outline btn-sm" @click="copyCodexSkillInstall(item)">Codex</button>
                      <button class="btn btn-ghost btn-sm" @click="downloadSkillZip(item)"><Icon name="download" :size="13" /> ZIP</button>
                    </div>
                  </article>
                  <div v-if="!filteredCommunitySkills.length" class="empty">
                    <span class="empty-icon"><Icon name="book" :size="18" /></span>
                    <div class="empty-title">还没有公开的 Skill</div>
                    <div class="empty-desc">在创作空间创建 Skill 并选择「公开」，就会出现在这里。</div>
                    <button class="btn btn-primary btn-sm" @click="changePage('studioSkills')">去创建</button>
                  </div>
                </template>

                <!-- tools -->
                <template v-else-if="page === 'shareTools'">
                  <article v-for="item in filteredCommunityTools" :key="communityToolCode(item)" class="tile">
                    <div class="tile-head">
                      <span class="tile-kicker"><Icon name="zap" :size="13" /> {{ item.communityType === 'builtin' ? '内置工具' : '动态工具' }}</span>
                      <button class="btn btn-ghost btn-sm" @click="toggleCommunityLike(item)">{{ likeLabel(item) }}</button>
                    </div>
                    <div class="tile-title">{{ item.displayName || item.toolName }}</div>
                    <div class="tile-desc">{{ item.displayDescription || '暂无描述' }}</div>
                    <div class="tile-meta">
                      <code class="inline">{{ communityToolCode(item) }}</code>
                      <span class="badge badge-outline">{{ communityToolCountLabel(item) }}</span>
                    </div>
                    <div class="tile-foot">
                      <button class="btn btn-primary btn-sm" @click="openCommunityToolDetail(item)">查看</button>
                    </div>
                  </article>
                  <div v-if="!filteredCommunityTools.length" class="empty">
                    <span class="empty-icon"><Icon name="zap" :size="18" /></span>
                    <div class="empty-title">还没有匹配的工具</div>
                    <div class="empty-desc">调整筛选条件，或到创作空间发布动态工具。</div>
                  </div>
                </template>

                <!-- prompts -->
                <template v-else-if="page === 'sharePrompts'">
                  <article v-for="item in filteredCommunityPrompts" :key="item.id" class="tile">
                    <div class="tile-head">
                      <span class="tile-kicker"><Icon name="file" :size="13" /> Prompt</span>
                      <button class="btn btn-ghost btn-sm" @click="toggleCommunityLike(item)">{{ likeLabel(item) }}</button>
                    </div>
                    <div class="tile-title">{{ item.title || item.promptName }}</div>
                    <div class="tile-desc">{{ item.description || '暂无描述' }}</div>
                    <div class="tile-meta">
                      <code class="inline">{{ item.promptName }}</code>
                      <span class="badge badge-outline">{{ parseJsonArray(item.argumentsSchema).length }} 个参数</span>
                    </div>
                    <div class="tile-foot">
                      <button class="btn btn-primary btn-sm" @click="openCommunityPromptDetail(item)">查看</button>
                    </div>
                  </article>
                  <div v-if="!filteredCommunityPrompts.length" class="empty">
                    <span class="empty-icon"><Icon name="file" :size="18" /></span>
                    <div class="empty-title">还没有公开的 Prompt</div>
                    <div class="empty-desc">Prompt 模板可以在创作空间里编辑并公开。</div>
                    <button class="btn btn-primary btn-sm" @click="changePage('studioPrompts')">去创建</button>
                  </div>
                </template>

                <!-- resources -->
                <template v-else-if="page === 'shareResources'">
                  <article v-for="item in filteredCommunityResources" :key="item.id" class="tile">
                    <div class="tile-head">
                      <span class="tile-kicker"><Icon name="layers" :size="13" /> Resource</span>
                      <button class="btn btn-ghost btn-sm" @click="toggleCommunityLike(item)">{{ likeLabel(item) }}</button>
                    </div>
                    <div class="tile-title">{{ item.name }}</div>
                    <div class="tile-desc">{{ item.description || '暂无描述' }}</div>
                    <div class="tile-meta">
                      <code class="inline">{{ item.resourceUri }}</code>
                      <span class="badge badge-outline">{{ item.mimeType || 'text/markdown' }}</span>
                    </div>
                    <div class="tile-foot">
                      <button class="btn btn-primary btn-sm" @click="openCommunityResourceDetail(item)">查看</button>
                    </div>
                  </article>
                  <div v-if="!filteredCommunityResources.length" class="empty">
                    <span class="empty-icon"><Icon name="layers" :size="18" /></span>
                    <div class="empty-title">还没有公开的 Resource</div>
                    <div class="empty-desc">Resource 用于给 MCP Client 提供只读知识片段。</div>
                    <button class="btn btn-primary btn-sm" @click="changePage('studioResources')">去创建</button>
                  </div>
                </template>

                <!-- apis -->
                <template v-else>
                  <article v-for="item in filteredCommunityApis" :key="item.id" class="tile">
                    <div class="tile-head">
                      <span class="tile-kicker"><Icon name="globe" :size="13" /> API</span>
                      <button class="btn btn-ghost btn-sm" @click="toggleCommunityLike(item)">{{ likeLabel(item) }}</button>
                    </div>
                    <div class="tile-title">{{ item.name }}</div>
                    <div class="tile-desc">{{ item.description || '暂无描述' }}</div>
                    <div class="tile-meta">
                      <span class="badge badge-primary">{{ item.method || 'GET' }}</span>
                      <code class="inline">{{ item.configKey }}</code>
                      <span v-if="item.category" class="badge badge-outline">{{ item.category }}</span>
                    </div>
                    <div class="tile-foot">
                      <button class="btn btn-primary btn-sm" @click="openCommunityApiDetail(item)">查看</button>
                    </div>
                  </article>
                  <div v-if="!filteredCommunityApis.length" class="empty">
                    <span class="empty-icon"><Icon name="globe" :size="18" /></span>
                    <div class="empty-title">还没有公开的 API</div>
                    <div class="empty-desc">先把外部 HTTP 接口接入创作空间，再发布为能力。</div>
                    <button class="btn btn-primary btn-sm" @click="changePage('studioApis')">去创建</button>
                  </div>
                </template>
              </div>
            </div>
          </section>
        </template>

        <!-- --------------------------------------------------- share config -->
        <template v-else-if="page === 'shareConfig'">
          <div class="split">
            <section class="card">
              <div class="card-head">
                <div class="card-title">我的 Token</div>
                <div class="card-tools">
                  <button class="btn btn-ghost btn-sm" @click="changePage('shareTokens')">管理</button>
                </div>
              </div>
              <div class="card-body tight">
                <div class="list">
                  <button
                    v-for="token in rows"
                    :key="token.id"
                    :class="['list-item', { active: activeConfigTokenId === token.id }]"
                    @click="activeConfigTokenId = token.id; applyConfigTokenSelections()"
                  >
                    <span class="truncate">{{ token.tokenName }}</span>
                    <span :class="['badge', Number(token.isActive) === 1 ? 'badge-success' : 'badge-outline']">{{ Number(token.isActive) === 1 ? '启用' : '禁用' }}</span>
                  </button>
                  <div v-if="!rows.length" class="empty" style="padding: 24px 12px">
                    <div class="empty-title">还没有 Token</div>
                    <div class="empty-desc">先创建一个 Token，再来挑选能力。</div>
                    <button class="btn btn-primary btn-sm" @click="changePage('shareTokens')">新建 Token</button>
                  </div>
                </div>
              </div>
            </section>

            <section class="card">
              <div class="card-head">
                <div>
                  <div class="card-title">能力选择</div>
                  <div class="card-desc">{{ activeConfigTokenId ? '勾选要暴露给这个 Token 的能力，保存后立即生效' : '请先在左侧选择一个 Token' }}</div>
                </div>
                <div class="card-tools">
                  <button class="btn btn-outline btn-sm" :disabled="!activeConfigTokenId" @click="installShareConfigToCursor">
                    <Icon name="terminal" :size="13" /> 一键配置到 Cursor
                  </button>
                  <button class="btn btn-primary btn-sm" :disabled="!activeConfigTokenId" @click="saveShareMcpConfig">
                    <Icon name="save" :size="13" /> 保存配置
                  </button>
                </div>
              </div>

              <div class="card-body">
                <div class="section">
                  <div class="section-head">
                    <div class="section-title">Tools</div>
                    <div style="display: flex; gap: 6px">
                      <button class="btn btn-ghost btn-sm" @click="selectShareToolGroup('builtin')">全选内置</button>
                      <button class="btn btn-ghost btn-sm" @click="clearShareToolGroup('builtin')">清空内置</button>
                      <button class="btn btn-ghost btn-sm" @click="selectShareToolGroup('dynamic')">全选动态</button>
                      <button class="btn btn-ghost btn-sm" @click="clearShareToolGroup('dynamic')">清空动态</button>
                      <button class="btn btn-ghost btn-sm" @click="selectShareToolGroup('feishu')">全选飞书</button>
                    </div>
                  </div>
                  <div class="check-grid three">
                    <label v-for="tool in builtinTools" :key="'b-' + tool.name" class="check-item">
                      <input v-model="selectedTokenTools" type="checkbox" :value="tool.name" />
                      <span><b>{{ tool.name }}</b><br /><small class="muted">{{ tool.description }}</small></span>
                    </label>
                    <label v-for="tool in dynamicToolOptions" :key="'d-' + tool.toolName" class="check-item">
                      <input v-model="selectedTokenTools" type="checkbox" :value="tool.toolName" />
                      <span><b>{{ tool.toolName }}</b><br /><small class="muted">{{ tool.toolDescription || '动态工具' }}</small></span>
                    </label>
                    <label v-for="tool in feishuToolOptions" :key="'f-' + tool.toolName" class="check-item">
                      <input v-model="selectedTokenTools" type="checkbox" :value="tool.toolName" />
                      <span><b>{{ tool.toolName }}</b><br /><small class="muted">{{ tool.toolDescription || '飞书工具' }}</small></span>
                    </label>
                  </div>
                </div>

                <div class="section">
                  <div class="section-head">
                    <div class="section-title">Prompts</div>
                    <div style="display: flex; gap: 6px">
                      <button class="btn btn-ghost btn-sm" @click="selectedTokenPrompts = selectablePromptNames()">全选</button>
                      <button class="btn btn-ghost btn-sm" @click="selectedTokenPrompts = []">清空</button>
                    </div>
                  </div>
                  <div class="check-grid">
                    <label v-for="prompt in promptOptions" :key="prompt.promptName" class="check-item">
                      <input v-model="selectedTokenPrompts" type="checkbox" :value="prompt.promptName" />
                      <span><b>{{ prompt.title || prompt.promptName }}</b><br /><small class="muted">{{ prompt.description || prompt.promptName }}</small></span>
                    </label>
                    <div v-if="!promptOptions.length" class="hint">没有可用的 Prompt</div>
                  </div>
                </div>

                <div class="section">
                  <div class="section-head">
                    <div class="section-title">Resources</div>
                    <div style="display: flex; gap: 6px">
                      <button class="btn btn-ghost btn-sm" @click="selectedTokenResources = selectableResourceUris()">全选</button>
                      <button class="btn btn-ghost btn-sm" @click="selectedTokenResources = []">清空</button>
                    </div>
                  </div>
                  <div class="check-grid">
                    <label v-for="resource in resourceOptions" :key="resource.resourceUri" class="check-item">
                      <input v-model="selectedTokenResources" type="checkbox" :value="resource.resourceUri" />
                      <span><b>{{ resource.name }}</b><br /><small class="muted">{{ resource.resourceUri }}</small></span>
                    </label>
                    <div v-if="!resourceOptions.length" class="hint">没有可用的 Resource</div>
                  </div>
                </div>
              </div>
            </section>
          </div>
        </template>

        <!-- ---------------------------------------------------- share tokens -->
        <template v-else-if="page === 'shareTokens'">
          <section class="card">
            <div class="card-head">
              <div>
                <div class="card-title">我的 Token</div>
                <div class="card-desc">共 {{ rows.length }} 个 · 新建后会弹出一次性明文，请立即保存</div>
              </div>
              <div class="card-tools">
                <button class="btn btn-primary btn-sm" @click="openCreate"><Icon name="plus" :size="13" /> 新建 Token</button>
                <button class="btn btn-outline btn-sm" :disabled="loading" @click="load"><Icon name="refresh" :size="13" /> 刷新</button>
              </div>
            </div>
            <div class="card-body">
              <div class="grid-cards">
                <article v-for="token in rows" :key="token.id" class="tile">
                  <div class="tile-head">
                    <span class="tile-kicker"><Icon name="key" :size="13" /> Token</span>
                    <span :class="['badge', Number(token.isActive) === 1 ? 'badge-success' : 'badge-outline']">{{ Number(token.isActive) === 1 ? '启用' : '禁用' }}</span>
                  </div>
                  <div class="tile-title">{{ token.tokenName }}</div>
                  <div class="tile-desc">{{ token.tokenPrefix || '' }}<span class="muted small"> · {{ permissionLabel(token.permissions) }}</span></div>
                  <div class="tile-meta">
                    <span class="badge badge-outline">过期：{{ token.expireTime || '不过期' }}</span>
                    <span class="badge badge-outline">最后使用：{{ token.lastUsedTime || '未使用' }}</span>
                  </div>
                  <div class="tile-foot">
                    <button class="btn btn-outline btn-sm" @click="edit(token)">编辑</button>
                    <button class="btn btn-ghost btn-sm" @click="updateSelections(token)">工具</button>
                    <button class="btn btn-ghost btn-sm" @click="updateTokenPromptSelections(token)">Prompt</button>
                    <button class="btn btn-ghost btn-sm" @click="updateTokenResourceSelections(token)">Resource</button>
                  </div>
                </article>
                <div v-if="!rows.length" class="empty">
                  <span class="empty-icon"><Icon name="key" :size="18" /></span>
                  <div class="empty-title">还没有 Token</div>
                  <div class="empty-desc">创建一个 Token，即可在 Cursor / Codex 中接入这个 MCP Server。</div>
                  <button class="btn btn-primary btn-sm" @click="openCreate">新建 Token</button>
                </div>
              </div>
            </div>
          </section>
        </template>

        <!-- ---------------------------------------------------- studio home -->
        <template v-else-if="page === 'studioHome'">
          <section class="card">
            <div class="card-body">
              <div class="row between wrap" style="gap: 16px">
                <div>
                  <div class="page-kicker">MCP Studio</div>
                  <h2 style="margin: 4px 0 6px">把内部能力接成 MCP 能力</h2>
                  <p class="muted small" style="margin: 0">请求配置 → 动态工具 → Prompt / Resource / Skill，全部在这里编排。</p>
                </div>
                <div style="display: flex; gap: 8px">
                  <button class="btn btn-primary" @click="changePage('studioApis')"><Icon name="globe" :size="14" /> 接入 API</button>
                  <button class="btn btn-outline" @click="changePage('studioTools')"><Icon name="zap" :size="14" /> 创建 Tool</button>
                </div>
              </div>
            </div>
          </section>

          <div class="stat-grid three" style="margin-top: 16px">
            <div class="stat">
              <div class="stat-top"><span class="stat-label">已接 API</span><span class="stat-icon"><Icon name="globe" :size="14" /></span></div>
              <div class="stat-value">{{ studioApiStats.all }}</div>
              <div class="stat-note">已上线 {{ studioApiStats.online }} · 草稿 {{ studioApiStats.draft }}</div>
            </div>
            <div class="stat">
              <div class="stat-top"><span class="stat-label">动态工具</span><span class="stat-icon"><Icon name="zap" :size="14" /></span></div>
              <div class="stat-value">{{ filteredStudioTools.length }}</div>
              <div class="stat-note">Groovy 脚本编排</div>
            </div>
            <div class="stat">
              <div class="stat-top"><span class="stat-label">发布入口</span><span class="stat-icon"><Icon name="rocket" :size="14" /></span></div>
              <div class="stat-value">{{ filteredStudioSkills.length + filteredStudioPrompts.length + filteredStudioResources.length }}</div>
              <div class="stat-note">Skill / Prompt / Resource</div>
            </div>
          </div>

          <div class="grid-cards" style="margin-top: 16px">
            <article
              v-for="lane in [
                { key: 'studioSkills', title: 'Skills', icon: 'book', desc: '把排查流程、写作规范沉淀成可安装的 Skill 包。', count: filteredStudioSkills.length },
                { key: 'studioTools', title: 'Tools', icon: 'zap', desc: '用 Groovy 把多个 API 组合成一个原子工具。', count: filteredStudioTools.length },
                { key: 'studioPrompts', title: 'Prompts', icon: 'file', desc: '可复用的提示词模板，支持参数与工具白名单。', count: filteredStudioPrompts.length },
                { key: 'studioResources', title: 'Resources', icon: 'layers', desc: '只读知识片段，供客户端按 URI 拉取。', count: filteredStudioResources.length },
                { key: 'studioApis', title: 'API', icon: 'globe', desc: '把外部 HTTP 接口注册成可调试的请求配置。', count: studioApiStats.all }
              ]"
              :key="lane.key"
              class="tile"
            >
              <div class="tile-head">
                <span class="tile-kicker"><Icon :name="lane.icon" :size="13" /> {{ lane.title }}</span>
                <span class="badge badge-outline">{{ lane.count }}</span>
              </div>
              <div class="tile-desc">{{ lane.desc }}</div>
              <div class="tile-foot">
                <button class="btn btn-outline btn-sm" @click="changePage(lane.key)">进入<Icon name="chevron-right" :size="13" /></button>
              </div>
            </article>
          </div>
        </template>
        <!-- ------------------------------------------- studio list: 5 kinds -->
        <template v-else-if="['studioSkills','studioTools','studioPrompts','studioResources','studioApis'].includes(page)">
          <section class="card">
            <div class="card-head">
              <div>
                <div class="card-title">{{ title }}</div>
                <div class="card-desc">{{ desc }}</div>
              </div>
              <div class="card-tools">
                <label class="search-input">
                  <Icon name="search" :size="13" />
                  <input
                    class="input"
                    :placeholder="'搜索 ' + title"
                    :value="page === 'studioSkills' ? skillKeyword : page === 'studioTools' ? toolKeyword : page === 'studioPrompts' ? promptKeyword : page === 'studioResources' ? resourceKeyword : apiKeyword"
                    @input="page === 'studioSkills' ? skillKeyword = $event.target.value : page === 'studioTools' ? toolKeyword = $event.target.value : page === 'studioPrompts' ? promptKeyword = $event.target.value : page === 'studioResources' ? resourceKeyword = $event.target.value : apiKeyword = $event.target.value"
                  />
                </label>
                <div class="segmented">
                  <button
                    :class="{ active: (page === 'studioSkills' ? skillStatusFilter : page === 'studioTools' ? toolStatusFilter : page === 'studioPrompts' ? promptStatusFilter : page === 'studioResources' ? resourceStatusFilter : apiStatusFilter) === 'all' }"
                    @click="page === 'studioSkills' ? skillStatusFilter = 'all' : page === 'studioTools' ? toolStatusFilter = 'all' : page === 'studioPrompts' ? promptStatusFilter = 'all' : page === 'studioResources' ? resourceStatusFilter = 'all' : apiStatusFilter = 'all'"
                  >全部</button>
                  <button
                    :class="{ active: (page === 'studioSkills' ? skillStatusFilter : page === 'studioTools' ? toolStatusFilter : page === 'studioPrompts' ? promptStatusFilter : page === 'studioResources' ? resourceStatusFilter : apiStatusFilter) === 'online' }"
                    @click="page === 'studioSkills' ? skillStatusFilter = 'online' : page === 'studioTools' ? toolStatusFilter = 'online' : page === 'studioPrompts' ? promptStatusFilter = 'online' : page === 'studioResources' ? resourceStatusFilter = 'online' : apiStatusFilter = 'online'"
                  >已上线</button>
                  <button
                    :class="{ active: (page === 'studioSkills' ? skillStatusFilter : page === 'studioTools' ? toolStatusFilter : page === 'studioPrompts' ? promptStatusFilter : page === 'studioResources' ? resourceStatusFilter : apiStatusFilter) === 'draft' }"
                    @click="page === 'studioSkills' ? skillStatusFilter = 'draft' : page === 'studioTools' ? toolStatusFilter = 'draft' : page === 'studioPrompts' ? promptStatusFilter = 'draft' : page === 'studioResources' ? resourceStatusFilter = 'draft' : apiStatusFilter = 'draft'"
                  >草稿</button>
                </div>
                <button class="btn btn-primary btn-sm" @click="openCreate"><Icon name="plus" :size="13" /> 新建</button>
              </div>
            </div>

            <div class="card-body">
              <div class="grid-cards">
                <!-- skills -->
                <template v-if="page === 'studioSkills'">
                  <article v-for="item in filteredStudioSkills" :key="item.id" class="tile">
                    <div class="tile-head">
                      <span class="tile-kicker"><Icon name="book" :size="13" /> Skill</span>
                      <div style="display: flex; gap: 4px">
                        <span :class="['badge', publishClass(item.publishStatus)]">{{ publishLabel(item.publishStatus) }}</span>
                        <span class="badge badge-outline">{{ visibilityLabel(item.publishStatus) }}</span>
                      </div>
                    </div>
                    <div class="tile-title">{{ item.name }}</div>
                    <div class="tile-desc">{{ item.description || '暂无描述' }}</div>
                    <div class="tile-meta">
                      <code class="inline">{{ item.skillCode }}</code>
                      <span v-if="item.category" class="badge badge-outline">{{ item.category }}</span>
                      <span v-if="item.userName" class="badge badge-outline">{{ item.userName }}</span>
                    </div>
                    <div class="tile-foot">
                      <button class="btn btn-outline btn-sm" @click="openSkillEditor(item)">编辑</button>
                      <button class="btn btn-ghost btn-sm" @click="toggleOnlineSkill(item)">{{ onlineActionLabel(item.publishStatus) }}</button>
                      <button class="btn btn-ghost btn-sm" :disabled="Number(item.publishStatus) === 0" @click="toggleVisibilitySkill(item)">{{ visibilityActionLabel(item.publishStatus) }}</button>
                    </div>
                  </article>
                  <div v-if="!filteredStudioSkills.length" class="empty">
                    <span class="empty-icon"><Icon name="book" :size="18" /></span>
                    <div class="empty-title">还没有 Skill</div>
                    <div class="empty-desc">新建 → 上传 SKILL.md → 上线，就能被客户端安装。</div>
                    <button class="btn btn-primary btn-sm" @click="openSkillEditor()">新建 Skill</button>
                  </div>
                </template>

                <!-- tools -->
                <template v-else-if="page === 'studioTools'">
                  <article v-for="item in filteredStudioTools" :key="item.id" class="tile">
                    <div class="tile-head">
                      <span class="tile-kicker"><Icon name="zap" :size="13" /> Tool</span>
                      <div style="display: flex; gap: 4px">
                        <span :class="['badge', publishClass(item.publishStatus)]">{{ publishLabel(item.publishStatus) }}</span>
                        <span class="badge badge-outline">{{ visibilityLabel(item.publishStatus) }}</span>
                      </div>
                    </div>
                    <div class="tile-title">{{ item.toolName }}</div>
                    <div class="tile-desc">{{ item.toolDescription || '暂无描述' }}</div>
                    <div class="tile-meta">
                      <span class="badge badge-outline">{{ linkedRequestLabel(item.linkedRequestKeys) }}</span>
                      <span class="badge badge-outline">{{ linkedDataSourceLabel(item.linkedDataSourceIds) }}</span>
                    </div>
                    <div class="tile-foot">
                      <button class="btn btn-outline btn-sm" @click="openToolEditor(item)">编辑</button>
                      <button class="btn btn-ghost btn-sm" @click="toggleOnlineTool(item)">{{ onlineActionLabel(item.publishStatus) }}</button>
                      <button class="btn btn-ghost btn-sm" :disabled="Number(item.publishStatus) === 0" @click="toggleVisibilityTool(item)">{{ visibilityActionLabel(item.publishStatus) }}</button>
                    </div>
                  </article>
                  <div v-if="!filteredStudioTools.length" class="empty">
                    <span class="empty-icon"><Icon name="zap" :size="18" /></span>
                    <div class="empty-title">还没有动态工具</div>
                    <div class="empty-desc">选择已接入的 API，自动生成 Groovy 脚本与 inputSchema。</div>
                    <button class="btn btn-primary btn-sm" @click="openToolEditor()">新建 Tool</button>
                  </div>
                </template>

                <!-- prompts -->
                <template v-else-if="page === 'studioPrompts'">
                  <article v-for="item in filteredStudioPrompts" :key="item.id" class="tile">
                    <div class="tile-head">
                      <span class="tile-kicker"><Icon name="file" :size="13" /> Prompt</span>
                      <div style="display: flex; gap: 4px">
                        <span :class="['badge', publishClass(item.publishStatus)]">{{ publishLabel(item.publishStatus) }}</span>
                        <span class="badge badge-outline">{{ visibilityLabel(item.publishStatus) }}</span>
                      </div>
                    </div>
                    <div class="tile-title">{{ item.title || item.promptName }}</div>
                    <div class="tile-desc">{{ item.description || '暂无描述' }}</div>
                    <div class="tile-meta">
                      <code class="inline">{{ item.promptName }}</code>
                      <span class="badge badge-outline">{{ parseJsonArray(item.argumentsSchema).length }} 个参数</span>
                    </div>
                    <div class="tile-foot">
                      <button class="btn btn-outline btn-sm" @click="openPromptEditor(item)">编辑</button>
                      <button class="btn btn-ghost btn-sm" @click="toggleOnlinePrompt(item)">{{ onlineActionLabel(item.publishStatus) }}</button>
                      <button class="btn btn-ghost btn-sm" :disabled="Number(item.publishStatus) === 0" @click="toggleVisibilityPrompt(item)">{{ visibilityActionLabel(item.publishStatus) }}</button>
                    </div>
                  </article>
                  <div v-if="!filteredStudioPrompts.length" class="empty">
                    <span class="empty-icon"><Icon name="file" :size="18" /></span>
                    <div class="empty-title">还没有 Prompt</div>
                    <div class="empty-desc">用双花括号占位符书写模板，可绑定工具白名单。</div>
                    <button class="btn btn-primary btn-sm" @click="openPromptEditor()">新建 Prompt</button>
                  </div>
                </template>

                <!-- resources -->
                <template v-else-if="page === 'studioResources'">
                  <article v-for="item in filteredStudioResources" :key="item.id" class="tile">
                    <div class="tile-head">
                      <span class="tile-kicker"><Icon name="layers" :size="13" /> Resource</span>
                      <div style="display: flex; gap: 4px">
                        <span :class="['badge', publishClass(item.publishStatus)]">{{ publishLabel(item.publishStatus) }}</span>
                        <span class="badge badge-outline">{{ visibilityLabel(item.publishStatus) }}</span>
                      </div>
                    </div>
                    <div class="tile-title">{{ item.name }}</div>
                    <div class="tile-desc">{{ item.description || '暂无描述' }}</div>
                    <div class="tile-meta">
                      <code class="inline">{{ item.resourceUri }}</code>
                      <span class="badge badge-outline">{{ item.mimeType || 'text/markdown' }}</span>
                    </div>
                    <div class="tile-foot">
                      <button class="btn btn-outline btn-sm" @click="openResourceEditor(item)">编辑</button>
                      <button class="btn btn-ghost btn-sm" @click="toggleOnlineResource(item)">{{ onlineActionLabel(item.publishStatus) }}</button>
                      <button class="btn btn-ghost btn-sm" :disabled="Number(item.publishStatus) === 0" @click="toggleVisibilityResource(item)">{{ visibilityActionLabel(item.publishStatus) }}</button>
                    </div>
                  </article>
                  <div v-if="!filteredStudioResources.length" class="empty">
                    <span class="empty-icon"><Icon name="layers" :size="18" /></span>
                    <div class="empty-title">还没有 Resource</div>
                    <div class="empty-desc">上传 Markdown 或直接粘贴内容，按 URI 暴露给客户端。</div>
                    <button class="btn btn-primary btn-sm" @click="openResourceEditor()">新建 Resource</button>
                  </div>
                </template>

                <!-- apis -->
                <template v-else>
                  <article v-for="item in filteredStudioApis" :key="item.id" class="tile">
                    <div class="tile-head">
                      <span class="tile-kicker"><Icon name="globe" :size="13" /> API</span>
                      <div style="display: flex; gap: 4px">
                        <span :class="['badge', publishClass(item.publishStatus)]">{{ publishLabel(item.publishStatus) }}</span>
                        <span class="badge badge-outline">{{ visibilityLabel(item.publishStatus) }}</span>
                      </div>
                    </div>
                    <div class="tile-title">{{ item.name }}</div>
                    <div class="tile-desc">{{ item.description || '暂无描述' }}</div>
                    <div class="tile-meta">
                      <span class="badge badge-primary">{{ item.method || 'GET' }}</span>
                      <code class="inline">{{ item.configKey }}</code>
                    </div>
                    <code class="inline block" style="display: block; margin-top: 8px">{{ item.url }}</code>
                    <div class="tile-foot">
                      <button class="btn btn-outline btn-sm" @click="openApiEditor(item)">编辑</button>
                      <button class="btn btn-ghost btn-sm" @click="openApiDebug(item)">调试</button>
                      <button class="btn btn-ghost btn-sm" @click="toggleOnlineApi(item)">{{ onlineActionLabel(item.publishStatus) }}</button>
                      <button class="btn btn-ghost btn-sm" :disabled="Number(item.publishStatus) === 0" @click="toggleVisibilityApi(item)">{{ visibilityActionLabel(item.publishStatus) }}</button>
                    </div>
                  </article>
                  <div v-if="!filteredStudioApis.length" class="empty">
                    <span class="empty-icon"><Icon name="globe" :size="18" /></span>
                    <div class="empty-title">还没有接入 API</div>
                    <div class="empty-desc">填写 URL、Header 与 Body 模板，保存后即可调试并发布。</div>
                    <button class="btn btn-primary btn-sm" @click="openApiEditor()">接入第一个 API</button>
                  </div>
                </template>
              </div>
            </div>
          </section>
        </template>

        <!-- ------------------------------------------------------ API editor -->
        <template v-else-if="page === 'studioApiEdit'">
          <div class="editor">
            <header class="editor-head">
              <div class="editor-head-main">
                <button class="btn btn-ghost btn-sm" @click="changePage('studioApis')"><Icon name="arrow-left" :size="14" /> 返回</button>
                <div>
                  <div class="page-kicker">API Studio</div>
                  <h2 class="page-title" style="font-size: 19px">{{ model.name || model.configKey || '未命名 API' }}</h2>
                </div>
              </div>
              <div class="editor-actions">
                <span :class="['badge', publishClass(model.publishStatus)]">{{ publishLabel(model.publishStatus) }}</span>
                <span class="badge badge-outline">{{ visibilityLabel(model.publishStatus) }}</span>
                <button class="btn btn-outline btn-sm" @click="openApiDebug(model)">调试</button>
                <button class="btn btn-primary btn-sm" :disabled="apiSaving" @click="saveApiEditor">
                  <Icon name="save" :size="13" /> {{ apiSaving ? '保存中…' : '保存' }}
                </button>
              </div>
            </header>

            <div class="editor-board">
              <section class="panel">
                <div class="panel-head">基础信息</div>
                <div class="panel-body">
                  <div class="field"><span class="field-label">配置键 configKey</span><input v-model="model.configKey" class="input" placeholder="demo_user_detail" /></div>
                  <div class="field"><span class="field-label">名称</span><input v-model="model.name" class="input" placeholder="查询演示用户详情" /></div>
                  <div class="field"><span class="field-label">分类</span><input v-model="model.category" class="input" placeholder="演示" /></div>
                  <div class="field-row">
                    <div class="field">
                      <span class="field-label">方法</span>
                      <select v-model="model.method" class="select">
                        <option v-for="opt in httpMethodOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                      </select>
                    </div>
                    <div class="field">
                      <span class="field-label">类型</span>
                      <select v-model="model.type" class="select">
                        <option v-for="opt in requestTypeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                      </select>
                    </div>
                  </div>
                  <div class="field"><span class="field-label">URL</span><input v-model="model.url" class="input" placeholder="http://localhost:8080/api/demo/users/{userId}" /></div>
                  <div class="field"><span class="field-label">描述</span><textarea v-model="model.description" class="textarea" rows="3" placeholder="给调用方看的说明"></textarea></div>
                  <div class="field-row">
                    <div class="field"><span class="field-label">连接超时(ms)</span><input v-model.number="model.connectTimeoutMs" class="input" type="number" /></div>
                    <div class="field"><span class="field-label">读取超时(ms)</span><input v-model.number="model.readTimeoutMs" class="input" type="number" /></div>
                  </div>
                  <div class="field">
                    <span class="field-label">发布状态</span>
                    <select v-model.number="model.publishStatus" class="select">
                      <option v-for="opt in publishStatusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                    </select>
                    <span class="field-hint">草稿不会出现在 Tools 列表；「公开」会进入社区。</span>
                  </div>
                </div>
              </section>

              <section class="panel">
                <div class="panel-head">
                  <div class="tabs">
                    <button :class="['tab', { active: apiEditorTab !== 'response' }]" @click="apiEditorTab = 'request'">请求</button>
                    <button :class="['tab', { active: apiEditorTab === 'response' }]" @click="apiEditorTab = 'response'">调试</button>
                  </div>
                </div>
                <div class="panel-body">
                  <template v-if="apiEditorTab !== 'response'">
                    <div class="field"><span class="field-label">默认参数 paramsDefault (JSON)</span><textarea v-model="model.paramsDefault" class="textarea code-input" rows="6" placeholder='{"userId": 1}'></textarea></div>
                    <div class="field"><span class="field-label">请求头 headers (JSON)</span><textarea v-model="model.headers" class="textarea code-input" rows="5" placeholder='{"Content-Type": "application/json"}'></textarea></div>
                    <div class="field"><span class="field-label">Body 模板（支持双花括号占位）</span><textarea v-model="model.bodyTemplate" class="textarea code-input" rows="8" placeholder='{"keyword": "{keyword}", "page": 1}'></textarea></div>
                  </template>
                  <template v-else>
                    <div class="field"><span class="field-label">调试参数 (JSON)</span><textarea v-model="apiEditorDebugParams" class="textarea code-input" rows="6" placeholder='{"userId": 1}'></textarea></div>
                    <button class="btn btn-primary btn-sm" :disabled="debugLoading" @click="sendApiEditor">
                      <Icon name="play" :size="13" /> {{ debugLoading ? '请求中…' : '发送请求' }}
                    </button>
                    <div class="section">
                      <div class="section-head"><div class="section-title">响应</div><span class="tiny muted">{{ debugSummary }}</span></div>
                      <pre class="code tall">{{ prettyJsonText(debugResult) }}</pre>
                    </div>
                  </template>
                </div>
              </section>
            </div>
          </div>
        </template>

        <!-- ----------------------------------------------------- Tool editor -->
        <template v-else-if="page === 'studioToolEdit'">
          <div class="editor">
            <header class="editor-head">
              <div class="editor-head-main">
                <button class="btn btn-ghost btn-sm" @click="changePage('studioTools')"><Icon name="arrow-left" :size="14" /> 返回</button>
                <div>
                  <div class="page-kicker">Tool Studio</div>
                  <h2 class="page-title" style="font-size: 19px">{{ model.toolName || '未命名 Tool' }}</h2>
                </div>
              </div>
              <div class="editor-actions">
                <span :class="['badge', publishClass(model.publishStatus)]">{{ publishLabel(model.publishStatus) }}</span>
                <button class="btn btn-outline btn-sm" :disabled="toolDebugLoading" @click="sendToolEditor">
                  <Icon name="play" :size="13" /> 调试
                </button>
                <button class="btn btn-primary btn-sm" :disabled="toolSaving" @click="saveToolEditor">
                  <Icon name="save" :size="13" /> {{ toolSaving ? '保存中…' : '保存' }}
                </button>
              </div>
            </header>

            <div class="editor-board">
              <section class="panel">
                <div class="panel-head">基础信息</div>
                <div class="panel-body">
                  <div class="field"><span class="field-label">工具名</span><input v-model="model.toolName" class="input" placeholder="demo_user_lookup" /><span class="field-hint">MCP 调用名，建议 snake_case</span></div>
                  <div class="field"><span class="field-label">描述</span><textarea v-model="model.toolDescription" class="textarea" rows="4" placeholder="给模型看的用途说明"></textarea></div>
                  <div class="field">
                    <span class="field-label">发布状态</span>
                    <select v-model.number="model.publishStatus" class="select">
                      <option v-for="opt in publishStatusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                    </select>
                  </div>
                  <div class="divider"></div>
                  <div class="field">
                    <span class="field-label">关联 API</span>
                    <div class="check-grid">
                      <label v-for="api in toolApiOptions" :key="api.value" class="check-item">
                        <input v-model="selectedToolRequestKeys" type="checkbox" :value="api.value" />
                        <span>{{ api.label }}</span>
                      </label>
                      <div v-if="!toolApiOptions.length" class="hint">还没有已上线的 API</div>
                    </div>
                  </div>
                  <div class="field">
                    <span class="field-label">关联数据源</span>
                    <div class="check-grid">
                      <label v-for="ds in toolDataSourceOptions" :key="ds.value" class="check-item">
                        <input v-model="selectedToolDataSourceIds" type="checkbox" :value="ds.value" />
                        <span>{{ ds.label }}</span>
                      </label>
                      <div v-if="!toolDataSourceOptions.length" class="hint">没有可用数据源</div>
                    </div>
                  </div>
                </div>
              </section>

              <section class="panel">
                <div class="panel-head">
                  <b>Groovy 脚本</b>
                  <div style="display: flex; gap: 6px">
                    <button class="btn btn-ghost btn-sm" @click="generateToolScriptFromSelection">由 API 生成</button>
                    <button class="btn btn-ghost btn-sm" @click="generateToolScriptFromDataSourceSelection">由数据源生成</button>
                    <button class="btn btn-ghost btn-sm" @click="model.groovyScript = defaultToolScript()">重置</button>
                  </div>
                </div>
                <div class="panel-body">
                  <textarea v-model="model.groovyScript" class="textarea code-input" rows="18" spellcheck="false"></textarea>
                  <div class="field" style="margin-top: 12px">
                    <span class="field-label">inputSchema (JSON Schema)</span>
                    <textarea v-model="model.inputSchema" class="textarea code-input" rows="10" spellcheck="false"></textarea>
                    <button class="btn btn-outline btn-sm" style="margin-top: 8px" @click="generateToolSchemaFromScript">从脚本推断 schema</button>
                  </div>
                </div>
              </section>

              <section class="panel">
                <div class="panel-head"><b>调试</b></div>
                <div class="panel-body">
                  <div class="field">
                    <span class="field-label">参数 (JSON)</span>
                    <textarea v-model="toolDebugParams" class="textarea code-input" rows="8" spellcheck="false"></textarea>
                  </div>
                  <button class="btn btn-outline btn-sm btn-block" :disabled="toolDebugLoading" @click="generateToolDebugParamsFromSelection">根据关联能力生成参数</button>
                  <div class="section">
                    <div class="section-head"><div class="section-title">结果</div></div>
                    <pre class="code tall">{{ toolDebugText }}</pre>
                  </div>
                </div>
              </section>
            </div>
          </div>
        </template>
        <!-- ---------------------------------------------------- Skill editor -->
        <template v-else-if="page === 'studioSkillEdit'">
          <div class="editor">
            <header class="editor-head">
              <div class="editor-head-main">
                <button class="btn btn-ghost btn-sm" @click="changePage('studioSkills')"><Icon name="arrow-left" :size="14" /> 返回</button>
                <div>
                  <div class="page-kicker">Skill Studio</div>
                  <h2 class="page-title" style="font-size: 19px">{{ model.name || '未命名 Skill' }}</h2>
                </div>
              </div>
              <div class="editor-actions">
                <span :class="['badge', publishClass(model.publishStatus)]">{{ publishLabel(model.publishStatus) }}</span>
                <button class="btn btn-outline btn-sm" @click="openCursorSkillInstall(model)">安装到 Cursor</button>
                <button class="btn btn-outline btn-sm" @click="copyCodexSkillInstall(model)">安装到 Codex</button>
                <button class="btn btn-primary btn-sm" :disabled="skillSaving" @click="saveSkillEditor">
                  <Icon name="save" :size="13" /> {{ skillSaving ? '保存中…' : '保存' }}
                </button>
              </div>
            </header>

            <div class="editor-board two">
              <section class="panel">
                <div class="panel-head">基础信息</div>
                <div class="panel-body">
                  <div class="field"><span class="field-label">Skill 名称</span><input v-model="model.name" class="input" placeholder="log-triage" /></div>
                  <div class="field"><span class="field-label">编码</span><input v-model="model.skillCode" class="input" placeholder="SKILL0000000005" /><span class="field-hint">留空自动生成，格式 SKILL + 10 位数字</span></div>
                  <div class="field"><span class="field-label">分类</span><input v-model="model.category" class="input" placeholder="运维" /></div>
                  <div class="field"><span class="field-label">描述</span><textarea v-model="model.description" class="textarea" rows="4" placeholder="这个 Skill 解决什么问题"></textarea></div>
                  <div class="field">
                    <span class="field-label">发布状态</span>
                    <select v-model.number="model.publishStatus" class="select">
                      <option v-for="opt in publishStatusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                    </select>
                  </div>
                  <div class="divider"></div>
                  <div class="field">
                    <span class="field-label">SKILL.md 文件</span>
                    <input class="input" type="file" accept=".md,text/markdown" @change="onSkillFileChange" />
                    <span class="field-hint">{{ skillSelectedFile ? '已选择：' + skillSelectedFile.name : (model.fileName ? '当前文件：' + model.fileName : '支持 frontmatter + 正文，保存时自动上传') }}</span>
                  </div>
                  <button class="btn btn-outline btn-sm" :disabled="!model.id" @click="loadSkillPreview">重新加载预览</button>
                </div>
              </section>

              <section class="panel">
                <div class="panel-head"><b>预览</b><span class="tiny muted">{{ model.fileSize || 0 }} B</span></div>
                <div class="panel-body">
                  <div v-if="skillPreviewLoading" class="hint">加载中…</div>
                  <div v-else-if="skillPreviewHtml" class="md" v-html="skillPreviewHtml"></div>
                  <pre v-else-if="skillPreviewText" class="code tall">{{ skillPreviewText }}</pre>
                  <div v-else class="empty">
                    <span class="empty-icon"><Icon name="book" :size="18" /></span>
                    <div class="empty-title">还没有内容</div>
                    <div class="empty-desc">上传 SKILL.md 或先保存一次，即可在这里预览渲染效果。</div>
                  </div>
                </div>
              </section>
            </div>
          </div>
        </template>

        <!-- --------------------------------------------------- Prompt editor -->
        <template v-else-if="page === 'studioPromptEdit'">
          <div class="editor">
            <header class="editor-head">
              <div class="editor-head-main">
                <button class="btn btn-ghost btn-sm" @click="changePage('studioPrompts')"><Icon name="arrow-left" :size="14" /> 返回</button>
                <div>
                  <div class="page-kicker">Prompt Studio</div>
                  <h2 class="page-title" style="font-size: 19px">{{ model.title || model.promptName || '未命名 Prompt' }}</h2>
                </div>
              </div>
              <div class="editor-actions">
                <span :class="['badge', publishClass(model.publishStatus)]">{{ publishLabel(model.publishStatus) }}</span>
                <button class="btn btn-outline btn-sm" :disabled="promptPreviewLoading" @click="previewPromptEditor">
                  <Icon name="play" :size="13" /> 预览
                </button>
                <button class="btn btn-primary btn-sm" :disabled="promptSaving" @click="savePromptEditor">
                  <Icon name="save" :size="13" /> {{ promptSaving ? '保存中…' : '保存' }}
                </button>
              </div>
            </header>

            <div class="editor-board">
              <section class="panel">
                <div class="panel-head">基础信息</div>
                <div class="panel-body">
                  <div class="field"><span class="field-label">Prompt 名称</span><input v-model="model.promptName" class="input" placeholder="demo_text_to_sql_system" /></div>
                  <div class="field"><span class="field-label">标题</span><input v-model="model.title" class="input" placeholder="示例：生成 SQL" /></div>
                  <div class="field"><span class="field-label">描述</span><textarea v-model="model.description" class="textarea" rows="3"></textarea></div>
                  <div class="field">
                    <span class="field-label">发布状态</span>
                    <select v-model.number="model.publishStatus" class="select">
                      <option v-for="opt in publishStatusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                    </select>
                  </div>
                  <div class="divider"></div>
                  <div class="field">
                    <span class="field-label">参数</span>
                    <div v-for="(item, index) in promptArgumentItems" :key="item._key" class="field-row" style="align-items: flex-end; margin-bottom: 8px">
                      <div class="field" style="flex: 1.4">
                        <input class="input" :value="item.name" placeholder="参数名" @input="updatePromptArgument(index, 'name', $event.target.value)" />
                      </div>
                      <div class="field" style="flex: 1.6">
                        <input class="input" :value="item.description" placeholder="说明" @input="updatePromptArgument(index, 'description', $event.target.value)" />
                      </div>
                      <div class="field" style="flex: 1">
                        <input class="input" :value="item.defaultValue" placeholder="默认值" @input="updatePromptArgument(index, 'defaultValue', $event.target.value)" />
                      </div>
                      <label class="check-item" style="flex: none">
                        <input :checked="item.required" type="checkbox" @change="updatePromptArgument(index, 'required', $event.target.checked)" />
                        <span class="small">必填</span>
                      </label>
                      <button class="btn btn-ghost btn-icon" @click="removePromptArgument(index)"><Icon name="trash" :size="14" /></button>
                    </div>
                    <button class="btn btn-outline btn-sm" @click="addPromptArgument"><Icon name="plus" :size="13" /> 添加参数</button>
                  </div>
                  <div class="divider"></div>
                  <div class="field">
                    <span class="field-label">工具白名单</span>
                    <div class="check-grid">
                      <label v-for="tool in promptToolOptions" :key="tool.value" class="check-item">
                        <input v-model="selectedPromptToolNames" type="checkbox" :value="tool.value" />
                        <span>{{ tool.label }}</span>
                      </label>
                      <div v-if="!promptToolOptions.length" class="hint">没有可选工具</div>
                    </div>
                    <button class="btn btn-outline btn-sm" style="margin-top: 8px" @click="insertPromptToolsPlaceholder">插入 tools 占位块</button>
                  </div>
                </div>
              </section>

              <section class="panel">
                <div class="panel-head"><b>模板正文</b></div>
                <div class="panel-body">
                  <textarea v-model="model.templateContent" class="textarea code-input" rows="22" spellcheck="false"></textarea>
                  <div class="section">
                    <div class="section-head"><div class="section-title">预览参数 (JSON)</div></div>
                    <textarea v-model="promptPreviewArgs" class="textarea code-input" rows="5" spellcheck="false"></textarea>
                  </div>
                </div>
              </section>

              <section class="panel">
                <div class="panel-head"><b>渲染结果</b></div>
                <div class="panel-body">
                  <div v-if="promptPreviewLoading" class="hint">渲染中…</div>
                  <pre v-else class="code tall">{{ promptPreviewResult || '点击「预览」生成渲染结果' }}</pre>
                </div>
              </section>
            </div>
          </div>
        </template>

        <!-- ------------------------------------------------- Resource editor -->
        <template v-else-if="page === 'studioResourceEdit'">
          <div class="editor">
            <header class="editor-head">
              <div class="editor-head-main">
                <button class="btn btn-ghost btn-sm" @click="changePage('studioResources')"><Icon name="arrow-left" :size="14" /> 返回</button>
                <div>
                  <div class="page-kicker">Resource Studio</div>
                  <h2 class="page-title" style="font-size: 19px">{{ model.name || '未命名 Resource' }}</h2>
                </div>
              </div>
              <div class="editor-actions">
                <span :class="['badge', publishClass(model.publishStatus)]">{{ publishLabel(model.publishStatus) }}</span>
                <span class="badge badge-outline">{{ visibilityLabel(model.publishStatus) }}</span>
                <button class="btn btn-primary btn-sm" :disabled="resourceSaving" @click="saveResourceEditor">
                  <Icon name="save" :size="13" /> {{ resourceSaving ? '保存中…' : '保存' }}
                </button>
              </div>
            </header>

            <div class="editor-board two">
              <section class="panel">
                <div class="panel-head">基础信息</div>
                <div class="panel-body">
                  <div class="field"><span class="field-label">Resource URI</span><input v-model="model.resourceUri" class="input" placeholder="bear://xxx/text-to-sql/schema/student" /></div>
                  <div class="field"><span class="field-label">名称</span><input v-model="model.name" class="input" placeholder="student 表结构" /></div>
                  <div class="field"><span class="field-label">描述</span><textarea v-model="model.description" class="textarea" rows="3"></textarea></div>
                  <div class="field-row">
                    <div class="field"><span class="field-label">MIME</span><input v-model="model.mimeType" class="input" placeholder="text/markdown" /></div>
                    <div class="field">
                      <span class="field-label">发布状态</span>
                      <select v-model.number="model.publishStatus" class="select">
                        <option v-for="opt in publishStatusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                      </select>
                    </div>
                  </div>
                  <div class="field">
                    <span class="field-label">内容文件</span>
                    <input class="input" type="file" accept=".md,.txt,.json,text/markdown" @change="onResourceFileChange" />
                    <span class="field-hint">{{ resourceSelectedFile ? '已选择：' + resourceSelectedFile.name : (model.fileName ? '当前文件：' + model.fileName : '保存时自动上传') }}</span>
                  </div>
                  <button class="btn btn-outline btn-sm" :disabled="!model.id" @click="loadResourcePreview">重新加载预览</button>
                </div>
              </section>

              <section class="panel">
                <div class="panel-head"><b>预览</b><span class="tiny muted">{{ model.fileSize || 0 }} B</span></div>
                <div class="panel-body">
                  <div v-if="resourcePreviewLoading" class="hint">加载中…</div>
                  <div v-else-if="resourcePreviewHtml" class="md" v-html="resourcePreviewHtml"></div>
                  <pre v-else-if="resourcePreviewText" class="code tall">{{ resourcePreviewText }}</pre>
                  <div v-else class="empty">
                    <span class="empty-icon"><Icon name="layers" :size="18" /></span>
                    <div class="empty-title">还没有内容</div>
                    <div class="empty-desc">上传内容文件或先保存一次，即可预览。</div>
                  </div>
                </div>
              </section>
            </div>
          </div>
        </template>

        <!-- ------------------------------------------------ create/edit sheet -->
        <div v-if="drawer" class="sheet-mask" @click.self="drawer = false">
          <div :class="['sheet', ['requests','dataSources'].includes(page) ? 'lg' : '']">
            <header class="sheet-head">
              <div>
                <div class="sheet-title">{{ drawerTitle }}</div>
                <div class="sheet-desc">{{ page === 'shareTokens' ? '生成后请立即保存明文 Token' : '带 * 的字段建议填写完整以便他人复用' }}</div>
              </div>
              <button class="btn btn-ghost btn-icon" @click="drawer = false"><Icon name="x" /></button>
            </header>

            <div class="sheet-body">
              <div class="form-grid">
                <!-- tokens / shareTokens -->
                <template v-if="page === 'tokens' || page === 'shareTokens'">
                  <label v-if="page === 'tokens'" class="field span-all">
                    <span class="field-label">所属用户</span>
                    <select v-model="model.userId" class="select">
                      <option v-for="opt in userOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                    </select>
                  </label>
                  <label class="field span-all"><span class="field-label">Token 名称</span><input v-model="model.tokenName" class="input" placeholder="cursor-local" /></label>
                  <div class="field span-all">
                    <span class="field-label">权限</span>
                    <div class="segmented">
                      <button v-for="opt in tokenPermissionOptions" :key="opt.label" :class="{ active: JSON.stringify(model.permissions) === JSON.stringify(opt.value) }" @click="model.permissions = opt.value">{{ opt.label }}</button>
                    </div>
                  </div>
                  <div class="field">
                    <span class="field-label">状态</span>
                    <div class="segmented">
                      <button v-for="opt in activeStatusOptions" :key="opt.value" :class="{ active: model.isActive === opt.value }" @click="model.isActive = opt.value">{{ opt.label }}</button>
                    </div>
                  </div>
                  <label class="field"><span class="field-label">过期时间</span><input v-model="model.expireTime" class="input" placeholder="不填则不过期" /></label>
                </template>

                <!-- users -->
                <template v-else-if="page === 'users'">
                  <label class="field"><span class="field-label">用户名</span><input v-model="model.username" class="input" placeholder="dev-user" /></label>
                  <label class="field"><span class="field-label">展示名</span><input v-model="model.displayName" class="input" placeholder="研发用户" /></label>
                  <label v-if="!model.id" class="field span-all">
                    <span class="field-label">初始密码</span>
                    <input v-model="model.password" class="input" placeholder="不填则使用系统默认密码（ChangeMe123），登录后请立即修改" />
                  </label>
                  <div class="field span-all">
                    <span class="field-label">启用状态</span>
                    <div class="segmented">
                      <button v-for="opt in activeStatusOptions" :key="opt.value" :class="{ active: model.isEnabled === opt.value }" @click="model.isEnabled = opt.value">{{ opt.label }}</button>
                    </div>
                  </div>
                </template>

                <!-- roles -->
                <template v-else-if="page === 'roles'">
                  <label class="field"><span class="field-label">角色编码</span><input v-model="model.roleCode" class="input" placeholder="DEVELOPER" /></label>
                  <label class="field"><span class="field-label">角色名称</span><input v-model="model.roleName" class="input" placeholder="开发者" /></label>
                  <label class="field span-all"><span class="field-label">描述</span><textarea v-model="model.description" class="textarea" rows="3"></textarea></label>
                  <div class="field span-all">
                    <span class="field-label">启用状态</span>
                    <div class="segmented">
                      <button v-for="opt in activeStatusOptions" :key="opt.value" :class="{ active: model.isEnabled === opt.value }" @click="model.isEnabled = opt.value">{{ opt.label }}</button>
                    </div>
                  </div>
                </template>

                <!-- requests -->
                <template v-else-if="page === 'requests'">
                  <label class="field"><span class="field-label">配置键</span><input v-model="model.configKey" class="input" placeholder="demo_user_detail" /></label>
                  <label class="field"><span class="field-label">名称</span><input v-model="model.name" class="input" placeholder="查询用户详情" /></label>
                  <label class="field span-all"><span class="field-label">URL</span><input v-model="model.url" class="input" placeholder="http://localhost:8080/api/demo/users/{userId}" /></label>
                  <label class="field"><span class="field-label">方法</span>
                    <select v-model="model.method" class="select">
                      <option v-for="opt in httpMethodOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                    </select>
                  </label>
                  <label class="field"><span class="field-label">类型</span>
                    <select v-model="model.type" class="select">
                      <option v-for="opt in requestTypeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                    </select>
                  </label>
                  <label class="field"><span class="field-label">服务名</span><input v-model="model.serviceName" class="input" placeholder="demo-service" /></label>
                  <label class="field"><span class="field-label">方法名</span><input v-model="model.methodName" class="input" placeholder="getUserDetail" /></label>
                  <label class="field span-all"><span class="field-label">默认参数 paramsDefault</span><textarea v-model="model.paramsDefault" class="textarea code-input" rows="4"></textarea></label>
                  <label class="field span-all"><span class="field-label">请求头 headers</span><textarea v-model="model.headers" class="textarea code-input" rows="3"></textarea></label>
                  <label class="field span-all"><span class="field-label">Body 模板</span><textarea v-model="model.bodyTemplate" class="textarea code-input" rows="4"></textarea></label>
                  <label class="field span-all"><span class="field-label">参数 schema</span><textarea v-model="model.argsSchema" class="textarea code-input" rows="3"></textarea></label>
                  <label class="field"><span class="field-label">分类</span><input v-model="model.category" class="input" placeholder="演示" /></label>
                  <label class="field"><span class="field-label">限流(次/分)</span><input v-model.number="model.rateLimitPerMinute" class="input" type="number" /></label>
                  <label class="field span-all"><span class="field-label">描述</span><textarea v-model="model.description" class="textarea" rows="2"></textarea></label>
                </template>

                <!-- data sources -->
                <template v-else-if="page === 'dataSources'">
                  <label class="field"><span class="field-label">数据源名称</span><input v-model="model.name" class="input" placeholder="演示库" /></label>
                  <label class="field"><span class="field-label">数据源键</span><input v-model="model.datasourceKey" class="input" :disabled="!!model.id" placeholder="demo_db" /></label>
                  <label class="field span-all"><span class="field-label">JDBC URL</span><input v-model="model.jdbcUrl" class="input" /></label>
                  <label class="field"><span class="field-label">类型</span>
                    <select v-model="model.dbType" class="select">
                      <option v-for="opt in dataSourceDbTypeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                    </select>
                  </label>
                  <label class="field"><span class="field-label">发布状态</span>
                    <select v-model.number="model.publishStatus" class="select">
                      <option v-for="opt in dataSourcePublishStatusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                    </select>
                  </label>
                  <label class="field"><span class="field-label">用户名</span><input v-model="model.username" class="input" /></label>
                  <label class="field"><span class="field-label">密码</span><input v-model="model.password" class="input" type="password" :placeholder="model.id ? '留空表示不修改' : '数据库密码'" /></label>
                  <label class="field span-all"><span class="field-label">额外连接参数</span><textarea v-model="model.extraJdbcProps" class="textarea code-input" rows="3" placeholder='{"useSSL":"false"}'></textarea></label>
                  <label class="field span-all"><span class="field-label">描述</span><textarea v-model="model.description" class="textarea" rows="2"></textarea></label>
                </template>

                <!-- fallback -->
                <template v-else>
                  <label v-for="(value, key) in model" v-show="!['id','tokenHash','tokenPrefix','createTime','updateTime','lastUsedTime','lastUsedIp'].includes(key)" :key="key" class="field span-all">
                    <span class="field-label">{{ key }}</span>
                    <textarea
                      v-if="['headers','bodyTemplate','paramsDefault','inputSchema','groovyScript','linkedRequestKeys','linkedDataSourceIds','description','argsSchema'].includes(key)"
                      v-model="model[key]"
                      class="textarea code-input"
                      rows="3"
                    ></textarea>
                    <input v-else v-model="model[key]" class="input" />
                  </label>
                </template>
              </div>
            </div>

            <footer class="sheet-foot">
              <button v-if="page === 'dataSources'" class="btn btn-outline btn-sm" :disabled="dataSourceTesting" style="margin-right: auto" @click="testDataSourceConnection">
                {{ dataSourceTesting ? '测试中…' : '测试连接' }}
              </button>
              <button class="btn btn-outline" @click="drawer = false">取消</button>
              <button class="btn btn-primary" @click="save"><Icon name="save" :size="13" /> 保存</button>
            </footer>
          </div>
        </div>
        <!-- ---------------------------------------------------------- sheets -->
        <div v-if="oneTimeTokenOpen" class="sheet-mask" @click.self="closeOneTimeTokenModal">
          <div class="sheet">
            <header class="sheet-head">
              <div>
                <div class="sheet-title">Token 已创建</div>
                <div class="sheet-desc">「{{ oneTimeTokenName }}」的明文只显示这一次，请立即保存</div>
              </div>
              <button class="btn btn-ghost btn-icon" @click="closeOneTimeTokenModal"><Icon name="x" /></button>
            </header>
            <div class="sheet-body">
              <pre class="code" style="white-space: pre-wrap; word-break: break-all">{{ rawToken }}</pre>
              <p class="hint">丢失后只能重新生成，服务端只保存哈希。</p>
            </div>
            <footer class="sheet-foot">
              <button class="btn btn-outline" @click="closeOneTimeTokenModal">关闭</button>
              <button class="btn btn-primary" @click="copyRawToken"><Icon name="copy" :size="13" /> 复制 Token</button>
            </footer>
          </div>
        </div>

        <div v-if="apiDebugOpen" class="sheet-mask" @click.self="apiDebugOpen = false">
          <div class="sheet lg">
            <header class="sheet-head">
              <div>
                <div class="sheet-title">调试 · {{ activeStudioApi?.name || activeStudioApi?.configKey }}</div>
                <div class="sheet-desc">{{ activeStudioApi?.method }} {{ activeStudioApi?.url }}</div>
              </div>
              <button class="btn btn-ghost btn-icon" @click="apiDebugOpen = false"><Icon name="x" /></button>
            </header>
            <div class="sheet-body">
              <label class="field">
                <span class="field-label">参数 (JSON)</span>
                <textarea v-model="debugParams" class="textarea code-input" rows="8" spellcheck="false"></textarea>
              </label>
              <div class="request-line">
                <span class="badge badge-primary">{{ activeStudioApi?.method || 'GET' }}</span>
                <code class="inline">{{ activeStudioApi?.url }}</code>
              </div>
              <div class="section">
                <div class="section-head">
                  <div class="section-title">响应</div>
                  <span :class="['response-state', debugResult && debugResult.success === false ? 'fail' : 'ok']">{{ debugSummary }}</span>
                </div>
                <pre class="code tall" :class="{ error: debugResult && debugResult.success === false }">{{ prettyJsonText(debugResult) || '尚未发送请求' }}</pre>
              </div>
            </div>
            <footer class="sheet-foot">
              <button class="btn btn-outline" @click="apiDebugOpen = false">关闭</button>
              <button class="btn btn-primary" :disabled="debugLoading" @click="runApiDebug">
                <Icon name="play" :size="13" /> {{ debugLoading ? '请求中…' : '发送请求' }}
              </button>
            </footer>
          </div>
        </div>

        <div v-if="communityDetailOpen" class="sheet-mask" @click.self="communityDetailOpen = false">
          <div class="sheet xl">
            <header class="sheet-head">
              <div>
                <div class="sheet-title">{{ communityDetailTitle }}</div>
                <div class="sheet-desc">
                  <span class="badge badge-outline">{{ activeCommunityItem?.communityKind }}</span>
                  <span style="margin-left: 8px">{{ activeCommunityItem?.description || '' }}</span>
                </div>
              </div>
              <div style="display: flex; gap: 6px">
                <button class="btn btn-ghost btn-sm" @click="toggleCommunityLike(activeCommunityItem)">{{ likeLabel(activeCommunityItem) }}</button>
                <button class="btn btn-ghost btn-icon" @click="communityDetailOpen = false"><Icon name="x" /></button>
              </div>
            </header>
            <div class="sheet-body">
              <template v-if="activeCommunityItem?.communityKind === 'skill'">
                <div class="kv-grid">
                  <div class="kv"><span>编码</span><code class="inline">{{ activeCommunityItem.skillCode || communityCode('SKILL', activeCommunityItem.id) }}</code></div>
                  <div class="kv"><span>分类</span><span>{{ activeCommunityItem.category || '-' }}</span></div>
                  <div class="kv"><span>文件</span><span>{{ activeCommunityItem.fileName || '-' }}</span></div>
                </div>
                <div class="tag-list" style="margin: 12px 0">
                  <button class="btn btn-outline btn-sm" @click="openCursorSkillInstall(activeCommunityItem)">安装到 Cursor</button>
                  <button class="btn btn-outline btn-sm" @click="copyCodexSkillInstall(activeCommunityItem)">安装到 Codex</button>
                  <button class="btn btn-ghost btn-sm" @click="downloadSkillZip(activeCommunityItem)"><Icon name="download" :size="13" /> 下载 ZIP</button>
                </div>
                <pre class="code" style="max-height: 46vh">{{ buildCodexSkillInstallCommand(activeCommunityItem) }}</pre>
              </template>

              <template v-else-if="activeCommunityItem?.communityKind === 'tool'">
                <div class="kv-grid">
                  <div class="kv"><span>工具名</span><code class="inline">{{ communityToolCode(activeCommunityItem) }}</code></div>
                  <div class="kv"><span>类型</span><span>{{ activeCommunityItem.communityType === 'builtin' ? '内置工具' : '动态工具' }}</span></div>
                  <div class="kv"><span>能力</span><span>{{ communityToolCountLabel(activeCommunityItem) }}</span></div>
                </div>
                <div class="section">
                  <div class="section-head"><div class="section-title">inputSchema</div></div>
                  <pre class="code tall">{{ prettyJsonText(parseMaybeJson(activeCommunityItem.inputSchema)) }}</pre>
                </div>
              </template>

              <template v-else-if="activeCommunityItem?.communityKind === 'prompt'">
                <div class="kv-grid">
                  <div class="kv"><span>Prompt</span><code class="inline">{{ activeCommunityItem.promptName }}</code></div>
                  <div class="kv"><span>参数</span><span>{{ parseJsonArray(activeCommunityItem.argumentsSchema).length }} 个</span></div>
                </div>
                <div class="section">
                  <div class="section-head"><div class="section-title">模板</div></div>
                  <pre class="code tall">{{ activeCommunityItem.templateContent }}</pre>
                </div>
              </template>

              <template v-else-if="activeCommunityItem?.communityKind === 'resource'">
                <div class="kv-grid">
                  <div class="kv"><span>URI</span><code class="inline">{{ activeCommunityItem.resourceUri }}</code></div>
                  <div class="kv"><span>MIME</span><span>{{ activeCommunityItem.mimeType || '-' }}</span></div>
                </div>
                <div v-if="communityResourceContentLoading" class="hint">加载内容…</div>
                <div v-else-if="communityResourceContentError" class="hint">{{ communityResourceContentError }}</div>
                <div v-else class="md" v-html="renderMarkdown(communityResourceContent)"></div>
              </template>

              <template v-else>
                <div class="kv-grid">
                  <div class="kv"><span>配置键</span><code class="inline">{{ activeCommunityItem?.configKey }}</code></div>
                  <div class="kv"><span>方法</span><span class="badge badge-primary">{{ activeCommunityItem?.method || 'GET' }}</span></div>
                  <div class="kv"><span>分类</span><span>{{ activeCommunityItem?.category || '-' }}</span></div>
                </div>
                <div class="section">
                  <div class="section-head"><div class="section-title">URL</div></div>
                  <pre class="code">{{ activeCommunityItem?.url }}</pre>
                </div>
              </template>
            </div>
            <footer class="sheet-foot">
              <button class="btn btn-outline" @click="communityDetailOpen = false">关闭</button>
            </footer>
          </div>
        </div>

        <div v-if="dynamicToolDetailOpen" class="sheet-mask" @click.self="dynamicToolDetailOpen = false">
          <div class="sheet lg">
            <header class="sheet-head">
              <div>
                <div class="sheet-title">{{ activeDynamicTool?.toolName }}</div>
                <div class="sheet-desc">{{ activeDynamicTool?.toolDescription || '动态工具详情' }}</div>
              </div>
              <button class="btn btn-ghost btn-icon" @click="dynamicToolDetailOpen = false"><Icon name="x" /></button>
            </header>
            <div class="sheet-body">
              <div class="kv-grid three">
                <div class="kv"><span>关联 API</span><span>{{ linkedRequestLabel(activeDynamicTool?.linkedRequestKeys) }}</span></div>
                <div class="kv"><span>关联数据源</span><span>{{ linkedDataSourceLabel(activeDynamicTool?.linkedDataSourceIds) }}</span></div>
                <div class="kv"><span>状态</span><span>{{ publishLabel(activeDynamicTool?.publishStatus) }}</span></div>
              </div>
              <div class="section">
                <div class="section-head"><div class="section-title">inputSchema</div></div>
                <pre class="code tall">{{ prettyJsonText(parseMaybeJson(activeDynamicTool?.inputSchema)) }}</pre>
              </div>
              <div class="section">
                <div class="section-head"><div class="section-title">Groovy 脚本</div></div>
                <pre class="code tall">{{ activeDynamicTool?.groovyScript }}</pre>
              </div>
            </div>
            <footer class="sheet-foot">
              <button class="btn btn-outline" @click="dynamicToolDetailOpen = false">关闭</button>
            </footer>
          </div>
        </div>

        <div v-if="auditDetailOpen" class="sheet-mask" @click.self="auditDetailOpen = false">
          <div class="sheet lg">
            <header class="sheet-head">
              <div>
                <div class="sheet-title">调用详情</div>
                <div class="sheet-desc">{{ activeAuditLog?.createTime }} · {{ activeAuditLog?.userName || '-' }}</div>
              </div>
              <button class="btn btn-ghost btn-icon" @click="auditDetailOpen = false"><Icon name="x" /></button>
            </header>
            <div class="sheet-body">
              <div class="kv-grid three">
                <div class="kv"><span>类型</span><span>{{ capabilityTypeLabel(activeAuditLog?.capabilityType) }}</span></div>
                <div class="kv"><span>能力</span><span>{{ activeAuditLog?.capabilityName }}</span></div>
                <div class="kv"><span>动作</span><span>{{ capabilityActionLabel(activeAuditLog?.capabilityAction) }}</span></div>
                <div class="kv"><span>状态</span><span :class="['badge', String(activeAuditLog?.status).toUpperCase() === 'SUCCESS' ? 'badge-success' : 'badge-danger']">{{ activeAuditLog?.status }}</span></div>
                <div class="kv"><span>耗时</span><span>{{ activeAuditLog?.durationMs }} ms</span></div>
              </div>
              <div class="section">
                <div class="section-head"><div class="section-title">请求参数</div></div>
                <pre class="code tall">{{ prettyJsonText(parseMaybeJson(activeAuditLog?.requestParams)) }}</pre>
              </div>
              <div class="section">
                <div class="section-head"><div class="section-title">响应摘要</div></div>
                <pre class="code tall">{{ activeAuditLog?.responseSummary || '-' }}</pre>
              </div>
              <div v-if="activeAuditLog?.errorMessage" class="section">
                <div class="section-head"><div class="section-title">错误</div></div>
                <pre class="code error">{{ activeAuditLog.errorMessage }}</pre>
              </div>
            </div>
            <footer class="sheet-foot">
              <button class="btn btn-outline" @click="auditDetailOpen = false">关闭</button>
            </footer>
          </div>
        </div>

        <div v-if="userRoleOpen" class="sheet-mask" @click.self="userRoleOpen = false">
          <div class="sheet">
            <header class="sheet-head">
              <div>
                <div class="sheet-title">分配角色 · {{ activeUser?.displayName || activeUser?.username }}</div>
                <div class="sheet-desc">一个用户可以拥有多个角色，权限取并集</div>
              </div>
              <button class="btn btn-ghost btn-icon" @click="userRoleOpen = false"><Icon name="x" /></button>
            </header>
            <div class="sheet-body">
              <div class="check-grid">
                <label v-for="role in roles" :key="role.id" class="check-item">
                  <input v-model="selectedUserRoles" type="checkbox" :value="role.id" />
                  <span><b>{{ role.roleName }}</b><br /><small class="muted">{{ role.roleCode }}</small></span>
                </label>
              </div>
            </div>
            <footer class="sheet-foot">
              <button class="btn btn-outline" @click="userRoleOpen = false">取消</button>
              <button class="btn btn-primary" @click="saveUserRoles">保存</button>
            </footer>
          </div>
        </div>

        <div v-if="toolPermissionOpen" class="sheet-mask" @click.self="toolPermissionOpen = false">
          <div class="sheet xl">
            <header class="sheet-head">
              <div>
                <div class="sheet-title">角色工具权限 · {{ activeRole?.roleName }}</div>
                <div class="sheet-desc">勾选后该角色下的用户即可调用这些工具</div>
              </div>
              <button class="btn btn-ghost btn-icon" @click="toolPermissionOpen = false"><Icon name="x" /></button>
            </header>
            <div class="sheet-body">
              <div class="section">
                <div class="section-head">
                  <div class="section-title">内置工具</div>
                  <div style="display: flex; gap: 6px">
                    <button class="btn btn-ghost btn-sm" @click="selectRoleToolGroup('builtin')">全选</button>
                    <button class="btn btn-ghost btn-sm" @click="clearRoleToolGroup('builtin')">清空</button>
                  </div>
                </div>
                <div class="check-grid three">
                  <label v-for="tool in builtinTools" :key="'rb-' + tool.name" class="check-item">
                    <input v-model="selectedRoleTools" type="checkbox" :value="tool.name" />
                    <span><b>{{ tool.name }}</b><br /><small class="muted">{{ tool.description }}</small></span>
                  </label>
                </div>
              </div>
              <div class="section">
                <div class="section-head">
                  <div class="section-title">动态工具</div>
                  <div style="display: flex; gap: 6px">
                    <button class="btn btn-ghost btn-sm" @click="selectRoleToolGroup('dynamic')">全选</button>
                    <button class="btn btn-ghost btn-sm" @click="clearRoleToolGroup('dynamic')">清空</button>
                  </div>
                </div>
                <div class="check-grid three">
                  <label v-for="tool in dynamicToolOptions" :key="'rd-' + tool.toolName" class="check-item">
                    <input v-model="selectedRoleTools" type="checkbox" :value="tool.toolName" />
                    <span><b>{{ tool.toolName }}</b><br /><small class="muted">{{ tool.toolDescription || '动态工具' }}</small></span>
                  </label>
                  <div v-if="!dynamicToolOptions.length" class="hint">还没有已上线的动态工具</div>
                </div>
              </div>
              <div class="section">
                <div class="section-head">
                  <div class="section-title">飞书工具</div>
                  <div style="display: flex; gap: 6px">
                    <button class="btn btn-ghost btn-sm" @click="selectRoleToolGroup('feishu')">全选</button>
                    <button class="btn btn-ghost btn-sm" @click="clearRoleToolGroup('feishu')">清空</button>
                  </div>
                </div>
                <div class="check-grid three">
                  <label v-for="tool in feishuToolOptions" :key="'rf-' + tool.toolName" class="check-item">
                    <input v-model="selectedRoleTools" type="checkbox" :value="tool.toolName" />
                    <span><b>{{ tool.toolName }}</b><br /><small class="muted">{{ tool.toolDescription || '飞书工具' }}</small></span>
                  </label>
                  <div v-if="!feishuToolOptions.length" class="hint">未配置飞书集成</div>
                </div>
              </div>
            </div>
            <footer class="sheet-foot">
              <button class="btn btn-ghost btn-sm" style="margin-right: auto" @click="clearRoleTools">全部清空</button>
              <button class="btn btn-outline" @click="toolPermissionOpen = false">取消</button>
              <button class="btn btn-primary" @click="saveRoleTools">保存</button>
            </footer>
          </div>
        </div>

        <div v-if="promptPermissionOpen" class="sheet-mask" @click.self="promptPermissionOpen = false">
          <div class="sheet lg">
            <header class="sheet-head">
              <div>
                <div class="sheet-title">角色 Prompt 权限 · {{ activeRole?.roleName }}</div>
                <div class="sheet-desc">仅勾选项会出现在 Prompt 列表</div>
              </div>
              <button class="btn btn-ghost btn-icon" @click="promptPermissionOpen = false"><Icon name="x" /></button>
            </header>
            <div class="sheet-body">
              <div class="check-grid">
                <label v-for="prompt in promptOptions" :key="prompt.promptName" class="check-item">
                  <input v-model="selectedRolePrompts" type="checkbox" :value="prompt.promptName" />
                  <span><b>{{ prompt.title || prompt.promptName }}</b><br /><small class="muted">{{ prompt.promptName }}</small></span>
                </label>
                <div v-if="!promptOptions.length" class="hint">还没有可用的 Prompt</div>
              </div>
            </div>
            <footer class="sheet-foot">
              <button class="btn btn-ghost btn-sm" style="margin-right: auto" @click="clearRolePrompts">全部清空</button>
              <button class="btn btn-outline" @click="promptPermissionOpen = false">取消</button>
              <button class="btn btn-primary" @click="saveRolePrompts">保存</button>
            </footer>
          </div>
        </div>

        <div v-if="resourcePermissionOpen" class="sheet-mask" @click.self="resourcePermissionOpen = false">
          <div class="sheet lg">
            <header class="sheet-head">
              <div>
                <div class="sheet-title">角色 Resource 权限 · {{ activeRole?.roleName }}</div>
                <div class="sheet-desc">按 URI 授权只读资源</div>
              </div>
              <button class="btn btn-ghost btn-icon" @click="resourcePermissionOpen = false"><Icon name="x" /></button>
            </header>
            <div class="sheet-body">
              <div class="check-grid">
                <label v-for="resource in resourceOptions" :key="resource.resourceUri" class="check-item">
                  <input v-model="selectedRoleResources" type="checkbox" :value="resource.resourceUri" />
                  <span><b>{{ resource.name }}</b><br /><small class="muted">{{ resource.resourceUri }}</small></span>
                </label>
                <div v-if="!resourceOptions.length" class="hint">还没有可用的 Resource</div>
              </div>
            </div>
            <footer class="sheet-foot">
              <button class="btn btn-ghost btn-sm" style="margin-right: auto" @click="clearRoleResources">全部清空</button>
              <button class="btn btn-outline" @click="resourcePermissionOpen = false">取消</button>
              <button class="btn btn-primary" @click="saveRoleResources">保存</button>
            </footer>
          </div>
        </div>

        <div v-if="tokenSelectionOpen" class="sheet-mask" @click.self="tokenSelectionOpen = false">
          <div class="sheet xl">
            <header class="sheet-head">
              <div>
                <div class="sheet-title">Token 工具选择 · {{ activeToken?.tokenName }}</div>
                <div class="sheet-desc">Token 级别白名单，优先于角色权限生效</div>
              </div>
              <button class="btn btn-ghost btn-icon" @click="tokenSelectionOpen = false"><Icon name="x" /></button>
            </header>
            <div class="sheet-body">
              <div class="section">
                <div class="section-head">
                  <div class="section-title">内置工具</div>
                  <div style="display: flex; gap: 6px">
                    <button class="btn btn-ghost btn-sm" @click="selectTokenToolGroup('builtin')">全选</button>
                    <button class="btn btn-ghost btn-sm" @click="clearTokenToolGroup('builtin')">清空</button>
                  </div>
                </div>
                <div class="check-grid three">
                  <label v-for="tool in builtinTools" :key="'tb-' + tool.name" class="check-item">
                    <input v-model="selectedTokenTools" type="checkbox" :value="tool.name" />
                    <span><b>{{ tool.name }}</b><br /><small class="muted">{{ tool.description }}</small></span>
                  </label>
                </div>
              </div>
              <div class="section">
                <div class="section-head">
                  <div class="section-title">动态工具</div>
                  <div style="display: flex; gap: 6px">
                    <button class="btn btn-ghost btn-sm" @click="selectTokenToolGroup('dynamic')">全选</button>
                    <button class="btn btn-ghost btn-sm" @click="clearTokenToolGroup('dynamic')">清空</button>
                  </div>
                </div>
                <div class="check-grid three">
                  <label v-for="tool in dynamicToolOptions" :key="'td-' + tool.toolName" class="check-item">
                    <input v-model="selectedTokenTools" type="checkbox" :value="tool.toolName" />
                    <span><b>{{ tool.toolName }}</b><br /><small class="muted">{{ tool.toolDescription || '动态工具' }}</small></span>
                  </label>
                </div>
              </div>
            </div>
            <footer class="sheet-foot">
              <button class="btn btn-ghost btn-sm" style="margin-right: auto" @click="clearTokenTools">全部清空</button>
              <button class="btn btn-outline" @click="tokenSelectionOpen = false">取消</button>
              <button class="btn btn-primary" @click="saveTokenSelections">保存</button>
            </footer>
          </div>
        </div>

        <div v-if="tokenPromptSelectionOpen" class="sheet-mask" @click.self="tokenPromptSelectionOpen = false">
          <div class="sheet lg">
            <header class="sheet-head">
              <div>
                <div class="sheet-title">Token Prompt 选择 · {{ activeToken?.tokenName }}</div>
                <div class="sheet-desc">勾选该 Token 可使用的 Prompt 模板</div>
              </div>
              <button class="btn btn-ghost btn-icon" @click="tokenPromptSelectionOpen = false"><Icon name="x" /></button>
            </header>
            <div class="sheet-body">
              <div class="check-grid">
                <label v-for="prompt in promptOptions" :key="prompt.promptName" class="check-item">
                  <input v-model="selectedTokenPrompts" type="checkbox" :value="prompt.promptName" />
                  <span><b>{{ prompt.title || prompt.promptName }}</b><br /><small class="muted">{{ prompt.promptName }}</small></span>
                </label>
              </div>
            </div>
            <footer class="sheet-foot">
              <button class="btn btn-ghost btn-sm" style="margin-right: auto" @click="clearTokenPrompts">全部清空</button>
              <button class="btn btn-outline" @click="tokenPromptSelectionOpen = false">取消</button>
              <button class="btn btn-primary" @click="saveTokenPromptSelections">保存</button>
            </footer>
          </div>
        </div>

        <div v-if="tokenResourceSelectionOpen" class="sheet-mask" @click.self="tokenResourceSelectionOpen = false">
          <div class="sheet lg">
            <header class="sheet-head">
              <div>
                <div class="sheet-title">Token Resource 选择 · {{ activeToken?.tokenName }}</div>
                <div class="sheet-desc">按 URI 勾选可读取的资源</div>
              </div>
              <button class="btn btn-ghost btn-icon" @click="tokenResourceSelectionOpen = false"><Icon name="x" /></button>
            </header>
            <div class="sheet-body">
              <div class="check-grid">
                <label v-for="resource in resourceOptions" :key="resource.resourceUri" class="check-item">
                  <input v-model="selectedTokenResources" type="checkbox" :value="resource.resourceUri" />
                  <span><b>{{ resource.name }}</b><br /><small class="muted">{{ resource.resourceUri }}</small></span>
                </label>
              </div>
            </div>
            <footer class="sheet-foot">
              <button class="btn btn-ghost btn-sm" style="margin-right: auto" @click="clearTokenResources">全部清空</button>
              <button class="btn btn-outline" @click="tokenResourceSelectionOpen = false">取消</button>
              <button class="btn btn-primary" @click="saveTokenResourceSelections">保存</button>
            </footer>
          </div>
        </div>
      </div>
    </div>
  </div>

  <ToastHost />
</template>
