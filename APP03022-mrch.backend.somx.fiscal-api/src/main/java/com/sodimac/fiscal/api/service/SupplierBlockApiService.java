package com.sodimac.fiscal.api.service;

/**
 * Cliente para consultar bloqueos de publicación de proveedores en util-api.
 *
 * Cubre dos niveles de bloqueo usados al registrar facturas:
 * - Bloqueo por TIPO de proveedor (catálogo CatBloqueoTipoProveedor) → BUS2028
 * - Bloqueo por PROVEEDOR individual (tabla supplier_block) → BUS2029
 *
 * Ambas validaciones son tolerantes a fallos: si util-api no responde, se asume
 * NO bloqueado para no frenar el registro por indisponibilidad del servicio.
 *
 * @author Sodimac Tech Team
 * @since QA junio-2026 (bloqueo de proveedores)
 */
public interface SupplierBlockApiService {

    /**
     * Indica si el TIPO del proveedor está bloqueado para publicar facturas (BUS2028).
     *
     * @param supplierNumber número de proveedor
     * @return true si el tipo está bloqueado; false si no, o si no se pudo determinar
     */
    boolean isSupplierTypeBlocked(String supplierNumber);

    /**
     * Indica si el proveedor individual está bloqueado para publicar facturas (BUS2029).
     *
     * @param supplierNumber número de proveedor
     * @return true si el proveedor está bloqueado; false si no, o si no se pudo determinar
     */
    boolean isSupplierBlocked(String supplierNumber);
}
