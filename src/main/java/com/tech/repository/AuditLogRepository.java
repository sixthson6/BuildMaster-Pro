package com.tech.repository;


import com.tech.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
//    List<AuditLog> findByEntityType(String entityType);
    List<AuditLog> findByActor(String actor);
}
