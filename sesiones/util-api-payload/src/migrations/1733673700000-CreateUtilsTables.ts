// src/migrations/1733673700000-CreateUtilsTables.ts
import { MigrationInterface, QueryRunner } from "typeorm";

/**
 * Migracion: Crear tablas del modulo Herramientas y Utilerias
 * JIRA: STM-1212
 * Tablas: cat_module, cat_message, application_msg, cat_process, cat_item_type, cat_item
 */
export class CreateUtilsTables1733673700000 implements MigrationInterface {
    name = 'CreateUtilsTables1733673700000';

    public async up(queryRunner: QueryRunner): Promise<void> {
        // =====================================================
        // TABLA: cat_module - Catalogo de modulos
        // =====================================================
        await queryRunner.query(`
            CREATE TABLE cat_module (
                id_module           SERIAL PRIMARY KEY,
                name                VARCHAR(100) NOT NULL,
                description         VARCHAR(250),
                created_by          BIGINT,
                created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_by          BIGINT,
                updated_at          TIMESTAMP
            )
        `);

        await queryRunner.query(`COMMENT ON TABLE cat_module IS 'Catalogo de modulos del sistema'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_module.id_module IS 'Identificador unico del modulo'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_module.name IS 'Nombre del modulo'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_module.description IS 'Descripcion del modulo'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_module.created_by IS 'ID del usuario que registro'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_module.created_at IS 'Fecha de creacion del registro'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_module.updated_by IS 'ID del usuario que actualizo'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_module.updated_at IS 'Fecha de ultima actualizacion'`);

        await queryRunner.query(`CREATE INDEX idx_cat_module_name ON cat_module(name)`);

        // =====================================================
        // TABLA: cat_message - Catalogo de mensajes
        // =====================================================
        await queryRunner.query(`
            CREATE TABLE cat_message (
                id_message          SERIAL PRIMARY KEY,
                message_code        VARCHAR(50) NOT NULL,
                id_message_type     INTEGER NOT NULL,
                description         VARCHAR(500),
                created_by          BIGINT,
                created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_by          BIGINT,
                updated_at          TIMESTAMP
            )
        `);

        await queryRunner.query(`COMMENT ON TABLE cat_message IS 'Catalogo de mensajes del sistema'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_message.id_message IS 'Identificador unico del mensaje'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_message.message_code IS 'Codigo unico del mensaje'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_message.id_message_type IS 'Tipo de mensaje (error, info, warning, etc)'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_message.description IS 'Descripcion o texto del mensaje'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_message.created_by IS 'ID del usuario que registro'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_message.created_at IS 'Fecha de creacion del registro'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_message.updated_by IS 'ID del usuario que actualizo'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_message.updated_at IS 'Fecha de ultima actualizacion'`);

        await queryRunner.query(`CREATE INDEX idx_cat_message_code ON cat_message(message_code)`);
        await queryRunner.query(`CREATE INDEX idx_cat_message_type ON cat_message(id_message_type)`);
        await queryRunner.query(`ALTER TABLE cat_message ADD CONSTRAINT uk_cat_message_code UNIQUE (message_code)`);

        // =====================================================
        // TABLA: application_msg - Mensajes por aplicativo
        // =====================================================
        await queryRunner.query(`
            CREATE TABLE application_msg (
                id_application_msg  SERIAL PRIMARY KEY,
                id_message          INTEGER NOT NULL,
                id_application      INTEGER NOT NULL,
                created_by          BIGINT,
                created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
        `);

        await queryRunner.query(`COMMENT ON TABLE application_msg IS 'Relacion de mensajes por aplicativo'`);
        await queryRunner.query(`COMMENT ON COLUMN application_msg.id_application_msg IS 'Identificador unico'`);
        await queryRunner.query(`COMMENT ON COLUMN application_msg.id_message IS 'Referencia al catalogo de mensajes'`);
        await queryRunner.query(`COMMENT ON COLUMN application_msg.id_application IS 'Identificador del aplicativo'`);
        await queryRunner.query(`COMMENT ON COLUMN application_msg.created_by IS 'ID del usuario que registro'`);
        await queryRunner.query(`COMMENT ON COLUMN application_msg.created_at IS 'Fecha de creacion del registro'`);

        await queryRunner.query(`CREATE INDEX idx_application_msg_app ON application_msg(id_application)`);
        await queryRunner.query(`CREATE INDEX idx_application_msg_message ON application_msg(id_message)`);
        await queryRunner.query(`ALTER TABLE application_msg ADD CONSTRAINT uk_application_msg UNIQUE (id_message, id_application)`);

        // =====================================================
        // TABLA: cat_process - Catalogo de procesos
        // =====================================================
        await queryRunner.query(`
            CREATE TABLE cat_process (
                id_process          SERIAL PRIMARY KEY,
                name                VARCHAR(100) NOT NULL,
                description         VARCHAR(250),
                created_by          BIGINT,
                created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_by          BIGINT,
                updated_at          TIMESTAMP
            )
        `);

        await queryRunner.query(`COMMENT ON TABLE cat_process IS 'Catalogo de procesos del sistema'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_process.id_process IS 'Identificador unico del proceso'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_process.name IS 'Nombre del proceso'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_process.description IS 'Descripcion del proceso'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_process.created_by IS 'ID del usuario que registro'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_process.created_at IS 'Fecha de creacion del registro'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_process.updated_by IS 'ID del usuario que actualizo'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_process.updated_at IS 'Fecha de ultima actualizacion'`);

        await queryRunner.query(`CREATE INDEX idx_cat_process_name ON cat_process(name)`);

        // =====================================================
        // TABLA: cat_item_type - Catalogo de tipos de elemento
        // =====================================================
        await queryRunner.query(`
            CREATE TABLE cat_item_type (
                id_item_type        SERIAL PRIMARY KEY,
                name                VARCHAR(100) NOT NULL,
                description         VARCHAR(250),
                created_by          BIGINT,
                created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_by          BIGINT,
                updated_at          TIMESTAMP
            )
        `);

        await queryRunner.query(`COMMENT ON TABLE cat_item_type IS 'Catalogo de tipos de elemento'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_item_type.id_item_type IS 'Identificador unico del tipo de elemento'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_item_type.name IS 'Nombre del tipo de elemento'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_item_type.description IS 'Descripcion del tipo de elemento'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_item_type.created_by IS 'ID del usuario que registro'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_item_type.created_at IS 'Fecha de creacion del registro'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_item_type.updated_by IS 'ID del usuario que actualizo'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_item_type.updated_at IS 'Fecha de ultima actualizacion'`);

        await queryRunner.query(`CREATE INDEX idx_cat_item_type_name ON cat_item_type(name)`);

        // =====================================================
        // TABLA: cat_item - Catalogo de elementos
        // =====================================================
        await queryRunner.query(`
            CREATE TABLE cat_item (
                id_item             SERIAL PRIMARY KEY,
                name                VARCHAR(100) NOT NULL,
                description         VARCHAR(250),
                created_by          BIGINT,
                created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_by          BIGINT,
                updated_at          TIMESTAMP
            )
        `);

        await queryRunner.query(`COMMENT ON TABLE cat_item IS 'Catalogo de elementos del sistema'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_item.id_item IS 'Identificador unico del elemento'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_item.name IS 'Nombre del elemento'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_item.description IS 'Descripcion del elemento'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_item.created_by IS 'ID del usuario que registro'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_item.created_at IS 'Fecha de creacion del registro'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_item.updated_by IS 'ID del usuario que actualizo'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_item.updated_at IS 'Fecha de ultima actualizacion'`);

        await queryRunner.query(`CREATE INDEX idx_cat_item_name ON cat_item(name)`);
    }

    public async down(queryRunner: QueryRunner): Promise<void> {
        await queryRunner.query(`DROP TABLE IF EXISTS cat_item CASCADE`);
        await queryRunner.query(`DROP TABLE IF EXISTS cat_item_type CASCADE`);
        await queryRunner.query(`DROP TABLE IF EXISTS cat_process CASCADE`);
        await queryRunner.query(`DROP TABLE IF EXISTS application_msg CASCADE`);
        await queryRunner.query(`DROP TABLE IF EXISTS cat_message CASCADE`);
        await queryRunner.query(`DROP TABLE IF EXISTS cat_module CASCADE`);
    }
}
