package com.sodimac.fiscal.api.security;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * STM-1403: Cliente para consultar atributos de seguridad del usuario en util-api.
 * Cache en memoria con TTL 5 min para evitar hammering del util-api.
 */
@Component
public class UtilApiSecurityClient {

    private static final Logger log = LoggerFactory.getLogger(UtilApiSecurityClient.class);
    private static final long CACHE_TTL_MS = 5 * 60 * 1000L;

    private final String utilApiUrl;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final Map<String, CachedContext> cache = new ConcurrentHashMap<>();
    private final Map<String, CachedEvents> eventCache = new ConcurrentHashMap<>();

    public UtilApiSecurityClient(@Value("${utils.api.url:http://localhost:3712}") String utilApiUrl) {
        this.utilApiUrl = utilApiUrl;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        this.mapper = new ObjectMapper();
    }

    public SecurityAttributes getAttributesBySub(String sub) {
        if (sub == null || sub.isBlank()) return SecurityAttributes.empty();

        long now = System.currentTimeMillis();
        CachedContext cached = cache.get(sub);
        if (cached != null && cached.expiresAt > now) {
            return cached.data;
        }

        try {
            URI uri = URI.create(utilApiUrl + "/api/security/user-attributes-by-key/" + sub);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                log.warn("util-api returned {} for sub={}", res.statusCode(), sub);
                return SecurityAttributes.empty();
            }

            JsonNode root = mapper.readTree(res.body());
            JsonNode attrs = root.path("data").path("attributes");

            List<String> vendors = new ArrayList<>();
            List<String> types = new ArrayList<>();
            List<String> groups = new ArrayList<>();

            if (attrs.isArray()) {
                for (JsonNode a : attrs) {
                    String typeKey = a.path("typeKey").asText(null);
                    String valueKey = a.path("valueKey").asText(null);
                    if (typeKey == null || valueKey == null) continue;
                    switch (typeKey) {
                        case "ATR001" -> vendors.add(valueKey);
                        case "ATR002" -> types.add(valueKey);
                        case "ATR004" -> groups.add(valueKey);
                        default -> { /* ignore */ }
                    }
                }
            }

            SecurityAttributes result = new SecurityAttributes(vendors, types, groups);
            cache.put(sub, new CachedContext(result, now + CACHE_TTL_MS));
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("util-api fetch interrupted for sub={}: {}", sub, e.getMessage());
            return SecurityAttributes.empty();
        } catch (Exception e) {
            log.warn("util-api fetch error for sub={}: {}", sub, e.getMessage());
            return SecurityAttributes.empty();
        }
    }

    public boolean hasPermission(String userKey, String eventKey) {
        if (userKey == null || userKey.isBlank() || eventKey == null || eventKey.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(utilApiUrl + "/api/security/has-permission/"
                    + URLEncoder.encode(userKey, StandardCharsets.UTF_8) + "/"
                    + URLEncoder.encode(eventKey, StandardCharsets.UTF_8));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                log.warn("util-api has-permission returned {} for userKey={} eventKey={}", res.statusCode(), userKey, eventKey);
                return false;
            }
            JsonNode root = mapper.readTree(res.body());
            return root.path("data").path("allowed").asBoolean(false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("util-api has-permission interrupted for userKey={}: {}", userKey, e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("util-api has-permission error for userKey={}: {}", userKey, e.getMessage());
            return false;
        }
    }

    /**
     * ¿El usuario tiene habilitado el evento (botón) indicado? Consulta el MISMO endpoint que
     * usa el front (`/api/security/user-details/{userKey}`), que resuelve el modelo por perfil
     * (module_process/profile_module) ya poblado — a diferencia de {@link #hasPermission} que va
     * contra el modelo por rol (event_permission/role_permission), sin seed. El evento se busca
     * en cualquier aplicativo del contexto (equivalente a hasEventInAnyApp del front). Fail-closed.
     */
    public boolean hasEvent(String userKey, String eventKey) {
        if (userKey == null || userKey.isBlank() || eventKey == null || eventKey.isBlank()) {
            return false;
        }
        return fetchAllowedEvents(userKey).contains(eventKey);
    }

    private java.util.Set<String> fetchAllowedEvents(String userKey) {
        long now = System.currentTimeMillis();
        CachedEvents cached = eventCache.get(userKey);
        if (cached != null && cached.expiresAt > now) {
            return cached.events;
        }
        java.util.Set<String> events = new java.util.HashSet<>();
        try {
            URI uri = URI.create(utilApiUrl + "/api/security/user-details/"
                    + URLEncoder.encode(userKey, StandardCharsets.UTF_8));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                log.warn("util-api user-details returned {} for userKey={}", res.statusCode(), userKey);
                return events;
            }
            JsonNode root = mapper.readTree(res.body());
            JsonNode apps = root.path("data").path("apps");
            if (apps.isArray()) {
                for (JsonNode app : apps) {
                    JsonNode evs = app.path("events");
                    if (evs.isArray()) {
                        for (JsonNode ev : evs) {
                            String key = ev.path("key").asText(null);
                            if (key != null && !key.isBlank()) {
                                events.add(key);
                            }
                        }
                    }
                }
            }
            eventCache.put(userKey, new CachedEvents(events, now + CACHE_TTL_MS));
            return events;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("util-api user-details interrupted for userKey={}: {}", userKey, e.getMessage());
            return events;
        } catch (Exception e) {
            log.warn("util-api user-details error for userKey={}: {}", userKey, e.getMessage());
            return events;
        }
    }

    public void clearCache() {
        cache.clear();
        eventCache.clear();
    }

    public record SecurityAttributes(List<String> vendors, List<String> types, List<String> groups) {
        public static SecurityAttributes empty() {
            return new SecurityAttributes(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }
    }

    private record CachedContext(SecurityAttributes data, long expiresAt) {}

    private record CachedEvents(java.util.Set<String> events, long expiresAt) {}
}
