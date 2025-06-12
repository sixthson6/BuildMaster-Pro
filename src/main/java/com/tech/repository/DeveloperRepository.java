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

    Optional<Developer> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Developer> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Developer> findBySkillsContainingIgnoreCase(String skills, Pageable pageable);

    @Query("SELECT DISTINCT d FROM Developer d LEFT JOIN FETCH d.tasks WHERE d.id = :id")
    Optional<Developer> findByIdWithTasks(@Param("id") Long id);

    @Query("SELECT DISTINCT d FROM Developer d JOIN d.tasks t WHERE t.project.id = :projectId")
    Page<Developer> findDevelopersByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    @Query("SELECT DISTINCT d FROM Developer d JOIN d.tasks t WHERE t.status IN ('TODO', 'IN_PROGRESS')")
    Page<Developer> findDevelopersWithActiveTasks(Pageable pageable);

    @Query("SELECT COUNT(t) FROM Task t JOIN t.assignedDevelopers d WHERE d.id = :developerId")
    long countTasksByDeveloperId(@Param("developerId") Long developerId);

    @Query("SELECT d FROM Developer d WHERE d.tasks IS EMPTY")
    Page<Developer> findDevelopersWithoutTasks(Pageable pageable);
}
