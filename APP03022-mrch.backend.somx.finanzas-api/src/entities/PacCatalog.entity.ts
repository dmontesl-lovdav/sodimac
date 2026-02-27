import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
    OneToMany
} from 'typeorm';
import { VersionCatalog } from './VersionCatalog.entity.js';

/**
 * Catálogo de Proveedores Autorizados de Certificación del SAT
 */
@Entity('pac_catalog')
export class PacCatalog {
    @PrimaryGeneratedColumn({ name: 'pac_id' })
    pacId!: number;

    @Column({ type: 'varchar', length: 50, nullable: false })
    name!: string;

    @Column({ type: 'varchar', length: 254, nullable: true })
    description?: string;

    @Column({ type: 'varchar', length: 254, nullable: true })
    url?: string;

    @Column({ type: 'varchar', length: 254, nullable: true })
    license?: string;

    @Column({ name: 'valid_from', type: 'date', nullable: true })
    validFrom?: Date;

    @Column({ name: 'valid_to', type: 'date', nullable: true })
    validTo?: Date;

    @Column({ name: 'catalog_msg_id', type: 'numeric', precision: 4, nullable: true })
    catalogMsgId?: number;

    @Column({ type: 'integer', default: 1 })
    status!: number;

    // Auditoría
    @Column({ name: 'created_by', type: 'bigint', nullable: true })
    createdBy?: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp', default: () => 'CURRENT_TIMESTAMP' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'bigint', nullable: true })
    updatedBy?: number;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;

    // Relaciones
    @OneToMany(() => VersionCatalog, versionCatalog => versionCatalog.pac)
    versions?: VersionCatalog[];
}
