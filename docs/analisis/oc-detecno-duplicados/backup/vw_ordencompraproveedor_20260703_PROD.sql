-- ============================================================
-- RESPALDO / ROLLBACK — VISTA vw_ordencompraproveedor
-- Origen: SODIMAC_SAP_PROD (10.138.150.124:5319)
-- Capturado: 2026-07-03 via OBJECT_DEFINITION (definicion vigente en PROD)
-- Verificado: IDENTICO a la captura del 2026-06-25.
-- NOTA: la vista NO se modifica en el plan (el SP reescrito ya no la usa). Se
--       respalda por seguridad. Restaurar con DROP VIEW + CREATE, o ALTER VIEW.
-- ============================================================
CREATE VIEW [dbo].[vw_ordencompraproveedor]
AS
SELECT
  A.IdOrdenCompra
, A.NumeroProveedor
, C.nombre Nombre
, A.OrdenCompra
, A.Recepcion
, A.Sucursal
, A.NoGuia
, A.ImporteSinImpuesto
, A.FechaRecepcion
, ISNULL((SELECT B.elemento FROM AdminCatalogo B WHERE B.idCatalogo=19 AND B.idElemento = A.Estatus AND B.activo=1),'Otro') Estatus
, (
     CASE
	      WHEN LTRIM(RTRIM(A.Origen)) = 'TRA'  THEN
		  'Transporte'
		  WHEN LTRIM(RTRIM(A.Origen)) = 'SLI' OR A.Origen IS NULL  THEN
		  'Mercancía'
		  ELSE
		  'Otros'
     END
  ) Origen
, A.FechaMovimiento
, A.MotivoCancelacion
, A.FechaRegistro
, (CASE WHEN A.Estatus != 2 THEN ''  ELSE ISNULL(A.Uuid,'') END) UUID
, A.Estatus EstatusDetecno
FROM OrdenCompraProveedor A
LEFT JOIN CatProveedor C
ON A.NumeroProveedor = C.codigoProveedor
AND C.estatus='A'
