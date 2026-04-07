package com.qorpy.api.service;

import com.qorpy.api.dto.response.AdminUserDto;
import com.qorpy.api.entity.AdminUser;
import com.qorpy.api.respository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;

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