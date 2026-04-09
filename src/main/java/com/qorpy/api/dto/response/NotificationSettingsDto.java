package com.qorpy.api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class NotificationSettingsDto {
    private UUID adminId;
    private boolean emailCriticalAlerts;
}
