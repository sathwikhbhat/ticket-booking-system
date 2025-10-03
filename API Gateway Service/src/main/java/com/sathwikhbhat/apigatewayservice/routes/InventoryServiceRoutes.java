package com.sathwikhbhat.apigatewayservice.routes;

import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class InventoryServiceRoutes {

    @Bean
    public RouterFunction<ServerResponse> inventoryRoutes() {
        return GatewayRouterFunctions.route("inventory-service")
                .route(RequestPredicates.GET("/api/v1/inventory/events"),
                        HandlerFunctions.forward("http://localhost:8082/api/v1/inventory/events"))
                .route(RequestPredicates.GET("/api/v1/inventory/venue/{venueId}"),
                        request -> forwardWithPathVariables(request, "venueId", "http://localhost:8082/api/v1/inventory/venue"))
                .route(RequestPredicates.GET("/api/v1/inventory/event/{eventId}"),
                        request -> forwardWithPathVariables(request, "eventId", "http://localhost:8082/api/v1/inventory/event"))
                .route(RequestPredicates.PUT("/api/v1/inventory/event/{eventId}/capacity/{ticketsBooked}"),
                        request -> {
                            String eventId = request.pathVariable("eventId");
                            String ticketsBooked = request.pathVariable("ticketsBooked");
                            String url = String.format("http://localhost:8082/api/v1/inventory/event/%s/capacity/%s", eventId, ticketsBooked);
                            return HandlerFunctions.forward(url).handle(request);
                        })
                .build();
    }

    private static ServerResponse forwardWithPathVariables(ServerRequest request,
                                                           String pathVariable,
                                                           String baseUrl) throws Exception {
        String value = request.pathVariable(pathVariable);
        return HandlerFunctions.forward(baseUrl + "/" + value).handle(request);
    }

}