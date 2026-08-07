SELECT COUNT(*) AS total,
  COUNT(DISTINCT CONCAT(NumeroProveedor,'-',OrdenCompra,'-',Recepcion,'-',
        FechaRecepcion,'-',Estatus)) AS distintos
FROM OrdenCompraProveedor WITH (NOLOCK);


-- construir tabla limpia
SELECT IdOrdenCompra, NumeroProveedor, OrdenCompra, Recepcion, Sucursal, NoGuia,
       ImporteSinImpuesto, FechaRecepcion, Estatus, Origen, FechaMovimiento,
       MotivoCancelacion, FechaRegistro, Uuid
INTO OrdenCompraProveedor_Clean
FROM (
  SELECT *,
         ROW_NUMBER() OVER (
           PARTITION BY NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus
           ORDER BY CASE WHEN ISNULL(Uuid,'') <> '' THEN 0 ELSE 1 END,
                    FechaRegistro DESC) AS rn
  FROM OrdenCompraProveedor WITH (NOLOCK)
) t
WHERE rn = 1;

SELECT COUNT(*) AS total_limpio FROM OrdenCompraProveedor_Clean; 


SELECT OBJECT_ID('dbo.uspRegistroOrdenCompraProveedor') AS sp_existe;



BEGIN TRAN;
  EXEC sp_rename 'OrdenCompraProveedor',       'OrdenCompraProveedor_OLD_20260703';
  EXEC sp_rename 'OrdenCompraProveedor_Clean', 'OrdenCompraProveedor';
COMMIT;


-- 0 duplicados esperado (0 filas)
SELECT NumeroProveedor,OrdenCompra,Recepcion,FechaRecepcion,Estatus,COUNT(*) c
FROM OrdenCompraProveedor
GROUP BY NumeroProveedor,OrdenCompra,Recepcion,FechaRecepcion,Estatus
HAVING COUNT(*)>1;

CREATE UNIQUE CLUSTERED INDEX UX_OrdenCompraProveedor_Negocio
ON OrdenCompraProveedor (NumeroProveedor,OrdenCompra,Recepcion,FechaRecepcion,Estatus)
WITH (IGNORE_DUP_KEY = ON);




-- ============================================================
-- SP reescrito: uspRegistroOrdenCompraProveedor
-- Depende de: indice unico UX_OrdenCompraProveedor_Negocio (IGNORE_DUP_KEY=ON) de 02.
-- ============================================================
ALTER PROCEDURE [dbo].[uspRegistroOrdenCompraProveedor]
AS
BEGIN
  SET NOCOUNT ON;

  BEGIN TRY
    BEGIN TRAN;

    -- 0) Dedup defensivo de la temp (por si acumulo filas entre corridas previas)
    ;WITH t AS (
      SELECT NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus, FechaRegistro,
             ROW_NUMBER() OVER (
               PARTITION BY NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus
               ORDER BY FechaRegistro DESC) AS rn
      FROM OrdenCompraProveedorTemp)
    DELETE FROM t WHERE rn > 1;

    -- 1) INSERT idempotente. Duplicados exactos -> ignorados por el indice unico.
    --    Cambio de estatus -> clave nueva -> entra como version nueva.
    INSERT INTO OrdenCompraProveedor
      (IdOrdenCompra, NumeroProveedor, OrdenCompra, Recepcion, Sucursal, NoGuia,
       ImporteSinImpuesto, FechaRecepcion, Estatus, Origen, FechaMovimiento,
       MotivoCancelacion, FechaRegistro, Uuid)
    SELECT
       IdOrdenCompra, NumeroProveedor, OrdenCompra, Recepcion, Sucursal, NoGuia,
       ImporteSinImpuesto, FechaRecepcion, Estatus, Origen, FechaMovimiento,
       MotivoCancelacion, FechaRegistro, NULL
    FROM OrdenCompraProveedorTemp;

    -- 2) Ultima factura por (prov, oc, recepcion) sin MAX correlacionado
    ;WITH fact AS (
      SELECT A.Uuid,
             CONVERT(NUMERIC, B.Extra1) AS IdProveedor,
             CONVERT(NUMERIC, B.Extra2) AS OrdenCompra,
             CONVERT(NUMERIC, B.Extra3) AS Recepcion,
             ROW_NUMBER() OVER (
               PARTITION BY B.Extra1, B.Extra2, B.Extra3
               ORDER BY A.Fecha DESC) AS rn
      FROM Comprobante A
      INNER JOIN Addenda B ON A.Uuid = B.Uuid
      WHERE B.Tipo = 1)
    SELECT Uuid, IdProveedor, OrdenCompra, Recepcion
    INTO #Factura
    FROM fact
    WHERE rn = 1;

    -- 3) Asignar Uuid (vacio si estatus != 2)
    UPDATE A
      SET A.Uuid = CASE WHEN A.Estatus <> 2 THEN '' ELSE B.Uuid END
    FROM OrdenCompraProveedor A
    INNER JOIN #Factura B
      ON A.NumeroProveedor = B.IdProveedor
     AND A.OrdenCompra     = B.OrdenCompra
     AND A.Recepcion       = B.Recepcion;

    TRUNCATE TABLE OrdenCompraProveedorTemp;
    DROP TABLE #Factura;

    COMMIT;
  END TRY
  BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;   -- relanzar para que el batch registre el fallo (no silenciar)
  END CATCH;
