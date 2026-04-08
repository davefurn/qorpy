package com.qorpy.api.dto.response;

import com.qorpy.api.enums.AccountStatus;
import com.qorpy.api.enums.KycStatus;
import com.qorpy.api.enums.SubscriptionTier;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class TaxpayerDto {
    private UUID id;
    private String name;
    private String tin;
    private String email;
    private String phone;
    private KycStatus kycStatus;
    private SubscriptionTier subscriptionTier;
    private AccountStatus accountStatus;
    private String suspensionReason;
    private OffsetDateTime registeredAt;
    private OffsetDateTime updatedAt;
    private boolean hasPendingFlag;
    // Populated in profile view only
    private Long totalInvoices;
    private Long compliantCount;
    private Long nonCompliantCount;
    private Double complianceRate;
}