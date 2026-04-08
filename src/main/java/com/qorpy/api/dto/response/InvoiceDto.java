package com.qorpy.api.dto.response;

import com.qorpy.api.enums.ComplianceFlag;
import com.qorpy.api.enums.InvoiceStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class InvoiceDto {
    private UUID id;
    private String invoiceNumber;
    private UUID taxpayerId;
    private String taxpayerName;
    private String taxpayerTin;
    private BigDecimal amount;
    private InvoiceStatus submissionStatus;
    private ComplianceFlag complianceFlag;
    private Map<String, Object> payload;
    private Map<String, Object> nrsValidationResult;
    private OffsetDateTime submittedAt;
    private OffsetDateTime updatedAt;
}