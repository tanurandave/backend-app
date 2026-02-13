package com.training.backend_app.service;

import com.training.backend_app.dto.TimetableResponse;
import com.training.backend_app.entity.Course;
import com.training.backend_app.entity.Enrollment;
import com.training.backend_app.entity.Slot;
import com.training.backend_app.repository.EnrollmentRepository;
import com.training.backend_app.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final EnrollmentRepository enrollmentRepository;
    private final SlotRepository slotRepository;

    public List<TimetableResponse> getStudentTimetable(Long studentId) {
        // Get all enrollments for the student
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        
        // Get all course IDs the student is enrolled in
        List<Long> courseIds = enrollments.stream()
                .map(enrollment -> enrollment.getCourse().getId())
                .collect(Collectors.toList());
        
        // Get all slots for these courses
        List<Slot> allSlots = slotRepository.findAll();
        
        // Filter slots for enrolled courses
        List<Slot> studentSlots = allSlots.stream()
                .filter(slot -> slot.getModule() != null && courseIds.contains(slot.getModule().getCourse().getId()))
                .collect(Collectors.toList());
        
        // Build timetable response
        List<TimetableResponse> timetable = new ArrayList<>();
        
        for (Slot slot : studentSlots) {
            Course course = slot.getModule().getCourse();
            
            TimetableResponse.TimetableResponseBuilder builder = TimetableResponse.builder()
                    .slotId(slot.getId())
                    .weekId(slot.getWeek().getId())
                    .weekNumber(slot.getWeek().getWeekNumber())
                    .courseId(course.getId())
                    .courseName(course.getName())
                    .moduleId(slot.getModule().getId())
                    .moduleName(slot.getModule().getName())
                    .trainerId(slot.getTrainer().getId())
                    .trainerName(slot.getTrainer().getName())
                    .dayOfWeek(slot.getDayOfWeek().name())
                    .slotNumber(slot.getSlotNumber());
            
            timetable.add(builder.build());
        }
        
        return timetable;
    }
}
