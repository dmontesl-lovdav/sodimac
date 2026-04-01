-- ============================================================
-- Query directa del SP uspGetExclusiones
-- Parametros: @pIdUsuario=2, @pFolio='', @pComentario='',
--             @pIdPeriodo=0, @pIdTipoExclusion=0,
--             @pNumProveedor='', @pOrdenCompra=''
-- Perfil usuario 2: 1 (Financiero) → RAMA 1
-- BD: SODIMAC_REBATES_PROD
-- Fecha: 2026-04-01
-- ============================================================

USE [SODIMAC_REBATES_PROD]

-- Query que ejecuta el SP (RAMA 1: perfil NOT IN 3,4,11)
-- Con parametros sustituidos tal cual llegan
SELECT
e.IdExclusion, e.IdCatTipoRebate, IdCatTipoExclusion, IdCatEstatusExclusion, IdCatPeriodo,
Folio, Comentario, IdUsuarioSolicitud, IdUsuarioAutorizacion, FechaHoraSolicitud, FechaHoraAutorizacion,
e.Activo, Contabilizado, Evidencia, Imagen
FROM [dbo].[Exclusion] e
LEFT JOIN [dbo].[ExclusionCarga] ec ON e.IdExclusion = ec.IdExclusion
LEFT JOIN [dbo].[ExclusionCargaDet] ecd ON ec.IdExclusionCarga = ecd.IdExclusionCarga
INNER JOIN vw_perfilRebate Reb ON e.IdCatTipoRebate = Reb.IdCatTipoRebate AND Reb.idCatPerfil = 1 AND Reb.IdUsuario = 2
WHERE e.Activo = 1
AND (ecd.Activo = 1 OR ecd.Activo IS NULL)
AND (ec.Activo = 1 OR ec.Activo IS NULL)
-- @pFolio = '' → '' nunca es NULL, CASE retorna '' → ('' = e.Folio) OR e.Folio LIKE '%%'
-- El LIKE '%%' es TRUE para todo → NO FILTRA
AND ((('' = e.Folio) OR e.Folio LIKE '%%'))
-- @pComentario = '' → mismo problema, NO FILTRA
AND ((('' = e.Comentario) OR e.Comentario LIKE '%%'))
-- @pIdPeriodo = 0 → 0 nunca es NULL, CASE retorna 0 → IdCatPeriodo = 0
-- Si no existen registros con IdCatPeriodo=0 → NO RETORNA NADA
AND (0 = e.IdCatPeriodo)
-- @pIdTipoExclusion = 0 → mismo problema → IdCatTipoExclusion = 0
AND (0 = e.IdCatTipoExclusion)
-- @pNumProveedor = '' → '' no es NULL → ecd.NumProveedor = ''
AND (ecd.NumProveedor = '')
-- @pOrdenCompra = '' → '' no es NULL → ecd.OrdenCompra = ''
AND (ecd.OrdenCompra = '')
GROUP BY e.IdExclusion, e.IdCatTipoRebate, IdCatTipoExclusion, IdCatEstatusExclusion, IdCatPeriodo,
Folio, Comentario, IdUsuarioSolicitud, IdUsuarioAutorizacion, FechaHoraSolicitud, FechaHoraAutorizacion,
e.Activo, Contabilizado, Evidencia, Imagen
ORDER BY e.IdExclusion DESC;


-- ============================================================
-- DIAGNOSTICO: Los filtros con '' y 0 causan problemas:
--
-- 1. IdCatPeriodo = 0        → probablemente no existe periodo 0
-- 2. IdCatTipoExclusion = 0  → probablemente no existe tipo 0
-- 3. NumProveedor = ''       → probablemente no hay proveedores con string vacio
-- 4. OrdenCompra = ''        → probablemente no hay OC con string vacio
-- 5. Folio LIKE '%%'         → trae todo (no filtra)
-- 6. Comentario LIKE '%%'    → trae todo (no filtra)
--
-- Resultado: la query probablemente NO retorna nada (por los = 0 y = '')
-- pero el motor tiene que hacer full scan + JOINs + GROUP BY para descubrirlo
-- → TIMEOUT
-- ============================================================


-- ============================================================
-- QUERY CORREGIDA: convertir '' a NULL y 0 a NULL
-- Asi los filtros opcionales se desactivan correctamente
-- ============================================================
SELECT
e.IdExclusion, e.IdCatTipoRebate, IdCatTipoExclusion, IdCatEstatusExclusion, IdCatPeriodo,
Folio, Comentario, IdUsuarioSolicitud, IdUsuarioAutorizacion, FechaHoraSolicitud, FechaHoraAutorizacion,
e.Activo, Contabilizado, Evidencia, Imagen
FROM [dbo].[Exclusion] e
LEFT JOIN [dbo].[ExclusionCarga] ec ON e.IdExclusion = ec.IdExclusion
LEFT JOIN [dbo].[ExclusionCargaDet] ecd ON ec.IdExclusionCarga = ecd.IdExclusionCarga
INNER JOIN vw_perfilRebate Reb ON e.IdCatTipoRebate = Reb.IdCatTipoRebate AND Reb.idCatPerfil = 1 AND Reb.IdUsuario = 2
WHERE e.Activo = 1
AND (ecd.Activo = 1 OR ecd.Activo IS NULL)
AND (ec.Activo = 1 OR ec.Activo IS NULL)
GROUP BY e.IdExclusion, e.IdCatTipoRebate, IdCatTipoExclusion, IdCatEstatusExclusion, IdCatPeriodo,
Folio, Comentario, IdUsuarioSolicitud, IdUsuarioAutorizacion, FechaHoraSolicitud, FechaHoraAutorizacion,
e.Activo, Contabilizado, Evidencia, Imagen
ORDER BY e.IdExclusion DESC;
