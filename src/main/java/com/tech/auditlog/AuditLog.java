package com.tech.auditlog;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
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

    @Indexed
    private String entityType; // "Developer", "Project", "Task"

    @Indexed
    private String entityId;   // ID of the affected entity

    @Indexed
    private String action;     // "CREATE", "UPDATE", "DELETE"

    @Indexed
    private String actorName;  // Username or system identifier

    @Indexed
    private LocalDateTime timestamp;

    // Store the actual data as a flexible map
    private Map<String, Object> dataSnapshot;

    // Store previous state for updates
    private Map<String, Object> previousData;

    // Additional metadata
    private String description;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
}