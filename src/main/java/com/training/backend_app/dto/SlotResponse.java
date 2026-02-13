package com.training.backend_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotResponse {

    private Long id;
    private Long weekId;
    private Long courseId;
    private String courseName;
    private Long moduleId;
    private String moduleName;
    private Long trainerId;
    private String trainerName;
    private String dayOfWeek;
    private Integer slotNumber;
}
