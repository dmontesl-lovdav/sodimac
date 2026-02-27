// src/entities/CatParameter.entity.ts
import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
    Index
} from 'typeorm';

/**
 * Catalogo de parametros de configuracion del sistema
 * JIRA: STM-1212
 */
@Entity('cat_parameter')
@Index('idx_cat_parameter_name', ['name'])
@Index('idx_cat_parameter_id_module', ['idModule'])
export class CatParameter {
    @PrimaryGeneratedColumn({ name: 'id_parameter' })
    idParameter!: number;

    @Column({ name: 'id_module', type: 'integer', nullable: false })
    idModule!: number;

    @Column({ name: 'id_type', type: 'integer', nullable: false })
    idType!: number;

    @Column({ name: 'name', type: 'varchar', length: 50, nullable: false })
    name!: string;

    @Column({ name: 'description', type: 'varchar', length: 250, nullable: true })
    description?: string;

    @Column({ name: 'value', type: 'varchar', length: 150, nullable: false })
    value!: string;

    @Column({ name: 'version', type: 'decimal', precision: 4, scale: 1, default: 1.0 })
    version!: number;

    @Column({ name: 'start_date', type: 'timestamp', default: () => 'CURRENT_TIMESTAMP' })
    startDate!: Date;

    @Column({ name: 'end_date', type: 'timestamp', nullable: true })
    endDate?: Date;

    @Column({ name: 'status', type: 'integer', default: 1 })
    status!: number;

    // Campos de auditoria (patron finanzas-api)
    @Column({ name: 'created_by', type: 'bigint', nullable: true })
    createdBy?: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp', default: () => 'CURRENT_TIMESTAMP' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'bigint', nullable: true })
    updatedBy?: number;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;
}
