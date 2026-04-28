package com.bugtracker.service;

import com.bugtracker.dto.CommentDto;
import com.bugtracker.entity.Bug;
import com.bugtracker.entity.Comment;
import com.bugtracker.entity.User;
import com.bugtracker.exception.ResourceNotFoundException;
import com.bugtracker.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    public Comment addComment(CommentDto dto, Bug bug, User user) {
        Comment comment = new Comment();
        comment.setContent(dto.getContent());
        comment.setBug(bug);
        comment.setUser(user);
        Comment saved = commentRepository.save(comment);
        notificationService.notifyCommentAdded(bug, user);
        log.info("Comment added to bug #{} by {}", bug.getId(), user.getUsername());
        return saved;
    }

    public void deleteComment(Long id, User requestingUser) {
        Comment comment = commentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", id));
        // Only owner or admin can delete
        boolean isOwner = comment.getUser().getId().equals(requestingUser.getId());
        boolean isAdmin = requestingUser.getRoles().stream()
            .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"));
        if (!isOwner && !isAdmin) {
            throw new com.bugtracker.exception.UnauthorizedException("You cannot delete this comment");
        }
        commentRepository.delete(comment);
    }
}
