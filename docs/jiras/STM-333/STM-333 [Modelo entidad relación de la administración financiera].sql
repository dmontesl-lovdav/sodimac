-- Requiere la extensión uuid-ossp para generar UUIDs
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===============================
-- 1. Tabla: origin_catalog
-- Catálogo de orígenes de datos
-- ===============================
CREATE TABLE origin_catalog (
    origin_id     NUMERIC(3) PRIMARY KEY, -- Identificador único del origen
    name          VARCHAR(100) NOT NULL,  -- Nombre del origen
    description   VARCHAR(256),           -- Descripción del origen
    status        NUMERIC DEFAULT 1,      -- Estado del registro (1 = activo, 0 = inactivo)
    created_by    BIGINT,                 -- Identificador del usuario que creó el registro
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha y hora de creación
    updated_by    BIGINT,                 -- Identificador del usuario que actualizó el registro
    updated_at    TIMESTAMP               -- Fecha y hora de la última actualización
);

-- ===============================
-- 2. Tabla: status_catalog
-- Catálogo de estados de los procesos
-- ===============================
CREATE TABLE status_catalog (
    status        NUMERIC(2) PRIMARY KEY, -- Código del estado
    name          VARCHAR(50) NOT NULL,   -- Nombre del estado
    description   VARCHAR(256),           -- Descripción del estado
    created_by    BIGINT,                 -- Identificador del usuario que creó el registro
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha y hora de creación
    updated_by    BIGINT,                 -- Identificador del usuario que actualizó el registro
    updated_at    TIMESTAMP,              -- Fecha y hora de la última actualización

    CONSTRAINT chk_status_catalog_status 
        CHECK (status BETWEEN 0 AND 99)
);

-- ===============================
-- 3. Tabla: purchase_order
-- Órdenes de compra
-- ===============================
CREATE TABLE purchase_order (
    purchase_order_id     UUID PRIMARY KEY DEFAULT uuid_generate_v4(), -- Identificador único de la orden de compra
    supplier_number       BIGINT NOT NULL,          -- Número del proveedor (estandarizado a BIGINT)
    origin_id             NUMERIC(3),               -- Identificador del origen (FK)
    amount                NUMERIC(16,2),            -- Monto total de la orden
    status                NUMERIC(2),               -- Estado de la orden (FK)
    purchase_order_date   TIMESTAMP,                -- Fecha de la orden de compra
    created_by            BIGINT,                   -- Identificador del usuario que creó el registro
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha y hora de creación
    updated_by            BIGINT,                   -- Identificador del usuario que actualizó el registro
    updated_at            TIMESTAMP,                -- Fecha y hora de la última actualización

    CONSTRAINT fk_purchase_order_origin
        FOREIGN KEY (origin_id) REFERENCES origin_catalog(origin_id),

    CONSTRAINT fk_purchase_order_status
        FOREIGN KEY (status) REFERENCES status_catalog(status)
);

-- ===============================
-- 4. Tabla: rebate
-- Reembolsos
-- ===============================
CREATE TABLE rebate (
    rebate_id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(), -- Identificador único del reembolso
    document_number     VARCHAR(100) NOT NULL, -- Número del documento
    document_reference  VARCHAR(100),          -- Referencia del documento
    sap_document        VARCHAR(50),           -- Documento del sistema SAP
    supplier_number     BIGINT,                -- Número del proveedor (estandarizado a BIGINT)
    amount              NUMERIC(16,2),         -- Monto del reembolso
    origin_id           NUMERIC(3),            -- Identificador del origen del reembolso (corregido nombre)
    period_id           INTEGER,               -- Identificador del periodo
    due_date            TIMESTAMP,             -- Fecha de vencimiento
    posting_date        TIMESTAMP,             -- Fecha de contabilización
    status              NUMERIC(2),            -- Estado del reembolso
    created_by          BIGINT,                -- Identificador del usuario que creó el registro
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha y hora de creación
    updated_by          BIGINT,                -- Identificador del usuario que actualizó el registro
    updated_at          TIMESTAMP,             -- Fecha y hora de la última actualización
    
    CONSTRAINT fk_rebate_status
        FOREIGN KEY (status) REFERENCES status_catalog(status),

    CONSTRAINT fk_rebate_origin
        FOREIGN KEY (origin_id) REFERENCES origin_catalog(origin_id)
);

