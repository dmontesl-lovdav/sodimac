-- ============================================================================
-- CATALOGOS API - Esquema de Proveedores
-- Esquema: shared_catalogs
-- Descripcion: Tablas para gestion del catalogo de proveedores y bloqueos
-- Tickets: STM-1225, STM-1224
-- Fecha: 2025-12-10
-- Actualizado: 2025-12-12 - Agregado supplier_block (STM-1224)
-- ============================================================================

-- ============================================================================
-- Tabla: supplier_type
-- Descripcion: Catalogo de tipos de proveedor (Nacional, Internacional, etc.)
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.supplier_type (
    id SERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(100) NOT NULL,
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

COMMENT ON TABLE shared_catalogs.supplier_type IS 'Catalogo de tipos de proveedor del sistema';
COMMENT ON COLUMN shared_catalogs.supplier_type.id IS 'Identificador unico autoincremental';
COMMENT ON COLUMN shared_catalogs.supplier_type.code IS 'Codigo unico del tipo de proveedor (NAC, INT, etc.)';
COMMENT ON COLUMN shared_catalogs.supplier_type.description IS 'Descripcion del tipo de proveedor';
COMMENT ON COLUMN shared_catalogs.supplier_type.status IS 'Estado del registro: 1=Activo, 0=Inactivo';
COMMENT ON COLUMN shared_catalogs.supplier_type.created_at IS 'Fecha de creacion del registro';
COMMENT ON COLUMN shared_catalogs.supplier_type.created_by IS 'Usuario que creo el registro';
COMMENT ON COLUMN shared_catalogs.supplier_type.updated_at IS 'Fecha de ultima actualizacion';
COMMENT ON COLUMN shared_catalogs.supplier_type.updated_by IS 'Usuario que actualizo el registro';

CREATE INDEX IF NOT EXISTS idx_supplier_type_code ON shared_catalogs.supplier_type(code);
CREATE INDEX IF NOT EXISTS idx_supplier_type_status ON shared_catalogs.supplier_type(status);

-- ============================================================================
-- Tabla: payment_condition
-- Descripcion: Catalogo de condiciones de pago disponibles para proveedores
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.payment_condition (
    id SERIAL PRIMARY KEY,
    supplier_number VARCHAR(20),
    condition_name VARCHAR(100) NOT NULL,
    days INTEGER NOT NULL DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

COMMENT ON TABLE shared_catalogs.payment_condition IS 'Catalogo de condiciones de pago para proveedores';
COMMENT ON COLUMN shared_catalogs.payment_condition.id IS 'Identificador unico autoincremental';
COMMENT ON COLUMN shared_catalogs.payment_condition.supplier_number IS 'Numero de proveedor asociado (opcional, para condiciones especificas)';
COMMENT ON COLUMN shared_catalogs.payment_condition.condition_name IS 'Nombre de la condicion de pago';
COMMENT ON COLUMN shared_catalogs.payment_condition.days IS 'Dias de plazo para el pago';
COMMENT ON COLUMN shared_catalogs.payment_condition.status IS 'Estado del registro: 1=Activo, 0=Inactivo';
COMMENT ON COLUMN shared_catalogs.payment_condition.created_at IS 'Fecha de creacion del registro';
COMMENT ON COLUMN shared_catalogs.payment_condition.created_by IS 'Usuario que creo el registro';
COMMENT ON COLUMN shared_catalogs.payment_condition.updated_at IS 'Fecha de ultima actualizacion';
COMMENT ON COLUMN shared_catalogs.payment_condition.updated_by IS 'Usuario que actualizo el registro';

CREATE INDEX IF NOT EXISTS idx_payment_condition_supplier ON shared_catalogs.payment_condition(supplier_number);
CREATE INDEX IF NOT EXISTS idx_payment_condition_status ON shared_catalogs.payment_condition(status);

-- ============================================================================
-- Tabla: supplier
-- Descripcion: Catalogo maestro de proveedores del sistema
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.supplier (
    id SERIAL PRIMARY KEY,
    supplier_number VARCHAR(20) NOT NULL UNIQUE,
    rfc VARCHAR(13) NOT NULL,
    business_name VARCHAR(255) NOT NULL,
    supplier_type_id INTEGER,
    logo VARCHAR(500),
    payment_condition_id INTEGER,
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),

    CONSTRAINT fk_supplier_type
        FOREIGN KEY (supplier_type_id)
        REFERENCES shared_catalogs.supplier_type(id),
    CONSTRAINT fk_supplier_payment_condition
        FOREIGN KEY (payment_condition_id)
        REFERENCES shared_catalogs.payment_condition(id)
);

COMMENT ON TABLE shared_catalogs.supplier IS 'Catalogo maestro de proveedores del sistema FBC';
COMMENT ON COLUMN shared_catalogs.supplier.id IS 'Identificador unico autoincremental';
COMMENT ON COLUMN shared_catalogs.supplier.supplier_number IS 'Numero unico de proveedor (codigo de negocio)';
COMMENT ON COLUMN shared_catalogs.supplier.rfc IS 'RFC del proveedor (12 o 13 caracteres)';
COMMENT ON COLUMN shared_catalogs.supplier.business_name IS 'Razon social o nombre del proveedor';
COMMENT ON COLUMN shared_catalogs.supplier.supplier_type_id IS 'FK al tipo de proveedor';
COMMENT ON COLUMN shared_catalogs.supplier.logo IS 'URL o path del logo del proveedor';
COMMENT ON COLUMN shared_catalogs.supplier.payment_condition_id IS 'FK a la condicion de pago del proveedor';
COMMENT ON COLUMN shared_catalogs.supplier.status IS 'Estado del registro: 1=Activo, 0=Inactivo';
COMMENT ON COLUMN shared_catalogs.supplier.created_at IS 'Fecha de creacion del registro';
COMMENT ON COLUMN shared_catalogs.supplier.created_by IS 'Usuario que creo el registro';
COMMENT ON COLUMN shared_catalogs.supplier.updated_at IS 'Fecha de ultima actualizacion';
COMMENT ON COLUMN shared_catalogs.supplier.updated_by IS 'Usuario que actualizo el registro';

CREATE INDEX IF NOT EXISTS idx_supplier_number ON shared_catalogs.supplier(supplier_number);
CREATE INDEX IF NOT EXISTS idx_supplier_rfc ON shared_catalogs.supplier(rfc);
CREATE INDEX IF NOT EXISTS idx_supplier_type ON shared_catalogs.supplier(supplier_type_id);
CREATE INDEX IF NOT EXISTS idx_supplier_status ON shared_catalogs.supplier(status);

-- ============================================================================
-- Tabla: supplier_block (STM-1224)
-- Descripcion: Bloqueos temporales de proveedores por rango de fechas
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.supplier_block (
    id SERIAL PRIMARY KEY,
    supplier_number VARCHAR(20) NOT NULL,
    valid_from DATE NOT NULL,
    valid_to DATE NOT NULL,
    block_reason VARCHAR(255),
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),

    CONSTRAINT chk_supplier_block_dates CHECK (valid_to >= valid_from)
);

