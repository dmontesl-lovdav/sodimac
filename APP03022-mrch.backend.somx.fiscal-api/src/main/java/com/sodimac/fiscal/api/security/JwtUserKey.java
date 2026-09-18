package com.sodimac.fiscal.api.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

public final class JwtUserKey {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JwtUserKey() {
    }

    public static String resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String authorization = request.getHeader("Authorization");
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
            Map<String, Object> payload = OBJECT_MAPPER.readValue(
                    payloadJson,
                    new TypeReference<Map<String, Object>>() {
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
            return null;
        }
    }
}
