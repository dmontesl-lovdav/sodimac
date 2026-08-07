SELECT idControlVentaCes, fechaTrx, totalVentasCes, totalRegistrados, estatus, fechaRegistro, fechaMod
FROM SODIMAC_SAP_PROD.dbo.ControlVentaCes;




select count(1) from OrdenCompraProveedorTemp;

select count(1) from OrdenCompraProveedor; --379,252

select distinct (dateadd(dd, datediff(dd,0, FechaRegistro), 0) )
from OrdenCompraProveedor order by (dateadd(dd, datediff(dd,0, FechaRegistro), 0) ) desc;

select DATETRUNC(getdate()) 
select dateadd(dd, datediff(dd,0, getDate()), 0)

/*
 * CREATE PROCEDURE [dbo].[uspRegistroOrdenCompraProveedor] 
AS

BEGIN TRANSACTION;

BEGIN
TRY

	INSERT INTO OrdenCompraProveedor
	SELECT
	  IdOrdenCompra
	, NumeroProveedor
	, OrdenCompra
	, Recepcion
	, Sucursal
	, NoGuia
	, ImporteSinImpuesto
	, FechaRecepcion
	, Estatus
	, Origen
	, FechaMovimiento
	, MotivoCancelacion
	, FechaRegistro
	, NULL Uuid
	FROM OrdenCompraProveedorTemp A
	WHERE  NOT EXISTS (
		SELECT	1
		FROM OrdenCompraProveedor B
		WHERE A.NumeroProveedor = B.NumeroProveedor
		AND A.OrdenCompra = B.OrdenCompra
		AND A.Recepcion = B.Recepcion
		AND A.FechaRecepcion = B.FechaRecepcion
		AND A.Estatus = B.Estatus
	);

	SELECT *
	INTO #OrdenCompraActual_Temp
	FROM vw_ordencompraproveedor A
	WHERE A.FechaRegistro = (
	   SELECT MAX(B.FechaRegistro)
	   FROM [dbo].[vw_ordencompraproveedor] B
	   WHERE A.NumeroProveedor = B.NumeroProveedor
	   AND A.OrdenCompra = B.OrdenCompra
	   AND A.Recepcion = B.Recepcion
	);

	INSERT INTO OrdenCompraProveedor
	SELECT
	  IdOrdenCompra
	, NumeroProveedor
	, OrdenCompra
	, Recepcion
	, Sucursal
	, NoGuia
	, ImporteSinImpuesto
	, FechaRecepcion
	, Estatus
	, Origen
	, FechaMovimiento
	, MotivoCancelacion
	, FechaRegistro
	, NULL Uuid
	FROM OrdenCompraProveedorTemp A
	WHERE  EXISTS (
		SELECT	1
		FROM #OrdenCompraActual_Temp B
		WHERE A.NumeroProveedor = B.NumeroProveedor
		AND A.OrdenCompra = B.OrdenCompra
		AND A.Recepcion = B.Recepcion
		AND A.FechaRecepcion = B.FechaRecepcion
		AND A.Estatus <> B.EstatusDetecno
	);

	SELECT A.Uuid
	, CONVERT(NUMERIC,B.Extra1)  IdProveedor
	, CONVERT(NUMERIC,B.Extra2)  OrdenCompra
	, CONVERT(NUMERIC,B.Extra3)  Recepcion
	INTO #FacturaTemp
	FROM Comprobante A
	INNER JOIN Addenda B
	ON A.Uuid = B.Uuid
	WHERE B.Tipo = 1
	AND A.Fecha = (
	    SELECT MAX(SUBA.Fecha)
		FROM Comprobante SUBA
		INNER JOIN Addenda SUBB
		ON SUBA.Uuid = SUBB.Uuid
		WHERE SUBB.Tipo = 1
		AND SUBB.Extra1 = B.Extra1
		AND SUBB.Extra2 = B.Extra2
		AND SUBB.Extra3 = B.Extra3
	)
  
	 UPDATE A  SET A.Uuid = (CASE WHEN A.Estatus != 2 THEN '' ELSE  B.Uuid END)
	 FROM OrdenCompraProveedor A
	 INNER JOIN #FacturaTemp B
	 ON A.NumeroProveedor = B.IdProveedor
	 AND A.OrdenCompra = B.OrdenCompra
	 AND A.Recepcion = B.Recepcion;

     TRUNCATE TABLE OrdenCompraProveedorTemp;
     DROP TABLE  #OrdenCompraActual_Temp
	--DROP TABLE #OrdenCompraTemp;
	 DROP TABLE #FacturaTemp;


	COMMIT;

END TRY

BEGIN CATCH
    ROLLBACK;
END CATCH;
 * */

