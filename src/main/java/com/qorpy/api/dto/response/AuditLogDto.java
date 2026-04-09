package com.qorpy.api.dto.response;

import com.qorpy.api.enums.AuditActionType;
import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class AuditLogDto {
    private UUID id;
    private OffsetDateTime createdAt;
    private UUID adminId;
    private String adminName;
    private AuditActionType actionType;
    private String targetEntityType;
    private UUID targetEntityId;
    private Map<String, Object> beforeValue;
    private Map<String, Object> afterValue;
}