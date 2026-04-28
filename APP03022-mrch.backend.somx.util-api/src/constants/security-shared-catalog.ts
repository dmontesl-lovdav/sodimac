/**
 * Códigos de catalog_header en shared_catalogs (alinear con core_security_relaciones_shared_catalogs.sql).
 */
export const SECURITY_CATALOG_HEADER = {
    modulo: 'CatModulo',
    aplicativo: 'CatAplicativo',
    evento: 'CatEvento',
    perfil: 'CatPerfil',
    rol: 'CatRol',
    permiso: 'CatPermiso',
    tipoAtributo: 'CatAtributo',
    proveedor: 'CatProveedor',
    tipoProveedor: 'CatTipoProveedor',
} as const;
