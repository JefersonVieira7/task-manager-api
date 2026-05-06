package com.jefersondev.tasks.domain.entities.dto;

import com.jefersondev.tasks.domain.entities.TaskPriority;
import com.jefersondev.tasks.domain.entities.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskDto(

        UUID id,

        @NotBlank(message = "Title is required")
        @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
        String title,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        LocalDateTime dueDate,
        TaskStatus status,
        TaskPriority priority,
        UUID taskListId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}