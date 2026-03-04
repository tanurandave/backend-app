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
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @PostMapping("/{courseId}/modules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleResponse> addModule(@PathVariable("courseId") Long courseId,
            @Valid @RequestBody ModuleRequest request) {
        return ResponseEntity.ok(courseService.addModule(courseId, request));
    }

    @GetMapping("/{courseId}/modules")
    public ResponseEntity<List<ModuleResponse>> getModulesByCourseId(@PathVariable("courseId") Long courseId) {
        return ResponseEntity.ok(courseService.getModulesByCourseId(courseId));
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateCourse(@PathVariable("courseId") Long courseId,
            @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(courseId, request));
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable("courseId") Long courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{courseId}/trainers")
    public ResponseEntity<List<com.training.backend_app.dto.UserResponse>> getTrainersByCourseId(
            @PathVariable("courseId") Long courseId) {
        return ResponseEntity.ok(courseService.getTrainersByCourseId(courseId));
    }

    @DeleteMapping("/modules/{moduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteModule(@PathVariable("moduleId") Long moduleId) {
        courseService.deleteModule(moduleId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/modules/{moduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleResponse> updateModule(@PathVariable("moduleId") Long moduleId,
            @Valid @RequestBody ModuleRequest request) {
        return ResponseEntity.ok(courseService.updateModule(moduleId, request));
    }

    @PostMapping("/{courseId}/syllabus")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> uploadSyllabus(@PathVariable("courseId") Long courseId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(courseService.uploadSyllabus(courseId, file));
    }

    @GetMapping("/{courseId}/syllabus")
    public ResponseEntity<byte[]> downloadSyllabus(@PathVariable("courseId") Long courseId) {
        com.training.backend_app.entity.Course course = courseService.getSyllabusData(courseId);
        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"" + course.getSyllabusFileName() + "\"")
                .header("Content-Type", course.getSyllabusContentType())
                .body(course.getSyllabusData());
    }

    @DeleteMapping("/{courseId}/syllabus")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> deleteSyllabus(@PathVariable("courseId") Long courseId) {
        return ResponseEntity.ok(courseService.deleteSyllabus(courseId));
    }

    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<List<CourseResponse>> getCoursesByTrainerId(@PathVariable("trainerId") Long trainerId) {
        return ResponseEntity.ok(courseService.getCoursesByTrainerId(trainerId));
    }
}
