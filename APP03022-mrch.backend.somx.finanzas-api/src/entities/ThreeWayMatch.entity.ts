// src/entities/ThreeWayMatch.entity.ts
import {
    Entity,
    Column,
    PrimaryGeneratedColumn,
    Index,
    CreateDateColumn,
    UpdateDateColumn,
} from "typeorm";

@Entity({ schema: "tenant_finance", name: "three_way_match" })
@Index(
    ["numeroProveedor", "ordenCompra", "recepcion"],
    { unique: true }
)
export class ThreeWayMatch {

    @PrimaryGeneratedColumn("uuid", { name: "three_way_match_uuid" })
    id!: string;

    @Column({ name: "vendor_number", type: "varchar", length: 50 })
    numeroProveedor!: string;

    @Column({ name: "purchase_order_number", type: "varchar", length: 50 })
    ordenCompra!: string;

    @Column({ name: "purchase_order_date", type: "date" })
    fechaOrdenCompra!: Date;

    @Column({ name: "purchase_order_amount", type: "numeric", precision: 18, scale: 2, nullable: true })
    montoOrdenCompra!: number | null;

    @Column({ name: "reception_number", type: "varchar", length: 50 })
    recepcion!: string;

    @Column({ name: "reception_date", type: "date" })
    fechaRecepcion!: Date;

    @Column({ name: "reception_amount", type: "numeric", precision: 18, scale: 2, nullable: true })
    montoRecepcion!: number | null;

    @Column({ name: "invoice_series", type: "varchar", length: 20, nullable: true })
    serie!: string | null;

    @Column({ name: "invoice_folio", type: "varchar", length: 50, nullable: true })
    folio!: string | null;

    @Column({ name: "invoice_uuid", type: "varchar", length: 50, nullable: true })
    uuid!: string | null;

    @Column({ name: "invoice_stamp_date", type: "date", nullable: true })
    fechaTimbrado!: Date | null;

    @Column({ name: "invoice_amount", type: "numeric", precision: 18, scale: 2, nullable: true })
    montoFactura!: number | null;

    @Column({ name: "credit_note_number", type: "varchar", length: 50, nullable: true })
    numeroNotaCredito!: string | null;

    @Column({ name: "credit_note_amount", type: "numeric", precision: 18, scale: 2, nullable: true })
    montoNotaCredito!: number | null;

    @Column({ name: "document_number", type: "varchar", length: 50, nullable: true })
    numeroDocumento!: string | null;

    @Column({ name: "sap_document", type: "varchar", length: 50, nullable: true })
    documentoSap!: string | null;

    @Column({ name: "accounting_date", type: "date", nullable: true })
    fechaContable!: Date | null;

    @Column({ name: "accounting_amount", type: "numeric", precision: 18, scale: 2, nullable: true })
    montoContable!: number | null;

    @Column({ name: "payment_reference", type: "varchar", length: 50, nullable: true })
    referenciaPago!: string | null;

    @Column({ name: "payment_date", type: "date", nullable: true })
    fechaPago!: Date | null;

    @Column({ name: "payment_amount", type: "numeric", precision: 18, scale: 2, nullable: true })
    montoPago!: number | null;

    @Column({ name: "currency", type: "varchar", length: 10, nullable: true })
    currency!: string | null;

    @Column({ name: "exchange_rate", type: "numeric", precision: 18, scale: 6, nullable: true })
    exchangeRate!: number | null;

    @Column({ name: "status", type: "int" })
    estatus!: number;

    @Column({ name: "created_by", type: "int", nullable: true })
    createdBy!: number | null;

    @CreateDateColumn({ name: "created_at", type: "timestamp with time zone" })
    createdAt!: Date;

    @Column({ name: "updated_by", type: "int", nullable: true })
    updatedBy!: number | null;

    @UpdateDateColumn({ name: "updated_at", type: "timestamp with time zone" })
    updatedAt!: Date;
}
