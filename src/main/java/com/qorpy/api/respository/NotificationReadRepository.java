package com.qorpy.api.respository;

import com.qorpy.api.entity.NotificationRead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationReadRepository extends JpaRepository<NotificationRead, UUID> {

    Optional<NotificationRead> findByNotificationIdAndAdminId(UUID notificationId, UUID adminId);

    long countByAdminIdAndIsReadFalse(UUID adminId);
}