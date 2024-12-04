package com.example.lab1.controller;

import com.example.lab1.domain.dto.JwtAuthenticationResponse;
import com.example.lab1.domain.dto.SignRequest;
import com.example.lab1.domain.entity.StudyGroup;
import com.example.lab1.service.AuthenticationService;
import com.example.lab1.service.ImportService;
import com.example.lab1.service.StudyGroupService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/api")
@RestController
public class RESTControllerForTest {
    private final AuthenticationService authenticationService;
    private final StudyGroupService studyGroupService;
    private final ImportService importService;

    @PostMapping("/sign-up")
    public JwtAuthenticationResponse signUp(@RequestBody @Valid SignRequest request) {
        return authenticationService.signUp(request);
    }

    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignRequest request) {
        return authenticationService.signIn(request);
    }

    @GetMapping("/groups")
    public List<StudyGroup> getAllGroups() {
        return studyGroupService.findAll();
    }

    @PostMapping("/study-groups")
    public ResponseEntity<String> importStudyGroups(@RequestParam("file") MultipartFile file) {
        try {
            long savedCount = importService.saveDataFromFile(file);
            importService.saveImportHistory(file, savedCount);
            return ResponseEntity.ok("Импорт успешно завершен. Сохранено " + savedCount + " элементов.");
        } catch (Exception e) {
            importService.saveImportHistory(file,  0);
            return ResponseEntity.badRequest().body("Ошибка при импорте: " + e.getMessage());
        }
    }
}
