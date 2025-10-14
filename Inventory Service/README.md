# Inventory Service

Handles event + venue data and keeps track of remaining ticket capacity. Also temporarily owns some shared tables (customer, order). Other services call these REST endpoints.

## 1. Responsibilities
| Area | Responsibility |
|------|----------------|
| Venue | Lookup metadata |
| Event | Query individual / list all |
| Capacity | Expose capacity value  |

## 2. Tech Stack
| Component | Version | Notes |
|-----------|---------|-------|
| Spring Boot | 3.5.6 | REST + JPA |
| MySQL | 8.3.0 | Schema `ticketing` |
| Flyway | Core + MySQL | Versioned migrations |
| springdoc-openapi | 2.8.0 | Swagger UI |

## 3. Schema
| Table | Columns (selected) | Source Migration | Notes |
|-------|--------------------|------------------|-------|
| venue | id, name, address, total_capacity | V1__init.sql | |
| event | id, name, venue_id, total_capacity, left_capacity, ticket_price | V1__init.sql + V2__add_ticket_column_in_event_table.sql | ticket_price default 500.00 |
| customer | id, name, email, address | V3__create_customer_table.sql | |
| order | id, total, quantity, placed_at, customer_id, event_id | V4__create_order_table.sql | quantity = ticket count |

## 4. API Endpoints
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1/inventory/events | List all events |
| GET | /api/v1/inventory/venue/{venueId} | Get venue by ID |
| GET | /api/v1/inventory/event/{eventId} | Get event details |
| PUT | /api/v1/inventory/event/{eventId}/capacity/{ticketsBooked} | Reduce event capacity by tickets booked |

## 5. Configuration
Key properties:
```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/ticketing
spring.jpa.hibernate.ddl-auto=none
springdoc.api-docs.path=/v3/api-docs
```

## 6. Running (Standalone)
From project root / or inside this directory:
```powershell
docker-compose up -d
```
```powershell
# Build & Run
mvn clean install
mvn spring-boot:run
```
Service: http://localhost:8080
<br>
Swagger: http://localhost:8080/swagger-ui.html

## 7. Docker Compose Components
| Service | Port | Purpose |
|---------|------|---------|
| mysql | 3306 | Init via init.sql |
| zookeeper | 2181 | Kafka coordination |
| kafka-broker | 9092 / 29092 | Broker (host/internal) |
| kafka-ui | 8084 | Topic inspection |
| kafka-schema-registry | 8083 | Schema registry (not referenced by services) |
| keycloak / keycloak-db | 8091 / internal | Auth server & DB |

## 8. Data Lifecycle
* Flyway runs migrations on startup.
* No destructive auto DDL.
* Capacity gets reduced after the Order Service processes a booking event.


## 9. Local Debug Workflow
1. Start compose stack
2. Launch the service
3. GET `/api/v1/inventory/events`
4. (Optional) Watch the logs at DEBUG level

## 10. Troubleshooting
| Symptom | Check |
|---------|-------|
| Migration failed | File name ordering / SQL syntax |
| Connection refused | MySQL container healthy? Port 3306 free? |
| Empty tables | Did init.sql execute? Flyway baseline? |
| Swagger 404 | App started? `/swagger-ui.html` correct? |

---
Part of the Ticket Booking System – see the [root README](../README.md) for overall architecture.