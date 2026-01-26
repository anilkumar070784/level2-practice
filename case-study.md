# 1. Problem Context: Asset & Telemetry Platform

### System characteristics

* Millions of assets (turbines, solar panels, batteries)
* Continuous telemetry ingestion (power, temperature, vibration)
* Real-time alerts
* Historical analytics
* Regulatory reporting
* High availability & scalability

**Monolith will not survive this scale**

So we move to **microservices + DDD**.

---

# 2. High-Level Microservices Architecture

```
                    ┌──────────────┐
                    │ API Gateway  │
                    └──────┬───────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌────────────┐   ┌────────────────┐   ┌─────────────────┐
│ Asset Svc  │   │ Telemetry Svc  │   │ Alerting Svc     │
└────────────┘   └────────────────┘   └─────────────────┘
        │                  │                  │
┌────────────┐   ┌────────────────┐   ┌─────────────────┐
│ Asset DB   │   │ Telemetry DB   │   │ Alerts DB       │
└────────────┘   └────────────────┘   └─────────────────┘

        ┌────────────────────────────────────────┐
        │ Analytics / Reporting / Billing Svc    │
        └────────────────────────────────────────┘
```

---

# 3. Why Domain-Driven Design (DDD) Is Needed

### Problem without DDD

* God services
* Mixed responsibilities
* Tight coupling
* Unclear ownership

### DDD gives us

* Clear domain boundaries
* Language alignment with business
* Independent evolution

---

## Core Domains (Bounded Contexts)

| Bounded Context   | Responsibility                   |
| ----------------- | -------------------------------- |
| Asset Context     | Asset lifecycle & metadata       |
| Telemetry Context | High-volume telemetry ingestion  |
| Alert Context     | Rules, thresholds, notifications |
| Analytics Context | Aggregations, trends, reports    |
| Identity Context  | Users, roles, access             |

Each **bounded context becomes one or more microservices**.

> **Bounded context = boundary where a domain model is consistent**

---

# 4. Service Boundaries (VERY IMPORTANT)

### Asset Service

**Owns:**

* Asset registration
* Asset type (turbine, solar)
* Location
* Capacity

**Does NOT care about telemetry volume**

---

### Telemetry Service

**Owns:**

* Ingestion
* Validation
* Storage
* Time-series data

Optimized for:

* Write-heavy
* Append-only
* High throughput

---

### Alerting Service

**Owns:**

* Threshold rules
* Real-time alerts
* Escalations

Consumes telemetry **events**, not databases.

---

### Analytics Service

**Owns:**

* Aggregations
* Daily/monthly reports
* Compliance exports

Consumes **processed data**, not raw telemetry.

---

## Rule of Thumb

> **If two parts scale differently, they should not be the same service**

---

# 5. Database Per Service (Non-Negotiable)

### Why NOT shared database?

* Tight coupling
* Schema conflicts
* Deployment lock-step
* Hidden dependencies

---

### Correct Approach

| Service           | Database Type         |
| ----------------- | --------------------- |
| Asset Service     | Relational (Postgres) |
| Telemetry Service | Time-series / NoSQL   |
| Alert Service     | Relational            |
| Analytics Service | OLAP / Data warehouse |

Each service:

* Owns its schema
* Controls migrations
* Evolves independently

> **Service boundary = database boundary**

---

# 6. Configuration Management – Spring Cloud Config

### Problem

* Hardcoded configs
* Different configs per env
* Restart needed for changes

---

### Solution: Central Config Server

```
Git Repo
  ├── asset-service.yml
  ├── telemetry-service.yml
  ├── alert-service.yml
```

Services load config at startup:

```yaml
spring:
  config:
    import: configserver:
```

### Benefits

* Centralized config
* Environment-specific configs
* Secure secrets
* Zero code change

---

# 7. Service Discovery (Docker-Based)

### Problem

* Containers are dynamic
* IPs change
* Hardcoding endpoints fails

---

### Docker-Based Discovery Options

* Docker DNS
* Docker Compose service names
* Kubernetes DNS (future-proof)

Example:

```text
http://telemetry-service:8080
```

### Why needed

* Auto-discovery
* Scaling services horizontally
* No manual wiring

---

# 8. Centralized Routing – API Gateway

### Why Gateway?

* One entry point
* Security enforcement
* Rate limiting
* Routing

---

### Responsibilities

* Route `/assets/**` → Asset Service
* Route `/telemetry/**` → Telemetry Service
* Auth validation
* Request throttling

### Tools

* Spring Cloud Gateway
* Kong
* NGINX

> **Clients never talk to services directly**

---

# 9. Cross-Cutting Concerns (Handled Centrally)

## Authentication & Authorization

* JWT / OAuth2
* Token validated at Gateway
* Role-based access

---

## Logging

* Correlation ID injected at Gateway
* Propagated across services
* Central log aggregation

---

## Rate Limiting

* Protect ingestion APIs
* Prevent overload
* Fair usage per client

Handled **once**, not in every service.

---

# 10. Flyway Migrations (Schema Versioning)

### Problem without migrations

* Manual DB changes
* Inconsistent schemas
* Rollback nightmares

---

### Flyway Example

```text
V1__create_asset_table.sql
V2__add_capacity_column.sql
```

Startup:

* Flyway checks version
* Applies pending migrations
* Guarantees schema consistency

### Why critical

* Independent deployments
* Safe rollbacks
* Auditable changes

---

# 11. Transaction Patterns (VERY IMPORTANT)

### Why NOT distributed transactions?

* Slow
* Fragile
* Don’t scale

---

## Correct Patterns

### 1. Saga Pattern

* Event-driven consistency
* Compensating actions

Example:

```
Asset Registered → Telemetry Enabled → Alert Rules Created
```

If step 2 fails → compensate step 1.

---

### 2. Eventual Consistency

* Telemetry ingested
* Alerts triggered asynchronously
* Analytics updated later

> **Consistency is delayed, not lost**

---

# 12. Logging Patterns (Production-Grade)

### Structured Logging

```json
{
  "service": "telemetry-service",
  "assetId": "TURBINE-101",
  "event": "INGESTED",
  "correlationId": "abc-123"
}
```

---

### Correlation ID Flow

* Generated at Gateway
* Passed via headers
* Logged by every service

---

### Centralized Log Store

* ELK / OpenSearch
* Search by assetId
* Trace failures across services

---

# 13. Why This Architecture Works

### Scalability

* Telemetry scales independently
* Analytics runs async

### Resilience

* Failure isolated per service
* No cascade failures

### Maintainability

* Clear ownership
* Smaller codebases
* Independent deployments

### Cloud-Native Ready

* Docker
* Kubernetes
* Horizontal scaling

---