create table ControlOrdenCompraDetecno
(
	idControlOrdenCompraDetecno int NOT NULL,
	fechaTrx datetime NOT NULL,
	totalOrdenCompra int NOT NULL,
	estatus int NOT NULL,
	fechaRegistro datetime NULL
);



SELECT OBJECT_DEFINITION(OBJECT_ID('dbo.uspRegistroOrdenCompraProveedor')) AS sp_actual;
SELECT OBJECT_DEFINITION(OBJECT_ID('dbo.vw_ordencompraproveedor'))       AS vista_actual;




SELECT DB_NAME();  -- DEBE decir SODIMAC_SAP_PROD

-- 1) rollback del incidente ya termino? (fila vacia)
SELECT session_id, status, command, percent_complete
FROM sys.dm_exec_requests WHERE session_id = 98;

-- 2) tamano actual
SELECT COUNT(*) AS total FROM OrdenCompraProveedor WITH (NOLOCK);

-- 3) distintos (objetivo)
SELECT COUNT(DISTINCT CONCAT(NumeroProveedor,'-',OrdenCompra,'-',Recepcion,'-',
       FechaRecepcion,'-',Estatus)) AS distintos
FROM OrdenCompraProveedor WITH (NOLOCK);


SELECT NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus, COUNT(*) copias
FROM OrdenCompraProveedor WITH (NOLOCK)
GROUP BY NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus
HAVING COUNT(*) > 1;



PASO 1 — Construir tabla limpia (solo lee, seguro)

SELECT DB_NAME();  -- confirmar SODIMAC_SAP_PROD

IF OBJECT_ID('OrdenCompraProveedor_Clean') IS NOT NULL DROP TABLE OrdenCompraProveedor_Clean;

SELECT IdOrdenCompra, NumeroProveedor, OrdenCompra, Recepcion, Sucursal, NoGuia,
       ImporteSinImpuesto, FechaRecepcion, Estatus, Origen, FechaMovimiento,
       MotivoCancelacion, FechaRegistro, Uuid
INTO OrdenCompraProveedor_Clean
FROM (
  SELECT IdOrdenCompra, NumeroProveedor, OrdenCompra, Recepcion, Sucursal, NoGuia,
         ImporteSinImpuesto, FechaRecepcion, Estatus, Origen, FechaMovimiento,
         MotivoCancelacion, FechaRegistro, Uuid,
         ROW_NUMBER() OVER (
           PARTITION BY NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus
           ORDER BY CASE WHEN ISNULL(Uuid,'') <> '' THEN 0 ELSE 1 END,
                    FechaRegistro DESC) AS rn
  FROM OrdenCompraProveedor WITH (NOLOCK)
) t
WHERE rn = 1;
PASO 2 — Validar (checkpoint)

SELECT COUNT(*) AS total_limpio FROM OrdenCompraProveedor_Clean;   -- esperado 283,694
SELECT COUNT(*) AS con_uuid FROM OrdenCompraProveedor_Clean WHERE ISNULL(Uuid,'') <> '';

-- 0 duplicados en la limpia (0 filas)
SELECT NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus, COUNT(*) c
FROM OrdenCompraProveedor_Clean
GROUP BY NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus
HAVING COUNT(*) > 1;
Checkpoint: total_limpio = 283,694 y la 2da query 0 filas. Si no cuadra → paramos.


IF OBJECT_ID('OrdenCompraProveedor_BKP_20260703') IS NOT NULL DROP TABLE OrdenCompraProveedor_BKP_20260703;
SELECT IdOrdenCompra, NumeroProveedor, OrdenCompra, Recepcion, Sucursal, NoGuia,
       ImporteSinImpuesto, FechaRecepcion, Estatus, Origen, FechaMovimiento,
       MotivoCancelacion, FechaRegistro, Uuid
