# Smart Campus – Sensor & Room Management API

**5COSC022W – Client-Server Architectures | University of Westminster**
**Student:** Jay Nileshbhai Patel, w2105573(7)

A RESTful API built with JAX-RS (Jersey 2.32) and deployed on Apache Tomcat 9. It manages rooms and IoT sensors across a smart campus. You can register rooms, attach sensors to them, post sensor readings, and query historical data. The API enforces business rules such as blocking readings from sensors that are under maintenance, preventing deletion of rooms that still have sensors assigned, and rejecting sensors linked to non-existent rooms.

No database. No Spring Boot. Just Jersey, Jackson, ConcurrentHashMaps, and java.util.logging.

---

## Tech Stack

| Layer | Technology |
|---|---|
| REST Framework | Jersey 2.32 (JAX-RS 2.1, javax.ws.rs.*) |
| JSON | Jackson via jersey-media-json-jackson |
| Server | Apache Tomcat 9.0.x (WAR deployment) |
| Build | Maven 3, WAR packaging |
| Java | Java 11 (source + target) |
| IDE | Apache NetBeans (Maven Web Application) |

---

## Build and Run

### Prerequisites
- JDK 11 or later
- Apache NetBeans with Apache Tomcat 9.0.x configured
- Maven (bundled with NetBeans)

### Steps

**1. Clone the repo**
```bash
git clone https://github.com/[your-username]/SmartCampusAPI.git
```

**2. Open in NetBeans**
File → Open Project → select the `SmartCampusAPI` folder. NetBeans detects it as a Maven web project and loads it automatically.

**3. Set Tomcat as the server**
Right-click the project → Properties → Run → set Server to Apache Tomcat 9.0.x. If Tomcat is not listed, go to Tools → Servers → Add Server → Apache Tomcat or TomEE, then point it to your Tomcat installation folder.

**4. Clean and Build**
Right-click the project → Clean and Build. Wait for `BUILD SUCCESS` in the Output panel. This produces a `.war` file in the `target/` folder.

**5. Run**
Right-click the project → Run (or press F6). NetBeans deploys the WAR to Tomcat and starts the server automatically.

**6. Verify**
Open a browser or Postman and hit:
```
GET http://localhost:8080/SmartCampusAPI/api/v1
```
You should get a JSON discovery response with links to rooms and sensors.

**Base URL:** `http://localhost:8080/SmartCampusAPI/api/v1`

---

## Endpoints

| Method | Path | Description | Status Codes |
|---|---|---|---|
| GET | `/` | Discovery endpoint – API metadata and resource links | 200 |
| GET | `/rooms` | List all rooms as full objects | 200 |
| POST | `/rooms` | Create a new room | 201, 415 |
| GET | `/rooms/{roomId}` | Get a specific room by ID | 200, 404 |
| DELETE | `/rooms/{roomId}` | Delete a room (blocked if sensors are assigned) | 204, 404, 409 |
| GET | `/sensors` | List all sensors, optional `?type=` filter | 200 |
| POST | `/sensors` | Register a sensor, validates roomId exists | 201, 415, 422 |
| GET | `/sensors/{sensorId}/readings` | Get reading history for a sensor | 200, 404 |
| POST | `/sensors/{sensorId}/readings` | Add a new reading (sensor must be ACTIVE) | 201, 403, 404 |

---

## Sample Data

| Type | ID | Details |
|---|---|---|
| Room | LIB-301 | Library Quiet Study, capacity 40 |
| Room | LAB-101 | Computer Lab, capacity 30 |
| Sensor | TEMP-001 | Temperature, ACTIVE, 21.5°C, in LIB-301 |
| Sensor | CO2-001 | CO2, MAINTENANCE, 400.0 ppm, in LAB-101 |

TEMP-001 is ACTIVE so you can POST readings to it straight away.
CO2-001 is in MAINTENANCE so POSTing a reading to it returns 403 immediately.

---

## curl Commands

