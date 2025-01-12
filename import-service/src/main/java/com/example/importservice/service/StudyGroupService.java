package com.example.importservice.service;

import com.example.importservice.domain.entity.StudyGroup;
import com.example.importservice.repository.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StudyGroupService {
    private final StudyGroupRepository studyGroupRepository;
    private final PersonService personService;

    @Cacheable(value = "studyGroups", key = "'all'")
    public List<StudyGroup> findAll() {
        return studyGroupRepository.findAll();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CacheEvict(value = "studyGroups", allEntries = true)
    public void save(StudyGroup studyGroup) {
        try {
            if (studyGroup.getGroupAdmin() != null) {
                personService.savePerson(studyGroup.getGroupAdmin());
            }
            
            if(studyGroupRepository.existsByName(studyGroup.getName())) {
                studyGroup.setName(studyGroup.getName() + " " + generateUniqueId(LocalDateTime.now()));
            }
            studyGroupRepository.saveAndFlush(studyGroup);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Ошибка сохранения группы: " + e.getMessage());
        }
    }

    public long countThereGroupAdmin(long adminId) {
        List<StudyGroup> allGroup = findAll();
        long count = allGroup.stream()
                .filter(studyGroup -> studyGroup.getGroupAdmin().getId() == adminId)
                .count();
        System.out.println(count);
        return count;
    }
    public static String generateUniqueId(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return dateTime.format(formatter) + dateTime.getNano();
    }
}