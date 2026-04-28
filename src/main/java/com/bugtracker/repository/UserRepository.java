package com.bugtracker.repository;

import com.bugtracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = com.bugtracker.entity.RoleName.ROLE_DEVELOPER")
    List<User> findAllDevelopers();

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = com.bugtracker.entity.RoleName.ROLE_TESTER")
    List<User> findAllTesters();
}
