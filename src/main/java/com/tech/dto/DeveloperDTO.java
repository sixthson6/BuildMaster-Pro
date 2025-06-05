package com.tech.dto;

import lombok.Data;
import java.util.List;

@Data
public class DeveloperDTO {
    private Long id;
    private String name;
    private String email;
    private String skills;
//    private List<TaskDTO> tasks;
    private List<Long> taskIds;
}
