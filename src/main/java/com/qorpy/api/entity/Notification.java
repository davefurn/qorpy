package com.qorpy.api.entity;

import com.qorpy.api.enums.AlertSeverity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "notifications",
    indexes = {
        @Index(name = "idx_notifications_triggered_at",  columnList = "triggered_at DESC"),
        @Index(name = "idx_notifications_alert_rule_id", columnList = "alert_rule_id"),
        @Index(name = "idx_notifications_severity",      columnList = "severity"),
        @Index(name = "idx_notifications_entity",
               columnList = "entity_type, entity_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "alert_rule_id",
        referencedColumnName = "id",
        nullable = true,
        foreignKey = @ForeignKey(name = "fk_notifications_alert_rule_id")
    )
    private AlertRule alertRule;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "triggered_at", nullable = false, updatable = false)
    private OffsetDateTime triggeredAt;

    @PrePersist
    protected void onCreate() {
        triggeredAt = OffsetDateTime.now();
    }
}