package com.example.lab1.controller;

import com.example.lab1.domain.entity.Person;
import com.example.lab1.domain.entity.StudyGroup;
import com.example.lab1.domain.entity.auth.User;
import com.example.lab1.domain.entity.enums.Color;
import com.example.lab1.domain.entity.enums.Country;
import com.example.lab1.domain.entity.enums.FormOfEducation;
import com.example.lab1.domain.entity.enums.Semester;
import com.example.lab1.service.PersonService;
import com.example.lab1.service.UserService;
import static com.example.lab1.common.AppConstants.*;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;



@Controller
@RequestMapping("/user/people")
public class PersonController {

    @Autowired
    private final UserService userService;
    @Autowired
    private final PersonService personService;
    @Autowired
    private final WebSocketController webSocketController;

    @GetMapping("/create")
    public String showCreateForm(HttpSession session, Model model, HttpServletResponse response) {

        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
        model.addAttribute(STUDY_GROUP_ATTR, new StudyGroup());
        model.addAttribute(PERSON_ATTR, new Person());
        createFullModel(model);
        response.setStatus(HttpServletResponse.SC_OK);
        return "user/create-edit";

    }

    @PostMapping("/create")
    public String createAdmin(@Valid @ModelAttribute(PERSON_ATTR) Person person,
                              BindingResult result, Principal principal, Model model,
                              HttpServletResponse response) {
        model.addAttribute(STUDY_GROUP_ATTR, new StudyGroup());
        if (result.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            createFullModel(model);
            return CREATE_EDIT_PAGE;
        }
        User currentUser = userService.findByUsername(principal.getName());
        person.setCreatedBy(currentUser);
        personService.savePerson(person);
        response.setStatus(HttpServletResponse.SC_CREATED);
        webSocketController.notifyClients(CREATE_MESSAGE);
        return REDIRECT_USER;

    }

    @GetMapping("/edit/{id}")
    public String showPersonEditForm(HttpSession session,
                                     @PathVariable String id,
                                     Principal principal,
                                     Model model,
                                     HttpServletResponse response) {
        long personId = Long.parseLong(id);
        Person person = personService.getById(personId);

        if (!person.isCanBeChanged() ||
                (!principal.getName().equals(person.getCreatedBy().getUsername()) && !isAdmin(principal))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            model.addAttribute(ERROR_MESSAGE, "У вас нет прав для редактирования этого администратора");
            return ERROR_403;
        }

        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
        model.addAttribute(PERSON_ATTR, person);
        model.addAttribute(STUDY_GROUP_ATTR, new StudyGroup());
        createFullModel(model);
        response.setStatus(HttpServletResponse.SC_OK);
        return CREATE_EDIT_PAGE;

    }

    @PostMapping("/edit/{id}")
    public String updatePerson(@PathVariable String id,
                               @Valid @ModelAttribute Person person,
                               BindingResult result,
                               Principal principal,
                               Model model,
                               HttpServletResponse response) {
        long personId = Long.parseLong(id);
        model.addAttribute(STUDY_GROUP_ATTR, new StudyGroup());

        if (result.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            createFullModel(model);
            return CREATE_EDIT_PAGE;
        }

        Person existingPerson = personService.getById(personId);

        if (!existingPerson.isCanBeChanged() ||
                (!principal.getName().equals(existingPerson.getCreatedBy().getUsername()) && !isAdmin(principal))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            model.addAttribute(ERROR_MESSAGE, "У вас нет прав для редактирования этого администратора");
            return ERROR_403;
        }

        personService.update(personId, person);
        response.setStatus(HttpServletResponse.SC_OK);
        webSocketController.notifyClients(EDIT_MESSAGE + personId);
        return REDIRECT_USER;
    }

    @GetMapping("/delete/{id}")
    public String getDeletePerson(@PathVariable String id,
                               Principal principal,
                               Model model,
                               HttpServletResponse response) {
        long personId = Long.parseLong(id);
        Person person = personService.getById(personId);

        if (!principal.getName().equals(person.getCreatedBy().getUsername()) && !isAdmin(principal)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            model.addAttribute(ERROR_MESSAGE, "У вас нет прав для удаления этого администратора");
            return ERROR_403;
        }

        personService.deleteById(personId);
        response.setStatus(HttpServletResponse.SC_OK);
        webSocketController.notifyClients(DELETE_MESSAGE + personId);
        return REDIRECT_USER;
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
