package com.training.backend_app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Module ID is required")
    private Long moduleId;

    @NotNull(message = "Trainer ID is required")
    private Long trainerId;

    @NotBlank(message = "Day of week is required")
    private String dayOfWeek;

    @NotNull(message = "Slot number is required")
    @Min(value = 1, message = "Slot number must be between 1 and 4")
    @Max(value = 4, message = "Slot number must be between 1 and 4")
    private Integer slotNumber;
}
