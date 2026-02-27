package com.sodimac.catman.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Component
public class SpringDocConfigurer {

    @Bean
    public OpenAPI configure() {
        return new OpenAPI()
                .specVersion(SpecVersion.V30)
                .info(new Info()
                        .title("Catalogos API - FBC Portal")
                        .description("""
                                API de catálogos compartidos para el portal Falabella Business Center (FBC).

                                ## Funcionalidades principales:
                                - Consulta de catálogos maestros del sistema
                                - Soporte multi-idioma (Español, Inglés, Portugués)
                                - Consulta de mensajes por clave estandarizada

                                ## Idiomas soportados:
                                - `1` = Español (por defecto)
                                - `2` = Inglés
                                - `3` = Portugués
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo FBC")
                                .email("fbc-team@falabella.com")))
                .addServersItem(new Server()
                        .url("/")
                        .description("Servidor actual"))
                .addServersItem(new Server()
                        .url("https://dev.vendor.fbusinesscenter.com/ppsomx/catalogos")
                        .description("Desarrollo"));
    }
}
