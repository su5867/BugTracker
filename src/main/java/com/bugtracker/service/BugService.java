package com.bugtracker.service;

import com.bugtracker.dto.BugDto;
import com.bugtracker.entity.*;
import com.bugtracker.exception.ResourceNotFoundException;
import com.bugtracker.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BugService {

    private final BugRepository        bugRepository;
    private final ProjectRepository    projectRepository;
    private final UserRepository       userRepository;
    private final AttachmentRepository attachmentRepository;
    private final BugHistoryRepository bugHistoryRepository;
    private final FileStorageService   fileStorageService;
    private final NotificationService  notificationService;

    // ── Read ──────────────────────────────────────────────────────────────

    /** Simple load – used for edit/status-update where we don't need lazy collections */
    @Transactional(readOnly = true)
    public Bug findById(Long id) {
        return bugRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bug", id));
    }

    /** Full load with JOIN FETCH – used for the detail view page */
    @Transactional(readOnly = true)
    public Bug findByIdWithDetails(Long id) {
        return bugRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bug", id));
    }

    @Transactional(readOnly = true)
    public Page<Bug> findBugsWithFilters(BugStatus status, Priority priority,
                                          Long projectId, Long assignedUserId,
                                          String keyword, Pageable pageable) {
        Specification<Bug> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null)         predicates.add(cb.equal(root.get("status"),   status));
            if (priority != null)       predicates.add(cb.equal(root.get("priority"), priority));
            if (projectId != null)      predicates.add(cb.equal(root.get("project").get("id"), projectId));
            if (assignedUserId != null) predicates.add(cb.equal(root.get("assignedTo").get("id"), assignedUserId));
            if (keyword != null && !keyword.isBlank())
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return bugRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Bug> findByAssignedTo(User user, Pageable pageable) {
        return bugRepository.findByAssignedTo(user, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Bug> findByCreatedBy(User user, Pageable pageable) {
        return bugRepository.findByCreatedBy(user, pageable);
    }

    // ── Count helpers for dashboards ──────────────────────────────────────

    @Transactional(readOnly = true)
    public long countByStatus(BugStatus status) {
        return bugRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countByAssignedToAndStatus(User user, BugStatus status) {
        return bugRepository.countByAssignedToAndStatus(user, status);
    }

    @Transactional(readOnly = true)
    public long countOpenByAssignedTo(User user) {
        return bugRepository.countOpenByAssignedTo(user);
    }

    @Transactional(readOnly = true)
    public long countByCreatedBy(User user) {
        return bugRepository.countByCreatedBy(user);
    }

    @Transactional(readOnly = true)
    public long countByCreatedByAndStatus(User user, BugStatus status) {
        return bugRepository.countByCreatedByAndStatus(user, status);
    }

    @Transactional(readOnly = true)
    public List<Object[]> countGroupByPriority() {
        return bugRepository.countGroupByPriority();
    }

    @Transactional(readOnly = true)
    public List<Object[]> countGroupByStatus() {
        return bugRepository.countGroupByStatus();
    }

    // ── Write ─────────────────────────────────────────────────────────────

    public Bug createBug(BugDto dto, User createdBy) throws IOException {
        Project project = projectRepository.findById(dto.getProjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Project", dto.getProjectId()));

        Bug bug = new Bug();
        bug.setTitle(dto.getTitle());
        bug.setDescription(dto.getDescription());
        bug.setPriority(dto.getPriority());
        bug.setSeverity(dto.getSeverity());
        bug.setStatus(BugStatus.OPEN);
        bug.setProject(project);
        bug.setCreatedBy(createdBy);

        if (dto.getAssignedToId() != null) {
            User dev = userRepository.findById(dto.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("User", dto.getAssignedToId()));
            bug.setAssignedTo(dev);
        }

        Bug saved = bugRepository.save(bug);

        if (dto.getAttachment() != null && !dto.getAttachment().isEmpty()) {
            saveAttachment(dto.getAttachment(), saved, createdBy);
        }
        addHistory(saved, createdBy, "Status", null, BugStatus.OPEN.name());
        if (saved.getAssignedTo() != null) {
            notificationService.notifyBugAssigned(saved, saved.getAssignedTo());
        }
        log.info("Bug created: {} (ID: {})", saved.getTitle(), saved.getId());
        return saved;
    }

    public Bug updateBug(Long id, BugDto dto, User updatedBy) throws IOException {
        Bug bug = findById(id);
        String oldStatus   = bug.getStatus().name();
        String oldAssignee = bug.getAssignedTo() != null ? bug.getAssignedTo().getUsername() : "Unassigned";

        bug.setTitle(dto.getTitle());
        bug.setDescription(dto.getDescription());
        bug.setPriority(dto.getPriority());
        bug.setSeverity(dto.getSeverity());

        BugStatus newStatus = dto.getStatus() != null ? dto.getStatus() : bug.getStatus();
        if (!newStatus.equals(bug.getStatus())) {
            addHistory(bug, updatedBy, "Status", oldStatus, newStatus.name());
            notificationService.notifyStatusUpdated(bug, oldStatus, newStatus.name());
        }
        bug.setStatus(newStatus);

        if (dto.getAssignedToId() != null) {
            User newDev = userRepository.findById(dto.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("User", dto.getAssignedToId()));
            if (!newDev.getUsername().equals(oldAssignee)) {
                addHistory(bug, updatedBy, "Assignee", oldAssignee, newDev.getUsername());
                notificationService.notifyBugAssigned(bug, newDev);
            }
            bug.setAssignedTo(newDev);
        } else {
            bug.setAssignedTo(null);
        }

        if (dto.getAttachment() != null && !dto.getAttachment().isEmpty()) {
            saveAttachment(dto.getAttachment(), bug, updatedBy);
        }
        return bugRepository.save(bug);
    }

    public Bug updateStatus(Long id, BugStatus newStatus, User updatedBy) {
        Bug bug = findById(id);
        String oldStatus = bug.getStatus().name();
        if (!newStatus.equals(bug.getStatus())) {
            addHistory(bug, updatedBy, "Status", oldStatus, newStatus.name());
            notificationService.notifyStatusUpdated(bug, oldStatus, newStatus.name());
        }
        bug.setStatus(newStatus);
        return bugRepository.save(bug);
    }

    public void deleteBug(Long id) {
        Bug bug = findById(id);
        bug.getAttachments().forEach(a -> fileStorageService.delete(a.getFilePath()));
        bugRepository.delete(bug);
        log.info("Bug deleted: {}", id);
    }

    public Attachment getAttachment(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void saveAttachment(MultipartFile file, Bug bug, User uploadedBy) throws IOException {
        String path = fileStorageService.store(file);
        Attachment a = new Attachment();
        a.setOriginalName(file.getOriginalFilename());
        a.setFilePath(path);
        a.setFileType(file.getContentType());
        a.setFileSize(file.getSize());
        a.setBug(bug);
        a.setUploadedBy(uploadedBy);
        attachmentRepository.save(a);
    }

    private void addHistory(Bug bug, User changedBy, String field, String oldVal, String newVal) {
        BugHistory h = new BugHistory();
        h.setBug(bug);
        h.setChangedBy(changedBy);
        h.setFieldChanged(field);
        h.setOldValue(oldVal);
        h.setNewValue(newVal);
        bugHistoryRepository.save(h);
    }
}
