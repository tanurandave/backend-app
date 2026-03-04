package com.training.backend_app.controller;

import com.training.backend_app.dto.AssignmentResponse;
import com.training.backend_app.dto.MaterialResponse;
import com.training.backend_app.dto.SubmissionResponse;
import com.training.backend_app.entity.Assignment;
import com.training.backend_app.entity.Material;
import com.training.backend_app.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping("/assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<AssignmentResponse> createAssignment(
            @RequestParam("trainerId") Long trainerId,
            @RequestParam("courseId") Long courseId,
            @RequestParam(value = "moduleId", required = false) Long moduleId,
            @RequestParam(value = "slotId", required = false) Long slotId,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "dueDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDate,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {

        return ResponseEntity.ok(resourceService.createAssignment(trainerId, courseId, moduleId, slotId, title,
                description, dueDate, file));
    }

    @PostMapping("/materials")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<MaterialResponse> createMaterial(
            @RequestParam("trainerId") Long trainerId,
            @RequestParam("courseId") Long courseId,
            @RequestParam(value = "moduleId", required = false) Long moduleId,
            @RequestParam(value = "slotId", required = false) Long slotId,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {

        return ResponseEntity
                .ok(resourceService.createMaterial(trainerId, courseId, moduleId, slotId, title, description, file));
    }

    @GetMapping("/assignments/course/{courseId}")
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsByCourse(@PathVariable("courseId") Long courseId) {
        return ResponseEntity.ok(resourceService.getAssignmentsByCourse(courseId));
    }

    @DeleteMapping("/assignments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<Void> deleteAssignment(@PathVariable("id") Long id) {
        resourceService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/materials/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<Void> deleteMaterial(@PathVariable("id") Long id) {
        resourceService.deleteMaterial(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/materials/course/{courseId}")
    public ResponseEntity<List<MaterialResponse>> getMaterialsByCourse(@PathVariable("courseId") Long courseId) {
        return ResponseEntity.ok(resourceService.getMaterialsByCourse(courseId));
    }

    @GetMapping("/assignments/trainer/{trainerId}")
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsByTrainer(@PathVariable("trainerId") Long trainerId) {
        return ResponseEntity.ok(resourceService.getAssignmentsByTrainer(trainerId));
    }

    @GetMapping("/materials/trainer/{trainerId}")
    public ResponseEntity<List<MaterialResponse>> getMaterialsByTrainer(@PathVariable("trainerId") Long trainerId) {
        return ResponseEntity.ok(resourceService.getMaterialsByTrainer(trainerId));
    }

    @PostMapping("/assignments/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubmissionResponse> submitAssignment(
            @PathVariable("id") Long assignmentId,
            @RequestParam("studentId") Long studentId,
            @RequestParam(value = "comment", required = false) String comment,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        return ResponseEntity.ok(resourceService.submitAssignment(assignmentId, studentId, comment, file));
    }

    @GetMapping("/assignments/{id}/submissions")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<List<SubmissionResponse>> getSubmissions(@PathVariable("id") Long assignmentId) {
        return ResponseEntity.ok(resourceService.getSubmissionsByAssignment(assignmentId));
    }

    @GetMapping("/assignments/{id}/download")
    public ResponseEntity<byte[]> downloadAssignment(@PathVariable("id") Long id) {
        Assignment assignment = resourceService.getAssignmentEntity(id);
        if (assignment.getFileData() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + assignment.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(
                        assignment.getContentType() != null ? assignment.getContentType() : "application/octet-stream"))
                .body(assignment.getFileData());
    }

    @GetMapping("/materials/{id}/download")
    public ResponseEntity<byte[]> downloadMaterial(@PathVariable("id") Long id) {
        Material material = resourceService.getMaterialEntity(id);
        if (material.getFileData() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + material.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(
                        material.getContentType() != null ? material.getContentType() : "application/octet-stream"))
                .body(material.getFileData());
    }
}
