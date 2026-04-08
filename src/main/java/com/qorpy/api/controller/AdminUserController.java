package com.qorpy.api.controller;

import com.qorpy.api.dto.request.admin.AdminUserCreateRequest;
import com.qorpy.api.dto.request.admin.UpdateAdminUserRequest;
import com.qorpy.api.dto.response.AdminUserDto;
import com.qorpy.api.security.AdminUserDetails;
import com.qorpy.api.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Page<AdminUserDto>> getAllUsers(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminUserService.getAllUsers(search, pageable));
    }

    @PostMapping
    public ResponseEntity<AdminUserDto> createUser(
            @Valid @RequestBody AdminUserCreateRequest request,
            @AuthenticationPrincipal AdminUserDetails currentUser) {
        AdminUserDto created = adminUserService.createUser(request, currentUser.getAdminUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<AdminUserDto> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateAdminUserRequest request,
            @AuthenticationPrincipal AdminUserDetails currentUser) {
        AdminUserDto updated = adminUserService.updateUser(userId, request, currentUser.getAdminUser());
        return ResponseEntity.ok(updated);
    }
}