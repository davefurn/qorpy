package com.qorpy.api.service;

import com.qorpy.api.entity.AdminUser;
import com.qorpy.api.entity.AuditLog;
import com.qorpy.api.enums.AuditActionType;
import com.qorpy.api.respository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void logAction(AdminUser admin, String actionTypeStr, String targetType, UUID targetId) {
        AuditLog log = AuditLog.builder()
                .admin(admin)

                .actionType(AuditActionType.valueOf(actionTypeStr))
                .targetEntityType(targetType)
                .targetEntityId(targetId)
                .build();

        auditLogRepository.save(log);
    }
}