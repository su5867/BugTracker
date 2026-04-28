package com.bugtracker.controller;

import com.bugtracker.dto.ProjectDto;
import com.bugtracker.entity.Project;
import com.bugtracker.entity.User;
import com.bugtracker.security.CustomUserDetails;
import com.bugtracker.service.ProjectService;
import com.bugtracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectService projectService;
    private final UserService    userService;

    // ── List ──────────────────────────────────────────────────────────────
    @GetMapping
    public String listProjects(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User currentUser = userDetails.getUser();
        boolean isAdmin  = isAdmin(currentUser);
        List<Project> projects = isAdmin
            ? projectService.findAll()
            : projectService.findByMember(currentUser);

        model.addAttribute("projects", projects);
        model.addAttribute("isAdmin",  isAdmin);
        return "project/list";
    }

    // ── New form ──────────────────────────────────────────────────────────
    @GetMapping("/new")
    public String newProjectForm(Model model) {
        model.addAttribute("projectDto", new ProjectDto());
        model.addAttribute("allUsers",   userService.findAll());
        return "project/form";
    }

    // ── Create ────────────────────────────────────────────────────────────
    @PostMapping
    public String createProject(@Valid @ModelAttribute("projectDto") ProjectDto dto,
                                BindingResult result,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("allUsers", userService.findAll());
            return "project/form";
        }
        Project project = projectService.createProject(dto, userDetails.getUser());
        redirectAttributes.addFlashAttribute("success",
            "Project '" + project.getName() + "' created successfully.");
        return "redirect:/projects";
    }

    // ── View detail (uses JOIN FETCH to avoid LazyInitializationException) ──
    @GetMapping("/{id}")
    public String viewProject(@PathVariable Long id, Model model) {
        Project project = projectService.findByIdWithDetails(id);
        model.addAttribute("project", project);
        return "project/detail";
    }

    // ── Edit form ─────────────────────────────────────────────────────────
    @GetMapping("/{id}/edit")
    public String editProjectForm(@PathVariable Long id, Model model) {
        Project project = projectService.findByIdWithDetails(id);
        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setStartDate(project.getStartDate());
        dto.setStatus(project.getStatus());
        dto.setMemberIds(project.getMembers().stream()
            .map(User::getId).collect(Collectors.toList()));

        model.addAttribute("projectDto", dto);
        model.addAttribute("project",    project);
        model.addAttribute("allUsers",   userService.findAll());
        return "project/form";
    }

    // ── Update ────────────────────────────────────────────────────────────
    @PostMapping("/{id}/update")
    public String updateProject(@PathVariable Long id,
                                @Valid @ModelAttribute("projectDto") ProjectDto dto,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("allUsers", userService.findAll());
            model.addAttribute("project",  projectService.findById(id));
            return "project/form";
        }
        projectService.updateProject(id, dto);
        redirectAttributes.addFlashAttribute("success", "Project updated successfully.");
        return "redirect:/projects/" + id;
    }

    // ── Delete ────────────────────────────────────────────────────────────
    @PostMapping("/{id}/delete")
    public String deleteProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        projectService.deleteProject(id);
        redirectAttributes.addFlashAttribute("success", "Project deleted successfully.");
        return "redirect:/projects";
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private boolean isAdmin(User user) {
        return user.getRoles().stream()
            .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"));
    }
}
