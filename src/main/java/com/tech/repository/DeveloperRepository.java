package com.tech.repository;

import com.tech.model.Developer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Long> {

    // Find developer by email
    Optional<Developer> findByEmail(String email);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find developers by name containing (case insensitive)
    Page<Developer> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Find developers by skills containing (case insensitive)
    Page<Developer> findBySkillsContainingIgnoreCase(String skills, Pageable pageable);

    // Custom query to find developers with their tasks
    @Query("SELECT DISTINCT d FROM Developer d LEFT JOIN FETCH d.tasks WHERE d.id = :id")
    Optional<Developer> findByIdWithTasks(@Param("id") Long id);

    // Find developers assigned to a specific project
    @Query("SELECT DISTINCT d FROM Developer d JOIN d.tasks t WHERE t.project.id = :projectId")
    Page<Developer> findDevelopersByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    // Find developers with active tasks
    @Query("SELECT DISTINCT d FROM Developer d JOIN d.tasks t WHERE t.status IN ('TODO', 'IN_PROGRESS')")
    Page<Developer> findDevelopersWithActiveTasks(Pageable pageable);

    // Count tasks for a developer
    @Query("SELECT COUNT(t) FROM Task t JOIN t.assignedDevelopers d WHERE d.id = :developerId")
    long countTasksByDeveloperId(@Param("developerId") Long developerId);

    // Find developers with no assigned tasks
    @Query("SELECT d FROM Developer d WHERE d.tasks IS EMPTY")
    Page<Developer> findDevelopersWithoutTasks(Pageable pageable);
}
