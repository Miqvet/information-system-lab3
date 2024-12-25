package com.example.lab1.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.transaction.TransactionException;

import java.sql.SQLException;
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

    @ExceptionHandler({JsonMappingException.class,
                    JsonParseException.class})
    public ModelAndView handleJsonParseException(JsonParseException e, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("error/bad-request");
        modelAndView.addObject("errorMessage", "Ошибка в формате файла: " + e.getMessage());
        response.setStatus(HttpStatus.NOT_FOUND.value());
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

    @ExceptionHandler({
        PessimisticLockingFailureException.class,
        ObjectOptimisticLockingFailureException.class,
        CannotAcquireLockException.class
    })
    public ModelAndView handleTransactionException(Exception e, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("error/conflict");
        modelAndView.addObject("errorMessage", "Конфликт при параллельном доступе: " +
            "Объект был изменен другим пользователем. Пожалуйста, попробуйте снова.");
        response.setStatus(HttpStatus.CONFLICT.value());
        return modelAndView;
    }
    @ExceptionHandler({
            SQLException.class,
            TransactionException.class
    })
    public ModelAndView handleServiceException(Exception e, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("error/conflict");
        modelAndView.addObject("errorMessage", "Возникла ошибка: " +
                "Один из сервисов системы в данный момент не отвечает");
        response.setStatus(HttpStatus.CONFLICT.value());
        return modelAndView;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ModelAndView handleDataIntegrityViolation(DataIntegrityViolationException e,
                                                    HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("error/conflict");
        String message = "Ошибка целостности данных: ";
        message = message + extractDetails(e.getMessage());
        modelAndView.addObject("errorMessage", message);
        response.setStatus(HttpStatus.CONFLICT.value());
        return modelAndView;
    }
    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneralException(Exception e, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("error/bad-request");
        modelAndView.addObject("errorMessage", "Произошла ошибка: " + e.getMessage() + "\n" + e.getClass());
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return modelAndView;
    }
    private static String extractDetails(String errorString) {
        String keyword = "Подробности: ";
        int startIndex = errorString.indexOf(keyword);

        if (startIndex != -1) {
            startIndex += keyword.length();
            int endIndex = errorString.indexOf(']', startIndex);
            if (endIndex != -1) {
                return errorString.substring(startIndex, endIndex).trim();
            }
        }
        return "Детали не найдены.";
    }
} 