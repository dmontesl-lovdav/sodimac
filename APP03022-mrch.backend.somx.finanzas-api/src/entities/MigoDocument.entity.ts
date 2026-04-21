import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
    OneToMany,
} from 'typeorm';
import { MigoDocumentReception } from './MigoDocumentReception.entity.js';

export enum MigoStatus {
    PUBLICADO = 9,
    AUTORIZADO = 0,
    RECHAZADO = 8,
}

@Entity('migo_document')
export class MigoDocument {
    @PrimaryGeneratedColumn('uuid', { name: 'migo_document_id' })
    migoDocumentId!: string;

    @Column({ name: 'folio', type: 'varchar', length: 50, nullable: false, unique: true })
    folio!: string;

    @Column({ name: 'file_name', type: 'varchar', length: 255, nullable: false })
    fileName!: string;

    @Column({ name: 'total_records', type: 'int', default: 0 })
    totalRecords!: number;

    @Column({ name: 'numero_oc', type: 'int', default: 0 })
    numeroOc!: number;

    @Column({ name: 'monto_oc', type: 'numeric', precision: 16, scale: 2, default: 0 })
    montoOc!: number;

    @Column({ name: 'numero_recepcion', type: 'int', default: 0 })
    numeroRecepcion!: number;

    @Column({ name: 'numero_rechazo_oc', type: 'int', default: 0 })
    numeroRechazoOc!: number;

    @Column({ name: 'status', type: 'smallint', default: MigoStatus.PUBLICADO })
    status!: number;

    @Column({ name: 'rejection_reason', type: 'varchar', length: 500, nullable: true })
    rejectionReason?: string;

    @Column({ name: 'published_at', type: 'timestamp', nullable: false })
    publishedAt!: Date;

    @Column({ name: 'authorized_at', type: 'timestamp', nullable: true })
    authorizedAt?: Date;

    @Column({ name: 'fecha_flujo', type: 'timestamp', nullable: true })
    fechaFlujo?: Date;

    @Column({ name: 'created_by', type: 'bigint', nullable: true })
    createdBy?: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp', default: () => 'CURRENT_TIMESTAMP' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'bigint', nullable: true })
    updatedBy?: number;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;

    @OneToMany(() => MigoDocumentReception, r => r.migoDocument, { cascade: true, eager: false })
    receptions?: MigoDocumentReception[];
}
