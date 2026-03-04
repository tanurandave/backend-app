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
public class SubmissionResponse {
    private Long id;
    private Long assignmentId;
    private String assignmentTitle;
    private Long studentId;
    private String studentName;
    private String status;
    private String comment;
    private String fileName;
    private LocalDateTime submittedAt;
}
