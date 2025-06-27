package com.tech.controller;

import com.tech.dto.CreateTaskDTO;
import com.tech.dto.TaskDTO;
import com.tech.dto.summary.TaskSummary;
import com.tech.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER', 'CONTRACTOR')")
    public ResponseEntity<Page<TaskSummary>> getAllTasks(Pageable pageable) {
        Page<TaskSummary> tasks = taskService.getAllTasks(pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER', 'CONTRACTOR')")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        TaskDTO task = taskService.getTaskById(Math.toIntExact(id));
        return ResponseEntity.ok(task);
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER', 'CONTRACTOR')")
    public ResponseEntity<Page<TaskDTO>> getTasksByProject(@PathVariable Long projectId, Pageable pageable) {
        Page<TaskDTO> tasks = taskService.getTasksByProjectId(Math.toIntExact(projectId), pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/developer/{developerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER', 'CONTRACTOR')")
    public ResponseEntity<Page<TaskDTO>> getTasksByDeveloper(@PathVariable Long developerId, Pageable pageable) {
        Page<TaskDTO> tasks = taskService.getTasksByDeveloperId(Math.toIntExact(developerId), pageable);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody CreateTaskDTO createTaskDTO) {
        TaskDTO createdTask = taskService.createTask(createTaskDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN') or hasRole('DEVELOPER') and @accessChecker.isTaskOwner(#id)")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id,
                                              @Valid @RequestBody CreateTaskDTO updateTaskDTO) {
        TaskDTO updatedTask = taskService.updateTask(Math.toIntExact(id), updateTaskDTO);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(Math.toIntExact(id));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/assign/{developerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TaskDTO> assignDeveloperToTask(@PathVariable Long taskId,
                                                         @PathVariable Long developerId) {
        TaskDTO updatedTask = taskService.assignDeveloperToTask(Math.toIntExact(taskId), Math.toIntExact(developerId));
        return ResponseEntity.ok(updatedTask);
    }
}
