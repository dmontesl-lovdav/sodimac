-- ============================================================
-- VISTA: dbo.vw_ordencompraproveedor
-- BD: SODIMAC_SAP_PROD (10.138.150.124:5319)
-- Capturado: 2026-06-25 (referencia analisis duplicados OC Detecno)
-- ============================================================
ALTER VIEW [dbo].[vw_ordencompraproveedor]
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
AND C.estatus='A';
