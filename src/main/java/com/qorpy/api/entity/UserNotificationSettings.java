package com.qorpy.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "user_notification_settings",
    indexes = {
        @Index(name = "idx_user_notif_settings_admin_id", columnList = "admin_id",
               unique = true)
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "admin_id",
        referencedColumnName = "id",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_user_notif_settings_admin_id")
    )
    private AdminUser admin;

    @Column(name = "email_critical_alerts", nullable = false)
    private boolean emailCriticalAlerts = false;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = OffsetDateTime.now();
        emailCriticalAlerts = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}