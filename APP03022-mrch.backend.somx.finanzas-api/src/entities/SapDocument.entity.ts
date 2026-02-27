import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn
} from 'typeorm';

/**
 * Documentos del sistema SAP
 */
@Entity('sap_document')
export class SapDocument {
    @PrimaryGeneratedColumn('uuid', { name: 'sap_document_id' })
    sapDocumentId!: string;

    @Column({ name: 'document_number', type: 'varchar', length: 100, nullable: true })
    documentNumber?: string;

    @Column({ name: 'document_reference', type: 'varchar', length: 100, nullable: true })
    documentReference?: string;

    @Column({ name: 'supplier_number', type: 'bigint', nullable: true })
    supplierNumber?: number;

    @Column({ name: 'amount', type: 'numeric', precision: 16, scale: 2, nullable: true })
    amount?: number;

    @Column({ name: 'sap_code', type: 'varchar', length: 10, nullable: true })
    sapCode?: string;

    @Column({ name: 'message', type: 'varchar', length: 254, nullable: true })
    message?: string;

    @Column({ name: 'sap_status', type: 'numeric', precision: 2, nullable: true })
    sapStatus?: number;

    @Column({ name: 'document_type', type: 'varchar', length: 5, nullable: true })
    documentType?: string;

    // Auditoría
    @Column({ name: 'created_by', type: 'bigint', nullable: true })
    createdBy?: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp', default: () => 'CURRENT_TIMESTAMP' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'bigint', nullable: true })
    updatedBy?: number;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;
}
