# Smart Campus – Sensor & Room Management API

> **Module:** 5COSC022W – Client-Server Architectures  
> **Student:** [Your Name / Student ID]  
> **Technology Stack:** Java · JAX-RS (Jersey) · Maven · Grizzly Embedded HTTP Server

---

## Table of Contents

1. [API Design Overview](#1-api-design-overview)
2. [How to Build and Launch the Server](#2-how-to-build-and-launch-the-server)
3. [Sample curl Commands](#3-sample-curl-commands)
4. [Report – Answers to Coursework Questions](#4-report--answers-to-coursework-questions)

---

## 1. API Design Overview

The Smart Campus API is a RESTful web service built with **JAX-RS (Jersey)** that manages campus Rooms and the IoT Sensors installed within them. It models the physical structure of the campus as a resource hierarchy, with full CRUD support for rooms and sensors, historical reading logs per sensor, and a robust error-handling layer that ensures clients always receive meaningful, structured JSON responses.

### Base URL

```
http://localhost:8080/api/v1
```

### Resource Hierarchy

```
GET    /api/v1                                →  Discovery (API metadata & resource links)

GET    /api/v1/rooms                          →  List all rooms
POST   /api/v1/rooms                          →  Create a new room
GET    /api/v1/rooms/{roomId}                 →  Get a single room by ID
DELETE /api/v1/rooms/{roomId}                 →  Delete a room (blocked if sensors are assigned)

GET    /api/v1/sensors                        →  List all sensors (supports ?type= filter)
POST   /api/v1/sensors                        →  Create a sensor (roomId must already exist)

GET    /api/v1/sensors/{sensorId}/readings    →  Get all historical readings for a sensor
POST   /api/v1/sensors/{sensorId}/readings    →  Add a new reading (sensor must be ACTIVE)
```

### Data Models

**Room**

| Field | Type | Description |
|---|---|---|
| `id` | String | Unique identifier e.g. `LIB-301` |
| `name` | String | Human-readable room name |
| `capacity` | int | Maximum occupancy |
| `sensorIds` | List\<String\> | IDs of sensors installed in this room |

**Sensor**

| Field | Type | Description |
|---|---|---|
| `id` | String | Unique identifier e.g. `TEMP-001` |
| `type` | String | Category e.g. `Temperature`, `CO2`, `Occupancy` |
| `status` | String | `ACTIVE`, `MAINTENANCE`, or `OFFLINE` |
| `currentValue` | double | Most recent measurement (updated on each new reading) |
| `roomId` | String | Foreign key linking to the room where the sensor is installed |

**SensorReading**

| Field | Type | Description |
|---|---|---|
| `id` | String | Unique reading event ID |
| `timestamp` | long | Epoch time in milliseconds |
| `value` | double | The measured value recorded by the sensor |

### Error Response Format

Every error returns a consistent JSON body — no stack traces are ever exposed to the client:

```json
{
  "errorMessage": "Room cannot be deleted because it still has sensors assigned.",
  "errorCode": 409,
  "documentation": "https://smartcampus.edu/api/docs/errors"
}
```

| HTTP Status | Trigger Scenario | Exception Class |
|---|---|---|
| 404 Not Found | Room or sensor ID does not exist | `NotFoundException` (JAX-RS built-in) |
| 409 Conflict | Deleting a room that still has sensors assigned | `RoomNotEmptyException` |
| 422 Unprocessable Entity | Creating a sensor with a `roomId` that does not exist | `LinkedResourceNotFoundException` |
| 403 Forbidden | Adding a reading to a MAINTENANCE or OFFLINE sensor | `SensorUnavailableException` |
| 500 Internal Server Error | Any unexpected runtime exception | `GlobalExceptionMapper` |

### Project Structure

```
SmartCampusAPI/
├── pom.xml
└── src/main/java/com/smartcampus/
    ├── SmartCampusApplication.java                      ← @ApplicationPath("/api/v1"), registers all classes
    ├── filter/
    │   └── LoggingFilter.java                           ← Logs every request (method + URI) and response (status)
    ├── exception/
    │   ├── LinkedResourceNotFoundException.java
    │   ├── RoomNotEmptyException.java
    │   └── SensorUnavailableException.java
    ├── mapper/
    │   ├── GlobalExceptionMapper.java                   ← Catch-all → HTTP 500
    │   ├── LinkedResourceNotFoundExceptionMapper.java   ← → HTTP 422
    │   ├── RoomNotEmptyExceptionMapper.java             ← → HTTP 409
    │   └── SensorUnavailableExceptionMapper.java        ← → HTTP 403
    ├── model/
    │   ├── ErrorMessage.java
    │   ├── Room.java
    │   ├── Sensor.java
    │   └── SensorReading.java
    └── resources/
        ├── DiscoveryResource.java       ← GET /api/v1
        ├── RoomResource.java            ← /api/v1/rooms  (also holds all shared static data maps)
        ├── SensorResource.java          ← /api/v1/sensors
        └── SensorReadingResource.java   ← /api/v1/sensors/{id}/readings (sub-resource)
```

---

## 2. How to Build and Launch the Server

### Prerequisites

- **Java JDK 11** or higher — verify with `java -version`
- **Apache Maven 3.6+** — verify with `mvn -version`
- No database installation required. All data is stored in-memory. Two rooms and two sensors are pre-loaded on startup.

---

### Step 1 — Clone the repository

```bash
git clone https://github.com/[your-username]/SmartCampusAPI.git
cd SmartCampusAPI
```

---

### Step 2 — Build the project

```bash
mvn clean package
```

Maven downloads all dependencies (Jersey, Grizzly, Jackson) and compiles the project. A successful build ends with `BUILD SUCCESS` in the terminal.

---

### Step 3 — Start the server

```bash
mvn exec:java -Dexec.mainClass="com.smartcampus.Main"
```

Or, if the project produces an executable JAR:

```bash
java -jar target/SmartCampusAPI-1.0-SNAPSHOT.jar
```

The embedded Grizzly server starts on **port 8080**. You will see output similar to:

```
Smart Campus API started at: http://localhost:8080/api/v1
Press ENTER to stop the server...
```

---

### Step 4 — Verify the server is running

Run the following command in a new terminal window:

```bash
curl http://localhost:8080/api/v1
```

A JSON discovery response confirms the server is live and ready to accept requests.

---

### Step 5 — Stop the server

Press `ENTER` in the terminal where the server is running, or press `Ctrl + C`.

---

## 3. Sample curl Commands

The server pre-loads two rooms (`LIB-301`, `LAB-101`) and two sensors (`TEMP-001` status `ACTIVE`, `CO2-001` status `MAINTENANCE`) on startup, so all commands below work immediately without any prior setup steps.

---

### Command 1 — GET /api/v1 · API Discovery

```bash
curl -X GET http://localhost:8080/api/v1 \
  -H "Accept: application/json"
```

**Expected response — 200 OK:**

```json
{
  "version": "v1",
  "contact": "admin@smartcampus.local",
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

---

### Command 2 — POST /api/v1/rooms · Create a new room

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "id": "HALL-01",
    "name": "Main Lecture Hall",
    "capacity": 200
  }'
```

**Expected response — 201 Created:**

```json
{
  "id": "HALL-01",
  "name": "Main Lecture Hall",
  "capacity": 200,
  "sensorIds": []
}
```

---

### Command 3 — POST /api/v1/sensors · Create a sensor linked to a room

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "OCC-001",
    "type": "Occupancy",
    "status": "ACTIVE",
    "currentValue": 0.0,
    "roomId": "HALL-01"
  }'
```

**Expected response — 201 Created:**

```json
{
  "id": "OCC-001",
  "type": "Occupancy",
  "status": "ACTIVE",
  "currentValue": 0.0,
  "roomId": "HALL-01"
}
```

---

### Command 4 — GET /api/v1/sensors?type=CO2 · Filtered sensor retrieval

```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2" \
  -H "Accept: application/json"
```

**Expected response — 200 OK:**

```json
[
  {
    "id": "CO2-001",
    "type": "CO2",
    "status": "MAINTENANCE",
    "currentValue": 400.0,
    "roomId": "LAB-101"
  }
]
```

---

### Command 5 — POST /api/v1/sensors/{sensorId}/readings · Add a reading to an ACTIVE sensor

```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{
    "id": "READ-001",
    "timestamp": 1714000000000,
    "value": 23.7
  }'
```

**Expected response — 201 Created:**

```json
{
  "id": "READ-001",
  "timestamp": 1714000000000,
  "value": 23.7
}
```

---

### Command 6 — GET /api/v1/sensors/{sensorId}/readings · Retrieve reading history

```bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Accept: application/json"
```

**Expected response — 200 OK:**

```json
[
  {
    "id": "READ-001",
    "timestamp": 1714000000000,
    "value": 23.7
  }
]
```

---

### Command 7 — DELETE /api/v1/rooms/{roomId} · 409 Conflict when room has sensors

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301 \
  -H "Accept: application/json"
```

**Expected response — 409 Conflict:**

```json
{
  "errorMessage": "Room cannot be deleted because it still has sensors assigned.",
  "errorCode": 409,
  "documentation": "https://smartcampus.edu/api/docs/errors"
}
```

---

## 4. Report – Answers to Coursework Questions

---

### Part 1 – Service Architecture & Setup

---

#### Q1.1 — JAX-RS Resource Lifecycle and the Impact on In-Memory Data Management

By default, JAX-RS creates a **brand-new instance of every Resource class for each incoming HTTP request** (per-request lifecycle). The framework instantiates the class, handles the request, and then discards the instance. This is the behaviour defined by the JAX-RS specification when no explicit scope annotation is applied.

**Impact on in-memory data management:**

Because each request receives a fresh object instance, any fields declared at the instance level would be initialised from scratch on every call — making it impossible to persist data between requests. To work around this, all shared data structures must be declared as `static` fields. Static fields belong to the class itself rather than to any particular instance, so they survive for the entire lifetime of the JVM and are shared across every concurrent request.

In this project, the shared data stores in `RoomResource` are declared as:

```java
public static final Map<String, Room> rooms = new ConcurrentHashMap<>();
public static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
public static final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();
```

`ConcurrentHashMap` is chosen over a standard `HashMap` specifically to **prevent race conditions**. Multiple HTTP requests can arrive simultaneously from different clients on separate threads. A plain `HashMap` is not thread-safe and can suffer from data corruption, lost updates, or `ConcurrentModificationException` under concurrent access. `ConcurrentHashMap` uses fine-grained internal locking to allow concurrent reads and writes safely, without requiring the developer to manage `synchronized` blocks manually, ensuring data integrity under load.

---

#### Q1.2 — Why HATEOAS is a Hallmark of Advanced RESTful Design

**HATEOAS** (Hypermedia as the Engine of Application State) is the principle that API responses should embed hyperlinks pointing to related resources and available next actions, rather than requiring clients to construct URLs themselves from external documentation.

For example, a HATEOAS-compliant response to `GET /api/v1/rooms/LIB-301` might look like:

```json
{
  "id": "LIB-301",
  "name": "Library Quiet Study",
  "_links": {
    "self": "/api/v1/rooms/LIB-301",
    "sensors": "/api/v1/sensors?roomId=LIB-301",
    "all-rooms": "/api/v1/rooms"
  }
}
```

**Benefits over static documentation:**

- **Discoverability:** A client can explore the entire API starting from the single root URL by following links, with no prior knowledge of the URL structure required.
- **Loose coupling:** If URL patterns are refactored, clients that navigate by following links rather than hardcoding paths do not need any code changes on their side.
- **State-aware navigation:** Links can reflect what actions are currently valid for a resource. For instance, a room that still has sensors assigned could omit the `delete` link, preventing invalid operations before they are even attempted.
- **Self-documenting:** Developers testing the API can discover available operations from the response itself, reducing reliance on external documentation that may be out of date.

HATEOAS is Level 3 of the Richardson Maturity Model — the highest level of RESTful design maturity.

---

### Part 2 – Room Management

---

#### Q2.1 — Implications of Returning Only IDs vs Full Room Objects in a List Response

When `GET /api/v1/rooms` is called, there are two design approaches:

**Returning only IDs** (e.g., `["LIB-301", "LAB-101"]`):
- Produces a very small response payload — beneficial on constrained networks or with very large collections.
- However, a client needing any room details must then issue a separate `GET /rooms/{id}` request for every room returned. With 100 rooms, this becomes 101 HTTP round-trips — the classic **N+1 problem** — dramatically increasing latency and server load.

**Returning full room objects** *(this implementation)*:
- A single HTTP round-trip delivers everything a client needs to render a complete rooms view.
- Eliminates the N+1 problem entirely and reduces client-side complexity.
- The trade-off is a larger response payload, but for a typical university campus room count this overhead is negligible.

A common real-world middle ground is to return a **summary representation** (e.g., `id` and `name` only) in list responses, reserving the full object for the single-item endpoint `GET /rooms/{id}`. This balances bandwidth efficiency with client convenience.

---

#### Q2.2 — Is the DELETE Operation Idempotent in This Implementation?

**Yes**, the DELETE operation is idempotent in this implementation, provided the room has no sensors assigned.

Idempotency means that making **the same request multiple times produces the same final server state** as making it once. It does not require every call to return the same HTTP status code.

In this implementation:

- **First DELETE on an empty room:** The room is found and removed → server returns **204 No Content**.
- **Second DELETE on the same room ID:** The room no longer exists → `NotFoundException` is thrown → server returns **404 Not Found**.

The server state after both calls is identical: the room is absent from the data store. The 404 on the second call is informational — it is not a side-effect or a state change — and is consistent with the HTTP/1.1 specification's definition of idempotency.

This contrasts with POST, which is non-idempotent by definition: sending the same POST twice genuinely creates two separate resources.

---

### Part 3 – Sensor Operations & Linking

---

#### Q3.1 — Technical Consequences of Sending the Wrong Content-Type to a POST Endpoint

The `@Consumes(MediaType.APPLICATION_JSON)` annotation declares that the POST endpoint will only process request bodies with a `Content-Type` of `application/json`. If a client sends data with a different content type — for example `text/plain` or `application/xml` — the JAX-RS runtime intercepts the mismatch **before any application code is executed**.

Jersey's content negotiation engine compares the `Content-Type` header of the incoming request against the media types declared in `@Consumes`. When there is no match, the framework automatically returns **HTTP 415 Unsupported Media Type**. The resource method body is never entered.

This is a key feature of JAX-RS: format validation is handled at the framework level, producing a consistent, standards-compliant error response with no extra developer code. The developer does not need to write any conditional logic to inspect or validate the content type themselves.

---

#### Q3.2 — Why Query Parameters Are Superior to Path Segments for Filtering Collections

**Path-based filtering** (e.g., `/api/v1/sensors/type/CO2`):
- Encodes the filter value directly into the URL structure, falsely implying that `CO2` is a uniquely identifiable **resource** rather than a filter criterion.
- Violates the REST principle that URL path segments should identify resources, not describe how to search or narrow them.
- Becomes immediately unmanageable when multiple filters are needed: `/sensors/type/CO2/status/ACTIVE/room/LAB-101`.
- Requires a new route definition for every new filter combination.

**Query parameter filtering** (e.g., `/api/v1/sensors?type=CO2`) *(this implementation)*:
- The path cleanly identifies the resource collection (`/sensors`); the query string describes how to narrow the result — a proper semantic separation.
- Multiple filters compose naturally: `?type=CO2&status=ACTIVE`.
- The base URL `/api/v1/sensors` remains stable and consistent regardless of what filters are applied, making it bookmarkable and cache-friendly.
- New optional filter parameters can be added without any routing changes — just a new `@QueryParam` annotation in the method signature.

The guiding REST design principle is: **path segments identify resources; query parameters filter, sort, or paginate them**.

---

### Part 4 – Deep Nesting with Sub-Resources

---

#### Q4.1 — Architectural Benefits of the Sub-Resource Locator Pattern

In this project, `SensorResource` does not define the readings endpoints directly. Instead, it uses a **sub-resource locator** that delegates to a dedicated `SensorReadingResource` class:

```java
@Path("/{sensorId}/readings")
public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
    return new SensorReadingResource(sensorId);
}
```

This pattern provides several architectural advantages over placing all nested endpoint logic in one monolithic controller:

**Single Responsibility:** Each class has one clearly scoped job. `SensorResource` manages sensor CRUD; `SensorReadingResource` manages reading operations. Neither class contains the logic of the other.

**Maintainability:** Changes to reading logic — such as adding pagination, value aggregation, or access control — are fully isolated to `SensorReadingResource`. There is zero risk of accidentally breaking sensor management code when modifying reading behaviour.

**Scalability of Complexity:** As an API grows, a single controller class handling every nested path (`/sensors/{id}/readings`, `/sensors/{id}/calibration`, `/sensors/{id}/alerts`) quickly becomes enormous and unnavigable. The sub-resource locator pattern keeps each class focused and manageable regardless of how many sub-paths are added.

**Testability:** `SensorReadingResource` can be instantiated and tested in isolation with a known `sensorId`, without bootstrapping the entire sensor routing layer.

**Reusability:** The same sub-resource class could be returned from multiple parent locators if the API design required it, eliminating logic duplication.

---

### Part 5 – Error Handling, Exception Mapping & Logging

---

#### Q5.1 — Why HTTP 422 is More Semantically Accurate than HTTP 404 for a Missing Linked Resource

When a client POSTs a new sensor with a `roomId` that does not correspond to any existing room, the situation is:

- The **HTTP request is syntactically valid** — it is well-formed JSON sent to the correct endpoint.
- The **endpoint `/api/v1/sensors` exists** — a 404 would incorrectly imply the URL itself cannot be found.
- The **problem is purely semantic**: a field inside the request body references an entity that is not present in the system.

**HTTP 404 Not Found** signals that the URL path in the request line does not map to any resource. Using 404 here would mislead the client into thinking the `/sensors` endpoint is missing, which is factually incorrect.

**HTTP 422 Unprocessable Entity** is the precise code for this scenario: the server has understood the request format, located the correct endpoint, but cannot fulfil the request because the payload contains a semantic error — specifically, a reference to a non-existent resource. It immediately tells the client: "your request was structurally valid, but a value inside your request body is invalid." This guides the developer directly to inspect and correct the `roomId` field, rather than questioning their URL.

---

#### Q5.2 — Security Risks of Exposing Internal Java Stack Traces to API Consumers

Returning raw Java stack traces in HTTP error responses is a serious security vulnerability for several reasons:

**Technology fingerprinting:** Stack traces expose the full class and package hierarchy (e.g., `com.smartcampus.resources.RoomResource`), the JAX-RS implementation and version (e.g., `org.glassfish.jersey.server`), and the JDK version. An attacker uses this to identify the exact software stack and cross-reference it against public vulnerability databases (CVE, NVD) to find known exploits targeting those specific versions.

**Application logic disclosure:** Method names and line numbers reveal the internal code structure and execution flow, helping an attacker identify which code paths handle sensitive operations and where boundary conditions or edge cases might be exploitable.

**File system path disclosure:** Stack traces frequently include absolute server-side file paths (e.g., `/home/appuser/SmartCampusAPI/src/...`), exposing the deployment directory structure and internal naming conventions useful for further attacks such as directory traversal.

**Error message leakage:** The exception message itself may inadvertently expose internal state such as field names, configuration values, or query structures that were never intended to be visible to external clients.

The `GlobalExceptionMapper` in this project mitigates all of these risks by catching every unhandled `Throwable`, writing the full stack trace to the **server log only** (for developer debugging), and returning a generic, information-free body to the client:

```json
{
  "errorMessage": "An unexpected error occurred. Please contact support.",
  "errorCode": 500,
  "documentation": "https://smartcampus.edu/api/docs/errors"
}
```

---

#### Q5.3 — Why JAX-RS Filters Are Superior to Manual Logging Inside Every Resource Method

**Logging** is a classic **cross-cutting concern** — functionality that must apply uniformly across every endpoint, regardless of the specific business logic being executed at that endpoint.

**If logging were inserted manually into each resource method:**
- Every method would contain repetitive `LOGGER.info(...)` boilerplate entirely unrelated to its business purpose, reducing readability.
- If the log format, log level, or log destination changes, every method across every resource class must be updated — creating high maintenance cost and a real risk of inconsistency.
- A developer writing a new resource method could easily forget to include logging, creating silent blind spots in system observability.
- Unit tests for resource methods are complicated by logging side-effects tangled into the same code under test.

**Using a JAX-RS `ContainerRequestFilter` and `ContainerResponseFilter`** *(this implementation)*:

- **Single source of truth:** All logging logic lives in one class (`LoggingFilter.java`). Any change to log format or behaviour requires editing exactly one file.
- **Guaranteed coverage:** The filter is registered once in `SmartCampusApplication` and fires automatically for every single request and response — no endpoint can accidentally be left unmonitored.
- **Clean separation of concerns:** Resource classes contain only business logic and have no dependency on or awareness of the logging implementation.
- **Extensibility without disruption:** New cross-cutting concerns — authentication, CORS headers, rate-limiting, request ID injection — can be added as separate filter classes without modifying any existing resource code.
- **Testability:** Resource methods can be unit-tested in complete isolation with no logging side-effects to account for.

This is a direct application of the **Chain of Responsibility** design pattern and is conceptually equivalent to middleware pipelines in frameworks such as Express.js (Node.js) and Django (Python).
