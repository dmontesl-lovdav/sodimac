import { Entity, Column, PrimaryGeneratedColumn } from 'typeorm';

@Entity({ name: 'finanzas_payments' })
export class FinanzasPayment {
    @PrimaryGeneratedColumn('uuid', { name: 'finanzas_payment_uuid' })
    finanzasPaymentUuid!: string;

    @Column({ name: 'company', type: 'int', nullable: false })
    company!: number;

    @Column({ name: 'document_number', type: 'varchar', length: 100, nullable: false })
    documentNumber!: string;

    @Column({ name: 'document_reference', type: 'varchar', length: 100, nullable: false })
    documentReference!: string;

    @Column({ name: 'vendor_number', type: 'int', nullable: false })
    vendorNumber!: number;

    @Column({ name: 'amount', type: 'decimal', precision: 15, scale: 2, nullable: false })
    amount!: string;

    @Column({ name: 'currency', type: 'varchar', length: 3, nullable: false, default: 'MXN' })
    currency!: string;

    @Column({ name: 'document_type', type: 'varchar', length: 5, nullable: false })
    documentType!: string;

    @Column({ name: 'sap_document', type: 'varchar', length: 50, nullable: false })
    sapDocument!: string | null;

    @Column({ name: 'payment_date', type: 'date', nullable: false })
    paymentDate!: Date;

    @Column({ name: 'status', type: 'int', nullable: false, default: 1 })
    status!: number;

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