-- ===============================
-- 5. Tabla: stamping_rebate
-- Timbrado de reembolsos
-- ===============================
CREATE TABLE stamping_rebate (
    stamping_rebate_id      UUID PRIMARY KEY DEFAULT uuid_generate_v4(), -- Identificador único del estampado
    rebate_id               UUID,           -- Clave foránea del reembolso al que pertenece
    uuid                    UUID,           -- UUID del timbre fiscal (corregido tipo de dato)
    supplier_number         BIGINT,         -- Número del proveedor (estandarizado a BIGINT)
    status                  NUMERIC(2),     -- Estado del timbrado (FK)
    created_by              BIGINT,         -- Identificador del usuario que creó el registro
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha y hora de creación
    updated_by              BIGINT,         -- Identificador del usuario que actualizó el registro
    updated_at              TIMESTAMP,      -- Fecha y hora de la última actualización
    
    CONSTRAINT fk_stamping_rebate_rebate
        FOREIGN KEY (rebate_id)
        REFERENCES rebate(rebate_id),

    CONSTRAINT fk_stamping_rebate_status
        FOREIGN KEY (status) REFERENCES status_catalog(status)
);

-- ===============================
-- 6. Tabla: sap_document
-- Documentos del sistema SAP
-- ===============================
CREATE TABLE sap_document (
    sap_document_id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(), -- Identificador único del documento SAP
    document_number         VARCHAR(100), -- Número del documento
    document_reference      VARCHAR(100), -- Referencia del documento
    supplier_number         BIGINT, -- Número del proveedor (estandarizado a BIGINT)
    amount                  NUMERIC(16,2), -- Monto del documento
    sap_code                VARCHAR(10), -- Código del sistema SAP
    message                 VARCHAR(254), -- Mensaje de error o de proceso
    sap_status              NUMERIC(2), -- Estado del documento SAP (estandarizado tipo)
    document_type           VARCHAR(5), -- Tipo de documento
    created_by              BIGINT, -- Identificador del usuario que creó el registro
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha y hora de creación
    updated_by              BIGINT, -- Identificador del usuario que actualizó el registro
    updated_at              TIMESTAMP -- Fecha y hora de la última actualización
);

-- ===============================
-- 7. Tabla: reception
-- Recepciones de mercancía
-- ===============================
CREATE TABLE reception (
    reception_id        UUID PRIMARY KEY DEFAULT uuid_generate_v4(), -- Identificador único de la recepción
    purchase_order_id   UUID,           -- Identificador de la orden de compra asociada (FK)
    origin_id           NUMERIC(3),     -- Identificador del origen (FK)
    destination_id      NUMERIC(3),     -- Identificador del destino (FK)
    amount              NUMERIC(16,2),  -- Monto total de la recepción
    status              NUMERIC(2),     -- Estado de la recepción (FK)
    comment             VARCHAR(256),   -- Comentarios adicionales
    reception_date      DATE,           -- Fecha de recepción
    created_by          BIGINT,         -- Identificador del usuario que creó el registro
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha y hora de creación
    updated_by          BIGINT,         -- Identificador del usuario que actualizó el registro
    updated_at          TIMESTAMP,      -- Fecha y hora de la última actualización

    CONSTRAINT fk_reception_purchase_order
        FOREIGN KEY (purchase_order_id) REFERENCES purchase_order(purchase_order_id),

    CONSTRAINT fk_reception_origin
        FOREIGN KEY (origin_id) REFERENCES origin_catalog(origin_id),

    CONSTRAINT fk_reception_destination
        FOREIGN KEY (destination_id) REFERENCES origin_catalog(origin_id),
    
    CONSTRAINT fk_reception_status
        FOREIGN KEY (status) REFERENCES status_catalog(status)
);

-- ===============================
-- 8. Tabla: reception_sku
-- Detalle de SKUs en recepciones
-- ===============================
CREATE TABLE reception_sku (
    reception_sku_id    UUID PRIMARY KEY DEFAULT uuid_generate_v4(), -- Identificador único del SKU de recepción
    reception_id        UUID NOT NULL,  -- Identificador de la recepción a la que pertenece (FK)
    sku                 VARCHAR(15) NOT NULL, -- Código del producto o SKU
    description         VARCHAR(256) NOT NULL, -- Descripción del producto
    quantity            NUMERIC(20,6),  -- Cantidad del producto
    unit_cost           NUMERIC(16,2),  -- Costo unitario del producto
    total_cost          NUMERIC(26,2),  -- Costo total del producto (corregida precision)
    status              NUMERIC(2),     -- Estado del SKU en la recepción (FK)
    created_by          BIGINT,         -- Identificador del usuario que creó el registro
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha y hora de creación
    updated_by          BIGINT,         -- Identificador del usuario que actualizó el registro
    updated_at          TIMESTAMP,      -- Fecha y hora de la última actualización

    CONSTRAINT fk_reception_sku_reception
        FOREIGN KEY (reception_id) REFERENCES reception(reception_id),
    
    CONSTRAINT fk_reception_sku_status
        FOREIGN KEY (status) REFERENCES status_catalog(status)
);

