package com.sodimac.fiscal.api.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;

/**
 * Filtro que extrae el idioma del header Accept-Language o X-Language
 * y lo almacena en el LocaleContextHolder de Spring.
 *
 * El idioma se usa posteriormente por MessageCatalogService para
 * obtener mensajes en el idioma correcto desde catalogos-api.
 *
 * Headers soportados (en orden de prioridad):
 * 1. X-Language: Valor directo (es, en, pt)
 * 2. Accept-Language: Formato estándar HTTP (es-MX, en-US, etc.)
 *
 * Si no se especifica idioma, usa español (es) como default.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@Component
@Order(1)
@Slf4j
public class LocaleFilter implements Filter {

    private static final String HEADER_X_LANGUAGE = "X-Language";
    private static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    private static final Locale DEFAULT_LOCALE = new Locale("es");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        Locale locale = extractLocale(httpRequest);

        try {
            LocaleContextHolder.setLocale(locale);
            log.debug("Locale establecido: {} para request: {}",
                    locale.getLanguage(), httpRequest.getRequestURI());

            chain.doFilter(request, response);
        } finally {
            LocaleContextHolder.resetLocaleContext();
        }
    }

    /**
     * Extrae el locale del request HTTP.
     *
     * @param request Request HTTP
     * @return Locale extraído o default (es)
     */
    private Locale extractLocale(HttpServletRequest request) {
        // Prioridad 1: Header X-Language (valor directo: es, en, pt)
        String xLanguage = request.getHeader(HEADER_X_LANGUAGE);
        if (xLanguage != null && !xLanguage.trim().isEmpty()) {
            String lang = xLanguage.trim().toLowerCase();
            log.debug("Idioma detectado de X-Language: {}", lang);
            return new Locale(lang);
        }

        // Prioridad 2: Header Accept-Language (formato estándar)
        String acceptLanguage = request.getHeader(HEADER_ACCEPT_LANGUAGE);
        if (acceptLanguage != null && !acceptLanguage.trim().isEmpty()) {
            // Tomar solo el primer idioma (antes de la coma)
            String firstLang = acceptLanguage.split(",")[0].trim();
            // Extraer solo el código de idioma (antes del guión: es-MX -> es)
            String langCode = firstLang.split("-")[0].toLowerCase();
            log.debug("Idioma detectado de Accept-Language: {} -> {}", acceptLanguage, langCode);
            return new Locale(langCode);
        }

        // Default: español
        log.debug("No se detectó idioma, usando default: es");
        return DEFAULT_LOCALE;
    }
}
