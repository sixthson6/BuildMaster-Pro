package com.tech.dto.summary;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TaskSummary {
    private Long id;
    private String title;
    private String status;
    private LocalDate dueDate;
    private Long projectId;
    private String projectName;
    private List<String> assignedDeveloperNames;
}
