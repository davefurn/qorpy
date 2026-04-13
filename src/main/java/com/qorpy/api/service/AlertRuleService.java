//package com.qorpy.api.service;
//
//import com.qorpy.api.dto.request.alert.CreateAlertRuleRequest;
//import com.qorpy.api.dto.request.alert.UpdateAlertRuleRequest;
//import com.qorpy.api.dto.response.AlertRuleDto;
//import com.qorpy.api.entity.AdminUser;
//import com.qorpy.api.entity.AlertRule;
//import com.qorpy.api.exception.ResourceNotFoundException;
//import com.qorpy.api.respository.AlertRuleRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
///**
// * EP-07 — US-023 Configure Alert Rules
// */
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class AlertRuleService {
//
//    private final AlertRuleRepository alertRuleRepository;
//    private final AuditLogService auditLogService;
//
//    /** List all alert rules (active and inactive) */
//   @Transactional
//    public List<AlertRuleDto> getAllRules() {
//        return alertRuleRepository.findAllByOrderByCreatedAtDesc()
//                .stream().map(this::toDto).collect(Collectors.toList());
//    }
//
//    /** List only active rules */
//    @Transactional
//    public List<AlertRuleDto> getActiveRules() {
//        return alertRuleRepository.findByIsActiveTrue()
//                .stream().map(this::toDto).collect(Collectors.toList());
//    }
//
//    /** US-023 — Create a new alert rule */
//    @Transactional
//    public AlertRuleDto createRule(CreateAlertRuleRequest request, AdminUser admin) {
//        AlertRule rule = AlertRule.builder()
//                .createdBy(admin)
//                .name(request.getName())
//                .triggerType(request.getTriggerType())
//                .thresholdValue(request.getThresholdValue())
//                .severity(request.getSeverity())
//                .isActive(true)
//                .build();
//
//        AlertRule saved = alertRuleRepository.save(rule);
//
//        auditLogService.logActionWithDetails(admin, "ALERT_CONFIGURED", "ALERT_RULE",
//                saved.getId(), null,
//                Map.of("name", saved.getName(),
//                        "triggerType", saved.getTriggerType().name(),
//                        "threshold", saved.getThresholdValue(),
//                        "severity", saved.getSeverity().name()));
//
//        return toDto(saved);
//    }
//
//    /** US-023 — Edit an existing alert rule */
//    @Transactional
//    public AlertRuleDto updateRule(UUID ruleId, UpdateAlertRuleRequest request, AdminUser admin) {
//        AlertRule rule = findById(ruleId);
//
//        Map<String, Object> before = new java.util.HashMap<>();
//        before.put("name", rule.getName());
//        before.put("triggerType", rule.getTriggerType() != null ? rule.getTriggerType().name() : null);
//        before.put("threshold", rule.getThresholdValue());
//        before.put("severity", rule.getSeverity() != null ? rule.getSeverity().name() : null);
//
//        rule.setName(request.getName());
//        rule.setTriggerType(request.getTriggerType());
//        rule.setThresholdValue(request.getThresholdValue());
//        rule.setSeverity(request.getSeverity());
//
//        AlertRule saved = alertRuleRepository.save(rule);
//
//        auditLogService.logActionWithDetails(admin, "ALERT_EDITED", "ALERT_RULE",
//                saved.getId(), before,
//                Map.of("name", saved.getName(),
//                        "triggerType", saved.getTriggerType().name(),
//                        "threshold", saved.getThresholdValue(),
//                        "severity", saved.getSeverity().name()));
//
//        return toDto(saved);
//    }
//
//    /** US-023 — Deactivate an alert rule (soft delete) */
//    @Transactional
//    public AlertRuleDto deactivateRule(UUID ruleId, AdminUser admin) {
//        AlertRule rule = findById(ruleId);
//
//        Map<String, Object> before = Map.of("isActive", rule.isActive());
//        rule.setActive(false);
//        AlertRule saved = alertRuleRepository.save(rule);
//
//        auditLogService.logActionWithDetails(admin, "ALERT_DEACTIVATED", "ALERT_RULE",
//                saved.getId(), before, Map.of("isActive", false));
//
//        return toDto(saved);
//    }
//
//    /** Re-activate a previously deactivated rule */
//    @Transactional
//    public AlertRuleDto activateRule(UUID ruleId, AdminUser admin) {
//        AlertRule rule = findById(ruleId);
//        Map<String, Object> before = Map.of("isActive", rule.isActive());
//        rule.setActive(true);
//        AlertRule saved = alertRuleRepository.save(rule);
//
//        auditLogService.logActionWithDetails(admin, "ALERT_CONFIGURED", "ALERT_RULE",
//                saved.getId(), before, Map.of("isActive", true));
//
//        return toDto(saved);
//    }
//
//    // ── helpers ──
//
//    private AlertRule findById(UUID id) {
//        return alertRuleRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Alert rule not found: " + id));
//    }
//
//    private AlertRuleDto toDto(AlertRule r) {
//        return AlertRuleDto.builder()
//                .id(r.getId())
//                .name(r.getName())
//                .triggerType(r.getTriggerType())
//                .thresholdValue(r.getThresholdValue())
//                .severity(r.getSeverity())
//                .isActive(r.isActive())
//                .createdById(r.getCreatedBy().getId())
//                .createdByName(r.getCreatedBy().getFullName())
//                .createdAt(r.getCreatedAt())
//                .updatedAt(r.getUpdatedAt())
//                .build();
//    }
//}


package com.qorpy.api.service;

import com.qorpy.api.dto.request.alert.CreateAlertRuleRequest;
import com.qorpy.api.dto.request.alert.UpdateAlertRuleRequest;
import com.qorpy.api.dto.response.AlertRuleDto;
import com.qorpy.api.entity.AdminUser;
import com.qorpy.api.entity.AlertRule;
import com.qorpy.api.exception.ResourceNotFoundException;
import com.qorpy.api.respository.AlertRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * EP-07 — US-023 Configure Alert Rules
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertRuleService {

    private final AlertRuleRepository alertRuleRepository;
    private final AuditLogService auditLogService;

    /** List all alert rules (active and inactive) */
    public List<AlertRuleDto> getAllRules() {
        return alertRuleRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    /** List only active rules */
    public List<AlertRuleDto> getActiveRules() {
        return alertRuleRepository.findByIsActiveTrue()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    /** US-023 — Create a new alert rule */
    @Transactional
    public AlertRuleDto createRule(CreateAlertRuleRequest request, AdminUser admin) {
        AlertRule rule = AlertRule.builder()
                .createdBy(admin)
                .name(request.getName())
                .triggerType(request.getTriggerType())
                .thresholdValue(request.getThresholdValue())
                .severity(request.getSeverity())
                .isActive(true)
                .build();

        AlertRule saved = alertRuleRepository.save(rule);

        auditLogService.logActionWithDetails(admin, "ALERT_CONFIGURED", "ALERT_RULE",
                saved.getId(), null, buildSafeMap(saved));

        return toDto(saved);
    }

    /** US-023 — Edit an existing alert rule */
    @Transactional
    public AlertRuleDto updateRule(UUID ruleId, UpdateAlertRuleRequest request, AdminUser admin) {
        AlertRule rule = findById(ruleId);

        // Safely capture the before state
        Map<String, Object> before = buildSafeMap(rule);

        rule.setName(request.getName());
        rule.setTriggerType(request.getTriggerType());
        rule.setThresholdValue(request.getThresholdValue());
        rule.setSeverity(request.getSeverity());

        AlertRule saved = alertRuleRepository.save(rule);

        // Safely capture the after state
        auditLogService.logActionWithDetails(admin, "ALERT_EDITED", "ALERT_RULE",
                saved.getId(), before, buildSafeMap(saved));

        return toDto(saved);
    }

    /** US-023 — Deactivate an alert rule (soft delete) */
    @Transactional
    public AlertRuleDto deactivateRule(UUID ruleId, AdminUser admin) {
        AlertRule rule = findById(ruleId);

        Map<String, Object> before = new HashMap<>();
        before.put("isActive", rule.isActive());

        rule.setActive(false);
        AlertRule saved = alertRuleRepository.save(rule);

        Map<String, Object> after = new HashMap<>();
        after.put("isActive", false);

        auditLogService.logActionWithDetails(admin, "ALERT_DEACTIVATED", "ALERT_RULE",
                saved.getId(), before, after);

        return toDto(saved);
    }

    /** Re-activate a previously deactivated rule */
    @Transactional
    public AlertRuleDto activateRule(UUID ruleId, AdminUser admin) {
        AlertRule rule = findById(ruleId);

        Map<String, Object> before = new HashMap<>();
        before.put("isActive", rule.isActive());

        rule.setActive(true);
        AlertRule saved = alertRuleRepository.save(rule);

        Map<String, Object> after = new HashMap<>();
        after.put("isActive", true);

        auditLogService.logActionWithDetails(admin, "ALERT_CONFIGURED", "ALERT_RULE",
                saved.getId(), before, after);

        return toDto(saved);
    }

    // ── helpers ──

    private AlertRule findById(UUID id) {
        return alertRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert rule not found: " + id));
    }

    // Safe Map Builder to prevent NullPointerExceptions
    private Map<String, Object> buildSafeMap(AlertRule rule) {
        Map<String, Object> safeMap = new HashMap<>();
        safeMap.put("name", rule.getName());
        safeMap.put("triggerType", rule.getTriggerType() != null ? rule.getTriggerType().name() : null);
        safeMap.put("threshold", rule.getThresholdValue());
        safeMap.put("severity", rule.getSeverity() != null ? rule.getSeverity().name() : null);
        return safeMap;
    }

    private AlertRuleDto toDto(AlertRule r) {
        return AlertRuleDto.builder()
                .id(r.getId())
                .name(r.getName())
                .triggerType(r.getTriggerType())
                .thresholdValue(r.getThresholdValue())
                .severity(r.getSeverity())
                .isActive(r.isActive())
                .createdById(r.getCreatedBy().getId())
                .createdByName(r.getCreatedBy().getFullName())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}