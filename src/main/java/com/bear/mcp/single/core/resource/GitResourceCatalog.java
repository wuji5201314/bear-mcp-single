package com.bear.mcp.single.core.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Reads the resources bundled in the application JAR. */
@Component
public class GitResourceCatalog {
    private static final String RESOURCE_ROOT = "mcp-resources";
    private static final String MANIFEST_PATH = RESOURCE_ROOT + "/manifest.json";

    private final Map<String, GitResource> resourcesByUri;

    public GitResourceCatalog(ObjectMapper objectMapper) {
        this.resourcesByUri = loadManifest(objectMapper);
    }

    public List<GitResource> list() {
        return List.copyOf(resourcesByUri.values());
    }

    public Optional<GitResource> find(String uri) {
        return Optional.ofNullable(resourcesByUri.get(uri));
    }

    public Optional<String> read(String uri) {
        GitResource resource = resourcesByUri.get(uri);
        if (resource == null) {
            return Optional.empty();
        }
        try (InputStream input = classPathResource(resource).getInputStream()) {
            return Optional.of(new String(input.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("读取 Git Resource 失败: " + uri, exception);
        }
    }

    public String objectKey(GitResource resource) {
        return "resource/mcp-mirror/" + resource.fileName();
    }

    private Map<String, GitResource> loadManifest(ObjectMapper objectMapper) {
        try (InputStream input = new ClassPathResource(MANIFEST_PATH).getInputStream()) {
            List<GitResource> resources = objectMapper.readValue(
                    input,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, GitResource.class));
            Map<String, GitResource> result = new LinkedHashMap<>();
            for (GitResource resource : resources) {
                validate(resource);
                if (result.put(resource.uri(), resource) != null) {
                    throw new IllegalStateException("Git Resource URI 重复: " + resource.uri());
                }
                if (!classPathResource(resource).exists()) {
                    throw new IllegalStateException("Git Resource 文件不存在: " + resource.fileName());
                }
            }
            return result;
        } catch (IOException exception) {
            throw new IllegalStateException("加载 Git Resource manifest 失败: " + MANIFEST_PATH, exception);
        }
    }

    private ClassPathResource classPathResource(GitResource resource) {
        return new ClassPathResource(RESOURCE_ROOT + "/" + resource.fileName());
    }

    private void validate(GitResource resource) {
        if (resource == null || resource.uri() == null || resource.uri().isBlank()
                || resource.fileName() == null || resource.fileName().isBlank()) {
            throw new IllegalStateException("Git Resource manifest 存在缺少 URI 或文件名的记录");
        }
        if (resource.fileName().contains("..") || resource.fileName().contains("/")
                || resource.fileName().contains("\\")) {
            throw new IllegalStateException("Git Resource 文件名不合法: " + resource.fileName());
        }
    }

    public record GitResource(
            String uri,
            String name,
            String description,
            String mimeType,
            String fileName,
            Long fileSize
    ) {
        public GitResource {
            name = name == null ? "" : name;
            description = description == null ? "" : description;
            mimeType = mimeType == null || mimeType.isBlank() ? "text/markdown" : mimeType;
        }
    }
}
