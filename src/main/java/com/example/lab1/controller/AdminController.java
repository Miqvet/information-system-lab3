package com.example.lab1.controller;

import com.example.lab1.domain.entity.Person;
import com.example.lab1.domain.entity.auth.Role;
import com.example.lab1.domain.entity.auth.User;
import com.example.lab1.domain.entity.enums.RoleName;
import com.example.lab1.repository.auth.RoleRepository;
import com.example.lab1.repository.auth.UserRepository;
import com.example.lab1.service.StudyGroupService;

import static com.example.lab1.common.AppConstants.*;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final StudyGroupService studyGroupService;


    @GetMapping("/approves")
    public String adminPage(HttpSession session, Model model, HttpServletResponse response) {
        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
        model.addAttribute("requests", userRepository.findByWishToBeAdmin(true));
        response.setStatus(HttpServletResponse.SC_OK);
        return "admin/index";
    }


    @PostMapping("/approves")
    public String approveAdminRequest(@RequestParam("userId") Long userId, 
                                    Model model, 
                                    HttpServletResponse response) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        Role userRole = roleRepository.findByName(RoleName.valueOf("ROLE_ADMIN"))
                .orElseThrow(() -> new RuntimeException("Role not found"));
            
        user.setWishToBeAdmin(false);
        user.setRole(userRole);
        userRepository.save(user);
        response.setStatus(HttpServletResponse.SC_OK);
        return "admin/index";

    }


    @GetMapping("/admin-page")
    public String adminPageRequest(HttpSession session, Model model, HttpServletResponse response) {

        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
        response.setStatus(HttpServletResponse.SC_OK);
        return ADMIN_PAGE_VIEW;

    }

    @PostMapping("/admin-page/less-than")
    public String countShouldBeExpelledLessThan(@RequestParam(value = "threshold1", required = false) String threshold, 
                                              Model model, 
                                              HttpSession session,
                                              HttpServletResponse response) {
        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
        if (threshold == null || threshold.isEmpty() || !threshold.matches("\\d+")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("message1", THRESHOLD_ERROR_MSG);
            return ADMIN_PAGE_VIEW;
        }

        int thresholdInt = Integer.parseInt(threshold);
        long count = studyGroupService.countByShouldBeExpelledLessThan(thresholdInt);
        model.addAttribute("countLessThan", count);
        response.setStatus(HttpServletResponse.SC_OK);
        return ADMIN_PAGE_VIEW;

    }

    @PostMapping("/admin-page/greater-than")
    public String countShouldBeExpelledGreaterThan(@RequestParam(value = "threshold2", required = false) String threshold, 
                                                 Model model, 
                                                 HttpSession session,
                                                 HttpServletResponse response) {
        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
        if (threshold == null || threshold.isEmpty() || !threshold.matches("\\d+")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("message2", THRESHOLD_ERROR_MSG);
            return ADMIN_PAGE_VIEW;
        }

        int thresholdInt = Integer.parseInt(threshold);
        long count = studyGroupService.countByShouldBeExpelledGreaterThan(thresholdInt);
        model.addAttribute("countGreaterThan", count);
        response.setStatus(HttpServletResponse.SC_OK);
        return ADMIN_PAGE_VIEW;

    }


    @GetMapping("/admin-page/unique-admins")
    public String getUniqueGroupAdmins(HttpSession session, Model model, HttpServletResponse response) {
        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
        List<Person> uniqueAdmins = studyGroupService.findUniqueGroupAdmins();
        model.addAttribute("uniqueAdmins", uniqueAdmins);
        response.setStatus(HttpServletResponse.SC_OK);
        return ADMIN_PAGE_VIEW;

    }


    @PostMapping("/admin-page/expel-group")
    public String expelGroupStudents(@RequestParam(value = "groupId", required = false) String groupId, 
                                   Model model, 
                                   HttpSession session,
                                   HttpServletResponse response) {
        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
        if (groupId == null || groupId.isEmpty() || !groupId.matches("\\d+")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute(MESSAGE3_ATTR, "ID группы должно быть числом");
            return ADMIN_PAGE_VIEW;
        }

        if (!studyGroupService.getById(Integer.parseInt(groupId)).isCanBeChanged()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            model.addAttribute(MESSAGE3_ATTR, "Группа должна быть изменяемой");
            return ADMIN_PAGE_VIEW;
        }

        int groupIdInt = Integer.parseInt(groupId);
        studyGroupService.expelGroupStudents(groupIdInt);
        response.setStatus(HttpServletResponse.SC_OK);
        return ADMIN_PAGE_VIEW;

    }

    @PostMapping("/admin-page/transfer-students")
    public String transferStudents(@RequestParam(value = "fromGroupId", required = false) String fromGroupId,
                                   @RequestParam(value = "toGroupId", required = false) String toGroupId,
                                   Model model, 
                                   HttpSession session,
                                   HttpServletResponse response) {
        model.addAttribute(USERNAME_ATTR, session.getAttribute(USERNAME_ATTR));
        model.addAttribute(IS_ADMIN_ATTR, session.getAttribute(IS_ADMIN_ATTR));
        if (fromGroupId == null || fromGroupId.isEmpty() || toGroupId == null || toGroupId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute(MESSAGE4_ATTR, "ID группы не может быть пустым");
            return ADMIN_PAGE_VIEW;
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (!studyGroupService.getById(Integer.parseInt(fromGroupId)).isCanBeChanged()) {
            stringBuilder.append("Group ").append(fromGroupId).append(" ");
        }
        if(!studyGroupService.getById(Integer.parseInt(toGroupId)).isCanBeChanged()){
            stringBuilder.append("Group ").append(toGroupId).append(" ");
        }
        if(!stringBuilder.isEmpty()){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            stringBuilder.append("must be can changed");
            model.addAttribute(MESSAGE4_ATTR, stringBuilder.toString());
            return ADMIN_PAGE_VIEW;
        }

        int fromGroupIdInt = Integer.parseInt(fromGroupId);
        int toGroupIdInt = Integer.parseInt(toGroupId);

        studyGroupService.transferStudents(fromGroupIdInt, toGroupIdInt);
        response.setStatus(HttpServletResponse.SC_OK);
        return ADMIN_PAGE_VIEW;
    }
}
