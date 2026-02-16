package com.training.backend_app.service;

import com.training.backend_app.dto.CourseRequest;
import com.training.backend_app.dto.CourseResponse;
import com.training.backend_app.dto.ModuleRequest;
import com.training.backend_app.dto.ModuleResponse;
import com.training.backend_app.entity.Course;
import com.training.backend_app.entity.Enrollment;
import com.training.backend_app.entity.Module;
import com.training.backend_app.entity.Slot;
import com.training.backend_app.repository.CourseRepository;
import com.training.backend_app.repository.EnrollmentRepository;
import com.training.backend_app.repository.ModuleRepository;
import com.training.backend_app.repository.SlotRepository;
import com.training.backend_app.repository.UserRepository;
import com.training.backend_app.entity.User;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

        private final CourseRepository courseRepository;
        private final ModuleRepository moduleRepository;
        private final EnrollmentRepository enrollmentRepository;
        private final SlotRepository slotRepository;
        private final UserRepository userRepository;

        @Transactional
        public CourseResponse createCourse(CourseRequest request) {
                User trainer = null;
                if (request.getPrimaryTrainerId() != null) {
                        trainer = userRepository.findById(request.getPrimaryTrainerId())
                                        .orElseThrow(() -> new RuntimeException("Trainer not found"));
                        // specific validation check to see if User is actually ROLE_TRAINER could
                        // happen here
                }

                Course course = Course.builder()
                                .name(request.getName())
                                .description(request.getDescription())
                                .duration(request.getDuration())
                                .primaryTrainer(trainer)
                                .build();

                courseRepository.save(course);

                return mapToResponse(course);
        }

        public List<CourseResponse> getAllCourses() {
                return courseRepository.findAll().stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        public CourseResponse getCourseById(Long id) {
                Course course = courseRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Course not found"));
                return mapToResponse(course);
        }

        @Transactional
        public ModuleResponse addModule(Long courseId, ModuleRequest request) {
                Course course = courseRepository.findById(courseId)
                                .orElseThrow(() -> new RuntimeException("Course not found"));

                Module module = Module.builder()
                                .name(request.getName())
                                .description(request.getDescription())
                                .duration(request.getDuration())
                                .orderNumber(request.getOrderNumber())
                                .course(course)
                                .build();

                moduleRepository.save(module);

                return ModuleResponse.builder()
                                .id(module.getId())
                                .name(module.getName())
                                .description(module.getDescription())
                                .duration(module.getDuration())
                                .orderNumber(module.getOrderNumber())
                                .courseId(course.getId())
                                .build();
        }

        public List<ModuleResponse> getModulesByCourseId(Long courseId) {
                return moduleRepository.findByCourseId(courseId).stream()
                                .map(module -> ModuleResponse.builder()
                                                .id(module.getId())
                                                .name(module.getName())
                                                .description(module.getDescription())
                                                .duration(module.getDuration())
                                                .orderNumber(module.getOrderNumber())
                                                .courseId(courseId)
                                                .build())
                                .collect(Collectors.toList());
        }

        private CourseResponse mapToResponse(Course course) {
                List<ModuleResponse> modules = moduleRepository.findByCourseId(course.getId()).stream()
                                .map(module -> ModuleResponse.builder()
                                                .id(module.getId())
                                                .name(module.getName())
                                                .description(module.getDescription())
                                                .duration(module.getDuration())
                                                .orderNumber(module.getOrderNumber())
                                                .courseId(course.getId())
                                                .build())
                                .collect(Collectors.toList());

                return CourseResponse.builder()
                                .id(course.getId())
                                .name(course.getName())
                                .description(course.getDescription())
                                .duration(course.getDuration())
                                .primaryTrainerName(course.getPrimaryTrainer() != null
                                                ? course.getPrimaryTrainer().getName()
                                                : "Unassigned")
                                .modules(modules)
                                .createdAt(course.getCreatedAt())
                                .build();
        }

        @Transactional
        public CourseResponse updateCourse(Long courseId, CourseRequest request) {
                Course course = courseRepository.findById(courseId)
                                .orElseThrow(() -> new RuntimeException("Course not found"));

                course.setName(request.getName());
                course.setDescription(request.getDescription());
                course.setDuration(request.getDuration());

                if (request.getPrimaryTrainerId() != null) {
                        User trainer = userRepository.findById(request.getPrimaryTrainerId())
                                        .orElseThrow(() -> new RuntimeException("Trainer not found"));
                        course.setPrimaryTrainer(trainer);
                } else {
                        // Decide if null means remove trainer or ignore? Usually ignore or explicit
                        // null.
                        // Let's assume if sent as null it might mean clear or just not updating.
                        // For now, let's say we only update if provided, or if explicitly handled.
                        // Given the DTO structure, null usually means 'no change' or 'no value'.
                        // To clear, client might send -1 or specific flag.
                        // But simpler: if the user sends null, we might keep existing.
                        // However, for strict updates, assume standard Put behavior?
                        // Let's just update if present.
                }

                courseRepository.save(course);

                return mapToResponse(course);
        }

        @Transactional
        public void deleteCourse(Long courseId) {
                Course course = courseRepository.findById(courseId)
                                .orElseThrow(() -> new RuntimeException("Course not found"));

                // Delete all enrollments for this course
                List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
                enrollmentRepository.deleteAll(enrollments);

                // Dissociate slots (set course and module to null)
                List<Slot> slots = slotRepository.findByCourseId(courseId);
                for (Slot slot : slots) {
                        slot.setCourse(null);
                        slot.setModule(null);
                        slot.setTrainer(null);
                        slotRepository.save(slot);
                }

                // Also ensure slots referencing modules of this course are cleared (if any
                // didn't have course_id set)
                List<Module> modules = moduleRepository.findByCourseId(courseId);
                for (Module module : modules) {
                        List<Slot> moduleSlots = slotRepository.findByModuleId(module.getId());
                        for (Slot slot : moduleSlots) {
                                slot.setModule(null);
                                slot.setCourse(null);
                                slotRepository.save(slot);
                        }
                }

                // Now delete modules specifically if cascading is not set up (though standard
                // cascade might handle it, explicit is safer here given previous issues)
                moduleRepository.deleteAll(modules);

                courseRepository.delete(course);
        }

        public List<com.training.backend_app.dto.UserResponse> getTrainersByCourseId(Long courseId) {
                return slotRepository.findByCourseId(courseId).stream()
                                .map(Slot::getTrainer)
                                .filter(java.util.Objects::nonNull)
                                .map(t -> com.training.backend_app.dto.UserResponse.builder()
                                                .id(t.getId())
                                                .name(t.getName())
                                                .email(t.getEmail())
                                                .role(t.getRole())
                                                .createdAt(t.getCreatedAt())
                                                .build())
                                .distinct()
                                .collect(Collectors.toList());
        }

        @Transactional
        public void deleteModule(Long moduleId) {
                Module module = moduleRepository.findById(moduleId)
                                .orElseThrow(() -> new RuntimeException("Module not found"));

                // Dissociate slots (set module to null)
                List<Slot> slots = slotRepository.findByModuleId(moduleId);
                for (Slot slot : slots) {
                        slot.setModule(null);
                        slotRepository.save(slot);
                }

                moduleRepository.delete(module);
        }

        @Transactional
        public ModuleResponse updateModule(Long moduleId, ModuleRequest request) {
                Module module = moduleRepository.findById(moduleId)
                                .orElseThrow(() -> new RuntimeException("Module not found"));

                module.setName(request.getName());
                module.setDescription(request.getDescription());
                module.setDuration(request.getDuration());
                // Not simple to update orderNumber without reordering everything, skipping for
                // now unless requested

                moduleRepository.save(module);

                return ModuleResponse.builder()
                                .id(module.getId())
                                .name(module.getName())
                                .description(module.getDescription())
                                .duration(module.getDuration())
                                .orderNumber(module.getOrderNumber())
                                .courseId(module.getCourse().getId())
                                .build();
        }
}
