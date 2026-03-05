-- =====================================================
-- SODIMAC_SAP_DEV - Desglose de CFDI 4.0
-- Tablas para almacenar el desglose de facturas y NC
-- descargadas del portal FBC (STM-719)
-- =====================================================

-- Tabla principal: Comprobante
CREATE TABLE dbo.Comprobante (
    id_comprobante  INT IDENTITY(1,1) NOT NULL,
    fiscal_uuid     VARCHAR(36)    NOT NULL,
    invoice_uuid    VARCHAR(36)    NULL,
    version         VARCHAR(5)     NOT NULL,
    serie           VARCHAR(25)    NULL,
    folio           VARCHAR(49)    NULL,
    fecha           DATETIME       NOT NULL,
    subtotal        DECIMAL(18,6)  NOT NULL,
    total           DECIMAL(18,6)  NOT NULL,
    descuento       DECIMAL(18,6)  NULL,
    moneda          VARCHAR(3)     DEFAULT 'MXN' NOT NULL,
    tipo_cambio     DECIMAL(18,6)  DEFAULT 1.0 NULL,
    tipo_comprobante VARCHAR(2)    NOT NULL,
    metodo_pago     VARCHAR(3)     NULL,
    forma_pago      VARCHAR(10)    NULL,
    condiciones_pago VARCHAR(255)  NULL,
    lugar_expedicion VARCHAR(5)    NULL,
    exportacion     VARCHAR(2)     NULL,
    no_certificado  VARCHAR(30)    NULL,
    sello           VARCHAR(MAX)   NULL,
    certificado     VARCHAR(MAX)   NULL,
    xml_completo    VARCHAR(MAX)   NULL,
    fecha_timbrado  DATETIME       NULL,
    rfc_prov_certif VARCHAR(13)    NULL,
    no_certificado_sat VARCHAR(30) NULL,
    estatus_proceso VARCHAR(20)    DEFAULT 'PROCESADO' NOT NULL,
    fecha_registro  DATETIME       DEFAULT GETDATE() NOT NULL,
    CONSTRAINT PK_Comprobante PRIMARY KEY (id_comprobante),
    CONSTRAINT UQ_Comprobante_fiscal_uuid UNIQUE (fiscal_uuid)
);
CREATE NONCLUSTERED INDEX IX_Comprobante_tipo ON dbo.Comprobante (tipo_comprobante);
CREATE NONCLUSTERED INDEX IX_Comprobante_fecha ON dbo.Comprobante (fecha DESC);
CREATE NONCLUSTERED INDEX IX_Comprobante_fecha_registro ON dbo.Comprobante (fecha_registro DESC);

-- Emisor (proveedor)
CREATE TABLE dbo.Emisor (
    id_emisor       INT IDENTITY(1,1) NOT NULL,
    id_comprobante  INT            NOT NULL,
    rfc             VARCHAR(13)    NOT NULL,
    nombre          NVARCHAR(300)  NULL,
    regimen_fiscal  VARCHAR(5)     NULL,
    CONSTRAINT PK_Emisor PRIMARY KEY (id_emisor),
    CONSTRAINT FK_Emisor_Comprobante FOREIGN KEY (id_comprobante)
        REFERENCES dbo.Comprobante(id_comprobante) ON DELETE CASCADE
);
CREATE NONCLUSTERED INDEX IX_Emisor_rfc ON dbo.Emisor (rfc);
CREATE NONCLUSTERED INDEX IX_Emisor_comprobante ON dbo.Emisor (id_comprobante);

-- Receptor (empresa / Sodimac)
CREATE TABLE dbo.Receptor (
    id_receptor     INT IDENTITY(1,1) NOT NULL,
    id_comprobante  INT            NOT NULL,
    rfc             VARCHAR(13)    NOT NULL,
    nombre          NVARCHAR(300)  NULL,
    uso_cfdi        VARCHAR(5)     NULL,
    regimen_fiscal  VARCHAR(5)     NULL,
    domicilio_fiscal VARCHAR(5)    NULL,
    CONSTRAINT PK_Receptor PRIMARY KEY (id_receptor),
    CONSTRAINT FK_Receptor_Comprobante FOREIGN KEY (id_comprobante)
        REFERENCES dbo.Comprobante(id_comprobante) ON DELETE CASCADE
);
CREATE NONCLUSTERED INDEX IX_Receptor_comprobante ON dbo.Receptor (id_comprobante);

