package com.training.backend_app.repository;

import com.training.backend_app.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {

    List<Slot> findByWeekId(Long weekId);

    Optional<Slot> findByWeekIdAndDayOfWeekAndSlotNumber(Long weekId, DayOfWeek dayOfWeek, Integer slotNumber);

    @Query("SELECT s FROM Slot s WHERE s.trainer.id = :trainerId AND s.dayOfWeek = :dayOfWeek AND s.slotNumber = :slotNumber")
    Optional<Slot> findByTrainerAndDayAndSlot(Long trainerId, DayOfWeek dayOfWeek, Integer slotNumber);

    List<Slot> findByModuleId(Long moduleId);

    boolean existsByWeekIdAndTrainerIdAndDayOfWeekAndSlotNumber(Long weekId, Long trainerId, DayOfWeek dayOfWeek,
            Integer slotNumber);

    List<Slot> findByTrainerId(Long trainerId);

    List<Slot> findByCourseId(Long courseId);
}
