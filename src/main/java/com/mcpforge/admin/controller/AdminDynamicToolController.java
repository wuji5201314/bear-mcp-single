package com.mcpforge.admin.controller;

import com.mcpforge.admin.res.AdminDynamicToolRes;
import com.mcpforge.admin.service.AdminDynamicToolService;
import com.mcpforge.common.api.ApiResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 管理端仅展示动态工具，创作空间负责新增和编辑。 */
@RestController
@CrossOrigin
@RequestMapping("/api/admin/dynamic-tools")
public class AdminDynamicToolController {
    private final AdminDynamicToolService dynamicToolService;

    public AdminDynamicToolController(AdminDynamicToolService dynamicToolService) {
        this.dynamicToolService = dynamicToolService;
    }

    @GetMapping
    public ApiResponse<List<AdminDynamicToolRes>> list() {
        return ApiResponse.success(dynamicToolService.list());
    }
}
