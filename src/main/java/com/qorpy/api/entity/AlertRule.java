package com.qorpy.api.entity;

import com.qorpy.api.enums.AlertSeverity;
import com.qorpy.api.enums.AlertTriggerType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "alert_rules",
    indexes = {
        @Index(name = "idx_alert_rules_created_by",   columnList = "created_by"),
        @Index(name = "idx_alert_rules_is_active",    columnList = "is_active"),
        @Index(name = "idx_alert_rules_trigger_type", columnList = "trigger_type"),
        @Index(name = "idx_alert_rules_severity",     columnList = "severity")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // FK → admin_users.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "created_by",
        referencedColumnName = "id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_alert_rules_created_by_admin")
    )
    private AdminUser createdBy;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "trigger_type", nullable = false, length = 50)
    private AlertTriggerType triggerType;

    @Column(name = "threshold_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal thresholdValue;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
        if (severity == null) severity = AlertSeverity.WARNING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}