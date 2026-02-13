package com.greenko.alertservice.api;

import com.greenko.alertservice.dto.AlertResponseDto;
import com.greenko.alertservice.dto.TelemetryForAlertRequestDto;
import com.greenko.alertservice.model.Alert;
import com.greenko.alertservice.repository.AlertRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertRepository alertRepository;

    public AlertController(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @GetMapping
    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    @PostMapping("/evaluate")
    public AlertResponseDto evaluate(@RequestBody TelemetryForAlertRequestDto telemetry) {
        boolean alert = telemetry.temperature() > 90.0;
        String severity = alert ? "CRITICAL" : "NORMAL";
        String message = alert ? "Temperature exceeded threshold" : "No alert";

        if (alert) {
            Alert entity = new Alert();
            entity.setAssetId(telemetry.assetId());
            entity.setPower(telemetry.power());
            entity.setTemperature(telemetry.temperature());
            entity.setSeverity(severity);
            entity.setMessage(message);
            entity.setCreatedAt(LocalDateTime.now());
            alertRepository.save(entity);
        }

        return new AlertResponseDto(alert, severity, message);
    }
}