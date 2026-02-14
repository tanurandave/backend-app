package com.training.backend_app.controller;

import com.training.backend_app.dto.BulkEnrollRequest;
import com.training.backend_app.dto.CourseResponse;
import com.training.backend_app.dto.EnrollmentRequest;
import com.training.backend_app.dto.EnrollmentResponse;
import com.training.backend_app.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> getAllEnrollments() {
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentResponse> enrollStudent(@Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.ok(enrollmentService.enrollStudent(request));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsByStudentId(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudentId(studentId));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsByCourseId(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourseId(courseId));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> bulkEnrollStudents(@Valid @RequestBody BulkEnrollRequest request) {
        return ResponseEntity.ok(enrollmentService.bulkEnrollStudents(request));
    }

    @GetMapping("/student/{studentId}/courses")
    public ResponseEntity<List<CourseResponse>> getStudentCourses(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentService.getStudentCourses(studentId));
    }
}
