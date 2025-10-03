package com.sathwikhbhat.bookingservice.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI bookingServiceAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Booking Service API")
                        .description("API documentation for the Booking Service")
                        .version("v1.0.0"));
    }

}