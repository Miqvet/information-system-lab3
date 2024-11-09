package com.example.lab1.controller;

import com.example.lab1.domain.dto.SignRequest;
import com.example.lab1.domain.entity.auth.Role;
import com.example.lab1.domain.entity.auth.User;
import com.example.lab1.domain.entity.enums.RoleName;
import com.example.lab1.repository.auth.RoleRepository;
import com.example.lab1.repository.auth.UserRepository;
import com.example.lab1.service.AuthenticationService;
import com.example.lab1.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@Controller
public class PublicController {
    private static final String REGISTER_PAGE = "register";
    private static final String USERNAME_ATTR = "username";
    private static final String IS_ADMIN_ATTR = "isAdmin";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";

    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals(ROLE_ADMIN));
        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, isAdmin);
        return "home";
    }


    @GetMapping("/login")
    public String login(HttpSession session, Model model) {
        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(HttpSession session, Model model) {
        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
        model.addAttribute("user", new User());
        return REGISTER_PAGE;
    }

    @PostMapping("/register")
    public String signUp(@ModelAttribute @Valid User user, BindingResult result, Model model) {
        if (userService.existsByUsername(user.getUsername())) {
            result.rejectValue(USERNAME_ATTR, null, "Пользователь с таким логином уже существует");
            return REGISTER_PAGE;
        }
        if (result.hasErrors()) {
            return REGISTER_PAGE;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (!userRepository.existsByRole_Name(RoleName.valueOf(ROLE_ADMIN)) && user.isWishToBeAdmin()) {
            Role userRole = roleRepository.findByName(RoleName.valueOf(ROLE_ADMIN)).orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(userRole);
            user.setWishToBeAdmin(false);
        } else {
            Role userRole = roleRepository.findByName(RoleName.valueOf(ROLE_USER)).orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(userRole);
        }
        userService.save(user);

        return "redirect:/login";
    }
}
