import { MigrationInterface, QueryRunner } from 'typeorm';

/**
 * Migración V1 - LIMPIEZA COMPLETA DE BASE DE DATOS
 * Eliminación de todas las tablas y dependencias
 * Solo para desarrollo - NO ejecutar en producción
 * Equivalente a: V1__Drop_all_tables.sql de Flyway
 */
export class DropAllTables1700000000000 implements MigrationInterface {
    public async up(queryRunner: QueryRunner): Promise<void> {
        // NO usar session_replication_role (requiere superuser)
        // CASCADE se encarga de las dependencias de foreign keys

        // ===============================
        // ELIMINAR TABLAS EN ORDEN CORRECTO (respetando FKs)
        // ===============================

        // Primero: Tablas dependientes
        await queryRunner.query('DROP TABLE IF EXISTS vendor_block CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS fiscal_payments CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS finanzas_payments CASCADE');

        // Tablas de shipping guide
        await queryRunner.query('DROP TABLE IF EXISTS shipping_guide_document CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS shipping_guide_purchase_order CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS shipping_guide CASCADE');

        // Tablas de reception (reemplazan receipt/receipt_sku)
        await queryRunner.query('DROP TABLE IF EXISTS reception_sku CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS reception CASCADE');

        // Tablas obsoletas (si existen)
        await queryRunner.query('DROP TABLE IF EXISTS receipt_sku CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS receipt CASCADE');

        // Otras tablas fiscales
        await queryRunner.query('DROP TABLE IF EXISTS accounts_payable CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS rebate CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS sap_document CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS stamped_rebate CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS purchase_order CASCADE');

        // Catálogos
        await queryRunner.query('DROP TABLE IF EXISTS supplier_block CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS origin_catalog CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS status_catalog CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS version_catalog CASCADE');
        await queryRunner.query('DROP TABLE IF EXISTS pac_catalog CASCADE');

        // NOTA: Tablas CFDI eliminadas de finanzas-api
        // Las 12 tablas CFDI ahora están SOLO en fiscal-api (schema: tenant_fiscal)
    }

    public async down(_queryRunner: QueryRunner): Promise<void> {
        // No hay rollback para eliminación de tablas
        console.log('No rollback available for DropAllTables migration');
    }
}