```bash
# 1 - Discovery endpoint: confirms API version and links to /rooms and /sensors
curl -s http://localhost:8080/SmartCampusAPI/api/v1

# 2 - List all rooms: returns LIB-301 and LAB-101 as full objects
curl -s http://localhost:8080/SmartCampusAPI/api/v1/rooms

# 3 - Get a single room by ID: returns full room object including sensorIds
curl -s http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301

# 4 - Create a new room: expects 201 Created plus Location header
curl -s -i -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"HALL-01","name":"Main Lecture Hall","capacity":200}'

# 5 - Delete a room that still has sensors (LIB-301 has TEMP-001): expects 409 Conflict
curl -s -i -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301

# 6 - Delete the empty room created in step 4: expects 204 No Content
curl -s -i -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/HALL-01

# 7 - Create a sensor linked to LAB-101: expects 201 Created
curl -s -i -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"OCC-001","type":"Occupancy","status":"ACTIVE","currentValue":0,"roomId":"LAB-101"}'

# 8 - Create a sensor with a roomId that does not exist: expects 422 Unprocessable Entity
curl -s -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-999","type":"Temperature","status":"ACTIVE","currentValue":0,"roomId":"FAKE-999"}'

# 9 - Filter sensors by type: expects only CO2-001 in the result
curl -s "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2"

# 10 - Post a reading to an ACTIVE sensor (TEMP-001): expects 201 Created
curl -s -i -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"id":"READ-001","timestamp":1714000000000,"value":23.7}'

# 11 - Get reading history for a sensor: returns the reading posted in step 10
curl -s http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings

# 12 - Post a reading to a MAINTENANCE sensor (CO2-001): expects 403 Forbidden
curl -s -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d '{"id":"READ-002","timestamp":1714000005000,"value":500}'
```

---

## Report Answers

---

### Part 1.1

**Question:** In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

**Answer:**
JAX-RS uses a per-request lifecycle by default. The runtime creates a brand new instance of each resource class for every incoming HTTP request and throws it away once the response is sent. Anything stored as an instance field disappears at the end of that request.

This means the data cannot live inside the resource class itself. It has to go somewhere that survives across requests. In this project the shared maps for rooms, sensors, and readings are declared as `static` fields on `RoomResource`, so they belong to the class rather than any particular instance and persist for the entire lifetime of the application.

The issue with static fields is that Tomcat handles multiple requests on multiple threads simultaneously. Two concurrent POST requests could read the same map state, both try to write, and silently corrupt it. A plain `HashMap` is not thread-safe and can cause data loss or unexpected behaviour under concurrent access. Using `ConcurrentHashMap` solves this because it handles concurrent reads and writes safely through internal segment-level locking. No explicit `synchronized` blocks are needed, and threads can operate on the map at the same time without racing each other.

---

### Part 1.2

**Question:** Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

**Answer:**
HATEOAS stands for Hypermedia as the Engine of Application State. The idea is that each API response includes links telling the client where it can go next, rather than the client having those URLs hardcoded from a document it read externally.

In this API, hitting `GET /api/v1` returns a `resources` map with links to `/api/v1/rooms` and `/api/v1/sensors`. A client following those links does not need to know the URL structure in advance — it discovers it from the root.

The benefit over static documentation is that the API becomes self-describing. If a URL path changes or a new resource is added, clients following embedded links pick up the change automatically. Clients that hardcode URLs break the moment the server changes them. Static documentation goes stale; links do not. This places the API at Level 3 of Richardson's REST Maturity Model, the highest level, because it fully decouples clients from any assumed knowledge of the server's URL structure.

---

### Part 2.1

**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.

**Answer:**
Returning only IDs produces a small payload, which is useful on slow networks or when the collection is very large. The problem is that a client needing any actual room data then has to fire a separate GET request for every ID it received. With 50 rooms, that is 51 HTTP round-trips. This is the N+1 problem and it hammers latency and server load.

Returning full room objects costs more bandwidth per response but solves the N+1 problem entirely. One request gives the client everything it needs to render a full view. For a university campus room list where the collection is small and room objects are lightweight, the bandwidth cost is negligible and the reduction in round-trips is a clear win.

A middle ground used in production APIs is to return summary objects in list responses — just `id` and `name` — and reserve the full representation for the single-item `GET /rooms/{roomId}` endpoint. This project returns full objects to keep things simple and eliminate any need for follow-up requests.

---

### Part 2.2

**Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

**Answer:**
It depends on which scenario you are in.

For `DELETE /rooms/HALL-01` where the room exists and has no sensors: the first call removes it and returns 204 No Content. A second call finds nothing there and returns 404 Not Found. The server state is the same both times — the room is gone — but the response code differs. RFC 7231 considers this acceptable under the definition of idempotency, which is about consistent server state rather than identical response codes.

For `DELETE /rooms/LIB-301` where the room has sensors attached: every call returns 409 Conflict and nothing changes on the server. That path is fully idempotent in both state and response code.

So the operation satisfies the idempotency requirement because repeated calls leave the server in the same state, but clients should be aware that the 404 on a second successful-deletion attempt is expected and not an error in their logic.

---

### Part 3.1

**Question:** We explicitly use the `@Consumes(MediaType.APPLICATION_JSON)` annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

**Answer:**
If a client sends a POST request with `Content-Type: text/plain` or `Content-Type: application/xml` to a method annotated with `@Consumes(MediaType.APPLICATION_JSON)`, JAX-RS returns 415 Unsupported Media Type before the method body runs at all.

The check happens at the dispatch layer. Jersey compares the incoming `Content-Type` header against the `@Consumes` declaration. If they do not match, the resource method is never invoked. No exception mapper runs, no custom logic fires. The client gets a clean 415 straight from the runtime.