COMMENT ON TABLE shared_catalogs.supplier_block IS 'Bloqueos temporales de proveedores por rango de fechas (STM-1224)';
COMMENT ON COLUMN shared_catalogs.supplier_block.id IS 'Identificador unico autoincremental';
COMMENT ON COLUMN shared_catalogs.supplier_block.supplier_number IS 'Numero de proveedor SAP bloqueado';
COMMENT ON COLUMN shared_catalogs.supplier_block.valid_from IS 'Fecha de inicio de vigencia del bloqueo';
COMMENT ON COLUMN shared_catalogs.supplier_block.valid_to IS 'Fecha de fin de vigencia del bloqueo';
COMMENT ON COLUMN shared_catalogs.supplier_block.block_reason IS 'Motivo del bloqueo';
COMMENT ON COLUMN shared_catalogs.supplier_block.status IS 'Estado del registro: 1=Activo, 0=Inactivo';
COMMENT ON COLUMN shared_catalogs.supplier_block.created_at IS 'Fecha de creacion del registro';
COMMENT ON COLUMN shared_catalogs.supplier_block.created_by IS 'Usuario que creo el registro';
COMMENT ON COLUMN shared_catalogs.supplier_block.updated_at IS 'Fecha de ultima actualizacion';
COMMENT ON COLUMN shared_catalogs.supplier_block.updated_by IS 'Usuario que actualizo el registro';

CREATE INDEX IF NOT EXISTS idx_supplier_block_number ON shared_catalogs.supplier_block(supplier_number);
CREATE INDEX IF NOT EXISTS idx_supplier_block_dates ON shared_catalogs.supplier_block(valid_from, valid_to);
CREATE INDEX IF NOT EXISTS idx_supplier_block_status ON shared_catalogs.supplier_block(status);

