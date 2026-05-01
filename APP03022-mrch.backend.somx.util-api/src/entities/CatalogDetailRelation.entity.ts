import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    Index,
    ManyToOne,
    JoinColumn,
    Unique,
    BeforeInsert,
    BeforeUpdate
} from 'typeorm';
import { CatalogDetail } from './CatalogDetail.entity.js';

@Entity({ name: 'catalog_detail_relation', schema: 'shared_catalogs' })
@Unique('uk_catalog_detail_relation', ['sourceDetail', 'targetDetail', 'relationType'])
@Index('idx_cdr_source', ['sourceDetail'])
@Index('idx_cdr_target', ['targetDetail'])
@Index('idx_cdr_type', ['relationType'])
@Index('idx_cdr_status', ['status'])
export class CatalogDetailRelation {
    public static readonly STATUS_ACTIVE = 1;
    public static readonly STATUS_INACTIVE = 0;

    public static readonly RELATION_DEPENDS_ON = 'DEPENDS_ON';
    public static readonly RELATION_REQUIRES = 'REQUIRES';
    public static readonly RELATION_EXCLUDES = 'EXCLUDES';
    public static readonly RELATION_PARENT_OF = 'PARENT_OF';

    @PrimaryGeneratedColumn({ name: 'id', type: 'integer' })
    id!: number;

    @ManyToOne(() => CatalogDetail, { nullable: false })
    @JoinColumn({ name: 'source_detail_id' })
    sourceDetail!: CatalogDetail;

    @Column({ name: 'source_detail_id', type: 'integer', nullable: false })
    sourceDetailId!: number;

    @ManyToOne(() => CatalogDetail, { nullable: false })
    @JoinColumn({ name: 'target_detail_id' })
    targetDetail!: CatalogDetail;

    @Column({ name: 'target_detail_id', type: 'integer', nullable: false })
    targetDetailId!: number;

    @Column({ name: 'relation_type', type: 'varchar', length: 20, nullable: false, default: 'DEPENDS_ON' })
    relationType!: string;

    @Column({ name: 'status', type: 'integer', nullable: false, default: 1 })
    status!: number;

    @Column({ name: 'valid_from', type: 'date', nullable: true })
    validFrom?: string | null;

    @Column({ name: 'valid_to', type: 'date', nullable: true })
    validTo?: string | null;

    @Column({ name: 'created_at', type: 'timestamp', nullable: false, update: false })
    createdAt!: Date;

    @Column({ name: 'created_by', type: 'varchar', length: 100, nullable: true })
    createdBy?: string | null;

    @Column({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date | null;

    @Column({ name: 'updated_by', type: 'varchar', length: 100, nullable: true })
    updatedBy?: string | null;

    @BeforeInsert()
    onCreate() {
        this.createdAt = new Date();
    }

    @BeforeUpdate()
    onUpdate() {
        this.updatedAt = new Date();
    }
}

