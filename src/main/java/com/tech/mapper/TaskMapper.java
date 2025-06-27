package com.tech.mapper;

import com.tech.dto.CreateTaskDTO;
import com.tech.dto.DeveloperDTO;
import com.tech.dto.TaskDTO;
import com.tech.dto.summary.TaskSummary;
import com.tech.model.Developer;
import com.tech.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskMapper {

    @Autowired
    private DeveloperMapper developerMapper;

    public TaskDTO toDto(Task task) {
        if (task == null) return null;

        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus() != null ? task.getStatus().name() : null);
        dto.setDueDate(task.getDueDate());

        if (task.getProject() != null) {
            dto.setProjectId(task.getProject().getId());
            dto.setProjectName(task.getProject().getName());
        }

        if (task.getAssignedDevelopers() != null) {
            List<DeveloperDTO> developers = task.getAssignedDevelopers().stream()
                    .map(developerMapper::toDtoWithoutTasks)
                    .collect(Collectors.toList());
            dto.setAssignedDevelopers(developers);
        }

        return dto;
    }

    public TaskSummary toSummary(Task task) {
        if (task == null) return null;

        TaskSummary dto = new TaskSummary();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setStatus(task.getStatus() != null ? task.getStatus().name() : null);
        dto.setDueDate(task.getDueDate());

        if (task.getProject() != null) {
            dto.setProjectId(task.getProject().getId());
            dto.setProjectName(task.getProject().getName());
        }

        if (task.getAssignedDevelopers() != null && !task.getAssignedDevelopers().isEmpty()) {
            dto.setAssignedDeveloperNames(task.getAssignedDevelopers().stream()
                    .map(Developer::getName)
                    .collect(Collectors.toList()));
        } else {
            dto.setAssignedDeveloperNames(Collections.emptyList());
        }

        return dto;
    }

    public Task toEntity(CreateTaskDTO createTaskDTO) {
        if (createTaskDTO == null) return null;

        Task task = new Task();
        task.setTitle(createTaskDTO.getTitle());
        task.setDescription(createTaskDTO.getDescription());
        task.setStatus(mapStatus(createTaskDTO.getStatus()));
        task.setDueDate(createTaskDTO.getDueDate());
        return task;
    }

    public void updateEntityFromDto(CreateTaskDTO updateTaskDTO, Task task) {
        if (updateTaskDTO == null || task == null) return;

        task.setTitle(updateTaskDTO.getTitle());
        task.setDescription(updateTaskDTO.getDescription());
        task.setStatus(mapStatus(updateTaskDTO.getStatus()));
        task.setDueDate(updateTaskDTO.getDueDate());
    }

    public DeveloperDTO mapDeveloperWithoutTasks(Developer developer) {
        return developerMapper.toDtoWithoutTasks(developer);
    }

    private Task.TaskStatus mapStatus(String status) {
        if (status == null) {
            return Task.TaskStatus.TODO;
        }
        try {
            return Task.TaskStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Task.TaskStatus.TODO;
        }
    }
}
