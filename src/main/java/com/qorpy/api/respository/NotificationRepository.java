package com.qorpy.api.respository;

import com.qorpy.api.entity.Notification;
import com.qorpy.api.enums.AlertSeverity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID>,
        JpaSpecificationExecutor<Notification> {

    List<Notification> findTop20ByOrderByTriggeredAtDesc();

    Page<Notification> findBySeverityAndTriggeredAtBetween(
            AlertSeverity severity, OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    Page<Notification> findBySeverity(AlertSeverity severity, Pageable pageable);

    Page<Notification> findByTriggeredAtBetween(
            OffsetDateTime from, OffsetDateTime to, Pageable pageable);
}