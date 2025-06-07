package com.tech.service;

import com.tech.auditlog.AuditService;
import com.tech.model.Developer;
import com.tech.repository.DeveloperRepository;
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
public class DeveloperService {

    private final DeveloperRepository developerRepository;
    private final AuditService auditService;

    public Developer createDeveloper(Developer developer, String actorName) {
        log.info("Creating new developer: {} by user: {}", developer.getName(), actorName);

        Developer savedDeveloper = developerRepository.save(developer);

        // Log the create action
        auditService.logAction("Developer", savedDeveloper.getId().toString(), "CREATE", actorName, savedDeveloper);

        log.info("Developer created successfully with ID: {}", savedDeveloper.getId());
        return savedDeveloper;
    }

    public Developer updateDeveloper(Long developerId, Developer updatedDeveloper, String actorName) {
        log.info("Updating developer with ID: {} by user: {}", developerId, actorName);

        Developer existingDeveloper = developerRepository.findById(developerId)
                .orElseThrow(() -> new EntityNotFoundException("Developer not found with ID: " + developerId));

        // Create a copy of the existing developer for audit logging
        Developer previousDeveloper = createDeveloperCopy(existingDeveloper);

        // Update the developer fields
        existingDeveloper.setName(updatedDeveloper.getName());
        existingDeveloper.setEmail(updatedDeveloper.getEmail());
        existingDeveloper.setRole(updatedDeveloper.getRole());
        existingDeveloper.setExperienceLevel(updatedDeveloper.getExperienceLevel());
        existingDeveloper.setSkills(updatedDeveloper.getSkills());
        existingDeveloper.setDepartment(updatedDeveloper.getDepartment());
        existingDeveloper.setSalary(updatedDeveloper.getSalary());
        existingDeveloper.setHireDate(updatedDeveloper.getHireDate());
        existingDeveloper.setStatus(updatedDeveloper.getStatus());

        Developer savedDeveloper = developerRepository.save(existingDeveloper);

        // Log the update action with both current and previous data
        auditService.logAction("Developer", developerId.toString(), "UPDATE", actorName, savedDeveloper, previousDeveloper);

        log.info("Developer updated successfully with ID: {}", developerId);
        return savedDeveloper;
    }

    public void deleteDeveloper(Long developerId, String actorName) {
        log.info("Deleting developer with ID: {} by user: {}", developerId, actorName);

        Developer existingDeveloper = developerRepository.findById(developerId)
                .orElseThrow(() -> new EntityNotFoundException("Developer not found with ID: " + developerId));

        developerRepository.deleteById(developerId);

        // Log the delete action
        auditService.logAction("Developer", developerId.toString(), "DELETE", actorName, null, existingDeveloper);

        log.info("Developer deleted successfully with ID: {}", developerId);
    }

    // Developer status update with audit logging
    public Developer updateDeveloperStatus(Long developerId, String newStatus, String actorName) {
        log.info("Updating developer status for ID: {} to {} by user: {}", developerId, newStatus, actorName);

        Developer existingDeveloper = developerRepository.findById(developerId)
                .orElseThrow(() -> new EntityNotFoundException("Developer not found with ID: " + developerId));

        Developer previousDeveloper = createDeveloperCopy(existingDeveloper);
        String oldStatus = existingDeveloper.getStatus();

        existingDeveloper.setStatus(newStatus);
        Developer savedDeveloper = developerRepository.save(existingDeveloper);

        // Log the status update
        auditService.logAction("Developer", developerId.toString(), "STATUS_UPDATE", actorName, savedDeveloper, previousDeveloper);

        log.info("Developer status updated from {} to {} for ID: {}", oldStatus, newStatus, developerId);
        return savedDeveloper;
    }

    // Salary update with special audit logging (sensitive data)
    public Developer updateDeveloperSalary(Long developerId, Double newSalary, String actorName) {
        log.info("Updating developer salary for ID: {} by user: {}", developerId, actorName);

        Developer existingDeveloper = developerRepository.findById(developerId)
                .orElseThrow(() -> new EntityNotFoundException("Developer not found with ID: " + developerId));

        Developer previousDeveloper = createDeveloperCopy(existingDeveloper);
        Double oldSalary = existingDeveloper.getSalary();

        existingDeveloper.setSalary(newSalary);
        Developer savedDeveloper = developerRepository.save(existingDeveloper);

        // Log the salary update with special action type
        auditService.logAction("Developer", developerId.toString(), "SALARY_UPDATE", actorName, savedDeveloper, previousDeveloper);

        log.info("Developer salary updated from {} to {} for ID: {}", oldSalary, newSalary, developerId);
        return savedDeveloper;
    }

    // Read operations (no audit logging needed)
    public Optional<Developer> getDeveloperById(Long developerId) {
        return developerRepository.findById(developerId);
    }

    public List<Developer> getAllDevelopers() {
        return developerRepository.findAll();
    }

    public Page<Developer> getDevelopersPaginated(Pageable pageable) {
        return developerRepository.findAll(pageable);
    }

    public List<Developer> getDevelopersByRole(String role) {
        return developerRepository.findByRole(role);
    }

    public List<Developer> getDevelopersByExperienceLevel(String experienceLevel) {
        return developerRepository.findByExperienceLevel(experienceLevel);
    }

    public List<Developer> getDevelopersByDepartment(String department) {
        return developerRepository.findByDepartment(department);
    }

    public List<Developer> getDevelopersByStatus(String status) {
        return developerRepository.findByStatus(status);
    }

    public List<Developer> getDevelopersBySkill(String skill) {
        return developerRepository.findBySkillsContaining(skill);
    }

    // Helper method to create a copy for audit logging
    private Developer createDeveloperCopy(Developer original) {
        Developer copy = new Developer();
        copy.setId(original.getId());
        copy.setName(original.getName());
        copy.setEmail(original.getEmail());
        copy.setRole(original.getRole());
        copy.setExperienceLevel(original.getExperienceLevel());
        copy.setSkills(original.getSkills() != null ? List.copyOf(original.getSkills()) : null);
        copy.setDepartment(original.getDepartment());
        copy.setSalary(original.getSalary());
        copy.setHireDate(original.getHireDate());
        copy.setStatus(original.getStatus());
        copy.setCreatedAt(original.getCreatedAt());
        copy.setUpdatedAt(original.getUpdatedAt());
        return copy;
    }
}