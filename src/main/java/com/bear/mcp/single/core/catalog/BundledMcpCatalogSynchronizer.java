package com.bear.mcp.single.core.catalog;

import com.bear.mcp.single.core.entity.McpDynamicToolEntity;
import com.bear.mcp.single.core.entity.McpPromptTemplateEntity;
import com.bear.mcp.single.core.entity.McpRequestConfigEntity;
import com.bear.mcp.single.core.entity.McpRoleEntity;
import com.bear.mcp.single.core.entity.McpRolePromptEntity;
import com.bear.mcp.single.core.entity.McpRoleToolEntity;
import com.bear.mcp.single.core.entity.McpUserPromptSelectionEntity;
import com.bear.mcp.single.core.entity.McpUserTokenEntity;
import com.bear.mcp.single.core.entity.McpUserToolSelectionEntity;
import com.bear.mcp.single.core.entity.McpSkillEntity;
import com.bear.mcp.single.core.mapper.McpDynamicToolMapper;
import com.bear.mcp.single.core.mapper.McpPromptTemplateMapper;
import com.bear.mcp.single.core.mapper.McpRequestConfigMapper;
import com.bear.mcp.single.core.mapper.McpRoleMapper;
import com.bear.mcp.single.core.mapper.McpRolePromptMapper;
import com.bear.mcp.single.core.mapper.McpRoleToolMapper;
import com.bear.mcp.single.core.mapper.McpSkillMapper;
import com.bear.mcp.single.core.mapper.McpUserPromptSelectionMapper;
import com.bear.mcp.single.core.mapper.McpUserTokenMapper;
import com.bear.mcp.single.core.mapper.McpUserToolSelectionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Synchronizes bundled Tools, APIs, Prompts and Skills into the local database. */
@Component
@Slf4j
public class BundledMcpCatalogSynchronizer {
    private static final Set<String> PROMPT_TOOLS = Set.of("list_mcp_prompts", "render_prompt");
    private static final Set<String> TOOL_ADMIN_CAPABILITIES = Set.of(
            "list_dynamic_tools", "create_dynamic_tool", "get_skill");

    private final BundledMcpCatalog catalog;
    private final McpRequestConfigMapper requestConfigMapper;
    private final McpDynamicToolMapper dynamicToolMapper;
    private final McpPromptTemplateMapper promptMapper;
    private final McpSkillMapper skillMapper;
    private final McpRoleMapper roleMapper;
    private final McpRoleToolMapper roleToolMapper;
    private final McpRolePromptMapper rolePromptMapper;
    private final McpUserTokenMapper tokenMapper;
    private final McpUserToolSelectionMapper toolSelectionMapper;
    private final McpUserPromptSelectionMapper promptSelectionMapper;

