# Ticket Booking System

A personal learning project made of a few Spring Boot microservices for booking event tickets. It uses Kafka for async messaging, a simple API gateway for routing + auth, and Docker Compose to spin up the supporting stuff (MySQL, Kafka, Keycloak, etc.).


## 1. Overview
You can create a booking for an event. Instead of writing the booking straight to an orders table, the Booking Service publishes a Kafka message. The Order Service listens, creates the order, and asks Inventory to reduce remaining capacity. Auth (when you go through the gateway) uses Keycloak-issued JWTs.

## 2. System Architecture & Event Flow
<p align="center">
	<img src="Project%20Flow.png" alt="Ticket Booking System Architecture and Event Flow" width="70%" />
	<br/>
	<em>High-level architecture: client -> gateway -> services (Booking, Inventory, Order) with Kafka event flow.</em>
</p>


1. Client calls booking endpoint through the gateway (with JWT if enabled).
2. Booking Service checks the event + capacity using Inventory.
3. It publishes a `BookingEvent` to Kafka (no DB row here).
4. Order Service consumes the event, writes an order, asks Inventory to decrement capacity.
5. Done. Order exists, capacity reduced.

## 3. Services Summary
| Service | Port | Purpose | External Interfaces |
|---------|------|---------|---------------------|
| API Gateway | 8090 | Central routing & security | Keycloak, Inventory, Booking (Swagger aggregation) |
| Inventory Service | 8080 | Venue & event data, capacity | MySQL, Gateway, Booking/Order services |
| Booking Service | 8081 | Accepts bookings, emits events | Kafka (producer), Inventory (REST), MySQL |
| Order Service | 8082 | Consumes booking events to create orders | Kafka (consumer), Inventory (REST), MySQL |

## 4. Technology Stack
| Layer | Tech | Notes |
|-------|------|-------|
| Language | Java 21 | LTS Version |
| Framework | Spring Boot 3.5.6 | Plain Boot setup |
| Messaging | Kafka (single node) | For async order creation |
| Auth | Keycloak 24.0.1 | Simple local realm import |
| DB | MySQL 8.3.0 | One schema `ticketing` |
| Migrations | Flyway | Only wired in Inventory |
| API Docs | springdoc-openapi | Auto-generated Swagger UIs |
| Resilience | Resilience4j | Basic circuit breaker on gateway route |
| Orchestration | Docker Compose | Local only |

### 4.1 Build Artifacts
Each folder is its own Spring Boot app. Run `mvn clean install` inside a service and you get a runnable jar in `target/`.

## 5. Directory Structure
```
ticket-booking-system/
├── API Gateway Service/
├── Inventory Service/
│   ├── docker-compose.yml   (Infra stack: MySQL, Kafka, Zookeeper, Keycloak, UI, Schema Registry)
│   └── docker/
├── Booking Service/
├── Order Service/
└── README.md
```

## 6. Data Model (Current Tables)
| Table | Key Columns (selected) | Purpose | Notes |
|-------|------------------------|---------|-------|
| venue | id, name, address, total_capacity | Physical or virtual venue | V1 |
| event | id, name, venue_id, total_capacity, left_capacity, ticket_price | Bookable entity with pricing | ticket_price added V2 |
| customer | id, name, email, address | Customer context | V3 |
| order | id, customer_id, event_id, quantity, total | Order materialization | quantity = ticket count (V4) |

## 7. Event Model & Flow
Booking Service publishes a `BookingEvent` (userId, eventId, ticketCount, totalPrice) to Kafka topic `booking` instead of writing the order directly. Order Service consumes it, creates the order, then asks Inventory to reduce remaining capacity.

### Kafka Topics Overview
| Topic | Producer | Consumer | Payload |
|-------|----------|----------|---------|
| booking | Booking Service | Order Service | BookingEvent { userId, eventId, ticketCount, totalPrice } |


## 8. Local Development

### 8.1 Start Infrastructure
```cmd
cd "Inventory Service"
docker-compose up -d
```
Wait until containers are up and healthy (MySQL, kafka-broker, keycloak, etc.).

### 8.2 Build All Services
```cmd
mvn -q -f "API Gateway Service\pom.xml" clean install
mvn -q -f "Inventory Service\pom.xml" clean install
mvn -q -f "Booking Service\pom.xml" clean install
mvn -q -f "Order Service\pom.xml" clean install
```

### 8.3 Run (Recommended Order)
```cmd
mvn -f "Inventory Service\pom.xml" spring-boot:run
mvn -f "Booking Service\pom.xml" spring-boot:run
mvn -f "Order Service\pom.xml" spring-boot:run
mvn -f "API Gateway Service\pom.xml" spring-boot:run
```

### 8.4 Smoke Test
```powershell
curl http://localhost:8090/api/v1/inventory/events
```
This route is protected. Quick dev token steps (Keycloak):
1. Open http://localhost:8091, log in to the admin console, create a test user and set a password (disable temporary password).
2. Get an access token (password grant example):
	```powershell
	curl -X POST "http://localhost:8091/realms/ticketing-security/protocol/openid-connect/token" \
	  -H "Content-Type: application/x-www-form-urlencoded" \
	  -d "client_id=<your-public-client>" \
	  -d "grant_type=password" \
	  -d "username=testuser" \
	  -d "password=changeme"
	```
	Copy the value of `access_token` from the JSON response.
