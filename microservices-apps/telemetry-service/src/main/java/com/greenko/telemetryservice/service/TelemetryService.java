package com.greenko.telemetryservice.service;

import com.greenko.telemetryservice.client.AlertClient;
import com.greenko.telemetryservice.client.AssetClient;
import com.greenko.telemetryservice.dto.AlertEvaluationResponseDto;
import com.greenko.telemetryservice.dto.AssetResponseDto;
import com.greenko.telemetryservice.dto.TelemetryRequestDto;
import org.springframework.stereotype.Service;

@Service
public class TelemetryService {

    private final AssetClient assetClient;
    private final AlertClient alertClient;

    public TelemetryService(AssetClient assetClient, AlertClient alertClient) {
        this.assetClient = assetClient;
        this.alertClient = alertClient;
    }

    public AssetResponseDto fetchAssetDetails(String assetId) {
        return assetClient.getAssetById(assetId);
    }

    public AlertEvaluationResponseDto evaluateAlert(String assetId, double power, double temperature) {
        TelemetryRequestDto request = new TelemetryRequestDto(assetId, power, temperature);
        return alertClient.evaluate(request);
    }
}