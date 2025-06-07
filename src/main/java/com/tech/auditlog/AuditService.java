package com.tech.auditlog;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final HttpServletRequest request; // For getting request details

    @Async("auditExecutor")
    public CompletableFuture<Void> logAction(String entityType, String entityId, String action,
                                             String actorName, Object currentData, Object previousData) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action.toUpperCase())
                    .actorName(actorName != null ? actorName : "system")
                    .timestamp(LocalDateTime.now())
                    .dataSnapshot(convertToMap(currentData))
                    .previousData(convertToMap(previousData))
                    .description(generateDescription(entityType, action))
                    .ipAddress(getClientIpAddress())
                    .userAgent(request.getHeader("User-Agent"))
                    .sessionId(request.getSession(false) != null ? request.getSession().getId() : null)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit log created for {} {} on {} by {}", action, entityType, entityId, actorName);
        } catch (Exception e) {
            log.error("Failed to create audit log for {} {} on {}: {}",
                    action, entityType, entityId, e.getMessage(), e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async("auditExecutor")
    public CompletableFuture<Void> logAction(String entityType, String entityId, String action, String actorName, Object currentData) {
        return logAction(entityType, entityId, action, actorName, currentData, null);
    }

    // Synchronous methods for querying logs
    public Page<AuditLog> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    public Page<AuditLog> getLogsByEntityType(String entityType, Pageable pageable) {
        return auditLogRepository.findByEntityType(entityType, pageable);
    }

    public Page<AuditLog> getLogsByActorName(String actorName, Pageable pageable) {
        return auditLogRepository.findByActorName(actorName, pageable);
    }

    public Page<AuditLog> getLogsByEntityAndId(String entityType, String entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
    }

    public Page<AuditLog> getLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action.toUpperCase(), pageable);
    }

    public Page<AuditLog> getLogsByDateRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(startTime, endTime, pageable);
    }

    public Page<AuditLog> getLogsByMultipleCriteria(String entityType, String actorName,
                                                    String action, Pageable pageable) {
        return auditLogRepository.findByMultipleCriteria(
                entityType,
                actorName,
                action != null ? action.toUpperCase() : null,
                pageable
        );
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc();
    }

    public Map<String, Long> getLogStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalLogs", auditLogRepository.count());
        stats.put("developerLogs", auditLogRepository.countByEntityType("Developer"));
        stats.put("projectLogs", auditLogRepository.countByEntityType("Project"));
        stats.put("taskLogs", auditLogRepository.countByEntityType("Task"));
        stats.put("createActions", auditLogRepository.countByAction("CREATE"));
        stats.put("updateActions", auditLogRepository.countByAction("UPDATE"));
        stats.put("deleteActions", auditLogRepository.countByAction("DELETE"));
        return stats;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMap(Object data) {
        if (data == null) {
            return null;
        }

        try {
            // Use Jackson ObjectMapper for better conversion
            return objectMapper.convertValue(data, Map.class);
        } catch (Exception e) {
            log.warn("Could not convert object to map using ObjectMapper, falling back to reflection: {}", e.getMessage());

            // Fallback to reflection-based conversion
            Map<String, Object> map = new HashMap<>();
            try {
                Class<?> clazz = data.getClass();
                java.lang.reflect.Field[] fields = clazz.getDeclaredFields();

                for (java.lang.reflect.Field field : fields) {
                    field.setAccessible(true);
                    Object value = field.get(data);

                    if (value != null) {
                        // Handle different types appropriately
                        if (isPrimitiveOrWrapper(value.getClass()) ||
                                value instanceof String ||
                                value instanceof java.time.LocalDate ||
                                value instanceof java.time.LocalDateTime) {
                            map.put(field.getName(), value);
                        } else if (value instanceof java.util.Collection) {
                            map.put(field.getName(), "Collection[" + ((java.util.Collection<?>) value).size() + "]");
                        } else if (value instanceof java.util.Map) {
                            map.put(field.getName(), "Map[" + ((java.util.Map<?, ?>) value).size() + "]");
                        } else {
                            // For complex objects, store a summary
                            map.put(field.getName(), value.getClass().getSimpleName() + ": " + String.valueOf(value));
                        }
                    }
                }
            } catch (Exception reflectionException) {
                log.error("Reflection-based conversion also failed: {}", reflectionException.getMessage());
                map.put("data", String.valueOf(data));
                map.put("dataType", data.getClass().getSimpleName());
            }
            return map;
        }
    }

    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == Boolean.class || clazz == Byte.class || clazz == Character.class ||
                clazz == Double.class || clazz == Float.class || clazz == Integer.class ||
                clazz == Long.class || clazz == Short.class;
    }

    private String generateDescription(String entityType, String action) {
        return String.format("%s %s operation", entityType, action.toLowerCase());
    }

    private String getClientIpAddress() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
