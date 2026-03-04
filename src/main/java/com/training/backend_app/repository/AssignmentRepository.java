package com.training.backend_app.repository;

import com.training.backend_app.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourseId(Long courseId);

    List<Assignment> findBySlotId(Long slotId);

    List<Assignment> findByModuleId(Long moduleId);

    List<Assignment> findByTrainerId(Long trainerId);
}
