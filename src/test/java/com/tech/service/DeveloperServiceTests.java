package com.tech.service;

import com.tech.dto.CreateDeveloperDTO;
import com.tech.dto.DeveloperDTO;
import com.tech.mapper.DeveloperMapper;
import com.tech.model.Developer;
import com.tech.repository.DeveloperRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList; // Added for mutable list
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeveloperService Unit Tests")
class DeveloperServiceTest {

    @Mock
    private DeveloperRepository developerRepository;

    @Mock
    private DeveloperMapper developerMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private DeveloperService developerService;

    private Developer developer1;
    private DeveloperDTO developerDTO1;
    private CreateDeveloperDTO createDeveloperDTO;

    @BeforeEach
    void setUp() {
        // Initialize Developer object
        developer1 = new Developer();
        developer1.setId(1L);
        developer1.setName("Alice Smith");
        developer1.setEmail("alice@example.com");
        developer1.setSkills("Java, Spring");
        developer1.setTasks(new ArrayList<>()); // Initialize with mutable list

        developerDTO1 = new DeveloperDTO();
        developerDTO1.setId(1L);
        developerDTO1.setName("Alice Smith");
        developerDTO1.setEmail("alice@example.com");
        developerDTO1.setSkills("Java, Spring");

        createDeveloperDTO = new CreateDeveloperDTO();
        createDeveloperDTO.setName("Bob Johnson");
        createDeveloperDTO.setEmail("bob@example.com");
        createDeveloperDTO.setSkills("Python, Django");
    }

    @Test
    @DisplayName("Should return all developers with pagination")
    void getAllDevelopers_shouldReturnAllDevelopers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Developer> developerPage = new PageImpl<>(Arrays.asList(developer1), pageable, 1);

        when(developerRepository.findAll(pageable)).thenReturn(developerPage);
        when(developerMapper.toDto(any(Developer.class))).thenReturn(developerDTO1);

