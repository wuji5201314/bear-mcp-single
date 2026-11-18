package com.bear.mcp.single.core.resource;

import com.bear.mcp.single.core.context.McpUserContext;
import com.bear.mcp.single.core.entity.McpResourceEntity;
import com.bear.mcp.single.core.entity.McpUserResourceSelectionEntity;
import com.bear.mcp.single.core.mapper.McpResourceMapper;
import com.bear.mcp.single.core.mapper.McpUserResourceSelectionMapper;
import com.bear.mcp.single.core.storage.TosStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceAccessServiceTest {

    @Mock
    private McpResourceMapper resourceMapper;

    @Mock
    private McpUserResourceSelectionMapper selectionMapper;

    @Mock
    private TosStorageService tosStorageService;

    @Test
    void shouldReadBundledContentBeforeTos() {
        GitResourceCatalog catalog = new GitResourceCatalog(new com.fasterxml.jackson.databind.ObjectMapper());
        ResourceAccessService service = new ResourceAccessService(
                resourceMapper, selectionMapper, tosStorageService, catalog);
        McpResourceEntity entity = new McpResourceEntity();
        entity.setResourceUri("bear://xxx/text-to-sql/schema/student");
        entity.setObjectKey("resource/missing-from-tos.md");

        String content = service.readText(entity);

        assertThat(content).contains("student");
        verify(tosStorageService, never()).downloadString(entity.getObjectKey());
    }

    @Test
    void shouldExposeBundledResourceWhenDatabaseMetadataIsMissing() {
        GitResourceCatalog catalog = new GitResourceCatalog(new com.fasterxml.jackson.databind.ObjectMapper());
        ResourceAccessService service = new ResourceAccessService(
                resourceMapper, selectionMapper, tosStorageService, catalog);
        when(selectionMapper.findEnabledByTokenId(7L)).thenReturn(List.of(selection(7L,
                "bear://xxx/text-to-sql/schema/student")));
        when(resourceMapper.findByUri("bear://xxx/text-to-sql/schema/student")).thenReturn(null);
        McpUserContext context = new McpUserContext(
                10001L,
                7L,
                "demo-admin",
                Set.of("ADMIN"),
                Set.of(),
                Set.of(),
                Set.of("bear://xxx/text-to-sql/schema/student"),
                "session",
                "127.0.0.1");

        McpResourceEntity result = service.findAccessibleByUri(
                context, "bear://xxx/text-to-sql/schema/student");

        assertThat(result).isNotNull();
        assertThat(result.getFileName()).endsWith(".md");
        assertThat(service.readText(result)).contains("student");
    }

    @Test
    void shouldRespectDisabledDatabaseMetadataForBundledUri() {
        GitResourceCatalog catalog = new GitResourceCatalog(new com.fasterxml.jackson.databind.ObjectMapper());
        ResourceAccessService service = new ResourceAccessService(
                resourceMapper, selectionMapper, tosStorageService, catalog);
        String uri = "bear://xxx/text-to-sql/schema/student";
        when(selectionMapper.findEnabledByTokenId(7L)).thenReturn(List.of(selection(7L, uri)));
        McpResourceEntity disabled = new McpResourceEntity();
        disabled.setResourceUri(uri);
        disabled.setEnabled(0);
        disabled.setPublishStatus(2);
        when(resourceMapper.findByUri(uri)).thenReturn(disabled);

        McpUserContext context = new McpUserContext(
                10001L, 7L, "demo-admin", Set.of("ADMIN"), Set.of(), Set.of(), Set.of(uri),
                "session", "127.0.0.1");

        assertThat(service.findAccessibleByUri(context, uri)).isNull();
    }

    private McpUserResourceSelectionEntity selection(Long tokenId, String uri) {
        McpUserResourceSelectionEntity selection = new McpUserResourceSelectionEntity();
        selection.setTokenId(tokenId);
        selection.setResourceUri(uri);
        selection.setEnabled(1);
        return selection;
    }
}
