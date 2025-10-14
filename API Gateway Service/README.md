# API Gateway Service

Simple edge service for authentication, routing, a little resilience, and combined API docs.

## 1. Overview
Entry point for calls: validates JWT (Keycloak), applies a basic circuit breaker + timeout, and merges downstream OpenAPI docs.

## 2. Features
| Category | Capability | Notes |
|----------|-----------|-------|
| Security | OAuth2 Resource Server | Keycloak realm: `ticketing-security` |
| Routing | Path-based forwarding | Inventory endpoints currently exposed |
| Docs | Swagger aggregation | Multi-service UI at `/swagger-ui.html` |
| Resilience | Circuit breaker, retry, timeout | Basic Resilience4j config |
| Observability | Actuator health endpoints | All endpoints exposed in dev |

## 3. Current Routes
| Method | Incoming Path | Downstream Target |
|--------|---------------|------------------|
| GET | `/api/v1/inventory/events` | http://localhost:8080/api/v1/inventory/events |
| GET | `/api/v1/inventory/venue/{venueId}` | http://localhost:8080/api/v1/inventory/venue/{venueId} |
| GET | `/api/v1/inventory/event/{eventId}` | http://localhost:8080/api/v1/inventory/event/{eventId} |
| PUT | `/api/v1/inventory/event/{eventId}/capacity/{ticketsBooked}` | http://localhost:8080/api/v1/inventory/event/{eventId}/capacity/{ticketsBooked} |
| POST | `/api/v1/booking` | http://localhost:8081/api/v1/booking |
| POST | /fallbackRoute | (internal) |


## 4. Security Model
| Aspect | Detail |
|--------|-------|
| Auth Server | Keycloak (container, port 8091) |
| Token Type | JWT (RS256) |
| Validation | Issuer + JWK Set URI |
| Public Paths | `/actuator/**`, `/v3/api-docs/**`, `/swagger-ui.html`, `/swagger-ui/**`, `/docs/**` |
| Protected Paths | All other routes |

Only Inventory & Booking show up in the aggregated docs (Order Service has no HTTP endpoints).

## 5. OpenAPI Aggregation
Configured via `springdoc.swagger-ui.urls[n]` entries in `application.properties`:

| Name | Upstream API Docs Path |
|------|------------------------|
| Inventory Service | /docs/inventoryservice/v3/api-docs |
| Booking Service | /docs/bookingservice/v3/api-docs |

## 6. Resilience4j Settings
| Property | Value | Purpose |
|----------|-------|---------|
| slidingWindowSize | 8 | Statistical window |
| failureRateThreshold | 50% | Trip threshold |
| waitDurationInOpenState | 5s | Cooldown |
| timeout-duration | 3s | Time limiter |
| retry max-attempts | 3 | Retry policy |

## 7. Configuration Snippet
```properties
server.port=8090
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8091/realms/ticketing-security
```

## 8. Build & Run
```powershell
mvn clean install
mvn spring-boot:run
```
Needs the shared infra (Keycloak etc.) running first.

## 9. Health & Diagnostics
| Endpoint | Purpose |
|----------|---------|
| /actuator/health | Liveness/readiness (basic) |
| /actuator | List all actuator endpoints |


## 10. Troubleshooting
| Issue | Action |
|-------|--------|
| 401 Unauthorized | Token issuer / client config in Keycloak |
| 404 on docs | Aggregation URLs & downstream service running |
| Circuit breaker OPEN | Downstream healthy? thresholds too strict? |
---
Part of the Ticket Booking System – see the [root README](../README.md) for overall architecture.