import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    ManyToOne,
    JoinColumn,
} from 'typeorm';
import { MigoDocument } from './MigoDocument.entity.js';

@Entity('migo_document_reception')
export class MigoDocumentReception {
    @PrimaryGeneratedColumn('uuid', { name: 'migo_reception_id' })
    migoReceptionId!: string;

    @Column({ name: 'migo_document_id', type: 'uuid', nullable: false })
    migoDocumentId!: string;

    @Column({ name: 'nro_oc', type: 'bigint', nullable: false })
    nroOc!: number;

    @Column({ name: 'nro_recepcion', type: 'bigint', nullable: false })
    nroRecepcion!: number;

    @Column({ name: 'sucursal', type: 'bigint', nullable: false })
    sucursal!: number;

    @Column({ name: 'nro_guia', type: 'varchar', length: 50, nullable: true })
    nroGuia?: string;

    @Column({ name: 'origen', type: 'varchar', length: 50, nullable: true })
    origen?: string;

    @Column({ name: 'fecha_recepcion', type: 'date', nullable: false })
    fechaRecepcion!: Date;

    @Column({ name: 'importe_sin_impuesto', type: 'numeric', precision: 16, scale: 2, nullable: false })
    importeSinImpuesto!: number;

    @Column({ name: 'sku', type: 'varchar', length: 50, nullable: true })
    sku?: string;

    @Column({ name: 'descripcion_sku', type: 'varchar', length: 255, nullable: true })
    descripcionSku?: string;

    @Column({ name: 'cantidad', type: 'numeric', precision: 16, scale: 4, nullable: false })
    cantidad!: number;

    @Column({ name: 'importe_unitario', type: 'numeric', precision: 16, scale: 4, nullable: false })
    importeUnitario!: number;

    @Column({ name: 'importe_sin_impuesto_det', type: 'numeric', precision: 16, scale: 2, nullable: false })
    importeSinImpuestoDet!: number;

    @Column({ name: 'monto_oc', type: 'numeric', precision: 16, scale: 2, nullable: true })
    montoOc?: number;

    @Column({ name: 'is_valid', type: 'boolean', default: true })
    isValid!: boolean;

    @Column({ name: 'validation_error', type: 'varchar', length: 500, nullable: true })
    validationError?: string;

    @Column({ name: 'row_number', type: 'int', nullable: true })
    rowNumber?: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp', default: () => 'CURRENT_TIMESTAMP' })
    createdAt!: Date;

    @ManyToOne(() => MigoDocument, d => d.receptions)
    @JoinColumn({ name: 'migo_document_id' })
    migoDocument?: MigoDocument;
}
