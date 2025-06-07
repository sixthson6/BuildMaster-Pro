package com.tech.service;

import com.tech.auditlog.AuditService;
import com.tech.model.Project;
import com.tech.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AuditService auditService;

    public Project createProject(Project project, String actorName) {
        log.info("Creating new project: {} by user: {}", project.getName(), actorName);

        Project savedProject = projectRepository.save(project);

        // Log the create action - only current data, no previous data
        auditService.logAction("Project", savedProject.getId().toString(), "CREATE", actorName, savedProject);

        log.info("Project created successfully with ID: {}", savedProject.getId());
        return savedProject;
    }

    public Project updateProject(Long projectId, Project updatedProject, String actorName) {
        log.info("Updating project with ID: {} by user: {}", projectId, actorName);

        Project existingProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with ID: " + projectId));

        // Create a copy of the existing project for audit logging
        Project previousProject = createProjectCopy(existingProject);

        // Update the project fields
        existingProject.setName(updatedProject.getName());
        existingProject.setDescription(updatedProject.getDescription());
        existingProject.setStartDate(updatedProject.getStartDate());
        existingProject.setEndDate(updatedProject.getEndDate());
        existingProject.setStatus(updatedProject.getStatus());
        existingProject.setBudget(updatedProject.getBudget());
        existingProject.setTeamLead(updatedProject.getTeamLead());

        Project savedProject = projectRepository.save(existingProject);

        // Log the update action with both current and previous data
        auditService.logAction("Project", projectId.toString(), "UPDATE", actorName, savedProject, previousProject);

        log.info("Project updated successfully with ID: {}", projectId);
        return savedProject;
    }

    public void deleteProject(Long projectId, String actorName) {
        log.info("Deleting project with ID: {} by user: {}", projectId, actorName);

        Project existingProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with ID: " + projectId));

        projectRepository.deleteById(projectId);

        // Log the delete action - no current data, only previous data
        auditService.logAction("Project", projectId.toString(), "DELETE", actorName, null, existingProject);

        log.info("Project deleted successfully with ID: {}", projectId);
    }

    // Read operations (no audit logging needed)
    public Optional<Project> getProjectById(Long projectId) {
        return projectRepository.findById(projectId);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Page<Project> getProjectsPaginated(Pageable pageable) {
        return projectRepository.findAll(pageable);
    }

    public List<Project> getProjectsByStatus(String status) {
        return projectRepository.findByStatus(status);
    }

    public List<Project> getProjectsByTeamLead(String teamLead) {
        return projectRepository.findByTeamLead(teamLead);
    }

    // Helper method to create a copy for audit logging
    private Project createProjectCopy(Project original) {
        Project copy = new Project();
        copy.setId(original.getId());
        copy.setName(original.getName());
        copy.setDescription(original.getDescription());
        copy.setStartDate(original.getStartDate());
        copy.setEndDate(original.getEndDate());
        copy.setStatus(original.getStatus());
        copy.setBudget(original.getBudget());
        copy.setTeamLead(original.getTeamLead());
        copy.setCreatedAt(original.getCreatedAt());
        copy.setUpdatedAt(original.getUpdatedAt());
        return copy;
    }
}