END




-- baseline
SELECT COUNT(*) AS antes FROM OrdenCompraProveedor; -- 57330

-- sembrar temp con 3 claves NUEVAS (no existen)
TRUNCATE TABLE OrdenCompraProveedorTemp; -- -1
select count(1) from OrdenCompraProveedorTemp;

SELECT name, type_desc, is_unique FROM sys.indexes
WHERE object_id = OBJECT_ID('OrdenCompraProveedor') AND name = 'UX_OrdenCompraProveedor_Negocio';




-- RE-CORRER con la misma data (simula 2da corrida)
INSERT INTO OrdenCompraProveedorTemp
 (IdOrdenCompra,NumeroProveedor,OrdenCompra,Recepcion,Sucursal,NoGuia,ImporteSinImpuesto,
  FechaRecepcion,Estatus,Origen,FechaMovimiento,MotivoCancelacion,FechaRegistro)
VALUES
 (999001,99901,88801,77701,'TEST','G1',100,'2026-01-15T00:00:00','2','SLI','','',GETDATE()),
 (999002,99901,88802,77702,'TEST','G2',200,'2026-01-15T00:00:00','2','SLI','','',GETDATE()),
 (999003,99901,88803,77703,'TEST','G3',300,'2026-01-15T00:00:00','2','SLI','','',GETDATE());

EXEC uspRegistroOrdenCompraProveedor;
SELECT COUNT(*) AS tras_2da FROM OrdenCompraProveedor;   -- DEBE SEGUIR antes + 3







SELECT COUNT(*) AS antes FROM OrdenCompraProveedor; -- 57330


SELECT COUNT(*) AS antes FROM OrdenCompraProveedor; -- 57330

-- sembrar temp con 3 claves NUEVAS (no existen)
TRUNCATE TABLE OrdenCompraProveedorTemp; -- -1
select count(1) from OrdenCompraProveedorTemp;


SELECT name, type_desc, is_unique FROM sys.indexes
WHERE object_id = OBJECT_ID('OrdenCompraProveedor') AND name = 'UX_OrdenCompraProveedor_Negocio';

"name","type_desc","is_unique"
UX_OrdenCompraProveedor_Negocio,CLUSTERED,1



INSERT INTO OrdenCompraProveedorTemp
 (IdOrdenCompra,NumeroProveedor,OrdenCompra,Recepcion,Sucursal,NoGuia,ImporteSinImpuesto,
  FechaRecepcion,Estatus,Origen,FechaMovimiento,MotivoCancelacion,FechaRegistro)
VALUES
 (999001,99901,88801,77701,'TEST','G1',100,'2026-01-15T00:00:00','2','SLI','','',GETDATE()),
 (999002,99901,88802,77702,'TEST','G2',200,'2026-01-15T00:00:00','2','SLI','','',GETDATE()),
 (999003,99901,88803,77703,'TEST','G3',300,'2026-01-15T00:00:00','2','SLI','','',GETDATE());

