package com.bugtracker.dto;

import com.bugtracker.entity.BugStatus;
import com.bugtracker.entity.Priority;
import com.bugtracker.entity.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BugDto {
    private Long id;

    @NotBlank(message = "Bug title is required")
    private String title;

    private String description;

    @NotNull(message = "Priority is required")
    private Priority priority = Priority.MEDIUM;

    @NotNull(message = "Severity is required")
    private Severity severity = Severity.MAJOR;

    private BugStatus status = BugStatus.OPEN;

    @NotNull(message = "Project is required")
    private Long projectId;

    private Long assignedToId;

    private MultipartFile attachment;
}
