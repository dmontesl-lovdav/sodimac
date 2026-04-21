import { Entity, Column, PrimaryGeneratedColumn } from 'typeorm';

@Entity({ name: 'transaction_id_error_logs' })
export class TransactionIdErrorLog {
    @PrimaryGeneratedColumn('uuid', { name: 'transaction_id_error_log_uuid' })
    transactionIdErrorLogUuid!: string;

    @Column({ name: 'id_usuario', type: 'varchar', length: 100, nullable: true })
    idUsuario?: string | null;

    @Column({ name: 'origen', type: 'varchar', length: 150, nullable: true })
    origen?: string | null;

    @Column({ name: 'codigo_modulo', type: 'varchar', length: 4, nullable: true })
    codigoModulo?: string | null;

    @Column({ name: 'pantalla_origen', type: 'varchar', length: 100, nullable: true })
    pantallaOrigen?: string | null;

    @Column({ name: 'caso', type: 'varchar', length: 200, nullable: true })
    caso?: string | null;

    @Column({ name: 'codigo_error', type: 'varchar', length: 50, nullable: false })
    codigoError!: string;

    @Column({ name: 'descripcion_error', type: 'text', nullable: false })
    descripcionError!: string;

    @Column({ name: 'folio_intentado', type: 'varchar', length: 13, nullable: true })
    folioIntentado?: string | null;

    @Column({ name: 'uuid_intentado', type: 'uuid', nullable: true })
    uuidIntentado?: string | null;

    @Column({ name: 'metadatos', type: 'jsonb', nullable: true })
    metadatos?: Record<string, unknown> | null;

    @Column({ name: 'created_at', type: 'timestamp', nullable: false })
    createdAt!: Date;
}