        Page<DeveloperDTO> result = developerService.getAllDevelopers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(developerDTO1.getId(), result.getContent().get(0).getId());
        verify(developerRepository, times(1)).findAll(pageable);
        verify(developerMapper, times(1)).toDto(developer1);
    }

    @Test
    @DisplayName("Should return developer by ID when found")
    void getDeveloperById_shouldReturnDeveloper_whenFound() {
        when(developerRepository.findById(1L)).thenReturn(Optional.of(developer1));
        when(developerMapper.toDto(developer1)).thenReturn(developerDTO1);

        DeveloperDTO result = developerService.getDeveloperById(1L);

        assertNotNull(result);
        assertEquals(developerDTO1.getId(), result.getId());
        verify(developerRepository, times(1)).findById(1L);
        verify(developerMapper, times(1)).toDto(developer1);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when developer not found by ID")
    void getDeveloperById_shouldThrowException_whenNotFound() {
        when(developerRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> developerService.getDeveloperById(99L));
        verify(developerRepository, times(1)).findById(99L);
        verifyNoInteractions(developerMapper);
    }

    @Test
    @DisplayName("Should create and return a new developer")
    void createDeveloper_shouldSaveAndReturnDeveloper() {
        Developer newDeveloper = new Developer();
        newDeveloper.setName(createDeveloperDTO.getName());
        newDeveloper.setEmail(createDeveloperDTO.getEmail());
        newDeveloper.setSkills(createDeveloperDTO.getSkills());

        Developer savedDeveloper = new Developer();
        savedDeveloper.setId(2L);
        savedDeveloper.setName(createDeveloperDTO.getName());
        savedDeveloper.setEmail(createDeveloperDTO.getEmail());
        savedDeveloper.setSkills(createDeveloperDTO.getSkills());

        DeveloperDTO savedDeveloperDTO = new DeveloperDTO();
        savedDeveloperDTO.setId(2L);
        savedDeveloperDTO.setName(createDeveloperDTO.getName());
        savedDeveloperDTO.setEmail(createDeveloperDTO.getEmail());
        savedDeveloperDTO.setSkills(createDeveloperDTO.getSkills());

        when(developerMapper.toEntity(createDeveloperDTO)).thenReturn(newDeveloper);
        when(developerRepository.save(newDeveloper)).thenReturn(savedDeveloper);
        when(developerMapper.toDto(savedDeveloper)).thenReturn(savedDeveloperDTO);
        doNothing().when(auditLogService).logDeveloperAction(anyString(), any(DeveloperDTO.class));

        DeveloperDTO result = developerService.createDeveloper(createDeveloperDTO);

        assertNotNull(result);
        assertEquals(savedDeveloperDTO.getName(), result.getName());
        verify(developerMapper, times(1)).toEntity(createDeveloperDTO);
        verify(developerRepository, times(1)).save(newDeveloper);
        verify(developerMapper, times(1)).toDto(savedDeveloper);
        verify(auditLogService, times(1)).logDeveloperAction("CREATE", savedDeveloperDTO);
    }

    @Test
    @DisplayName("Should update and return an existing developer when found")
    void updateDeveloper_shouldUpdateAndReturnDeveloper_whenFound() {
        CreateDeveloperDTO updateDeveloperDTO = new CreateDeveloperDTO();
        updateDeveloperDTO.setName("Alice Smith Updated");
        updateDeveloperDTO.setEmail("alice.updated@example.com");
        updateDeveloperDTO.setSkills("Java, Spring, Microservices");

        Developer updatedDeveloperEntity = new Developer();
        updatedDeveloperEntity.setId(1L);
        updatedDeveloperEntity.setName("Alice Smith Updated");
        updatedDeveloperEntity.setEmail("alice.updated@example.com");
        updatedDeveloperEntity.setSkills("Java, Spring, Microservices");
        updatedDeveloperEntity.setTasks(new ArrayList<>());

        DeveloperDTO updatedDeveloperDTOExpected = new DeveloperDTO();
        updatedDeveloperDTOExpected.setId(1L);
        updatedDeveloperDTOExpected.setName("Alice Smith Updated");
        updatedDeveloperDTOExpected.setEmail("alice.updated@example.com");
        updatedDeveloperDTOExpected.setSkills("Java, Spring, Microservices");

        when(developerRepository.findById(1L)).thenReturn(Optional.of(developer1));
        doNothing().when(developerMapper).updateEntityFromDto(updateDeveloperDTO, developer1);
        when(developerRepository.save(developer1)).thenReturn(updatedDeveloperEntity);
        when(developerMapper.toDto(updatedDeveloperEntity)).thenReturn(updatedDeveloperDTOExpected);
        doNothing().when(auditLogService).logDeveloperAction(anyString(), any(DeveloperDTO.class));

        DeveloperDTO result = developerService.updateDeveloper(1L, updateDeveloperDTO);

        assertNotNull(result);
        assertEquals(updatedDeveloperDTOExpected.getName(), result.getName());
        verify(developerRepository, times(1)).findById(1L);
        verify(developerMapper, times(1)).updateEntityFromDto(updateDeveloperDTO, developer1);
        verify(developerRepository, times(1)).save(developer1);
        verify(developerMapper, times(1)).toDto(updatedDeveloperEntity);
        verify(auditLogService, times(1)).logDeveloperAction("UPDATE", updatedDeveloperDTOExpected);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when updating a non-existent developer")
    void updateDeveloper_shouldThrowException_whenNotFound() {
        CreateDeveloperDTO updateDeveloperDTO = new CreateDeveloperDTO();
        when(developerRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> developerService.updateDeveloper(99L, updateDeveloperDTO));
        verify(developerRepository, times(1)).findById(99L);
        verifyNoInteractions(developerMapper);
        verifyNoInteractions(auditLogService);
    }
}
