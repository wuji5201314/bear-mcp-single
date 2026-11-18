package com.bear.mcp.single.core.feishu;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 单体版 Bear 作为上游 MCP Server 时，代理调用飞书官方远程 MCP 服务。
 */
@Service
public class FeishuRemoteMcpService {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final String DEFAULT_ALLOWED_TOOLS = String.join(",",
            "search-user",
            "get-user",
            "fetch-file",
            "search-doc",
            "create-doc",
            "fetch-doc",
            "update-doc",
            "list-docs",
            "get-comments",
            "add-comments");
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;
    private final AtomicLong requestId = new AtomicLong(1);
    private final StringRedisTemplate redisTemplate;
    private final Set<String> discoveredToolNames = ConcurrentHashMap.newKeySet();

    @Value("${feishu.mcp.url:https://mcp.feishu.cn/mcp}")
    private String feishuMcpUrl;

    @Value("${feishu.app-id:}")
    private String appId;

    @Value("${feishu.app-secret:}")
    private String appSecret;

    @Value("${feishu.token.tenant-url:https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal}")
    private String tenantAccessTokenUrl;

    @Value("${feishu.mcp.credential-type:TAT}")
    private String credentialType;

    @Value("${feishu.token.redis-key:bear:mcp:feishu:tenant-access-token}")
    private String tenantAccessTokenRedisKey;

    public FeishuRemoteMcpService(ObjectMapper objectMapper, StringRedisTemplate redisTemplate) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public Map<String, Object> initialize(String allowedToolsCsv) {
        return postJsonRpc(allowedToolsCsv, "initialize", null);
    }

    public Map<String, Object> listTools(String allowedToolsCsv) {
        return postJsonRpc(allowedToolsCsv, "tools/list", null);
    }

    /**
     * 从飞书 MCP 实时发现工具定义。调用方提供的名称同时作为上游 allow-list，避免把未授权工具暴露给客户端。
     */
    public List<RemoteTool> discoverTools(List<String> allowedToolNames) {
        try {
            List<String> requestedNames = allowedToolNames == null ? List.of() : allowedToolNames;
            Map<String, Object> response = listTools(requestedNames.isEmpty() ? null : String.join(",", requestedNames));
            if (!Boolean.TRUE.equals(response.get("success")) || !(response.get("result") instanceof Map<?, ?> result)) {
                return List.of();
            }
            Object toolsValue = result.get("tools");
            if (!(toolsValue instanceof List<?> tools)) {
                return List.of();
            }
            List<RemoteTool> discovered = new ArrayList<>();
            for (Object value : tools) {
                if (!(value instanceof Map<?, ?> tool)) {
                    continue;
                }
                String name = String.valueOf(tool.get("name"));
                if (!StringUtils.hasText(name) || (!requestedNames.isEmpty() && !requestedNames.contains(name))) {
                    continue;
                }
                Object description = tool.get("description");
                Object inputSchema = tool.get("inputSchema");
                try {
                    discovered.add(new RemoteTool(name, description == null ? "" : String.valueOf(description),
                            inputSchema == null ? "{}" : objectMapper.writeValueAsString(inputSchema)));
                } catch (Exception ignored) {
                    discovered.add(new RemoteTool(name, description == null ? "" : String.valueOf(description), "{}"));
                }
            }
            discovered.forEach(tool -> discoveredToolNames.add(tool.name()));
            return discovered;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    /** 工具只能在一次成功的远程 tools/list 发现后被转发调用。 */
    public boolean isDiscoveredTool(String toolName) {
        return StringUtils.hasText(toolName) && discoveredToolNames.contains(toolName);
    }

    public Map<String, Object> callTool(String toolName, String argumentsJson) {
        if (!StringUtils.hasText(toolName)) {
            return failure("tool_name 不能为空");
        }
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("name", toolName.trim());
            params.put("arguments", parseArgumentsJson(argumentsJson));
            return postJsonRpc(toolName.trim(), "tools/call", params);
        } catch (IllegalArgumentException e) {
            return failure(e.getMessage());
        }
    }

    public Map<String, Object> callTool(String toolName, Map<String, Object> arguments) {
        if (!StringUtils.hasText(toolName)) {
            return failure("tool_name 不能为空");
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("name", toolName.trim());
        params.put("arguments", arguments != null ? arguments : Map.of());
        return postJsonRpc(toolName.trim(), "tools/call", params);
    }

    private Map<String, Object> postJsonRpc(String allowedToolsCsv, String method, Map<String, Object> params) {
        Credential credential = resolveCredential();
        if (!StringUtils.hasText(credential.token())) {
            return failure("缺少飞书访问凭证。请配置 FEISHU_APP_ID 和 FEISHU_APP_SECRET，由服务端自动获取 tenant_access_token");
        }

        String allowedTools = StringUtils.hasText(allowedToolsCsv) ? allowedToolsCsv.trim() : DEFAULT_ALLOWED_TOOLS;

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("jsonrpc", "2.0");
            body.put("id", requestId.getAndIncrement());
            body.put("method", method);
            if (params != null) {
                body.put("params", params);
            }

            Request request = new Request.Builder()
                    .url(feishuMcpUrl)
                    .post(RequestBody.create(objectMapper.writeValueAsString(body), JSON_MEDIA_TYPE))
                    .header("Content-Type", "application/json")
                    .header(credential.headerName(), credential.token())
                    .header("X-Lark-MCP-Allowed-Tools", allowedTools)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                ResponseBody responseBody = response.body();
                String responseText = responseBody != null ? responseBody.string() : "";
                if (!response.isSuccessful()) {
                    Map<String, Object> out = failure("请求飞书 MCP 失败 HTTP " + response.code());
                    out.put("detail", truncate(responseText, 1200));
                    return out;
                }
                return normalizeJsonRpcResponse(responseText, method);
            }
        } catch (Exception e) {
            return failure("调用飞书 MCP 失败: " + e.getMessage());
        }
    }

