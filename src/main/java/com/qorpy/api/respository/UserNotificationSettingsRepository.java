package com.qorpy.api.respository;

import com.qorpy.api.entity.UserNotificationSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserNotificationSettingsRepository extends JpaRepository<UserNotificationSettings, UUID> {
    Optional<UserNotificationSettings> findByAdminId(UUID adminId);
}