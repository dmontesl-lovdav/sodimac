package com.sodimac.fiscal.api.security;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenInterceptor.class);

    private final String swaggerPath;
    private final String apiDocsPath;
    private final JwtParser jwtParser;
    private final String authHeader;
    private final boolean unsecured;

    public JwtTokenInterceptor(
            @Value("${fiscal.jwt.signing.publicKey}") String b64PublicKey,
            @Value("${fiscal.jwt.header:Authorization}") String authHeader,
            @Value("${springdoc.swagger-ui.path}") String swaggerPath,
            @Value("${springdoc.api-docs.path}") String apiDocsPath) {

        if (b64PublicKey != null && !b64PublicKey.isBlank()) {
            try {
                PublicKey publicKeySpec = CertificateFactory
                        .getInstance("X509")
                        .generateCertificates(new ByteArrayInputStream(Base64.getDecoder().decode(b64PublicKey)))
                        .iterator().next().getPublicKey();
                this.jwtParser = Jwts.parser()
                        .verifyWith(publicKeySpec)
                        .build();
                this.unsecured = false;
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "REQUIRED VALUE fiscal.jwt.signing.publicKey IS NOT A VALID RSA public key.");
            }
        } else {
            this.jwtParser = Jwts.parser().unsecured().build();
            this.unsecured = true;
        }
        this.authHeader = authHeader;
        this.swaggerPath = ".*" + swaggerPath + ".*";
        this.apiDocsPath = ".*" + apiDocsPath + ".*";
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {

        String headerNames = request.getHeaderNames() != null ?
            String.join(",", java.util.Collections.list(request.getHeaderNames())) : "none";
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

        authorization = authorization.replace("Bearer ", "");
        Claims payload;
        try {
            if (this.unsecured) {
                payload = this.jwtParser.parseUnsecuredClaims(authorization).getPayload();
            } else {
                Jws<Claims> claims = this.jwtParser.parseSignedClaims(authorization);
                payload = claims.getPayload();
            }
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        String name = payload.get("name", String.class);
        String email = payload.get("email", String.class);
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
        /* No-op */ }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex) {
        /* No-op */ }
}