EXEC uspRegistroOrdenCompraProveedor;

SELECT COUNT(*) AS tras_2da FROM OrdenCompraProveedor;


DELETE FROM OrdenCompraProveedor WHERE Sucursal='TEST';
TRUNCATE TABLE OrdenCompraProveedorTemp;


DEV ya está limpio (y la tabla real ya tiene el índice único que bloquea duplicados). Para probar la limpieza con volumen, trabajamos en una tabla scratch aparte (heap, sin índice) donde sí podemos inyectar duplicados. Así no tocamos la tabla ya arreglada.

Secuencia como la pediste (+ paso 0 para inyectar, ya que DEV no tiene dups):

Paso 0 — Crear scratch + inyectar duplicados

SELECT DB_NAME();  -- SODIMAC_SAP_DEV

-- copia de la data real a tabla de prueba (heap, sin indice unico)
IF OBJECT_ID('OrdenCompraProveedor_DupTest') IS NOT NULL DROP TABLE OrdenCompraProveedor_DupTest;
SELECT IdOrdenCompra, NumeroProveedor, OrdenCompra, Recepcion, Sucursal, NoGuia,
       ImporteSinImpuesto, FechaRecepcion, Estatus, Origen, FechaMovimiento,
       MotivoCancelacion, FechaRegistro, Uuid
INTO OrdenCompraProveedor_DupTest
FROM OrdenCompraProveedor;

-- multiplicar: cada INSERT duplica el total. Corre 3 veces = x8 (~458k)
INSERT INTO OrdenCompraProveedor_DupTest
 (IdOrdenCompra, NumeroProveedor, OrdenCompra, Recepcion, Sucursal, NoGuia,
  ImporteSinImpuesto, FechaRecepcion, Estatus, Origen, FechaMovimiento,
  MotivoCancelacion, FechaRegistro, Uuid)
SELECT IdOrdenCompra, NumeroProveedor, OrdenCompra, Recepcion, Sucursal, NoGuia,
       ImporteSinImpuesto, FechaRecepcion, Estatus, Origen, FechaMovimiento,
       MotivoCancelacion, FechaRegistro, Uuid
FROM OrdenCompraProveedor_DupTest;
-- ^ ejecuta este INSERT 3 veces
Paso 1 — Sacar/ver los duplicados

-- resumen
SELECT COUNT(*) AS total,
  COUNT(DISTINCT CONCAT(NumeroProveedor,'-',OrdenCompra,'-',Recepcion,'-',
        FechaRecepcion,'-',Estatus)) AS distintos
FROM OrdenCompraProveedor_DupTest WITH (NOLOCK);

-- detalle: claves con mas copias
SELECT TOP 20 NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus, COUNT(*) AS copias
FROM OrdenCompraProveedor_DupTest WITH (NOLOCK)
GROUP BY NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus
HAVING COUNT(*) > 1
ORDER BY COUNT(*) DESC;
Esperado: total ~458k, distintos ~57k, copias = 8 en cada clave.

Paso 2 — Respaldar
La tabla OrdenCompraProveedor_DupTest ES el respaldo (queda intacta; la limpia se construye aparte). No se borra hasta validar.

Paso 3 — Limpiar (misma lógica del script 01)

IF OBJECT_ID('OrdenCompraProveedor_DupTest_Clean') IS NOT NULL DROP TABLE OrdenCompraProveedor_DupTest_Clean;
SELECT IdOrdenCompra, NumeroProveedor, OrdenCompra, Recepcion, Sucursal, NoGuia,
       ImporteSinImpuesto, FechaRecepcion, Estatus, Origen, FechaMovimiento,
       MotivoCancelacion, FechaRegistro, Uuid
