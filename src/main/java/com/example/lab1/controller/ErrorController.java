package com.example.lab1.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {
    private static final String USERNAME_ATTR = "username";
    private static final String IS_ADMIN_ATTR = "isAdmin";
    @GetMapping("/access-denied")
    public String accessDenied(HttpSession session, Model model) {
        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
        return "error/403";
    }

}
