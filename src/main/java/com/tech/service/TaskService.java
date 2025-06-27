package com.tech.service;

import com.tech.dto.CreateTaskDTO;
import com.tech.dto.TaskDTO;
import com.tech.dto.summary.TaskSummary;
import com.tech.mapper.TaskMapper;
import com.tech.model.Developer;
import com.tech.model.Project;
import com.tech.model.Task;
import com.tech.repository.DeveloperRepository;
import com.tech.repository.ProjectRepository;
import com.tech.repository.TaskRepository;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final DeveloperRepository developerRepository;
    private final TaskMapper taskMapper;
    private final AuditLogService auditLogService;
    private final MeterRegistry meterRegistry;

    private final io.micrometer.core.instrument.Counter tasksCreatedCounter;
    private final io.micrometer.core.instrument.Counter tasksUpdatedCounter;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository,
                       DeveloperRepository developerRepository, TaskMapper taskMapper,
                       AuditLogService auditLogService, MeterRegistry meterRegistry) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.developerRepository = developerRepository;
        this.taskMapper = taskMapper;
        this.auditLogService = auditLogService;
        this.meterRegistry = meterRegistry;

        this.tasksCreatedCounter = meterRegistry.counter("tasks.processed.created", "type", "created");
        this.tasksUpdatedCounter = meterRegistry.counter("tasks.processed.updated", "type", "updated");
    }

    @Cacheable(value = "tasksList", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public Page<TaskSummary> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable)
                .map(taskMapper::toSummary);
    }

    @Cacheable(value = "tasksByProject", key = "#projectId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public TaskDTO getTaskById(Integer id) {
        Task task = taskRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));
        return taskMapper.toDto(task);
    }

    @Cacheable(value = "tasksByDeveloper", key = "#developerId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public Page<TaskDTO> getTasksByProjectId(Integer projectId, Pageable pageable) {
        return taskRepository.findByProjectId(Long.valueOf(projectId), pageable)
                .map(taskMapper::toDto);
    }

    public Page<TaskDTO> getTasksByDeveloperId(Integer developerId, Pageable pageable) {
        return taskRepository.findByAssignedDevelopersId(Long.valueOf(developerId), pageable)
                .map(taskMapper::toDto);
    }

    @Transactional
    @CachePut(value = "taskById", key = "#result.id")
    @CacheEvict(value = {"tasksList", "tasksByProject", "tasksByDeveloper"}, allEntries = true)
    public TaskDTO createTask(CreateTaskDTO createTaskDTO) {
        Project project = projectRepository.findById(createTaskDTO.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + createTaskDTO.getProjectId()));

        Task task = taskMapper.toEntity(createTaskDTO);
        task.setProject(project);

        if (createTaskDTO.getAssignedDeveloperIds() != null && !createTaskDTO.getAssignedDeveloperIds().isEmpty()) {
            List<Developer> developers = developerRepository.findAllById(createTaskDTO.getAssignedDeveloperIds());
            task.setAssignedDevelopers(developers);
        }

        Task savedTask = taskRepository.save(task);
        TaskDTO taskDTO = taskMapper.toDto(savedTask);
        auditLogService.logTaskAction("CREATE", taskDTO);

        tasksCreatedCounter.increment();
        return taskDTO;
    }

    @Transactional
    @CachePut(value = "taskById", key = "#id")
    @CacheEvict(value = {"tasksList", "tasksByProject", "tasksByDeveloper"})
    public TaskDTO updateTask(Integer id, CreateTaskDTO updateTaskDTO) {
        Task existingTask = taskRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));

        taskMapper.updateEntityFromDto(updateTaskDTO, existingTask);

        if (updateTaskDTO.getAssignedDeveloperIds() != null) {
            List<Developer> developers = developerRepository.findAllById(updateTaskDTO.getAssignedDeveloperIds());
            existingTask.setAssignedDevelopers(developers);
        }

        Task updatedTask = taskRepository.save(existingTask);
        TaskDTO taskDTO = taskMapper.toDto(updatedTask);
        auditLogService.logTaskAction("UPDATE", taskDTO);
        tasksUpdatedCounter.increment();
        return taskDTO;
    }

    @Transactional
    @CacheEvict(value = {"taskById", "tasksList", "tasksByProject", "tasksByDeveloper"}, key = "#id", allEntries = true) // Evict specific ID and clear lists
    public void deleteTask(Integer id) {
        if (!taskRepository.existsById(Long.valueOf(id))) {
            throw new EntityNotFoundException("Task not found with id: " + id);
        }

        taskRepository.deleteById(Long.valueOf(id));
        auditLogService.logTaskAction("DELETE", new TaskDTO());

    }

    @Transactional
    @CachePut(value = "taskById", key = "#taskId")
    @CacheEvict(value = {"tasksList", "tasksByProject", "tasksByDeveloper"}, allEntries = true)
    public TaskDTO assignDeveloperToTask(Integer taskId, Integer developerId) {
        Task task = taskRepository.findById(Long.valueOf(taskId))
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        Developer developer = developerRepository.findById(Long.valueOf(developerId))
                .orElseThrow(() -> new EntityNotFoundException("Developer not found with id: " + developerId));

        if (!task.getAssignedDevelopers().contains(developer)) {
            task.getAssignedDevelopers().add(developer);
            taskRepository.save(task);
        }

        return taskMapper.toDto(task);
    }
}