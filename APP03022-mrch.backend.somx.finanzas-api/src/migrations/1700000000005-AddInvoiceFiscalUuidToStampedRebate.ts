import { MigrationInterface, QueryRunner } from 'typeorm';

/**
 * Migración V5 - Agregar columna invoice_fiscal_uuid a stamped_rebate
 *
 * PROPÓSITO:
 * - Vincular rebates timbrados con facturas en fiscal-api
 * - Permite relacionar stamped_rebate (finanzas) con invoice (fiscal)
 * - Parte de la refactorización STM-973 y STM-875
 *
 * NOTA:
 * - fiscal-api solo VALIDA XMLs, no guarda rebates
 * - finanzas-api gestiona rebates en tenant_finance
 */
export class AddInvoiceFiscalUuidToStampedRebate1700000000005 implements MigrationInterface {
    public async up(queryRunner: QueryRunner): Promise<void> {
        // Agregar columna invoice_fiscal_uuid a stamped_rebate
        await queryRunner.query(`
            ALTER TABLE stamped_rebate
            ADD COLUMN invoice_fiscal_uuid UUID
        `);

        await queryRunner.query(`
            COMMENT ON COLUMN stamped_rebate.invoice_fiscal_uuid IS
            'UUID fiscal de la factura relacionada (foreign key lógica a fiscal-api.invoice.fiscal_uuid)'
        `);

        // Crear índice para búsquedas por invoice_fiscal_uuid
        await queryRunner.query(`
            CREATE INDEX idx_stamped_rebate_invoice_fiscal_uuid
            ON stamped_rebate(invoice_fiscal_uuid)
        `);

        await queryRunner.query(`
            COMMENT ON INDEX idx_stamped_rebate_invoice_fiscal_uuid IS
            'Índice para búsquedas de rebates por factura fiscal'
        `);
    }

    public async down(queryRunner: QueryRunner): Promise<void> {
        // Rollback: eliminar índice y columna
        await queryRunner.query(`
            DROP INDEX IF EXISTS idx_stamped_rebate_invoice_fiscal_uuid
        `);

        await queryRunner.query(`
            ALTER TABLE stamped_rebate
            DROP COLUMN IF EXISTS invoice_fiscal_uuid
        `);
    }
}
