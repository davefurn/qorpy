package com.qorpy.api.controller;

import com.qorpy.api.dto.request.taxpayer.AnnotateTaxpayerRequest;
import com.qorpy.api.dto.request.taxpayer.SuspendTaxpayerRequest;
import com.qorpy.api.dto.response.TaxpayerAnnotationDto;
import com.qorpy.api.dto.response.TaxpayerDto;
import com.qorpy.api.security.AdminUserDetails;
import com.qorpy.api.service.TaxpayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/taxpayers")
@RequiredArgsConstructor
public class TaxpayerController {

    private final TaxpayerService taxpayerService;

    /**
     * US-009 — View Taxpayer List
     * Accessible by: SUPER_ADMIN, COMPLIANCE_OFFICER, VIEWER
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPLIANCE_OFFICER', 'VIEWER')")
    public ResponseEntity<Page<TaxpayerDto>> listTaxpayers(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(taxpayerService.listTaxpayers(search, pageable));
    }

    /**
     * US-010 — View Taxpayer Profile
     * Accessible by: SUPER_ADMIN, COMPLIANCE_OFFICER, VIEWER
     */
    @GetMapping("/{taxpayerId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPLIANCE_OFFICER', 'VIEWER')")
    public ResponseEntity<TaxpayerDto> getTaxpayerProfile(@PathVariable UUID taxpayerId) {
        return ResponseEntity.ok(taxpayerService.getTaxpayerProfile(taxpayerId));
    }

    /**
     * US-011 — Get annotations for a taxpayer
     * Accessible by: SUPER_ADMIN, COMPLIANCE_OFFICER
     */
    @GetMapping("/{taxpayerId}/annotations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<List<TaxpayerAnnotationDto>> getAnnotations(@PathVariable UUID taxpayerId) {
        return ResponseEntity.ok(taxpayerService.getAnnotations(taxpayerId));
    }

    /**
     * US-011 — Add flag or annotation to a taxpayer
     * Accessible by: SUPER_ADMIN, COMPLIANCE_OFFICER
     */
    @PostMapping("/{taxpayerId}/annotate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<TaxpayerAnnotationDto> annotate(
            @PathVariable UUID taxpayerId,
            @Valid @RequestBody AnnotateTaxpayerRequest request,
            @AuthenticationPrincipal AdminUserDetails currentUser) {
        return ResponseEntity.ok(
                taxpayerService.annotate(taxpayerId, request, currentUser.getAdminUser()));
    }

    /**
     * US-012 — Suspend a taxpayer account
     * Accessible by: SUPER_ADMIN only
     */
    @PatchMapping("/{taxpayerId}/suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<TaxpayerDto> suspend(
            @PathVariable UUID taxpayerId,
            @Valid @RequestBody SuspendTaxpayerRequest request,
            @AuthenticationPrincipal AdminUserDetails currentUser) {
        return ResponseEntity.ok(
                taxpayerService.suspend(taxpayerId, request, currentUser.getAdminUser()));
    }

    /**
     * US-012 — Reinstate a suspended taxpayer account
     * Accessible by: SUPER_ADMIN only
     */
    @PatchMapping("/{taxpayerId}/reinstate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<TaxpayerDto> reinstate(
            @PathVariable UUID taxpayerId,
            @AuthenticationPrincipal AdminUserDetails currentUser) {
        return ResponseEntity.ok(
                taxpayerService.reinstate(taxpayerId, currentUser.getAdminUser()));
    }
}