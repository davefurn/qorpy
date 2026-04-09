package com.qorpy.api.controller;

import com.qorpy.api.dto.response.AuditLogDto;
import com.qorpy.api.enums.AuditActionType;
import com.qorpy.api.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * US-020 / US-021 — View and filter the audit log.
     * Default: 50 per page, descending by createdAt.
     *
     * Query params (all optional):
     *   from        — OffsetDateTime ISO-8601
     *   to          — OffsetDateTime ISO-8601
     *   adminId     — filter by actor UUID
     *   actionTypes — comma-separated list of AuditActionType values
     */
    @GetMapping
    public ResponseEntity<Page<AuditLogDto>> getAuditLog(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,

            @RequestParam(required = false) UUID adminId,

            @RequestParam(required = false) List<AuditActionType> actionTypes,

            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(
                auditLogService.getAuditLog(from, to, adminId, actionTypes, pageable));
    }
}