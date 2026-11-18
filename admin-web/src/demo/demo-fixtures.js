// Demo fixtures used only when VITE_DEMO=1 (GitHub Pages / tunnel preview).
// Everything here is synthetic: no real endpoints, tokens or business data.
const now = '2026-01-01 10:00:00'

export const users = [
  { id: 1, username: 'demo-admin', displayName: '演示管理员', isEnabled: 1, createTime: now },
  { id: 2, username: 'demo-dev', displayName: '演示开发者', isEnabled: 1, createTime: now }
]

export const roles = [
  { id: 1, roleCode: 'ADMIN', roleName: '管理员', description: '全量管理权限', isEnabled: 1 },
  { id: 2, roleCode: 'DEVELOPER', roleName: '开发者', description: '创作与调试', isEnabled: 1 }
]

export const tokens = [
  { id: 11, userId: 1, tokenName: 'Cursor 本地', tokenPrefix: 'mcp_dev', permissions: '["mcp:tools:read","mcp:tools:call"]', isActive: 1, createTime: now, lastUsedTime: now, expireTime: null },
  { id: 12, userId: 2, tokenName: 'Codex 测试', tokenPrefix: 'mcp_tes', permissions: '["mcp:tools:read"]', isActive: 1, createTime: now, lastUsedTime: null, expireTime: null }
]

export const requestConfigs = [
  { id: 21, requestId: 'API0000000001', configKey: 'demo_clock', name: '当前时间', description: '返回服务器当前时间', type: 'MOCK', method: 'GET', category: '演示', url: 'mock://clock', paramsDefault: '{"message":"默认消息"}', headers: '{}', bodyTemplate: '', isEnabled: 1, publishStatus: 2, rateLimitPerMinute: 60, connectTimeoutMs: 3000, readTimeoutMs: 5000, createTime: now },
  { id: 22, requestId: 'API0000000002', configKey: 'demo_user_detail', name: '用户详情', description: '按 id 查询演示用户', type: 'HTTP', method: 'GET', category: '演示', url: 'http://localhost:8080/api/demo/users/{{userId}}', paramsDefault: '{"userId":1}', headers: '{"Content-Type":"application/json"}', bodyTemplate: '', isEnabled: 1, publishStatus: 1, rateLimitPerMinute: 60, connectTimeoutMs: 3000, readTimeoutMs: 5000, createTime: now },
  { id: 23, requestId: 'API0000000003', configKey: 'demo_order_search', name: '订单搜索', description: '分页搜索演示订单', type: 'HTTP', method: 'POST', category: '演示', url: 'http://localhost:8080/api/demo/orders/search', paramsDefault: '{"page":1,"size":20}', headers: '{"Content-Type":"application/json"}', bodyTemplate: '{"keyword":"{{keyword}}","page":{{page}}}', isEnabled: 1, publishStatus: 0, rateLimitPerMinute: 60, connectTimeoutMs: 3000, readTimeoutMs: 5000, createTime: now }
]

export const dataSources = [
  { id: 31, name: '演示库', datasourceKey: 'demo_db', description: '只读演示数据源', dbType: 'MYSQL', username: 'demo', jdbcUrl: 'jdbc:mysql://localhost:3306/bear_mcp_single', extraJdbcProps: '', publishStatus: 1, createTime: now }
]

export const resources = [
  { id: 41, resourceUri: 'bear://xxx/text-to-sql/business/student', name: '学员业务说明', description: '示例业务口径', mimeType: 'text/markdown', fileName: '001-xxx__text-to-sql__business__student.md', fileSize: 1979, enabled: 1, publishStatus: 2, createTime: now },
  { id: 42, resourceUri: 'bear://xxx/text-to-sql/schema/student', name: 'student 表结构', description: '示例 DDL', mimeType: 'text/markdown', fileName: '002-xxx__text-to-sql__schema__student.md', fileSize: 1731, enabled: 1, publishStatus: 1, createTime: now },
  { id: 43, resourceUri: 'bear://xxx/text-to-sql/table-catalog', name: '表目录', description: '示例表清单', mimeType: 'text/markdown', fileName: '003-xxx__text-to-sql__table-catalog.md', fileSize: 1800, enabled: 1, publishStatus: 0, createTime: now }
]

export const dynamicTools = [
  { id: 51, toolName: 'echo_dynamic', toolDescription: '回显输入参数', inputSchema: '{"type":"object","properties":{"message":{"type":"string"}},"required":["message"]}', groovyScript: 'if (!params.message) { return [ok: false, message: "message 不能为空"] }\nreturn [ok: true, echo: params.message]', linkedRequestKeys: '[]', linkedDataSourceIds: '[]', linkedFeishuToolNames: '[]', linkedRemoteCredentials: '[]', linkedMcpKeys: '[]', enabled: 1, publishStatus: 2, createTime: now },
  { id: 52, toolName: 'demo_user_lookup', toolDescription: '按 id 查询演示用户', inputSchema: '{"type":"object","properties":{"userId":{"type":"string"}},"required":["userId"]}', groovyScript: 'if (!userId) { return [ok: false, message: "userId 不能为空"] }\nreturn runRequest.runRequest("demo_user_detail", [userId: userId])', linkedRequestKeys: '["demo_user_detail"]', linkedDataSourceIds: '[]', linkedFeishuToolNames: '[]', linkedRemoteCredentials: '[]', linkedMcpKeys: '[]', enabled: 1, publishStatus: 1, createTime: now },
  { id: 53, toolName: 'demo_order_search', toolDescription: '分页搜索演示订单', inputSchema: '{"type":"object","properties":{"keyword":{"type":"string"},"page":{"type":"integer"}}}', groovyScript: 'return runRequest.runRequest("demo_order_search", [keyword: keyword, page: page ?: 1])', linkedRequestKeys: '["demo_order_search"]', linkedDataSourceIds: '[]', linkedFeishuToolNames: '[]', linkedRemoteCredentials: '[]', linkedMcpKeys: '[]', enabled: 1, publishStatus: 0, createTime: now }
]

