package com.prime.gateway.gateway.infrastructure.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("Progreso")
                        .description("""
                                Progreso is a platform that aims to facilitate the operational tasks in the execution process of the internships \n
                                in Prime Holding. The platform should provide functionalities for keeping track of interns’ progress and feedback, \n
                                absence tracking, and event management.\n
                                It should also be able to offer reports based on past internships.
                                """)
                        .version("v1.0")
                        .termsOfService("TOC"));
    }
}
