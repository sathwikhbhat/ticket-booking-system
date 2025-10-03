package com.sathwikhbhat.inventoryservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI inventoryServiceAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Inventory Service API")
                        .description("API documentation for the Inventory Service")
                        .version("v1.0.0"));
    }

}