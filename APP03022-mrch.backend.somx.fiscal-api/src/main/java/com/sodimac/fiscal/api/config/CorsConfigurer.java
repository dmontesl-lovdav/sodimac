/*───────────────────────────────────────────────────────────
 * src/main/java/com/sodimac/financiero/api/config/CorsConfigurer.java
 *──────────────────────────────────────────────────────────*/
package com.sodimac.fiscal.api.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración global de CORS.
 *
 * <p>
 * – Se leen dos propiedades:
 * · <b>cors.allowed-origins</b> (array coma-separado, admite comodines)
 * · <b>cors.allowed-mappings</b> (lista de paths que exponen CORS)
 *
 * – Por defecto permite todo: origins="*", mappings="/**".
 * Como usamos <i>allowedOriginPatterns</i>, es compatible con
 * <i>allowCredentials(true)</i> incluso cuando hay comodines.
 * </p>
 */
@Component
public class CorsConfigurer implements WebMvcConfigurer {

    private final String[] origins;
    private final List<String> mappings;

    public CorsConfigurer(
            @Value("${cors.allowed-origins:*}") String[] origins,
            @Value("${cors.allowed-mappings:/**}") List<String> mappings) {

        this.origins = origins;
        this.mappings = mappings;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        mappings.forEach(mapping -> registry.addMapping(mapping)
                /* ───────────── orígenes ───────────── */
                .allowedOriginPatterns(origins)
                /* ───────────── métodos ────────────── */
                .allowedMethods("GET", "POST", "PUT",
                        "PATCH", "DELETE", "OPTIONS")
                /* ──────────── cabeceras ───────────── */
                .allowedHeaders("*")
                /* ───────────── cookies ────────────── */
                .allowCredentials(true)
                /* ────────── cache pre-flight ──────── */
                .maxAge(3600));
    }
}