This is safer and cleaner than accepting any content type and trying to parse it manually. The runtime handles the rejection consistently across every endpoint, and the method body stays focused on processing valid JSON rather than having to guard against unexpected formats itself.

---

### Part 3.2

**Question:** You implemented this filtering using `@QueryParam`. Contrast this with an alternative design where the type is part of the URL path (e.g., `/api/v1/sensors/type/CO2`). Why is the query parameter approach generally considered superior for filtering and searching collections?

**Answer:**
There are three clear reasons why `@QueryParam` is the better choice here.

First, query parameters are optional by default. `GET /sensors` returns everything; `GET /sensors?type=CO2` returns filtered results. With a path-based design you would need separate resource methods for the filtered and unfiltered cases, which duplicates routing logic.

Second, query parameters compose naturally. If you later needed to filter by both type and status you would write `GET /sensors?type=CO2&status=ACTIVE`. The path equivalent would be `GET /sensors/type/CO2/status/ACTIVE`, which looks like a deeply nested sub-resource and makes no semantic sense.

Third, the URL path is meant to identify a resource. `/sensors` is the collection. `/sensors/type/CO2` implies that `type/CO2` is a named resource inside the sensors collection, which it is not. A query parameter says "give me the sensors collection, narrowed down this way." That matches what is actually happening.

---

### Part 4.1

**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

**Answer:**
The sub-resource locator in `SensorResource` is the method annotated with `@Path("/{sensorId}/readings")` that carries no HTTP verb annotation. It creates and returns a new `SensorReadingResource` instance, and Jersey dispatches the actual HTTP method to that class instead.

The alternative is putting everything in one class. `SensorResource` would end up handling sensor CRUD, reading history, reading creation, and anything else nested under `/sensors`. That class grows without limit and becomes hard to read, test, or modify without breaking something unrelated.

With the locator pattern, `SensorResource` handles sensor-level operations and `SensorReadingResource` handles the reading lifecycle. Each class has one job. If the reading logic needs to change, only `SensorReadingResource` is touched. If a new sub-resource like `/sensors/{id}/alerts` gets added later, it gets its own class. The hierarchy in the code mirrors the hierarchy in the URLs, which makes the codebase straightforward to reason about and navigate.

---

### Part 5.1

**Question:** Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

**Answer:**
404 Not Found means the URL the client requested does not exist on the server. But when a POST arrives at `/api/v1/sensors`, that endpoint is present and working. The problem is not with the URL at all.

The problem is inside the request body. The `roomId` field references a room that does not exist in the system. The JSON is syntactically valid, the Content-Type is correct, and the endpoint was found. The server just cannot act on the request because the data inside it is logically broken.

422 Unprocessable Entity is the right code for that situation. It tells the client: your request was well-formed and the server understood it, but the semantic content cannot be processed. A 404 response would make the client think they sent the request to the wrong URL and send them debugging in the wrong direction entirely.

---

### Part 5.2

**Question:** From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

**Answer:**
A stack trace hands an attacker a detailed map of the system without them needing access to the source code.

The first problem is technology fingerprinting. A trace exposes the Java version, Jersey version, Tomcat version, and every other framework visible in the call stack. An attacker takes those version numbers, searches public CVE databases, and finds known exploits that target that exact configuration.

The second problem is architecture exposure. Package and class names like `com.smartcampus.resources.RoomResource` or `com.smartcampus.mapper.GlobalExceptionMapper` reveal the internal structure, naming conventions, and where different parts of the logic live. That gives an attacker a clear picture of what to target.

The third problem is logic exposure. The sequence of method calls in a trace shows how the server processes a request internally, which helps someone craft inputs designed to reach specific code paths or trigger specific conditions.

The `GlobalExceptionMapper` in this project handles all of this by catching every unhandled `Throwable`, logging the full error server-side where only developers can see it, and returning only a generic message to the client. The client learns that something went wrong but learns nothing about the system behind it.

---

### Part 5.3

**Question:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

**Answer:**
The core problem with manual logging is reliability. If you add `Logger.info()` calls inside each resource method you have to remember every method, including every one added in the future. One missed call creates a blind spot. Error paths handled by exception mappers may never log at all because the resource method may not have run to completion.

A `ContainerRequestFilter` and `ContainerResponseFilter` pair runs on every single request and response automatically, including error paths. It is written once in `LoggingFilter`, annotated with `@Provider`, and Jersey registers it without any extra configuration in `SmartCampusApplication`.

It also keeps the resource methods clean. `RoomResource.deleteRoom()` should handle deletion logic. It should not be mixed up with log formatting. Keeping the two concerns separate makes both easier to read and maintain. If the log format needs to change later, one class is edited rather than every method across the entire codebase.
