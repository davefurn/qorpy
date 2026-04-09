package com.qorpy.api.service;

import com.qorpy.api.dto.request.notification.UpdateNotificationSettingsRequest;
import com.qorpy.api.dto.response.NotificationBellDto;
import com.qorpy.api.dto.response.NotificationDto;
import com.qorpy.api.dto.response.NotificationSettingsDto;
import com.qorpy.api.entity.AdminUser;
import com.qorpy.api.entity.Notification;
import com.qorpy.api.entity.NotificationRead;
import com.qorpy.api.entity.UserNotificationSettings;
import com.qorpy.api.enums.AlertSeverity;
import com.qorpy.api.exception.ResourceNotFoundException;
import com.qorpy.api.respository.NotificationReadRepository;
import com.qorpy.api.respository.NotificationRepository;
import com.qorpy.api.respository.UserNotificationSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * EP-07 — US-024, US-025, US-026
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationReadRepository notificationReadRepository;
    private final UserNotificationSettingsRepository settingsRepository;

    /**
     * US-024 — Bell icon data: unread count + 20 most recent notifications.
     */
    public NotificationBellDto getBellData(AdminUser admin) {
        long unread = notificationReadRepository.countByAdminIdAndIsReadFalse(admin.getId());

        List<Notification> recent = notificationRepository.findTop20ByOrderByTriggeredAtDesc();

        List<NotificationDto> dtos = recent.stream()
                .map(n -> toDto(n, admin.getId()))
                .collect(Collectors.toList());

        return NotificationBellDto.builder()
                .unreadCount(unread)
                .recent(dtos)
                .build();
    }

    /**
     * US-024 — Mark a single notification as read and return it.
     */
    @Transactional
    public NotificationDto markAsRead(UUID notificationId, AdminUser admin) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found: " + notificationId));

        NotificationRead read = notificationReadRepository
                .findByNotificationIdAndAdminId(notificationId, admin.getId())
                .orElseGet(() -> NotificationRead.builder()
                        .notification(notification)
                        .admin(admin)
                        .build());

        read.setRead(true);
        read.setReadAt(OffsetDateTime.now());
        notificationReadRepository.save(read);

        return toDto(notification, admin.getId());
    }

    /**
     * US-024 — Mark ALL notifications as read for the current admin.
     */
    @Transactional
    public void markAllAsRead(AdminUser admin) {
        List<Notification> all = notificationRepository.findTop20ByOrderByTriggeredAtDesc();
        for (Notification n : all) {
            notificationReadRepository
                    .findByNotificationIdAndAdminId(n.getId(), admin.getId())
                    .ifPresentOrElse(
                            r -> {
                                r.setRead(true);
                                r.setReadAt(OffsetDateTime.now());
                                notificationReadRepository.save(r);
                            },
                            () -> {
                                NotificationRead r = NotificationRead.builder()
                                        .notification(n)
                                        .admin(admin)
                                        .isRead(true)
                                        .readAt(OffsetDateTime.now())
                                        .build();
                                notificationReadRepository.save(r);
                            }
                    );
        }
    }

    /**
     * US-025 — Full notification history, filterable by severity and date range.
     */
    public Page<NotificationDto> getHistory(
            AlertSeverity severity,
            OffsetDateTime from,
            OffsetDateTime to,
            AdminUser admin,
            Pageable pageable) {

        Page<Notification> page;

        if (severity != null && from != null && to != null) {
            page = notificationRepository.findBySeverityAndTriggeredAtBetween(
                    severity, from, to, pageable);
        } else if (severity != null) {
            page = notificationRepository.findBySeverity(severity, pageable);
        } else if (from != null && to != null) {
            page = notificationRepository.findByTriggeredAtBetween(from, to, pageable);
        } else {
            page = notificationRepository.findAll(pageable);
        }

        return page.map(n -> toDto(n, admin.getId()));
    }

    /**
     * US-026 — Get notification email settings for the current admin.
     */
    public NotificationSettingsDto getSettings(AdminUser admin) {
        UserNotificationSettings settings = settingsRepository
                .findByAdminId(admin.getId())
                .orElseGet(() -> UserNotificationSettings.builder()
                        .admin(admin)
                        .emailCriticalAlerts(false)
                        .build());
        return toSettingsDto(settings, admin.getId());
    }

    /**
     * US-026 — Update email notification toggle for the current admin.
     */
    @Transactional
    public NotificationSettingsDto updateSettings(
            UpdateNotificationSettingsRequest request, AdminUser admin) {

        UserNotificationSettings settings = settingsRepository
                .findByAdminId(admin.getId())
                .orElseGet(() -> UserNotificationSettings.builder()
                        .admin(admin)
                        .build());

        settings.setEmailCriticalAlerts(request.isEmailCriticalAlerts());
        UserNotificationSettings saved = settingsRepository.save(settings);
        return toSettingsDto(saved, admin.getId());
    }

    // ── helpers ──

    private NotificationDto toDto(Notification n, UUID adminId) {
        boolean isRead = false;
        OffsetDateTime readAt = null;

        if (adminId != null) {
            var readRecord = notificationReadRepository
                    .findByNotificationIdAndAdminId(n.getId(), adminId);
            if (readRecord.isPresent()) {
                isRead  = readRecord.get().isRead();
                readAt  = readRecord.get().getReadAt();
            }
        }

        return NotificationDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .description(n.getDescription())
                .severity(n.getSeverity())
                .entityType(n.getEntityType())
                .entityId(n.getEntityId())
                .alertRuleId(n.getAlertRule() != null ? n.getAlertRule().getId() : null)
                .alertRuleName(n.getAlertRule() != null ? n.getAlertRule().getName() : null)
                .triggeredAt(n.getTriggeredAt())
                .isRead(isRead)
                .readAt(readAt)
                .build();
    }

    private NotificationSettingsDto toSettingsDto(UserNotificationSettings s, UUID adminId) {
        return NotificationSettingsDto.builder()
                .adminId(adminId)
                .emailCriticalAlerts(s.isEmailCriticalAlerts())
                .build();
    }
}