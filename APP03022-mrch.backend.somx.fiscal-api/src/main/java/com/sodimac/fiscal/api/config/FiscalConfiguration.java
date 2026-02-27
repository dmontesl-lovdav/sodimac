package com.sodimac.fiscal.api.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;


@Configuration
public class FiscalConfiguration {

    /**
     * RestTemplate para consumir catalogos-api.
     * Configurado con timeouts para evitar bloqueos.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Bean
    public Jaxb2Marshaller marshallerDetecno() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        // this package must match the package in the <generatePackage> specified in
        // pom.xml
        marshaller.setContextPath("com.sodimac.fiscal.api.detecno.wsdl");
        return marshaller;
    }

}
