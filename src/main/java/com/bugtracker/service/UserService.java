package com.bugtracker.service;

import com.bugtracker.dto.UserDto;
import com.bugtracker.entity.Role;
import com.bugtracker.entity.RoleName;
import com.bugtracker.entity.User;
import com.bugtracker.exception.ResourceNotFoundException;
import com.bugtracker.repository.RoleRepository;
import com.bugtracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    @Transactional(readOnly = true)
    public List<User> findAllDevelopers() {
        return userRepository.findAllDevelopers();
    }

    @Transactional(readOnly = true)
    public List<User> findAllTesters() {
        return userRepository.findAllTesters();
    }

    public User registerUser(UserDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + dto.getUsername());
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + dto.getEmail());
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());

        String roleName = (dto.getRole() == null || dto.getRole().isBlank()) ? "TESTER" : dto.getRole();
        RoleName rn = RoleName.valueOf("ROLE_" + roleName.toUpperCase());
        Role role = roleRepository.findByName(rn)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + rn));
        user.setRoles(Set.of(role));

        log.info("Registering new user: {}", user.getUsername());
        return userRepository.save(user);
    }

    public User updateUser(Long id, UserDto dto) {
        User user = findById(id);
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setEnabled(dto.isEnabled());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            RoleName rn = RoleName.valueOf("ROLE_" + dto.getRole().toUpperCase());
            Role role = roleRepository.findByName(rn)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + rn));
            user.setRoles(Set.of(role));
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = findById(id);
        userRepository.delete(user);
        log.info("Deleted user: {}", user.getUsername());
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
