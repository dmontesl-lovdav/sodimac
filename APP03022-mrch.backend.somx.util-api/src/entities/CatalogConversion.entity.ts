import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    Index,
    ManyToOne,
    JoinColumn,
    BeforeInsert,
    BeforeUpdate
} from 'typeorm';
import { CatalogDetail } from './CatalogDetail.entity.js';

@Entity({ name: 'catalog_conversion', schema: 'shared_catalogs' })
@Index('idx_cc_source', ['sourceElement'])
@Index('idx_cc_target', ['targetElement'])
@Index('idx_cc_status', ['status'])
export class CatalogConversion {
    public static readonly STATUS_ACTIVE = 1;
    public static readonly STATUS_INACTIVE = 0;

    @PrimaryGeneratedColumn({ name: 'id', type: 'integer' })
    id!: number;

    @ManyToOne(() => CatalogDetail, { nullable: false })
    @JoinColumn({ name: 'source_element_id' })
    sourceElement!: CatalogDetail;

    @Column({ name: 'source_element_id', type: 'integer', nullable: false })
    sourceElementId!: number;

    @ManyToOne(() => CatalogDetail, { nullable: false })
    @JoinColumn({ name: 'target_element_id' })
    targetElement!: CatalogDetail;

    @Column({ name: 'target_element_id', type: 'integer', nullable: false })
    targetElementId!: number;

    @Column({ name: 'valid_from', type: 'date', nullable: true })
    validFrom?: string | null;

    @Column({ name: 'valid_to', type: 'date', nullable: true })
    validTo?: string | null;

    @Column({ name: 'status', type: 'integer', nullable: false, default: 1 })
    status!: number;

    @Column({ name: 'is_principal', type: 'boolean', nullable: false, default: false })
    isPrincipal!: boolean;

    @Column({ name: 'created_by', type: 'varchar', length: 100, nullable: true })
    createdBy?: string | null;

    @Column({ name: 'updated_by', type: 'varchar', length: 100, nullable: true })
    updatedBy?: string | null;

    @Column({ name: 'created_at', type: 'timestamp', nullable: false, update: false })
    createdAt!: Date;

    @Column({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date | null;

    @BeforeInsert()
    onCreate() {
        this.createdAt = new Date();
    }

    @BeforeUpdate()
    onUpdate() {
        this.updatedAt = new Date();
    }
}

