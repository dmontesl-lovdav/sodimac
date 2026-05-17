// src/entities/CatMessage.entity.ts
import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
    Index
} from 'typeorm';

/**
 * Entidad CatMessage - Catalogo de mensajes del sistema
 * Tabla: cat_message
 * JIRA: STM-1212
 */
@Entity('cat_message')
@Index('idx_cat_message_code', ['messageCode'])
@Index('idx_cat_message_type', ['idMessageType'])
export class CatMessage {
    @PrimaryGeneratedColumn({ name: 'id_message' })
    idMessage!: number;

    @Column({ name: 'message_code', type: 'varchar', length: 50, nullable: false })
    messageCode!: string;

    @Column({ name: 'id_message_type', type: 'integer', nullable: false })
    idMessageType!: number;

    @Column({ name: 'description', type: 'varchar', length: 500, nullable: true })
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
