package com.sodimac.fiscal.api.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sodimac.fiscal.api.repository.AddendumRepository;
import com.sodimac.fiscal.api.security.UtilApiSecurityClient.SecurityAttributes;

/**
 * Resuelve los atributos de seguridad del usuario (STM-1403: vendors / tipos / grupos) consultando
 * la BD DIRECTO (core_security + shared_catalogs), sin pasar por util-api.
 * <p>
 * util-api es inalcanzable desde los backends en UAT; por eso los catálogos y el permiso de eventos
 * ya se resuelven directo de la BD. Este resolver hace lo mismo para el filtrado de resultados por
 * rol, replicando {@code getUserAttributesByKey} de util-api (merge de user_attribute + role_attribute,
 * todos activos). Mapea el type_key del catálogo de atributos: ATR001=vendors, ATR002=tipos,
 * ATR004=grupos. Cache en memoria 5 min, como el cliente HTTP original.
 */
@Component
public class UserAttributeResolver {

    private static final Logger log = LoggerFactory.getLogger(UserAttributeResolver.class);
    private static final long CACHE_TTL_MS = 5 * 60 * 1000L;

    private static final String ATTR_VENDORS = "ATR001";
    private static final String ATTR_TYPES = "ATR002";
    private static final String ATTR_GROUPS = "ATR004";

    private final AddendumRepository addendumRepository;
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    public UserAttributeResolver(AddendumRepository addendumRepository) {
        this.addendumRepository = addendumRepository;
    }

    /**
     * @param userKey clave del usuario del JWT (sub / preferred_username / email)
     * @return atributos activos del usuario; vacío si no tiene o ante cualquier error (fail-closed)
     */
    public SecurityAttributes resolve(String userKey) {
        if (userKey == null || userKey.isBlank()) {
            return SecurityAttributes.empty();
        }
        long now = System.currentTimeMillis();
        Cached cached = cache.get(userKey);
        if (cached != null && cached.expiresAt > now) {
            return cached.data;
        }
        try {
            List<Object[]> rows = addendumRepository.findUserSecurityAttributes(userKey);
            List<String> vendors = new ArrayList<>();
            List<String> types = new ArrayList<>();
            List<String> groups = new ArrayList<>();
            for (Object[] row : rows) {
                String typeKey = row[0] == null ? null : row[0].toString();
                String valueKey = row[1] == null ? null : row[1].toString();
                if (typeKey == null || valueKey == null || valueKey.isBlank()) {
                    continue;
                }
                switch (typeKey) {
                    case ATTR_VENDORS -> vendors.add(valueKey);
                    case ATTR_TYPES -> types.add(valueKey);
                    case ATTR_GROUPS -> groups.add(valueKey);
                    default -> { /* ignore */ }
                }
            }
            SecurityAttributes result = new SecurityAttributes(vendors, types, groups);
            cache.put(userKey, new Cached(result, now + CACHE_TTL_MS));
            return result;
        } catch (Exception e) {
            log.warn("Error resolviendo atributos de seguridad userKey={}: {}", userKey, e.getMessage());
            return SecurityAttributes.empty();
        }
    }

    /**
     * Números de proveedor para una lista de RFCs, directo de la BD (sin util-api). Usado por el
     * filtro cuando el request acota por RFC receptor. Normaliza los RFCs a mayúsculas.
     */
    public List<String> resolveSupplierNumbersByRfcs(List<String> rfcs) {
        if (rfcs == null || rfcs.isEmpty()) {
            return List.of();
        }
        try {
            List<String> upper = rfcs.stream()
                    .filter(r -> r != null && !r.isBlank())
                    .map(r -> r.trim().toUpperCase())
                    .toList();
            if (upper.isEmpty()) {
                return List.of();
            }
            return addendumRepository.findSupplierNumbersByRfcs(upper);
        } catch (Exception e) {
            log.warn("Error resolviendo supplier numbers por rfcs={}: {}", rfcs, e.getMessage());
            return List.of();
        }
    }

    public void clearCache() {
        cache.clear();
    }

    private record Cached(SecurityAttributes data, long expiresAt) {}
}
