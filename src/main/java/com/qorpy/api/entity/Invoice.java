package com.qorpy.api.entity;

import com.qorpy.api.enums.ComplianceFlag;
import com.qorpy.api.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
    name = "invoices",
    indexes = {
        @Index(name = "idx_invoices_taxpayer_id",   columnList = "taxpayer_id"),
        @Index(name = "idx_invoices_submitted_at",  columnList = "submitted_at DESC"),
        @Index(name = "idx_invoices_status",        columnList = "submission_status"),
        @Index(name = "idx_invoices_compliance",    columnList = "compliance_flag"),
        @Index(name = "idx_invoices_status_compliance",
               columnList = "submission_status, compliance_flag")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "taxpayer_id",
        referencedColumnName = "id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_invoices_taxpayer_id")
    )
    private Taxpayer taxpayer;

    @Column(name = "invoice_number", nullable = false, length = 100)
    private String invoiceNumber;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "submission_status", nullable = false, length = 20)
    private InvoiceStatus submissionStatus;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "compliance_flag", length = 20)
    private ComplianceFlag complianceFlag;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "nrs_validation_result", columnDefinition = "jsonb")
    private Map<String, Object> nrsValidationResult;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private OffsetDateTime submittedAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = OffsetDateTime.now();
        updatedAt   = OffsetDateTime.now();
        if (submissionStatus == null) submissionStatus = InvoiceStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}