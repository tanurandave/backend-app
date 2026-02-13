package com.training.backend_app.repository;

import com.training.backend_app.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    Optional<Course> findByName(String name);
    
    boolean existsByName(String name);
}
