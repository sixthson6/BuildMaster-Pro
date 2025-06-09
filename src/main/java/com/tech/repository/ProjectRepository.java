package com.tech.repository;

import com.tech.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tech.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByStatus(Project.ProjectStatus status, Pageable pageable);

    Page<Project> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Project> findByDeadlineBefore(LocalDate date, Pageable pageable);

    Page<Project> findByDeadlineBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.tasks WHERE p.id = :id")
    Project findByIdWithTasks(@Param("id") Long id);

    @Query("SELECT p FROM Project p WHERE p.status = 'ACTIVE' ORDER BY p.deadline ASC")
    List<Project> findActiveProjectsOrderByDeadline();

    long countByStatus(Project.ProjectStatus status);

    @Query("SELECT p FROM Project p WHERE p.deadline < CURRENT_DATE AND p.status NOT IN ('COMPLETED', 'CANCELLED')")
    Page<Project> findOverdueProjects(Pageable pageable);
}
