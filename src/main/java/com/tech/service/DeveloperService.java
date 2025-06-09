package com.tech.service;

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
    private final AuditLogService auditLogService;


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
        DeveloperDTO dto = developerMapper.toDto(savedDeveloper);
        auditLogService.logDeveloperAction("CREATE", dto);
        return dto;
    }

    @Transactional
    public DeveloperDTO updateDeveloper(Long id, CreateDeveloperDTO updateDeveloperDTO) {
        Developer existingDeveloper = developerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Developer not found with id: " + id));

        developerMapper.updateEntityFromDto(updateDeveloperDTO, existingDeveloper);
        Developer updatedDeveloper = developerRepository.save(existingDeveloper);
        DeveloperDTO dto = developerMapper.toDto(updatedDeveloper);
        auditLogService.logDeveloperAction("UPDATE", dto);
        return dto;
    }

    @Transactional
    public void deleteDeveloper(Long id) {
        if (!developerRepository.existsById(id)) {
            throw new EntityNotFoundException("Developer not found with id: " + id);
        }
        developerRepository.deleteById(id);
        auditLogService.logDeveloperAction("DELETE", new DeveloperDTO());
    }
}
