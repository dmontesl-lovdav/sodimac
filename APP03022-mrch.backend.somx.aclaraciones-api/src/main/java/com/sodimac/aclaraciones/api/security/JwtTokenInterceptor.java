// src/main/java/com/sodimac/aclaraciones/api/security/JwtTokenInterceptor.java
package com.sodimac.aclaraciones.api.security;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenInterceptor.class);

    private final boolean securityEnabled;
    private final String swaggerPath;
    private final String apiDocsPath;
    private final String authHeader;

    // Expected realm roles
    private final String resolverRole; // ppsomx-resolver
    private final String vendorRole; // ppsomx-vendor
    private final String adminRole; // ppsomx-admin
    private final boolean containsMatch;
    private final String clientId; // fbc-aclaraciones

    public JwtTokenInterceptor(
            @Value("${aclaraciones.jwt.enabled:false}") boolean securityEnabled,
            @Value("${aclaraciones.jwt.header:Authorization}") String authHeader,
            @Value("${springdoc.swagger-ui.path:/swagger-ui}") String swaggerPath,
            @Value("${springdoc.api-docs.path:/api-docs}") String apiDocsPath,
            // Defaults aligned to realm
            @Value("${aclaraciones.jwt.role.resolver:ppsomx-resolver}") String resolverRole,
            @Value("${aclaraciones.jwt.role.vendor:ppsomx-vendor}") String vendorRole,
            @Value("${aclaraciones.jwt.role.admin:ppsomx-admin}") String adminRole,
            @Value("${aclaraciones.jwt.contains-match:true}") boolean containsMatch,
            @Value("${aclaraciones.jwt.client-id:fbc-aclaraciones}") String clientId) {

        this.securityEnabled = securityEnabled;
        this.authHeader = authHeader;
        this.swaggerPath = ".*" + swaggerPath + ".*";
        this.apiDocsPath = ".*" + apiDocsPath + ".*";
        this.resolverRole = resolverRole;
        this.vendorRole = vendorRole;
        this.adminRole = adminRole;
        this.containsMatch = containsMatch;
        this.clientId = clientId;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (!securityEnabled) {
            request.setAttribute("session", new Session("dev", "dev@local", List.of("DEV")));
            return true;
        }

        if (request.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.name()))
            return true;

        String uri = request.getRequestURI();
        if (uri.matches(this.swaggerPath)
                || uri.matches(".*/swagger-ui/.*")
                || uri.matches(".*/swagger-ui\\.html.*")
                || uri.matches(this.apiDocsPath)
                || uri.matches(".*/api-docs(\\.yaml|\\.json)?")
                || "/error".equals(uri)
                || "/favicon.ico".equals(uri)) {
            return true;
        }

        String authorization = request.getHeader(this.authHeader);
        if (authorization == null || authorization.isEmpty() || !authorization.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        String token = authorization.substring("Bearer ".length()).trim();

        DecodedJWT jwt;
        try {
            jwt = JWT.decode(token);
        } catch (Exception e) {
            log.debug("JWT decode failed: {}", e.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"invalid_token\"}");
            return false;
        }

        String name = optStr(jwt.getClaim("name"));
        String email = optStr(jwt.getClaim("email"));

        // Build effective groups/roles from groups + realm_access +
        // resource_access[clientId]
        Set<String> eff = new LinkedHashSet<>();

        List<String> groupsClaim = safeList(jwt.getClaim("groups"));
        if (groupsClaim != null)
            eff.addAll(groupsClaim);

        Map<String, Object> realmAccess = jwt.getClaim("realm_access").asMap();
        if (realmAccess != null) {
            Object rolesObj = realmAccess.get("roles");
            if (rolesObj instanceof Collection<?> coll) {
                for (Object r : coll)
                    if (r != null)
                        eff.add(String.valueOf(r));
            }
        }

        Map<String, Object> resourceAccess = jwt.getClaim("resource_access").asMap();
        if (resourceAccess != null) {
            Object clientObj = resourceAccess.get(clientId);
            if (clientObj instanceof Map<?, ?> rc) {
                Object rolesObj = rc.get("roles");
                if (rolesObj instanceof Collection<?> roles) {
                    for (Object r : roles) {
                        if (r == null)
                            continue;
                        String role = String.valueOf(r); // ppsomx-vendor
                        eff.add(role);
                        eff.add(clientId + ":" + role); // fbc-aclaraciones:ppsomx-vendor
                        eff.add(clientId + "/roles/" + role); // fbc-aclaraciones/roles/ppsomx-vendor
                    }
                }
            }
        }

        List<String> groups = new ArrayList<>(eff);

        boolean isAdmin = matchesAny(groups, adminRole);
        boolean isResolver = matchesAny(groups, resolverRole) || isAdmin; // admin is superuser
        boolean isVendor = matchesAny(groups, vendorRole);

        if (!isResolver && !isVendor) {
            groups.add(vendorRole); // keep previous behavior: degrade to vendor
            log.warn("No resolver/vendor match → adding '{}' by default. Effective groups={}", vendorRole, groups);
        }

        log.info("GroupValidator → expected(resolver='{}', vendor='{}', admin='{}'), groups={}",
                resolverRole, vendorRole, adminRole, String.join(",", groups));

        request.setAttribute("session", new Session(name, email, groups));
        return true;
    }

    private boolean matchesAny(List<String> groups, String expectedRaw) {
        if (groups == null || groups.isEmpty() || expectedRaw == null || expectedRaw.isBlank())
            return false;

        final String expected = normalizeRole(expectedRaw);
        final String withColon = (clientId + ":" + expected).toLowerCase();
        final String withPath = (clientId + "/roles/" + expected).toLowerCase();

        for (String g : groups) {
            if (g == null)
                continue;
            final String gi = g.toLowerCase();
            final String gn = normalizeRole(gi);

            if (gn.equals(expected))
                return true; // ppsomx-vendor
            if (gi.equals(withColon))
                return true; // fbc-aclaraciones:ppsomx-vendor
            if (gi.equals(withPath))
                return true; // fbc-aclaraciones/roles/ppsomx-vendor
            if (containsMatch && (gi.contains(expected) || gn.contains(expected)))
                return true;
        }
        return false;
    }

    private String normalizeRole(String v) {
        if (v == null)
            return "";
        String s = v.trim();
        while (s.startsWith("/"))
            s = s.substring(1);
        String lower = s.toLowerCase();
        String cid = clientId == null ? "" : clientId.toLowerCase();

        String colonPrefix = cid + ":";
        String pathPrefix = cid + "/roles/";

        if (lower.startsWith(colonPrefix))
            return lower.substring(colonPrefix.length());
        if (lower.startsWith(pathPrefix))
            return lower.substring(pathPrefix.length());
        return lower;
    }

    private List<String> safeList(Claim claim) {
        try {
            return claim.asList(String.class);
        } catch (Exception e) {
            return null;
        }
    }

    private String optStr(Claim c) {
        try {
            return c.asString();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void postHandle(HttpServletRequest req, HttpServletResponse res, Object h, ModelAndView m) {
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object h, Exception ex) {
    }
}
