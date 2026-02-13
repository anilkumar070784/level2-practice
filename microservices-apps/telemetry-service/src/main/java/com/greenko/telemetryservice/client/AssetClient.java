package com.greenko.telemetryservice.client;

import com.greenko.telemetryservice.dto.AssetResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "asset-service", url = "${ASSET_SERVICE_URL:http://localhost:8081}")
public interface AssetClient {
    @GetMapping("/api/assets/{assetId}")
    AssetResponseDto getAssetById(@PathVariable("assetId") String assetId);
}