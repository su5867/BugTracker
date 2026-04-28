package com.bugtracker.config;

import com.bugtracker.entity.*;
import com.bugtracker.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProjectRepository projectRepository;
    private final BugRepository bugRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            log.info("Seed data already exists — skipping initialization.");
            return;
        }

        log.info("Initializing seed data...");

        // Create Roles
        Role adminRole = roleRepository.save(new Role(RoleName.ROLE_ADMIN));
        Role devRole   = roleRepository.save(new Role(RoleName.ROLE_DEVELOPER));
        Role testerRole = roleRepository.save(new Role(RoleName.ROLE_TESTER));

        // Create Admin
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@bugtracker.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFullName("System Administrator");
        admin.setRoles(Set.of(adminRole));
        userRepository.save(admin);

        // Create Developer
        User dev = new User();
        dev.setUsername("dev1");
        dev.setEmail("dev1@bugtracker.com");
        dev.setPassword(passwordEncoder.encode("dev123"));
        dev.setFullName("John Developer");
        dev.setRoles(Set.of(devRole));
        userRepository.save(dev);

        // Create Developer 2
        User dev2 = new User();
        dev2.setUsername("dev2");
        dev2.setEmail("dev2@bugtracker.com");
        dev2.setPassword(passwordEncoder.encode("dev123"));
        dev2.setFullName("Jane Developer");
        dev2.setRoles(Set.of(devRole));
        userRepository.save(dev2);

        // Create Tester
        User tester = new User();
        tester.setUsername("tester1");
        tester.setEmail("tester1@bugtracker.com");
        tester.setPassword(passwordEncoder.encode("test123"));
        tester.setFullName("Alice Tester");
        tester.setRoles(Set.of(testerRole));
        userRepository.save(tester);

        // Create Projects
        Project p1 = new Project();
        p1.setName("E-Commerce Platform");
        p1.setDescription("Online shopping platform with cart and payment integration.");
        p1.setStartDate(LocalDate.now().minusMonths(3));
        p1.setStatus(ProjectStatus.ACTIVE);
        p1.setCreatedBy(admin);
        p1.setMembers(Set.of(dev, dev2, tester));
        projectRepository.save(p1);

        Project p2 = new Project();
        p2.setName("Mobile Banking App");
        p2.setDescription("Secure mobile banking application for iOS and Android.");
        p2.setStartDate(LocalDate.now().minusMonths(1));
        p2.setStatus(ProjectStatus.ACTIVE);
        p2.setCreatedBy(admin);
        p2.setMembers(Set.of(dev, tester));
        projectRepository.save(p2);

        // Create Bugs
        createBug(p1, "Login button not working on Safari",
            "Users report that the login button is unresponsive in Safari browser.",
            Priority.HIGH, Severity.CRITICAL, BugStatus.OPEN, dev, tester);

        createBug(p1, "Cart total calculation incorrect",
            "When applying discount code, the cart total shows wrong amount.",
            Priority.CRITICAL, Severity.BLOCKER, BugStatus.IN_PROGRESS, dev, tester);

        createBug(p1, "Product images not loading on mobile",
            "Product images fail to load on mobile devices with slow connections.",
            Priority.MEDIUM, Severity.MAJOR, BugStatus.OPEN, dev2, tester);

        createBug(p2, "OTP not received for some carriers",
            "Users on certain mobile carriers do not receive OTP messages.",
            Priority.HIGH, Severity.CRITICAL, BugStatus.OPEN, dev, tester);

        createBug(p2, "Session timeout too short",
            "Users get logged out after 2 minutes of inactivity which is too aggressive.",
            Priority.LOW, Severity.MINOR, BugStatus.RESOLVED, dev2, tester);

        log.info("Seed data initialized successfully.");
        log.info("Default credentials: admin/admin123 | dev1/dev123 | tester1/test123");
    }

    private void createBug(Project project, String title, String description,
                            Priority priority, Severity severity, BugStatus status,
                            User assignedTo, User createdBy) {
        Bug bug = new Bug();
        bug.setTitle(title);
        bug.setDescription(description);
        bug.setPriority(priority);
        bug.setSeverity(severity);
        bug.setStatus(status);
        bug.setProject(project);
        bug.setAssignedTo(assignedTo);
        bug.setCreatedBy(createdBy);
        bugRepository.save(bug);
    }
}
