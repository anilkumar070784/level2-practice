## Spring Cloud Config 

## 1. Why do we need Spring Cloud Config?

### Problem without Config Server

Imagine you have these services:

* asset-service
* telemetry-service
* alert-service
* maintenance-service

Each service has:

```
application.yml
```

Now think about:

* DB credentials
* Kafka brokers
* Feign timeouts
* Feature flags
* Environment configs (dev / qa / prod)

### What happens?

* Same config duplicated in many repos
* Manual edits per service
* Risky redeploys
* Inconsistent configs

For example:

```
kafka.bootstrap-servers
```

One service updated, another forgotten → production issues.

---

## 2. What is Spring Cloud Config?

**Spring Cloud Config** provides:

> **Centralized, externalized configuration** for all microservices

* Config lives outside the service code
* Services fetch config at startup
* Config can change without code changes

Think of it as:

> **Git-backed configuration management for Spring Boot**

---

## 3. Asset & Telemetry System – Where Config Server Fits

![Image](https://miro.medium.com/1%2ASckDaXxM3o9nds3FZMZIzQ.png)

### Central Config Server

* Reads config from Git
* Serves config over HTTP

### Client Services

* asset-service
* telemetry-service
* alert-service

Each service:

* Knows **only** config server URL
* Not DB passwords, not Kafka URLs

---

## 4. What Goes into Config Server?

### Examples (very realistic)

#### Asset Service

* DB URL
* JPA settings
* Feature flags

#### Telemetry Service

* Kafka brokers
* Ingestion limits
* Retention policies

#### Alert Service

* Threshold defaults
* Notification toggles

---

## 5. How Spring Cloud Config Works (Flow)

1. Service starts
2. Service contacts Config Server
3. Config Server reads from Git
4. Config returned based on:

    * application name
    * profile (dev / prod)
5. Service boots with external config

---

## 6. Implementing Spring Cloud Config (Step-by-Step)

### Step 1: Create Config Server

#### Dependency

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

#### Enable Config Server

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
}
```

---

### Step 2: Configure Git Backend

```yaml
server:
  port: 8888

spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-org/config-repo
          clone-on-start: true
```

---

### Step 3: Config Repository Structure

```
config-repo/
 ├── application.yml
 ├── asset-service.yml
 ├── telemetry-service.yml
 ├── alert-service.yml
 ├── asset-service-dev.yml
 ├── telemetry-service-prod.yml
```

---

### Step 4: Sample Config (telemetry-service.yml)

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092

telemetry:
  ingestion:
    max-events-per-second: 10000
```

---

## 7. Configure Client Services

### Dependency (Client)

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

---

### bootstrap.yml (IMPORTANT)

```yaml
spring:
  application:
    name: telemetry-service
  cloud:
    config:
      uri: http://localhost:8888
```

### Config must load **before** application.yml.

---

## 8. Environment Profiles (Dev / Prod)

Run service with:

```
--spring.profiles.active=prod
```

Config server serves:

```
telemetry-service-prod.yml
```

No code change. No rebuild.

---

## 9. Refreshing Config Without Restart

### Use case

* Change alert thresholds
* Toggle a feature

### Enable Actuator

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Expose refresh endpoint:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: refresh
```

### Refresh

```
POST /actuator/refresh
```

---

## 10. @RefreshScope Example

```java
@RefreshScope
@Component
public class TelemetryConfig {

    @Value("${telemetry.ingestion.max-events-per-second}")
    private int maxEvents;
}
```

Value updates at runtime.

---

## 11. Why Config Server is CRITICAL for Microservices

### Without it

* Manual config sync
* Accidental misconfigurations
* Downtime for small changes

### With it

* Central control
* Audit via Git
* Rollback configs easily
* Same config across services

