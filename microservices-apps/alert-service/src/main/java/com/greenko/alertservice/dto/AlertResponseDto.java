package com.greenko.alertservice.dto;

public record AlertResponseDto(
        boolean alert,
        String severity,
        String message
) {
}