-- ============================================================================
-- Tabla: cost_center
-- Descripcion: Catalogo de centros de costo
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.cost_center (
    id SERIAL PRIMARY KEY,
    cost_center_code VARCHAR(20) NOT NULL UNIQUE,
    sap_branch VARCHAR(20),
    sodimac_branch VARCHAR(20),
    description VARCHAR(255),
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

COMMENT ON TABLE shared_catalogs.cost_center IS 'Catalogo de centros de costo';
COMMENT ON COLUMN shared_catalogs.cost_center.id IS 'Identificador unico autoincremental';
COMMENT ON COLUMN shared_catalogs.cost_center.cost_center_code IS 'Codigo del centro de costo';
COMMENT ON COLUMN shared_catalogs.cost_center.sap_branch IS 'Codigo de sucursal en SAP';
COMMENT ON COLUMN shared_catalogs.cost_center.sodimac_branch IS 'Codigo de sucursal en Sodimac';
COMMENT ON COLUMN shared_catalogs.cost_center.description IS 'Descripcion del centro de costo';
COMMENT ON COLUMN shared_catalogs.cost_center.status IS 'Estado del registro: 1=Activo, 0=Inactivo';
COMMENT ON COLUMN shared_catalogs.cost_center.created_at IS 'Fecha de creacion del registro';
COMMENT ON COLUMN shared_catalogs.cost_center.created_by IS 'Usuario que creo el registro';
COMMENT ON COLUMN shared_catalogs.cost_center.updated_at IS 'Fecha de ultima actualizacion';
COMMENT ON COLUMN shared_catalogs.cost_center.updated_by IS 'Usuario que actualizo el registro';

CREATE INDEX IF NOT EXISTS idx_cost_center_code ON shared_catalogs.cost_center(cost_center_code);
CREATE INDEX IF NOT EXISTS idx_cost_center_sap_branch ON shared_catalogs.cost_center(sap_branch);
CREATE INDEX IF NOT EXISTS idx_cost_center_status ON shared_catalogs.cost_center(status);

-- ============================================================================
-- Tabla: benefit_center
-- Descripcion: Catalogo de centros de beneficio
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.benefit_center (
    id SERIAL PRIMARY KEY,
    benefit_center_code VARCHAR(20) NOT NULL UNIQUE,
    sap_branch VARCHAR(20),
    sodimac_branch VARCHAR(20),
    description VARCHAR(255),
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

COMMENT ON TABLE shared_catalogs.benefit_center IS 'Catalogo de centros de beneficio';
COMMENT ON COLUMN shared_catalogs.benefit_center.id IS 'Identificador unico autoincremental';
COMMENT ON COLUMN shared_catalogs.benefit_center.benefit_center_code IS 'Codigo del centro de beneficio';
COMMENT ON COLUMN shared_catalogs.benefit_center.sap_branch IS 'Codigo de sucursal en SAP';
COMMENT ON COLUMN shared_catalogs.benefit_center.sodimac_branch IS 'Codigo de sucursal en Sodimac';
COMMENT ON COLUMN shared_catalogs.benefit_center.description IS 'Descripcion del centro de beneficio';
COMMENT ON COLUMN shared_catalogs.benefit_center.status IS 'Estado del registro: 1=Activo, 0=Inactivo';
COMMENT ON COLUMN shared_catalogs.benefit_center.created_at IS 'Fecha de creacion del registro';
COMMENT ON COLUMN shared_catalogs.benefit_center.created_by IS 'Usuario que creo el registro';
COMMENT ON COLUMN shared_catalogs.benefit_center.updated_at IS 'Fecha de ultima actualizacion';
COMMENT ON COLUMN shared_catalogs.benefit_center.updated_by IS 'Usuario que actualizo el registro';

CREATE INDEX IF NOT EXISTS idx_benefit_center_code ON shared_catalogs.benefit_center(benefit_center_code);
CREATE INDEX IF NOT EXISTS idx_benefit_center_sap_branch ON shared_catalogs.benefit_center(sap_branch);
CREATE INDEX IF NOT EXISTS idx_benefit_center_status ON shared_catalogs.benefit_center(status);

-- ============================================================================
-- Tabla: accounting_account
-- Descripcion: Catalogo de cuentas contables
-- ============================================================================
CREATE TABLE IF NOT EXISTS shared_catalogs.accounting_account (
    id SERIAL PRIMARY KEY,
    benefit_center_code VARCHAR(20) NOT NULL,
    sap_branch VARCHAR(20),
    sodimac_branch VARCHAR(20),
    description VARCHAR(255),
    status INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

COMMENT ON TABLE shared_catalogs.accounting_account IS 'Catalogo de cuentas contables';
COMMENT ON COLUMN shared_catalogs.accounting_account.id IS 'Identificador unico autoincremental';
COMMENT ON COLUMN shared_catalogs.accounting_account.benefit_center_code IS 'Codigo del centro de beneficio asociado';
COMMENT ON COLUMN shared_catalogs.accounting_account.sap_branch IS 'Codigo de sucursal en SAP';
COMMENT ON COLUMN shared_catalogs.accounting_account.sodimac_branch IS 'Codigo de sucursal en Sodimac';
COMMENT ON COLUMN shared_catalogs.accounting_account.description IS 'Descripcion de la cuenta contable';
COMMENT ON COLUMN shared_catalogs.accounting_account.status IS 'Estado del registro: 1=Activo, 0=Inactivo';
COMMENT ON COLUMN shared_catalogs.accounting_account.created_at IS 'Fecha de creacion del registro';
COMMENT ON COLUMN shared_catalogs.accounting_account.created_by IS 'Usuario que creo el registro';
COMMENT ON COLUMN shared_catalogs.accounting_account.updated_at IS 'Fecha de ultima actualizacion';
COMMENT ON COLUMN shared_catalogs.accounting_account.updated_by IS 'Usuario que actualizo el registro';

CREATE INDEX IF NOT EXISTS idx_accounting_account_benefit_center ON shared_catalogs.accounting_account(benefit_center_code);
CREATE INDEX IF NOT EXISTS idx_accounting_account_sap_branch ON shared_catalogs.accounting_account(sap_branch);
CREATE INDEX IF NOT EXISTS idx_accounting_account_status ON shared_catalogs.accounting_account(status);

-- ============================================================================
-- DATOS INICIALES
-- ============================================================================

-- Tipos de proveedor
INSERT INTO shared_catalogs.supplier_type (code, description, created_by, status)
VALUES
    ('NAC', 'Proveedor Nacional', 'system', 1),
    ('INT', 'Proveedor Internacional', 'system', 1),
    ('MIX', 'Proveedor Mixto', 'system', 1)
ON CONFLICT (code) DO NOTHING;

-- Condiciones de pago estandar
INSERT INTO shared_catalogs.payment_condition (condition_name, days, created_by, status)
VALUES
    ('Contado', 0, 'system', 1),
    ('15 dias', 15, 'system', 1),
    ('30 dias', 30, 'system', 1),
    ('45 dias', 45, 'system', 1),
    ('60 dias', 60, 'system', 1),
    ('90 dias', 90, 'system', 1)
ON CONFLICT DO NOTHING;

-- Centros de costo
INSERT INTO shared_catalogs.cost_center (cost_center_code, sap_branch, sodimac_branch, description, created_by, status)
VALUES
    ('CC001', 'SAP001', 'SOD001', 'Centro de Costo Principal CDMX', 'system', 1),
    ('CC002', 'SAP002', 'SOD002', 'Centro de Costo Guadalajara', 'system', 1),
    ('CC003', 'SAP003', 'SOD003', 'Centro de Costo Monterrey', 'system', 1),
    ('CC004', 'SAP004', 'SOD004', 'Centro de Costo Puebla', 'system', 1),
    ('CC005', 'SAP005', 'SOD005', 'Centro de Costo Queretaro', 'system', 1)
ON CONFLICT (cost_center_code) DO NOTHING;

-- Centros de beneficio
INSERT INTO shared_catalogs.benefit_center (benefit_center_code, sap_branch, sodimac_branch, description, created_by, status)
VALUES
    ('CB001', 'SAP001', 'SOD001', 'Centro de Beneficio Retail', 'system', 1),
    ('CB002', 'SAP002', 'SOD002', 'Centro de Beneficio Mayoreo', 'system', 1),
    ('CB003', 'SAP003', 'SOD003', 'Centro de Beneficio E-Commerce', 'system', 1),
    ('CB004', 'SAP004', 'SOD004', 'Centro de Beneficio Servicios', 'system', 1),
    ('CB005', 'SAP005', 'SOD005', 'Centro de Beneficio Corporativo', 'system', 1)
ON CONFLICT (benefit_center_code) DO NOTHING;

-- Cuentas contables
INSERT INTO shared_catalogs.accounting_account (benefit_center_code, sap_branch, sodimac_branch, description, created_by, status)
VALUES
    ('CB001', 'SAP001', 'SOD001', 'Cuenta Contable Gastos Operativos', 'system', 1),
    ('CB001', 'SAP001', 'SOD001', 'Cuenta Contable Inventarios', 'system', 1),
    ('CB002', 'SAP002', 'SOD002', 'Cuenta Contable Ventas Mayoreo', 'system', 1),
    ('CB003', 'SAP003', 'SOD003', 'Cuenta Contable Ventas Online', 'system', 1),
    ('CB004', 'SAP004', 'SOD004', 'Cuenta Contable Servicios Profesionales', 'system', 1),
    ('CB005', 'SAP005', 'SOD005', 'Cuenta Contable Gastos Administrativos', 'system', 1)
ON CONFLICT DO NOTHING;

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
