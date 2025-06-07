package com.tech.auditlog;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Validated
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAllLogs(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLog> logs = auditService.getAllLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/entity/{entityType}")
    public ResponseEntity<Page<AuditLog>> getLogsByEntityType(
            @PathVariable @NotBlank String entityType,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLog> logs = auditService.getLogsByEntityType(entityType, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/actor/{actorName}")
    public ResponseEntity<Page<AuditLog>> getLogsByActorName(
            @PathVariable @NotBlank String actorName,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLog> logs = auditService.getLogsByActorName(actorName, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<Page<AuditLog>> getLogsByEntityAndId(
            @PathVariable @NotBlank String entityType,
            @PathVariable @NotBlank String entityId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLog> logs = auditService.getLogsByEntityAndId(entityType, entityId, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<Page<AuditLog>> getLogsByAction(
            @PathVariable @NotBlank String action,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLog> logs = auditService.getLogsByAction(action, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/date-range")
    public ResponseEntity<Page<AuditLog>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @PageableDefault(size = 20) Pageable pageable) {

        if (startTime.isAfter(endTime)) {
            return ResponseEntity.badRequest().build();
        }

        Page<AuditLog> logs = auditService.getLogsByDateRange(startTime, endTime, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AuditLog>> searchLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String actorName,
            @RequestParam(required = false) String action,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLog> logs = auditService.getLogsByMultipleCriteria(entityType, actorName, action, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<AuditLog>> getRecentLogs() {
        List<AuditLog> logs = auditService.getRecentLogs();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getLogStatistics() {
        Map<String, Long> stats = auditService.getLogStatistics();
        return ResponseEntity.ok(stats);
    }
}