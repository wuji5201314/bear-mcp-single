package com.mcpforge.admin.controller;

import com.mcpforge.admin.res.AdminAuditLogRes;
import com.mcpforge.admin.service.AdminAuditLogService;
import com.mcpforge.common.api.ApiResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/admin/audit-logs")
public class AdminAuditLogController {
    private final AdminAuditLogService auditLogService;

    public AdminAuditLogController(AdminAuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<List<AdminAuditLogRes>> list() {
        return ApiResponse.success(auditLogService.listRecent());
    }
}
