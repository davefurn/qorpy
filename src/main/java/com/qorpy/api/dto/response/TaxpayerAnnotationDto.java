package com.qorpy.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class TaxpayerAnnotationDto {
    private UUID id;
    private String note;
    private boolean isFlag;
    private UUID adminId;
    private String adminName;
    private OffsetDateTime createdAt;
}