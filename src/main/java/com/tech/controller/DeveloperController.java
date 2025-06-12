package com.tech.controller;

import com.tech.dto.CreateDeveloperDTO;
import com.tech.dto.DeveloperDTO;
import com.tech.service.DeveloperService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/developers")
@RequiredArgsConstructor
public class DeveloperController {

    private final DeveloperService developerService;
    @Cacheable
    @GetMapping
    public ResponseEntity<Page<DeveloperDTO>> getAllDevelopers(Pageable pageable) {
        Page<DeveloperDTO> developers = developerService.getAllDevelopers(pageable);
        return ResponseEntity.ok(developers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeveloperDTO> getDeveloperById(@PathVariable Long id) {
        DeveloperDTO developer = developerService.getDeveloperById(id);
        return ResponseEntity.ok(developer);
    }

    @PostMapping
    public ResponseEntity<DeveloperDTO> createDeveloper(@Valid @RequestBody CreateDeveloperDTO createDeveloperDTO) {
        DeveloperDTO createdDeveloper = developerService.createDeveloper(createDeveloperDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDeveloper);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeveloperDTO> updateDeveloper(@PathVariable Long id,
                                                        @Valid @RequestBody CreateDeveloperDTO updateDeveloperDTO) {
        DeveloperDTO updatedDeveloper = developerService.updateDeveloper(id, updateDeveloperDTO);
        return ResponseEntity.ok(updatedDeveloper);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeveloper(@PathVariable Long id) {
        developerService.deleteDeveloper(id);
        return ResponseEntity.noContent().build();
    }
}