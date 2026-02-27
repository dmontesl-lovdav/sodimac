import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
    ManyToOne,
    JoinColumn
} from 'typeorm';
import { StatusCatalog } from './StatusCatalog.entity.js';
import { OriginCatalog } from './OriginCatalog.entity.js';
import { StampedRebate } from './StampedRebate.entity.js';

/**
 * Reembolsos
 */
@Entity('rebate')
export class Rebate {
    @PrimaryGeneratedColumn('uuid', { name: 'rebate_uuid' })
    rebateId!: string;

    @Column({ name: 'document_number', type: 'varchar', length: 100, nullable: false })
    documentNumber!: string;

    @Column({ name: 'reference_number', type: 'varchar', length: 100, nullable: true })
    documentReference?: string;

    @Column({ name: 'sap_document', type: 'varchar', length: 50, nullable: true })
    sapDocument?: string;

    @Column({ name: 'vendor_number', type: 'integer', nullable: true })
    supplierNumber?: number;

    @Column({ name: 'amount', type: 'numeric', precision: 16, scale: 2, nullable: true })
    amount?: number;

    @Column({ name: 'source', type: 'integer', nullable: true })
    originId?: number;

    @Column({ name: 'period_id', type: 'integer', nullable: true })
    periodId?: number;

    @Column({ name: 'due_date', type: 'timestamp', nullable: true })
    dueDate?: Date;

    @Column({ name: 'posting_date', type: 'timestamp', nullable: true })
    postingDate?: Date;

    @Column({ name: 'status', type: 'numeric', precision: 2, nullable: true })
    status?: number;

    // Auditoría
    @Column({ name: 'created_by', type: 'bigint', nullable: true })
    createdBy?: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp', default: () => 'CURRENT_TIMESTAMP' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'bigint', nullable: true })
    updatedBy?: number;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;

    // Relaciones
    @ManyToOne(() => StatusCatalog)
    @JoinColumn({ name: 'status' })
    statusCatalog?: StatusCatalog;

    @ManyToOne(() => OriginCatalog)
    @JoinColumn({ name: 'source' })
    origin?: OriginCatalog;

    @ManyToOne(() => StampedRebate, stampedRebate => stampedRebate.rebates)
    @JoinColumn({ name: 'document_number', referencedColumnName: 'documentNumber' })
    stampedRebate?: StampedRebate;
}