3. Call the protected endpoint:
	```powershell
	curl -H "Authorization: Bearer <paste_access_token>" http://localhost:8090/api/v1/inventory/events
	```


## 9. Infrastructure (Docker Compose)
| Component | Port(s) | Notes |
|-----------|---------|-------|
| MySQL | 3306 | Schema auto-init via `init.sql` |
| Zookeeper | 2181 | Kafka coordination |
| Kafka Broker | 9092 / 29092 | 9092 host, 29092 internal |
| Kafka UI | 8084 | Web UI for Kafka topics |
| Keycloak | 8091 | Realm auto-import from `docker/keycloak/realms/` |
| Schema Registry | 8083 | For Kafka schema management (included in compose) |


## 10. Configuration & Env Vars
Some notable properties (see each service README for full list):
| Service | Key Properties |
|---------|----------------|
| Gateway | Keycloak issuer & JWK set, Resilience4j circuit breaker, swagger aggregation |
| Inventory | DB connection, Flyway, Swagger paths |
| Booking | DB, Kafka producer, Inventory base URL |
| Order | DB, Kafka consumer group & deserializer mapping |


## 11. API Documentation

Canonical OpenAPI / Swagger entry point:

| Logical API Scope | How to View | Notes |
|-------------------|-------------|-------|
| Aggregated (Gateway) | http://localhost:8090/swagger-ui.html | Single source of truth – contains Inventory + Booking routes. |
| Inventory (direct) | http://localhost:8080/swagger-ui.html (internal) | Exists for debugging only; prefer Gateway aggregated UI. |
| Booking (direct) | http://localhost:8081/swagger-ui.html (internal) | Exists for debugging only; prefer Gateway aggregated UI. |
| Order Service | N/A | No HTTP API (Kafka consumer only). |

Important:
- Use ONLY the Gateway Swagger (`:8090/swagger-ui.html`) when exploring or testing APIs; it reflects the routed, secured surface your clients should rely on.
- Direct service Swagger UIs are considered implementation detail and may diverge (e.g., security filters, path prefixes) from the externally supported contract.
- Order Service has no REST endpoints by design.


## 12. REST Controllers & Endpoints

### Inventory Service (`InventoryController`)
Base Path: `/api/v1/inventory`

| Method | Path | Description |
|--------|------|-------------|
| GET  | `/events` | List all events |
| GET  | `/venue/{id}` | Get venue by id |
| GET  | `/event/{id}` | Get event by id |
| PUT  | `/event/{id}/capacity/{ticketsBooked}` | Decrement remaining capacity by ticketsBooked |

### Booking Service (`BookingController`)
Base Path: `/api/v1`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/booking` | Create a booking (emits Kafka event) |

You can still reach the aggregated Swagger UI at the gateway, or hit each service directly.


## 13. Security
| Aspect | Detail |
|--------|--------|
| Auth Server | Keycloak (Dev mode) |
| Protection Scope | Gateway-enforced for routed endpoints |
| Public Endpoints | Actuator, OpenAPI docs (whitelisted) |
| Token Validation | JWKS + Issuer URI configured in Gateway |

Only the gateway validates tokens. Internal services just assume that the calls are fine.

## 14. Observability & Resilience
| Feature | Implementation |
|---------|----------------|
| Health | Spring Boot Actuator (Gateway: `/actuator/health`) |
| Circuit Breaking | Resilience4j circuit breaker + fallback for Booking route |
| Retry / Timeout | Resilience4j default configs (properties) |

Only the gateway has circuit breaker / retry / timeout config.


## 15. Common Workflows
| Goal | Steps |
|------|-------|
| Add new event | Insert into `event` table |
| Create booking | POST to Booking Service (via Gateway) with JWT |
| Inspect events | Open Kafka UI (http://localhost:8084) -> topic `booking` |
| Verify order creation | Query `order` table after producing booking |


## 16. Troubleshooting
| Symptom | Check |
|---------|-------|
| 401 at Gateway | Valid JWT? Keycloak running? Realm imported? |
| Booking rejected | Event capacity? Inventory service reachable? |
| Order not created | Kafka topic messages present? Consumer lag? |
| DB errors | MySQL up? Credentials & port 3306 free? |
| Swagger missing services | Aggregation URLs in gateway properties |


## 17. Dive Deeper
For configuration details, environment variables, and service‑specific notes, read each service's README:

- [API Gateway Service](./API%20Gateway%20Service/README.md)
- [Inventory Service](./Inventory%20Service/README.md)
- [Booking Service](./Booking%20Service/README.md)
- [Order Service](./Order%20Service/README.md)

If you're not sure where to start, begin with the Inventory Service to understand the data model, then the Booking + Order flow, and finally the Gateway for auth and routing concerns.


## Acknowledgements
Shout‑out to [leetjourney](https://github.com/leetjourney)