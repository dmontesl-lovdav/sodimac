import { Entity, Column, PrimaryColumn } from 'typeorm';

@Entity('accounts_payable')
export class AccountsPayable {
  @PrimaryColumn('uuid', { name: 'accounts_payable_uuid' })
  accountsPayableUuid!: string;

  @Column({ name: 'company', type: 'integer' })
  company!: number;

  @Column({ name: 'document_date', type: 'date' })
  documentDate!: Date;

  @Column({ name: 'reference_number', type: 'varchar', length: 100 })
  referenceNumber!: string;

  @Column({ name: 'document_number', type: 'varchar', length: 100 })
  documentNumber!: string;

  @Column({ name: 'currency', type: 'varchar', length: 3, default: 'MXN' })
  currency!: string;

  @Column({ name: 'exchange_rate', type: 'numeric', precision: 18, scale: 6, default: 1 })
  exchangeRate!: number;

  @Column({ name: 'debit_credit', type: 'varchar', length: 1 })
  debitCredit!: string;

  @Column({ name: 'gl_account', type: 'varchar', length: 20 })
  glAccount!: string;

  @Column({ name: 'vendor_number', type: 'integer' })
  vendorNumber!: number;

  @Column({ name: 'amount', type: 'numeric', precision: 15, scale: 2 })
  amount!: number;

  @Column({ name: 'branch', type: 'integer' })
  branch!: number;

  @Column({ name: 'payment_term', type: 'varchar', length: 5 })
  paymentTerm!: string;

  @Column({ name: 'due_date', type: 'date' })
  dueDate!: Date;

  @Column({ name: 'hold_indicator', type: 'varchar', length: 1, default: 'N' })
  holdIndicator!: string;

  @Column({ name: 'source_system', type: 'varchar', length: 20 })
  sourceSystem!: string;

  @Column({ name: 'sent_at', type: 'timestamp', nullable: true })
  sentAt!: Date;

  @Column({ name: 'posting_date', type: 'date' })
  postingDate!: Date;

  @Column({ name: 'document_class', type: 'varchar', length: 5 })
  documentClass!: string;

  @Column({ name: 'reference_id', type: 'varchar', length: 40 })
  referenceId!: string;

  @Column({ name: 'cost_center', type: 'varchar', length: 15 })
  costCenter!: string;

  @Column({ name: 'profit_center', type: 'varchar', length: 15 })
  profitCenter!: string;

  @Column({ name: 'sent_flag', type: 'integer', default: 0 })
  sentFlag!: number;

  @Column({ name: 'receipt_date', type: 'date' })
  receiptDate!: Date;

  @Column({ name: 'document_type', type: 'varchar', length: 15 })
  documentType!: string;

  @Column({ name: 'etl_source', type: 'varchar', length: 20 })
  etlSource!: string;

  @Column({ name: 'period_id', type: 'integer' })
  periodId!: number;

  @Column({ name: 'resent_by', type: 'bigint', nullable: true })
  resentBy!: number;

  @Column({ name: 'resent_at', type: 'timestamp', nullable: true })
  resentAt!: Date;

  @Column({ name: 'created_by', type: 'bigint', nullable: true })
  createdBy!: number;

  @Column({ name: 'created_at', type: 'timestamp', default: () => 'CURRENT_TIMESTAMP' })
  createdAt!: Date;

  @Column({ name: 'updated_by', type: 'bigint', nullable: true })
  updatedBy!: number;

  @Column({ name: 'updated_at', type: 'timestamp', nullable: true })
  updatedAt!: Date;
}
