package com.training.backend_app.controller;

import com.training.backend_app.dto.CourseRequest;
import com.training.backend_app.dto.CourseResponse;
import com.training.backend_app.dto.ModuleRequest;
import com.training.backend_app.dto.ModuleResponse;
import com.training.backend_app.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.createCourse(request));
    }

    @GetMapping
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @PostMapping("/{courseId}/modules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleResponse> addModule(@PathVariable Long courseId, @Valid @RequestBody ModuleRequest request) {
        return ResponseEntity.ok(courseService.addModule(courseId, request));
    }

    @GetMapping("/{courseId}/modules")
    public ResponseEntity<List<ModuleResponse>> getModulesByCourseId(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.getModulesByCourseId(courseId));
    }
}
