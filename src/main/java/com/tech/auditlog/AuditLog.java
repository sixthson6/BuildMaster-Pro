package com.tech.auditlog;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    private String id;

    private String entityType; // "Developer", "Project", "Task"
    private String entityId;   // ID of the affected entity
    private String action;     // "CREATE", "UPDATE", "DELETE"
    private String actorName;  // Username or system identifier
    private LocalDateTime timestamp;

    // Store the actual data as a flexible map
    private Map<String, Object> dataSnapshot;

    // Store previous state for updates
    private Map<String, Object> previousData;

    // Additional metadata
    private String description;
    private String ipAddress;
    private String userAgent;
}
