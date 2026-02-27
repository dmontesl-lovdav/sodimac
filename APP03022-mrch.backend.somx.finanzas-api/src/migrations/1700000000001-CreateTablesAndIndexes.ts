import { MigrationInterface, QueryRunner } from 'typeorm';

/**
 * Migración V2 - CREACIÓN COMPLETA DEL ESQUEMA
 * Creación de todas las tablas e índices del sistema
 * Incluye esquema CFDI y módulo fiscal
 * Equivalente a: V2__Create_tables_and_indexes.sql de Flyway
 */
export class CreateTablesAndIndexes1700000000001 implements MigrationInterface {
    public async up(queryRunner: QueryRunner): Promise<void> {
        // NOTA: uuid-ossp extension ya debe existir en la base de datos
        // NO intentamos crearla para evitar problemas de permisos

        // ===============================
        // ESQUEMA CFDI - TABLAS BASE
        // ===============================

        // 1. Tabla: pac_catalog
        await queryRunner.query(`
            CREATE TABLE pac_catalog (
                pac_id SERIAL PRIMARY KEY,
                name VARCHAR(50) NOT NULL,
                description VARCHAR(254),
                url VARCHAR(254),
                license VARCHAR(254),
                valid_from DATE,
                valid_to DATE,
                catalog_msg_id NUMERIC(4),
                status INTEGER DEFAULT 1,

                created_by BIGINT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_by BIGINT,
                updated_at TIMESTAMP,

                CONSTRAINT chk_pac_status CHECK (status IN (0, 1)),
                CONSTRAINT chk_pac_valid_dates CHECK (valid_from IS NULL OR valid_to IS NULL OR valid_from <= valid_to)
            )
        `);

        await queryRunner.query(`COMMENT ON TABLE pac_catalog IS 'Catálogo de Proveedores Autorizados de Certificación del SAT'`);

        // 2. Tabla: version_catalog
        await queryRunner.query(`
            CREATE TABLE version_catalog (
                version_id SERIAL PRIMARY KEY,
                name VARCHAR(50) NOT NULL,
                description VARCHAR(254),
                version NUMERIC(7,3) NOT NULL,
                document_type CHAR(1) NOT NULL,
                pac_id INTEGER NOT NULL,
                valid_from TIMESTAMP,
                valid_to TIMESTAMP,
                structure_url VARCHAR(254),
                status INTEGER DEFAULT 1,

                created_by BIGINT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_by BIGINT,
                updated_at TIMESTAMP,

                CONSTRAINT fk_version_catalog_pac
                    FOREIGN KEY (pac_id) REFERENCES pac_catalog(pac_id),
                CONSTRAINT chk_version_status CHECK (status IN (0, 1)),
                CONSTRAINT chk_version_valid_dates CHECK (valid_from IS NULL OR valid_to IS NULL OR valid_from <= valid_to),
                CONSTRAINT chk_version_document_type CHECK (document_type IN ('I', 'E', 'T', 'N', 'P'))
            )
        `);

        await queryRunner.query(`COMMENT ON TABLE version_catalog IS 'Catálogo de versiones de CFDI soportadas por el sistema'`);

        // ===============================
        // NOTA: 12 Tablas CFDI ELIMINADAS
        // ===============================
        // Las siguientes tablas fueron removidas de finanzas-api porque pertenecen a fiscal-api:
        // - issuer, receiver, authorized_receiver_catalog (Catálogos CFDI)
        // - invoice, addendum, related_cfdi (Facturas CFDI)
        // - payments, payment, related_documents, equivalence_dr, totals (Complementos de Pago)
        // - log (Bitácora de timbrado)
        //
        // Estas tablas están implementadas en fiscal-api (schema: tenant_fiscal)
        // finanzas-api NO debe duplicar el esquema CFDI
        // ===============================

        // ===============================
        // MÓDULO FISCAL - TABLAS ADICIONALES
        // ===============================

        // 15. Tabla: stamped_rebate
        await queryRunner.query(`
            CREATE TABLE stamped_rebate (
                stamped_rebate_uuid   UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
                document_number       VARCHAR(100) NOT NULL,
                reference_number      VARCHAR(100) NOT NULL,
                status                INTEGER      NOT NULL DEFAULT 1,

                created_by            BIGINT,
                created_at            TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                updated_by            BIGINT,
                updated_at            TIMESTAMP,

                CONSTRAINT chk_stamped_rebate_status CHECK (status IN (0, 1, 2, 3)),
                CONSTRAINT uq_stamped_rebate_doc UNIQUE (document_number)
            )
        `);

        await queryRunner.query(`COMMENT ON TABLE stamped_rebate IS 'Descuentos y rebates timbrados fiscalmente'`);

        // 16. Tabla: rebate
        await queryRunner.query(`
            CREATE TABLE rebate (
                rebate_uuid           UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
                document_number       VARCHAR(100) NOT NULL,
                reference_number      VARCHAR(100) NOT NULL,
                sap_document          VARCHAR(50)  NOT NULL,
                vendor_number         INTEGER      NOT NULL,
                amount                DECIMAL(15,2) NOT NULL,
                source                INTEGER      NOT NULL,
                period_id             INTEGER      NOT NULL,
                due_date              DATE         NOT NULL,
                posting_date          DATE         NOT NULL,
                status                INTEGER      NOT NULL DEFAULT 1,

                created_by            BIGINT,
                created_at            TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                updated_by            BIGINT,
                updated_at            TIMESTAMP,

                CONSTRAINT fk_rebate_stamped_rebate
                    FOREIGN KEY (document_number) REFERENCES stamped_rebate(document_number),
                CONSTRAINT chk_rebate_amount CHECK (amount >= 0),
                CONSTRAINT chk_rebate_status CHECK (status IN (0, 1, 2, 3))
            )
        `);

        await queryRunner.query(`COMMENT ON TABLE rebate IS 'Descuentos y rebates aplicados a proveedores'`);

        // 17. Tabla: sap_document
        await queryRunner.query(`
            CREATE TABLE sap_document (
                sap_document_uuid     UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
                document_number       VARCHAR(100) NOT NULL,
                reference_number      VARCHAR(100) NOT NULL,
                vendor_number         INTEGER      NOT NULL,
                amount                DECIMAL(15,2) NOT NULL,
                source                INTEGER      NOT NULL,
                doc_sap               VARCHAR(15)  NOT NULL,
                message               VARCHAR(254),
                sap_status            INTEGER      NOT NULL DEFAULT 1,
                document_type         VARCHAR(5)   NOT NULL,

                created_by            BIGINT,
                created_at            TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                updated_by            BIGINT,
                updated_at            TIMESTAMP,

                CONSTRAINT chk_sap_document_amount CHECK (amount >= 0),
                CONSTRAINT chk_sap_document_status CHECK (sap_status IN (0, 1, 2, 3)),
                CONSTRAINT uq_sap_document_ref UNIQUE (document_number, reference_number)
            )
        `);

        await queryRunner.query(`COMMENT ON TABLE sap_document IS 'Documentos integrados desde SAP'`);

        // 18. Tabla: accounts_payable
        await queryRunner.query(`
            CREATE TABLE accounts_payable (
                accounts_payable_uuid UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
                company               INTEGER      NOT NULL,
                document_date         DATE         NOT NULL,
                reference_number      VARCHAR(100) NOT NULL,
                document_number       VARCHAR(100) NOT NULL,
                currency              VARCHAR(3)   NOT NULL DEFAULT 'MXN',
                exchange_rate         DECIMAL(18,6) NOT NULL DEFAULT 1,
                debit_credit          VARCHAR(1)   NOT NULL,
                gl_account            VARCHAR(20)  NOT NULL,
                vendor_number         INTEGER      NOT NULL,
                amount                DECIMAL(15,2) NOT NULL,
                branch                INTEGER      NOT NULL,
                payment_term          VARCHAR(5)   NOT NULL,
                due_date              DATE         NOT NULL,
                hold_indicator        VARCHAR(1)   NOT NULL DEFAULT 'N',
                source_system         VARCHAR(20)  NOT NULL,
                sent_at               TIMESTAMP,
                posting_date          DATE         NOT NULL,
                document_class        VARCHAR(5)   NOT NULL,
                reference_id          VARCHAR(40)  NOT NULL,
                cost_center           VARCHAR(15)  NOT NULL,
                profit_center         VARCHAR(15)  NOT NULL,
                sent_flag             INTEGER      NOT NULL DEFAULT 0,
                receipt_date          DATE         NOT NULL,
                document_type         VARCHAR(15)  NOT NULL,
                etl_source            VARCHAR(20)  NOT NULL,
                period_id             INTEGER      NOT NULL,
                resent_by             BIGINT,
                resent_at             TIMESTAMP,

                created_by            BIGINT,
                created_at            TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                updated_by            BIGINT,
                updated_at            TIMESTAMP,

                CONSTRAINT chk_accounts_payable_amount CHECK (amount >= 0),
                CONSTRAINT chk_accounts_payable_exchange_rate CHECK (exchange_rate > 0),
                CONSTRAINT chk_accounts_payable_debit_credit CHECK (debit_credit IN ('D', 'C')),
                CONSTRAINT chk_accounts_payable_hold_indicator CHECK (hold_indicator IN ('Y', 'N')),
                CONSTRAINT chk_accounts_payable_sent_flag CHECK (sent_flag IN (0, 1))
            )
        `);

        await queryRunner.query(`COMMENT ON TABLE accounts_payable IS 'Cuentas por pagar integradas desde sistemas ERP'`);

        // 19. Tabla: purchase_order
        await queryRunner.query(`
            CREATE TABLE purchase_order (
                purchase_order_uuid    UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
                order_number           VARCHAR(50)  NOT NULL,
                vendor_number          INTEGER      NOT NULL,
                source_id              INTEGER      NOT NULL,
                total_amount           DECIMAL(15,2) NOT NULL,
                currency               VARCHAR(3)   NOT NULL DEFAULT 'MXN',
                status                 INTEGER      NOT NULL DEFAULT 1,
                order_date             DATE         NOT NULL,
                delivery_date          DATE,
                terms_and_conditions   TEXT,

                created_by             BIGINT,
                created_at             TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                updated_by             BIGINT,
                updated_at             TIMESTAMP,

                CONSTRAINT chk_purchase_order_amount CHECK (total_amount >= 0),
                CONSTRAINT chk_purchase_order_status CHECK (status IN (0, 1, 2, 3, 4)),
                CONSTRAINT uq_purchase_order_number UNIQUE (order_number)
            )
        `);

        await queryRunner.query(`COMMENT ON TABLE purchase_order IS 'Órdenes de compra del sistema'`);

        // 20-21. Tablas: receipt y receipt_sku ELIMINADAS
        // ===============================
        // NOTA: Tablas obsoletas eliminadas - reemplazadas por reception/reception_sku
        // Las nuevas tablas se crearán en migration posterior
        // Migración: receipt → reception (con mejoras de estados y validaciones)
        // ===============================

        // 22. Tabla: shipping_guide
        await queryRunner.query(`
            CREATE TABLE shipping_guide (
                shipping_guide_id      UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
                guide_number           VARCHAR(50)  NOT NULL,
                vendor_number          INTEGER      NOT NULL,
                truck_plate            VARCHAR(20),
                trailer_plate          VARCHAR(20),
                driver_name            VARCHAR(100),
                driver_license         VARCHAR(50),
                source_id              INTEGER      NOT NULL,
                destination_id         INTEGER,
                delivery_type          INTEGER      NOT NULL,
                amount                 DECIMAL(15,2) NOT NULL,
                currency               VARCHAR(3)   NOT NULL DEFAULT 'MXN',
                status                 INTEGER      NOT NULL DEFAULT 1,
                comments               TEXT,
                delivery_date          DATE         NOT NULL,
                estimated_arrival      TIMESTAMP,
                actual_arrival         TIMESTAMP,
                sent_at                TIMESTAMP,

                created_by             BIGINT,
                created_at             TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                updated_by             BIGINT,
                updated_at             TIMESTAMP,

                CONSTRAINT chk_shipping_guide_amount CHECK (amount >= 0),
                CONSTRAINT chk_shipping_guide_status CHECK (status IN (0, 1, 2, 3, 4)),
                CONSTRAINT uq_shipping_guide_number UNIQUE (guide_number)
            )
        `);

        await queryRunner.query(`COMMENT ON TABLE shipping_guide IS 'Guías de embarque y envío'`);

        // 23. Tabla: fiscal_payments
        await queryRunner.query(`
            CREATE TABLE fiscal_payments (
                fiscal_payment_uuid    UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
                payment_number         VARCHAR(50)  NOT NULL,
                company                INTEGER      NOT NULL,
                document_number        VARCHAR(100) NOT NULL,
                reference_number       VARCHAR(100) NOT NULL,
                vendor_number          INTEGER      NOT NULL,
                amount                 DECIMAL(15,2) NOT NULL,
                currency               VARCHAR(3)   NOT NULL DEFAULT 'MXN',
                document_type          VARCHAR(5)   NOT NULL,
                sap_document           VARCHAR(50),
                payment_date           DATE         NOT NULL,
                status                 INTEGER      NOT NULL DEFAULT 1,
                payment_method         VARCHAR(10),
                bank_account           VARCHAR(50),
                reference_payment      VARCHAR(100),

                created_by             BIGINT,
                created_at             TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                updated_by             BIGINT,
                updated_at             TIMESTAMP,

                CONSTRAINT chk_fiscal_payments_amount CHECK (amount >= 0),
                CONSTRAINT chk_fiscal_payments_status CHECK (status IN (0, 1, 2, 3)),
                CONSTRAINT uq_fiscal_payments_number UNIQUE (payment_number)
            )
        `);

        await queryRunner.query(`COMMENT ON TABLE fiscal_payments IS 'Pagos fiscales realizados a proveedores'`);

        // 24. Tabla: vendor_block
        await queryRunner.query(`
            CREATE TABLE vendor_block (
                vendor_block_uuid      UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
                vendor_number          INTEGER      NOT NULL,
                block_reason           VARCHAR(10)  NOT NULL,
                block_description      TEXT,
                start_date             DATE         NOT NULL,
                end_date               DATE,
                status                 INTEGER      NOT NULL DEFAULT 1,
                auto_unblock           BOOLEAN      NOT NULL DEFAULT FALSE,
                block_type             VARCHAR(5)   NOT NULL,

                created_by             BIGINT       NOT NULL,
                created_at             TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                updated_by             BIGINT,
                updated_at             TIMESTAMP,

                CONSTRAINT chk_vendor_block_status CHECK (status IN (0, 1, 2)),
                CONSTRAINT chk_vendor_block_dates CHECK (end_date IS NULL OR start_date <= end_date),
                CONSTRAINT chk_vendor_block_type CHECK (block_type IN ('TEMP', 'PERM', 'PART'))
            )
        `);

        await queryRunner.query(`COMMENT ON TABLE vendor_block IS 'Bloqueos aplicados a proveedores'`);

        // ===============================
        // ÍNDICES PARA OPTIMIZACIÓN
        // ===============================

        await this.createIndexes(queryRunner);
    }

