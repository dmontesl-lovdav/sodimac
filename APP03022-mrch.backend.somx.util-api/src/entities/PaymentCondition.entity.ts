import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    Index,
    BeforeInsert,
    BeforeUpdate
} from 'typeorm';

@Entity({ name: 'payment_condition', schema: 'shared_catalogs' })
@Index('idx_pc_supplier_number', ['supplierNumber'])
@Index('idx_pc_status', ['status'])
export class PaymentCondition {
    public static readonly STATUS_ACTIVE = 1;
    public static readonly STATUS_INACTIVE = 0;

    @PrimaryGeneratedColumn({ name: 'id', type: 'integer' })
    id!: number;

    @Column({ name: 'supplier_number', type: 'varchar', length: 20, nullable: true })
    supplierNumber?: string | null;

    @Column({ name: 'condition_name', type: 'varchar', length: 100, nullable: false })
    conditionName!: string;

    @Column({ name: 'days', type: 'integer', nullable: false, default: 0 })
    days!: number;

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
}

