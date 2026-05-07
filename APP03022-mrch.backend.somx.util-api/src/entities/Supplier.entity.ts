import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    Index,
    ManyToOne,
    JoinColumn,
    BeforeInsert,
    BeforeUpdate
} from 'typeorm';
import { SupplierType } from './SupplierType.entity.js';
import { PaymentCondition } from './PaymentCondition.entity.js';

@Entity({ name: 'supplier', schema: 'shared_catalogs' })
@Index('idx_supplier_number', ['supplierNumber'], { unique: true })
@Index('idx_supplier_rfc', ['rfc'])
@Index('idx_supplier_type', ['supplierType'])
@Index('idx_supplier_status', ['status'])
export class Supplier {
    public static readonly STATUS_ACTIVE = 1;
    public static readonly STATUS_INACTIVE = 0;

    @PrimaryGeneratedColumn({ name: 'id', type: 'integer' })
    id!: number;

    @Column({ name: 'supplier_number', type: 'varchar', length: 20, nullable: false, unique: true })
    supplierNumber!: string;

    @Column({ name: 'rfc', type: 'varchar', length: 13, nullable: false })
    rfc!: string;

    @Column({ name: 'business_name', type: 'varchar', length: 255, nullable: false })
    businessName!: string;

    @ManyToOne(() => SupplierType, { nullable: true })
    @JoinColumn({ name: 'supplier_type_id' })
    supplierType?: SupplierType | null;

    @Column({ name: 'supplier_type_id', type: 'integer', nullable: true })
    supplierTypeId?: number | null;

    @Column({ name: 'logo', type: 'varchar', length: 500, nullable: true })
    logo?: string | null;

    @ManyToOne(() => PaymentCondition, { nullable: true })
    @JoinColumn({ name: 'payment_condition_id' })
    paymentCondition?: PaymentCondition | null;

    @Column({ name: 'payment_condition_id', type: 'integer', nullable: true })
    paymentConditionId?: number | null;

    @Column({ name: 'email_financial', type: 'varchar', length: 255, nullable: true })
    emailFinancial?: string | null;

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

