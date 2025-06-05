package com.tech.service;

import com.tech.dto.CreateTaskDTO;
import com.tech.dto.TaskDTO;
import com.tech.mapper.TaskMapper;
import com.tech.model.Developer;
import com.tech.model.Project;
import com.tech.model.Task;
import com.tech.repository.DeveloperRepository;
import com.tech.repository.ProjectRepository;
import com.tech.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final DeveloperRepository developerRepository;
    private final TaskMapper taskMapper;

    public Page<TaskDTO> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable)
                .map(taskMapper::toDto);
    }

    public TaskDTO getTaskById(Integer id) {
        Task task = taskRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));
        return taskMapper.toDto(task);
    }

    public Page<TaskDTO> getTasksByProjectId(Integer projectId, Pageable pageable) {
        return taskRepository.findByProjectId(Long.valueOf(projectId), pageable)
                .map(taskMapper::toDto);
    }

    public Page<TaskDTO> getTasksByDeveloperId(Integer developerId, Pageable pageable) {
        return taskRepository.findByAssignedDevelopersId(Long.valueOf(developerId), pageable)
                .map(taskMapper::toDto);
    }

    @Transactional
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
        return taskMapper.toDto(savedTask);
    }

    @Transactional
    public TaskDTO updateTask(Integer id, CreateTaskDTO updateTaskDTO) {
        Task existingTask = taskRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));

        taskMapper.updateEntityFromDto(updateTaskDTO, existingTask);

        if (updateTaskDTO.getAssignedDeveloperIds() != null) {
            List<Developer> developers = developerRepository.findAllById(updateTaskDTO.getAssignedDeveloperIds());
            existingTask.setAssignedDevelopers(developers);
        }

        Task updatedTask = taskRepository.save(existingTask);
        return taskMapper.toDto(updatedTask);
    }

    @Transactional
    public void deleteTask(Integer id) {
        if (!taskRepository.existsById(Long.valueOf(id))) {
            throw new EntityNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(Long.valueOf(id));
    }

    @Transactional
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