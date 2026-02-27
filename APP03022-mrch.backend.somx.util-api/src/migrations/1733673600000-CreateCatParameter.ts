// src/migrations/1733673600000-CreateCatParameter.ts
import { MigrationInterface, QueryRunner } from "typeorm";

/**
 * Migracion: Crear tabla cat_parameter
 * JIRA: STM-1212
 */
export class CreateCatParameter1733673600000 implements MigrationInterface {
    name = 'CreateCatParameter1733673600000';

    public async up(queryRunner: QueryRunner): Promise<void> {
        // Crear tabla cat_parameter
        await queryRunner.query(`
            CREATE TABLE cat_parameter (
                id_parameter        SERIAL PRIMARY KEY,
                id_module           INTEGER NOT NULL,
                id_type             INTEGER NOT NULL,
                name                VARCHAR(50) NOT NULL,
                description         VARCHAR(250),
                value               VARCHAR(150) NOT NULL,
                version             DECIMAL(4,1) NOT NULL DEFAULT 1.0,
                start_date          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                end_date            TIMESTAMP,
                status              INTEGER NOT NULL DEFAULT 1,
                created_by          BIGINT,
                created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_by          BIGINT,
                updated_at          TIMESTAMP
            )
        `);

        // Comentarios
        await queryRunner.query(`COMMENT ON TABLE cat_parameter IS 'Catalogo de parametros de configuracion del sistema'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_parameter.id_parameter IS 'Identificador unico autoincremental'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_parameter.id_module IS 'Referencia al catalogo de modulos'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_parameter.id_type IS 'Referencia al catalogo de tipos de parametro'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_parameter.name IS 'Nombre tecnico del parametro'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_parameter.description IS 'Descripcion funcional del parametro'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_parameter.value IS 'Valor de configuracion'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_parameter.version IS 'Version del parametro (ej. 1.0, 1.1, 2.0)'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_parameter.start_date IS 'Fecha de inicio de vigencia'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_parameter.end_date IS 'Fecha fin de vigencia (NULL = indefinido)'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_parameter.status IS 'Estado: 1=Activo, 0=Inactivo'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_parameter.created_by IS 'ID del usuario que registro'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_parameter.created_at IS 'Fecha de creacion del registro'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_parameter.updated_by IS 'ID del usuario que actualizo'`);
        await queryRunner.query(`COMMENT ON COLUMN cat_parameter.updated_at IS 'Fecha de ultima actualizacion'`);

        // Indices
        await queryRunner.query(`CREATE INDEX idx_cat_parameter_name ON cat_parameter(name)`);
        await queryRunner.query(`CREATE INDEX idx_cat_parameter_id_module ON cat_parameter(id_module)`);

        // Constraint UNIQUE
        await queryRunner.query(`ALTER TABLE cat_parameter ADD CONSTRAINT uk_cat_parameter_name_version UNIQUE (name, version)`);
    }

    public async down(queryRunner: QueryRunner): Promise<void> {
        await queryRunner.query(`DROP TABLE IF EXISTS cat_parameter CASCADE`);
    }
}
