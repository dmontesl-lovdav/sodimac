-- =====================================================================
-- sap_document <-> folio fiscal (Ivan 2026-09-08)
--
-- Objetivo: ligar el movimiento contable SAP (tenant_finance.sap_document)
-- con la(s) factura(s)/NC por su folio fiscal (UUID SAT).
--
-- Regla de negocio (Ivan): UN documento SAP puede tener VARIOS folios fiscales
-- (por reintentos / errores / re-timbrado); cada folio corresponde a una sola
-- factura/NC. Como es 1:N, NO se resuelve con una columna en sap_document sino
-- con una tabla hija.
--
-- FK: solo al documento SAP padre. El folio (fiscal_uuid) queda como columna
-- INDEXADA SIN llave foranea: la addenda normal (tenant_fiscal.addendum) no tiene
-- el folio como campo unico, y ademas por errores se registran folios que pueden
-- no corresponder a una factura valida -> una FK al folio los rechazaria.
--
-- Idempotente. Correr en la BD de finanzas (tenant_finance).
-- OJO drift: confirmar que la columna id de sap_document se llama
-- 'sap_document_uuid'; si en el ambiente real es otra, ajustar pasos 1 y 2.
-- =====================================================================

-- 0. (verificacion manual) no debe haber duplicados en el id del padre:
--    SELECT sap_document_uuid, count(*) FROM tenant_finance.sap_document
--    GROUP BY sap_document_uuid HAVING count(*) > 1;   -- esperado 0 filas

-- 1. PK al padre (necesaria para poder referenciarlo por FK). ADD CONSTRAINT no
--    soporta IF NOT EXISTS -> se protege con un DO.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_schema = 'tenant_finance'
          AND table_name   = 'sap_document'
          AND constraint_type = 'PRIMARY KEY'
    ) THEN
        ALTER TABLE tenant_finance.sap_document
            ADD CONSTRAINT pk_sap_document PRIMARY KEY (sap_document_uuid);
    END IF;
END $$;

-- 2. Tabla hija: un documento SAP -> N folios fiscales.
CREATE TABLE IF NOT EXISTS tenant_finance.sap_document_fiscal_uuid (
    id                uuid   NOT NULL DEFAULT gen_random_uuid(),
    sap_document_uuid uuid   NOT NULL,
    fiscal_uuid       uuid   NOT NULL,   -- folio fiscal (UUID SAT) de la factura/NC
    created_by        bigint,
    created_at        timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_sap_document_fiscal_uuid PRIMARY KEY (id),
    CONSTRAINT fk_sdfu_sap_document FOREIGN KEY (sap_document_uuid)
        REFERENCES tenant_finance.sap_document (sap_document_uuid) ON DELETE CASCADE,
    CONSTRAINT uk_sdfu UNIQUE (sap_document_uuid, fiscal_uuid)
);

CREATE INDEX IF NOT EXISTS idx_sdfu_fiscal_uuid
    ON tenant_finance.sap_document_fiscal_uuid (fiscal_uuid);
