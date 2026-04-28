package com.bugtracker.service;

import com.bugtracker.entity.Bug;
import com.bugtracker.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    /**
     * Simulates sending notification when a bug is assigned to a developer.
     */
    public void notifyBugAssigned(Bug bug, User assignedTo) {
        if (assignedTo == null) return;
        log.info("[NOTIFICATION] Bug '{}' (ID: {}) assigned to {} <{}>",
            bug.getTitle(), bug.getId(), assignedTo.getFullName(), assignedTo.getEmail());
    }

    /**
     * Simulates sending notification when bug status changes.
     */
    public void notifyStatusUpdated(Bug bug, String oldStatus, String newStatus) {
        log.info("[NOTIFICATION] Bug '{}' (ID: {}) status changed from {} to {}",
            bug.getTitle(), bug.getId(), oldStatus, newStatus);
        if (bug.getCreatedBy() != null) {
            log.info("[NOTIFICATION] Email sent to reporter: {}", bug.getCreatedBy().getEmail());
        }
        if (bug.getAssignedTo() != null) {
            log.info("[NOTIFICATION] Email sent to assignee: {}", bug.getAssignedTo().getEmail());
        }
    }

    /**
     * Simulates sending notification when a comment is added.
     */
    public void notifyCommentAdded(Bug bug, User commenter) {
        log.info("[NOTIFICATION] New comment on Bug '{}' (ID: {}) by {}",
            bug.getTitle(), bug.getId(), commenter.getUsername());
    }
}
