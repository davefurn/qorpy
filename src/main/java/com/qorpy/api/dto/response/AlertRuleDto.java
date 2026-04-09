package com.qorpy.api.dto.response;

import com.qorpy.api.enums.AlertSeverity;
import com.qorpy.api.enums.AlertTriggerType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class AlertRuleDto {
    private UUID id;
    private String name;
    private AlertTriggerType triggerType;
    private BigDecimal thresholdValue;
    private AlertSeverity severity;
    private boolean isActive;
    private UUID createdById;
    private String createdByName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}