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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

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
        return projectMapper.toDto(savedProject);
    }

    @Transactional
    public ProjectDTO updateProject(Long id, CreateProjectDTO updateProjectDTO) {
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));

        projectMapper.updateEntityFromDto(updateProjectDTO, existingProject);
        Project updatedProject = projectRepository.save(existingProject);
        return projectMapper.toDto(updatedProject);
    }

    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new EntityNotFoundException("Project not found with id: " + id);
        }
        projectRepository.deleteById(id);
    }
}
