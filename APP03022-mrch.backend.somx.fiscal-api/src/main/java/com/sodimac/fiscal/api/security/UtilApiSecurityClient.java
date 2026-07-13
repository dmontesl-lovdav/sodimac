package com.sodimac.fiscal.api.security;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    public void clearCache() {
        cache.clear();
    }

    public record SecurityAttributes(List<String> vendors, List<String> types, List<String> groups) {
        public static SecurityAttributes empty() {
            return new SecurityAttributes(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }
    }

    private record CachedContext(SecurityAttributes data, long expiresAt) {}
}
