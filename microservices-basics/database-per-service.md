## What is “Database per Service”?

**Database per Service** means:

> Each microservice **exclusively owns its database**
> No other service can read from or write to it — **not even read-only**

So in practice:

* **Asset Service** → Asset DB
* **Telemetry Service** → Telemetry DB
* **Alert Service** → Alert DB
* **Maintenance Service** → Maintenance DB

No shared schemas. No cross-service joins.

---

## Why this concept exists 

Microservices are about **independent evolution**.

If two services share a database:

* Schema change in one → breaks another
* Scaling one → overloads shared DB
* You can’t deploy independently
* Teams get blocked by each other

At that point, you don’t have microservices — you have a **distributed monolith**.

---

## Asset & Telemetry System – Correct DB Ownership

### Asset Service DB

Stores:

* Asset identity
* Metadata
* Lifecycle state

**Technology fit**

* PostgreSQL / MySQL
* JPA
* Strong consistency

---

### Telemetry Service DB

Stores:

* High-volume sensor readings
* Time-series data

**Technology fit**

* TimescaleDB / InfluxDB / Cassandra
* Append-only writes
* Retention policies

---

### Alert Service DB

Stores:

* Thresholds
* Active alerts
* Alert history

**Technology fit**

* PostgreSQL / MongoDB
* Fast reads
* Rule evaluation support

---

### Maintenance Service DB

Stores:

* Work orders
* Schedules
* Technician assignments

**Technology fit**

* Relational DB
* Transactional consistency

---

## Same “AssetId”, Different Databases 

Each DB stores **its own view** of the asset.

Example:

### Asset DB

```
asset_id | name | location | status
```

### Telemetry DB

```
asset_id | metric | value | timestamp
```

### Alert DB

```
asset_id | threshold | severity
```

They share:

* `assetId` as a **reference**
  They do NOT share:
* Tables
* Foreign keys
* Joins

---

## How Services Stay in Sync (Without Sharing DB)

### Through Events (not joins)

Example:

* Asset Service emits `AssetRegistered`
* Other services store **only what they need**

Telemetry Service stores:

```
asset_id
```

Alert Service stores:

```
asset_id + thresholds
```

If asset name changes?

* Telemetry service **does not care**
* Alert service **does not care**

This is intentional.

---

## What About Queries That Need Data From Multiple Services?

Example:

> “Show asset details + latest telemetry + active alerts”

### Wrong way

```
SELECT * 
FROM asset_db.asset a
JOIN telemetry_db.telemetry t
JOIN alert_db.alert al
```

This breaks:

* Ownership
* Scaling
* Autonomy

---

### Correct approaches

#### Option 1: API Composition

* API Gateway calls:

    * Asset Service
    * Telemetry Service
    * Alert Service
* Aggregates response

Works for **low traffic dashboards**.

---

#### Option 2: Read Model / Projection 

Create a **Dashboard / Query Service**:

* Subscribes to events:

    * AssetRegistered
    * TelemetryAggregated
    * AlertRaised
* Maintains its own **read-only DB**

This is **CQRS-style design**.

---

## Transactions Across Databases? (The Big Question)

### Distributed transactions 

Avoid.

* Slow
* Fragile
* Not cloud-friendly

---

### Eventual consistency + Saga

Example:

1. Asset created
2. Event published
3. Telemetry service initializes asset
4. Alert service initializes thresholds

If one fails → compensate or retry.

This is how real systems work.

---

## Why DB per Service is CRITICAL for Telemetry Systems

Telemetry characteristics:

* Millions of writes per day
* Different indexing strategies
* Retention & compaction
* Horizontal scaling

If telemetry shares DB with assets:

* Asset CRUD becomes slow
* DB tuning becomes impossible
* One workload kills the other

Separation is survival, not theory.

---

## Spring Boot Perspective

Each service:

* Has its own `DataSource`
* Owns its own JPA entities
* Runs migrations independently (Flyway/Liquibase)

No shared entity JARs.
No common persistence module.

---

## Simple Rule to Remember

> **If two services share a database,
> they are not independent services.**

