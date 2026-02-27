import { Entity, Column, PrimaryGeneratedColumn } from 'typeorm';

@Entity({ name: 'vendor_block' })
export class VendorBlock {
    @PrimaryGeneratedColumn('uuid', { name: 'vendor_block_uuid' })
    vendorBlockUuid!: string;

    @Column({ name: 'vendor_number', type: 'int', nullable: false })
    vendorNumber!: number;

    @Column({ name: 'block_reason', type: 'varchar', length: 10, nullable: false })
    blockReason!: string;

    @Column({ name: 'block_description', type: 'text', nullable: true })
    blockDescription?: string | null;

    @Column({ name: 'start_date', type: 'date', nullable: false })
    startDate!: Date;

    @Column({ name: 'end_date', type: 'date', nullable: true })
    endDate?: Date | null;

    @Column({ name: 'status', type: 'int', nullable: false, default: 1 })
    status!: number;

    @Column({ name: 'auto_unblock', type: 'boolean', nullable: false, default: false })
    autoUnblock!: boolean;

    @Column({ name: 'block_type', type: 'varchar', length: 5, nullable: false })
    blockType!: string;

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
