package com.example.lab1.exception;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ModelAndView handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        ModelAndView modelAndView = new ModelAndView("error/method-not-allowed");
        modelAndView.addObject("message", "Ошибка: Метод " + ex.getMethod() + " не поддерживается для данного URL.");
        modelAndView.addObject("supportedMethods", String.join(", ", Objects.requireNonNull(ex.getSupportedMethods())));
        return modelAndView;
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, String>> handleJwtException(JwtException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Invalid token");
        response.put("message", "Access denied");
        response.put("JWTMessage", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgumentException(IllegalArgumentException e, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("error/bad-request");
        modelAndView.addObject("errorMessage", e.getMessage());
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return modelAndView;
    }

    @ExceptionHandler(NumberFormatException.class)
    public ModelAndView handleNumberFormatException(NumberFormatException e, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("error/bad-request");
        modelAndView.addObject("errorMessage", "Неверный формат числового параметра: " + e.getMessage());
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return modelAndView;
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ModelAndView handleNotFoundException(NoSuchElementException e, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("error/404");
        modelAndView.addObject("errorMessage", "Ресурс не найден: " + e.getMessage());
        response.setStatus(HttpStatus.NOT_FOUND.value());
        return modelAndView;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ModelAndView handleValidationException(ConstraintViolationException e, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("error/bad-request");
        modelAndView.addObject("errorMessage", "Ошибка валидации: " + e.getMessage());
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneralException(Exception e, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("error/bad-request");
        modelAndView.addObject("errorMessage", "Произошла ошибка: " + e.getMessage());
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return modelAndView;
    }
} 