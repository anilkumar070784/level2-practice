## 1. Basic Feign Setup 

### Dependency

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

### Enable Feign

```java
@SpringBootApplication
@EnableFeignClients
public class TelemetryServiceApplication {
}
```

---

## 2. Feign GET Example

### Use case

Telemetry Service wants **basic asset metadata**.

### Feign Client

```java
@FeignClient(
    name = "asset-service",
    path = "/api/v1/assets"
)
public interface AssetClient {

    @GetMapping("/{assetId}")
    AssetResponseDto getAssetById(
        @PathVariable("assetId") String assetId
    );
}
```

### Usage

```java
AssetResponseDto asset = assetClient.getAssetById(assetId);
```

---

## 3. Feign POST Example

### Use case

Maintenance Service creates a **work order** after alert.

### Feign Client

```java
@FeignClient(
    name = "maintenance-service",
    path = "/api/v1/work-orders"
)
public interface MaintenanceClient {

    @PostMapping
    WorkOrderResponse createWorkOrder(
        @RequestBody WorkOrderRequest request
    );
}
```

### Request DTO

```java
public record WorkOrderRequest(
    String assetId,
    String reason,
    String priority
) {}
```

### Call

```java
maintenanceClient.createWorkOrder(
    new WorkOrderRequest(assetId, "Overheating", "HIGH")
);
```

---

## 4. Feign with Request Headers

### Use case

Propagate **Correlation ID / Auth Token**.

#### Feign Client

```java
@GetMapping("/{assetId}")
AssetResponseDto getAssetById(
    @PathVariable String assetId,
    @RequestHeader("X-Correlation-Id") String correlationId
);
```

But this becomes repetitive.

---

## 5. Feign Interceptor 

### Why Interceptors?

* Centralized headers
* Auth token propagation
* Correlation IDs
* Tenant IDs

---

### Example: Correlation ID Interceptor

```java
@Component
public class FeignCorrelationInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {

        String correlationId =
            MDC.get("correlationId");

        if (correlationId != null) {
            template.header("X-Correlation-Id", correlationId);
        }
    }
}
```

Now **every Feign call** carries the header.

---

## 6. Auth Token Interceptor (JWT / OAuth)

```java
@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String token = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getCredentials()
                .toString();

        template.header("Authorization", "Bearer " + token);
    }
}
```

No controller changes needed.

---

## 7. Custom Feign Configuration (Per Client)

### Custom Config

```java
@Configuration
public class AssetFeignConfig {

    @Bean
    public RequestInterceptor assetServiceInterceptor() {
        return template ->
            template.header("X-Source", "telemetry-service");
    }
}
```

### Attach to Feign Client

```java
@FeignClient(
    name = "asset-service",
    configuration = AssetFeignConfig.class
)
public interface AssetClient {
}
```

---

## 8. Error Handling with Feign 

### Custom Error Decoder

```java
@Component
public class AssetErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        if (response.status() == 404) {
            return new AssetNotFoundException("Asset not found");
        }

        return new RuntimeException("Asset service error");
    }
}
```

This keeps business logic clean.

---

## 9. Timeout & Retry 

```yaml
feign:
  client:
    config:
      asset-service:
        connectTimeout: 2000
        readTimeout: 3000
        retryer:
          period: 100
          maxPeriod: 1000
          maxAttempts: 3
```

Telemetry services **must fail fast**.

---

## 10. IMPORTANT Microservices Rules 

### Rule 1: Feign is for READ-heavy calls

* Metadata lookup
* Validation
* Summaries

### Rule 2: Never put Feign in hot paths

* Telemetry ingestion
* Streaming pipelines

### Rule 3: Never chain Feign calls

```
Service A → B → C → D
```

That’s latency + failure cascade.

---


