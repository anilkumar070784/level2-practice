## 1. What is Inter-service Communication?

In microservices, services sometimes need to **ask another service for information**.

Example in your system:

* Asset Service needs **basic asset details**
* Telemetry Service needs to **validate asset existence**
* Dashboard needs **asset + alerts summary**

This communication can be:

* **Synchronous** → REST (Feign / RestTemplate)
* **Asynchronous** → Events (Kafka)

Here we’ll focus on **synchronous REST communication**.

---

## 2. When SHOULD services call each other synchronously?

Use REST calls **only when**:

* You need **immediate response**
* It’s a **query**, not a state change
* Failure can be tolerated with fallback

### Good examples

* “Get asset metadata”
* “Check if asset exists”
* “Fetch alert summary”

### Bad examples

* Telemetry ingestion
* Asset creation side effects
* Alert triggering

Those should be **event-driven**, not REST.

---

## 3. RestTemplate – What it is

`RestTemplate` is:

* A **low-level HTTP client**
* You manually build URLs, headers, requests
* Spring’s older approach

### Example 

```java
RestTemplate restTemplate = new RestTemplate();

AssetDto asset = restTemplate.getForObject(
    "http://asset-service/api/assets/{id}",
    AssetDto.class,
    assetId
);
```

### What YOU handle manually

* URL construction
* HTTP method
* Headers
* Error handling
* Retry logic
* Load balancing (earlier via Ribbon)

---

## 4. Feign Client – What it is

Feign is:

* A **declarative REST client**
* You write an **interface**
* Spring generates the HTTP client

### Same call using Feign

```java
@FeignClient(name = "asset-service")
public interface AssetClient {

    @GetMapping("/api/assets/{id}")
    AssetDto getAsset(@PathVariable String id);
}
```

Usage:

```java
AssetDto asset = assetClient.getAsset(assetId);
```

No URLs. No boilerplate.

---

## 5. Asset & Telemetry Use Case Comparison

### Scenario: Telemetry Service validates asset

#### Using RestTemplate

* Telemetry service must:

    * Know asset service URL
    * Handle timeouts
    * Handle failures

#### Using Feign

* Telemetry service:

    * Calls a Java method
    * Lets Spring handle HTTP

Feign feels like **calling another class**, not another service.

---



## 6. Why Feign is preferred in Microservices

### 1. Readability & Maintainability

Feign client acts as:

* A **service contract**
* Clear API boundary

Anyone reading code immediately knows:

> “Telemetry depends on Asset Service”

---

### 2. Integration with Spring Cloud

Feign works seamlessly with:

* Load balancing
* Circuit breakers
* Retries
* Timeouts

RestTemplate needs **manual wiring** for all this.

---

### 3. Centralized Error Handling

With Feign:

* You can plug in fallback logic
* Map HTTP errors cleanly

This is crucial when Asset Service is down.

---

## 7. Failure Handling (Very Important)

### What happens if Asset Service is DOWN?

#### Without protection

* Telemetry threads block
* Requests pile up
* Cascading failure

#### With Feign + resilience

* Fast failure
* Fallback response
* System stays alive

Example fallback:

```java
@FeignClient(
  name = "asset-service",
  fallback = AssetClientFallback.class
)
```

Telemetry can continue ingesting data.

---

## 8. When RestTemplate is still OK

RestTemplate is acceptable:

* Inside **monoliths**
* For **external APIs**
* For quick POCs

But in **microservices**:

* It becomes verbose
* Easy to misuse
* Harder to standardize

Spring itself recommends:

* **Feign** for service-to-service
* **WebClient** for reactive/non-blocking

---

## 9. Critical Microservices Rules 

### Rule 1: No REST for write side effects

Telemetry should **never** call:

```
POST /assets/updateHealth
```

Health updates come from **events**, not REST.

---

### Rule 2: REST calls must be bounded

* Asset Service can expose:

    * `/assets/{id}/summary`
* Not internal tables
* Not telemetry data

---

### Rule 3: REST is for queries, Events for facts

* “Give me asset info” → REST
* “Asset created” → Event

---

---

## 10. Summary

> **Feign = calling another service like a method**
> **RestTemplate = manually crafting HTTP calls**

In a system as complex as **Asset + Telemetry**, Feign keeps things **clean, explicit, and safe**.


