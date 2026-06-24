package com.jefersondev.tasks.domain.entities.dto;

public record AuthResponse(
        String token,
        String type,
        String email,
        String name
) {
    public AuthResponse(String token, String email, String name) {
        this(token, "Bearer", email, name);
    }
}