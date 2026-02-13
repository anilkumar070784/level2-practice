# 1. High-level Synchronous REST Architecture

```
Client / Device
     |
     v
 Telemetry Service
     |
     | REST
     v
 Asset Service
     |
     | REST
     v
 Alert Service
```

---

# 2. Service Responsibilities

## 2.1 Asset Service (Source of Truth)

### Responsibilities

* Asset registration
* Asset status
* Asset metadata

### APIs

```
POST /api/assets
GET  /api/assets/{assetId}
PUT  /api/assets/{assetId}/status
```

### Example Response

```json
{
  "assetId": "A101",
  "type": "GENERATOR",
  "status": "ACTIVE"
}
```

---

## 2.2 Telemetry Service 

In synchronous design, **Telemetry becomes the coordinator**

### Responsibilities

* Accept telemetry
* Validate asset
* Forward telemetry for alert evaluation

### APIs

```
POST /api/telemetry
```

---

## 2.3 Alert Service 

### Responsibilities

* Evaluate telemetry
* Return alert decision

### APIs

```
POST /api/alerts/evaluate
```

---

# 3. End-to-End Flow 

Let’s walk through **one telemetry request**.

---

## Step 1: Device sends telemetry

```
POST /api/telemetry
```

```json
{
  "assetId": "A101",
  "metric": "temperature",
  "value": 92.5
}
```

---

## Step 2: Telemetry → Asset Service (REST)

Telemetry must **validate asset existence**.

```
GET /api/assets/A101
```

### Possible outcomes

* Asset exists → continue
* 404 → reject telemetry

#### This is **tight coupling** introduced by sync calls.

---

## Step 3: Telemetry stores data

Telemetry Service saves:

```
(assetId, metric, value, timestamp)
```

---

## Step 4: Telemetry → Alert Service (REST)

```
POST /api/alerts/evaluate
```

```json
{
  "assetId": "A101",
  "metric": "temperature",
  "value": 92.5
}
```

---

## Step 5: Alert Service evaluates rule

Rule:

```
temperature > 90 → CRITICAL
```

Response:

```json
{
  "alert": true,
  "severity": "CRITICAL",
  "message": "Temperature exceeded threshold"
}
```

---

## Step 6: Telemetry responds to device

```json
{
  "status": "RECEIVED",
  "alertRaised": true
}
```

---

# 4. Problems You Will START Seeing 

This design **works**, but cracks appear as scale grows.

---

## 4.1 Latency stacking

```
Telemetry → Asset → Alert
```

Total latency =
Asset latency + Alert latency + Network latency

---

## 4.2 Cascading failures

* Asset Service down → Telemetry fails
* Alert Service slow → Telemetry threads block

One bad service **brings everything down**.

---

## 4.3 Telemetry becomes a God service

Telemetry now:

* Validates asset
* Stores data
* Calls alert logic
* Manages retries

This violates **single responsibility**.

---

## 4.4 Scaling problem

* Telemetry is high-traffic
* Asset is low-traffic

But:

* Asset now gets hammered by telemetry calls
* Scaling asset just for validation feels wrong

---


# 5. Key Takeaway 

> **Synchronous microservices are easier to build.
> Event-driven microservices are easier to scale.**

You *must* understand the first before trusting the second.

