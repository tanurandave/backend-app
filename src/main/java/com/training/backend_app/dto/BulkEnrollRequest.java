package com.training.backend_app.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkEnrollRequest {

    @NotEmpty(message = "Student IDs list cannot be empty")
    private List<Long> studentIds;

    @NotNull(message = "Course ID is required")
    private Long courseId;
}
