import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
} from 'typeorm';

@Entity({ schema: 'shared_catalogs', name: 'supplier' })
export class SharedSupplier {
    @PrimaryGeneratedColumn({ name: 'id', type: 'int' })
    id!: number;

    @Column({ name: 'supplier_number', type: 'varchar', length: 255, nullable: false })
    supplierNumber!: string;

    @Column({ name: 'rfc', type: 'varchar', length: 255, nullable: true })
    rfc?: string;

    @Column({ name: 'business_name', type: 'varchar', length: 500, nullable: true })
    businessName?: string;

    @Column({ name: 'supplier_type_id', type: 'int', nullable: true })
    supplierTypeId?: number;

    @Column({ name: 'logo', type: 'varchar', length: 500, nullable: true })
    logo?: string;

    @Column({ name: 'payment_condition_id', type: 'int', nullable: true })
    paymentConditionId?: number;

    @Column({ name: 'status', type: 'int', nullable: true })
    status?: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp', nullable: true })
    createdAt?: Date;

    @Column({ name: 'created_by', type: 'varchar', length: 255, nullable: true })
    createdBy?: string;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;

    @Column({ name: 'updated_by', type: 'varchar', length: 255, nullable: true })
    updatedBy?: string;

    @Column({ name: 'email_financial', type: 'varchar', length: 255, nullable: true })
    emailFinancial?: string;
}