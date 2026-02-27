import { MigrationInterface, QueryRunner } from 'typeorm';

/**
 * Migración V3 - DATOS INICIALES COMPLETOS
 * Inserción de datos maestros y catálogos base
 * Incluye datos CFDI y módulo fiscal
 * Equivalente a: V3__Insert_initial_data.sql de Flyway
 */
export class InsertInitialData1700000000004 implements MigrationInterface {
    public async up(queryRunner: QueryRunner): Promise<void> {
        // Set search_path explicitly to ensure we're in the correct schema
        await queryRunner.query(`SET search_path TO tenant_finance, public`);

        // ===============================
        // DATOS INICIALES CFDI
        // ===============================

        // Insertar PACs
        await queryRunner.query(`
            INSERT INTO pac_catalog (name, description, status, created_by) VALUES
            ('PAC_DEMO', 'PAC de demostración para pruebas', 1, 1),
            ('FINKOK', 'Finkok - Proveedor Autorizado de Certificación', 1, 1),
            ('SW SAPIEN', 'SW Sapien - Proveedor Autorizado de Certificación', 1, 1)
        `);

        // Insertar versiones CFDI
        await queryRunner.query(`
            INSERT INTO version_catalog (name, description, version, document_type, pac_id, status, created_by) VALUES
            ('CFDI 4.0 Ingreso', 'Comprobante Fiscal Digital 4.0 - Ingreso', 4.000, 'I', 1, 1, 1),
            ('CFDI 4.0 Egreso', 'Comprobante Fiscal Digital 4.0 - Egreso', 4.000, 'E', 1, 1, 1),
            ('CFDI 4.0 Pago', 'Comprobante Fiscal Digital 4.0 - Complemento de Pago', 4.000, 'P', 1, 1, 1),
            ('CFDI 3.3 Ingreso', 'Comprobante Fiscal Digital 3.3 - Ingreso (Legacy)', 3.300, 'I', 1, 0, 1),
            ('CFDI 3.3 Egreso', 'Comprobante Fiscal Digital 3.3 - Egreso (Legacy)', 3.300, 'E', 1, 0, 1)
        `);

        // ===============================
        // DATOS INICIALES MÓDULO FISCAL
        // ===============================

        // Insertar stamped_rebate
        await queryRunner.query(`
            INSERT INTO stamped_rebate (document_number, reference_number, invoice_fiscal_uuid, status, created_by) VALUES
            ('REB-2024-001', 'REF-001', NULL, 1, 1),
            ('REB-2024-002', 'REF-002', NULL, 1, 1),
            ('REB-DEMO-001', 'DEMO-REF-001', NULL, 1, 1)
        `);

        // Insertar rebate
        await queryRunner.query(`
            INSERT INTO rebate (
                document_number, reference_number, sap_document, vendor_number,
                amount, source, period_id, due_date, posting_date, status, created_by
            ) VALUES
            ('REB-2024-001', 'REF-001', 'SAP-001', 1001, 1500.00, 1, 202401, '2024-03-31', '2024-01-15', 1, 1),
            ('REB-2024-002', 'REF-002', 'SAP-002', 1002, 2500.00, 1, 202401, '2024-04-30', '2024-01-20', 1, 1),
            ('REB-DEMO-001', 'DEMO-REF-001', 'SAP-DEMO-001', 9999, 500.00, 1, 202401, '2024-12-31', '2024-01-01', 1, 1)
        `);

        // Insertar sap_document
        await queryRunner.query(`
            INSERT INTO sap_document (
                document_number, reference_number, vendor_number, amount, source,
                doc_sap, message, sap_status, document_type, created_by
            ) VALUES
            ('DOC-2024-001', 'REF-DOC-001', 1001, 10000.00, 1, 'SAP-DOC-001', 'Documento de prueba', 1, 'IN', 1),
            ('DOC-2024-002', 'REF-DOC-002', 1002, 15000.00, 1, 'SAP-DOC-002', 'Factura de servicios', 1, 'SV', 1),
            ('DOC-DEMO-001', 'REF-DEMO-001', 9999, 5000.00, 1, 'SAP-DEMO-001', 'Documento de demostración', 1, 'DM', 1)
        `);

        // Insertar accounts_payable
        await queryRunner.query(`
            INSERT INTO accounts_payable (
                company, document_date, reference_number, document_number, currency,
                exchange_rate, debit_credit, gl_account, vendor_number, amount,
                branch, payment_term, due_date, hold_indicator, source_system,
                posting_date, document_class, reference_id, cost_center, profit_center,
                sent_flag, receipt_date, document_type, etl_source, period_id, created_by
            ) VALUES
            (100, '2024-01-15', 'AP-REF-001', 'AP-DOC-001', 'MXN', 1.0, 'C', '2000001', 1001, 10000.00,
             1, 'N30', '2024-02-15', 'N', 'SAP', '2024-01-15', 'KR', 'EXT-REF-001', 'CC001', 'PC001',
             0, '2024-01-15', 'INVOICE', 'ETL_SAP', 202401, 1),
            (100, '2024-01-20', 'AP-REF-002', 'AP-DOC-002', 'MXN', 1.0, 'C', '2000002', 1002, 15000.00,
             1, 'N45', '2024-03-05', 'N', 'SAP', '2024-01-20', 'KR', 'EXT-REF-002', 'CC002', 'PC002',
             0, '2024-01-20', 'INVOICE', 'ETL_SAP', 202401, 1)
        `);

        // Insertar purchase_order
        await queryRunner.query(`
            INSERT INTO purchase_order (
                order_number, vendor_number, source_id, total_amount, currency, status,
                order_date, delivery_date, terms_and_conditions, created_by
            ) VALUES
            ('PO-2024-0001', 1001, 1, 15000.00, 'MXN', 1, '2024-01-15', '2024-02-15',
             'Términos estándar: Pago a 30 días, entrega en almacén central', 1),
            ('PO-2024-0002', 1002, 1, 25000.50, 'MXN', 1, '2024-01-20', '2024-02-05',
             'Entrega urgente: Requiere confirmación 24h antes. Pago a 15 días', 1),
            ('PO-2024-0003', 1003, 2, 8500.75, 'MXN', 2, '2024-01-10', '2024-02-10',
             'Orden completada exitosamente. Productos de oficina', 1),
            ('PO-2024-0004', 1001, 1, 45000.00, 'MXN', 3, '2024-01-25', '2024-03-15',
             'Pendiente aprobación gerencial por monto elevado. Equipos de cómputo', 1),
            ('PO-2024-0005', 1004, 3, 12750.25, 'MXN', 4, '2024-01-18', '2024-02-18',
             'Entrega parcial autorizada. Materiales de construcción', 1)
        `);

        // Insertar reception (NUEVA TABLA - Reemplaza receipt)
        await queryRunner.query(`
            DO $$
            DECLARE
                po_uuid_1 UUID;
                po_uuid_2 UUID;
                po_uuid_3 UUID;
                rec_uuid_1 UUID;
                rec_uuid_2 UUID;
                rec_uuid_3 UUID;
            BEGIN
                -- Obtener UUIDs de purchase orders
                SELECT purchase_order_uuid INTO po_uuid_1 FROM purchase_order WHERE order_number = 'PO-2024-0001';
                SELECT purchase_order_uuid INTO po_uuid_2 FROM purchase_order WHERE order_number = 'PO-2024-0002';
                SELECT purchase_order_uuid INTO po_uuid_3 FROM purchase_order WHERE order_number = 'PO-2024-0003';

                -- Insertar recepciones
                INSERT INTO reception (
                    purchase_order_uuid, origin_id, destination_id, amount, status,
                    comment, reception_date, created_by
                ) VALUES
                (po_uuid_1, 1, 2, 15000.00, 3, 'Recepción completa - Términos estándar cumplidos', '2024-02-15', 1)
                RETURNING reception_id INTO rec_uuid_1;

                INSERT INTO reception (
                    purchase_order_uuid, origin_id, destination_id, amount, status,
                    comment, reception_date, created_by
                ) VALUES
                (po_uuid_2, 1, 2, 25000.50, 3, 'Recepción urgente completada en tiempo', '2024-02-05', 1)
                RETURNING reception_id INTO rec_uuid_2;

                INSERT INTO reception (
                    purchase_order_uuid, origin_id, destination_id, amount, status,
                    comment, reception_date, created_by
                ) VALUES
                (po_uuid_3, 2, 2, 8500.75, 3, 'Recepción de productos de oficina - Orden cerrada', '2024-02-10', 1)
                RETURNING reception_id INTO rec_uuid_3;

                -- Insertar reception_sku (detalle de productos recibidos)
                INSERT INTO reception_sku (
                    reception_id, sku, description, quantity, unit_cost, total_cost, status
                ) VALUES
                -- Recepción 1: 10 laptops + 10 monitores
                (rec_uuid_1, 'LAP-HP-001', 'Laptop HP ProBook 450 G9', 10.000000, 12000.00, 120000.00, 1),
                (rec_uuid_1, 'MON-DELL-24', 'Monitor Dell 24 pulgadas', 10.000000, 3000.00, 30000.00, 1),

                -- Recepción 2: 50 cajas de papel + 20 sillas
                (rec_uuid_2, 'PAP-A4-500', 'Papel A4 caja 500 hojas', 50.000000, 150.00, 7500.00, 1),
                (rec_uuid_2, 'SIL-ERG-001', 'Silla ergonómica oficina', 20.000000, 1250.50, 25010.00, 1),

                -- Recepción 3: Materiales de oficina varios
                (rec_uuid_3, 'BOL-BIC-AZ', 'Bolígrafos BIC azul caja 50', 20.000000, 85.50, 1710.00, 1),
                (rec_uuid_3, 'CUA-A4-100', 'Cuadernos A4 100 hojas', 30.000000, 45.75, 1372.50, 1),
                (rec_uuid_3, 'ENG-UHU-40', 'Engrapadora UHU 40 hojas', 15.000000, 125.00, 1875.00, 1),
                (rec_uuid_3, 'CAR-PLAS-A4', 'Carpetas plásticas A4', 50.000000, 12.50, 625.00, 1),
                (rec_uuid_3, 'POST-3M-100', 'Post-it 3M pack 100', 40.000000, 65.00, 2600.00, 1);
            END $$;
        `);

        // Insertar shipping_guide
        await queryRunner.query(`
            DO $$
            DECLARE
                po_uuid_1 UUID;
                po_uuid_2 UUID;
                sg_id_1 UUID;
                sg_id_2 UUID;
            BEGIN
                -- Obtener UUIDs de purchase orders
                SELECT purchase_order_uuid INTO po_uuid_1 FROM purchase_order WHERE order_number = 'PO-2024-0001';
                SELECT purchase_order_uuid INTO po_uuid_2 FROM purchase_order WHERE order_number = 'PO-2024-0002';

                -- Insertar shipping guides
                INSERT INTO shipping_guide (
                    guide_number, vendor_number, truck_plate, trailer_plate, driver_name,
                    driver_license, source_id, destination_id, delivery_type, amount,
                    currency, status, comments, delivery_date, estimated_arrival, actual_arrival, created_by
                ) VALUES
                ('SG-2024-0001', 1001, 'ABC-123-MX', 'XYZ-456-MX', 'Juan Pérez Rodríguez', 'LIC-12345', 1, 2, 1, 15000.00,
                 'MXN', 3, 'Entrega exitosa - Productos estándar', '2024-02-15', '2024-02-15 09:00:00', '2024-02-15 08:45:00', 1)
                RETURNING shipping_guide_id INTO sg_id_1;

                INSERT INTO shipping_guide (
                    guide_number, vendor_number, truck_plate, trailer_plate, driver_name,
                    driver_license, source_id, destination_id, delivery_type, amount,
                    currency, status, comments, delivery_date, estimated_arrival, actual_arrival, created_by
                ) VALUES
                ('SG-2024-0002', 1002, 'DEF-789-MX', 'UVW-012-MX', 'María García López', 'LIC-67890', 1, 2, 1, 25000.50,
                 'MXN', 3, 'Entrega urgente completada a tiempo', '2024-02-05', '2024-02-05 08:00:00', '2024-02-05 07:50:00', 1)
                RETURNING shipping_guide_id INTO sg_id_2;

                -- Insertar shipping_guide_document (documentos adjuntos)
                INSERT INTO shipping_guide_document (
                    shipping_guide_id, file_name, file_type, status
                ) VALUES
                (sg_id_1, 'SG-2024-0001-Remision.pdf', 1, 1),
                (sg_id_1, 'SG-2024-0001-CartaPorte.pdf', 1, 1),
                (sg_id_1, 'SG-2024-0001-FotosCarga.jpg', 3, 1),
                (sg_id_2, 'SG-2024-0002-Remision.pdf', 1, 1),
                (sg_id_2, 'SG-2024-0002-ComprobanteEntrega.pdf', 1, 1);

                -- Insertar shipping_guide_purchase_order (relación M:N)
                INSERT INTO shipping_guide_purchase_order (
                    shipping_guide_id, purchase_order_id
                ) VALUES
                (sg_id_1, po_uuid_1),
                (sg_id_2, po_uuid_2);
            END $$;
        `);

        // Insertar finanzas_payments (NUEVA TABLA - Reemplaza fiscal_payments)
        await queryRunner.query(`
            INSERT INTO finanzas_payments (
                company, document_number, document_reference, vendor_number, amount,
                currency, document_type, sap_document, payment_date, status, created_by
            ) VALUES
            (1, 'PAY-2024-0001', 'AP-REF-001', 1001, 15000.00, 'MXN', 'PP', 'SAP-PAY-001',
             '2024-03-15', 1, 1),
            (1, 'PAY-2024-0002', 'AP-REF-002', 1002, 25000.50, 'MXN', 'PP', 'SAP-PAY-002',
             '2024-02-20', 1, 1),
            (1, 'PAY-2024-0003', 'AP-REF-003', 1003, 8500.75, 'MXN', 'PP', 'SAP-PAY-003',
             '2024-02-25', 2, 1)
        `);

        // Insertar vendor_block
        await queryRunner.query(`
            INSERT INTO vendor_block (
                vendor_number, block_reason, block_description, start_date, end_date,
                status, auto_unblock, block_type, created_by
            ) VALUES
            (2001, 'CREDIT', 'Bloqueo por límite de crédito excedido', '2024-01-01', '2024-03-31', 1, TRUE, 'TEMP', 1),
            (2002, 'LEGAL', 'Bloqueo por proceso legal', '2024-01-15', NULL, 1, FALSE, 'PERM', 1),
            (9998, 'DEMO', 'Bloqueo de demostración temporal', '2024-01-01', '2024-12-31', 1, TRUE, 'PART', 1)
        `);
    }

    public async down(queryRunner: QueryRunner): Promise<void> {
        // Eliminar datos en orden inverso (respetando FKs)
        await queryRunner.query('DELETE FROM vendor_block');
        await queryRunner.query('DELETE FROM finanzas_payments');
        await queryRunner.query('DELETE FROM shipping_guide_document');
        await queryRunner.query('DELETE FROM shipping_guide_purchase_order');
        await queryRunner.query('DELETE FROM shipping_guide');
        await queryRunner.query('DELETE FROM reception_sku');
        await queryRunner.query('DELETE FROM reception');
        await queryRunner.query('DELETE FROM purchase_order');
        await queryRunner.query('DELETE FROM accounts_payable');
        await queryRunner.query('DELETE FROM sap_document');
        await queryRunner.query('DELETE FROM rebate');
        await queryRunner.query('DELETE FROM stamped_rebate');
        await queryRunner.query('DELETE FROM version_catalog');
        await queryRunner.query('DELETE FROM pac_catalog');
    }
}
