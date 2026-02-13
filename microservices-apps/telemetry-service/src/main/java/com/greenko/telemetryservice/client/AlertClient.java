package com.greenko.telemetryservice.client;

import com.greenko.telemetryservice.dto.AlertEvaluationResponseDto;
import com.greenko.telemetryservice.dto.TelemetryRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "alert-service", url = "${ALERT_SERVICE_URL:http://localhost:8083}")
public interface AlertClient {
    @PostMapping("/api/alerts/evaluate")
    AlertEvaluationResponseDto evaluate(@RequestBody TelemetryRequestDto telemetry);
}