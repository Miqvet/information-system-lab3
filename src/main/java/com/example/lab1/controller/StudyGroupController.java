package com.example.lab1.controller;

import com.example.lab1.domain.entity.StudyGroup;
import com.example.lab1.domain.entity.Person;
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
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@Controller
@AllArgsConstructor
public class StudyGroupController {
    private static final String USERNAME_ATTR = "username";
    private static final String IS_ADMIN_ATTR = "isAdmin";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String PERSON_ATTR = "person";
    private static final String STUDY_GROUP_ATTR = "studyGroup";
    private static final String CREATE_EDIT_PAGE = "user/create-edit";
    private static final String REDIRECT_USER = "redirect:/user";
    private static final String ERROR_403 = "error/403";
    private static final String ERROR_404 = "error/404";
    private static final String DELETE_MESSAGE = "StudyGroup deleted with ID: ";
    private static final String ERROR_WINDOW = "error/bad-request";
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


    @GetMapping("/user")
    public String userPage(HttpSession session, Model model,
                           @RequestParam(defaultValue = "1") String page,
                           @RequestParam(defaultValue = "10") String size,
                           @RequestParam(defaultValue = "") String filterField,
                           @RequestParam(defaultValue = "") String filterValue,
                           @RequestParam(defaultValue = "id") String sortBy) {
        try {
            int pageNum = Integer.parseInt(page);
            int pageSize = Integer.parseInt(size);
            
            if (pageNum < 1 || pageSize < 1) {
                model.addAttribute(ERROR_MESSAGE, "Номер страницы и размер должны быть положительными числами");
                return ERROR_WINDOW;
            }
            String username = (String) session.getAttribute(USERNAME_ATTR);
            Boolean isAdmin = (Boolean) session.getAttribute(IS_ADMIN_ATTR);

            if (username == null || isAdmin == null) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                username = authentication.getName();
                isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(role -> role.getAuthority().equals(ROLE_ADMIN));
                session.setAttribute(USERNAME_ATTR, username);
                session.setAttribute(IS_ADMIN_ATTR, isAdmin);
            }

            
            model.addAttribute(USERNAME_ATTR, username);
            model.addAttribute(IS_ADMIN_ATTR, isAdmin);

          
            Page<StudyGroup> studyGroups = studyGroupService.findFilteredAndSorted(
                pageNum, pageSize, filterField, filterValue, sortBy);

            model.addAttribute("studyGroups", studyGroups);
            model.addAttribute("currentPage", pageNum);
            model.addAttribute("totalPages", studyGroups.getTotalPages());
            model.addAttribute("filterField", filterField);
            model.addAttribute("filterValue", filterValue);
            model.addAttribute("sortBy", sortBy);

            return "user/main-page";

        } catch (NumberFormatException e) {
            model.addAttribute(ERROR_MESSAGE, 
                "Неверный формат параметров пагинации. Пожалуйста, используйте целые числа для номера страницы и размера.");
            return ERROR_WINDOW;
        } catch (Exception e) {
            model.addAttribute(ERROR_MESSAGE, ERROR_REQUEST_MESSAGE + e.getMessage());
            return ERROR_WINDOW;
        }
    }

    @GetMapping("/user/create")
    public String showCreateForm(HttpSession session, Model model) {
        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
        model.addAttribute(STUDY_GROUP_ATTR, new StudyGroup());
        model.addAttribute(PERSON_ATTR, new Person());
        createFullModel(model);
        return "user/create-edit";
    }


    @PostMapping("/user/group/create")
    public String createStudyGroup(@Valid @ModelAttribute(STUDY_GROUP_ATTR ) StudyGroup studyGroup,
                                   BindingResult result, Principal principal, Model model, HttpSession session) {
        if (result.hasErrors()) {
            createFullModel(model);
            model.addAttribute(PERSON_ATTR, new Person());
            return "user/create-edit";
        }
        User currentUser = userService.findByUsername(principal.getName());
        studyGroup.setCreatedBy(currentUser);
        studyGroupService.save(studyGroup);
        webSocketController.notifyClients("Create new studyGroup");
        return "user/create-edit";
    }

    @GetMapping("/user/group/edit/{id}")
    public String showStudyGroupEditForm(HttpSession session, 
                                       @PathVariable String id,
                                       Principal principal, 
                                       Model model) {
        try {
            int studyGroupId = Integer.parseInt(id);

            StudyGroup studyGroup = studyGroupService.getById(studyGroupId);
            if (studyGroup == null) {
                model.addAttribute(ERROR_MESSAGE, GROUP_ID_MESSAGE + id + NOT_FOUND_MESSAGE);
                return ERROR_404;
            }
            if (!studyGroup.isCanBeChanged()) {
                model.addAttribute(ERROR_MESSAGE, "Эта группа не может быть изменена");
                return ERROR_403;
            }

            String username = principal.getName();
            boolean isCreator = username.equals(studyGroup.getCreatedBy().getUsername());
            boolean isAdmin = isAdmin(principal);

            if (!isCreator && !isAdmin) {
                model.addAttribute(ERROR_MESSAGE, 
                    "У вас нет прав для редактирования этой группы. Только создатель или администратор может её редактировать");
                return ERROR_403;
            }

            model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
            model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
            model.addAttribute(STUDY_GROUP_ATTR , studyGroup);
            model.addAttribute(PERSON_ATTR, new Person());
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

    @PostMapping("/user/group/edit/{id}")
    public String updateStudyGroup(@PathVariable String id, 
                                 @Valid @ModelAttribute StudyGroup studyGroup, 
                                 BindingResult result,
                                 Principal principal, 
                                 Model model) {
        try {
            int studyGroupId = Integer.parseInt(id);

            StudyGroup existingGroup = studyGroupService.getById(studyGroupId);
            if (existingGroup == null) {
                model.addAttribute(ERROR_MESSAGE, GROUP_ID_MESSAGE + id + NOT_FOUND_MESSAGE);
                return ERROR_WINDOW;
            }

            model.addAttribute(PERSON_ATTR, new Person());

      
            if (result.hasErrors()) {
                createFullModel(model);
                return CREATE_EDIT_PAGE;
            }

            if (!existingGroup.isCanBeChanged()) {
                model.addAttribute(ERROR_MESSAGE, "Эта группа не может быть изменена");
                return ERROR_403;
            }

            String username = principal.getName();
            boolean isCreator = username.equals(existingGroup.getCreatedBy().getUsername());
            boolean isAdmin = isAdmin(principal);

            if (!isCreator && !isAdmin) {
                model.addAttribute(ERROR_MESSAGE, 
                    "У вас нет прав для редактирования этой группы. Только создатель или администратор может её редактировать");
                return ERROR_403;
            }

       
            studyGroupService.update(studyGroupId, studyGroup);
            webSocketController.notifyClients(DELETE_MESSAGE);
            return REDIRECT_USER;

        } catch (NumberFormatException e) {
            model.addAttribute(ERROR_MESSAGE, INVALID_ID_FORMAT);
            return ERROR_WINDOW;
        } catch (Exception e) {
            model.addAttribute(ERROR_MESSAGE, ERROR_REQUEST_MESSAGE + e.getMessage());
            return ERROR_WINDOW;
        }
    }

    @PostMapping("/user/group/delete/{id}")
    public String deleteStudyGroup(@PathVariable String id, 
                                 Principal principal, 
                                 Model model) {
        try {
            int groupId = Integer.parseInt(id);
            StudyGroup studyGroup = studyGroupService.getById(groupId);
            
            if (studyGroup == null) {
                model.addAttribute(ERROR_MESSAGE, GROUP_ID_MESSAGE + id + NOT_FOUND_MESSAGE);
                return ERROR_WINDOW;
            }

            if (!principal.getName().equals(studyGroup.getCreatedBy().getUsername()) && !isAdmin(principal)) {
                model.addAttribute(ERROR_MESSAGE, "У вас нет прав для удаления этой группы");
                return ERROR_403;
            }

            studyGroupService.deleteById(groupId);
            webSocketController.notifyClients(DELETE_MESSAGE);
            return REDIRECT_USER;

        } catch (NumberFormatException e) {
            model.addAttribute(ERROR_MESSAGE, INVALID_ID_FORMAT);
            return ERROR_WINDOW;
        } catch (Exception e) {
            model.addAttribute(ERROR_MESSAGE, "Произошла ошибка при удалении: " + e.getMessage());
            return ERROR_WINDOW;
        }
    }



    @GetMapping("/user/find")
    public String findStudyGroup(@RequestParam("id") String id, Model model) {
        try {
            Long longId = Long.parseLong(id);
            StudyGroup studyGroup = studyGroupService.getById(Math.toIntExact(longId));
            model.addAttribute(STUDY_GROUP_ATTR, studyGroup);
            return "user/info-details";
        } catch (NumberFormatException e) {
            model.addAttribute(ERROR_MESSAGE, "Неверный формат ID. Пожалуйста, введите числовое значение.");
            return ERROR_WINDOW;
        } catch (RuntimeException e) {
            return ERROR_404;
        }
    }


    @GetMapping("/user/visualization")
    public String showVisualization(HttpSession session, Model model) {
        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
        return "user/visualization-page";
    }


    @GetMapping("/user/study-groups")
    public ResponseEntity<List<StudyGroup>> getStudyGroups() {
        List<StudyGroup> studyGroups =  studyGroupService.findAll();
        return ResponseEntity.ok(studyGroups);
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