    private async createIndexes(queryRunner: QueryRunner): Promise<void> {
        // Índices para pac_catalog
        await queryRunner.query(`CREATE UNIQUE INDEX idx_pac_catalog_name ON pac_catalog(name)`);
        await queryRunner.query(`CREATE INDEX idx_pac_catalog_status ON pac_catalog(status)`);

        // Índices para version_catalog
        await queryRunner.query(`CREATE INDEX idx_version_catalog_pac_id ON version_catalog(pac_id)`);
        await queryRunner.query(`CREATE INDEX idx_version_catalog_status ON version_catalog(status)`);

        // NOTA: Índices CFDI eliminados (issuer, receiver, invoice, payments, etc.)
        // Estos índices están en fiscal-api

        // Índices para módulo finanzas
        await queryRunner.query(`CREATE INDEX idx_accounts_payable_vendor_number ON accounts_payable(vendor_number)`);
        await queryRunner.query(`CREATE INDEX idx_purchase_order_vendor_number ON purchase_order(vendor_number)`);
        // NOTA: receipt e idx_receipt_purchase_order_uuid eliminados (reemplazados por reception)
        await queryRunner.query(`CREATE INDEX idx_shipping_guide_vendor_number ON shipping_guide(vendor_number)`);
        await queryRunner.query(`CREATE INDEX idx_fiscal_payments_vendor_number ON fiscal_payments(vendor_number)`);
        await queryRunner.query(`CREATE INDEX idx_vendor_block_vendor_number ON vendor_block(vendor_number)`);
    }

    public async down(queryRunner: QueryRunner): Promise<void> {
        // Eliminar en orden inverso (solo tablas de finanzas-api)
        await queryRunner.query('DROP TABLE IF EXISTS vendor_block CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS fiscal_payments CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS finanzas_payments CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS shipping_guide CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS receipt_sku CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS receipt CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS purchase_order CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS accounts_payable CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS sap_document CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS rebate CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS stamped_rebate CASCADE');

        // Catálogos compartidos
        await queryRunner.query('DROP TABLE IF EXISTS version_catalog CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS pac_catalog CASCADE');

        // NOTA: NO eliminamos uuid-ossp extension (puede ser compartida por otros esquemas)

        // NOTA: 12 tablas CFDI NO incluidas aquí (están en fiscal-api)
    }
}
