package com.example.lab1.service;

import com.example.lab1.domain.entity.StudyGroup;
import com.example.lab1.domain.entity.Person;
import com.example.lab1.domain.entity.auth.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final StudyGroupService studyGroupService;
    private final PersonService personService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void importStudyGroupsFromJson(MultipartFile file) throws IOException {
        List<StudyGroup> studyGroups = objectMapper.readValue(file.getInputStream(), new TypeReference<List<StudyGroup>>() {});
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByUsername(username);
        for (StudyGroup group : studyGroups) {
            Person groupAdmin = group.getGroupAdmin();
            groupAdmin.setCreatedBy(currentUser);
            Optional<Person> existingPerson = personService.findExistingPerson(groupAdmin);
            if (existingPerson.isPresent()) {
                System.out.println("Уже есть");
                groupAdmin = existingPerson.get();
            } else {
                System.out.println("нет пока");
                personService.savePerson(groupAdmin);
            }

            group.setGroupAdmin(groupAdmin);
            group.setCreatedBy(currentUser);
            studyGroupService.save(group);
        }
    }
}