package com.training.backend_app.repository;

import com.training.backend_app.entity.Week;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeekRepository extends JpaRepository<Week, Long> {
    
    Optional<Week> findByWeekNumber(Integer weekNumber);
    
    boolean existsByWeekNumber(Integer weekNumber);
}
