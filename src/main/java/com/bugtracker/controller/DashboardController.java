package com.bugtracker.controller;

import com.bugtracker.entity.*;
import com.bugtracker.security.CustomUserDetails;
import com.bugtracker.service.BugService;
import com.bugtracker.service.ProjectService;
import com.bugtracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final BugService     bugService;
    private final ProjectService projectService;
    private final UserService    userService;

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User currentUser = userDetails.getUser();

        boolean isAdmin = currentUser.getRoles().stream()
            .anyMatch(r -> r.getName() == RoleName.ROLE_ADMIN);
        boolean isDev = currentUser.getRoles().stream()
            .anyMatch(r -> r.getName() == RoleName.ROLE_DEVELOPER);

        if (isAdmin)     return adminDashboard(model);
        else if (isDev)  return developerDashboard(currentUser, model);
        else             return testerDashboard(currentUser, model);
    }

    // ── Admin ─────────────────────────────────────────────────────────────
    private String adminDashboard(Model model) {
        long openBugs       = bugService.countByStatus(BugStatus.OPEN);
        long inProgressBugs = bugService.countByStatus(BugStatus.IN_PROGRESS);
        long resolvedBugs   = bugService.countByStatus(BugStatus.RESOLVED);
        long closedBugs     = bugService.countByStatus(BugStatus.CLOSED);

        model.addAttribute("totalProjects",  (long) projectService.findAll().size());
        model.addAttribute("activeProjects", projectService.countByStatus(ProjectStatus.ACTIVE));
        model.addAttribute("totalUsers",     (long) userService.findAll().size());
        model.addAttribute("openBugs",       openBugs);
        model.addAttribute("inProgressBugs", inProgressBugs);
        model.addAttribute("resolvedBugs",   resolvedBugs);
        model.addAttribute("closedBugs",     closedBugs);
        return "dashboard/admin";
    }

    // ── Developer ─────────────────────────────────────────────────────────
    private String developerDashboard(User user, Model model) {
        // FIX: use dedicated count queries instead of Page.stream() + MAX_VALUE
        long openBugs       = bugService.countByAssignedToAndStatus(user, BugStatus.OPEN);
        long inProgressBugs = bugService.countByAssignedToAndStatus(user, BugStatus.IN_PROGRESS);
        long resolvedBugs   = bugService.countByAssignedToAndStatus(user, BugStatus.RESOLVED);

        model.addAttribute("openBugs",       openBugs);
        model.addAttribute("inProgressBugs", inProgressBugs);
        model.addAttribute("resolvedBugs",   resolvedBugs);
        return "dashboard/developer";
    }

    // ── Tester ────────────────────────────────────────────────────────────
    private String testerDashboard(User user, Model model) {
        // FIX: use dedicated count queries instead of Page.stream() + MAX_VALUE
        long myReported  = bugService.countByCreatedBy(user);
        long openBugs    = bugService.countByCreatedByAndStatus(user, BugStatus.OPEN);
        long resolvedBugs = bugService.countByCreatedByAndStatus(user, BugStatus.RESOLVED)
                          + bugService.countByCreatedByAndStatus(user, BugStatus.CLOSED);

        model.addAttribute("myReported",   myReported);
        model.addAttribute("openBugs",     openBugs);
        model.addAttribute("resolvedBugs", resolvedBugs);
        return "dashboard/tester";
    }
}
