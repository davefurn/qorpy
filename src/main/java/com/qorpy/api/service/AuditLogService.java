package com.qorpy.api.service;

import com.qorpy.api.dto.response.AuditLogDto;
import com.qorpy.api.entity.AdminUser;
import com.qorpy.api.entity.AuditLog;
import com.qorpy.api.enums.AuditActionType;
import com.qorpy.api.respository.AuditLogRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /** Log a simple action with no before/after values */
    public void logAction(AdminUser admin, String actionTypeStr,
                          String targetType, UUID targetId) {
        AuditLog log = AuditLog.builder()
                .admin(admin)
                .actionType(AuditActionType.valueOf(actionTypeStr))
                .targetEntityType(targetType)
                .targetEntityId(targetId)
                .build();
        auditLogRepository.save(log);
    }

    /** Log a state-changing action with before/after snapshots */
    public void logActionWithDetails(AdminUser admin, String actionTypeStr,
                                     String targetType, UUID targetId,
                                     Map<String, Object> beforeValue,
                                     Map<String, Object> afterValue) {
        AuditLog log = AuditLog.builder()
                .admin(admin)
                .actionType(AuditActionType.valueOf(actionTypeStr))
                .targetEntityType(targetType)
                .targetEntityId(targetId)
                .beforeValue(beforeValue)
                .afterValue(afterValue)
                .build();
        auditLogRepository.save(log);
    }

    /**
     * US-020 / US-021 — Paginated, filterable audit log (Super Admin only).
     * All filter params are optional.
     */
    @Transactional
    public Page<AuditLogDto> getAuditLog(
            OffsetDateTime from,
            OffsetDateTime to,
            UUID adminId,
            List<AuditActionType> actionTypes,
            Pageable pageable) {

        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            if (adminId != null) {
                predicates.add(cb.equal(root.get("admin").get("id"), adminId));
            }
            if (actionTypes != null && !actionTypes.isEmpty()) {
                predicates.add(root.get("actionType").in(actionTypes));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return auditLogRepository.findAll(spec, pageable).map(this::toDto);
    }

    private AuditLogDto toDto(AuditLog log) {
        return AuditLogDto.builder()
                .id(log.getId())
                .createdAt(log.getCreatedAt())
                .adminId(log.getAdmin() != null ? log.getAdmin().getId() : null)
                .adminName(log.getAdmin() != null ? log.getAdmin().getFullName() : "System")
                .actionType(log.getActionType())
                .targetEntityType(log.getTargetEntityType())
                .targetEntityId(log.getTargetEntityId())
                .beforeValue(log.getBeforeValue())
                .afterValue(log.getAfterValue())
                .build();
    }
}