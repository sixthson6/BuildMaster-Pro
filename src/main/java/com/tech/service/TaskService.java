package com.tech.service;

import com.tech.auditlog.AuditService;
import com.tech.model.Task;
import com.tech.repository.TaskRepository;
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
public class TaskService {

    private final TaskRepository taskRepository;
    private final AuditService auditService;

    public Task createTask(Task task, String actorName) {
        log.info("Creating new task: {} by user: {}", task.getTitle(), actorName);

        Task savedTask = taskRepository.save(task);

        // Log the create action
        auditService.logAction("Task", savedTask.getId().toString(), "CREATE", actorName, savedTask);

        log.info("Task created successfully with ID: {}", savedTask.getId());
        return savedTask;
    }

    public Task updateTask(Long taskId, Task updatedTask, String actorName) {
        log.info("Updating task with ID: {} by user: {}", taskId, actorName);

        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + taskId));

        // Create a copy of the existing task for audit logging
        Task previousTask = createTaskCopy(existingTask);

        // Update the task fields
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());
//        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setDueDate(updatedTask.getDueDate());
//        existingTask.setAssignedTo(updatedTask.getAssignedTo());
//        existingTask.setEstimatedHours(updatedTask.getEstimatedHours());
//        existingTask.setActualHours(updatedTask.getActualHours());
//        existingTask.setProjectId(updatedTask.getProjectId());

        Task savedTask = taskRepository.save(existingTask);

        // Log the update action with both current and previous data
        auditService.logAction("Task", taskId.toString(), "UPDATE", actorName, savedTask, previousTask);

        log.info("Task updated successfully with ID: {}", taskId);
        return savedTask;
    }

    public void deleteTask(Long taskId, String actorName) {
        log.info("Deleting task with ID: {} by user: {}", taskId, actorName);

        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + taskId));

        taskRepository.deleteById(taskId);

        // Log the delete action
        auditService.logAction("Task", taskId.toString(), "DELETE", actorName, null, existingTask);

        log.info("Task deleted successfully with ID: {}", taskId);
    }

    // Task status update with audit logging
    public Task updateTaskStatus(Long taskId, String newStatus, String actorName) {
        log.info("Updating task status for ID: {} to {} by user: {}", taskId, newStatus, actorName);

        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + taskId));

        Task previousTask = createTaskCopy(existingTask);
        String oldStatus = existingTask.getStatus();

        existingTask.setStatus(newStatus);
        Task savedTask = taskRepository.save(existingTask);

        // Log the status update with specific description
        auditService.logAction("Task", taskId.toString(), "STATUS_UPDATE", actorName, savedTask, previousTask);

        log.info("Task status updated from {} to {} for ID: {}", oldStatus, newStatus, taskId);
        return savedTask;
    }

    // Read operations (no audit logging needed)
    public Optional<Task> getTaskById(Long taskId) {
        return taskRepository.findById(taskId);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Page<Task> getTasksPaginated(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    public List<Task> getTasksByStatus(String status) {
        return taskRepository.findByStatus(status);
    }

    public List<Task> getTasksByAssignedTo(String assignedTo) {
        return taskRepository.findByAssignedTo(assignedTo);
    }

    public List<Task> getTasksByProjectId(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    public List<Task> getTasksByPriority(String priority) {
        return taskRepository.findByPriority(priority);
    }

    // Helper method to create a copy for audit logging
    private Task createTaskCopy(Task original) {
        Task copy = new Task();
        copy.setId(original.getId());
        copy.setTitle(original.getTitle());
        copy.setDescription(original.getDescription());
        copy.setStatus(original.getStatus());
//        copy.setPriority(original.getPriority());
        copy.setDueDate(original.getDueDate());
//        copy.setAssignedTo(original.getAssignedTo());
//        copy.setEstimatedHours(original.getEstimatedHours());
//        copy.setActualHours(original.getActualHours());
//        copy.setProjectId(original.getProjectId());
//        copy.setCreatedAt(original.getCreatedAt());
//        copy.setUpdatedAt(original.getUpdatedAt());
//        return copy;
    }
}