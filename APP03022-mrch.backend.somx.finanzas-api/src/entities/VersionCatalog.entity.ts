import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
    ManyToOne,
    JoinColumn
} from 'typeorm';
import { PacCatalog } from './PacCatalog.entity.js';

@Entity('version_catalog')
export class VersionCatalog {
    @PrimaryGeneratedColumn({ name: 'version_id' })
    versionId!: number;

    @Column({ type: 'varchar', length: 50, nullable: false })
    name!: string;

    @Column({ type: 'varchar', length: 254, nullable: true })
    description?: string;

    @Column({ type: 'numeric', precision: 7, scale: 3, nullable: false })
    version!: number;

    @Column({ name: 'document_type', type: 'char', length: 1, nullable: false })
    documentType!: string;

    @Column({ name: 'pac_id', type: 'integer', nullable: false })
    pacId!: number;

    @Column({ name: 'valid_from', type: 'timestamp', nullable: true })
    validFrom?: Date;

    @Column({ name: 'valid_to', type: 'timestamp', nullable: true })
    validTo?: Date;

    @Column({ name: 'structure_url', type: 'varchar', length: 254, nullable: true })
    structureUrl?: string;

    @Column({ type: 'integer', default: 1 })
    status!: number;

    @Column({ name: 'created_by', type: 'bigint', nullable: true })
    createdBy?: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp', default: () => 'CURRENT_TIMESTAMP' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'bigint', nullable: true })
    updatedBy?: number;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;

    @ManyToOne(
        () => PacCatalog,
        (pac) => pac.versions
    )
    @JoinColumn({ name: 'pac_id' })
    pac!: any;

}
