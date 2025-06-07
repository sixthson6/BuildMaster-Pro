package com.tech.mappers;

import com.tech.dto.CreateTaskDTO;
import com.tech.dto.TaskDTO;
import com.tech.model.Task;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {DeveloperMapper.class})
public interface TaskMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "assignedDevelopers", source = "assignedDevelopers", qualifiedByName = "mapDevelopersWithoutTasks")
    @Mapping(target = "status", source = "status")
    TaskDTO toDto(Task task);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "assignedDevelopers", ignore = true)
    @Mapping(target = "status", expression = "java(mapStatus(createTaskDTO.getStatus()))")
    Task toEntity(CreateTaskDTO createTaskDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "assignedDevelopers", ignore = true)
    @Mapping(target = "status", expression = "java(mapStatus(updateTaskDTO.getStatus()))")
    void updateEntityFromDto(CreateTaskDTO updateTaskDTO, @MappingTarget Task task);

    @Named("mapDevelopersWithoutTasks")
    @Mapping(target = "tasks", ignore = true)
    com.tech.dto.DeveloperDTO mapDeveloperWithoutTasks(com.tech.model.Developer developer);

    default Task.TaskStatus mapStatus(String status) {
        if (status == null) {
            return Task.TaskStatus.TODO;
        }
        try {
            return Task.TaskStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Task.TaskStatus.TODO;
        }
    }

    default String mapStatus(Task.TaskStatus status) {
        return status != null ? status.name() : Task.TaskStatus.TODO.name();
    }
}
