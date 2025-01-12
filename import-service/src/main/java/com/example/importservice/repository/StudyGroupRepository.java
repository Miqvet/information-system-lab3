package com.example.importservice.repository;

import com.example.importservice.domain.entity.StudyGroup;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Integer>, JpaSpecificationExecutor<StudyGroup> {
    Page<StudyGroup> findByNameContaining(String name, Pageable pageable);

    long countByShouldBeExpelledLessThan(int shouldBeExpelled);
    long countByShouldBeExpelledGreaterThan(int shouldBeExpelled);

    boolean existsByName(String name);
    List<StudyGroup> getStudyGroupsByName(String string);

    @Query("SELECT DISTINCT sg FROM StudyGroup sg " +
    "LEFT JOIN FETCH sg.coordinates " +
    "LEFT JOIN FETCH sg.createdBy " +
    "LEFT JOIN FETCH sg.createdBy.role")
    List<StudyGroup> findAll();
}
