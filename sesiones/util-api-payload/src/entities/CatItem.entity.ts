// src/entities/CatItem.entity.ts
import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
    Index
} from 'typeorm';

/**
 * Entidad CatItem - Catalogo de elementos
 * Tabla: cat_item
 * JIRA: STM-1212
 */
@Entity('cat_item')
@Index('idx_cat_item_name', ['name'])
export class CatItem {
    @PrimaryGeneratedColumn({ name: 'id_item' })
    idItem!: number;

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
