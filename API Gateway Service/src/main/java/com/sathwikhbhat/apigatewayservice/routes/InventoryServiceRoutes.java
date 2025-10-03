package com.sathwikhbhat.apigatewayservice.routes;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;

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
                        HandlerFunctions.http("http://localhost:8080/api/v1/inventory/events"))

                .route(RequestPredicates.path("/api/v1/inventory/venue/{venueId}"),
                        request -> forwardWithPathVariables(request,
                                "http://localhost:8080/api/v1/inventory/venue/",
                                "venueId"))

                .route(RequestPredicates.path("/api/v1/inventory/event/{eventId}"),
                        request -> forwardWithPathVariables(request,
                                "http://localhost:8080/api/v1/inventory/event/",
                                "eventId"))

                .route(RequestPredicates.PUT("/api/v1/inventory/event/{eventId}/capacity/{ticketsBooked}"),
                        request -> forwardWithPathVariables(request,
                                "http://localhost:8080/api/v1/inventory/event/",
                                "eventId", "capacity", "ticketsBooked"))
                .build();
    }

    private static ServerResponse forwardWithPathVariables(ServerRequest request, String baseUrl, String... pathVariables) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);

        for (int i = 0; i < pathVariables.length; i++) {
            String pathVariable = pathVariables[i];
            String value = request.pathVariable(pathVariable);

            if (i > 0) {
                urlBuilder.append("/");
            }
            urlBuilder.append(value);
        }

        return HandlerFunctions.http(urlBuilder.toString()).handle(request);
    }

    @Bean
    public RouterFunction<ServerResponse> inventoryServiceApiDocs() {
        return GatewayRouterFunctions.route("inventory-service-api-docs")
                .route(RequestPredicates.path("/docs/inventoryservice/v3/api-docs"),
                        HandlerFunctions.http("http://localhost:8080"))
                .filter(setPath("/v3/api-docs"))
                .build();
    }

}