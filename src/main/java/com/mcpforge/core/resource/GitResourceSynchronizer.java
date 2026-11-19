package com.mcpforge.core.resource;

import com.mcpforge.core.entity.McpResourceEntity;
import com.mcpforge.core.entity.McpRoleEntity;
import com.mcpforge.core.entity.McpRoleResourceEntity;
import com.mcpforge.core.entity.McpRoleToolEntity;
import com.mcpforge.core.entity.McpUserResourceSelectionEntity;
import com.mcpforge.core.entity.McpUserTokenEntity;
import com.mcpforge.core.entity.McpUserToolSelectionEntity;
import com.mcpforge.core.mapper.McpResourceMapper;
import com.mcpforge.core.mapper.McpRoleMapper;
import com.mcpforge.core.mapper.McpRoleResourceMapper;
import com.mcpforge.core.mapper.McpRoleToolMapper;
import com.mcpforge.core.mapper.McpUserResourceSelectionMapper;
import com.mcpforge.core.mapper.McpUserTokenMapper;
import com.mcpforge.core.mapper.McpUserToolSelectionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Synchronizes bundled Resource metadata into the existing permission model. */
@Component
@Slf4j
public class GitResourceSynchronizer {
    private static final Set<String> RESOURCE_TOOLS = Set.of("list_mcp_resources", "read_mcp_resource");

    private final GitResourceCatalog gitResourceCatalog;
    private final McpResourceMapper resourceMapper;
    private final McpRoleMapper roleMapper;
    private final McpRoleResourceMapper roleResourceMapper;
    private final McpRoleToolMapper roleToolMapper;
    private final McpUserTokenMapper tokenMapper;
    private final McpUserResourceSelectionMapper resourceSelectionMapper;
    private final McpUserToolSelectionMapper toolSelectionMapper;
    private final JdbcTemplate jdbcTemplate;

