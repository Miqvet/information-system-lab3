package com.example.lab1.exception;

import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ModelAndView handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        ModelAndView modelAndView = new ModelAndView("error/method-not-allowed");
        modelAndView.addObject("message", "Ошибка: Метод " + ex.getMethod() + " не поддерживается для данного URL.");
        modelAndView.addObject("supportedMethods", String.join(", ", ex.getSupportedMethods()));
        return modelAndView;
    }
} 