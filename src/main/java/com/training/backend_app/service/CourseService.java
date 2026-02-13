package com.training.backend_app.service;

import com.training.backend_app.dto.CourseRequest;
import com.training.backend_app.dto.CourseResponse;
import com.training.backend_app.dto.ModuleRequest;
import com.training.backend_app.dto.ModuleResponse;
import com.training.backend_app.entity.Course;
import com.training.backend_app.entity.Module;
import com.training.backend_app.repository.CourseRepository;
import com.training.backend_app.repository.ModuleRepository;
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

    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        Course course = Course.builder()
                .name(request.getName())
                .description(request.getDescription())
                .duration(request.getDuration())
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
                .course(course)
                .build();

        moduleRepository.save(module);

        return ModuleResponse.builder()
                .id(module.getId())
                .name(module.getName())
                .description(module.getDescription())
                .duration(module.getDuration())
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
                        .courseId(course.getId())
                        .build())
                .collect(Collectors.toList());

        return CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .duration(course.getDuration())
                .modules(modules)
                .createdAt(course.getCreatedAt())
                .build();
    }
}
