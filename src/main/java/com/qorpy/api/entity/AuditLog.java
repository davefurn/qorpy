package com.qorpy.api.entity;

import com.qorpy.api.enums.AuditActionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_audit_logs_admin_id",     columnList = "admin_id"),
        @Index(name = "idx_audit_logs_created_at",   columnList = "created_at DESC"),
        @Index(name = "idx_audit_logs_action_type",  columnList = "action_type"),
        @Index(name = "idx_audit_logs_target_entity",
               columnList = "target_entity_type, target_entity_id")
    }
)
@Immutable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "admin_id",
        referencedColumnName = "id",
        nullable = true,
        foreignKey = @ForeignKey(name = "fk_audit_logs_admin_id")
    )
    private AdminUser admin;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "action_type", nullable = false, length = 100)
    private AuditActionType actionType;

    @Column(name = "target_entity_type", length = 100)
    private String targetEntityType;

    @Column(name = "target_entity_id")
    private UUID targetEntityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before_value", columnDefinition = "jsonb")
    private Map<String, Object> beforeValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after_value", columnDefinition = "jsonb")
    private Map<String, Object> afterValue;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}