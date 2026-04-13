package com.qorpy.api.controller;

import com.qorpy.api.dto.request.notification.UpdateNotificationSettingsRequest;
import com.qorpy.api.dto.response.NotificationBellDto;
import com.qorpy.api.dto.response.NotificationDto;
import com.qorpy.api.dto.response.NotificationSettingsDto;
import com.qorpy.api.enums.AlertSeverity;
import com.qorpy.api.security.AdminUserDetails;
import com.qorpy.api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * US-024 — Bell icon data: unread count + 20 most recent notifications.
     */
//    @GetMapping("/recent-notification")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPLIANCE_OFFICER', 'VIEWER')")
//    public ResponseEntity<NotificationBellDto> getBellData(
//            @AuthenticationPrincipal AdminUserDetails currentUser) {
//        return ResponseEntity.ok(
//                notificationService.getBellData(currentUser.getAdminUser()));
//    }

    /**
     * US-024 — Mark a single notification as read.
     */
    @PatchMapping("/{notificationId}/mark-as-read")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPLIANCE_OFFICER', 'VIEWER')")
    public ResponseEntity<NotificationDto> markAsRead(
            @PathVariable UUID notificationId,
            @AuthenticationPrincipal AdminUserDetails currentUser) {
        return ResponseEntity.ok(
                notificationService.markAsRead(notificationId, currentUser.getAdminUser()));
    }

    /**
     * US-024 — Mark all notifications as read.
     */
    @PatchMapping("/mark-all-as-read")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPLIANCE_OFFICER', 'VIEWER')")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal AdminUserDetails currentUser) {
        notificationService.markAllAsRead(currentUser.getAdminUser());
        return ResponseEntity.noContent().build();
    }

    /**
     * US-025 — Full notification history, filterable by severity and date range.
     */
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPLIANCE_OFFICER', 'VIEWER')")
    public ResponseEntity<Page<NotificationDto>> getHistory(
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @PageableDefault(size = 25, sort = "triggeredAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @AuthenticationPrincipal AdminUserDetails currentUser) {

        return ResponseEntity.ok(
                notificationService.getHistory(severity, from, to,
                        currentUser.getAdminUser(), pageable));
    }

    /**
     * US-026 — Get current admin's email notification settings.
     */
    @GetMapping("/settings")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPLIANCE_OFFICER', 'VIEWER')")
    public ResponseEntity<NotificationSettingsDto> getSettings(
            @AuthenticationPrincipal AdminUserDetails currentUser) {
        return ResponseEntity.ok(
                notificationService.getSettings(currentUser.getAdminUser()));
    }

    /**
     * US-026 — Update email notification toggle.
     */
    @PutMapping("/settings")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPLIANCE_OFFICER', 'VIEWER')")
    public ResponseEntity<NotificationSettingsDto> updateSettings(
            @RequestBody UpdateNotificationSettingsRequest request,
            @AuthenticationPrincipal AdminUserDetails currentUser) {
        return ResponseEntity.ok(
                notificationService.updateSettings(request, currentUser.getAdminUser()));
    }
}