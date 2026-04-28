package com.bugtracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentDto {

    @NotBlank(message = "Comment content cannot be empty")
    private String content;

    private Long bugId;
}
