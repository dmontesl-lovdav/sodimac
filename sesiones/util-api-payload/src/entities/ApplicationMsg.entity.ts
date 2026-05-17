// src/entities/ApplicationMsg.entity.ts
import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    Index
} from 'typeorm';

/**
 * Entidad ApplicationMsg - Mensajes por aplicativo
 * Tabla: application_msg
 * JIRA: STM-1212
 */
@Entity('application_msg')
@Index('idx_application_msg_app', ['idApplication'])
@Index('idx_application_msg_message', ['idMessage'])
export class ApplicationMsg {
    @PrimaryGeneratedColumn({ name: 'id_application_msg' })
    idApplicationMsg!: number;

    @Column({ name: 'id_message', type: 'integer', nullable: false })
    idMessage!: number;

    @Column({ name: 'id_application', type: 'integer', nullable: false })
    idApplication!: number;

    // Campos de auditoria (solo creacion, no actualizacion segun modelo)
    @Column({ name: 'created_by', type: 'bigint', nullable: true })
    createdBy?: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp', default: () => 'CURRENT_TIMESTAMP' })
    createdAt!: Date;
}
