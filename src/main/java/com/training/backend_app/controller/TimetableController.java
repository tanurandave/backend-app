package com.training.backend_app.controller;

import com.training.backend_app.dto.TimetableResponse;
import com.training.backend_app.service.TimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<TimetableResponse>> getStudentTimetable(@PathVariable("studentId") Long studentId) {
        return ResponseEntity.ok(timetableService.getStudentTimetable(studentId));
    }
}
