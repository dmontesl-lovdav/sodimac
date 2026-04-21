import { Entity, Column, PrimaryColumn } from 'typeorm';

@Entity({ name: 'finanzas_payment_headers' })
export class FinanzasPaymentHeader {
    @PrimaryColumn('uuid', { name: 'payment_header_uuid' })
    paymentHeaderUuid!: string;

    @Column({ name: 'company', type: 'int', nullable: false })
    company!: number;

    @Column({ name: 'anio', type: 'int', nullable: false })
    anio!: number;

    @Column({ name: 'vendor_number', type: 'int', nullable: false })
    vendorNumber!: number;

    @Column({ name: 'currency', type: 'varchar', length: 3, nullable: false, default: 'MXN' })
    currency!: string;

    // antes: importe
    @Column({ name: 'total_amount', type: 'decimal', precision: 15, scale: 2, nullable: false })
    totalAmount!: string;

    @Column({ name: 'payment_date', type: 'date', nullable: false })
    paymentDate!: Date;

    @Column({ name: 'status', type: 'int', nullable: false, default: 1 })
    status!: number;

    @Column({ name: 'created_by', type: 'bigint', nullable: true })
    createdBy?: number | null;

    @Column({ name: 'created_at', type: 'timestamp', nullable: false })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'bigint', nullable: true })
    updatedBy?: number | null;

    @Column({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date | null;
}