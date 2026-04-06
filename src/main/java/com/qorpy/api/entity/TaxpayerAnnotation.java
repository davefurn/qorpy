package com.qorpy.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "taxpayer_annotations",
    indexes = {
        @Index(name = "idx_annotations_taxpayer_id", columnList = "taxpayer_id"),
        @Index(name = "idx_annotations_admin_id",    columnList = "admin_id"),
        @Index(name = "idx_annotations_created_at",  columnList = "created_at DESC"),
        @Index(name = "idx_annotations_is_flag",     columnList = "is_flag")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxpayerAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "taxpayer_id",
        referencedColumnName = "id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_annotations_taxpayer_id")
    )
    private Taxpayer taxpayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "admin_id",
        referencedColumnName = "id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_annotations_admin_id")
    )
    private AdminUser admin;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String note;

    @Column(name = "is_flag", nullable = false)
    private boolean isFlag = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}