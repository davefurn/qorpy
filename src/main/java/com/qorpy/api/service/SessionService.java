package com.qorpy.api.service;

import com.qorpy.api.entity.AdminUser;
import com.qorpy.api.respository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final AdminUserRepository adminUserRepository;

    @Transactional
    public void invalidateUserSessions(UUID userId) {
        AdminUser user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTokenVersion(user.getTokenVersion() + 1);
        adminUserRepository.save(user);
    }
}