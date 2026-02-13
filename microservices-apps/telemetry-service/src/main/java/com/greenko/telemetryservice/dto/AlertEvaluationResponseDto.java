package com.greenko.telemetryservice.dto;

public record AlertEvaluationResponseDto(
        boolean alert,
        String severity,
        String message
) {}