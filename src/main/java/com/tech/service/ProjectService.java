package com.tech.service;

import com.tech.dto.CreateProjectDTO;
import com.tech.dto.ProjectDTO;
import com.tech.mapper.ProjectMapper;
import com.tech.model.Project;
import com.tech.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tech.auditlog.AuditService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final AuditService auditService;

    public Page<ProjectDTO> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable)
                .map(projectMapper::toDto);
    }

    public ProjectDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
        return projectMapper.toDto(project);
    }

    @Transactional
    public ProjectDTO createProject(CreateProjectDTO createProjectDTO) {
        Project project = projectMapper.toEntity(createProjectDTO);
        Project savedProject = projectRepository.save(project);

        // Log the creation
        auditService.logAction("Project", savedProject.getId().toString(),
                "CREATE", getCurrentUser(), savedProject);

        return projectMapper.toDto(savedProject);
    }

    @Transactional
    public ProjectDTO updateProject(Long id, CreateProjectDTO updateProjectDTO) {
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));

        // Store previous state for audit
        Project previousState = new Project();
        previousState.setId(existingProject.getId());
        previousState.setName(existingProject.getName());
        previousState.setDescription(existingProject.getDescription());
        previousState.setDeadline(existingProject.getDeadline());
        previousState.setStatus(existingProject.getStatus());

        projectMapper.updateEntityFromDto(updateProjectDTO, existingProject);
        Project updatedProject = projectRepository.save(existingProject);

        // Log the update
        auditService.logAction("Project", updatedProject.getId().toString(),
                "UPDATE", getCurrentUser(), updatedProject, previousState);

        return projectMapper.toDto(updatedProject);
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));

        // Log the deletion before actually deleting
        auditService.logAction("Project", id.toString(),
                "DELETE", getCurrentUser(), project);

        projectRepository.deleteById(id);
    }

    private String getCurrentUser() {
        // In a real application, this would get the current authenticated user
        // For now, return a default value
        return "system";
    }
}