    public BundledMcpCatalogSynchronizer(BundledMcpCatalog catalog,
                                         McpRequestConfigMapper requestConfigMapper,
                                         McpDynamicToolMapper dynamicToolMapper,
                                         McpPromptTemplateMapper promptMapper,
                                         McpSkillMapper skillMapper,
                                         McpRoleMapper roleMapper,
                                         McpRoleToolMapper roleToolMapper,
                                         McpRolePromptMapper rolePromptMapper,
                                         McpUserTokenMapper tokenMapper,
                                         McpUserToolSelectionMapper toolSelectionMapper,
                                         McpUserPromptSelectionMapper promptSelectionMapper) {
        this.catalog = catalog;
        this.requestConfigMapper = requestConfigMapper;
        this.dynamicToolMapper = dynamicToolMapper;
        this.promptMapper = promptMapper;
        this.skillMapper = skillMapper;
        this.roleMapper = roleMapper;
        this.roleToolMapper = roleToolMapper;
        this.rolePromptMapper = rolePromptMapper;
        this.tokenMapper = tokenMapper;
        this.toolSelectionMapper = toolSelectionMapper;
        this.promptSelectionMapper = promptSelectionMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void synchronize() {
        try {
            int apiCount = synchronizeRequestConfigs();
            int toolCount = synchronizeDynamicTools();
            int promptCount = synchronizePrompts();
            int skillCount = synchronizeSkills();
            Set<String> targetRoles = targetRoleCodes();
            synchronizeRoleTools(targetRoles);
            synchronizeRolePrompts(targetRoles);
            synchronizeTokenTools();
            synchronizeTokenPrompts();
            log.info("Git MCP Catalog 同步完成，apis={}, tools={}, prompts={}, skills={}, roles={}",
                    apiCount, toolCount, promptCount, skillCount, targetRoles.size());
        } catch (RuntimeException exception) {
            // 同步是兼容层；数据库迁移或远端依赖异常不应阻断已有 MCP 服务启动。
            log.error("Git MCP Catalog 元数据同步失败，运行时仍使用已有数据库配置", exception);
        }
    }

    private int synchronizeRequestConfigs() {
        int changed = 0;
        for (McpRequestConfigEntity source : catalog.requestConfigs()) {
            McpRequestConfigEntity existing = requestConfigMapper.findByConfigKey(source.getConfigKey());
            if (existing == null && source.getRequestId() != null) {
                existing = requestConfigMapper.findByRequestId(source.getRequestId());
            }
            McpRequestConfigEntity target = copyRequestConfig(source);
            if (existing == null) {
                requestConfigMapper.insert(target);
            } else {
                target.setId(existing.getId());
                target.setCreatorId(existing.getCreatorId());
                requestConfigMapper.update(target);
            }
            changed++;
        }
        return changed;
    }

    private int synchronizeDynamicTools() {
        int changed = 0;
        for (McpDynamicToolEntity source : catalog.dynamicTools()) {
            McpDynamicToolEntity existing = dynamicToolMapper.findByName(source.getToolName());
            McpDynamicToolEntity target = copyDynamicTool(source);
            if (existing == null) {
                dynamicToolMapper.insert(target);
            } else {
                target.setId(existing.getId());
                dynamicToolMapper.update(target);
            }
            changed++;
        }
        return changed;
    }

    private int synchronizePrompts() {
        int changed = 0;
        for (McpPromptTemplateEntity source : catalog.prompts()) {
            McpPromptTemplateEntity existing = promptMapper.findByName(source.getPromptName());
            McpPromptTemplateEntity target = copyPrompt(source);
            if (existing == null) {
                promptMapper.insert(target);
            } else {
                target.setId(existing.getId());
                target.setCreatorId(existing.getCreatorId());
                promptMapper.update(target);
            }
            changed++;
        }
        return changed;
    }

    private int synchronizeSkills() {
        int changed = 0;
        for (McpSkillEntity source : catalog.skills()) {
            McpSkillEntity existing = skillMapper.findByCode(source.getSkillCode());
            McpSkillEntity target = copySkill(source);
            boolean contentAvailable = catalog.readSkill(source.getSkillCode()).isPresent();
            target.setObjectKey(catalog.skillObjectKey(source.getSkillCode()));
            if (!contentAvailable) {
                target.setEnabled(0);
                target.setPublishStatus(0);
            }
            if (existing == null) {
                skillMapper.insert(target);
            } else {
                target.setId(existing.getId());
                target.setCreatorId(existing.getCreatorId());
                skillMapper.update(target);
            }
            changed++;
        }
        return changed;
    }

    private Set<String> targetRoleCodes() {
        List<McpRoleEntity> activeRoles = roleMapper.findAll().stream()
                .filter(role -> Integer.valueOf(1).equals(role.getIsEnabled()))
                .toList();
        Set<String> targetRoles = activeRoles.stream()
                .map(McpRoleEntity::getRoleCode)
                .filter(code -> "ADMIN".equalsIgnoreCase(code) || "SUPERADMIN".equalsIgnoreCase(code))
                .collect(Collectors.toSet());
        if (!targetRoles.isEmpty()) {
            return targetRoles;
        }
        Set<String> roleCodesWithCatalogCapabilities = roleToolMapper.findAll().stream()
                .filter(item -> TOOL_ADMIN_CAPABILITIES.contains(item.getToolName()))
                .map(McpRoleToolEntity::getRoleCode)
                .collect(Collectors.toSet());
        return activeRoles.stream()
                .map(McpRoleEntity::getRoleCode)
                .filter(roleCodesWithCatalogCapabilities::contains)
                .collect(Collectors.toSet());
    }

    private void synchronizeRoleTools(Set<String> roleCodes) {
        for (String roleCode : roleCodes) {
            for (McpDynamicToolEntity source : catalog.dynamicTools()) {
                McpRoleToolEntity relation = new McpRoleToolEntity();
                relation.setRoleCode(roleCode);
                relation.setToolName(source.getToolName());
                roleToolMapper.insertIgnore(relation);
            }
        }
    }

    private void synchronizeRolePrompts(Set<String> roleCodes) {
        for (String roleCode : roleCodes) {
            for (McpPromptTemplateEntity source : catalog.prompts()) {
                McpRolePromptEntity relation = new McpRolePromptEntity();
                relation.setRoleCode(roleCode);
                relation.setPromptName(source.getPromptName());
                rolePromptMapper.insertIgnore(relation);
            }
        }
    }

    private void synchronizeTokenTools() {
        Map<Long, List<McpUserToolSelectionEntity>> existing = toolSelectionMapper.findAll().stream()
                .collect(Collectors.groupingBy(McpUserToolSelectionEntity::getTokenId));
        Set<Long> tokenIdsWithTools = existing.entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(item -> Integer.valueOf(1).equals(item.getEnabled())))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        for (McpUserTokenEntity token : tokenMapper.findAll()) {
            if (!Integer.valueOf(1).equals(token.getIsActive()) || !tokenIdsWithTools.contains(token.getId())) {
                continue;
            }
            Set<String> existingNames = existing.getOrDefault(token.getId(), List.of()).stream()
                    .map(McpUserToolSelectionEntity::getToolName)
                    .collect(Collectors.toSet());
            for (McpDynamicToolEntity source : catalog.dynamicTools()) {
                if (existingNames.contains(source.getToolName())) {
                    continue;
                }
                McpUserToolSelectionEntity selection = new McpUserToolSelectionEntity();
                selection.setTokenId(token.getId());
                selection.setToolName(source.getToolName());
                selection.setToolType("DYNAMIC");
                selection.setEnabled(1);
                toolSelectionMapper.insertIfAbsent(selection);
            }
        }
    }

