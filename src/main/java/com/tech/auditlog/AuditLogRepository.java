package com.tech.auditlog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    // Find logs by entity type
    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

    // Find logs by actor name
    Page<AuditLog> findByActorName(String actorName, Pageable pageable);

    // Find logs by entity type and entity ID
    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId, Pageable pageable);

    // Find logs by action type
    Page<AuditLog> findByAction(String action, Pageable pageable);

    // Find logs within a date range
    Page<AuditLog> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    // Find logs by entity type and actor name
    Page<AuditLog> findByEntityTypeAndActorName(String entityType, String actorName, Pageable pageable);

    // Fixed MongoDB query for multiple criteria
    @Query("{ $and: [ " +
            "{ $or: [ { $and: [ { 'entityType': { $exists: true } }, { 'entityType': ?0 } ] }, { $expr: { $eq: [?0, null] } } ] }, " +
            "{ $or: [ { $and: [ { 'actorName': { $exists: true } }, { 'actorName': ?1 } ] }, { $expr: { $eq: [?1, null] } } ] }, " +
            "{ $or: [ { $and: [ { 'action': { $exists: true } }, { 'action': ?2 } ] }, { $expr: { $eq: [?2, null] } } ] } " +
            "] }")
    Page<AuditLog> findByMultipleCriteria(String entityType, String actorName, String action, Pageable pageable);

    // Count logs by entity type
    long countByEntityType(String entityType);

    // Count logs by actor name
    long countByActorName(String actorName);

    // Count logs by action
    long countByAction(String action);

    // Find recent logs (last N records)
    List<AuditLog> findTop50ByOrderByTimestampDesc();

    // Find logs by entity ID across all types
    Page<AuditLog> findByEntityId(String entityId, Pageable pageable);
}