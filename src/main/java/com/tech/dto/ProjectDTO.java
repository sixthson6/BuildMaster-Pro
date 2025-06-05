package com.tech.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDate deadline;
    private String status;
//    private List<TaskDTO> tasks;
    private List<Long> taskIds;
}
