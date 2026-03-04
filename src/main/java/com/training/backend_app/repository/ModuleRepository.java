package com.training.backend_app.repository;

import com.training.backend_app.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    
    List<Module> findByCourseId(Long courseId);
    
    Optional<Module> findByIdAndCourseId(Long id, Long courseId);
}
