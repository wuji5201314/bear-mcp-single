package com.bear.mcp.single.core.catalog;

import com.bear.mcp.single.core.entity.McpDynamicToolEntity;
import com.bear.mcp.single.core.entity.McpPromptTemplateEntity;
import com.bear.mcp.single.core.entity.McpRequestConfigEntity;
import com.bear.mcp.single.core.entity.McpSkillEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/** Loads the non-resource MCP definitions bundled in the application JAR. */
@Component
public class BundledMcpCatalog {
    private static final String CATALOG_ROOT = "mcp-catalog";
    private static final String SKILL_ROOT = "mcp-skills";

    private final List<McpRequestConfigEntity> requestConfigs;
    private final List<McpDynamicToolEntity> dynamicTools;
    private final List<McpPromptTemplateEntity> prompts;
    private final List<McpSkillEntity> skills;

    public BundledMcpCatalog(ObjectMapper objectMapper) {
        this.requestConfigs = load(objectMapper, "request-configs.json", McpRequestConfigEntity.class);
        this.dynamicTools = load(objectMapper, "dynamic-tools.json", McpDynamicToolEntity.class);
        this.prompts = load(objectMapper, "prompts.json", McpPromptTemplateEntity.class);
        this.skills = load(objectMapper, "skills.json", McpSkillEntity.class);
    }

    public List<McpRequestConfigEntity> requestConfigs() {
        return requestConfigs;
    }

    public List<McpDynamicToolEntity> dynamicTools() {
        return dynamicTools;
    }

    public List<McpPromptTemplateEntity> prompts() {
        return prompts;
    }

    public List<McpSkillEntity> skills() {
        return skills;
    }

    public Optional<String> readSkill(String skillCode) {
        if (skillCode == null || !skillCode.matches("^SKILL[0-9]{10}$")) {
            return Optional.empty();
        }
        ClassPathResource resource = new ClassPathResource(
                SKILL_ROOT + "/" + skillCode + "/SKILL.md");
        if (!resource.exists()) {
            return Optional.empty();
        }
        try (InputStream input = resource.getInputStream()) {
            return Optional.of(new String(input.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("读取 Git Skill 失败: " + skillCode, exception);
        }
    }

    public String skillObjectKey(String skillCode) {
        return "skills/" + skillCode + "/SKILL.md";
    }

    private <T> List<T> load(ObjectMapper objectMapper, String filename, Class<T> elementType) {
        String path = CATALOG_ROOT + "/" + filename;
        try (InputStream input = new ClassPathResource(path).getInputStream()) {
            return List.copyOf(objectMapper.readValue(
                    input,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, elementType)));
        } catch (IOException exception) {
            throw new IllegalStateException("加载 Git MCP Catalog 失败: " + path, exception);
        }
    }
}
