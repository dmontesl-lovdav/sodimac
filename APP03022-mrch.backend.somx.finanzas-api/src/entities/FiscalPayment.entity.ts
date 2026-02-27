import { Entity, Column, PrimaryGeneratedColumn } from 'typeorm';

@Entity({ name: 'fiscal_payments' })
export class FiscalPayment {
    @PrimaryGeneratedColumn('uuid', { name: 'fiscal_payment_uuid' })
    fiscalPaymentUuid!: string;

    @Column({ name: 'payment_number', type: 'varchar', length: 50, nullable: false, unique: true })
    paymentNumber!: string;

    @Column({ name: 'company', type: 'int', nullable: false })
    company!: number;

    @Column({ name: 'document_number', type: 'varchar', length: 100, nullable: false })
    documentNumber!: string;

    @Column({ name: 'reference_number', type: 'varchar', length: 100, nullable: false })
    referenceNumber!: string;

    @Column({ name: 'vendor_number', type: 'int', nullable: false })
    vendorNumber!: number;

    @Column({ name: 'amount', type: 'decimal', precision: 15, scale: 2, nullable: false })
    amount!: string;

    @Column({ name: 'currency', type: 'varchar', length: 3, nullable: false, default: 'MXN' })
    currency!: string;

    @Column({ name: 'document_type', type: 'varchar', length: 5, nullable: false })
    documentType!: string;

    @Column({ name: 'sap_document', type: 'varchar', length: 50, nullable: true })
    sapDocument?: string | null;

    @Column({ name: 'payment_date', type: 'date', nullable: false })
    paymentDate!: Date;

    @Column({ name: 'status', type: 'int', nullable: false, default: 1 })
    status!: number;

    @Column({ name: 'payment_method', type: 'varchar', length: 10, nullable: true })
    paymentMethod?: string | null;

    @Column({ name: 'bank_account', type: 'varchar', length: 50, nullable: true })
    bankAccount?: string | null;

    @Column({ name: 'reference_payment', type: 'varchar', length: 100, nullable: true })
    referencePayment?: string | null;

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
