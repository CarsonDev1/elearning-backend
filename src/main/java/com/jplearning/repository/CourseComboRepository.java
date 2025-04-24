package com.jplearning.repository;

import com.jplearning.entity.Course;
import com.jplearning.entity.CourseCombo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CourseComboRepository extends JpaRepository<CourseCombo, Long> {
    Page<CourseCombo> findByIsActiveTrue(Pageable pageable);

    Page<CourseCombo> findByIsActiveTrueAndValidUntilAfter(LocalDateTime now, Pageable pageable);

    @Query("SELECT cb FROM CourseCombo cb JOIN cb.courses c WHERE c.id = :courseId AND cb.isActive = true")
    List<CourseCombo> findActiveCombosByCourseId(Long courseId);

    Page<CourseCombo> findByTitleContainingIgnoreCaseAndIsActiveTrue(String title, Pageable pageable);

    List<CourseCombo> findByTitleContainingIgnoreCaseAndIsActiveTrue(String title);
}