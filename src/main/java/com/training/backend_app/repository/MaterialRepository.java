package com.training.backend_app.repository;

import com.training.backend_app.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByCourseId(Long courseId);

    List<Material> findBySlotId(Long slotId);

    List<Material> findByModuleId(Long moduleId);

    List<Material> findByTrainerId(Long trainerId);
}
