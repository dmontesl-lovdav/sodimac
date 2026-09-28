package com.sodimac.fiscal.api.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sodimac.fiscal.api.repository.AddendumRepository;

/**
 * Resuelve si un usuario tiene habilitado un evento (botón) consultando la BD DIRECTO
 * (core_security + shared_catalogs), sin pasar por util-api.
 * <p>
 * util-api es inalcanzable desde los backends en UAT; por eso los catálogos ya se leen
 * directo de la BD. El candado de permisos (cancelar / reproceso / complemento) tenía la
 * misma dependencia (via {@code UtilApiSecurityClient.hasPermission/hasEvent}) → 403 en UAT.
 * Este resolver usa la conexión de BD propia de fiscal-api y replica el modelo por perfil
 * que arma el endpoint {@code user-details}. Fail-closed: cualquier error → false.
 */
@Component
public class PermissionResolver {

    private static final Logger log = LoggerFactory.getLogger(PermissionResolver.class);

    private final AddendumRepository addendumRepository;

    public PermissionResolver(AddendumRepository addendumRepository) {
        this.addendumRepository = addendumRepository;
    }

    /**
     * @param userKey clave del usuario del JWT (sub / preferred_username / email)
     * @param eventKey clave del evento (ej. EVT011 cancelar, EVT012 reproceso, EVT016 publicar)
     * @return true solo si el usuario tiene el evento activo vía su perfil
     */
    public boolean hasEvent(String userKey, String eventKey) {
        if (userKey == null || userKey.isBlank() || eventKey == null || eventKey.isBlank()) {
            return false;
        }
        try {
            return addendumRepository.existsEventForUser(userKey, eventKey);
        } catch (Exception e) {
            log.warn("Error resolviendo permiso userKey={} eventKey={}: {}", userKey, eventKey, e.getMessage());
            return false;
        }
    }
}
