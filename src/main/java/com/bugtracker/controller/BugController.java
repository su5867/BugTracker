package com.bugtracker.controller;

import com.bugtracker.dto.BugDto;
import com.bugtracker.dto.CommentDto;
import com.bugtracker.entity.*;
import com.bugtracker.security.CustomUserDetails;
import com.bugtracker.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/bugs")
@RequiredArgsConstructor
@Slf4j
public class BugController {

    private final BugService         bugService;
    private final CommentService     commentService;
    private final ProjectService     projectService;
    private final UserService        userService;
    private final FileStorageService fileStorageService;

    // ── List ──────────────────────────────────────────────────────────────
    @GetMapping
    public String listBugs(@RequestParam(required = false) String status,
                           @RequestParam(required = false) String priority,
                           @RequestParam(required = false) Long projectId,
                           @RequestParam(required = false) Long assignedUserId,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model) {

        BugStatus bugStatus   = parseEnum(BugStatus.class,  status);
        Priority  bugPriority = parseEnum(Priority.class, priority);

        Page<Bug> bugs = bugService.findBugsWithFilters(
            bugStatus, bugPriority, projectId, assignedUserId, keyword,
            PageRequest.of(page, size, Sort.by("createdAt").descending())
        );

        model.addAttribute("bugs",                 bugs);
        model.addAttribute("projects",             projectService.findAll());
        model.addAttribute("developers",           userService.findAllDevelopers());
        model.addAttribute("statuses",             BugStatus.values());
        model.addAttribute("priorities",           Priority.values());
        model.addAttribute("currentStatus",        status);
        model.addAttribute("currentPriority",      priority);
        model.addAttribute("currentProjectId",     projectId);
        model.addAttribute("currentAssignedUserId",assignedUserId);
        model.addAttribute("keyword",              keyword);
        return "bug/list";
    }

    // ── New Bug Form ──────────────────────────────────────────────────────
    @GetMapping("/new")
    public String newBugForm(@RequestParam(required = false) Long projectId, Model model) {
        BugDto dto = new BugDto();
        if (projectId != null) dto.setProjectId(projectId);
        model.addAttribute("bugDto",     dto);
        model.addAttribute("projects",   projectService.findAll());
        model.addAttribute("developers", userService.findAllDevelopers());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("severities", Severity.values());
        model.addAttribute("isNew",      true);
        return "bug/form";
    }