    private void synchronizeTokenPrompts() {
        Map<Long, List<McpUserPromptSelectionEntity>> existing = promptSelectionMapper.findAll().stream()
                .collect(Collectors.groupingBy(McpUserPromptSelectionEntity::getTokenId));
        Set<Long> tokenIdsWithPrompts = existing.entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(item -> Integer.valueOf(1).equals(item.getEnabled())))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        Set<Long> tokenIdsWithPromptTools = toolSelectionMapper.findAll().stream()
                .filter(item -> Integer.valueOf(1).equals(item.getEnabled()))
                .filter(item -> PROMPT_TOOLS.contains(item.getToolName()))
                .map(McpUserToolSelectionEntity::getTokenId)
                .collect(Collectors.toSet());
        tokenIdsWithPrompts.addAll(tokenIdsWithPromptTools);
        for (McpUserTokenEntity token : tokenMapper.findAll()) {
            if (!Integer.valueOf(1).equals(token.getIsActive()) || !tokenIdsWithPrompts.contains(token.getId())) {
                continue;
            }
            Set<String> existingNames = existing.getOrDefault(token.getId(), List.of()).stream()
                    .map(McpUserPromptSelectionEntity::getPromptName)
                    .collect(Collectors.toSet());
            for (McpPromptTemplateEntity source : catalog.prompts()) {
                if (existingNames.contains(source.getPromptName())) {
                    continue;
                }
                McpUserPromptSelectionEntity selection = new McpUserPromptSelectionEntity();
                selection.setTokenId(token.getId());
                selection.setPromptName(source.getPromptName());
                selection.setEnabled(1);
                promptSelectionMapper.insertIfAbsent(selection);
            }
        }
    }

    private McpRequestConfigEntity copyRequestConfig(McpRequestConfigEntity source) {
        McpRequestConfigEntity target = new McpRequestConfigEntity();
        target.setRequestId(source.getRequestId());
        target.setConfigKey(source.getConfigKey());
        target.setName(source.getName());
        target.setType(source.getType());
        target.setMethod(source.getMethod());
        target.setUrl(source.getUrl());
        target.setHeaders(source.getHeaders());
        target.setBodyTemplate(source.getBodyTemplate());
        target.setParamsDefault(source.getParamsDefault());
        target.setConnectTimeoutMs(source.getConnectTimeoutMs());
        target.setReadTimeoutMs(source.getReadTimeoutMs());
        target.setServiceName(source.getServiceName());
        target.setMethodName(source.getMethodName());
        target.setArgsSchema(source.getArgsSchema());
        target.setCreatorId(null);
        target.setIsEnabled(source.getIsEnabled());
        target.setRateLimitPerMinute(source.getRateLimitPerMinute());
        target.setPublishStatus(source.getPublishStatus());
        target.setDescription(source.getDescription());
        target.setCategory(source.getCategory());
        return target;
    }

    private McpDynamicToolEntity copyDynamicTool(McpDynamicToolEntity source) {
        McpDynamicToolEntity target = new McpDynamicToolEntity();
        target.setToolName(source.getToolName());
        target.setToolDescription(source.getToolDescription());
        target.setInputSchema(source.getInputSchema());
        target.setGroovyScript(source.getGroovyScript());
        target.setLinkedRequestKeys(source.getLinkedRequestKeys());
        target.setLinkedDataSourceIds(source.getLinkedDataSourceIds());
        target.setLinkedFeishuToolNames(source.getLinkedFeishuToolNames());
        target.setLinkedRemoteCredentials(source.getLinkedRemoteCredentials());
        target.setLinkedMcpKeys(source.getLinkedMcpKeys());
        target.setEnabled(source.getEnabled());
        target.setPublishStatus(source.getPublishStatus());
        return target;
    }

    private McpPromptTemplateEntity copyPrompt(McpPromptTemplateEntity source) {
        McpPromptTemplateEntity target = new McpPromptTemplateEntity();
        target.setPromptName(source.getPromptName());
        target.setTitle(source.getTitle());
        target.setDescription(source.getDescription());
        target.setArgumentsSchema(source.getArgumentsSchema());
        target.setTemplateContent(source.getTemplateContent());
        target.setLinkedToolNames(source.getLinkedToolNames());
        target.setCreatorId(null);
        target.setEnabled(source.getEnabled());
        target.setPublishStatus(source.getPublishStatus());
        return target;
    }

    private McpSkillEntity copySkill(McpSkillEntity source) {
        McpSkillEntity target = new McpSkillEntity();
        target.setSkillCode(source.getSkillCode());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setCategory(source.getCategory());
        target.setFileName(source.getFileName());
        target.setFileSize(source.getFileSize());
        target.setCreatorId(null);
        target.setEnabled(source.getEnabled());
        target.setPublishStatus(source.getPublishStatus());
        return target;
    }
}