-- ===============================
-- 9. Tabla: shipping_guide
-- Guías de envío
-- ===============================
CREATE TABLE shipping_guide (
    shipping_guide_id       UUID PRIMARY KEY DEFAULT uuid_generate_v4(), -- Identificador único de la guía de envío
    supplier_number         BIGINT,                 -- Número del proveedor (estandarizado a BIGINT)
    plate                   VARCHAR(30),            -- Placa del vehículo de transporte
    trailer_plate           VARCHAR(30),            -- Placa del remolque
    origin_id               NUMERIC(3),             -- Identificador del origen (FK)
    delivery_type           VARCHAR(10),            -- Tipo de entrega
    amount                  NUMERIC(16,2),          -- Monto total de la guía
    status                  NUMERIC(2),             -- Estado de la guía (FK)
    comment                 VARCHAR(100),           -- Comentarios adicionales
    delivery_date           TIMESTAMP,              -- Fecha de entrega
    shipping_date           TIMESTAMP,              -- Fecha de envío
    created_by              BIGINT,                 -- Identificador del usuario que creó el registro
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha y hora de creación
    updated_by              BIGINT,                 -- Identificador del usuario que actualizó el registro
    updated_at              TIMESTAMP,              -- Fecha y hora de la última actualización
    
    CONSTRAINT fk_shipping_guide_origin
        FOREIGN KEY (origin_id) REFERENCES origin_catalog(origin_id),
        
    CONSTRAINT fk_shipping_guide_status
        FOREIGN KEY (status) REFERENCES status_catalog(status)
);

-- ===============================
-- 10. Tabla: shipping_guide_document
-- Documentos asociados a las guías de envío
-- ===============================
CREATE TABLE shipping_guide_document (
    shipping_guide_document_id  UUID PRIMARY KEY DEFAULT uuid_generate_v4(), -- Identificador único del documento de la guía
    shipping_guide_id           UUID,                   -- Identificador de la guía de envío (FK)
    file_name                   VARCHAR(100),           -- Nombre del archivo
    file_type                   SMALLINT,               -- Tipo de archivo
    status                      NUMERIC(2),             -- Estado del documento (FK)
    created_by                  BIGINT,                 -- Identificador del usuario que creó el registro
    created_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha y hora de creación
    updated_by                  BIGINT,                 -- Identificador del usuario que actualizó el registro
    updated_at                  TIMESTAMP,              -- Fecha y hora de la última actualización
    
    CONSTRAINT fk_shipping_guide_document_guide
        FOREIGN KEY (shipping_guide_id)
        REFERENCES shipping_guide(shipping_guide_id),

    CONSTRAINT fk_shipping_guide_document_status
        FOREIGN KEY (status) REFERENCES status_catalog(status)
);

-- ===============================
-- 11. Tabla: shipping_guide_purchase_order
-- Relación entre guías de envío y órdenes de compra
-- ===============================
CREATE TABLE shipping_guide_purchase_order (
    shipping_guide_purchase_order_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), -- Identificador único de la relación
    shipping_guide_id       UUID NOT NULL,          -- Identificador de la guía de envío (FK)
    purchase_order_id       UUID NOT NULL,          -- Identificador de la orden de compra (FK)
    created_by              BIGINT,                 -- Identificador del usuario que creó el registro
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha y hora de creación
    updated_by              BIGINT,                 -- Identificador del usuario que actualizó el registro
    updated_at              TIMESTAMP,              -- Fecha y hora de la última actualización

    CONSTRAINT fk_sgpo_shipping_guide
        FOREIGN KEY (shipping_guide_id) REFERENCES shipping_guide(shipping_guide_id),
    
    CONSTRAINT fk_sgpo_purchase_order
        FOREIGN KEY (purchase_order_id) REFERENCES purchase_order(purchase_order_id)
);

