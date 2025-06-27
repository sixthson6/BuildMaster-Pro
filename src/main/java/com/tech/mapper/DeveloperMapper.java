package com.tech.mapper;

import com.tech.dto.CreateDeveloperDTO;
import com.tech.dto.DeveloperDTO;
import com.tech.dto.summary.DeveloperSummary;
import com.tech.model.Developer;
import com.tech.model.Task;
import org.springframework.stereotype.Component;

@Component
public class DeveloperMapper {

    public DeveloperDTO toDto(Developer developer) {
        if (developer == null) return null;

        DeveloperDTO dto = new DeveloperDTO();
        dto.setId(developer.getId());
        dto.setName(developer.getName());
        dto.setEmail(developer.getEmail());
        dto.setSkills(developer.getSkills());
        dto.setTaskIds(developer.getTasks() != null ? developer.getTasks().stream().map(Task::getId).toList() : null);
        return dto;
    }

    public DeveloperSummary toSummary(Developer developer) {
        if (developer == null) return null;

        DeveloperSummary dto = new DeveloperSummary();
        dto.setId(developer.getId());
        dto.setName(developer.getName());
        dto.setEmail(developer.getEmail());
        return dto;
    }

    public DeveloperSummary toSummaryDto(Developer developer) {
        if (developer == null) return null;

        DeveloperSummary dto = new DeveloperSummary();
        dto.setId(developer.getId());
        dto.setName(developer.getName());
        dto.setEmail(developer.getEmail());
        return dto;
    }

    public Developer toEntity(CreateDeveloperDTO createDeveloperDTO) {
        if (createDeveloperDTO == null) return null;

        Developer developer = new Developer();
        developer.setName(createDeveloperDTO.getName());
        developer.setEmail(createDeveloperDTO.getEmail());
        developer.setSkills(createDeveloperDTO.getSkills());
        return developer;
    }

    public void updateEntityFromDto(CreateDeveloperDTO updateDeveloperDTO, Developer developer) {
        if (updateDeveloperDTO == null || developer == null) return;

        developer.setName(updateDeveloperDTO.getName());
        developer.setEmail(updateDeveloperDTO.getEmail());
        developer.setSkills(updateDeveloperDTO.getSkills());
    }

    public DeveloperDTO toDtoWithoutTasks(Developer developer) {
        return toDto(developer);
    }
}