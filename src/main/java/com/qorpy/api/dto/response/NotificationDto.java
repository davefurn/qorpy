package com.qorpy.api.dto.response;

import com.qorpy.api.enums.AlertSeverity;
import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationDto {
    private UUID id;
    private String title;
    private String description;
    private AlertSeverity severity;
    private String entityType;
    private UUID entityId;
    private UUID alertRuleId;
    private String alertRuleName;
    private OffsetDateTime triggeredAt;
    private boolean isRead;
    private OffsetDateTime readAt;
}