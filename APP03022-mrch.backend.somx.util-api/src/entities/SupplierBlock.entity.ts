import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    Index,
    BeforeInsert,
    BeforeUpdate
} from 'typeorm';

@Entity({ name: 'supplier_block', schema: 'shared_catalogs' })
@Index('idx_sb_supplier_number', ['supplierNumber'])
@Index('idx_sb_valid_range', ['validFrom', 'validTo'])
@Index('idx_sb_status', ['status'])
export class SupplierBlock {
    public static readonly STATUS_ACTIVE = 1;
    public static readonly STATUS_INACTIVE = 0;

    @PrimaryGeneratedColumn({ name: 'id', type: 'integer' })
    id!: number;

    @Column({ name: 'supplier_number', type: 'varchar', length: 20, nullable: false })
    supplierNumber!: string;

    @Column({ name: 'valid_from', type: 'date', nullable: false })
    validFrom!: string;

    @Column({ name: 'valid_to', type: 'date', nullable: false })
    validTo!: string;

    @Column({ name: 'block_reason', type: 'varchar', length: 255, nullable: true })
    blockReason?: string | null;

    @Column({ name: 'status', type: 'integer', nullable: false, default: 1 })
    status!: number;

    @Column({ name: 'created_at', type: 'timestamp', nullable: false, update: false })
    createdAt!: Date;

    @Column({ name: 'created_by', type: 'varchar', length: 100, nullable: true })
    createdBy?: string | null;

    @Column({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date | null;

    @Column({ name: 'updated_by', type: 'varchar', length: 100, nullable: true })
    updatedBy?: string | null;

    @BeforeInsert()
    onCreate() {
        this.createdAt = new Date();
    }

    @BeforeUpdate()
    onUpdate() {
        this.updatedAt = new Date();
    }

    isCurrentlyBlocked(): boolean {
        if (this.status !== SupplierBlock.STATUS_ACTIVE) return false;
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const from = new Date(this.validFrom);
        const to = new Date(this.validTo);
        return today >= from && today <= to;
    }
}

