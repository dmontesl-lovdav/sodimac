import { Entity, Column, PrimaryGeneratedColumn } from 'typeorm';

@Entity({ name: 'transaction_ids' })
export class TransactionId {
    @PrimaryGeneratedColumn('uuid', { name: 'transaction_id_uuid' })
    transactionIdUuid!: string;

    @Column({ name: 'folio_visible', type: 'varchar', length: 13, nullable: false })
    folioVisible!: string;

    @Column({ name: 'uuid_interno', type: 'uuid', nullable: false })
    uuidInterno!: string;

    @Column({ name: 'codigo_modulo', type: 'varchar', length: 4, nullable: false })
    codigoModulo!: string;

    @Column({ name: 'pantalla_origen', type: 'varchar', length: 100, nullable: false })
    pantallaOrigen!: string;

    @Column({ name: 'caso', type: 'varchar', length: 200, nullable: false })
    caso!: string;

    @Column({ name: 'id_usuario', type: 'varchar', length: 100, nullable: false })
    idUsuario!: string;

    @Column({ name: 'origen', type: 'varchar', length: 150, nullable: false })
    origen!: string;

    @Column({ name: 'estatus', type: 'varchar', length: 30, nullable: false, default: 'GENERATED' })
    estatus!: string;

    @Column({ name: 'metadatos', type: 'jsonb', nullable: true })
    metadatos?: Record<string, unknown> | null;

    @Column({ name: 'created_at', type: 'timestamp', nullable: false })
    createdAt!: Date;
}