package com.bugtracker.service;

import com.bugtracker.dto.ProjectDto;
import com.bugtracker.entity.Project;
import com.bugtracker.entity.ProjectStatus;
import com.bugtracker.entity.User;
import com.bugtracker.exception.ResourceNotFoundException;
import com.bugtracker.repository.ProjectRepository;
import com.bugtracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository    userRepository;

    @Transactional(readOnly = true)
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Project findById(Long id) {
        return projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    /** For the detail page – eagerly loads members + bugs to avoid lazy init errors */
    @Transactional(readOnly = true)
    public Project findByIdWithDetails(Long id) {
        return projectRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    @Transactional(readOnly = true)
    public List<Project> findByMember(User user) {
        return projectRepository.findByMember(user);
    }

    @Transactional(readOnly = true)
    public List<Project> findByStatus(ProjectStatus status) {
        return projectRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countByStatus(ProjectStatus status) {
        return projectRepository.countByStatus(status);
    }

    public Project createProject(ProjectDto dto, User createdBy) {
        Project project = new Project();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setStartDate(dto.getStartDate());
        project.setStatus(dto.getStatus() != null ? dto.getStatus() : ProjectStatus.ACTIVE);
        project.setCreatedBy(createdBy);

        if (dto.getMemberIds() != null && !dto.getMemberIds().isEmpty()) {
            Set<User> members = new HashSet<>(userRepository.findAllById(dto.getMemberIds()));
            project.setMembers(members);
        }
        log.info("Creating project: {}", project.getName());
        return projectRepository.save(project);
    }

    public Project updateProject(Long id, ProjectDto dto) {
        Project project = findById(id);
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setStartDate(dto.getStartDate());
        project.setStatus(dto.getStatus());

        Set<User> members = dto.getMemberIds() != null && !dto.getMemberIds().isEmpty()
            ? new HashSet<>(userRepository.findAllById(dto.getMemberIds()))
            : new HashSet<>();
        project.setMembers(members);

        log.info("Updating project: {}", project.getName());
        return projectRepository.save(project);
    }

    public void deleteProject(Long id) {
        Project project = findById(id);
        projectRepository.delete(project);
        log.info("Deleted project: {}", project.getName());
    }
}
