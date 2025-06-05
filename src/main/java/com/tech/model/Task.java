package com.tech.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "tasks")
@Data
@EqualsAndHashCode(exclude = {"project", "assignedDevelopers"})
@ToString(exclude = {"project", "assignedDevelopers"})
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.TODO;

    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @NotNull
    private Project project;

    @ManyToMany
    @JoinTable(
            name = "task_developers",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "developer_id")
    )
    private List<Developer> assignedDevelopers;

    public enum TaskStatus {
        TODO, IN_PROGRESS, REVIEW, DONE
    }
}