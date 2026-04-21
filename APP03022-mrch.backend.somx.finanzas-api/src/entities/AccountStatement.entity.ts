import { Entity, Column, PrimaryGeneratedColumn } from 'typeorm';

@Entity({ name: 'account_statement' })
export class AccountStatement {
    @PrimaryGeneratedColumn('uuid', { name: 'account_statement_uuid' })
    accountStatementUuid!: string;

    @Column({ name: 'vendor_number', type: 'bigint', nullable: false })
    vendorNumber!: number;

    @Column({ name: 'year', type: 'int', nullable: false })
    year!: number;

    @Column({ name: 'month', type: 'smallint', nullable: false })
    month!: number;

    @Column({ name: 'version', type: 'int', nullable: false, default: 1 })
    version!: number;

    @Column({ name: 'status', type: 'int', nullable: false })
    status!: number;

    @Column({ name: 'initial_balance', type: 'decimal', precision: 18, scale: 2, nullable: true })
    initialBalance?: string | null;

    @Column({ name: 'final_balance', type: 'decimal', precision: 18, scale: 2, nullable: true })
    finalBalance?: string | null;

    @Column({ name: 'process_date', type: 'timestamp', nullable: true })
    processedAt?: Date | null;

    @Column({ name: 'review_date', type: 'timestamp', nullable: true })
    reviewedAt?: Date | null;

    @Column({ name: 'issue_date', type: 'timestamp', nullable: true })
    issuedAt?: Date | null;

    @Column({ name: 'period_start_date', type: 'date', nullable: true })
    periodStart?: Date | null;
    
    @Column({ name: 'period_end_date', type: 'date', nullable: true })
    periodEnd?: Date | null;

    @Column({ name: 'previous_statement_uuid', type: 'uuid', nullable: true })
    previousStatementUuid?: string | null;

    @Column({ name: 'created_by', type: 'bigint', nullable: true })
    createdBy?: number | null;

    @Column({ name: 'created_at', type: 'timestamp', nullable: true })
    createdAt?: Date | null;

    @Column({ name: 'updated_by', type: 'bigint', nullable: true })
    updatedBy?: number | null;

    @Column({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date | null;
}
