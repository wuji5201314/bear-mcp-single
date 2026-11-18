package com.bear.mcp.single.core.skill;

import com.bear.mcp.single.core.catalog.BundledMcpCatalog;
import com.bear.mcp.single.core.entity.McpSkillEntity;
import com.bear.mcp.single.core.storage.TosStorageService;
import org.springframework.stereotype.Service;

/** Reads Skill Markdown from Git first and keeps TOS compatibility for older Skills. */
@Service
public class SkillContentService {
    private final BundledMcpCatalog catalog;
    private final TosStorageService tosStorageService;

    public SkillContentService(BundledMcpCatalog catalog, TosStorageService tosStorageService) {
        this.catalog = catalog;
        this.tosStorageService = tosStorageService;
    }

    public String read(McpSkillEntity skill) {
        if (skill == null) {
            return "";
        }
        return catalog.readSkill(skill.getSkillCode())
                .orElseGet(() -> tosStorageService.downloadString(skill.getObjectKey()));
    }
}
