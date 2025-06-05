package com.tech.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private String status;
    private LocalDate dueDate;
    private Long projectId;
    private String projectName;
    private List<DeveloperDTO> assignedDevelopers;
}
