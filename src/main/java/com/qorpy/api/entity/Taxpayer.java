package com.qorpy.api.entity;

import com.qorpy.api.enums.AccountStatus;
import com.qorpy.api.enums.KycStatus;
import com.qorpy.api.enums.SubscriptionTier;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "taxpayers",
    indexes = {
        @Index(name = "idx_taxpayers_tin",            columnList = "tin"),
        @Index(name = "idx_taxpayers_account_status", columnList = "account_status"),
        @Index(name = "idx_taxpayers_kyc_status",     columnList = "kyc_status"),
        @Index(name = "idx_taxpayers_suspended_by",   columnList = "suspended_by")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Taxpayer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String tin;

    @Column
    private String email;

    @Column(length = 50)
    private String phone;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "kyc_status", nullable = false, length = 20)
    private KycStatus kycStatus;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "subscription_tier", nullable = false, length = 20)
    private SubscriptionTier subscriptionTier;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "account_status", nullable = false, length = 20)
    private AccountStatus accountStatus;

    @Column(name = "suspension_reason", columnDefinition = "TEXT")
    private String suspensionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "suspended_by",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "fk_taxpayers_suspended_by_admin")
    )
    private AdminUser suspendedBy;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private OffsetDateTime registeredAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        registeredAt = OffsetDateTime.now();
        updatedAt    = OffsetDateTime.now();
        if (kycStatus == null)        kycStatus        = KycStatus.PENDING;
        if (subscriptionTier == null) subscriptionTier = SubscriptionTier.FREE;
        if (accountStatus == null)    accountStatus    = AccountStatus.ACTIVE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}