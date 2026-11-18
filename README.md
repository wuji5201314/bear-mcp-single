# Bear MCP Single

一个自托管（single-node）的 MCP Server：把 HTTP 接口、Groovy 动态脚本、数据库、Prompt 模板、Skill 与静态资源统一注册成 MCP Tool / Prompt / Resource，并带一个 Vue3 管理后台做可视化编排。

前端生成的 Skill、MCP Tool、请求配置、Resource 都只是「数据」——改完立刻生效，不需要重新打包。

> 说明：本仓库为脱敏后的开源版本，示例数据中的组织名、域名、账号、密钥均已替换为 `xxx` 占位符，仅保留结构与玩法。

## 功能

- **MCP 网关**：Streamable HTTP 协议，支持 tools / prompts / resources 三类能力，按 Token 授权
- **动态工具**：在线写 Groovy 脚本 + 调用白名单请求配置，脚本沙箱执行、可限制执行时间与输出
- **请求配置**：把任意 HTTP 接口包装成可复用请求模板，支持 `{{参数}}` 占位符与默认参数
- **数据源**：配置 JDBC 数据源后直接暴露只读查询工具
- **Prompt / Skill / Resource**：模板化 Prompt、Markdown 技能包、Git 同步的资源文档
- **管理后台**：用户、角色、Token、工具/提示词权限、审计日志
- **多租户式隔离**：每个 Token 独立选择可见工具与资源

## 技术栈

| 层 | 选型 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.3、Spring AI 1.1（MCP Server）、MyBatis、Groovy 4 |
| 存储 | MySQL 8、Redis（Token / 会话缓存）、TOS 或任意 S3 兼容对象存储（可选） |
| 前端 | Vue 3、Vite 6、自建 shadcn 风格设计系统（无 UI 框架依赖，内置深浅色主题） |
| 部署 | Docker 多阶段构建 + docker-compose + Jenkins Pipeline 示例 |

## 目录结构

```
src/main/java/com/bear/mcp/single
├── admin/          管理端接口（用户、角色、Token、审计）
├── gateway/        MCP 协议过滤器（鉴权、tools/prompts/resources 分流）
├── core/
│   ├── tools/      内置 MCP 工具（计算、系统信息、脚本、请求、资源等）
│   ├── groovy/     动态脚本引擎与安全定制
│   ├── request/    请求配置执行器
│   ├── resource/   Resource 目录、Git 同步与访问控制
│   ├── skill/      Skill 装载与发布
│   └── catalog/    随包发布的示例 catalog
└── publicapi/      对外开放的 MCP 入口
src/main/resources
├── mcp-catalog/    示例 请求配置 / 动态工具 / Prompt / Skill 元数据
├── mcp-resources/  示例 Resource（Markdown）与 manifest
└── mcp-skills/     Skill 正文
admin-web/          管理后台（Vue3 + Vite）
db/init.sql         建表 + 演示数据
deploy/             后端容器化与 CI 示例
```

## 快速开始

### 1. 初始化数据库

```bash
mysql -u root -p -e "CREATE DATABASE bear_mcp_single DEFAULT CHARSET utf8mb4;"
mysql -u root -p bear_mcp_single < db/init.sql
```

`db/init.sql` 会建表并写入一套演示数据：管理员 `demo-admin`、演示 Token、示例请求配置与动态工具。
演示管理员密码为 `admin123`，**上线前务必在后台改掉**。

### 2. 启动后端

```bash
# 依赖：JDK 17、Maven 3.8+
export BEAR_MYSQL_URL='jdbc:mysql://localhost:3306/bear_mcp_single?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai'
export BEAR_MYSQL_USERNAME=root
export BEAR_MYSQL_PASSWORD=your_password

mvn spring-boot:run
```

默认监听 `8090`，管理端接口在 `/api/admin/**`，MCP 入口在 `/mcp`。

### 3. 启动管理后台

```bash
cd admin-web
npm install
npm run dev     # http://localhost:5173，/api 已代理到 8090
```

用 `demo-admin / admin123` 登录。

### 4. 调一次 MCP

Token 在管理后台「Token 管理」里创建（演示库内置 `mcp_dev_token`）。

```bash
# 列出工具
curl -s http://localhost:8090/mcp \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer mcp_dev_token' \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/list","params":{}}'

# 调用工具
curl -s http://localhost:8090/mcp \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer mcp_dev_token' \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"current_time","arguments":{}}}'
```

## 关键配置

配置统一走环境变量，默认值见 `src/main/resources/application.yml`，生产用 `application-prod.yaml` + `deploy/.env`。

| 变量 | 说明 | 默认 |
| --- | --- | --- |
| `BEAR_MYSQL_URL` / `BEAR_MYSQL_USERNAME` / `BEAR_MYSQL_PASSWORD` | 业务库连接 | 本地 `localhost:3306/bear_mcp_single` |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | Redis 连接 | `localhost:6379` |
| `BEAR_ADMIN_JWT_SECRET` | 管理端 JWT 签名密钥，务必替换 | `change-me-in-production` |
| `BEAR_MCP_DEMO_TOKEN` | 演示 Token（仅用于本地联调） | `mcp_dev_token` |
| `BEAR_REPOSITORY_ROOT` | 代码仓库工具可读的根目录（只读挂载） | `/data/repositories` |
| `BEAR_TOS_*` | 对象存储（可选，未配置则资源存库） | 空 |
| `FEISHU_APP_ID` / `FEISHU_APP_SECRET` | 飞书 MCP 接入（可选） | 空 |
| `VOLCENGINE_*` | 日志服务接入（可选） | 空 |

敏感能力（对象存储、日志服务、外部 MCP）都是可选依赖：不配置时对应工具直接返回未配置状态，不影响其他功能。

## 自定义扩展

1. 管理后台「请求配置」里登记一个 HTTP 接口（URL、Header、Body 都支持 `{{参数}}`）。
2. 「动态工具」里写 Groovy 脚本，通过 `runRequest.runRequest("配置键", [参数: 值])` 调它。
3. 需要给模型看的说明文档，放到「Resource」里；需要复用的方法论文本，放到「Skill」里。
4. 发布后模型端 `tools/list` 就能看到新工具，无需重启。

内置示例（`src/main/resources/mcp-catalog`）：`echo_dynamic`、`demo_user_lookup`、`demo_order_search`，可作为模板照着改。

## 部署

```bash
cp deploy/.env.example deploy/.env   # 填入真实配置
docker compose -f deploy/docker-compose.yml up -d
```

`deploy/jenkins-pipeline.groovy` 与 `admin-web/deploy/jenkins-pipeline.groovy` 是 CI 参考流程（拉代码 → 构建镜像 → 滚动重启），其中的仓库地址、部署目录、服务器地址均为占位符，使用前替换成你自己的。

## 安全提示

- 仓库内**不含**任何真实密钥：所有密钥项都走环境变量，示例值为空。
- 管理端默认口令、演示 Token 仅用于本地体验，生产环境请重置并关闭未使用的工具。
- Groovy 动态脚本按白名单方式调用请求配置，脚本执行有超时与输出上限；仍建议只在受信环境下开放脚本编辑权限。
- Resource / Repository 工具默认只读，若挂载代码仓库请使用只读卷。

## License

MIT
