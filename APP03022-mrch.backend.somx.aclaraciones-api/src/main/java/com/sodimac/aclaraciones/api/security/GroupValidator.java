// src/main/java/com/sodimac/aclaraciones/api/security/GroupValidator.java
package com.sodimac.aclaraciones.api.security;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.sodimac.aclaraciones.api.exception.GenericException;

import java.util.List;

@Component
@Aspect
public class GroupValidator {

    private static final Logger log = LoggerFactory.getLogger(GroupValidator.class);

    // Expected realm roles
    private final String resolverRole; // ppsomx-resolver
    private final String vendorRole; // ppsomx-vendor
    private final String adminRole; // ppsomx-admin
    private final boolean containsMatch;
    private final String clientId; // fbc-aclaraciones

    public GroupValidator(
            // Defaults aligned to realm
            @Value("${aclaraciones.jwt.role.resolver:ppsomx-resolver}") String resolverRole,
            @Value("${aclaraciones.jwt.role.vendor:ppsomx-vendor}") String vendorRole,
            @Value("${aclaraciones.jwt.role.admin:ppsomx-admin}") String adminRole,
            @Value("${aclaraciones.jwt.contains-match:true}") boolean containsMatch,
            @Value("${aclaraciones.jwt.client-id:fbc-aclaraciones}") String clientId) {
        this.resolverRole = resolverRole;
        this.vendorRole = vendorRole;
        this.adminRole = adminRole;
        this.containsMatch = containsMatch;
        this.clientId = clientId;
    }

    @Before("@annotation(com.sodimac.aclaraciones.api.security.RequireRole)")
    public void validate(JoinPoint jp) throws GenericException {
        Session session = null;
        for (Object arg : jp.getArgs()) {
            if (arg instanceof Session s) {
                session = s;
                break;
            }
        }
        if (session == null) {
            throw new GenericException("FORBIDDEN", HttpStatus.FORBIDDEN.value());
        }

        List<String> groups = session.getGroups();
        log.info("GroupValidator → expected(resolver='{}', vendor='{}', admin='{}'), groups={}",
                resolverRole, vendorRole, adminRole, StringUtils.join(groups, ","));

        boolean isAdmin = matches(groups, adminRole);
        boolean isResolver = matches(groups, resolverRole) || isAdmin; // admin == superuser
        boolean isVendor = matches(groups, vendorRole);

        if (!isResolver && !isVendor) {
            // Keep previous behavior: allow but mark as non-operator
            log.warn("No resolver/vendor match → degrade to VENDOR (allow request).");
            session.setOperator(false);
            return;
        }

        session.setOperator(isResolver);
    }

    private boolean matches(List<String> groups, String expectedRaw) {
        if (groups == null || groups.isEmpty() || StringUtils.isBlank(expectedRaw))
            return false;

        final String expected = normalizeRole(expectedRaw);
        final String withColon = (clientId + ":" + expected).toLowerCase();
        final String withPath = (clientId + "/roles/" + expected).toLowerCase();

        for (String g : groups) {
            if (StringUtils.isBlank(g))
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

    // Remove client prefixes that sometimes appear in Keycloak
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
}
