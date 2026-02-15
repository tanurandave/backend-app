package com.training.backend_app.controller;

import com.training.backend_app.dto.SlotRequest;
import com.training.backend_app.dto.SlotResponse;
import com.training.backend_app.dto.WeekRequest;
import com.training.backend_app.dto.WeekResponse;
import com.training.backend_app.service.SchedulingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduling")
@RequiredArgsConstructor
public class SchedulingController {

    private final SchedulingService schedulingService;

    @PostMapping("/weeks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WeekResponse> createWeek(@Valid @RequestBody WeekRequest request) {
        return ResponseEntity.ok(schedulingService.createWeek(request));
    }

    @GetMapping("/weeks")
    public ResponseEntity<List<WeekResponse>> getAllWeeks() {
        return ResponseEntity.ok(schedulingService.getAllWeeks());
    }

    @PostMapping("/weeks/{weekId}/slots")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SlotResponse> createSlot(@PathVariable("weekId") Long weekId,
            @Valid @RequestBody SlotRequest request) {
        return ResponseEntity.ok(schedulingService.createSlot(weekId, request));
    }

    @GetMapping("/weeks/{weekId}/slots")
    public ResponseEntity<List<SlotResponse>> getSlotsByWeekId(@PathVariable("weekId") Long weekId) {
        return ResponseEntity.ok(schedulingService.getSlotsByWeekId(weekId));
    }

    @GetMapping("/trainers/{trainerId}/slots")
    public ResponseEntity<List<SlotResponse>> getSlotsByTrainerId(@PathVariable("trainerId") Long trainerId) {
        return ResponseEntity.ok(schedulingService.getSlotsByTrainerId(trainerId));
    }

    @PutMapping("/weeks/{weekId}/slots/{slotId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SlotResponse> updateSlot(@PathVariable("weekId") Long weekId,
            @PathVariable("slotId") Long slotId, @Valid @RequestBody SlotRequest request) {
        return ResponseEntity.ok(schedulingService.updateSlot(slotId, request));
    }
}
