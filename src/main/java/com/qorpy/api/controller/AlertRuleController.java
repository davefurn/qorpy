package com.qorpy.api.controller;

import com.qorpy.api.dto.request.alert.CreateAlertRuleRequest;
import com.qorpy.api.dto.request.alert.UpdateAlertRuleRequest;
import com.qorpy.api.dto.response.AlertRuleDto;
import com.qorpy.api.security.AdminUserDetails;
import com.qorpy.api.service.AlertRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alert-rules")
@RequiredArgsConstructor
public class AlertRuleController {

    private final AlertRuleService alertRuleService;

    /** US-023 — List all alert rules. Accessible by all roles. */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPLIANCE_OFFICER', 'VIEWER')")
    public ResponseEntity<List<AlertRuleDto>> getAllRules() {
        return ResponseEntity.ok(alertRuleService.getAllRules());
    }

    /** US-023 — Create a new alert rule. Super Admin only. */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AlertRuleDto> createRule(
            @Valid @RequestBody CreateAlertRuleRequest request,
            @AuthenticationPrincipal AdminUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(alertRuleService.createRule(request, currentUser.getAdminUser()));
    }

    /** US-023 — Edit an alert rule. Super Admin only. */
    @PutMapping("/{ruleId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AlertRuleDto> updateRule(
            @PathVariable UUID ruleId,
            @Valid @RequestBody UpdateAlertRuleRequest request,
            @AuthenticationPrincipal AdminUserDetails currentUser) {
        return ResponseEntity.ok(
                alertRuleService.updateRule(ruleId, request, currentUser.getAdminUser()));
    }

    /** US-023 — Deactivate an alert rule. Super Admin only. */
    @PatchMapping("/{ruleId}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AlertRuleDto> deactivateRule(
            @PathVariable UUID ruleId,
            @AuthenticationPrincipal AdminUserDetails currentUser) {
        return ResponseEntity.ok(
                alertRuleService.deactivateRule(ruleId, currentUser.getAdminUser()));
    }

    /** Re-activate a deactivated rule. Super Admin only. */
    @PatchMapping("/{ruleId}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AlertRuleDto> activateRule(
            @PathVariable UUID ruleId,
            @AuthenticationPrincipal AdminUserDetails currentUser) {
        return ResponseEntity.ok(
                alertRuleService.activateRule(ruleId, currentUser.getAdminUser()));
    }
}