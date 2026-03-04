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
public class EnrollmentResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private String courseDescription;
    private Integer courseDuration;
    private LocalDateTime enrolledAt;
    private String status;
}