INTO OrdenCompraProveedor_BKP_20260703
FROM OrdenCompraProveedor WITH (NOLOCK);

SELECT COUNT(*) FROM OrdenCompraProveedor_BKP_20260703;  -- debe dar 283,696


BEGIN TRAN;
  EXEC sp_rename 'OrdenCompraProveedor',       'OrdenCompraProveedor_OLD_20260703';
  EXEC sp_rename 'OrdenCompraProveedor_Clean', 'OrdenCompraProveedor';
COMMIT;

-- 0 duplicados (0 filas)
SELECT NumeroProveedor,OrdenCompra,Recepcion,FechaRecepcion,Estatus,COUNT(*) c
FROM OrdenCompraProveedor
GROUP BY NumeroProveedor,OrdenCompra,Recepcion,FechaRecepcion,Estatus
HAVING COUNT(*)>1;

CREATE UNIQUE CLUSTERED INDEX UX_OrdenCompraProveedor_Negocio
ON OrdenCompraProveedor (NumeroProveedor,OrdenCompra,Recepcion,FechaRecepcion,Estatus)
WITH (IGNORE_DUP_KEY = ON);




SELECT COUNT(*) AS antes FROM OrdenCompraProveedor;

TRUNCATE TABLE OrdenCompraProveedorTemp;
INSERT INTO OrdenCompraProveedorTemp
 (IdOrdenCompra,NumeroProveedor,OrdenCompra,Recepcion,Sucursal,NoGuia,ImporteSinImpuesto,
  FechaRecepcion,Estatus,Origen,FechaMovimiento,MotivoCancelacion,FechaRegistro)
VALUES
 (999001,99901,88801,77701,'TEST','G1',100,'2026-01-15T00:00:00','2','SLI','','',GETDATE());
EXEC uspRegistroOrdenCompraProveedor;
SELECT COUNT(*) AS tras_1 FROM OrdenCompraProveedor;   -- antes + 1


INSERT INTO OrdenCompraProveedorTemp
 (IdOrdenCompra,NumeroProveedor,OrdenCompra,Recepcion,Sucursal,NoGuia,ImporteSinImpuesto,
  FechaRecepcion,Estatus,Origen,FechaMovimiento,MotivoCancelacion,FechaRegistro)
VALUES
 (999001,99901,88801,77701,'TEST','G1',100,'2026-01-15T00:00:00','2','SLI','','',GETDATE());

EXEC uspRegistroOrdenCompraProveedor;
SELECT COUNT(*) AS tras_2 FROM OrdenCompraProveedor;   -- DEBE seguir = tras_1

-- limpiar prueba
DELETE FROM OrdenCompraProveedor WHERE Sucursal='TEST';
TRUNCATE TABLE OrdenCompraProveedorTemp;



DELETE FROM OrdenCompraProveedor WHERE Sucursal='TEST';
TRUNCATE TABLE OrdenCompraProveedorTemp;
SELECT COUNT(*) FROM OrdenCompraProveedor;  -- vuelve a 283,694


-- enero cargado
SELECT COUNT(*) AS enero FROM OrdenCompraProveedor WITH(NOLOCK)
WHERE FechaRecepcion >= '2026-01-01' AND FechaRecepcion < '2026-08-01';
--35275

-- total tabla
SELECT COUNT(*) AS total FROM OrdenCompraProveedor WITH(NOLOCK);
--318969

-- 0 duplicados (debe dar 0 filas)
SELECT NumeroProveedor,OrdenCompra,Recepcion,FechaRecepcion,Estatus,COUNT(*) c
FROM OrdenCompraProveedor WITH(NOLOCK)
GROUP BY NumeroProveedor,OrdenCompra,Recepcion,FechaRecepcion,Estatus
HAVING COUNT(*)>1;

-- temp vacia (SP la consumio)
SELECT COUNT(*) AS temp FROM OrdenCompraProveedorTemp WITH(NOLOCK);