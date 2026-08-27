-- STM-719 / Refactor esquema legado (opcion B)
-- Cambio ADITIVO solicitado para SODIMAC_SAP_DEV: tabla Receptor del CFDI 4.0.
-- La HU de descarga de facturas/NC marca el nodo Receptor como requerido; el esquema
-- legado no tiene tabla para el. Espejo del estilo de la tabla Emisor existente.
-- fiscal-download detecta en runtime si la tabla existe: si no, omite el nodo con WARN
-- (no truena), asi que este script puede aplicarse en cualquier momento.

CREATE TABLE dbo.Receptor (
    Uuid            VARCHAR(36)  NOT NULL,   -- UUID del TimbreFiscalDigital (= Comprobante.Uuid)
    Nombre          VARCHAR(254) NOT NULL,
    Rfc             VARCHAR(13)  NOT NULL,
    Regimen         VARCHAR(3)   NOT NULL,   -- RegimenFiscalReceptor
    UsoCFDI         VARCHAR(4)   NOT NULL,
    DomicilioFiscal VARCHAR(5)   NULL        -- DomicilioFiscalReceptor (codigo postal)
);

-- Opcional pero recomendado (Emisor legado no lo tiene, pero facilita consultas):
CREATE INDEX IX_Receptor_Uuid ON dbo.Receptor (Uuid);
