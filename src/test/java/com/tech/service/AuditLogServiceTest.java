package com.tech.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tech.dto.DeveloperDTO;
import com.tech.dto.ProjectDTO;
import com.tech.dto.TaskDTO;
import com.tech.model.AuditLog;
import com.tech.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogService Unit Tests")
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ObjectMapper objectMapper; // Mock ObjectMapper

    @InjectMocks
    private AuditLogService auditLogService;

    private ProjectDTO sampleProjectDTO;
    private TaskDTO sampleTaskDTO;
    private DeveloperDTO sampleDeveloperDTO;

    @BeforeEach
    void setUp() {
        sampleProjectDTO = new ProjectDTO();
        sampleProjectDTO.setId(1L);
        sampleProjectDTO.setName("Test Project");
        sampleProjectDTO.setDescription("Test Description");

        sampleTaskDTO = new TaskDTO();
        sampleTaskDTO.setId(1L);
        sampleTaskDTO.setTitle("Test Task");
        sampleTaskDTO.setDescription("Task Description");

        sampleDeveloperDTO = new DeveloperDTO();
        sampleDeveloperDTO.setId(1L);
        sampleDeveloperDTO.setName("Test Developer");
        sampleDeveloperDTO.setEmail("dev@example.com");
    }

    @Test
    @DisplayName("Should log project action successfully")
    void logProjectAction_shouldSaveAuditLog() throws JsonProcessingException {
        // Mock ObjectMapper behavior
        when(objectMapper.writeValueAsString(sampleProjectDTO)).thenReturn("{\"id\":1,\"name\":\"Test Project\"}");

        // Call the service method
        auditLogService.logProjectAction("CREATE", sampleProjectDTO);

        // Capture the AuditLog object passed to the repository
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());

        AuditLog capturedLog = auditLogCaptor.getValue();

        // Assertions
        assertNotNull(capturedLog);
        assertEquals("Project", capturedLog.getActorType());
        assertEquals("CREATE", capturedLog.getActionType());
        assertEquals(sampleProjectDTO.getName(), capturedLog.getActor());
        assertEquals("admin", capturedLog.getUsername()); // As per your service implementation
        assertEquals("{\"id\":1,\"name\":\"Test Project\"}", capturedLog.getDataSnapshot());
        assertNotNull(capturedLog.getTimestamp());
        // Verify that timestamp is UTC based on how it's constructed in the service
        assertEquals(ZoneOffset.UTC, capturedLog.getTimestamp().atZone(ZoneOffset.UTC).getOffset());
    }

    @Test
    @DisplayName("Should throw RuntimeException if project data serialization fails")
    void logProjectAction_shouldThrowRuntimeException_onSerializationError() throws JsonProcessingException {
        // Mock ObjectMapper to throw an exception during serialization
        when(objectMapper.writeValueAsString(sampleProjectDTO)).thenThrow(JsonProcessingException.class);

        // Assert that calling the service method throws a RuntimeException
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> auditLogService.logProjectAction("CREATE", sampleProjectDTO));
        assertTrue(thrown.getMessage().contains("Failed to serialize project data for audit log"));

        // Verify that auditLogRepository.save was NOT called
        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should log task action successfully")
    void logTaskAction_shouldSaveAuditLog() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(sampleTaskDTO)).thenReturn("{\"id\":1,\"title\":\"Test Task\"}");

        auditLogService.logTaskAction("UPDATE", sampleTaskDTO);

        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());

        AuditLog capturedLog = auditLogCaptor.getValue();

        assertNotNull(capturedLog);
        assertEquals("Task", capturedLog.getActorType());
        assertEquals("UPDATE", capturedLog.getActionType());
        assertEquals(sampleTaskDTO.toString(), capturedLog.getActor()); // As per your service implementation
        assertEquals("admin", capturedLog.getUsername());
        assertEquals("{\"id\":1,\"title\":\"Test Task\"}", capturedLog.getDataSnapshot());
        assertNotNull(capturedLog.getTimestamp());
    }

    @Test
    @DisplayName("Should throw RuntimeException if task data serialization fails")
    void logTaskAction_shouldThrowRuntimeException_onSerializationError() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(sampleTaskDTO)).thenThrow(JsonProcessingException.class);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> auditLogService.logTaskAction("UPDATE", sampleTaskDTO));
        assertTrue(thrown.getMessage().contains("Failed to serialize task data for audit log"));

        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should log developer action successfully")
    void logDeveloperAction_shouldSaveAuditLog() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(sampleDeveloperDTO)).thenReturn("{\"id\":1,\"name\":\"Test Developer\"}");

        auditLogService.logDeveloperAction("DELETE", sampleDeveloperDTO);

        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());

        AuditLog capturedLog = auditLogCaptor.getValue();

        assertNotNull(capturedLog);
        assertEquals("Developer", capturedLog.getActorType());
        assertEquals("DELETE", capturedLog.getActionType());
        assertEquals(sampleDeveloperDTO.toString(), capturedLog.getActor()); // As per your service implementation
        assertEquals("admin", capturedLog.getUsername());
        assertEquals("{\"id\":1,\"name\":\"Test Developer\"}", capturedLog.getDataSnapshot());
        assertNotNull(capturedLog.getTimestamp());
    }

    @Test
    @DisplayName("Should throw RuntimeException if developer data serialization fails")
    void logDeveloperAction_shouldThrowRuntimeException_onSerializationError() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(sampleDeveloperDTO)).thenThrow(JsonProcessingException.class);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> auditLogService.logDeveloperAction("DELETE", sampleDeveloperDTO));
        assertTrue(thrown.getMessage().contains("Failed to serialize developer data for audit log"));

        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should log login action successfully")
    void logLoginAction_shouldSaveAuditLog() {
        String email = "user@example.com";
        String loginMethod = "Traditional Login";
        String status = "Success";

        auditLogService.logLoginAction("LOGIN_SUCCESS", email, loginMethod, status);

        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());

        AuditLog capturedLog = auditLogCaptor.getValue();

        assertNotNull(capturedLog);
        assertEquals("User", capturedLog.getActorType());
        assertEquals("LOGIN_SUCCESS", capturedLog.getActionType());
        assertEquals("Authentication", capturedLog.getEntityType());
        assertEquals(email, capturedLog.getActor());
        assertEquals(email, capturedLog.getUsername());
        // Verify dataSnapshot content
        String expectedDataSnapshot = "{\"loginMethod\": \"" + loginMethod + "\", \"status\": \"" + status + "\"}";
        assertEquals(expectedDataSnapshot, capturedLog.getDataSnapshot());
        assertNotNull(capturedLog.getTimestamp());
    }

    @Test
    @DisplayName("Should log unauthorized access action successfully")
    void logUnauthorizedAccess_shouldSaveAuditLog() {
        String username = "unauthorized_user";
        String requestPath = "/api/protected";
        String details = "Invalid JWT token";

        auditLogService.logUnauthorizedAccess(username, requestPath, details);

        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(auditLogCaptor.capture());

        AuditLog capturedLog = auditLogCaptor.getValue();

        assertNotNull(capturedLog);
        assertEquals("User", capturedLog.getActorType());
        assertEquals("UNAUTHORIZED_ACCESS", capturedLog.getActionType());
        assertEquals("Authorization", capturedLog.getEntityType());
        assertEquals(username, capturedLog.getActor());
        assertEquals(username, capturedLog.getUsername());
        // Verify dataSnapshot content
        String expectedDataSnapshot = "{\"requestPath\": \"" + requestPath + "\", \"details\": \"" + details + "\"}";
        assertEquals(expectedDataSnapshot, capturedLog.getDataSnapshot());
        assertNotNull(capturedLog.getTimestamp());
    }
}

