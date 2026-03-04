package com.training.backend_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableResponse {

    private Long slotId;
    private Long weekId;
    private Integer weekNumber;
    private Long courseId;
    private String courseName;
    private Long moduleId;
    private String moduleName;
    private Long trainerId;
    private String trainerName;
    private String dayOfWeek;
    private Integer slotNumber;
}
