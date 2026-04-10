package com.qorpy.api.controller;

import com.qorpy.api.dto.response.DashboardMetricsDto;
import com.qorpy.api.security.AdminUserDetails;
import com.qorpy.api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * US-017 / US-018 / US-019 — Full dashboard payload.
     * period: 7, 30, or 90 days (default 30).
     * Accessible by all roles.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPLIANCE_OFFICER', 'VIEWER')")
    public ResponseEntity<DashboardMetricsDto> getMetrics(
            @RequestParam(defaultValue = "30") int period,
            @AuthenticationPrincipal AdminUserDetails userDetails) {

        // Constrain to allowed values
        int days = (period == 7 || period == 90) ? period : 30;
        return ResponseEntity.ok(dashboardService.getMetrics(days, userDetails.getAdminUser().getId()));
    }
}