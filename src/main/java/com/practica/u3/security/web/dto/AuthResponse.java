package com.practica.u3.security.web.dto;

public record AuthResponse(String token, String type, long expiresIn) {
}
