package com.training.backend_app.controller;

import com.training.backend_app.entity.User;
import com.training.backend_app.repository.CourseRepository;
import com.training.backend_app.repository.EnrollmentRepository;
import com.training.backend_app.repository.UserRepository;
import com.training.backend_app.repository.WeekRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final WeekRepository weekRepository;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        long totalStudents = userRepository.findByRole(User.Role.STUDENT).size();
        long totalTrainers = userRepository.findByRole(User.Role.TRAINER).size();
        long totalCourses = courseRepository.count();
        long totalEnrollments = enrollmentRepository.count();
        long totalWeeks = weekRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("students", totalStudents);
        stats.put("trainers", totalTrainers);
        stats.put("courses", totalCourses);
        stats.put("enrollments", totalEnrollments);
        stats.put("schedules", totalWeeks);

        return ResponseEntity.ok(stats);
    }
}
