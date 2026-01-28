package com.jefersondev.tasks.domain.entities.dto;

public record ErrorResponse(
        int status,
        String message,
        String details
) {
}
