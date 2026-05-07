package com.sodimac.fiscal.api.security;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * STM-1403: Decodifica JWT del request, consulta util-api y sobrescribe headers
 * x-user-vendors / x-user-types / x-user-groups con los valores reales del usuario.
 * <p>
 * Cliente NO puede falsificar estos headers — el filter siempre los reemplaza
 * con valores derivados del sub del JWT (validado por GCP gateway antes de
 * llegar al backend en uat/prod, decodificado sin verificar en dev).
 * <p>
 * Si security.enabled=false, no envuelve el request → modo dev permite tests
 * directos enviando headers manualmente.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityContextFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SecurityContextFilter.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final UtilApiSecurityClient utilApi;
    private final boolean securityEnabled;

    public SecurityContextFilter(
            UtilApiSecurityClient utilApi,
            @Value("${security.enabled:true}") boolean securityEnabled) {
        this.utilApi = utilApi;
        this.securityEnabled = securityEnabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (!securityEnabled) {
            // Dev: deja headers del cliente intactos para tests directos.
            chain.doFilter(request, response);
            return;
        }

        String sub = extractSub(request);
        if (sub == null) {
            // Sin JWT valido: deja request sin headers de seguridad.
            // JwtTokenInterceptor (corre despues) responde 401 si auth strict.
            chain.doFilter(request, response);
            return;
        }

        UtilApiSecurityClient.SecurityAttributes attrs = utilApi.getAttributesBySub(sub);

        Map<String, String> overrides = new HashMap<>();
        overrides.put("x-user-vendors", joinOrEmpty(attrs.vendors()));
        overrides.put("x-user-types",   joinOrEmpty(attrs.types()));
        overrides.put("x-user-groups",  joinOrEmpty(attrs.groups()));

        HttpServletRequest wrapped = new HeaderOverrideRequest(request, overrides);
        chain.doFilter(wrapped, response);
    }

    private String extractSub(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        String token = auth.substring("Bearer ".length()).trim();
        if (token.isEmpty()) return null;

        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode payload = MAPPER.readTree(payloadBytes);
            String sub = payload.path("sub").asText(null);
            if (sub != null && !sub.isBlank()) return sub;
            return payload.path("preferred_username").asText(null);
        } catch (Exception e) {
            log.debug("Failed to decode JWT payload: {}", e.getMessage());
            return null;
        }
    }

    private static String joinOrEmpty(java.util.List<String> values) {
        if (values == null || values.isEmpty()) return "";
        return String.join(",", values);
    }

    /**
     * Wrapper que sobrescribe headers seleccionados con valores fijos.
     * Cliente no puede leer/inyectar otros valores en estos headers.
     */
    private static final class HeaderOverrideRequest extends HttpServletRequestWrapper {
        private final Map<String, String> overrides;

        HeaderOverrideRequest(HttpServletRequest request, Map<String, String> overrides) {
            super(request);
            // Normaliza keys a lowercase para lookup case-insensitive.
            Map<String, String> norm = new HashMap<>();
            overrides.forEach((k, v) -> norm.put(k.toLowerCase(), v));
            this.overrides = norm;
        }

        @Override
        public String getHeader(String name) {
            String key = name == null ? "" : name.toLowerCase();
            if (overrides.containsKey(key)) return overrides.get(key);
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            String key = name == null ? "" : name.toLowerCase();
            if (overrides.containsKey(key)) {
                String value = overrides.get(key);
                return Collections.enumeration(value == null ? Collections.emptyList() : java.util.List.of(value));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            java.util.Set<String> names = new java.util.LinkedHashSet<>();
            Enumeration<String> orig = super.getHeaderNames();
            while (orig != null && orig.hasMoreElements()) {
                names.add(orig.nextElement());
            }
            names.addAll(overrides.keySet());
            return Collections.enumeration(names);
        }
    }
}
