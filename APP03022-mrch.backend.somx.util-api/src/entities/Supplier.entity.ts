import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, UpdateDateColumn } from 'typeorm';

@Entity({ schema: 'shared_catalogs', name: 'supplier' })
export class Supplier {
    @PrimaryGeneratedColumn({ name: 'id' })
    id!: number;

    @Column({ name: 'supplier_number', type: 'varchar', length: 50, nullable: true })
    supplierNumber!: string | null;

    @Column({ name: 'rfc', type: 'varchar', length: 20, nullable: true })
    rfc!: string | null;

    @Column({ name: 'business_name', type: 'varchar', length: 255, nullable: true })
    businessName!: string | null;

    @Column({ name: 'supplier_type_id', type: 'integer', nullable: true })
    supplierTypeId!: number | null;

    @Column({ name: 'logo', type: 'varchar', length: 500, nullable: true })
    logo!: string | null;

    @Column({ name: 'payment_condition_id', type: 'integer', nullable: true })
    paymentConditionId!: number | null;

    @Column({ name: 'status', type: 'integer', nullable: true })
    status!: number | null;

    @Column({ name: 'email_financial', type: 'varchar', length: 255, nullable: true })
    emailFinancial!: string | null;

    @CreateDateColumn({ name: 'created_at', nullable: true })
    createdAt!: Date | null;

    @Column({ name: 'created_by', type: 'varchar', length: 100, nullable: true })
    createdBy!: string | null;

    @UpdateDateColumn({ name: 'updated_at', nullable: true })
    updatedAt!: Date | null;

    @Column({ name: 'updated_by', type: 'varchar', length: 100, nullable: true })
    updatedBy!: string | null;
}
