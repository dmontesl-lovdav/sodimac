import {
    Entity,
    PrimaryColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
    Check
} from 'typeorm';

/**
 * Catálogo de estados de los procesos
 */
@Entity('status_catalog')
@Check('"status" >= 0 AND "status" <= 99')
export class StatusCatalog {
    @PrimaryColumn({ name: 'status', type: 'numeric', precision: 2 })
    status!: number;

    @Column({ type: 'varchar', length: 50, nullable: false })
    name!: string;

    @Column({ type: 'varchar', length: 256, nullable: true })
    description?: string;

    // Auditoría
    @Column({ name: 'created_by', type: 'bigint', nullable: true })
    createdBy?: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp', default: () => 'CURRENT_TIMESTAMP' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'bigint', nullable: true })
    updatedBy?: number;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;
}
