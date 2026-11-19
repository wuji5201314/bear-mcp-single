package com.mcpforge.core.mapper;

import com.mcpforge.core.entity.McpUserResourceSelectionEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** mcp_user_resource_selection 表 Mapper。 */
public interface McpUserResourceSelectionMapper {
    List<McpUserResourceSelectionEntity> findEnabledByTokenId(@Param("tokenId") Long tokenId);

    List<McpUserResourceSelectionEntity> findAll();

    int deleteByTokenId(@Param("tokenId") Long tokenId);

    int insert(McpUserResourceSelectionEntity entity);

    int insertIgnore(McpUserResourceSelectionEntity entity);

    int insertIfAbsent(McpUserResourceSelectionEntity entity);
}
