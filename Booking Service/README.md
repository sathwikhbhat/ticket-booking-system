# Booking Service

Accepts booking requests, validates availability with the Inventory Service, calculates pricing, and publishes a `BookingEvent` to Kafka for downstream order processing (no booking row persisted yet).

## 1. Responsibilities
| Concern | Role |
|---------|-----|
| Booking intake | Single POST endpoint |
| Capacity validation | Delegates to Inventory Service |
| Event emission | Kafka topic `booking` |
| Price calculation | Computes total price per booking |
| Customer lookup | Retrieves existing customer (must already exist) |

## 2. Tech Stack
| Component | Version |
|-----------|---------|
| Spring Boot | 3.5.6 |
| Spring Data JPA | Included |
| Spring Kafka | Latest |
| MySQL | 8.3.0 |
| OpenAPI | springdoc 2.8.0 |

## 3. API
| Method | Path | Description |
|--------|------|-------------|
| POST | /api/v1/booking | Create a new booking |

### 3.1 Request Example
```json
{
  "userId": 123,
  "eventId": 456,
  "ticketCount": 8
}
```

### 3.2 Response Example
Assuming default `ticket_price` 500.00 and `ticketCount` 8:
```json
{
  "eventId": 456,
  "userId": 123,
  "ticketCount": 8,
  "totalPrice": 4000.00
}
```

## 4. Event Model
Topic: `booking`
```java
public class BookingEvent {

  private Long userId;
  private Long eventId;
  private Long ticketCount;
  private BigDecimal totalPrice;

}
```
Producer configured through `spring.kafka.*` properties.

## 5. Configuration (Key Properties)
```properties
server.port=8081
inventory.service.url=http://localhost:8080/api/v1/inventory
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.template.default-topic=booking
```

## 6. Booking Flow
1. Receive POST at `/api/v1/booking`.
2. Load existing customer by ID.
3. Fetch event & capacity (and ticket_price) via Inventory Service.
4. Validate availability & compute `totalPrice = ticket_price * ticketCount`.
5. Publish `BookingEvent` to Kafka.
6. Return booking response.

## 7. Build & Run
```powershell
mvn clean install
mvn spring-boot:run
```
Runs on http://localhost:8081
<br>
Swagger: http://localhost:8081/swagger-ui.html

## 8. Integration Points
| External | Purpose |
|----------|---------|
| Inventory Service | Event availability lookup |
| Kafka | Event publication to `booking` topic |
| MySQL | Booking & customer persistence |

## 9. Local Development Checklist
1. Docker compose stack running (MySQL + Kafka + Keycloak if using gateway auth).
2. Inventory Service up (port 8080).
3. Ensure customer row exists (insert manually beforehand).
4. Start this service.
5. POST a booking request.
6. Inspect Kafka topic via Kafka UI (8084).

## 10. Testing (Manual Curl)
```bash
curl -X POST http://localhost:8081/api/v1/booking \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"eventId":1,"ticketCount":1}'
```

## 11. Troubleshooting
| Symptom | Check |
|---------|-------|
| 500 on booking | Inventory service reachable? JSON fields valid? |
| Kafka publish fails | Broker up? Topic auto-created? |
| Slow response | Upstream inventory latency / network |
| Wrong totalPrice | Verify `ticket_price` value & multiplication |


---
Part of the Ticket Booking System – see the [root README](../README.md) for overall architecture.