package com.tech.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "developers")
@Data
@EqualsAndHashCode(exclude = "tasks")
@ToString(exclude = "tasks")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Developer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @Size(max = 500)
    private String skills;

    @ManyToMany(mappedBy = "assignedDevelopers")
    private List<Task> tasks;
}
