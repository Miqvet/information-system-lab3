package com.example.lab1.service;

import com.example.lab1.domain.entity.StudyGroup;
import com.example.lab1.domain.entity.ImportHistory;
import com.example.lab1.domain.entity.Person;
import com.example.lab1.domain.entity.auth.User;
import com.example.lab1.repository.ImportHistoryRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final StudyGroupService studyGroupService;
    private final PersonService personService;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final ImportHistoryRepository importHistoryRepository;
    private final TransactionTemplate transactionTemplate;

    
    public void importStudyGroupsFromJson(MultipartFile file) throws IOException {
        ImportHistory importHistory = new ImportHistory();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByUsername(username);

        System.out.println(currentUser);

        importHistory.setFileName(file.getOriginalFilename());
        importHistory.setImportDate(LocalDateTime.now());
        importHistory.setAddedBy(currentUser);

        try {
            saveStudyGroups(file, currentUser);
            importHistory.setStatus(true);
            importHistoryRepository.save(importHistory);
        } catch (Exception e) {
            importHistory.setStatus(false);
            importHistoryRepository.save(importHistory);
            throw e;
        }
    }

    @Transactional
    public void saveStudyGroups(MultipartFile file, User currentUser) throws IOException {
        List<StudyGroup> studyGroups = objectMapper.readValue(file.getInputStream(),
                new TypeReference<List<StudyGroup>>() {});
        System.out.println(studyGroups);

        transactionTemplate.executeWithoutResult(status -> {
            try {
                for (StudyGroup group : studyGroups) {
                    Person groupAdmin = group.getGroupAdmin();
                    groupAdmin.setCreatedBy(currentUser);

                    Optional<Person> existingPerson = personService.findExistingPerson(groupAdmin);
                    if (existingPerson.isPresent()) {
                        groupAdmin = existingPerson.get();
                    } else {
                        personService.savePerson(groupAdmin);
                    }

                    group.setGroupAdmin(groupAdmin);
                    group.setCreatedBy(currentUser);
                    studyGroupService.save(group);
                }
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new RuntimeException("Ошибка при сохранении групп", e);
            }
        });
    }
}