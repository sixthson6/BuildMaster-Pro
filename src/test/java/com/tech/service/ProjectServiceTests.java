package com.tech.service;

import com.tech.dto.CreateProjectDTO;
import com.tech.dto.ProjectDTO;
import com.tech.mapper.ProjectMapper;
import com.tech.model.Project;
import com.tech.repository.ProjectRepository;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService Unit Tests")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ProjectService projectService;

    private Project project1;
    private ProjectDTO projectDTO1;
    private CreateProjectDTO createProjectDTO;

    @BeforeEach
    void setUp() {
        project1 = new Project();
        project1.setId(1L);
        project1.setName("Test Project 1");
        project1.setDescription("Description 1");
        project1.setDeadline(LocalDate.now().plusMonths(1));
        project1.setStatus(Project.ProjectStatus.ACTIVE);
        project1.setTasks(Collections.emptyList());

        projectDTO1 = new ProjectDTO();
        projectDTO1.setId(1L);
        projectDTO1.setName("Test Project 1");
        projectDTO1.setDescription("Description 1");
        projectDTO1.setDeadline(LocalDate.now().plusMonths(1));
        projectDTO1.setStatus("ACTIVE");
        projectDTO1.setTaskIds(Collections.emptyList());

        createProjectDTO = new CreateProjectDTO();
        createProjectDTO.setName("New Project");
        createProjectDTO.setDescription("New Description");
        createProjectDTO.setDeadline(LocalDate.now().plusMonths(2));
        createProjectDTO.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("Should return all projects with pagination")
    void getAllProjects_shouldReturnAllProjects() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> projectPage = new PageImpl<>(Arrays.asList(project1), pageable, 1);

        when(projectRepository.findAll(pageable)).thenReturn(projectPage);
        when(projectMapper.toDto(any(Project.class))).thenReturn(projectDTO1);

        Page<ProjectDTO> result = projectService.getAllProjects(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(projectDTO1.getId(), result.getContent().get(0).getId());
        verify(projectRepository, times(1)).findAll(pageable);
        verify(projectMapper, times(1)).toDto(project1);
    }

    @Test
    @DisplayName("Should return project by ID when found")
    void getProjectById_shouldReturnProject_whenFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project1));
        when(projectMapper.toDto(project1)).thenReturn(projectDTO1);

        ProjectDTO result = projectService.getProjectById(1L);

        assertNotNull(result);
        assertEquals(projectDTO1.getId(), result.getId());
        verify(projectRepository, times(1)).findById(1L);
        verify(projectMapper, times(1)).toDto(project1);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when project not found by ID")
    void getProjectById_shouldThrowException_whenNotFound() {
        when(projectRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> projectService.getProjectById(99L));
        verify(projectRepository, times(1)).findById(99L);
        verifyNoInteractions(projectMapper);
    }

    @Test
    @DisplayName("Should create and return a new project")
    void createProject_shouldSaveAndReturnProject() {
        Project newProject = new Project();
        newProject.setName(createProjectDTO.getName());
        newProject.setDescription(createProjectDTO.getDescription());
        newProject.setDeadline(createProjectDTO.getDeadline());
        newProject.setStatus(Project.ProjectStatus.ACTIVE);

        Project savedProject = new Project();
        savedProject.setId(2L);
        savedProject.setName(createProjectDTO.getName());
        savedProject.setDescription(createProjectDTO.getDescription());
        savedProject.setDeadline(createProjectDTO.getDeadline());
        savedProject.setStatus(Project.ProjectStatus.ACTIVE);

        ProjectDTO savedProjectDTO = new ProjectDTO();
        savedProjectDTO.setId(2L);
        savedProjectDTO.setName(createProjectDTO.getName());
        savedProjectDTO.setDescription(createProjectDTO.getDescription());
        savedProjectDTO.setDeadline(createProjectDTO.getDeadline());
        savedProjectDTO.setStatus("ACTIVE");


        when(projectMapper.toEntity(createProjectDTO)).thenReturn(newProject);
        when(projectRepository.save(newProject)).thenReturn(savedProject);
        when(projectMapper.toDto(savedProject)).thenReturn(savedProjectDTO);
        doNothing().when(auditLogService).logProjectAction(anyString(), any(ProjectDTO.class));

        ProjectDTO result = projectService.createProject(createProjectDTO);

        assertNotNull(result);
        assertEquals(savedProjectDTO.getName(), result.getName());
        verify(projectMapper, times(1)).toEntity(createProjectDTO);
        verify(projectRepository, times(1)).save(newProject);
        verify(projectMapper, times(1)).toDto(savedProject);
        verify(auditLogService, times(1)).logProjectAction("CREATE", savedProjectDTO);
    }

    @Test
    @DisplayName("Should update and return an existing project when found")
    void updateProject_shouldUpdateAndReturnProject_whenFound() {
        CreateProjectDTO updateProjectDTO = new CreateProjectDTO();
        updateProjectDTO.setName("Updated Project Name");
        updateProjectDTO.setDescription("Updated Description");
        updateProjectDTO.setDeadline(LocalDate.now().plusMonths(3));
        updateProjectDTO.setStatus("DONE");

        Project updatedProject = new Project();
        updatedProject.setId(1L);
        updatedProject.setName("Updated Project Name");
        updatedProject.setDescription("Updated Description");
        updatedProject.setDeadline(LocalDate.now().plusMonths(3));
        updatedProject.setStatus(Project.ProjectStatus.COMPLETED);
        updatedProject.setTasks(Collections.emptyList());

        ProjectDTO updatedProjectDTO = new ProjectDTO();
        updatedProjectDTO.setId(1L);
        updatedProjectDTO.setName("Updated Project Name");
        updatedProjectDTO.setDescription("Updated Description");
        updatedProjectDTO.setDeadline(LocalDate.now().plusMonths(3));
        updatedProjectDTO.setStatus("DONE");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project1));
        doNothing().when(projectMapper).updateEntityFromDto(updateProjectDTO, project1);
        when(projectRepository.save(project1)).thenReturn(updatedProject);
        when(projectMapper.toDto(updatedProject)).thenReturn(updatedProjectDTO);
        doNothing().when(auditLogService).logProjectAction(anyString(), any(ProjectDTO.class));

        ProjectDTO result = projectService.updateProject(1L, updateProjectDTO);

        assertNotNull(result);
        assertEquals(updatedProjectDTO.getName(), result.getName());
        assertEquals(updatedProjectDTO.getStatus(), result.getStatus());
        verify(projectRepository, times(1)).findById(1L);
        verify(projectMapper, times(1)).updateEntityFromDto(updateProjectDTO, project1);
        verify(projectRepository, times(1)).save(project1);
        verify(projectMapper, times(1)).toDto(updatedProject);
        verify(auditLogService, times(1)).logProjectAction("UPDATE", updatedProjectDTO);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when updating a non-existent project")
    void updateProject_shouldThrowException_whenNotFound() {
        CreateProjectDTO updateProjectDTO = new CreateProjectDTO();
        when(projectRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> projectService.updateProject(99L, updateProjectDTO));
        verify(projectRepository, times(1)).findById(99L);
        verifyNoInteractions(projectMapper);
        verifyNoInteractions(auditLogService);
    }

    @Test
    @DisplayName("Should delete project by ID when found")
    void deleteProject_shouldDeleteProject_whenFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project1));
        doNothing().when(projectRepository).deleteById(1L);
        when(projectMapper.toDto(project1)).thenReturn(projectDTO1);
        doNothing().when(auditLogService).logProjectAction(anyString(), any(ProjectDTO.class));


        assertDoesNotThrow(() -> projectService.deleteProject(1L));

        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, times(1)).deleteById(1L);
        verify(auditLogService, times(1)).logProjectAction("DELETE", projectDTO1);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when deleting a non-existent project")
    void deleteProject_shouldThrowException_whenNotFound() {
        when(projectRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> projectService.deleteProject(99L));
        verify(projectRepository, times(1)).findById(99L);
        verifyNoMoreInteractions(projectRepository);
        verifyNoInteractions(projectMapper);
        verifyNoInteractions(auditLogService);
    }
}