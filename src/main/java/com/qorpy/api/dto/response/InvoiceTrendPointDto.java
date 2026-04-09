package com.qorpy.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceTrendPointDto {
    private String date;
    private long compliant;
    private long nonCompliant;
    private long pending;
    private long total;
}