package com.tech.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    private String id;
    private String actorType;
    private String actionType;
    private LocalDateTime timestamp;
    private String entityType;
    private String actor;
    private String username;
    private Object dataSnapshot;
}

