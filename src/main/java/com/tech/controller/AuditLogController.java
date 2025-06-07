package com.tech.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tech.model.AuditLog;
import com.tech.repository.AuditLogRepository;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

//    @GetMapping("/entity/{type}")
//    public List<AuditLog> getLogsByEntityType(@PathVariable String type) {
//        return auditLogRepository.findByEntityType(type);
//    }

    @GetMapping("/actor/{actor}")
    public List<AuditLog> getLogsByActor(@PathVariable String actor) {
        return auditLogRepository.findByActor(actor);
    }
}
