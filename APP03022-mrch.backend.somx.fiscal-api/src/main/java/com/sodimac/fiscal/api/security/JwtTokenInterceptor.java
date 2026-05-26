package com.sodimac.fiscal.api.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenInterceptor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String swaggerPath;
    private final String apiDocsPath;
    private final String authHeader;

    public JwtTokenInterceptor(
            @Value("${fiscal.jwt.header:Authorization}") String authHeader,
            @Value("${springdoc.swagger-ui.path}") String swaggerPath,
            @Value("${springdoc.api-docs.path}") String apiDocsPath) {

        this.authHeader = authHeader;
        this.swaggerPath = ".*" + swaggerPath + ".*";
        this.apiDocsPath = ".*" + apiDocsPath + ".*";
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {

        String headerNames = request.getHeaderNames() != null
                ? String.join(",", java.util.Collections.list(request.getHeaderNames()))
                : "none";

        log.debug("ACCEPTING REQUEST ({}) WITH HEADERS: {}", request.getMethod(), headerNames);

        if (request.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.toString())) {
            return true;
        }

        if (request.getRequestURI().matches(this.swaggerPath)
                || request.getRequestURI().matches(this.apiDocsPath)) {
            return true;
        }

        String authorization = request.getHeader(this.authHeader);

        if (authorization == null || authorization.isEmpty() || !authorization.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        String token = authorization.replace("Bearer ", "");

        Map<String, Object> payload;

        try {
            String[] chunks = token.split("\\.");

            if (chunks.length < 2) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return false;
            }

            String payloadJson = new String(
                    Base64.getUrlDecoder().decode(chunks[1]),
                    StandardCharsets.UTF_8);

            payload = objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {
            });

        } catch (Exception e) {
            log.warn("Token inválido o no legible", e);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        String name = payload.get("name") instanceof String ? (String) payload.get("name") : null;
        String email = payload.get("email") instanceof String ? (String) payload.get("email") : null;

        List<String> groups = null;
        Object groupsObj = payload.get("groups");

        if (groupsObj instanceof List<?>) {
            groups = ((List<?>) groupsObj).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }

        log.debug("🙋 Usuario: {} | Email: {}", name, email);

        request.setAttribute("session", new Session(name, email, groups));

        return true;
    }

    /* ---------- Métodos vacíos del interceptor ---------- */
    @Override
    public void postHandle(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            ModelAndView modelAndView) {
        /* No-op */
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex) {
        /* No-op */
    }
}