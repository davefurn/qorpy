package com.qorpy.api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class TopNonCompliantTaxpayerDto {
    private UUID taxpayerId;
    private String taxpayerName;
    private String tin;
    private long totalInvoices;
    private long nonCompliantCount;
    private double complianceRate;
}