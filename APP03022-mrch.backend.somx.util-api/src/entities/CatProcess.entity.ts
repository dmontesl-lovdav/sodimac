// src/entities/CatProcess.entity.ts
import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
    Index
} from 'typeorm';

/**
 * Entidad CatProcess - Catalogo de procesos del sistema
 * Tabla: cat_process
 * JIRA: STM-1212
 */
@Entity('cat_process')
@Index('idx_cat_process_name', ['name'])
export class CatProcess {
    @PrimaryGeneratedColumn({ name: 'id_process' })
    idProcess!: number;

    @Column({ name: 'name', type: 'varchar', length: 100, nullable: false })
    name!: string;

    @Column({ name: 'description', type: 'varchar', length: 250, nullable: true })
    description?: string;

    // Campos de auditoria
    @Column({ name: 'created_by', type: 'bigint', nullable: true })
    createdBy?: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp', default: () => 'CURRENT_TIMESTAMP' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'bigint', nullable: true })
    updatedBy?: number;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;
}