    public GitResourceSynchronizer(GitResourceCatalog gitResourceCatalog,
                                   McpResourceMapper resourceMapper,
                                   McpRoleMapper roleMapper,
                                   McpRoleResourceMapper roleResourceMapper,
                                   McpRoleToolMapper roleToolMapper,
                                   McpUserTokenMapper tokenMapper,
                                   McpUserResourceSelectionMapper resourceSelectionMapper,
                                   McpUserToolSelectionMapper toolSelectionMapper,
                                   JdbcTemplate jdbcTemplate) {
        this.gitResourceCatalog = gitResourceCatalog;
        this.resourceMapper = resourceMapper;
        this.roleMapper = roleMapper;
        this.roleResourceMapper = roleResourceMapper;
        this.roleToolMapper = roleToolMapper;
        this.tokenMapper = tokenMapper;
        this.resourceSelectionMapper = resourceSelectionMapper;
        this.toolSelectionMapper = toolSelectionMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void synchronize() {
        try {
            List<GitResourceCatalog.GitResource> bundled = gitResourceCatalog.list();
            Set<String> addedUris = synchronizeResourceMetadata(bundled);
            Set<String> resourceUris = bundled.stream()
                    .map(GitResourceCatalog.GitResource::uri)
                    .collect(Collectors.toSet());
            int removedDuplicates = removeDuplicateDdlResources(resourceUris);

            Set<String> activeRoleCodes = roleMapper.findAll().stream()
                    .filter(role -> Integer.valueOf(1).equals(role.getIsEnabled()))
                    .map(McpRoleEntity::getRoleCode)
                    .collect(Collectors.toSet());
            Set<String> mappedResourceRoles = roleResourceMapper.findAll().stream()
                    .map(McpRoleResourceEntity::getRoleCode)
                    .filter(activeRoleCodes::contains)
                    .collect(Collectors.toSet());
            Set<String> toolGrantedRoles = roleToolMapper.findAll().stream()
                    .filter(item -> RESOURCE_TOOLS.contains(item.getToolName()))
                    .map(McpRoleToolEntity::getRoleCode)
                    .filter(activeRoleCodes::contains)
                    .collect(Collectors.toSet());
            mappedResourceRoles.addAll(toolGrantedRoles);
            synchronizeRolePermissions(mappedResourceRoles, resourceUris);
            synchronizeTokenSelections(resourceUris);
            log.info("Git Resource 同步完成，bundledCount={}, addedCount={}, removedDuplicateCount={}, roles={}, tokens={}",
                    bundled.size(), addedUris.size(), removedDuplicates, mappedResourceRoles.size(), tokenMapper.findAll().size());
        } catch (RuntimeException exception) {
            // 数据库迁移仍由部署配置负责；同步失败不能让已有 MCP 能力整体无法启动。
            log.error("Git Resource 元数据同步失败，运行时仍会使用已存在的数据库权限", exception);
        }
    }

    private Set<String> synchronizeResourceMetadata(List<GitResourceCatalog.GitResource> bundled) {
        Set<String> addedUris = new HashSet<>();
        for (GitResourceCatalog.GitResource resource : bundled) {
            McpResourceEntity existing = resourceMapper.findByUri(resource.uri());
            if (existing != null) {
                McpResourceEntity target = toEntity(resource);
                target.setId(existing.getId());
                target.setCreatorId(existing.getCreatorId());
                target.setEnabled(existing.getEnabled());
                target.setPublishStatus(existing.getPublishStatus());
                resourceMapper.update(target);
                continue;
            }
            McpResourceEntity entity = toEntity(resource);
            resourceMapper.insert(entity);
            addedUris.add(resource.uri());
        }
        return addedUris;
    }

    /**
     * The old catalog stored the same table document under both schema/ and ddl/.
     * Keep the canonical schema record and remove only ddl rows that have a matching
     * schema URI, including their stale permission and selection rows.
     */
    private int removeDuplicateDdlResources(Set<String> bundledUris) {
        List<Map<String, Object>> duplicateRows = jdbcTemplate.queryForList("""
                SELECT duplicate_resource.id
                     , duplicate_resource.resource_uri
                FROM mcp_resource duplicate_resource
                JOIN mcp_resource canonical_resource
                  ON canonical_resource.resource_uri = REPLACE(
                       duplicate_resource.resource_uri,
                       '/ddl/',
                       '/schema/'
                     )
                WHERE duplicate_resource.resource_uri LIKE 'mcpforge://xxx/text-to-sql/ddl/%'
                """);
        List<Long> duplicateIds = duplicateRows.stream()
                .filter(row -> !bundledUris.contains(String.valueOf(row.get("resource_uri"))))
                .map(row -> ((Number) row.get("id")).longValue())
                .toList();
        if (duplicateIds.isEmpty()) {
            return 0;
        }

        List<String> idStrings = duplicateIds.stream().map(String::valueOf).toList();
        String placeholders = String.join(",", Collections.nCopies(duplicateIds.size(), "?"));
        String resourceUriQuery = "SELECT resource_uri FROM mcp_resource WHERE id IN (" + placeholders + ")";
        List<String> duplicateUris = jdbcTemplate.queryForList(resourceUriQuery, String.class, duplicateIds.toArray());

        String uriPlaceholders = String.join(",", Collections.nCopies(duplicateUris.size(), "?"));
        jdbcTemplate.update(
                "DELETE FROM mcp_community_like WHERE target_type='RESOURCE' "
                        + "AND target_key IN (" + placeholders + ")",
                idStrings.toArray());
        jdbcTemplate.update(
                "DELETE FROM mcp_user_resource_selection WHERE resource_uri IN (" + uriPlaceholders + ")",
                duplicateUris.toArray());
        jdbcTemplate.update(
                "DELETE FROM mcp_role_resource WHERE resource_uri IN (" + uriPlaceholders + ")",
                duplicateUris.toArray());
        jdbcTemplate.update(
                "DELETE FROM mcp_resource WHERE id IN (" + placeholders + ")",
                duplicateIds.toArray());
        return duplicateIds.size();
    }

    private void synchronizeRolePermissions(Set<String> roleCodes, Set<String> resourceUris) {
        for (String roleCode : roleCodes) {
            for (String resourceUri : resourceUris) {
                McpRoleResourceEntity entity = new McpRoleResourceEntity();
                entity.setRoleCode(roleCode);
                entity.setResourceUri(resourceUri);
                roleResourceMapper.insertIgnore(entity);
            }
        }
    }

    private void synchronizeTokenSelections(Set<String> resourceUris) {
        Map<Long, List<McpUserResourceSelectionEntity>> selectionsByToken = resourceSelectionMapper.findAll().stream()
                .collect(Collectors.groupingBy(McpUserResourceSelectionEntity::getTokenId));
        Set<Long> tokensWithResourceTools = toolSelectionMapper.findAll().stream()
                .filter(item -> Integer.valueOf(1).equals(item.getEnabled()))
                .filter(item -> RESOURCE_TOOLS.contains(item.getToolName()))
                .map(McpUserToolSelectionEntity::getTokenId)
                .collect(Collectors.toSet());
        for (McpUserTokenEntity token : tokenMapper.findAll()) {
            if (!Integer.valueOf(1).equals(token.getIsActive())) {
                continue;
            }
            List<McpUserResourceSelectionEntity> selections = selectionsByToken
                    .getOrDefault(token.getId(), List.of());
            boolean hasEnabledSelection = selections.stream()
                    .anyMatch(item -> Integer.valueOf(1).equals(item.getEnabled()));
            if (!hasEnabledSelection && !tokensWithResourceTools.contains(token.getId())) {
                continue;
            }
            Set<String> existingUris = selections.stream()
                    .map(McpUserResourceSelectionEntity::getResourceUri)
                    .collect(Collectors.toSet());
            for (String resourceUri : resourceUris) {
                if (existingUris.contains(resourceUri)) {
                    continue;
                }
                McpUserResourceSelectionEntity entity = new McpUserResourceSelectionEntity();
                entity.setTokenId(token.getId());
                entity.setResourceUri(resourceUri);
                entity.setEnabled(1);
                resourceSelectionMapper.insertIfAbsent(entity);
            }
        }
    }

    private McpResourceEntity toEntity(GitResourceCatalog.GitResource resource) {
        McpResourceEntity entity = new McpResourceEntity();
        entity.setResourceUri(resource.uri());
        entity.setName(resource.name());
        entity.setDescription(resource.description());
        entity.setMimeType(resource.mimeType());
        entity.setObjectKey(gitResourceCatalog.objectKey(resource));
        entity.setFileName(resource.fileName());
        entity.setFileSize(resource.fileSize());
        entity.setCreatorId(null);
        entity.setEnabled(1);
        entity.setPublishStatus(2);
        return entity;
    }
}
