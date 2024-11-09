package com.example.lab1.controller;

import com.example.lab1.domain.entity.Person;
import com.example.lab1.domain.entity.StudyGroup;
import com.example.lab1.domain.entity.auth.User;
import com.example.lab1.domain.entity.enums.Color;
import com.example.lab1.domain.entity.enums.Country;
import com.example.lab1.domain.entity.enums.FormOfEducation;
import com.example.lab1.domain.entity.enums.Semester;
import com.example.lab1.service.PersonService;
import com.example.lab1.service.StudyGroupService;
import com.example.lab1.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@AllArgsConstructor
@RequestMapping("/user/people")
public class PersonController {
    private static final String USERNAME_ATTR = "username";
    private static final String IS_ADMIN_ATTR = "isAdmin";
    private static final String STUDY_GROUP_ATTR = "studyGroup";

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String PERSON_ATTR = "person";


    private static final String CREATE_EDIT_PAGE = "user/create-edit";
    private static final String ERROR_403 = "error/403";
    private static final String ERROR_404 = "error/404";
    private static final String ERROR_WINDOW = "error/bad-request";

    private static final String REDIRECT_USER = "redirect:/user";
    private static final String REDIRECT_CREATE = "redirect:/user/create";

    private static final String DELETE_MESSAGE = "StudyGroup deleted with ID: ";
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String ERROR_REQUEST_MESSAGE = "Произошла ошибка при обработке запроса: ";
    private static final String GROUP_ID_MESSAGE = "Группа с ID ";
    private static final String NOT_FOUND_MESSAGE = " не найдена";
    private static final String INVALID_ID_FORMAT = "Неверный формат ID";
    private static final String ADMIN_ID_MESSAGE = "Администратор с ID ";

    private final StudyGroupService studyGroupService;
    private final UserService userService;
    private final PersonService personService;
    private final WebSocketController webSocketController;

    @GetMapping("/create")
    public String showCreateForm(HttpSession session, Model model) {
        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
        model.addAttribute(STUDY_GROUP_ATTR, new StudyGroup());
        model.addAttribute(PERSON_ATTR, new Person());
        createFullModel(model);
        return "user/create-edit";
    }

    @PostMapping("/create")
    public String createAdmin(@Valid @ModelAttribute(PERSON_ATTR) Person person,
                              BindingResult result, Principal principal, Model model) {
        model.addAttribute(STUDY_GROUP_ATTR , new StudyGroup());
        if (result.hasErrors()) {
            createFullModel(model);
            return CREATE_EDIT_PAGE;
        }
        User currentUser = userService.findByUsername(principal.getName());
        person.setCreatedBy(currentUser);
        try {
            personService.savePerson(person);
        } catch (IllegalArgumentException e) {
            model.addAttribute("customPassportError", e.getMessage());
            createFullModel(model);
            return CREATE_EDIT_PAGE;
        }
        return REDIRECT_USER;

    }

    @GetMapping("/edit/{id}")
    public String showPersonEditForm(HttpSession session,
                                     @PathVariable String id,
                                     Principal principal,
                                     Model model) {
        try {
            long personId = Long.parseLong(id);
            Person person = personService.getById(personId);

            if (person == null) {
                model.addAttribute(ERROR_MESSAGE, ADMIN_ID_MESSAGE + id + NOT_FOUND_MESSAGE);
                return ERROR_WINDOW;
            }

            if (!person.isCanBeChanged() ||
                    (!principal.getName().equals(person.getCreatedBy().getUsername()) && !isAdmin(principal))) {
                model.addAttribute(ERROR_MESSAGE, "У вас нет прав для редактирования этого администратора");
                return ERROR_403;
            }

            model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
            model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
            model.addAttribute(PERSON_ATTR, person);
            model.addAttribute(STUDY_GROUP_ATTR, new StudyGroup());
            createFullModel(model);
            return CREATE_EDIT_PAGE;

        } catch (NumberFormatException e) {
            model.addAttribute(ERROR_MESSAGE, INVALID_ID_FORMAT);
            return ERROR_WINDOW;
        } catch (Exception e) {
            model.addAttribute(ERROR_MESSAGE, ERROR_REQUEST_MESSAGE + e.getMessage());
            return ERROR_WINDOW;
        }
    }

    @PostMapping("/edit/{id}")
    public String updatePerson(@PathVariable String id,
                               @Valid @ModelAttribute Person person,
                               BindingResult result,
                               Principal principal,
                               Model model) {
        try {
            long personId = Long.parseLong(id);
            model.addAttribute(STUDY_GROUP_ATTR, new StudyGroup());

            if (result.hasErrors()) {
                createFullModel(model);
                return CREATE_EDIT_PAGE;
            }

            Person existingPerson = personService.getById(personId);
            if (existingPerson == null) {
                model.addAttribute(ERROR_MESSAGE, ADMIN_ID_MESSAGE + id + NOT_FOUND_MESSAGE);
                return ERROR_WINDOW;
            }

            if (!existingPerson.isCanBeChanged() ||
                    (!principal.getName().equals(existingPerson.getCreatedBy().getUsername()) && !isAdmin(principal))) {
                model.addAttribute(ERROR_MESSAGE, "У вас нет прав для редактирования этого администратора");
                return ERROR_403;
            }

            try {
                personService.update(personId, person);
                webSocketController.notifyClients(DELETE_MESSAGE);
                return REDIRECT_USER;
            } catch (IllegalArgumentException e) {
                model.addAttribute("customPassportError", e.getMessage());
                createFullModel(model);
                return CREATE_EDIT_PAGE;
            }

        } catch (NumberFormatException e) {
            model.addAttribute(ERROR_MESSAGE, "Неверный формат ID администратора");
            return ERROR_WINDOW;
        } catch (Exception e) {
            model.addAttribute(ERROR_MESSAGE, ERROR_REQUEST_MESSAGE + e.getMessage());
            return ERROR_WINDOW;
        }
    }

    @PostMapping("/delete/{id}")
    public String deletePerson(@PathVariable String id,
                               Principal principal,
                               Model model) {
        try {
            long personId = Long.parseLong(id);
            Person person = personService.getById(personId);

            if (person == null) {
                model.addAttribute(ERROR_MESSAGE, ADMIN_ID_MESSAGE + id + NOT_FOUND_MESSAGE);
                return ERROR_WINDOW;
            }

            if (!principal.getName().equals(person.getCreatedBy().getUsername()) && !isAdmin(principal)) {
                model.addAttribute(ERROR_MESSAGE, "У вас нет прав для удаления этого администратора");
                return ERROR_403;
            }

            personService.deleteById(personId);
            webSocketController.notifyClients(DELETE_MESSAGE);
            return REDIRECT_USER;


        } catch (NumberFormatException e) {
            model.addAttribute(ERROR_MESSAGE, "Неверный формат ID администратора");
            return ERROR_WINDOW;
        } catch (Exception e) {
            model.addAttribute(ERROR_MESSAGE, "Произошла ошибка при удалении: " + e.getMessage());
            return ERROR_WINDOW;
        }
    }


    private void createFullModel(Model model){
        model.addAttribute("formOfEducationEnum", FormOfEducation.values());
        model.addAttribute("semesterEnum", Semester.values());
        model.addAttribute("countryEnum", Country.values());
        model.addAttribute("colorEnum", Color.values());
        model.addAttribute("persons", personService.findAll());
    }

    private boolean isAdmin(Principal principal) {
        UserDetails userDetails = (UserDetails) ((Authentication) principal).getPrincipal();
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(ROLE_ADMIN));
    }
}
