package com.sodimac.fiscal.api.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(PermissionInterceptor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final UtilApiSecurityClient securityClient;
    private final String authHeader;

    public PermissionInterceptor(
            UtilApiSecurityClient securityClient,
            @Value("${fiscal.jwt.header:Authorization}") String authHeader) {
        this.securityClient = securityClient;
        this.authHeader = authHeader;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {

        if (request.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.toString())) {
            return true;
        }

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequirePermission annotation = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }
        if (annotation == null) {
            return true;
        }

        String userKey = resolveUserKey(request);
        if (userKey == null) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }

        boolean allowed = securityClient.hasPermission(userKey, annotation.value());
        if (!allowed) {
            log.warn("Acceso denegado por permiso. userKey={} eventKey={}", userKey, annotation.value());
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }

        return true;
    }

    private String resolveUserKey(HttpServletRequest request) {
        String authorization = request.getHeader(this.authHeader);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        try {
            String[] chunks = authorization.replace("Bearer ", "").split("\\.");
            if (chunks.length < 2) {
                return null;
            }
            String payloadJson = new String(
                    Base64.getUrlDecoder().decode(chunks[1]),
                    StandardCharsets.UTF_8);
            Map<String, Object> payload = objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {
            });

            Object sub = payload.get("sub");
            Object preferred = payload.get("preferred_username");
            Object email = payload.get("email");

            if (sub instanceof String s && !s.isBlank()) {
                return s;
            }
            if (preferred instanceof String p && !p.isBlank()) {
                return p;
            }
            if (email instanceof String e && !e.isBlank()) {
                return e;
            }
            return null;
        } catch (Exception e) {
            log.warn("No se pudo resolver el usuario del token: {}", e.getMessage());
            return null;
        }
    }
}
