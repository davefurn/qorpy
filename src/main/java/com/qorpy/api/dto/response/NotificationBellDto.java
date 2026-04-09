package com.qorpy.api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class NotificationBellDto {
    private long unreadCount;
    private List<NotificationDto> recent;
}