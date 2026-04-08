package com.qorpy.api.service;

import com.qorpy.api.dto.request.admin.AdminUserCreateRequest;
import com.qorpy.api.dto.request.admin.UpdateAdminUserRequest;
import com.qorpy.api.dto.response.AdminUserDto;
import com.qorpy.api.entity.AdminUser;
import com.qorpy.api.enums.AdminRole;
import com.qorpy.api.enums.AdminStatus;
import com.qorpy.api.exception.BusinessException;
import com.qorpy.api.exception.DuplicateResourceException;
import com.qorpy.api.exception.ResourceNotFoundException;
import com.qorpy.api.respository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    public Page<AdminUserDto> getAllUsers(String search, Pageable pageable) {
        Page<AdminUser> userPage;
        if (search != null && !search.isBlank()) {
            userPage = adminUserRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    search, search, pageable);
        } else {
            userPage = adminUserRepository.findAll(pageable);
        }
        return userPage.map(this::toDto);
    }

    @Transactional
    public AdminUserDto createUser(AdminUserCreateRequest request, AdminUser createdBy) {
        if (adminUserRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        String tempPassword = generateTempPassword();

        AdminUser newUser = AdminUser.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .role(request.getRole())
                .status(AdminStatus.ACTIVE)
                .failedAttempts(0)
                .build();

        AdminUser saved = adminUserRepository.save(newUser);
        emailService.sendTemporaryPassword(saved.getEmail(), saved.getFullName(), tempPassword);
        auditLogService.logAction(createdBy, "ACCOUNT_CREATED", "ADMIN_USER", saved.getId());

        return toDto(saved);
    }

    @Transactional
    public AdminUserDto updateUser(UUID userId, UpdateAdminUserRequest request, AdminUser updatedBy) {
        AdminUser userToUpdate = adminUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Prevent editing another Super Admin account
        if (userToUpdate.getRole() == AdminRole.SUPER_ADMIN &&
                !userToUpdate.getId().equals(updatedBy.getId())) {
            throw new BusinessException("Cannot edit another Super Admin account");
        }

        // Prevent self-role change if currently Super Admin (downgrading themselves)
        if (userToUpdate.getId().equals(updatedBy.getId()) &&
                updatedBy.getRole() == AdminRole.SUPER_ADMIN &&
                request.getRole() != AdminRole.SUPER_ADMIN) {
            throw new BusinessException("Super Admin cannot downgrade their own role");
        }

        // Capture before state as Map for audit log
        Map<String, Object> before = Map.of(
                "fullName", userToUpdate.getFullName(),
                "role", userToUpdate.getRole().name()
        );

        userToUpdate.setFullName(request.getFullName());
        userToUpdate.setRole(request.getRole());

        AdminUser saved = adminUserRepository.save(userToUpdate);

        // Capture after state as Map for audit log
        Map<String, Object> after = Map.of(
                "fullName", saved.getFullName(),
                "role", saved.getRole().name()
        );

        auditLogService.logActionWithDetails(updatedBy, "ACCOUNT_EDITED", "ADMIN_USER",
                saved.getId(), before, after);

        return toDto(saved);
    }

    private String generateTempPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[9];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private AdminUserDto toDto(AdminUser user) {
        return AdminUserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}