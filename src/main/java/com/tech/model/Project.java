package com.tech.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;
import java.time.LocalDate;

//@Builder
@Entity
@Table(name = "projects")
@Data
@EqualsAndHashCode(exclude = "tasks")
@ToString(exclude = "tasks")
//@NoArgsConstructor
//@AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull
//    @Builder.Default
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status = ProjectStatus.ACTIVE;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks;

    public enum ProjectStatus {
        ACTIVE, COMPLETED, ON_HOLD, CANCELLED
    }
}
