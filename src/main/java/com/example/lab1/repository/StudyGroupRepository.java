package com.example.lab1.repository;

import com.example.lab1.domain.entity.StudyGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Integer>, JpaSpecificationExecutor<StudyGroup> {
    Page<StudyGroup> findByNameContaining(String name, Pageable pageable);

    long countByShouldBeExpelledLessThan(int shouldBeExpelled);
    long countByShouldBeExpelledGreaterThan(int shouldBeExpelled);
}
