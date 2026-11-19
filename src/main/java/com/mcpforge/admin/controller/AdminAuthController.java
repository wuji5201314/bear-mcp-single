package com.mcpforge.admin.controller;

import com.mcpforge.admin.req.AdminLoginReq;
import com.mcpforge.admin.res.AdminLoginRes;
import com.mcpforge.admin.service.AdminAuthService;
import com.mcpforge.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {
    private final AdminAuthService authService;
    public AdminAuthController(AdminAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AdminLoginRes> login(@Valid @RequestBody AdminLoginReq req) {
        return ApiResponse.success(authService.login(req));
    }
}
