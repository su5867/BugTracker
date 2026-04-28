package com.bugtracker.repository;

import com.bugtracker.entity.Project;
import com.bugtracker.entity.ProjectStatus;
import com.bugtracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByStatus(ProjectStatus status);

    @Query("SELECT p FROM Project p JOIN p.members m WHERE m = :user")
    List<Project> findByMember(@Param("user") User user);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    long countByStatus(@Param("status") ProjectStatus status);

    // Eagerly fetch members and bugs for the detail page to avoid lazy-load issues
    @Query("SELECT DISTINCT p FROM Project p " +
           "LEFT JOIN FETCH p.members " +
           "LEFT JOIN FETCH p.bugs b " +
           "LEFT JOIN FETCH b.assignedTo " +
           "LEFT JOIN FETCH b.createdBy " +
           "WHERE p.id = :id")
    Optional<Project> findByIdWithDetails(@Param("id") Long id);
}