export const prompts = [
  { id: 61, promptName: 'demo_text_to_sql_system', title: 'Text-to-SQL 系统提示', description: '把自然语言问题转成 SQL 的系统提示词', argumentsSchema: '[{"name":"question","description":"用户问题","required":true}]', templateContent: '你是 SQL 助手。先读取 resource 里的表结构，再输出一条只读 SQL。\n用户问题：{{question}}', linkedToolNames: '[]', enabled: 1, publishStatus: 2, createTime: now },
  { id: 62, promptName: 'demo_log_analysis_system', title: '日志排查提示', description: '按步骤排查线上问题', argumentsSchema: '[]', templateContent: '请按「确认现象 → 定位链路 → 给出结论」的顺序分析日志。', linkedToolNames: '[]', enabled: 1, publishStatus: 1, createTime: now }
]

export const skills = [
  { id: 71, skillCode: 'SKILL0000000005', name: 'demo-log-analysis', description: '线上日志排查示例 Skill', category: '运维', fileName: 'SKILL.md', fileSize: 1993, enabled: 1, publishStatus: 2, createTime: now }
]

export const audits = [
  { id: 81, createTime: now, capabilityType: 'TOOL', capabilityName: 'echo_dynamic', capabilityAction: 'CALL', userName: 'demo-admin', status: 'SUCCESS', durationMs: 12, requestParams: '{"message":"hi"}', responseSummary: '{"ok":true,"echo":"hi"}', errorMessage: null },
  { id: 82, createTime: now, capabilityType: 'PROMPT', capabilityName: 'demo_text_to_sql_system', capabilityAction: 'RENDER', userName: 'demo-dev', status: 'FAILED', durationMs: 40, requestParams: null, responseSummary: null, errorMessage: '缺少必填参数 question' },
  { id: 83, createTime: now, capabilityType: 'RESOURCE', capabilityName: 'bear://xxx/text-to-sql/schema/student', capabilityAction: 'READ', userName: 'demo-admin', status: 'SUCCESS', durationMs: 6, requestParams: null, responseSummary: '1731 bytes', errorMessage: null }
]

export const builtinTools = [
  { id: 'hello', name: 'hello', toolName: 'hello', displayName: 'hello', description: '最小连通性测试工具', likeCount: 3 },
  { id: 'current_time', name: 'current_time', toolName: 'current_time', displayName: 'current_time', description: '返回当前时间', likeCount: 3 },
  { id: 'system_info', name: 'system_info', toolName: 'system_info', displayName: 'system_info', description: '返回运行环境信息', likeCount: 3 },
  { id: 'list_dynamic_tools', name: 'list_dynamic_tools', toolName: 'list_dynamic_tools', displayName: 'list_dynamic_tools', description: '列出动态工具', likeCount: 3 }
]

export const dashboard = {
  users: users.length,
  roles: roles.length,
  activeTokens: tokens.filter((t) => Number(t.isActive) === 1).length,
  enabledRequests: requestConfigs.filter((r) => Number(r.isEnabled) === 1).length,
  enabledDynamicTools: dynamicTools.filter((t) => Number(t.enabled) === 1).length,
  todayCalls: 138,
  recentAudits: audits
}

const withLikes = (list, kind) => list.map((item, index) => ({ ...item, communityKind: kind, likeCount: 12 - index }))

export const community = {
  skills: withLikes(skills, 'skill'),
  tools: withLikes(dynamicTools, 'tool'),
  prompts: withLikes(prompts, 'prompt'),
  resources: withLikes(resources, 'resource'),
  apis: withLikes(requestConfigs, 'api')
}

export const skillMarkdown = `# demo-log-analysis

## 适用场景
线上接口出现 5xx、超时或结果异常时使用。

## 排查步骤
1. 确认现象：错误码、失败率、影响面。
2. 定位链路：网关 → 应用 → 数据库 / 下游。
3. 缩小范围：按时间、实例、参数分组对比。
4. 给出结论：根因、证据、下一步动作。

## 输出格式
- 现象
- 证据
- 结论
- 建议

## 约束
只读分析，不执行变更；涉及数据修改时给出方案由人确认。
`

export const resourceMarkdown = `# student 表结构（示例）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| name | varchar(64) | 学员姓名 |
| status | tinyint | 1 在读 / 0 结课 |
| created_at | datetime | 创建时间 |

> 仅用于演示 MCP Resource 的读取链路。
`
