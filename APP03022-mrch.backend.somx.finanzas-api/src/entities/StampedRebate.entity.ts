import { Entity, Column, PrimaryGeneratedColumn, OneToMany } from 'typeorm';
import { Rebate } from './Rebate.entity.js';

@Entity({ name: 'stamped_rebate' })
export class StampedRebate {
    @PrimaryGeneratedColumn('uuid', { name: 'stamped_rebate_uuid' })
    stampedRebateUuid!: string;

    @Column({ name: 'document_number', type: 'varchar', length: 100, nullable: false, unique: true })
    documentNumber!: string;

    @Column({ name: 'reference_number', type: 'varchar', length: 100, nullable: false })
    referenceNumber!: string;

    @Column({ name: 'status', type: 'int', nullable: false, default: 1 })
    status!: number;

    @Column({ name: 'invoice_fiscal_uuid', type: 'uuid', nullable: true })
    invoiceFiscalUuid?: string | null;

    @OneToMany(() => Rebate, rebate => rebate.stampedRebate)
    rebates?: Rebate[];

    // Audit fields
    @Column({ name: 'created_by', type: 'bigint', nullable: true })
    createdBy?: number | null;

    @Column({ name: 'created_at', type: 'timestamp', nullable: false })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'bigint', nullable: true })
    updatedBy?: number | null;

    @Column({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date | null;
}
