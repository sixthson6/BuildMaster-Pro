package com.tech.auditlog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async("auditExecutor")
    public void logAction(String entityType, String entityId, String action,
                          String actorName, Object currentData, Object previousData) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .actorName(actorName != null ? actorName : "system")
                    .timestamp(LocalDateTime.now())
                    .dataSnapshot(convertToMap(currentData))
                    .previousData(convertToMap(previousData))
                    .description(generateDescription(entityType, action))
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit log created for {} {} on {}", action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to create audit log for {} {} on {}: {}",
                    action, entityType, entityId, e.getMessage());
        }
    }

    @Async("auditExecutor")
    public void logAction(String entityType, String entityId, String action, String actorName, Object currentData) {
        logAction(entityType, entityId, action, actorName, currentData, null);
    }

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
        return auditLogRepository.findByAction(action, pageable);
    }

    public Page<AuditLog> getLogsByDateRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(startTime, endTime, pageable);
    }

    public Page<AuditLog> getLogsByMultipleCriteria(String entityType, String actorName,
                                                    String action, Pageable pageable) {
        return auditLogRepository.findByMultipleCriteria(entityType, actorName, action, pageable);
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
        return stats;
    }

    private Map<String, Object> convertToMap(Object data) {
        if (data == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();

        // Use reflection to convert object to map
        try {
            Class<?> clazz = data.getClass();
            java.lang.reflect.Field[] fields = clazz.getDeclaredFields();

            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(data);

                // Handle different types appropriately
                if (value != null) {
                    if (value instanceof java.time.LocalDate ||
                            value instanceof java.time.LocalDateTime ||
                            value instanceof String ||
                            value instanceof Number ||
                            value instanceof Boolean) {
                        map.put(field.getName(), value.toString());
                    } else if (value instanceof java.util.List) {
                        // For collections, store size or basic info
                        map.put(field.getName(), "List[" + ((java.util.List<?>) value).size() + "]");
                    } else {
                        // For complex objects, store class name and toString
                        map.put(field.getName(), value.getClass().getSimpleName() + ": " + value.toString());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not convert object to map: {}", e.getMessage());
            map.put("data", data.toString());
        }

        return map;
    }

    private String generateDescription(String entityType, String action) {
        return String.format("%s %s", entityType, action.toLowerCase());
    }
}
