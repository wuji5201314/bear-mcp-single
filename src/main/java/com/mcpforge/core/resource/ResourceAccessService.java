package com.mcpforge.core.resource;

import com.mcpforge.core.context.McpUserContext;
import com.mcpforge.core.entity.McpResourceEntity;
import com.mcpforge.core.entity.McpUserResourceSelectionEntity;
import com.mcpforge.core.mapper.McpResourceMapper;
import com.mcpforge.core.mapper.McpUserResourceSelectionMapper;
import com.mcpforge.core.storage.TosStorageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** MCP Resource 运行时权限和内容读取。 */
@Service
public class ResourceAccessService {
    private final McpResourceMapper resourceMapper;
    private final McpUserResourceSelectionMapper resourceSelectionMapper;
    private final TosStorageService tosStorageService;
    private final GitResourceCatalog gitResourceCatalog;

    public ResourceAccessService(McpResourceMapper resourceMapper,
                                 McpUserResourceSelectionMapper resourceSelectionMapper,
                                 TosStorageService tosStorageService,
                                 GitResourceCatalog gitResourceCatalog) {
        this.resourceMapper = resourceMapper;
        this.resourceSelectionMapper = resourceSelectionMapper;
        this.tosStorageService = tosStorageService;
        this.gitResourceCatalog = gitResourceCatalog;
    }

    public List<McpResourceEntity> listAccessibleResources(McpUserContext context) {
        if (context == null || context.tokenId() == null || context.allowedResources().isEmpty()) {
            return List.of();
        }
        Set<String> selectedUris = selectedUris(context.tokenId());
        if (selectedUris.isEmpty()) {
            return List.of();
        }
        List<McpResourceEntity> published = resourceMapper.findPublished();
        List<McpResourceEntity> result = new ArrayList<>(published.stream()
                .filter(item -> context.allowedResources().contains(item.getResourceUri()))
                .filter(item -> selectedUris.contains(item.getResourceUri()))
                .toList());

        Set<String> existingUris = new HashSet<>(resourceMapper.findAll().stream()
                .map(McpResourceEntity::getResourceUri)
                .toList());
        gitResourceCatalog.list().stream()
                .filter(item -> !existingUris.contains(item.uri()))
                .filter(item -> context.allowedResources().contains(item.uri()))
                .filter(item -> selectedUris.contains(item.uri()))
                .map(this::toEntity)
                .forEach(result::add);
        return result;
    }

    public McpResourceEntity findAccessibleByUri(McpUserContext context, String uri) {
        if (!canAccess(context, uri)) {
            return null;
        }
        McpResourceEntity entity = resourceMapper.findByUri(uri);
        if (entity != null) {
            return Integer.valueOf(1).equals(entity.getEnabled())
                    && entity.getPublishStatus() != null
                    && entity.getPublishStatus() != 0 ? entity : null;
        }
        return gitResourceCatalog.find(uri).map(this::toEntity).orElse(null);
    }

    public String readText(McpResourceEntity entity) {
        if (entity == null) {
            return "";
        }
        Optional<String> bundledContent = gitResourceCatalog.read(entity.getResourceUri());
        if (bundledContent.isPresent()) {
            return bundledContent.get();
        }
        if (entity.getObjectKey() == null || entity.getObjectKey().isBlank()) {
            return "";
        }
        return tosStorageService.downloadString(entity.getObjectKey());
    }

    private boolean canAccess(McpUserContext context, String uri) {
        if (context == null || uri == null || uri.isBlank()) {
            return false;
        }
        return context.allowedResources().contains(uri) && selectedUris(context.tokenId()).contains(uri);
    }

    private Set<String> selectedUris(Long tokenId) {
        if (tokenId == null) {
            return Set.of();
        }
        return resourceSelectionMapper.findEnabledByTokenId(tokenId).stream()
                .map(McpUserResourceSelectionEntity::getResourceUri)
                .collect(Collectors.toSet());
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
        entity.setEnabled(1);
        entity.setPublishStatus(2);
        return entity;
    }
}