INTO OrdenCompraProveedor_DupTest_Clean
FROM (
  SELECT IdOrdenCompra, NumeroProveedor, OrdenCompra, Recepcion, Sucursal, NoGuia,
         ImporteSinImpuesto, FechaRecepcion, Estatus, Origen, FechaMovimiento,
         MotivoCancelacion, FechaRegistro, Uuid,
         ROW_NUMBER() OVER (
           PARTITION BY NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus
           ORDER BY CASE WHEN ISNULL(Uuid,'') <> '' THEN 0 ELSE 1 END,
                    FechaRegistro DESC) AS rn
  FROM OrdenCompraProveedor_DupTest WITH (NOLOCK)
) t
WHERE rn = 1;
Paso 4 — Validar

SELECT
  (SELECT COUNT(*) FROM OrdenCompraProveedor_DupTest)                    AS con_duplicados,
  (SELECT COUNT(*) FROM OrdenCompraProveedor_DupTest_Clean)              AS limpio,
  (SELECT COUNT(DISTINCT CONCAT(NumeroProveedor,'-',OrdenCompra,'-',Recepcion,'-',
          FechaRecepcion,'-',Estatus)) FROM OrdenCompraProveedor_DupTest) AS distintos;

-- 0 SODIMAC_SAP_DEV.dbo.Addendaduplicados en la limpia (debe dar 0 filas)
SELECT NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus, COUNT(*) c
FROM OrdenCompraProveedor_DupTest_Clean
GROUP BY NumeroProveedor, OrdenCompra, Recepcion, FechaRecepcion, Estatus
HAVING COUNT(*) > 1;
Veredicto: limpio = distintos (~57k) y 0 duplicados → limpieza probada con volumen.

Limpieza del scratch (al terminar)

DROP TABLE OrdenCompraProveedor_DupTest;
DROP TABLE OrdenCompraProveedor_DupTest_Clean;
Corre 0→1→2→3→4 y pásame los 3 números del Paso 4 (con_duplicados / limpio / distintos).


select top 20 * from SODIMAC_SAP_DEV.dbo.Addenda 
select * from Comprobante where uuid in ('000084EC-4495-42E6-8F7E-AA6BEF7B5DA4',
'0000EAB2-BC8E-4E27-A96F-0B6DF9D56330',
'0000EEF1-40C4-40B3-9632-72BAFD84882D',
'0000F980-CD0E-0147-B9ED-3F8502A872A9',
'00018405-244D-44DA-B32D-09BE277B0FE7',
'00027BB1-0981-4466-8143-44991D098877',
'00028540-C84A-4DDE-8542-F662EA68E699',
'000303F4-5820-4AFE-BDBB-076A78C9715B',
'000389E6-CE88-4BE5-9140-A65264C5C776',
'0003A2B4-D088-4657-8C5B-A05086C9E4C2',
'0003D630-4D14-11EE-88AB-0FE1EDEABD9F',
'0003EC71-CC49-11EE-824C-BF37E1509F46',
'000439D3-B658-47DF-B797-622F4C03558E',
'00053162-78A5-4C98-A4CB-157844F2C21F',
'000537B0-A941-4EF6-99A1-8DAFC3ABD911',
'00055DF2-4913-4F48-8987-E6EF6CFECCF1',
'00059EB6-F05A-4BBC-A2EA-F1DA6A749867',
'0007c07c-bd7c-4340-a65b-3706745e501d',
'0007F7D1-C804-4971-9FA3-59AC505377D5',
'00080d32-13f7-4a0d-a1ee-b4a90fa5ebfd');



----------------------------------------------------------------------

1. Distribución de Tipo:


SELECT Tipo, COUNT(*) n FROM dbo.Addenda GROUP BY Tipo ORDER BY Tipo;
2. Qué TipoDeComprobante corresponde a cada Tipo de addenda:


SELECT c.TipoDeComprobante, a.Tipo, COUNT(*) n
FROM dbo.Addenda a
JOIN dbo.Comprobante c ON c.Uuid = a.Uuid
GROUP BY c.TipoDeComprobante, a.Tipo
ORDER BY c.TipoDeComprobante, a.Tipo;


3. Muestra de addenda por cada Tipo (para ver el mapeo Extra):


