package com.tech.security.config;

import com.tech.model.Developer;
import com.tech.model.Project;
import com.tech.model.Project.ProjectStatus;
import com.tech.model.Task;
import com.tech.model.Task.TaskStatus;
import com.tech.security.model.Role;
import com.tech.security.model.Role.ERole;
import com.tech.security.repository.RoleRepository;
import com.tech.repository.DeveloperRepository;
import com.tech.repository.ProjectRepository;
import com.tech.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // Import for @Transactional

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final DeveloperRepository developerRepository; // Inject DeveloperRepository
    private final ProjectRepository projectRepository;     // Inject ProjectRepository
    private final TaskRepository taskRepository;           // Inject TaskRepository

    @Override
    @Transactional // Ensure all data loading happens in a single transaction
    public void run(String... args) throws Exception {
        // 1. Initialize Roles (Existing Logic)
        initializeRoles();

        // 2. Clear existing data (Optional, for fresh runs during development)
        // BE CAREFUL WITH THIS IN PRODUCTION ENVIRONMENTS!
        // taskRepository.deleteAll();
        // projectRepository.deleteAll();
        // developerRepository.deleteAll();
        // System.out.println("Cleared existing Project, Task, and Developer data.");


        // 3. Create Sample Developers (if not already existing)
        if (developerRepository.count() == 0) {
            Developer dev1 = new Developer();
            dev1.setName("Alice Wonderland");
            dev1.setEmail("alice.w@example.com");
            dev1.setSkills("Java, Spring Boot, Microservices");
            developerRepository.save(dev1);

            Developer dev2 = new Developer();
            dev2.setName("Bob The Builder");
            dev2.setEmail("bob.b@example.com");
            dev2.setSkills("Python, Django, AWS Lambda");
            developerRepository.save(dev2);

            Developer dev3 = new Developer();
            dev3.setName("Charlie Chaplin");
            dev3.setEmail("charlie.c@example.com");
            dev3.setSkills("Frontend, React, UI/UX");
            developerRepository.save(dev3);

            System.out.println("Sample Developers created.");
        }

        List<Developer> allDevelopers = developerRepository.findAll();
        if (allDevelopers.isEmpty()) {
            System.err.println("No developers available. Please ensure developers are created.");
            return; // Exit if no developers to assign tasks to
        }

        // 4. Create Sample Projects (if not already existing)
        if (projectRepository.count() == 0) {
            Project project1 = new Project();
            project1.setName("E-commerce Platform Revamp");
            project1.setDescription("Modernizing the entire e-commerce platform with new technologies.");
            project1.setDeadline(LocalDate.of(2025, 12, 31));
            project1.setStatus(ProjectStatus.ACTIVE);
            projectRepository.save(project1);

            Project project2 = new Project();
            project2.setName("Internal Tool Development");
            project2.setDescription("Building a new internal CRM tool for sales team.");
            project2.setDeadline(LocalDate.of(2025, 6, 30));
            project2.setStatus(ProjectStatus.ACTIVE);
            projectRepository.save(project2);

            System.out.println("Sample Projects created.");
        }

        List<Project> allProjects = projectRepository.findAll();
        if (allProjects.isEmpty()) {
            System.err.println("No projects available. Please ensure projects are created.");
            return; // Exit if no projects to assign tasks to
        }


        // 5. Create Sample Tasks (if not already existing)
        if (taskRepository.count() == 0) {
            // Task 1 for Project 1
            Task task1 = new Task();
            task1.setTitle("Implement User Authentication");
            task1.setDescription("Set up Spring Security with JWT for user login/registration.");
            task1.setStatus(TaskStatus.TODO);
            task1.setDueDate(LocalDate.of(2025, 7, 15));
            task1.setProject(allProjects.get(0)); // Assign to Project 1
            if (!allDevelopers.isEmpty()) {
                task1.setAssignedDevelopers(Collections.singletonList(allDevelopers.get(0))); // Assign to first dev
            }
            taskRepository.save(task1);

            // Task 2 for Project 1
            Task task2 = new Task();
            task2.setTitle("Design Database Schema");
            task2.setDescription("Create ER diagrams and define tables for new platform features.");
            task2.setStatus(TaskStatus.IN_PROGRESS);
            task2.setDueDate(LocalDate.of(2025, 7, 20));
            task2.setProject(allProjects.get(0)); // Assign to Project 1
            if (allDevelopers.size() > 1) {
                task2.setAssignedDevelopers(Arrays.asList(allDevelopers.get(0), allDevelopers.get(1))); // Assign to first and second dev
            }
            taskRepository.save(task2);

            // Task 3 for Project 1
            Task task3 = new Task();
            task3.setTitle("Develop Product Catalog Service");
            task3.setDescription("Build REST APIs for product catalog management.");
            task3.setStatus(TaskStatus.REVIEW);
            task3.setDueDate(LocalDate.of(2025, 8, 1));
            task3.setProject(allProjects.get(0)); // Assign to Project 1
            if (allDevelopers.size() > 2) {
                task3.setAssignedDevelopers(Collections.singletonList(allDevelopers.get(2))); // Assign to third dev
            }
            taskRepository.save(task3);

            // Task 4 for Project 2
            Task task4 = new Task();
            task4.setTitle("CRM UI Mockups");
            task4.setDescription("Create wireframes and mockups for the new CRM user interface.");
            task4.setStatus(TaskStatus.TODO);
            task4.setDueDate(LocalDate.of(2025, 7, 25));
            task4.setProject(allProjects.get(1)); // Assign to Project 2
            if (allDevelopers.size() > 2) {
                task4.setAssignedDevelopers(Collections.singletonList(allDevelopers.get(2))); // Assign to third dev
            }
            taskRepository.save(task4);

            // Task 5 for Project 2
            Task task5 = new Task();
            task5.setTitle("Sales Data Migration Script");
            task5.setDescription("Write Python script to migrate old sales data to new CRM database.");
            task5.setStatus(TaskStatus.IN_PROGRESS);
            task5.setDueDate(LocalDate.of(2025, 8, 10));
            task5.setProject(allProjects.get(1)); // Assign to Project 2
            if (allDevelopers.size() > 1) {
                task5.setAssignedDevelopers(Collections.singletonList(allDevelopers.get(1))); // Assign to second dev
            }
            taskRepository.save(task5);

            System.out.println("Sample Tasks created.");
        }

        System.out.println("Data initialization complete!");
    }

    private void initializeRoles() {
        // Ensure all roles exist before proceeding
        if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
            roleRepository.save(Role.builder().name(ERole.ROLE_USER).build());
        }
        if (roleRepository.findByName(ERole.ROLE_CONTRACTOR).isEmpty()) {
            roleRepository.save(Role.builder().name(ERole.ROLE_CONTRACTOR).build());
        }
        if (roleRepository.findByName(ERole.ROLE_DEVELOPER).isEmpty()) {
            roleRepository.save(Role.builder().name(ERole.ROLE_DEVELOPER).build());
        }
        if (roleRepository.findByName(ERole.ROLE_MANAGER).isEmpty()) {
            roleRepository.save(Role.builder().name(ERole.ROLE_MANAGER).build());
        }
        if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(Role.builder().name(ERole.ROLE_ADMIN).build());
        }
        if (roleRepository.findByName(ERole.ROLE_MODERATOR).isEmpty()) {
            roleRepository.save(Role.builder().name(ERole.ROLE_MODERATOR).build());
        }
        System.out.println("Roles initialized successfully!");
    }
}

