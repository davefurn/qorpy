package com.qorpy.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "notification_reads",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_notif_read_per_admin",
            columnNames = {"notification_id", "admin_id"}
        )
    },
    indexes = {
        @Index(name = "idx_notif_reads_notification_id", columnList = "notification_id"),
        @Index(name = "idx_notif_reads_admin_id",        columnList = "admin_id"),
        @Index(name = "idx_notif_reads_is_read",         columnList = "is_read")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRead {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "notification_id",
        referencedColumnName = "id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_notif_reads_notification_id")
    )
    private Notification notification;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "admin_id",
        referencedColumnName = "id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_notif_reads_admin_id")
    )
    private AdminUser admin;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    @PrePersist
    protected void onCreate() {
        isRead = false;
    }
}