package com.qorpy.api.service;

import com.qorpy.api.dto.request.admin.AdminUserCreateRequest;
import com.qorpy.api.dto.response.AdminUserDto;
import com.qorpy.api.entity.AdminUser;
import com.qorpy.api.enums.AdminStatus;
import com.qorpy.api.exception.DuplicateResourceException;
import com.qorpy.api.respository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

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
        // Check for duplicate email
        if (adminUserRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        // Generate temporary password
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

        // Send email with temp password
        emailService.sendTemporaryPassword(saved.getEmail(), saved.getFullName(), tempPassword);

        // Audit log using the correct action type from the enum
        auditLogService.logAction(createdBy, "ACCOUNT_CREATED", "ADMIN_USER", saved.getId());

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