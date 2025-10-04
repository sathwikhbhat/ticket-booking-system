# Order Service

Listens to booking events on Kafka and turns them into order rows. It only stores ids (no JPA relationships).

## 1. Responsibilities
| Concern | Role |
|---------|-----|
| Event consumption | Kafka topic `booking` (group: order-service) |
| Order persistence | Writes order row per booking event |
| Inventory update | Decrement event capacity |

## 2. Tech Stack
| Component | Version |
|-----------|---------|
| Spring Boot | 3.5.6 |
| Spring Data JPA | Included |
| Spring Kafka | Consumer only (libs include producer but unused) |
| MySQL | 8.3.0 |

## 3. Event Contract Consumed
Topic: `booking`
```java
public class BookingEvent {

    private Long userId;
    private Long eventId;
    private Long ticketCount;
    private BigDecimal totalPrice;

}
```

## 4. Configuration (Key Properties)
```properties
server.port=8082
spring.kafka.consumer.group-id=order-service
spring.kafka.consumer.properties.spring.json.type.mapping=bookingEvent:com.sathwikhbhat.bookingservice.event.BookingEvent
```

## 5. Processing Flow
1. Kafka Listener receives `BookingEvent`.
2. Map -> Order entity.
3. Save to MySQL (`order` table).
4. Call Inventory Service to decrement capacity.

### 5.1 Listener Snippet
```java
@KafkaListener(topics = "booking", groupId = "order-service")
public void processBookingEvent(BookingEvent bookingEvent) {
    // Validate, map, persist order
}
```

## 6. Build & Run
```powershell
mvn clean install
mvn spring-boot:run
```
Runs on http://localhost:8082 (there are no REST endpoints in this service)

## 7. Integration Points
| External | Purpose |
|----------|---------|
| Kafka | Consume booking events |
| MySQL | Persist orders |
| Inventory Service | Capacity decrement |


## 8. Local Development Checklist
1. Compose stack (Kafka + MySQL) running.
2. Inventory & Booking up.
3. POST a booking.
4. Watch logs / check DB.

## 9. Troubleshooting
| Symptom | Check |
|---------|-------|
| No orders created | Events in Kafka UI? Consumer group active? |
| Deserialization errors | JSON type mapping property value |
| DB failures | MySQL up? creds correct? |
| High lag | Broker health / network / commits |

---
Part of the Ticket Booking System – see the [root README](../README.md) for overall architecture.