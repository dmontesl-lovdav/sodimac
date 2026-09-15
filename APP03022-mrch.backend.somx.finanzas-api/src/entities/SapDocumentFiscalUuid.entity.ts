import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    ManyToOne,
    JoinColumn,
} from 'typeorm';
import { SapDocument } from './SapDocument.entity.js';

/**
 * Folios fiscales (UUID SAT) asociados a un documento SAP.
 * Un documento SAP puede tener varios UUID fiscales.
 */
@Entity('sap_document_fiscal_uuid')
export class SapDocumentFiscalUuid {
    @PrimaryGeneratedColumn('uuid', { name: 'id' })
    id!: string;

    @Column({ name: 'sap_document_uuid', type: 'uuid' })
    sapDocumentUuid!: string;

    @Column({ name: 'fiscal_uuid', type: 'uuid' })
    fiscalUuid!: string;

    @Column({ name: 'created_by', type: 'bigint', nullable: true })
    createdBy?: number | null;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp', default: () => 'CURRENT_TIMESTAMP' })
    createdAt!: Date;

    @ManyToOne(() => SapDocument, (doc) => doc.fiscalUuids, { onDelete: 'CASCADE' })
    @JoinColumn({ name: 'sap_document_uuid' })
    sapDocument?: SapDocument;
}
