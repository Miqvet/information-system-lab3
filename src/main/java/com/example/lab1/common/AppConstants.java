package com.example.lab1.common;

public final class AppConstants {
    // Атрибуты
    public static final String USERNAME_ATTR = "username";
    public static final String IS_ADMIN_ATTR = "isAdmin";
    public static final String PERSON_ATTR = "person";
    public static final String STUDY_GROUP_ATTR = "studyGroup";
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String MESSAGE3_ATTR = "message3";
    public static final String MESSAGE4_ATTR = "message4";

    // Роли
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";

    // Страницы
    public static final String REGISTER_PAGE = "register";
    public static final String CREATE_EDIT_PAGE = "user/create-edit";
    public static final String ADMIN_PAGE_VIEW = "admin/admin-page";
    public static final String ERROR_403 = "error/403";
    public static final String ERROR_404 = "error/404";
    public static final String ERROR_WINDOW = "error/bad-request";

    // Редиректы
    public static final String REDIRECT_USER = "redirect:/user";

    // Сообщения
    public static final String DELETE_MESSAGE = "StudyGroup was deleted, ID: ";
    public static final String CREATE_MESSAGE = "StudyGroup was created";
    public static final String EDIT_MESSAGE = "StudyGroup was edited, ID: ";
    public static final String ERROR_REQUEST_MESSAGE = "Произошла ошибка при обработке запроса: ";
    public static final String GROUP_ID_MESSAGE = "Группа с ID ";
    public static final String NOT_FOUND_MESSAGE = " не найдена";
    public static final String INVALID_ID_FORMAT = "Неверный формат ID";
    public static final String THRESHOLD_ERROR_MSG = "Threshold must be a valid number";

    private AppConstants() {
    }
}