-- ===============================
-- 12. Tabla: supplier_block
-- Bloqueos de proveedores
-- ===============================
CREATE TABLE supplier_block (
    supplier_block_id       UUID PRIMARY KEY DEFAULT uuid_generate_v4(), -- Identificador único del bloqueo
    supplier_number         BIGINT,                 -- Número del proveedor (estandarizado a BIGINT)
    start_date              TIMESTAMP,              -- Fecha de inicio del bloqueo
    end_date                TIMESTAMP,              -- Fecha de finalización del bloqueo
    status                  NUMERIC(2),             -- Estado del bloqueo (FK)
    created_by              BIGINT,                 -- Identificador del usuario que creó el registro
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha y hora de creación
    updated_by              BIGINT,                 -- Identificador del usuario que actualizó el registro
    updated_at              TIMESTAMP,              -- Fecha y hora de la última actualización
    
    CONSTRAINT fk_supplier_block_status
        FOREIGN KEY (status) REFERENCES status_catalog(status),

    CONSTRAINT chk_supplier_block_dates 
        CHECK (start_date < end_date)
);

-- ===============================
-- ÍNDICES PARA MEJORAR RENDIMIENTO
-- ===============================

-- Índices para purchase_order
CREATE INDEX idx_purchase_order_supplier_number ON purchase_order(supplier_number);
CREATE INDEX idx_purchase_order_origin_id ON purchase_order(origin_id);
CREATE INDEX idx_purchase_order_status ON purchase_order(status);
CREATE INDEX idx_purchase_order_order_date ON purchase_order(purchase_order_date);

-- Índices para rebate
CREATE INDEX idx_rebate_document_number ON rebate(document_number);
CREATE INDEX idx_rebate_supplier_number ON rebate(supplier_number);
CREATE INDEX idx_rebate_status ON rebate(status);
CREATE INDEX idx_rebate_origin_id ON rebate(origin_id);

-- Índices para stamping_rebate
CREATE INDEX idx_stamping_rebate_rebate_id ON stamping_rebate(rebate_id);
CREATE INDEX idx_stamping_rebate_supplier_number ON stamping_rebate(supplier_number);
CREATE INDEX idx_stamping_rebate_status ON stamping_rebate(status);

-- Índices para sap_document
CREATE INDEX idx_sap_document_document_number ON sap_document(document_number);
CREATE INDEX idx_sap_document_supplier_number ON sap_document(supplier_number);
CREATE INDEX idx_sap_document_sap_status ON sap_document(sap_status);
CREATE INDEX idx_sap_document_document_type ON sap_document(document_type);

-- Índices para reception
CREATE INDEX idx_reception_purchase_order_id ON reception(purchase_order_id);
CREATE INDEX idx_reception_origin_id ON reception(origin_id);
CREATE INDEX idx_reception_destination_id ON reception(destination_id);
CREATE INDEX idx_reception_status ON reception(status);
CREATE INDEX idx_reception_reception_date ON reception(reception_date);

-- Índices para reception_sku
CREATE INDEX idx_reception_sku_reception_id ON reception_sku(reception_id);
CREATE INDEX idx_reception_sku_sku ON reception_sku(sku);
CREATE INDEX idx_reception_sku_status ON reception_sku(status);

-- Índices para shipping_guide
CREATE INDEX idx_shipping_guide_supplier_number ON shipping_guide(supplier_number);
CREATE INDEX idx_shipping_guide_origin_id ON shipping_guide(origin_id);
CREATE INDEX idx_shipping_guide_status ON shipping_guide(status);
CREATE INDEX idx_shipping_guide_delivery_date ON shipping_guide(delivery_date);
CREATE INDEX idx_shipping_guide_shipping_date ON shipping_guide(shipping_date);

-- Índices para shipping_guide_document
CREATE INDEX idx_shipping_guide_document_guide_id ON shipping_guide_document(shipping_guide_id);
CREATE INDEX idx_shipping_guide_document_status ON shipping_guide_document(status);
CREATE INDEX idx_shipping_guide_document_file_type ON shipping_guide_document(file_type);

-- Índices para shipping_guide_purchase_order
CREATE INDEX idx_sgpo_shipping_guide_id ON shipping_guide_purchase_order(shipping_guide_id);
CREATE INDEX idx_sgpo_purchase_order_id ON shipping_guide_purchase_order(purchase_order_id);

-- Índices para supplier_block
CREATE INDEX idx_supplier_block_supplier_number ON supplier_block(supplier_number);
CREATE INDEX idx_supplier_block_status ON supplier_block(status);
CREATE INDEX idx_supplier_block_start_date ON supplier_block(start_date);
CREATE INDEX idx_supplier_block_end_date ON supplier_block(end_date);