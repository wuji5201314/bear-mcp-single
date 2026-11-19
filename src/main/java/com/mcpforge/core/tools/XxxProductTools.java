package com.mcpforge.core.tools;

import com.mcpforge.core.context.McpUserContext;
import com.mcpforge.core.context.McpUserContextHolder;
import com.mcpforge.core.entity.McpResourceEntity;
import com.mcpforge.core.resource.ResourceAccessService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/** 提供 xxx 产品介绍 Resource 的便捷工具入口。 */
@Component
public class XxxProductTools {

    private static final String RESOURCE_URI = "mcpforge://xxx/product";

    private final ResourceAccessService resourceAccessService;

    public XxxProductTools(ResourceAccessService resourceAccessService) {
        this.resourceAccessService = resourceAccessService;
    }

    @Tool(name = "xxx_product_intro",
            description = "读取 xxx 产品介绍。返回已上线且当前 Token 有权限访问的产品介绍 Markdown 内容。")
    public Map<String, Object> xxxProductIntro() {
        McpUserContext context = McpUserContextHolder.get();
        McpResourceEntity resource = resourceAccessService.findAccessibleByUri(context, RESOURCE_URI);
        if (resource == null) {
            Map<String, Object> failure = new LinkedHashMap<>();
            failure.put("success", false);
            failure.put("message", "xxx 产品介绍 Resource 未发布、未授权或不存在: " + RESOURCE_URI);
            return failure;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("uri", RESOURCE_URI);
        result.put("name", resource.getName());
        result.put("description", resource.getDescription());
        result.put("mimeType", resource.getMimeType() == null ? "text/markdown" : resource.getMimeType());
        result.put("text", resourceAccessService.readText(resource));
        return result;
    }
}
