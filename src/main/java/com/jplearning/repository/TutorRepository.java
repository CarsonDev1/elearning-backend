package com.jplearning.repository;

import com.jplearning.entity.Tutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TutorRepository extends JpaRepository<Tutor, Long> {
    Page<Tutor> findByEnabled(boolean enabled, Pageable pageable);

    long countByEnabled(boolean enabled);
}