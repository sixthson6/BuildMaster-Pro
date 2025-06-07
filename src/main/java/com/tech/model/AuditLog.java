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
    private String actorType; // e.g., "Project", "Task", "Developer"
    private String actionType; // CREATE, UPDATE, DELETE
    private LocalDateTime timestamp;
    private String actor;
    private String username; // mock or real user
    private Object dataSnapshot; // entity JSON
}

