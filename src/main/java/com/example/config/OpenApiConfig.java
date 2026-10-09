package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Configuracion de OpenAPI/Swagger, separada deliberadamente de
 * CreatesSamplesData (esa clase solo siembra datos de prueba en la base de
 * datos; esto es documentacion de la API, una responsabilidad distinta).
 *
 * El bean de aqui abajo añade a la especificacion OpenAPI un esquema de
 * seguridad "bearer-jwt" de tipo HTTP Bearer. Gracias a esto, Swagger UI
 * (y Scalar, que tambien esta en el pom.xml) muestran un boton "Authorize"
 * donde pegar el token JWT una sola vez; a partir de ahi, todas las
 * peticiones que se prueben desde esa interfaz incluyen automaticamente la
 * cabecera "Authorization: Bearer <token>", sin tener que copiarla a mano en
 * cada endpoint protegido.
 *
 * El token en si se obtiene llamando antes a POST /api/auth/signin (no lo
 * genera este bean); esto solo le dice a la interfaz grafica DONDE pegarlo.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH_SCHEME = "bearer-jwt";

    @Bean
    OpenAPI customOpenAPI() {

        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        return new OpenAPI()
                .info(new Info()
                        .title("Rest API Demo Mostoles Backend 2026")
                        .description("API REST con HATEOAS y seguridad JWT")
                        .version("v1"))
                // Aplica el esquema de seguridad por defecto a TODOS los endpoints
                // documentados, para que el boton "Authorize" cubra toda la API
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME, bearerScheme));
    }
}
