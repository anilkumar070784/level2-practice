package com.greenko.telemetryservice.service;

import com.greenko.telemetryservice.dto.AssetResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class TelemetryService {

    private RestTemplate restTemplate;

    @Value("${ASSET_SERVICE_URL:http://localhost:8081}")
    private String assetServiceURL;

    public TelemetryService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public AssetResponseDto fetchAssetDetails(String assetId) {
        String url = assetServiceURL+"/api/assets/"+assetId;
        log.info(url);
        var assetDetails = restTemplate.getForObject(url, AssetResponseDto.class);
        return assetDetails;
    }






}
