package com.training.backend_app.service;

import com.training.backend_app.dto.AssignmentResponse;
import com.training.backend_app.dto.MaterialResponse;
import com.training.backend_app.dto.SubmissionResponse;
import com.training.backend_app.entity.Assignment;
import com.training.backend_app.entity.Material;
import com.training.backend_app.entity.Submission;
import com.training.backend_app.entity.Course;
import com.training.backend_app.entity.Module;
import com.training.backend_app.entity.Slot;
import com.training.backend_app.entity.User;
import com.training.backend_app.repository.AssignmentRepository;
import com.training.backend_app.repository.MaterialRepository;
import com.training.backend_app.repository.SubmissionRepository;
import com.training.backend_app.repository.CourseRepository;
import com.training.backend_app.repository.ModuleRepository;
import com.training.backend_app.repository.SlotRepository;
import com.training.backend_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final AssignmentRepository assignmentRepository;
    private final MaterialRepository materialRepository;
    private final SubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;

    @Transactional
    public AssignmentResponse createAssignment(Long trainerId, Long courseId, Long moduleId, Long slotId,
            String title, String description, LocalDateTime dueDate,
            MultipartFile file) throws IOException {
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new RuntimeException("Trainer not found with ID: " + trainerId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

        Module module = (moduleId != null && moduleId > 0) ? moduleRepository.findById(moduleId).orElse(null) : null;
        Slot slot = (slotId != null && slotId > 0) ? slotRepository.findById(slotId).orElse(null) : null;

        Assignment assignment = Assignment.builder()
                .title(title)
                .description(description)
                .dueDate(dueDate)
                .course(course)
                .module(module)
                .slot(slot)
                .trainer(trainer)
                .fileName(file != null ? file.getOriginalFilename() : null)
                .contentType(file != null ? file.getContentType() : null)
                .fileData(file != null ? file.getBytes() : null)
                .build();

        assignment = assignmentRepository.saveAndFlush(assignment);

        // Use the already fetched entities for mapping to avoid lazy loading issues
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .dueDate(assignment.getDueDate())
                .courseId(course.getId())
                .courseName(course.getName())
                .moduleId(module != null ? module.getId() : null)
                .moduleName(module != null ? module.getName() : null)
                .slotId(slot != null ? slot.getId() : null)
                .trainerId(trainer.getId())
                .trainerName(trainer.getName())
                .fileName(assignment.getFileName())
                .createdAt(assignment.getCreatedAt() != null ? assignment.getCreatedAt() : LocalDateTime.now())
                .build();
    }

    @Transactional
    public MaterialResponse createMaterial(Long trainerId, Long courseId, Long moduleId, Long slotId,
            String title, String description, MultipartFile file) throws IOException {
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new RuntimeException("Trainer not found with ID: " + trainerId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

        Module module = (moduleId != null && moduleId > 0) ? moduleRepository.findById(moduleId).orElse(null) : null;
        Slot slot = (slotId != null && slotId > 0) ? slotRepository.findById(slotId).orElse(null) : null;

        Material material = Material.builder()
                .title(title)
                .description(description)
                .course(course)
                .module(module)
                .slot(slot)
                .trainer(trainer)
                .fileName(file != null ? file.getOriginalFilename() : null)
                .contentType(file != null ? file.getContentType() : null)
                .fileData(file != null ? file.getBytes() : null)
                .build();

        material = materialRepository.saveAndFlush(material);

        // Use the already fetched entities for mapping
        return MaterialResponse.builder()
                .id(material.getId())
                .title(material.getTitle())
                .description(material.getDescription())
                .courseId(course.getId())
                .courseName(course.getName())
                .moduleId(module != null ? module.getId() : null)
                .moduleName(module != null ? module.getName() : null)
                .slotId(slot != null ? slot.getId() : null)
                .trainerId(trainer.getId())
                .trainerName(trainer.getName())
                .fileName(material.getFileName())
                .createdAt(material.getCreatedAt() != null ? material.getCreatedAt() : LocalDateTime.now())
                .build();
    }

    @Transactional
    public void deleteAssignment(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new RuntimeException("Assignment not found");
        }
        // Submissions will be deleted via cascade if configured, or manually here
        submissionRepository.findByAssignmentId(id).forEach(submissionRepository::delete);
        assignmentRepository.deleteById(id);
    }

    @Transactional
    public void deleteMaterial(Long id) {
        if (!materialRepository.existsById(id)) {
            throw new RuntimeException("Material not found");
        }
        materialRepository.deleteById(id);
    }

    public List<AssignmentResponse> getAssignmentsByTrainer(Long trainerId) {
        return assignmentRepository.findByTrainerId(trainerId).stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    public List<MaterialResponse> getMaterialsByTrainer(Long trainerId) {
        return materialRepository.findByTrainerId(trainerId).stream()
                .map(this::mapToMaterialResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubmissionResponse submitAssignment(Long assignmentId, Long studentId, String comment, MultipartFile file)
            throws IOException {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        User student = userRepository.findById(studentId).orElseThrow(() -> new RuntimeException("Student not found"));

        Submission submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElse(Submission.builder()
                        .assignment(assignment)
                        .student(student)
                        .build());

        submission.setStatus("SUBMITTED");
        submission.setComment(comment);
        if (file != null) {
            submission.setFileName(file.getOriginalFilename());
            submission.setContentType(file.getContentType());
            submission.setFileData(file.getBytes());
        }
        submission.setSubmittedAt(LocalDateTime.now());

        submission = submissionRepository.save(submission);
        return mapToSubmissionResponse(submission);
    }

    public List<SubmissionResponse> getSubmissionsByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId).stream()
                .map(this::mapToSubmissionResponse)
                .collect(Collectors.toList());
    }

    private SubmissionResponse mapToSubmissionResponse(Submission submission) {
        return SubmissionResponse.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignment().getId())
                .assignmentTitle(submission.getAssignment().getTitle())
                .studentId(submission.getStudent().getId())
                .studentName(submission.getStudent().getName())
                .status(submission.getStatus())
                .comment(submission.getComment())
                .fileName(submission.getFileName())
                .submittedAt(submission.getSubmittedAt())
                .build();
    }

    public List<AssignmentResponse> getAssignmentsByCourse(Long courseId) {
        return assignmentRepository.findByCourseId(courseId).stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    public List<MaterialResponse> getMaterialsByCourse(Long courseId) {
        return materialRepository.findByCourseId(courseId).stream()
                .map(this::mapToMaterialResponse)
                .collect(Collectors.toList());
    }

    public Assignment getAssignmentEntity(Long id) {
        return assignmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Assignment not found"));
    }

    public Material getMaterialEntity(Long id) {
        return materialRepository.findById(id).orElseThrow(() -> new RuntimeException("Material not found"));
    }

    private AssignmentResponse mapToAssignmentResponse(Assignment assignment) {
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .dueDate(assignment.getDueDate())
                .courseId(assignment.getCourse().getId())
                .courseName(assignment.getCourse().getName())
                .moduleId(assignment.getModule() != null ? assignment.getModule().getId() : null)
                .moduleName(assignment.getModule() != null ? assignment.getModule().getName() : null)
                .slotId(assignment.getSlot() != null ? assignment.getSlot().getId() : null)
                .trainerId(assignment.getTrainer().getId())
                .trainerName(assignment.getTrainer().getName())
                .fileName(assignment.getFileName())
                .createdAt(assignment.getCreatedAt())
                .build();
    }

    private MaterialResponse mapToMaterialResponse(Material material) {
        return MaterialResponse.builder()
                .id(material.getId())
                .title(material.getTitle())
                .description(material.getDescription())
                .courseId(material.getCourse().getId())
                .courseName(material.getCourse().getName())
                .moduleId(material.getModule() != null ? material.getModule().getId() : null)
                .moduleName(material.getModule() != null ? material.getModule().getName() : null)
                .slotId(material.getSlot() != null ? material.getSlot().getId() : null)
                .trainerId(material.getTrainer().getId())
                .trainerName(material.getTrainer().getName())
                .fileName(material.getFileName())
                .createdAt(material.getCreatedAt())
                .build();
    }
}
