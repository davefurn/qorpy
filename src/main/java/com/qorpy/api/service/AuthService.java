package com.qorpy.api.service;

import com.qorpy.api.dto.request.authentication.LoginRequest;
import com.qorpy.api.dto.response.AuthResponse;
import com.qorpy.api.entity.AdminUser;
import com.qorpy.api.entity.BlacklistedToken;
import com.qorpy.api.enums.AdminStatus;
import com.qorpy.api.respository.AdminUserRepository;
import com.qorpy.api.respository.BlacklistedTokenRepository;
import com.qorpy.api.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminUserRepository userRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuditLogService auditLogService;
    @Transactional
    public AuthResponse authenticate(LoginRequest request) {

        AdminUser user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        // Check if account is deactivated
        if (user.getStatus() == AdminStatus.INACTIVE) {
            throw new RuntimeException("Account is deactivated. Please contact an administrator.");
        }

        // Check if account is locked
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(OffsetDateTime.now())) {
            throw new RuntimeException("Account is temporarily locked due to multiple failed attempts. Please try again later.");
        }

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new BadCredentialsException("Invalid credentials");
        }

        // Successful login - reset failed attempts and lock, update last login
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        auditLogService.logAction(user, "LOGIN", "ADMIN_USER", user.getId());

        String token = jwtUtils.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    private void handleFailedLogin(AdminUser user) {
        user.setFailedAttempts(user.getFailedAttempts() + 1);

        if (user.getFailedAttempts() >= 5) {
            user.setLockedUntil(OffsetDateTime.now().plusMinutes(15));
        }
        userRepository.save(user);
        auditLogService.logAction(null, "LOGIN_FAILED", "ADMIN_USER", user.getId());
    }
    @Transactional
    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            BlacklistedToken blacklisted = new BlacklistedToken();
            blacklisted.setToken(token);
            // Sets expiry to 30 minutes from now, matching the JWT expiration
            blacklisted.setExpiryDate(new Date(System.currentTimeMillis() + 1800000));
            blacklistedTokenRepository.save(blacklisted);

            auditLogService.logAction(null, "LOGOUT", "ADMIN_USER", null);
        }
    }
}