package com.bear.mcp.single.core.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BundledMcpCatalogTest {

    @Test
    void loadsTheReplicatedCatalogFromClasspath() {
        BundledMcpCatalog catalog = new BundledMcpCatalog(new ObjectMapper());

        assertEquals(3, catalog.requestConfigs().size());
        assertEquals(3, catalog.dynamicTools().size());
        assertEquals(3, catalog.prompts().size());
        assertEquals(2, catalog.skills().size());
        assertTrue(catalog.requestConfigs().stream().allMatch(item -> item.getConfigKey() != null));
        assertTrue(catalog.dynamicTools().stream().allMatch(item -> item.getToolName() != null));
        assertTrue(catalog.prompts().stream().allMatch(item -> item.getPromptName() != null));
        assertTrue(catalog.skills().stream().allMatch(item -> item.getSkillCode() != null));
        assertEquals("skills/SKILL0000000005/SKILL.md", catalog.skillObjectKey("SKILL0000000005"));
        assertTrue(catalog.readSkill("SKILL0000000005").orElseThrow().contains("排查步骤"));
        assertTrue(catalog.readSkill("SKILL0000000001").isEmpty());
    }
}
