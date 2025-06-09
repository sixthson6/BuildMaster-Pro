package com.tech.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tech.model.AuditLog;
import com.tech.repository.AuditLogRepository;
import com.tech.dto.ProjectDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public void logProjectAction(String action, ProjectDTO projectDTO) {
        try {
            AuditLog log = AuditLog.builder()
                    .actorType("Project")
                    .actionType(action)
                    .actor(projectDTO.getName())
                    .username("admin")
                    .timestamp(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC))
                    .dataSnapshot(objectMapper.writeValueAsString(projectDTO))
                    .build();
            auditLogRepository.save(log);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize project data for audit log", e);
        }
    }
    public void logTaskAction(String action, Object taskDTO) {
        try {
            AuditLog log = AuditLog.builder()
                    .actorType("Task")
                    .actionType(action)
                    .actor(taskDTO.toString())
                    .username("admin")
                    .timestamp(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC))
                    .dataSnapshot(objectMapper.writeValueAsString(taskDTO))
                    .build();
            auditLogRepository.save(log);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize task data for audit log", e);
        }
    }
    public void logDeveloperAction(String action, Object developerDTO) {
        try {
            AuditLog log = AuditLog.builder()
                    .actorType("Developer")
                    .actionType(action)
                    .actor(developerDTO.toString())
                    .username("admin")
                    .timestamp(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC))
                    .dataSnapshot(objectMapper.writeValueAsString(developerDTO))
                    .build();
            auditLogRepository.save(log);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize developer data for audit log", e);
        }
    }
}
