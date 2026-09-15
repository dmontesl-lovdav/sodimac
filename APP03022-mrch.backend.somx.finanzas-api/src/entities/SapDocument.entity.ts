import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
    OneToMany,
} from 'typeorm';
import { SapDocumentFiscalUuid } from './SapDocumentFiscalUuid.entity.js';

/**
 * Documentos del sistema SAP (tenant_finance.sap_document)
 */
@Entity('sap_document')
export class SapDocument {
    @PrimaryGeneratedColumn('uuid', { name: 'sap_document_uuid' })
    sapDocumentUuid!: string;

    @Column({ name: 'document_number', type: 'varchar', length: 100 })
    documentNumber!: string;

    @Column({ name: 'reference_number', type: 'varchar', length: 100 })
    referenceNumber!: string;

    @Column({ name: 'vendor_number', type: 'int' })
    vendorNumber!: number;

    @Column({ name: 'amount', type: 'numeric', precision: 15, scale: 2 })
    amount!: number;

    @Column({ name: 'source', type: 'int' })
    source!: number;

    @Column({ name: 'doc_sap', type: 'varchar', length: 15 })
    docSap!: string;

    @Column({ name: 'message', type: 'varchar', length: 254, nullable: true })
    message?: string | null;

    @Column({ name: 'sap_status', type: 'int', default: 1 })
    sapStatus!: number;

    @Column({ name: 'document_type', type: 'varchar', length: 5 })
    documentType!: string;

    @Column({ name: 'created_by', type: 'bigint', nullable: true })
    createdBy?: number | null;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp', default: () => 'CURRENT_TIMESTAMP' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'bigint', nullable: true })
    updatedBy?: number | null;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date | null;

    @OneToMany(() => SapDocumentFiscalUuid, (row) => row.sapDocument, {
        cascade: ['insert'],
    })
    fiscalUuids?: SapDocumentFiscalUuid[];
}
