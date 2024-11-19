package com.example.lab1.controller;

import com.example.lab1.domain.entity.auth.Role;
import com.example.lab1.domain.entity.auth.User;
import com.example.lab1.domain.entity.enums.RoleName;
import com.example.lab1.repository.auth.RoleRepository;
import com.example.lab1.repository.auth.UserRepository;
import com.example.lab1.service.UserService;
import static com.example.lab1.common.AppConstants.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatusCode;
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

    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/home")
    public String home(HttpSession session, 
                      Model model, 
                      HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals(ROLE_ADMIN));
        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, isAdmin);
        response.setStatus(HttpServletResponse.SC_OK);
        return "home";
    }

    @GetMapping("/login")
    public String login(HttpSession session, 
                       Model model, 
                       HttpServletResponse response) {
        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
        response.setStatus(HttpServletResponse.SC_OK);
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(HttpSession session, 
                                     Model model, 
                                     HttpServletResponse response) {
            model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
            model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
            model.addAttribute("user", new User());
            response.setStatus(HttpServletResponse.SC_OK);
            return REGISTER_PAGE;
    }

    @PostMapping("/register")
    public String signUp(@ModelAttribute @Valid User user, 
                        BindingResult result, 
                        Model model,
                        HttpServletResponse response) {
        if (userService.existsByUsername(user.getUsername())) {
            response.setStatus(HttpServletResponse.SC_CONFLICT); // 409 Conflict
            result.rejectValue(USERNAME_ATTR, String.valueOf(HttpStatusCode.valueOf(409)), 
                    "Пользователь с таким логином уже существует");
            return REGISTER_PAGE;
        }

        if (result.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return REGISTER_PAGE;
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (!userRepository.existsByRole_Name(RoleName.valueOf(ROLE_ADMIN)) && user.isWishToBeAdmin()) {
            Role userRole = roleRepository.findByName(RoleName.valueOf(ROLE_ADMIN))
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(userRole);
            user.setWishToBeAdmin(false);
        } else {
            Role userRole = roleRepository.findByName(RoleName.valueOf(ROLE_USER))
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(userRole);
        }
            
        userService.save(user);
        response.setStatus(HttpServletResponse.SC_CREATED);
        return "redirect:/login";

    }
}
