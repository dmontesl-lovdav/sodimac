import { MigrationInterface, QueryRunner } from 'typeorm';

export class CreateMissingTables1700000000003 implements MigrationInterface {
    public async up(queryRunner: QueryRunner): Promise<void> {
        console.log('=== Creating missing tables: 5 tables (reception, reception_sku, finanzas_payments, shipping_guide_document, shipping_guide_purchase_order) ===');

        // ===============================
        // 1. TABLA: reception
        // ===============================
        await queryRunner.query(`
            CREATE TABLE IF NOT EXISTS reception (
                reception_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                purchase_order_uuid UUID,
                origin_id NUMERIC(3),
                destination_id NUMERIC(3),
                amount NUMERIC(16, 2),
                status NUMERIC(2),
                comment VARCHAR(256),
                reception_date DATE,
                created_by BIGINT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated_by BIGINT,
                updated_at TIMESTAMP,

                -- Foreign Keys
                CONSTRAINT fk_reception_purchase_order
                    FOREIGN KEY (purchase_order_uuid)
                    REFERENCES purchase_order(purchase_order_uuid)
                    ON DELETE CASCADE
            )
        `);

        await queryRunner.query(`
            COMMENT ON TABLE reception IS 'Recepciones de mercancía - reemplazo de receipt'
        `);

        await queryRunner.query(`
            COMMENT ON COLUMN reception.reception_id IS 'Identificador único UUID'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN reception.purchase_order_uuid IS 'Referencia a orden de compra'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN reception.origin_id IS 'Almacén de origen'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN reception.destination_id IS 'Almacén de destino'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN reception.amount IS 'Monto total de la recepción'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN reception.status IS 'Estado: 0=Borrador, 1=Confirmado, 2=En proceso, 3=Completado, 4=Cancelado'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN reception.reception_date IS 'Fecha de recepción de mercancía'
        `);

        console.log(' Table reception created');

        // ===============================
        // 2. TABLA: reception_sku
        // ===============================
        await queryRunner.query(`
            CREATE TABLE IF NOT EXISTS reception_sku (
                reception_sku_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                reception_id UUID NOT NULL,
                sku VARCHAR(15) NOT NULL,
                description VARCHAR(256) NOT NULL,
                quantity NUMERIC(20, 6),
                unit_cost NUMERIC(16, 2),
                total_cost NUMERIC(26, 2),
                status NUMERIC(2),
                created_by BIGINT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated_by BIGINT,
                updated_at TIMESTAMP,

                -- Foreign Keys
                CONSTRAINT fk_reception_sku_reception
                    FOREIGN KEY (reception_id)
                    REFERENCES reception(reception_id)
                    ON DELETE CASCADE
            )
        `);

        await queryRunner.query(`
            COMMENT ON TABLE reception_sku IS 'Detalle de SKUs en recepciones - reemplazo de receipt_sku'
        `);

        await queryRunner.query(`
            COMMENT ON COLUMN reception_sku.reception_sku_id IS 'Identificador único UUID'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN reception_sku.reception_id IS 'Referencia a la recepción padre'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN reception_sku.sku IS 'Código del producto (SKU)'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN reception_sku.quantity IS 'Cantidad recibida (hasta 6 decimales)'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN reception_sku.unit_cost IS 'Costo unitario'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN reception_sku.total_cost IS 'Costo total (quantity * unit_cost)'
        `);

        console.log(' Table reception_sku created');

        // ===============================
        // 3. TABLA: finanzas_payments
        // ===============================
        await queryRunner.query(`
            CREATE TABLE IF NOT EXISTS finanzas_payments (
                finanzas_payment_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                company INT NOT NULL,
                document_number VARCHAR(100) NOT NULL,
                document_reference VARCHAR(100) NOT NULL,
                vendor_number INT NOT NULL,
                amount DECIMAL(15, 2) NOT NULL,
                currency VARCHAR(3) NOT NULL DEFAULT 'MXN',
                document_type VARCHAR(5) NOT NULL,
                sap_document VARCHAR(50) NOT NULL,
                payment_date DATE NOT NULL,
                status INT NOT NULL DEFAULT 1,
                created_by BIGINT,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_by BIGINT,
                updated_at TIMESTAMP,

                -- Constraints
                CONSTRAINT chk_finanzas_payments_amount
                    CHECK (amount >= 0),
                CONSTRAINT chk_finanzas_payments_status
                    CHECK (status IN (0, 1, 2, 3))
            )
        `);

        await queryRunner.query(`
            COMMENT ON TABLE finanzas_payments IS 'Pagos del sistema financiero (origen: ER Pagos)'
        `);

        await queryRunner.query(`
            COMMENT ON COLUMN finanzas_payments.finanzas_payment_uuid IS 'Identificador único UUID'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN finanzas_payments.company IS 'Empresa/Sociedad (1=FBC)'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN finanzas_payments.document_number IS 'Número de documento de pago'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN finanzas_payments.document_reference IS 'Referencia del documento'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN finanzas_payments.vendor_number IS 'Número de proveedor'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN finanzas_payments.amount IS 'Monto del pago'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN finanzas_payments.currency IS 'Moneda (MXN, USD, EUR)'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN finanzas_payments.document_type IS 'Tipo de documento (PP=Pago proveedor)'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN finanzas_payments.sap_document IS 'Documento SAP asociado'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN finanzas_payments.payment_date IS 'Fecha de pago'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN finanzas_payments.status IS 'Estado: 0=Borrador, 1=Activo, 2=Procesado, 3=Cancelado'
        `);

        console.log(' Table finanzas_payments created');

        // ===============================
        // 4. TABLA: shipping_guide_document
        // ===============================
        await queryRunner.query(`
            CREATE TABLE IF NOT EXISTS shipping_guide_document (
                shipping_guide_document_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                shipping_guide_id UUID,
                file_name VARCHAR(100),
                file_type SMALLINT,
                status NUMERIC(2),
                created_by BIGINT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated_by BIGINT,
                updated_at TIMESTAMP,

                -- Foreign Keys
                CONSTRAINT fk_shipping_guide_document_guide
                    FOREIGN KEY (shipping_guide_id)
                    REFERENCES shipping_guide(shipping_guide_id)
                    ON DELETE CASCADE
            )
        `);

        await queryRunner.query(`
            COMMENT ON TABLE shipping_guide_document IS 'Documentos asociados a las guías de envío'
        `);

        await queryRunner.query(`
            COMMENT ON COLUMN shipping_guide_document.shipping_guide_document_id IS 'Identificador único UUID'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN shipping_guide_document.shipping_guide_id IS 'Referencia a la guía de envío'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN shipping_guide_document.file_name IS 'Nombre del archivo (ej: guia_001.pdf)'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN shipping_guide_document.file_type IS 'Tipo de archivo: 1=PDF, 2=Excel, 3=Imagen, etc.'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN shipping_guide_document.status IS 'Estado del documento'
        `);

        console.log(' Table shipping_guide_document created');

        // ===============================
        // 5. TABLA: shipping_guide_purchase_order
        // ===============================
        await queryRunner.query(`
            CREATE TABLE IF NOT EXISTS shipping_guide_purchase_order (
                shipping_guide_purchase_order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                shipping_guide_id UUID NOT NULL,
                purchase_order_id UUID NOT NULL,
                created_by BIGINT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                updated_by BIGINT,
                updated_at TIMESTAMP,

                -- Foreign Keys
                CONSTRAINT fk_shipping_guide_purchase_order_guide
                    FOREIGN KEY (shipping_guide_id)
                    REFERENCES shipping_guide(shipping_guide_id)
                    ON DELETE CASCADE,
                CONSTRAINT fk_shipping_guide_purchase_order_po
                    FOREIGN KEY (purchase_order_id)
                    REFERENCES purchase_order(purchase_order_uuid)
                    ON DELETE CASCADE,

                -- Unique constraint para evitar duplicados
                CONSTRAINT uq_shipping_guide_purchase_order
                    UNIQUE (shipping_guide_id, purchase_order_id)
            )
        `);

        await queryRunner.query(`
            COMMENT ON TABLE shipping_guide_purchase_order IS 'Relación muchos a muchos entre guías de envío y órdenes de compra'
        `);

        await queryRunner.query(`
            COMMENT ON COLUMN shipping_guide_purchase_order.shipping_guide_purchase_order_id IS 'Identificador único UUID'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN shipping_guide_purchase_order.shipping_guide_id IS 'Referencia a guía de envío'
        `);
        await queryRunner.query(`
            COMMENT ON COLUMN shipping_guide_purchase_order.purchase_order_id IS 'Referencia a orden de compra'
        `);

        console.log(' Table shipping_guide_purchase_order created');

        // ===============================
        // 6. ÍNDICES
        // ===============================

        // Índices para reception
        await queryRunner.query(`
            CREATE INDEX IF NOT EXISTS idx_reception_purchase_order
            ON reception(purchase_order_uuid)
        `);
        await queryRunner.query(`
            CREATE INDEX IF NOT EXISTS idx_reception_status
            ON reception(status)
        `);
        await queryRunner.query(`
            CREATE INDEX IF NOT EXISTS idx_reception_date
            ON reception(reception_date)
        `);
        await queryRunner.query(`
            CREATE INDEX IF NOT EXISTS idx_reception_created_at
            ON reception(created_at)
        `);

        // Índices para reception_sku
        await queryRunner.query(`
            CREATE INDEX IF NOT EXISTS idx_reception_sku_reception_id
            ON reception_sku(reception_id)
        `);
        await queryRunner.query(`
            CREATE INDEX IF NOT EXISTS idx_reception_sku_sku
            ON reception_sku(sku)
        `);

        // Índices para finanzas_payments
        await queryRunner.query(`
            CREATE INDEX IF NOT EXISTS idx_finanzas_payments_vendor
            ON finanzas_payments(vendor_number)
        `);
        await queryRunner.query(`
            CREATE INDEX IF NOT EXISTS idx_finanzas_payments_document
            ON finanzas_payments(document_number)
        `);
        await queryRunner.query(`
            CREATE INDEX IF NOT EXISTS idx_finanzas_payments_sap_doc
            ON finanzas_payments(sap_document)
        `);
        await queryRunner.query(`
            CREATE INDEX IF NOT EXISTS idx_finanzas_payments_date
            ON finanzas_payments(payment_date)
        `);
        await queryRunner.query(`
            CREATE INDEX IF NOT EXISTS idx_finanzas_payments_status
            ON finanzas_payments(status)
        `);

        // Índices para shipping_guide_document
        await queryRunner.query(`
            CREATE INDEX IF NOT EXISTS idx_shipping_guide_document_guide_id
            ON shipping_guide_document(shipping_guide_id)
        `);
        await queryRunner.query(`
            CREATE INDEX IF NOT EXISTS idx_shipping_guide_document_status
            ON shipping_guide_document(status)
        `);

        // Índices para shipping_guide_purchase_order
        await queryRunner.query(`
            CREATE INDEX IF NOT EXISTS idx_shipping_guide_po_guide_id
            ON shipping_guide_purchase_order(shipping_guide_id)
        `);
        await queryRunner.query(`
            CREATE INDEX IF NOT EXISTS idx_shipping_guide_po_order_id
            ON shipping_guide_purchase_order(purchase_order_id)
        `);

        console.log(' Indexes created for all 5 tables');
        console.log('=== Migration completed successfully ===');
    }

    public async down(queryRunner: QueryRunner): Promise<void> {
        console.log('=== Rolling back: Dropping 5 missing tables ===');

        // Drop en orden inverso para respetar FK constraints
        await queryRunner.query('DROP TABLE IF EXISTS shipping_guide_purchase_order CASCADE');
        console.log(' Table shipping_guide_purchase_order dropped');

        await queryRunner.query('DROP TABLE IF EXISTS shipping_guide_document CASCADE');
        console.log(' Table shipping_guide_document dropped');

        await queryRunner.query('DROP TABLE IF EXISTS reception_sku CASCADE');
        console.log(' Table reception_sku dropped');

        await queryRunner.query('DROP TABLE IF EXISTS reception CASCADE');
        console.log(' Table reception dropped');

        await queryRunner.query('DROP TABLE IF EXISTS finanzas_payments CASCADE');
        console.log(' Table finanzas_payments dropped');

        console.log('=== Rollback completed ===');
    }
}
