package com.qorpy.api.dto.response;

import com.qorpy.api.enums.InvoiceEventType;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class InvoiceHistoryDto {
    private UUID id;
    private InvoiceEventType eventType;
    private Map<String, Object> details;
    private OffsetDateTime createdAt;
}