-- Concepto (lineas del comprobante)
CREATE TABLE dbo.Concepto (
    id_concepto     INT IDENTITY(1,1) NOT NULL,
    id_comprobante  INT            NOT NULL,
    clave_prod_serv VARCHAR(10)    NOT NULL,
    no_identificacion VARCHAR(100) NULL,
    cantidad        DECIMAL(18,6)  NOT NULL,
    clave_unidad    VARCHAR(5)     NOT NULL,
    unidad          VARCHAR(50)    NULL,
    descripcion     NVARCHAR(1000) NOT NULL,
    valor_unitario  DECIMAL(18,6)  NOT NULL,
    importe         DECIMAL(18,6)  NOT NULL,
    descuento       DECIMAL(18,6)  NULL,
    objeto_imp      VARCHAR(5)     NULL,
    CONSTRAINT PK_Concepto PRIMARY KEY (id_concepto),
    CONSTRAINT FK_Concepto_Comprobante FOREIGN KEY (id_comprobante)
        REFERENCES dbo.Comprobante(id_comprobante) ON DELETE CASCADE
);
CREATE NONCLUSTERED INDEX IX_Concepto_comprobante ON dbo.Concepto (id_comprobante);

-- Impuestos (totales a nivel comprobante)
CREATE TABLE dbo.Impuestos (
    id_impuesto     INT IDENTITY(1,1) NOT NULL,
    id_comprobante  INT            NOT NULL,
    total_impuestos_trasladados DECIMAL(18,6) NULL,
    total_impuestos_retenidos   DECIMAL(18,6) NULL,
    CONSTRAINT PK_Impuestos PRIMARY KEY (id_impuesto),
    CONSTRAINT FK_Impuestos_Comprobante FOREIGN KEY (id_comprobante)
        REFERENCES dbo.Comprobante(id_comprobante) ON DELETE CASCADE
);
CREATE NONCLUSTERED INDEX IX_Impuestos_comprobante ON dbo.Impuestos (id_comprobante);

-- Traslado (impuestos trasladados a nivel comprobante)
CREATE TABLE dbo.Traslado (
    id_traslado     INT IDENTITY(1,1) NOT NULL,
    id_impuesto     INT            NOT NULL,
    base            DECIMAL(18,6)  NULL,
    impuesto        VARCHAR(5)     NOT NULL,
    tipo_factor     VARCHAR(10)    NOT NULL,
    tasa_o_cuota    DECIMAL(18,6)  NULL,
    importe         DECIMAL(18,6)  NULL,
    CONSTRAINT PK_Traslado PRIMARY KEY (id_traslado),
    CONSTRAINT FK_Traslado_Impuestos FOREIGN KEY (id_impuesto)
        REFERENCES dbo.Impuestos(id_impuesto) ON DELETE CASCADE
);

-- Retencion (impuestos retenidos a nivel comprobante)
CREATE TABLE dbo.Retencion (
    id_retencion    INT IDENTITY(1,1) NOT NULL,
    id_impuesto     INT            NOT NULL,
    base            DECIMAL(18,6)  NULL,
    impuesto        VARCHAR(5)     NOT NULL,
    tipo_factor     VARCHAR(10)    NOT NULL,
    tasa_o_cuota    DECIMAL(18,6)  NULL,
    importe         DECIMAL(18,6)  NULL,
    CONSTRAINT PK_Retencion PRIMARY KEY (id_retencion),
    CONSTRAINT FK_Retencion_Impuestos FOREIGN KEY (id_impuesto)
        REFERENCES dbo.Impuestos(id_impuesto) ON DELETE CASCADE
);

-- DetalleImpuesto (impuestos a nivel concepto/linea)
CREATE TABLE dbo.DetalleImpuesto (
    id_detalle      INT IDENTITY(1,1) NOT NULL,
    id_concepto     INT            NOT NULL,
    tipo            VARCHAR(10)    NOT NULL,  -- 'TRASLADO' o 'RETENCION'
    base            DECIMAL(18,6)  NULL,
    impuesto        VARCHAR(5)     NOT NULL,
    tipo_factor     VARCHAR(10)    NOT NULL,
    tasa_o_cuota    DECIMAL(18,6)  NULL,
    importe         DECIMAL(18,6)  NULL,
    CONSTRAINT PK_DetalleImpuesto PRIMARY KEY (id_detalle),
    CONSTRAINT FK_DetalleImpuesto_Concepto FOREIGN KEY (id_concepto)
        REFERENCES dbo.Concepto(id_concepto) ON DELETE CASCADE
);
CREATE NONCLUSTERED INDEX IX_DetalleImpuesto_concepto ON dbo.DetalleImpuesto (id_concepto);
CREATE NONCLUSTERED INDEX IX_DetalleImpuesto_tipo ON dbo.DetalleImpuesto (tipo);
