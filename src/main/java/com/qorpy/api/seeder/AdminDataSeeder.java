package com.qorpy.api.seeder;

import com.qorpy.api.entity.AdminUser;
import com.qorpy.api.enums.AdminRole;
import com.qorpy.api.enums.AdminStatus;
import com.qorpy.api.respository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminDataSeeder implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@qucoon.com";
        String targetPassword = "Admin@1234";

        adminUserRepository.findByEmail(adminEmail).ifPresentOrElse(
                user -> {
                    boolean needsUpdate = false;


                    if (!passwordEncoder.matches(targetPassword, user.getPasswordHash())) {
                        user.setPasswordHash(passwordEncoder.encode(targetPassword));
                        needsUpdate = true;
                    }


                    if (user.getStatus() != AdminStatus.ACTIVE || user.getLockedUntil() != null || user.getFailedAttempts() > 0) {
                        user.setStatus(AdminStatus.ACTIVE);
                        user.setFailedAttempts(0);
                        user.setLockedUntil(null);
                        needsUpdate = true;
                    }

                    if (needsUpdate) {
                        adminUserRepository.save(user);
                        System.out.println("✅ SECURITY: Super Admin account forcibly synced and unlocked.");
                    } else {
                        System.out.println("✅ SECURITY: Super Admin account verified. Ready for login.");
                    }
                },
                () -> {
                    AdminUser newAdmin = new AdminUser();
                    newAdmin.setEmail(adminEmail);
                    newAdmin.setFullName("Super Admin");
                    newAdmin.setPasswordHash(passwordEncoder.encode(targetPassword));
                    newAdmin.setRole(AdminRole.SUPER_ADMIN);
                    newAdmin.setStatus(AdminStatus.ACTIVE);
                    newAdmin.setFailedAttempts(0);
                    adminUserRepository.save(newAdmin);
                    System.out.println("✅ SECURITY: Super Admin account newly seeded.");
                }
        );
    }
}