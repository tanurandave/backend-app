package com.training.backend_app.service;

import com.training.backend_app.dto.BulkEnrollRequest;
import com.training.backend_app.dto.CourseResponse;
import com.training.backend_app.dto.EnrollmentRequest;
import com.training.backend_app.dto.EnrollmentResponse;
import com.training.backend_app.entity.Course;
import com.training.backend_app.entity.Enrollment;
import com.training.backend_app.entity.User;
import com.training.backend_app.repository.CourseRepository;
import com.training.backend_app.repository.EnrollmentRepository;
import com.training.backend_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public List<EnrollmentResponse> getAllEnrollments() {
        return enrollmentRepository.findAll().stream()
                .map(enrollment -> EnrollmentResponse.builder()
                        .id(enrollment.getId())
                        .studentId(enrollment.getStudent().getId())
                        .studentName(enrollment.getStudent().getName())
                        .courseId(enrollment.getCourse().getId())
                        .courseName(enrollment.getCourse().getName())
                        .enrolledAt(enrollment.getEnrolledAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public EnrollmentResponse enrollStudent(EnrollmentRequest request) {
        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        if (student.getRole() != User.Role.STUDENT) {
            throw new RuntimeException("User is not a student");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        boolean alreadyEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(request.getStudentId(), request.getCourseId());
        if (alreadyEnrolled) {
            throw new RuntimeException("Student is already enrolled in this course");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .build();

        enrollmentRepository.save(enrollment);

        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .studentId(student.getId())
                .studentName(student.getName())
                .courseId(course.getId())
                .courseName(course.getName())
                .enrolledAt(enrollment.getEnrolledAt())
                .build();
    }

    public List<EnrollmentResponse> getEnrollmentsByStudentId(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(enrollment -> EnrollmentResponse.builder()
                        .id(enrollment.getId())
                        .studentId(enrollment.getStudent().getId())
                        .studentName(enrollment.getStudent().getName())
                        .courseId(enrollment.getCourse().getId())
                        .courseName(enrollment.getCourse().getName())
                        .enrolledAt(enrollment.getEnrolledAt())
                        .build())
                .collect(Collectors.toList());
    }

    public List<EnrollmentResponse> getEnrollmentsByCourseId(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(enrollment -> EnrollmentResponse.builder()
                        .id(enrollment.getId())
                        .studentId(enrollment.getStudent().getId())
                        .studentName(enrollment.getStudent().getName())
                        .courseId(enrollment.getCourse().getId())
                        .courseName(enrollment.getCourse().getName())
                        .enrolledAt(enrollment.getEnrolledAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public List<EnrollmentResponse> bulkEnrollStudents(BulkEnrollRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        List<EnrollmentResponse> enrollmentResponses = new java.util.ArrayList<>();

        for (Long studentId : request.getStudentIds()) {
            try {
                // Check if student exists
                User student = userRepository.findById(studentId)
                        .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

                if (student.getRole() != User.Role.STUDENT) {
                    continue; // Skip if not a student
                }

                // Check if already enrolled
                boolean alreadyEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(studentId, request.getCourseId());
                if (alreadyEnrolled) {
                    continue; // Skip if already enrolled
                }

                // Create enrollment
                Enrollment enrollment = Enrollment.builder()
                        .student(student)
                        .course(course)
                        .build();

                enrollmentRepository.save(enrollment);

                enrollmentResponses.add(EnrollmentResponse.builder()
                        .id(enrollment.getId())
                        .studentId(student.getId())
                        .studentName(student.getName())
                        .courseId(course.getId())
                        .courseName(course.getName())
                        .enrolledAt(enrollment.getEnrolledAt())
                        .build());

            } catch (Exception e) {
                // Continue with next student if one fails
                continue;
            }
        }

        return enrollmentResponses;
    }

    public List<CourseResponse> getStudentCourses(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(enrollment -> {
                    Course course = enrollment.getCourse();
                    return CourseResponse.builder()
                            .id(course.getId())
                            .name(course.getName())
                            .description(course.getDescription())
                            .duration(course.getDuration())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