    private Map<String, Object> normalizeJsonRpcResponse(String body, String method) throws Exception {
        if (!StringUtils.hasText(body)) {
            return failure("飞书 MCP 返回空响应体");
        }
        JsonNode root = objectMapper.readTree(body);
        if (root.hasNonNull("error")) {
            Map<String, Object> out = failure(root.path("error").path("message").asText("飞书 MCP 返回错误"));
            out.put("mcp_error", objectMapper.convertValue(root.path("error"), new TypeReference<Map<String, Object>>() {
            }));
            return out;
        }

        JsonNode result = root.path("result");
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", !result.path("isError").asBoolean(false));
        out.put("method", method);
        out.put("result", objectMapper.convertValue(result, new TypeReference<Map<String, Object>>() {
        }));
        if (result.path("isError").asBoolean(false)) {
            out.put("message", extractContentText(result));
        }
        return out;
    }

    private Map<String, Object> parseArgumentsJson(String argumentsJson) {
        if (!StringUtils.hasText(argumentsJson)) {
            return Map.of();
        }
        try {
            JsonNode root = objectMapper.readTree(argumentsJson);
            if (!root.isObject()) {
                throw new IllegalArgumentException("arguments_json 必须是 JSON 对象字符串");
            }
            return objectMapper.convertValue(root, new TypeReference<Map<String, Object>>() {
            });
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("arguments_json 不是合法 JSON: " + e.getMessage());
        }
    }

    private Credential resolveCredential() {
        if ("TAT".equalsIgnoreCase(credentialType)) {
            return new Credential("X-Lark-MCP-TAT", getTenantAccessToken());
        }
        return new Credential("", "");
    }

    private String getTenantAccessToken() {
        synchronized (this) {
            String cached = redisTemplate.opsForValue().get(tenantAccessTokenRedisKey);
            if (StringUtils.hasText(cached)) {
                return cached;
            }
            CachedToken fetched = fetchTenantAccessToken(System.currentTimeMillis());
            long ttlSeconds = Math.max(60, (fetched.expireAtMillis() - System.currentTimeMillis()) / 1000L);
            redisTemplate.opsForValue().set(tenantAccessTokenRedisKey, fetched.token(), ttlSeconds, TimeUnit.SECONDS);
            return fetched.token();
        }
    }

    private CachedToken fetchTenantAccessToken(long now) {
        if (!StringUtils.hasText(appId) || !StringUtils.hasText(appSecret)) {
            throw new IllegalStateException("未配置 feishu.app-id 或 feishu.app-secret");
        }
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("app_id", appId.trim());
            body.put("app_secret", appSecret.trim());

            Request request = new Request.Builder()
                    .url(tenantAccessTokenUrl)
                    .post(RequestBody.create(objectMapper.writeValueAsString(body), JSON_MEDIA_TYPE))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                ResponseBody responseBody = response.body();
                String responseText = responseBody != null ? responseBody.string() : "";
                if (!response.isSuccessful()) {
                    throw new IllegalStateException("获取 tenant_access_token 失败 HTTP " + response.code()
                            + ": " + truncate(responseText, 500));
                }
                JsonNode root = objectMapper.readTree(responseText);
                int code = root.path("code").asInt(-1);
                if (code != 0) {
                    throw new IllegalStateException("获取 tenant_access_token 失败: "
                            + root.path("msg").asText("unknown") + " (code=" + code + ")");
                }
                String token = root.path("tenant_access_token").asText("");
                if (!StringUtils.hasText(token)) {
                    throw new IllegalStateException("获取 tenant_access_token 失败: 响应缺少 tenant_access_token");
                }
                int expireSeconds = root.path("expire").asInt(7200);
                long expireAtMillis = now + Math.max(60, expireSeconds - 120L) * 1000L;
                return new CachedToken(token, expireAtMillis);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static String extractContentText(JsonNode result) {
        JsonNode content = result.path("content");
        if (!content.isArray() || content.isEmpty()) {
            return "飞书 MCP 工具执行失败";
        }
        List<String> texts = new ArrayList<>();
        for (JsonNode item : content) {
            String text = item.path("text").asText(null);
            if (StringUtils.hasText(text)) {
                texts.add(text);
            }
        }
        return texts.isEmpty() ? "飞书 MCP 工具执行失败" : String.join("\n", texts);
    }

    private static Map<String, Object> failure(String message) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", false);
        out.put("message", message);
        return out;
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max) + "...";
    }

    private record Credential(String headerName, String token) {
    }

    private record CachedToken(String token, long expireAtMillis) {
    }

    public record RemoteTool(String name, String description, String inputSchema) {
    }
}
