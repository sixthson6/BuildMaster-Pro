package com.tech.security.service;

import com.tech.repository.DeveloperRepository;
import com.tech.repository.TaskRepository;
import com.tech.model.Developer;
import com.tech.model.Task;
import com.tech.security.model.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.List; // Import List

/**
 * AccessChecker provides custom security logic for Spring Security's @PreAuthorize
 * expressions. It evaluates whether the currently authenticated user has
 * ownership or specific rights over a resource.
 */
@Component
@RequiredArgsConstructor
public class AccessChecker {

    private static final Logger logger = LoggerFactory.getLogger(AccessChecker.class);

    private final TaskRepository taskRepository;
    private final DeveloperRepository developerRepository;

    public boolean isTaskOwner(Long taskId) {
        // 1. Get the current authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("AccessChecker: No authenticated user found for task ownership check.");
            return false;
        }

        String currentUserEmail;
        if (authentication.getPrincipal() instanceof User) {
            currentUserEmail = ((User) authentication.getPrincipal()).getEmail();
        } else if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            currentUserEmail = ((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal()).getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            currentUserEmail = (String) authentication.getPrincipal();
            if ("anonymousUser".equals(currentUserEmail)) {
                logger.warn("AccessChecker: Anonymous user attempting task ownership check.");
                return false;
            }
        } else {
            logger.error("AccessChecker: Unexpected principal type for task ownership check: {}", authentication.getPrincipal().getClass().getName());
            return false;
        }

        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            logger.warn("AccessChecker: Current user email is null or empty for task ownership check.");
            return false;
        }

        logger.debug("AccessChecker: Checking ownership for task ID {} by user with email {}", taskId, currentUserEmail);

        Optional<Task> taskOptional = taskRepository.findById(taskId);
        if (taskOptional.isEmpty()) {
            logger.warn("AccessChecker: Task with ID {} not found for ownership check.", taskId);
            return false;
        }
        Task task = taskOptional.get();

        Optional<Developer> currentDeveloperOptional = developerRepository.findByEmail(currentUserEmail);
        if (currentDeveloperOptional.isEmpty()) {
            logger.warn("AccessChecker: No Developer entity found matching current user's email: {}", currentUserEmail);
            return false;
        }
        Developer currentDeveloper = currentDeveloperOptional.get();

        List<Developer> assignedDevelopers = task.getAssignedDevelopers();
        if (assignedDevelopers == null || assignedDevelopers.isEmpty()) {
            logger.warn("AccessChecker: Task ID {} has no assigned developers.", taskId);
            return false;
        }

        boolean isOwner = assignedDevelopers.stream()
                .anyMatch(dev -> dev.getId().equals(currentDeveloper.getId()));

        logger.info("AccessChecker: User {} (Developer ID: {}) is owner of task {}? {}",
                currentUserEmail, currentDeveloper.getId(), taskId, isOwner);
        return isOwner;
    }
}
