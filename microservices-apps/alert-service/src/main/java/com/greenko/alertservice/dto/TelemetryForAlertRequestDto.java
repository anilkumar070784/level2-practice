package com.greenko.alertservice.dto;

public record TelemetryForAlertRequestDto(
        String assetId,
        double power,
        double temperature
) {
}