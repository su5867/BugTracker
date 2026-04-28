package com.bugtracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDto {
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String fullName;
    private String role; // ADMIN, DEVELOPER, TESTER
    private boolean enabled = true;
}