    // ── Create Bug ────────────────────────────────────────────────────────
    @PostMapping
    public String createBug(@Valid @ModelAttribute("bugDto") BugDto dto,
                            BindingResult result,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("projects",   projectService.findAll());
            model.addAttribute("developers", userService.findAllDevelopers());
            model.addAttribute("priorities", Priority.values());
            model.addAttribute("severities", Severity.values());
            model.addAttribute("isNew",      true);
            return "bug/form";
        }
        try {
            Bug bug = bugService.createBug(dto, userDetails.getUser());
            redirectAttributes.addFlashAttribute("success",
                "Bug #" + bug.getId() + " created successfully.");
            return "redirect:/bugs/" + bug.getId();
        } catch (IOException e) {
            model.addAttribute("error", "Failed to upload attachment: " + e.getMessage());
            model.addAttribute("projects",   projectService.findAll());
            model.addAttribute("developers", userService.findAllDevelopers());
            model.addAttribute("priorities", Priority.values());
            model.addAttribute("severities", Severity.values());
            model.addAttribute("isNew",      true);
            return "bug/form";
        }
    }

    // ── Bug Detail (uses JOIN FETCH to avoid LazyInitializationException) ─
    @GetMapping("/{id}")
    public String viewBug(@PathVariable Long id, Model model,
                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        Bug bug = bugService.findByIdWithDetails(id);
        model.addAttribute("bug",         bug);
        model.addAttribute("commentDto",  new CommentDto());
        model.addAttribute("statuses",    BugStatus.values());
        model.addAttribute("currentUser", userDetails.getUser());
        return "bug/detail";
    }

    // ── Edit Bug Form ─────────────────────────────────────────────────────
    @GetMapping("/{id}/edit")
    public String editBugForm(@PathVariable Long id, Model model) {
        Bug bug = bugService.findById(id);
        BugDto dto = new BugDto();
        dto.setId(bug.getId());
        dto.setTitle(bug.getTitle());
        dto.setDescription(bug.getDescription());
        dto.setPriority(bug.getPriority());
        dto.setSeverity(bug.getSeverity());
        dto.setStatus(bug.getStatus());
        dto.setProjectId(bug.getProject().getId());
        if (bug.getAssignedTo() != null) dto.setAssignedToId(bug.getAssignedTo().getId());

        model.addAttribute("bugDto",     dto);
        model.addAttribute("bug",        bug);
        model.addAttribute("projects",   projectService.findAll());
        model.addAttribute("developers", userService.findAllDevelopers());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("severities", Severity.values());
        model.addAttribute("statuses",   BugStatus.values());
        model.addAttribute("isNew",      false);
        return "bug/form";
    }

    // ── Update Bug ────────────────────────────────────────────────────────
    @PostMapping("/{id}/update")
    public String updateBug(@PathVariable Long id,
                            @Valid @ModelAttribute("bugDto") BugDto dto,
                            BindingResult result,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("bug",        bugService.findById(id));
            model.addAttribute("projects",   projectService.findAll());
            model.addAttribute("developers", userService.findAllDevelopers());
            model.addAttribute("priorities", Priority.values());
            model.addAttribute("severities", Severity.values());
            model.addAttribute("statuses",   BugStatus.values());
            model.addAttribute("isNew",      false);
            return "bug/form";
        }
        try {
            bugService.updateBug(id, dto, userDetails.getUser());
            redirectAttributes.addFlashAttribute("success", "Bug updated successfully.");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error",
                "Bug saved but file upload failed: " + e.getMessage());
        }
        return "redirect:/bugs/" + id;
    }

    // ── Quick Status Update ───────────────────────────────────────────────
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            BugStatus newStatus = BugStatus.valueOf(status.toUpperCase());
            bugService.updateStatus(id, newStatus, userDetails.getUser());
            redirectAttributes.addFlashAttribute("success", "Status updated to " + status);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid status: " + status);
        }
        return "redirect:/bugs/" + id;
    }

    // ── Delete Bug ────────────────────────────────────────────────────────
    @PostMapping("/{id}/delete")
    public String deleteBug(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bugService.deleteBug(id);
        redirectAttributes.addFlashAttribute("success", "Bug deleted successfully.");
        return "redirect:/bugs";
    }

    // ── Add Comment ───────────────────────────────────────────────────────
    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable Long id,
                             @Valid @ModelAttribute("commentDto") CommentDto dto,
                             BindingResult result,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Comment cannot be empty.");
            return "redirect:/bugs/" + id;
        }
        Bug bug = bugService.findById(id);
        dto.setBugId(id);
        commentService.addComment(dto, bug, userDetails.getUser());
        redirectAttributes.addFlashAttribute("success", "Comment added.");
        return "redirect:/bugs/" + id;
    }

    // ── Download Attachment ───────────────────────────────────────────────
    @GetMapping("/{bugId}/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long bugId,
                                                       @PathVariable Long attachmentId) {
        Attachment attachment = bugService.getAttachment(attachmentId);
        try {
            Resource resource = fileStorageService.load(attachment.getFilePath());
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + attachment.getOriginalName() + "\"")
                .body(resource);
        } catch (Exception e) {
            log.error("Failed to serve attachment {}: {}", attachmentId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────
    private <T extends Enum<T>> T parseEnum(Class<T> type, String value) {
        if (value == null || value.isBlank()) return null;
        try { return Enum.valueOf(type, value.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }
}
