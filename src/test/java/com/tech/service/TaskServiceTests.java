package com.tech.service;

import com.tech.dto.CreateTaskDTO;
import com.tech.dto.DeveloperDTO;
import com.tech.dto.TaskDTO;
import com.tech.mapper.TaskMapper;
import com.tech.model.Developer;
import com.tech.model.Project;
import com.tech.model.Task;
import com.tech.model.Task.TaskStatus;
import com.tech.repository.DeveloperRepository;
import com.tech.repository.ProjectRepository;
import com.tech.repository.TaskRepository;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private DeveloperRepository developerRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private TaskService taskService;

    private Project project;
    private Developer developer1;
    private DeveloperDTO developerDTO1;
    private Task task1;
    private TaskDTO taskDTO1;
    private CreateTaskDTO createTaskDTO;

    @BeforeEach
    void setUp() {
        // Initialize Project object
        project = new Project();
        project.setId(1L);
        project.setName("Project Alpha");
        project.setDescription("Description for Alpha");
        project.setDeadline(LocalDate.now().plusMonths(3));
        project.setStatus(Project.ProjectStatus.ACTIVE);
        project.setTasks(new ArrayList<>()); // Use ArrayList for mutable list

        // Initialize Developer object
        developer1 = new Developer();
        developer1.setId(1L);
        developer1.setName("Dev A");
        developer1.setEmail("dev.a@example.com");
        developer1.setSkills("Java");
        developer1.setTasks(new ArrayList<>()); // Use ArrayList for mutable list

        // Initialize DeveloperDTO for use in TaskDTO
        developerDTO1 = new DeveloperDTO();
        developerDTO1.setId(1L);
        developerDTO1.setName("Dev A");
        developerDTO1.setEmail("dev.a@example.com");
        developerDTO1.setSkills("Java");

        // Initialize Task object
        task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");
        task1.setDescription("Desc 1");
        task1.setStatus(TaskStatus.TODO);
        task1.setDueDate(LocalDate.now().plusDays(7));
        task1.setProject(project);
        task1.setAssignedDevelopers(Collections.singletonList(developer1)); // Immutable for initial setup

        taskDTO1 = new TaskDTO();
        taskDTO1.setId(1L);
        taskDTO1.setTitle("Task 1");
        taskDTO1.setDescription("Desc 1");
        taskDTO1.setStatus("TODO");
        taskDTO1.setDueDate(LocalDate.now().plusDays(7));
        taskDTO1.setProjectId(1L);
        taskDTO1.setAssignedDevelopers(Collections.singletonList(developerDTO1));
        // taskDTO1.setAssignedDeveloperIds is NOT used as per TaskDTO.java

        createTaskDTO = new CreateTaskDTO();
        createTaskDTO.setTitle("New Task");
        createTaskDTO.setDescription("New Task Desc");
        createTaskDTO.setStatus("IN_PROGRESS");
        createTaskDTO.setDueDate(LocalDate.now().plusDays(10));
        createTaskDTO.setProjectId(1L);
        createTaskDTO.setAssignedDeveloperIds(Collections.singletonList(1L));
    }

    @Test
    @DisplayName("Should return all tasks with pagination")
    void getAllTasks_shouldReturnAllTasks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(Collections.singletonList(task1), pageable, 1);

        when(taskRepository.findAll(pageable)).thenReturn(taskPage);
        when(taskMapper.toDto(any(Task.class))).thenReturn(taskDTO1);

        Page<TaskDTO> result = taskService.getAllTasks(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(taskDTO1.getId(), result.getContent().get(0).getId());
        verify(taskRepository, times(1)).findAll(pageable);
        verify(taskMapper, times(1)).toDto(task1);
    }

    @Test
    @DisplayName("Should return task by ID when found")
    void getTaskById_shouldReturnTask_whenFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task1));
        when(taskMapper.toDto(task1)).thenReturn(taskDTO1);

        TaskDTO result = taskService.getTaskById(1);

        assertNotNull(result);
        assertEquals(taskDTO1.getId(), result.getId());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskMapper, times(1)).toDto(task1);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when task not found by ID")
    void getTaskById_shouldThrowException_whenNotFound() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taskService.getTaskById(99));
        verify(taskRepository, times(1)).findById(99L);
        verifyNoInteractions(taskMapper);
    }

    @Test
    @DisplayName("Should return tasks by project ID with pagination")
    void getTasksByProjectId_shouldReturnTasks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(Collections.singletonList(task1), pageable, 1);

        when(taskRepository.findByProjectId(project.getId(), pageable)).thenReturn(taskPage);
        when(taskMapper.toDto(any(Task.class))).thenReturn(taskDTO1);

        Page<TaskDTO> result = taskService.getTasksByProjectId(project.getId().intValue(), pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(taskDTO1.getId(), result.getContent().get(0).getId());
        verify(taskRepository, times(1)).findByProjectId(project.getId(), pageable);
        verify(taskMapper, times(1)).toDto(task1);
    }

    @Test
    @DisplayName("Should return tasks by developer ID with pagination")
    void getTasksByDeveloperId_shouldReturnTasks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(Collections.singletonList(task1), pageable, 1);

        when(taskRepository.findByAssignedDevelopersId(developer1.getId(), pageable)).thenReturn(taskPage);
        when(taskMapper.toDto(any(Task.class))).thenReturn(taskDTO1);

        Page<TaskDTO> result = taskService.getTasksByDeveloperId(developer1.getId().intValue(), pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(taskDTO1.getId(), result.getContent().get(0).getId());
        verify(taskRepository, times(1)).findByAssignedDevelopersId(developer1.getId(), pageable);
        verify(taskMapper, times(1)).toDto(task1);
    }

    @Test
    @DisplayName("Should create and return a new task")
    void createTask_shouldSaveAndReturnTask() {
        Task newTask = new Task();
        newTask.setTitle(createTaskDTO.getTitle());
        newTask.setDescription(createTaskDTO.getDescription());
        newTask.setStatus(TaskStatus.valueOf(createTaskDTO.getStatus())); // Convert string to enum
        newTask.setDueDate(createTaskDTO.getDueDate());
        newTask.setProject(project);

        Task savedTask = new Task();
        savedTask.setId(2L);
        savedTask.setTitle(createTaskDTO.getTitle());
        savedTask.setDescription(createTaskDTO.getDescription());
        savedTask.setStatus(TaskStatus.valueOf(createTaskDTO.getStatus())); // Convert string to enum
        savedTask.setDueDate(createTaskDTO.getDueDate());
        savedTask.setProject(project);
        savedTask.setAssignedDevelopers(Collections.singletonList(developer1));

        TaskDTO savedTaskDTO = new TaskDTO();
        savedTaskDTO.setId(2L);
        savedTaskDTO.setTitle(createTaskDTO.getTitle());
        savedTaskDTO.setDescription(createTaskDTO.getDescription());
        savedTaskDTO.setStatus(createTaskDTO.getStatus());
        savedTaskDTO.setDueDate(createTaskDTO.getDueDate());
        savedTaskDTO.setProjectId(1L);
        savedTaskDTO.setAssignedDevelopers(Collections.singletonList(developerDTO1));


        when(projectRepository.findById(createTaskDTO.getProjectId())).thenReturn(Optional.of(project));
        when(developerRepository.findAllById(createTaskDTO.getAssignedDeveloperIds())).thenReturn(Collections.singletonList(developer1));

        // Mock the mapper to return the new Task object with appropriate properties set
        when(taskMapper.toEntity(createTaskDTO)).thenReturn(newTask);

        when(taskRepository.save(newTask)).thenReturn(savedTask);
        when(taskMapper.toDto(savedTask)).thenReturn(savedTaskDTO);
        doNothing().when(auditLogService).logTaskAction(anyString(), any(TaskDTO.class));

        TaskDTO result = taskService.createTask(createTaskDTO);

        assertNotNull(result);
        assertEquals(savedTaskDTO.getTitle(), result.getTitle());
        verify(projectRepository, times(1)).findById(createTaskDTO.getProjectId());
        verify(developerRepository, times(1)).findAllById(createTaskDTO.getAssignedDeveloperIds());
        verify(taskMapper, times(1)).toEntity(createTaskDTO); // Mapper creates entity
        verify(taskRepository, times(1)).save(newTask);
        verify(taskMapper, times(1)).toDto(savedTask);
        verify(auditLogService, times(1)).logTaskAction("CREATE", savedTaskDTO);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when creating task with non-existent project")
    void createTask_shouldThrowException_whenProjectNotFound() {
        when(projectRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taskService.createTask(createTaskDTO));
        verifyNoInteractions(taskRepository);
        verifyNoInteractions(taskMapper);
        verifyNoInteractions(auditLogService);
    }

    @Test
    @DisplayName("Should update and return an existing task when found")
    void updateTask_shouldUpdateAndReturnTask_whenFound() {
        CreateTaskDTO updateTaskDTO = new CreateTaskDTO();
        updateTaskDTO.setTitle("Updated Task Title");
        updateTaskDTO.setDescription("Updated Task Desc");
        updateTaskDTO.setStatus("DONE");
        updateTaskDTO.setDueDate(LocalDate.now().plusDays(15));
        updateTaskDTO.setProjectId(1L);
        updateTaskDTO.setAssignedDeveloperIds(Collections.singletonList(1L));

        Task updatedTaskEntity = new Task(); // This represents the entity *after* updates and saving
        updatedTaskEntity.setId(1L);
        updatedTaskEntity.setTitle("Updated Task Title");
        updatedTaskEntity.setDescription("Updated Task Desc");
        updatedTaskEntity.setStatus(TaskStatus.DONE);
        updatedTaskEntity.setDueDate(LocalDate.now().plusDays(15));
        updatedTaskEntity.setProject(project);
        updatedTaskEntity.setAssignedDevelopers(Collections.singletonList(developer1));

        TaskDTO updatedTaskDTOExpected = new TaskDTO();
        updatedTaskDTOExpected.setId(1L);
        updatedTaskDTOExpected.setTitle("Updated Task Title");
        updatedTaskDTOExpected.setDescription("Updated Task Desc");
        updatedTaskDTOExpected.setStatus("DONE");
        updatedTaskDTOExpected.setDueDate(LocalDate.now().plusDays(15));
        updatedTaskDTOExpected.setProjectId(1L);
        updatedTaskDTOExpected.setAssignedDevelopers(Collections.singletonList(developerDTO1));


        when(taskRepository.findById(1L)).thenReturn(Optional.of(task1));
        when(developerRepository.findAllById(updateTaskDTO.getAssignedDeveloperIds())).thenReturn(Collections.singletonList(developer1));
        // Mock the mapper's update method
        doNothing().when(taskMapper).updateEntityFromDto(updateTaskDTO, task1);
        when(taskRepository.save(task1)).thenReturn(updatedTaskEntity); // Return the updated entity
        when(taskMapper.toDto(updatedTaskEntity)).thenReturn(updatedTaskDTOExpected); // Mapper converts the updated entity

        doNothing().when(auditLogService).logTaskAction(anyString(), any(TaskDTO.class));

        TaskDTO result = taskService.updateTask(1, updateTaskDTO);

        assertNotNull(result);
        assertEquals(updatedTaskDTOExpected.getTitle(), result.getTitle());
        assertEquals(updatedTaskDTOExpected.getStatus(), result.getStatus());
        verify(taskRepository, times(1)).findById(1L);
        verify(developerRepository, times(1)).findAllById(updateTaskDTO.getAssignedDeveloperIds());
        verify(taskMapper, times(1)).updateEntityFromDto(updateTaskDTO, task1); // Verify mapper update
        verify(taskRepository, times(1)).save(task1); // Verify save on the existing entity
        verify(taskMapper, times(1)).toDto(updatedTaskEntity);
        verify(auditLogService, times(1)).logTaskAction("UPDATE", updatedTaskDTOExpected);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when updating a non-existent task")
    void updateTask_shouldThrowException_whenNotFound() {
        CreateTaskDTO updateTaskDTO = new CreateTaskDTO();
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taskService.updateTask(99, updateTaskDTO));
        verifyNoInteractions(projectRepository); // Ensure projectRepo is not called
        verifyNoInteractions(developerRepository); // Ensure developerRepo is not called
        verifyNoInteractions(taskMapper);
        verifyNoInteractions(auditLogService);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when assigning developer to non-existent task")
    void assignDeveloperToTask_shouldThrowException_whenTaskNotFound() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taskService.assignDeveloperToTask(99, 1));
        verifyNoInteractions(developerRepository);
        verifyNoInteractions(taskMapper);
        verifyNoInteractions(auditLogService);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when assigning non-existent developer to task")
    void assignDeveloperToTask_shouldThrowException_whenDeveloperNotFound() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(task1));
        when(developerRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taskService.assignDeveloperToTask(1, 99));
        verify(taskRepository, times(1)).findById(1L);
        verify(developerRepository, times(1)).findById(99L);
        verifyNoMoreInteractions(taskRepository);
        verifyNoInteractions(taskMapper);
        verifyNoInteractions(auditLogService);
    }
}
