package com.training.backend_app.controller;

import com.training.backend_app.dto.BulkEnrollRequest;
import com.training.backend_app.dto.BulkUploadResponse;
import com.training.backend_app.dto.CourseResponse;
import com.training.backend_app.dto.EnrollmentRequest;
import com.training.backend_app.dto.EnrollmentResponse;
import com.training.backend_app.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<List<EnrollmentResponse>> getAllEnrollments() {
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentResponse> enrollStudent(@Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.ok(enrollmentService.enrollStudent(request));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsByStudentId(
            @PathVariable("studentId") Long studentId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudentId(studentId));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsByCourseId(@PathVariable("courseId") Long courseId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourseId(courseId));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> bulkEnrollStudents(@Valid @RequestBody BulkEnrollRequest request) {
        return ResponseEntity.ok(enrollmentService.bulkEnrollStudents(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable("id") Long id) {
        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkUploadResponse> bulkEnrollByFile(
            @RequestParam("courseId") Long courseId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(enrollmentService.bulkEnrollFromFile(courseId, file));
    }

    @GetMapping("/student/{studentId}/courses")
    public ResponseEntity<List<CourseResponse>> getStudentCourses(@PathVariable("studentId") Long studentId) {
        return ResponseEntity.ok(enrollmentService.getStudentCourses(studentId));
    }

    @PostMapping("/request")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<EnrollmentResponse> requestEnrollment(@Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.ok(enrollmentService.requestEnrollment(request.getStudentId(), request.getCourseId()));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentResponse> approveEnrollment(@PathVariable("id") Long id) {
        return ResponseEntity.ok(enrollmentService.approveEnrollment(id));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentResponse> rejectEnrollment(@PathVariable("id") Long id) {
        return ResponseEntity.ok(enrollmentService.rejectEnrollment(id));
    }
}
