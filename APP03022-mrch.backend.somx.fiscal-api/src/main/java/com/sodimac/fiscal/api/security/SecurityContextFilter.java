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
            chain.doFilter(request, response);
            return;
        }

        String sub = extractSub(request);
        if (sub == null) {
            chain.doFilter(request, response);
            return;
        }

        UtilApiSecurityClient.SecurityAttributes attrs = utilApi.getAttributesBySub(sub);
        log.debug("STM-1403 sub={} vendors={} types={} groups={}", sub, attrs.vendors(), attrs.types(), attrs.groups());

        java.util.List<String> vendors = new java.util.ArrayList<>(attrs.vendors());
        java.util.List<String> types   = new java.util.ArrayList<>(attrs.types());
        java.util.List<String> groups  = new java.util.ArrayList<>(attrs.groups());

        if (vendors.isEmpty() && types.isEmpty() && groups.isEmpty()) {
            vendors = parseHeaderList(request.getHeader("x-user-vendors"));
            types   = parseHeaderList(request.getHeader("x-user-types"));
            groups  = parseHeaderList(request.getHeader("x-user-groups"));
        }

        java.util.List<String> rfcs = extractRfcs(request);
        if (!rfcs.isEmpty()) {
            java.util.List<String> rfcSuppliers = utilApi.getSupplierNumbersByRfcs(rfcs);
            if (!rfcSuppliers.isEmpty()) {
                if (vendors.isEmpty()) {
                    vendors = new java.util.ArrayList<>(rfcSuppliers);
                } else {
                    java.util.Set<String> allowed = new java.util.HashSet<>(rfcSuppliers);
                    vendors = vendors.stream().filter(allowed::contains).collect(java.util.stream.Collectors.toList());
                }
            }
        }

        Map<String, String> overrides = new HashMap<>();
        overrides.put("x-user-vendors", joinOrEmpty(vendors));
        overrides.put("x-user-types",   joinOrEmpty(types));
        overrides.put("x-user-groups",  joinOrEmpty(groups));

        HttpServletRequest wrapped = new HeaderOverrideRequest(request, overrides);
        chain.doFilter(wrapped, response);
    }

    private String extractSub(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        String token = auth.substring("Bearer ".length()).trim();
        if (token.isEmpty()) return null;

        try {
            String[] parts = token.split("\\.", -1);
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

    private java.util.List<String> extractRfcs(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return java.util.Collections.emptyList();
        String token = auth.substring("Bearer ".length()).trim();
        if (token.isEmpty()) return java.util.Collections.emptyList();

        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3) return java.util.Collections.emptyList();
            JsonNode payload = MAPPER.readTree(Base64.getUrlDecoder().decode(parts[1]));

            JsonNode list = null;
            for (String field : new String[] { "vendors-taxs", "vendorsTaxs", "taxIds", "tax_ids" }) {
                if (payload.has(field)) { list = payload.get(field); break; }
            }
            if (list == null || !list.isArray()) return java.util.Collections.emptyList();

            java.util.LinkedHashSet<String> rfcs = new java.util.LinkedHashSet<>();
            for (JsonNode v : list) {
                String raw = v.path("taxId").asText(null);
                if (raw == null) raw = v.path("rfc").asText(null);
                if (raw == null) raw = v.path("tax_id").asText(null);
                if (raw != null && !raw.isBlank()) rfcs.add(raw.trim());
            }
            return new java.util.ArrayList<>(rfcs);
        } catch (Exception e) {
            log.debug("Failed to extract RFCs from JWT payload: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    private static String joinOrEmpty(java.util.List<String> values) {
        if (values == null || values.isEmpty()) return "";
        return String.join(",", values);
    }

    private static java.util.List<String> parseHeaderList(String value) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (value == null || value.isBlank()) return out;
        for (String part : value.split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
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
