package com.example.lab1.controller;

import com.example.lab1.domain.dto.JwtAuthenticationResponse;
import com.example.lab1.domain.dto.SignRequest;
import com.example.lab1.domain.entity.StudyGroup;
import com.example.lab1.service.AuthenticationService;
import com.example.lab1.service.StudyGroupService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/api")
@RestController
public class RESTControllerForTest {
    private final AuthenticationService authenticationService;
    private final StudyGroupService studyGroupService;

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
}
