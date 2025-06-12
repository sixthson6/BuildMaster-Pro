package com.tech.repository;

import com.tech.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    Page<Task> findByAssignedDevelopersId(Long developerId, Pageable pageable);

    Page<Task> findByStatus(Task.TaskStatus status, Pageable pageable);

    Page<Task> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Task> findByDueDateBefore(LocalDate date, Pageable pageable);

    Page<Task> findByDueDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Task> findByProjectIdAndStatus(Long projectId, Task.TaskStatus status, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.dueDate < CURRENT_DATE AND t.status NOT IN ('DONE')")
    Page<Task> findOverdueTasks(Pageable pageable);

    @Query("SELECT t FROM Task t JOIN t.assignedDevelopers d WHERE d.id = :developerId AND t.project.id = :projectId")
    Page<Task> findByDeveloperIdAndProjectId(@Param("developerId") Long developerId,
                                             @Param("projectId") Long projectId,
                                             Pageable pageable);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
    long countByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") Task.TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.assignedDevelopers IS EMPTY")
    Page<Task> findUnassignedTasks(Pageable pageable);

    @Query("SELECT t FROM Task t WHERE SIZE(t.assignedDevelopers) > 1")
    Page<Task> findTasksWithMultipleDevelopers(Pageable pageable);

    @Query("SELECT new map(t.status as status, COUNT(t) as count) FROM Task t WHERE t.project.id = :projectId GROUP BY t.status")
    List<Object> getTaskStatisticsByProject(@Param("projectId") Long projectId);

    @Query("SELECT t FROM Task t WHERE t.project.status = :projectStatus")
    Page<Task> findByProjectStatus(@Param("projectStatus") com.tech.model.Project.ProjectStatus projectStatus, Pageable pageable);
}