SELECT TOP 10 a.Tipo, c.TipoDeComprobante, c.Serie, c.Folio,
       a.Uuid, a.Extra1, a.Extra2, a.Extra3, a.Extra4, a.Extra5, a.Extra6
FROM dbo.Addenda a
JOIN dbo.Comprobante c ON c.Uuid = a.Uuid
WHERE a.Tipo = 2;   -- repetir con Tipo=2 y, si existe, el Tipo de NC

"Tipo","TipoDeComprobante","Serie","Folio","Uuid","Extra1","Extra2","Extra3","Extra4","Extra5","Extra6"
2,I,DN,"37526","00027BB1-0981-4466-8143-44991D098877","251191","","","1.0","08AU6T13112023191448",V0164
2,I,WSE,"26989","000389E6-CE88-4BE5-9140-A65264C5C776","251513","","","1.0","62AG9G16012023143311",V0736
2,I,,"5180","0014ABF1-4BA6-414A-B0B9-CB46C8DCCFE8","251429","","","1.0","14AM5D29032023195919",V0713
2,I,WSE,"17203","0023B116-1A1F-4E48-9668-4302D182D96E","251513","","","1.0","63AG9G25032022185022",V0150
2,I,DN,"41318","00302416-7302-4D5F-B82D-A5667EFBA000","251191","","","1.0","69AK8T07032024184007",V0128
2,I,CV,"2514","00435125-6173-49D8-B915-65934A507E3A","252250","","","1.0","85AZ7P02106202409254",V1675
2,I,A,"2491","005a8629-65f8-46ee-886f-96482545b39a","251706","","","1.0",TH3349F27072022173100,V0042
2,I,,"12309","005D2CE2-E375-4390-9E8D-9B253BD837F8","251735","","","1.0",LD619391108202214233,V0035
2,I,,"8036","005DBC83-708F-495E-9C37-4038B4DA7596","251429","","","1.0","75AZ9W13022025163925",V0174
2,I,,"7589","005FD5AD-3A0D-4DE8-8CB6-B369B7D2B423","251429","","","1.0","22BD5E12112024213241",V0866

4. NC (tipo E): ¿existen y traen addenda?


SELECT COUNT(*) total_E FROM dbo.Comprobante WHERE TipoDeComprobante = 'E';

SELECT TOP 10 c.Uuid, c.Serie, c.Folio, a.Tipo,
       a.Extra1, a.Extra2, a.Extra3, a.Extra4, a.Extra5, a.Extra6
FROM dbo.Comprobante c
LEFT JOIN dbo.Addenda a ON a.Uuid = c.Uuid
WHERE c.TipoDeComprobante = 'E';
5. Nodo XML de addenda para transporte (Tipo=2) y NC — para saber cómo parsearlo:


-- Transporte
SELECT TOP 2 a.Tipo, c.Xml
FROM dbo.Addenda a JOIN dbo.Comprobante c ON c.Uuid = a.Uuid
WHERE a.Tipo = 2;

-- NC (si existen)
SELECT TOP 2 c.Uuid, c.Xml
FROM dbo.Comprobante c
WHERE c.TipoDeComprobante = 'E';
6. Link NC↔factura (por si la addenda de NC se apoya aquí):


SELECT TOP 10 * FROM dbo.CfdiRelacionados;


SELECT *
FROM SODIMAC_SAP_DEV.dbo.Comprobante
where uuid in ('D748C53F-2C61-4394-8642-DD6E18346D05', 'C2F81A61-D683-4E44-8D14-8CD7A11E5288')
ORDER BY FEcha DESC;


SELECT c.fiscal_uuid,
       a.Extra2 AS oc_guardada,
       a.Extra3 AS recep_guardada,
       a.Extra4 AS folio_guardado,
       a.Tipo
FROM SODIMAC_SAP_DEV.dbo.Comprobante c
JOIN SODIMAC_SAP_DEV.dbo.Addenda a ON a.Uuid = c.fiscal_uuid
WHERE c.fiscal_uuid IN (
  'C2F81A61-D683-4E44-8D14-8CD7A11E5288',
  'D748C53F-2C61-4394-8642-DD6E18346D05'
);
