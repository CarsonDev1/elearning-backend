package com.jplearning.repository;

import com.jplearning.entity.Course;
import com.jplearning.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findByCourseOrderByPosition(Course course);

    Integer countByCourse(Course course);
}