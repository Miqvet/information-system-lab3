package com.example.lab1.repository;

import com.example.lab1.domain.entity.StudyGroup;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Integer>, JpaSpecificationExecutor<StudyGroup> {
    Page<StudyGroup> findByNameContaining(String name, Pageable pageable);

    long countByShouldBeExpelledLessThan(int shouldBeExpelled);
    long countByShouldBeExpelledGreaterThan(int shouldBeExpelled);

    @Query("SELECT DISTINCT sg FROM StudyGroup sg " +
    "LEFT JOIN FETCH sg.coordinates " +
    "LEFT JOIN FETCH sg.createdBy " +
    "LEFT JOIN FETCH sg.createdBy.role")
    List<StudyGroup> findAll();
}
