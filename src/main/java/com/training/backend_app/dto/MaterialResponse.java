package com.training.backend_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialResponse {
    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private String courseName;
    private Long moduleId;
    private String moduleName;
    private Long slotId;
    private Long trainerId;
    private String trainerName;
    private String fileName;
    private LocalDateTime createdAt;
}
