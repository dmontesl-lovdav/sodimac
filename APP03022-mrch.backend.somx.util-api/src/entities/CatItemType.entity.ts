// src/entities/CatItemType.entity.ts
import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
    Index
} from 'typeorm';

/**
 * Entidad CatItemType - Catalogo de tipos de elemento
 * Tabla: cat_item_type
 * JIRA: STM-1212
 */
@Entity('cat_item_type')
@Index('idx_cat_item_type_name', ['name'])
export class CatItemType {
    @PrimaryGeneratedColumn({ name: 'id_item_type' })
    idItemType!: number;

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
