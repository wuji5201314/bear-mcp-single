package com.mcpforge.admin.controller;

import com.mcpforge.admin.req.AdminTokenPromptSelectionSaveReq;
import com.mcpforge.admin.req.AdminTokenResourceSelectionSaveReq;
import com.mcpforge.admin.req.AdminTokenSaveReq;
import com.mcpforge.admin.req.AdminTokenSelectionSaveReq;
import com.mcpforge.admin.res.AdminTokenCreatedRes;
import com.mcpforge.admin.res.AdminTokenPromptSelectionRes;
import com.mcpforge.admin.res.AdminTokenResourceSelectionRes;
import com.mcpforge.admin.res.AdminTokenRes;
import com.mcpforge.admin.res.AdminTokenSelectionRes;
import com.mcpforge.admin.service.AdminTokenService;
import com.mcpforge.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/admin")
public class AdminTokenController {
    private final AdminTokenService tokenService;

    public AdminTokenController(AdminTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/tokens")
    public ApiResponse<List<AdminTokenRes>> list() {
        return ApiResponse.success(tokenService.list());
    }

    @PostMapping("/tokens")
    public ApiResponse<AdminTokenCreatedRes> create(@Valid @RequestBody AdminTokenSaveReq req) {
        return ApiResponse.success(tokenService.create(req));
    }

    @PutMapping("/tokens/{id}")
    public ApiResponse<AdminTokenRes> update(@PathVariable Long id,
                                             @Valid @RequestBody AdminTokenSaveReq req) {
        return ApiResponse.success(tokenService.update(id, req));
    }

    @GetMapping("/token-selections")
    public ApiResponse<List<AdminTokenSelectionRes>> listSelections() {
        return ApiResponse.success(tokenService.listSelections());
    }

    @GetMapping("/token-prompt-selections")
    public ApiResponse<List<AdminTokenPromptSelectionRes>> listPromptSelections() {
        return ApiResponse.success(tokenService.listPromptSelections());
    }

    @GetMapping("/token-resource-selections")
    public ApiResponse<List<AdminTokenResourceSelectionRes>> listResourceSelections() {
        return ApiResponse.success(tokenService.listResourceSelections());
    }

    @PutMapping("/tokens/{tokenId}/selections")
    public ApiResponse<Void> replaceSelections(@PathVariable Long tokenId,
                                               @RequestBody AdminTokenSelectionSaveReq req) {
        tokenService.replaceSelections(tokenId, req);
        return ApiResponse.success();
    }

    @PutMapping("/tokens/{tokenId}/prompt-selections")
    public ApiResponse<Void> replacePromptSelections(@PathVariable Long tokenId,
                                                     @RequestBody AdminTokenPromptSelectionSaveReq req) {
        tokenService.replacePromptSelections(tokenId, req);
        return ApiResponse.success();
    }

    @PutMapping("/tokens/{tokenId}/resource-selections")
    public ApiResponse<Void> replaceResourceSelections(@PathVariable Long tokenId,
                                                       @RequestBody AdminTokenResourceSelectionSaveReq req) {
        tokenService.replaceResourceSelections(tokenId, req);
        return ApiResponse.success();
    }
}
