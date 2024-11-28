package com.example.lab1.controller;

import com.example.lab1.domain.entity.StudyGroup;
import com.example.lab1.domain.entity.Person;
import com.example.lab1.domain.entity.auth.User;
import com.example.lab1.domain.entity.enums.Color;
import com.example.lab1.domain.entity.enums.Country;
import com.example.lab1.domain.entity.enums.FormOfEducation;
import com.example.lab1.domain.entity.enums.Semester;
import com.example.lab1.service.ImportService;
import com.example.lab1.service.PersonService;
import com.example.lab1.service.StudyGroupService;
import com.example.lab1.service.UserService;
import static com.example.lab1.common.AppConstants.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
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
    private final StudyGroupService studyGroupService;
    private final UserService userService;
    private final PersonService personService;
    private final WebSocketController webSocketController;
    private final ImportService importService;


    @GetMapping("/user")
    public String userPage(HttpSession session, Model model,
                           @RequestParam(defaultValue = "1") String page,
                           @RequestParam(defaultValue = "10") String size,
                           @RequestParam(defaultValue = "") String filterField,
                           @RequestParam(defaultValue = "") String filterValue,
                           @RequestParam(defaultValue = "id") String sortBy,
                           HttpServletResponse response) {

        int pageNum = Integer.parseInt(page);
        int pageSize = Integer.parseInt(size);
            
        if (pageNum < 1 || pageSize < 1) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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


    }

    @GetMapping("/user/create")
    public String showCreateForm(HttpSession session, Model model, HttpServletResponse response) {
        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
        model.addAttribute(STUDY_GROUP_ATTR, new StudyGroup());
        model.addAttribute(PERSON_ATTR, new Person());
        createFullModel(model);
        return CREATE_EDIT_PAGE;
    }


    @PostMapping("/user/group/create")
    public String createStudyGroup(@Valid @ModelAttribute(STUDY_GROUP_ATTR) StudyGroup studyGroup,
                                   BindingResult result,
                                   Principal principal,
                                   Model model,
                                   HttpServletResponse response) {
        if (result.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            createFullModel(model);
            model.addAttribute(PERSON_ATTR, new Person());
            return CREATE_EDIT_PAGE;
        }
        User currentUser = userService.findByUsername(principal.getName());
        studyGroup.setCreatedBy(currentUser);
        studyGroupService.save(studyGroup);
        webSocketController.notifyClients(CREATE_MESSAGE);

        return REDIRECT_USER;

    }

    @GetMapping("/user/group/edit/{id}")
    public String showStudyGroupEditForm(HttpSession session, 
                                       @PathVariable String id,
                                       Principal principal, 
                                       Model model,
                                       HttpServletResponse response) {
        int studyGroupId = Integer.parseInt(id);
        StudyGroup studyGroup = studyGroupService.getById(studyGroupId);

        if (!studyGroup.isCanBeChanged()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            model.addAttribute(ERROR_MESSAGE, "Эта группа не может быть изменена");
            return ERROR_403;
        }

        String username = principal.getName();
        boolean isCreator = username.equals(studyGroup.getCreatedBy().getUsername());
        boolean isAdmin = isAdmin(principal);

        if (!isCreator && !isAdmin) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            model.addAttribute(ERROR_MESSAGE, "У вас нет прав для редактирования этой группы...");
            return ERROR_403;
        }

        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
        model.addAttribute(STUDY_GROUP_ATTR , studyGroup);
        model.addAttribute(PERSON_ATTR, new Person());
        createFullModel(model);
        return CREATE_EDIT_PAGE;


    }

    @PostMapping("/user/group/edit/{id}")
    public String updateStudyGroup(@PathVariable String id,
                                   @Valid @ModelAttribute StudyGroup studyGroup,
                                   BindingResult result,
                                   Principal principal,
                                   Model model,
                                   HttpServletResponse response) {
        var studyGroupId = Integer.parseInt(id);
        StudyGroup existingGroup = studyGroupService.getById(studyGroupId);

        model.addAttribute(PERSON_ATTR, new Person());

        if (result.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            createFullModel(model);
            return CREATE_EDIT_PAGE;
        }

        if (!existingGroup.isCanBeChanged()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            model.addAttribute(ERROR_MESSAGE, "Эта группа не может быть изменена");
            return ERROR_403;
        }

        String username = principal.getName();
        boolean isCreator = username.equals(existingGroup.getCreatedBy().getUsername());
        boolean isAdmin = isAdmin(principal);

        if (!isCreator && !isAdmin) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            model.addAttribute(ERROR_MESSAGE,
                        "У вас нет прав для редактирования этой группы. Только создатель или администратор может её редактировать");
            return ERROR_403;
        }

        studyGroupService.update(studyGroupId, studyGroup);
        webSocketController.notifyClients(EDIT_MESSAGE + studyGroupId);
        return REDIRECT_USER;
    }
    @GetMapping("/user/group/delete/{id}")
    public String getDeleteStudyGroup(@PathVariable String id,
                                      Principal principal,
                                      Model model,
                                      HttpServletResponse response) {

        int groupId = Integer.parseInt(id);
        StudyGroup studyGroup = studyGroupService.getById(groupId);

        if (!principal.getName().equals(studyGroup.getCreatedBy().getUsername()) && !isAdmin(principal)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            model.addAttribute(ERROR_MESSAGE, "У вас нет прав для удаления этой группы");
            return ERROR_403;
        }

        studyGroupService.deleteById(groupId);
        webSocketController.notifyClients(DELETE_MESSAGE + groupId);
        return REDIRECT_USER;
    }

    @GetMapping("/user/find")
    public String findStudyGroup(@RequestParam("id") String id, 
                           Model model,
                           HttpServletResponse response) {
        long longId = Long.parseLong(id);
        StudyGroup studyGroup = studyGroupService.getById(Math.toIntExact(longId));
        model.addAttribute(STUDY_GROUP_ATTR, studyGroup);
        return "user/info-details";
    }


    @GetMapping("/user/visualization")
    public String showVisualization(HttpSession session, 
                              Model model,
                              HttpServletResponse response) {

        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
        return "user/visualization-page";

    }


    @GetMapping("/user/study-groups")
    public ResponseEntity<List<StudyGroup>> getStudyGroups() {
        List<StudyGroup> studyGroups =  studyGroupService.findAll();
        return ResponseEntity.ok(studyGroups);
    }


    @PostMapping("/user/import")
    public String importStudyGroups(@RequestParam("file") MultipartFile file, Model model) {
        try {
            importService.importStudyGroupsFromJson(file);
            model.addAttribute("message", "Импорт успешно завершен");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            model.addAttribute("error", "Ошибка при импорте: " + e.getMessage());
        }
        return "redirect:/user";
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
