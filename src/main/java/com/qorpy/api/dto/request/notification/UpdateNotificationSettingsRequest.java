package com.qorpy.api.dto.request.notification;

import lombok.Data;

@Data
public class UpdateNotificationSettingsRequest {
    private boolean emailCriticalAlerts;
}