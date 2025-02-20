package com.koliving.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Arrays;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private String version;
    private String title;

    @Bean
    public OpenAPI apiV1() {
        version = "v1";
        title = "Koliving openApi " + version;

        SecurityScheme securityScheme = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .info(getInfo(version, title))
                .components(getComponents().addSecuritySchemes("bearerAuth", securityScheme))
                .security(Arrays.asList(securityRequirement))
                .addServersItem(getServer());
    }

//    @Bean
//    GroupedOpenApi authGroup() {
//        version = "v1";
//
//        String authApi = String.format("/api/%s/auth/**", version);
//
//        return GroupedOpenApi.builder()
//                .group("auth")
//                .pathsToMatch(authApi)
//                .build();
//    }



    private Components getComponents() {
        return new Components();
    }

    private Info getInfo(String version, String title) {
        return new Info()
                .version(version)
                .title(title)
                .description("Koliving");
    }

    private Server getServer() {
        return new Server()
                .url("/");
    }
}
