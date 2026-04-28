package com.bugtracker.repository;

import com.bugtracker.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BugRepository extends JpaRepository<Bug, Long>, JpaSpecificationExecutor<Bug> {

    Page<Bug> findByProject(Project project, Pageable pageable);
    Page<Bug> findByAssignedTo(User user, Pageable pageable);
    Page<Bug> findByCreatedBy(User user, Pageable pageable);
    List<Bug> findByAssignedToAndStatus(User user, BugStatus status);

    // ── Eagerly load all associations for the detail page ──
    @Query("SELECT DISTINCT b FROM Bug b " +
           "LEFT JOIN FETCH b.comments c " +
           "LEFT JOIN FETCH c.user " +
           "LEFT JOIN FETCH b.attachments " +
           "LEFT JOIN FETCH b.history h " +
           "LEFT JOIN FETCH h.changedBy " +
           "LEFT JOIN FETCH b.assignedTo " +
           "LEFT JOIN FETCH b.createdBy " +
           "LEFT JOIN FETCH b.project " +
           "WHERE b.id = :id")
    Optional<Bug> findByIdWithDetails(@Param("id") Long id);

    // ── Count queries ──
    @Query("SELECT COUNT(b) FROM Bug b WHERE b.status = :status")
    long countByStatus(@Param("status") BugStatus status);

    // FIX: use enum constant, not string literal
    @Query("SELECT COUNT(b) FROM Bug b WHERE b.assignedTo = :user AND b.status <> com.bugtracker.entity.BugStatus.CLOSED")
    long countOpenByAssignedTo(@Param("user") User user);

    @Query("SELECT COUNT(b) FROM Bug b WHERE b.createdBy = :user")
    long countByCreatedBy(@Param("user") User user);

    @Query("SELECT COUNT(b) FROM Bug b WHERE b.assignedTo = :user AND b.status = :status")
    long countByAssignedToAndStatus(@Param("user") User user, @Param("status") BugStatus status);

    @Query("SELECT COUNT(b) FROM Bug b WHERE b.createdBy = :user AND b.status = :status")
    long countByCreatedByAndStatus(@Param("user") User user, @Param("status") BugStatus status);

    @Query("SELECT b.priority, COUNT(b) FROM Bug b GROUP BY b.priority")
    List<Object[]> countGroupByPriority();

    @Query("SELECT b.status, COUNT(b) FROM Bug b GROUP BY b.status")
    List<Object[]> countGroupByStatus();
}
