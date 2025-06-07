package com.tech.mappers;

import com.tech.dto.CreateDeveloperDTO;
import com.tech.dto.DeveloperDTO;
import com.tech.model.Developer;
import com.tech.repository.DeveloperRepository;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface DeveloperMapper {

    @Mapping(target = "tasks", ignore = true)
    DeveloperDTO toDto(Developer developer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    Developer toEntity(CreateDeveloperDTO createDeveloperDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    void updateEntityFromDto(CreateDeveloperDTO updateDeveloperDTO, @MappingTarget Developer developer);

    // Simple DTO without tasks for avoiding circular references
    @Mapping(target = "tasks", ignore = true)
    DeveloperDTO toDtoWithoutTasks(Developer developer);

    @Mapping(target = "tasks", ignore = true)
    DeveloperDTO toDto(DeveloperRepository developerRepository);
}
