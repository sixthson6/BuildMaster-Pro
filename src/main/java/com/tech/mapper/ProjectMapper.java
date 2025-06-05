package com.tech.mapper;

import com.tech.dto.CreateProjectDTO;
import com.tech.dto.ProjectDTO;
import com.tech.model.Project;
import com.tech.model.Task;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ProjectMapper {

    public ProjectDTO toDto(Project project) {
        if (project == null) return null;

        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setDeadline(project.getDeadline());
        dto.setStatus(project.getStatus() != null ? project.getStatus().name() : null);
        if (project.getTasks() != null) {
           dto.setTaskIds(project.getTasks().stream().map(Task::getId).toList());
        }

        return dto;
    }

    public Project toEntity(CreateProjectDTO createProjectDTO) {
        if (createProjectDTO == null) return null;

        Project project = new Project();
        project.setName(createProjectDTO.getName());
        project.setDescription(createProjectDTO.getDescription());
        project.setDeadline(createProjectDTO.getDeadline());
        project.setStatus(mapStatus(createProjectDTO.getStatus()));
        return project;
    }

    public void updateEntityFromDto(CreateProjectDTO updateProjectDTO, Project project) {
        if (updateProjectDTO == null || project == null) return;

        project.setName(updateProjectDTO.getName());
        project.setDescription(updateProjectDTO.getDescription());
        project.setDeadline(updateProjectDTO.getDeadline());
        project.setStatus(mapStatus(updateProjectDTO.getStatus()));
    }

    private Project.ProjectStatus mapStatus(String status) {
        if (status == null) {
            return Project.ProjectStatus.ACTIVE;
        }
        try {
            return Project.ProjectStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Project.ProjectStatus.ACTIVE;
        }
    }
}
