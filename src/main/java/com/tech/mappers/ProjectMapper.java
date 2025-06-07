package com.tech.mappers;

import com.tech.dto.CreateProjectDTO;
import com.tech.dto.ProjectDTO;
import com.tech.model.Project;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {TaskMapper.class})
public interface ProjectMapper {

    @Mapping(target = "status", source = "status")
    ProjectDTO toDto(Project project);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "status", expression = "java(mapStatus(createProjectDTO.getStatus()))")
    Project toEntity(CreateProjectDTO createProjectDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "status", expression = "java(mapStatus(updateProjectDTO.getStatus()))")
    void updateEntityFromDto(CreateProjectDTO updateProjectDTO, @MappingTarget Project project);

    default Project.ProjectStatus mapStatus(String status) {
        if (status == null) {
            return Project.ProjectStatus.ACTIVE;
        }
        try {
            return Project.ProjectStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Project.ProjectStatus.ACTIVE;
        }
    }

    default String mapStatus(Project.ProjectStatus status) {
        return status != null ? status.name() : Project.ProjectStatus.ACTIVE.name();
    }
}
