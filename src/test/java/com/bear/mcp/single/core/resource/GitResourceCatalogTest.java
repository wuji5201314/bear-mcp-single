package com.bear.mcp.single.core.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GitResourceCatalogTest {

    @Test
    void shouldLoadAllBundledResourcesAndReadContent() {
        GitResourceCatalog catalog = new GitResourceCatalog(new ObjectMapper());

        assertThat(catalog.list()).hasSize(3);
        assertThat(catalog.find("bear://xxx/text-to-sql/schema/student"))
                .isPresent();
        assertThat(catalog.objectKey(catalog.find("bear://xxx/text-to-sql/schema/student").orElseThrow()))
                .isEqualTo("resource/mcp-mirror/002-xxx__text-to-sql__schema__student.md");
        assertThat(catalog.read("bear://xxx/text-to-sql/schema/student"))
                .isPresent()
                .get()
                .asString()
                .contains("student");
        assertThat(catalog.read("bear://missing/resource")).isEmpty();
    }
}
