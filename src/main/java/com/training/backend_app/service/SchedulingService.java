package com.training.backend_app.service;

import com.training.backend_app.dto.SlotRequest;
import com.training.backend_app.dto.SlotResponse;
import com.training.backend_app.dto.WeekRequest;
import com.training.backend_app.dto.WeekResponse;
import com.training.backend_app.entity.Course;
import com.training.backend_app.entity.Module;
import com.training.backend_app.entity.Slot;
import com.training.backend_app.entity.User;
import com.training.backend_app.entity.Week;
import com.training.backend_app.repository.CourseRepository;
import com.training.backend_app.repository.ModuleRepository;
import com.training.backend_app.repository.SlotRepository;
import com.training.backend_app.repository.UserRepository;
import com.training.backend_app.repository.WeekRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchedulingService {

    private final WeekRepository weekRepository;
    private final SlotRepository slotRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;

    @Transactional
    public WeekResponse createWeek(WeekRequest request) {
        Week week = Week.builder()
                .weekNumber(request.getWeekNumber())
                .build();

        weekRepository.save(week);

        return WeekResponse.builder()
                .id(week.getId())
                .weekNumber(week.getWeekNumber())
                .build();
    }

    public List<WeekResponse> getAllWeeks() {
        return weekRepository.findAll().stream()
                .map(week -> WeekResponse.builder()
                        .id(week.getId())
                        .weekNumber(week.getWeekNumber())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public SlotResponse createSlot(Long weekId, SlotRequest request) {
        Week week = weekRepository.findById(weekId)
                .orElseThrow(() -> new RuntimeException("Week not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Module module = moduleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new RuntimeException("Module not found"));

        User trainer = userRepository.findById(request.getTrainerId())
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        if (trainer.getRole() != User.Role.TRAINER) {
            throw new RuntimeException("User is not a trainer");
        }

        DayOfWeek dayOfWeek = DayOfWeek.valueOf(request.getDayOfWeek().toUpperCase());
        
        boolean trainerBooked = slotRepository.existsByWeekIdAndTrainerIdAndDayOfWeekAndSlotNumber(
                weekId, request.getTrainerId(), dayOfWeek, request.getSlotNumber());
        
        if (trainerBooked) {
            throw new RuntimeException("Trainer is already booked for this slot");
        }

        Slot slot = Slot.builder()
                .week(week)
                .course(course)
                .module(module)
                .trainer(trainer)
                .dayOfWeek(dayOfWeek)
                .slotNumber(request.getSlotNumber())
                .build();

        slotRepository.save(slot);

        return SlotResponse.builder()
                .id(slot.getId())
                .weekId(week.getId())
                .courseId(course.getId())
                .courseName(course.getName())
                .moduleId(module.getId())
                .moduleName(module.getName())
                .trainerId(trainer.getId())
                .trainerName(trainer.getName())
                .dayOfWeek(slot.getDayOfWeek().name())
                .slotNumber(slot.getSlotNumber())
                .build();
    }

    public List<SlotResponse> getSlotsByWeekId(Long weekId) {
        return slotRepository.findByWeekId(weekId).stream()
                .map(slot -> SlotResponse.builder()
                        .id(slot.getId())
                        .weekId(slot.getWeek().getId())
                        .courseId(slot.getCourse().getId())
                        .courseName(slot.getCourse().getName())
                        .moduleId(slot.getModule().getId())
                        .moduleName(slot.getModule().getName())
                        .trainerId(slot.getTrainer().getId())
                        .trainerName(slot.getTrainer().getName())
                        .dayOfWeek(slot.getDayOfWeek().name())
                        .slotNumber(slot.getSlotNumber())
                        .build())
                .collect(Collectors.toList());
    }

    public List<SlotResponse> getSlotsByTrainerId(Long trainerId) {
        return slotRepository.findByTrainerId(trainerId).stream()
                .map(slot -> SlotResponse.builder()
                        .id(slot.getId())
                        .weekId(slot.getWeek().getId())
                        .courseId(slot.getCourse().getId())
                        .courseName(slot.getCourse().getName())
                        .moduleId(slot.getModule().getId())
                        .moduleName(slot.getModule().getName())
                        .trainerId(slot.getTrainer().getId())
                        .trainerName(slot.getTrainer().getName())
                        .dayOfWeek(slot.getDayOfWeek().name())
                        .slotNumber(slot.getSlotNumber())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public SlotResponse updateSlot(Long slotId, SlotRequest request) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        Module module = moduleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new RuntimeException("Module not found"));

        User trainer = userRepository.findById(request.getTrainerId())
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        if (trainer.getRole() != User.Role.TRAINER) {
            throw new RuntimeException("User is not a trainer");
        }

        // Check if trainer is already booked for this slot (excluding current slot)
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(request.getDayOfWeek().toUpperCase());
        
        boolean trainerBooked = slotRepository.findByTrainerAndDayAndSlot(
                request.getTrainerId(), dayOfWeek, request.getSlotNumber())
                .filter(s -> !s.getId().equals(slotId))
                .isPresent();

        if (trainerBooked) {
            throw new RuntimeException("Trainer is already booked for this slot");
        }

        // Update slot
        slot.setModule(module);
        slot.setTrainer(trainer);
        slot.setDayOfWeek(dayOfWeek);
        slot.setSlotNumber(request.getSlotNumber());

        slotRepository.save(slot);

        return SlotResponse.builder()
                .id(slot.getId())
                .weekId(slot.getWeek().getId())
                .courseId(slot.getCourse().getId())
                .courseName(slot.getCourse().getName())
                .moduleId(module.getId())
                .moduleName(module.getName())
                .trainerId(trainer.getId())
                .trainerName(trainer.getName())
                .dayOfWeek(slot.getDayOfWeek().name())
                .slotNumber(slot.getSlotNumber())
                .build();
    }
}
