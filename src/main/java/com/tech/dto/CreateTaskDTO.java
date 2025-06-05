package com.tech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateTaskDTO {
    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 500)
    private String description;

    private String status;

    private LocalDate dueDate;

    @NotNull
    private Long projectId;

    private List<Long> assignedDeveloperIds;
}
