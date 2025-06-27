package com.tech.service;

import com.tech.dto.CreateProjectDTO;
import com.tech.dto.ProjectDTO;
import com.tech.dto.summary.ProjectSummary;
import com.tech.mapper.ProjectMapper;
import com.tech.model.Project;
import com.tech.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final AuditLogService auditLogService;

    @Cacheable(value = "projectsList", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public Page<ProjectSummary> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable)
                .map(projectMapper::toSummary);
    }

    @Cacheable(value = "projectsList", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public ProjectDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
        return projectMapper.toDto(project);
    }

    @Transactional
    @CachePut(value = "projectById", key = "#result.id")
    @CacheEvict(value = "projectsList", allEntries = true)
    public ProjectDTO createProject(CreateProjectDTO createProjectDTO) {
        Project project = projectMapper.toEntity(createProjectDTO);
        Project savedProject = projectRepository.save(project);
        ProjectDTO dto = projectMapper.toDto(savedProject);
        auditLogService.logProjectAction("CREATE", dto);
        return dto;
    }

    @Transactional
    @CachePut(value = "projectById", key = "#id")
    @CacheEvict(value = "projectsList", allEntries = true)
    public ProjectDTO updateProject(Long id, CreateProjectDTO updateProjectDTO) {
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));

        projectMapper.updateEntityFromDto(updateProjectDTO, existingProject);
        Project updatedProject = projectRepository.save(existingProject);
        ProjectDTO dto = projectMapper.toDto(updatedProject);
        auditLogService.logProjectAction("UPDATE", dto);
        return dto;
    }

    @Transactional
    @CacheEvict(value = {"projectById", "projectsList"}, key = "#id", allEntries = true)
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
        ProjectDTO dto = projectMapper.toDto(project);
        projectRepository.deleteById(id);
        auditLogService.logProjectAction("DELETE", dto);
    }
}
