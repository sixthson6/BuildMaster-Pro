package com.tech.service;

import com.tech.auditlog.AuditService;
import com.tech.dto.CreateDeveloperDTO;
import com.tech.dto.DeveloperDTO;
import com.tech.mapper.DeveloperMapper;
import com.tech.model.Developer;
import com.tech.repository.DeveloperRepository;
import org.springframework.cache.annotation.Cacheable;
import jakarta.persistence.EntityNotFoundException;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeveloperService {

    private final DeveloperRepository developerRepository;
    private final DeveloperMapper developerMapper;
    private final AuditService auditService;

    public Page<DeveloperDTO> getAllDevelopers(Pageable pageable) {
        return developerRepository.findAll(pageable)
                .map((java.util.function.Function<? super Developer, ? extends DeveloperDTO>) developerMapper::toDto);
    }

    @Cacheable("developers")
    public DeveloperDTO getDeveloperById(Long id) {
        Developer developer = developerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Developer not found with id: " + id));
        return developerMapper.toDto(developer);
    }

    @Transactional
    public DeveloperDTO createDeveloper(CreateDeveloperDTO createDeveloperDTO) {
        Developer developer = developerMapper.toEntity(createDeveloperDTO);
        Developer savedDeveloper = developerRepository.save(developer);

        // Log the creation
        auditService.logAction("Developer", savedDeveloper.getId().toString(),
                "CREATE", getCurrentUser(), savedDeveloper);

        return developerMapper.toDto(savedDeveloper);
    }

    @Transactional
    public DeveloperDTO updateDeveloper(Long id, CreateDeveloperDTO updateDeveloperDTO) {
        Developer existingDeveloper = developerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Developer not found with id: " + id));

        // Store previous state for audit
        Developer previousState = Developer.builder()
                .id(existingDeveloper.getId())
                .name(existingDeveloper.getName())
                .email(existingDeveloper.getEmail())
                .skills(existingDeveloper.getSkills())
                .build();

        developerMapper.updateEntityFromDto(updateDeveloperDTO, existingDeveloper);
        Developer updatedDeveloper = developerRepository.save(existingDeveloper);

        // Log the update
        auditService.logAction("Developer", updatedDeveloper.getId().toString(),
                "UPDATE", getCurrentUser(), updatedDeveloper, previousState);

        return developerMapper.toDto(updatedDeveloper);
    }

    @Transactional
    public void deleteDeveloper(Long id) {
        Developer developer = developerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Developer not found with id: " + id));

        // Log the deletion before actually deleting
        auditService.logAction("Developer", id.toString(),
                "DELETE", getCurrentUser(), developer);

        developerRepository.deleteById(id);
    }

    private String getCurrentUser() {
        // In a real application, this would get the current authenticated user
        // For now, return a default value
        return "system";
    }
}