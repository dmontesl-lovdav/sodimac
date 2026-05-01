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
import { CatalogHeader } from './CatalogHeader.entity.js';

@Entity({ name: 'catalog_detail', schema: 'shared_catalogs' })
@Unique('uk_catalog_detail_header_key', ['header', 'key'])
@Index('idx_catalog_detail_header', ['header'])
@Index('idx_catalog_detail_key', ['key'])
@Index('idx_catalog_detail_dict', ['dictId'])
@Index('idx_catalog_detail_status', ['status'])
export class CatalogDetail {
    public static readonly STATUS_ACTIVE = 1;
    public static readonly STATUS_INACTIVE = 0;

    @PrimaryGeneratedColumn({ name: 'id', type: 'integer' })
    id!: number;

    @ManyToOne(() => CatalogHeader, { nullable: false })
    @JoinColumn({ name: 'header_id' })
    header!: CatalogHeader;

    @Column({ name: 'header_id', type: 'integer', nullable: false })
    headerId!: number;

    @Column({ name: 'key', type: 'varchar', length: 64, nullable: false })
    key!: string;

    @Column({ name: 'dict_id', type: 'integer', nullable: false })
    dictId!: number;

    @Column({ name: 'color', type: 'varchar', length: 16, nullable: true })
    color?: string | null;

    @Column({ name: 'sort_order', type: 'integer', nullable: false, default: 0 })
    sortOrder!: number;

    @Column({ name: 'status', type: 'integer', nullable: false, default: 1 })
    status!: number;

    @Column({ name: 'internal_status', type: 'integer', nullable: true })
    internalStatus?: number | null;

    @Column({ name: 'external_key', type: 'varchar', length: 50, nullable: true })
    externalKey?: string | null;

    @Column({ name: 'value', type: 'varchar', length: 100, nullable: true })
    value?: string | null;

    @Column({ name: 'valid_from', type: 'date', nullable: true })
    validFrom?: string | null;

    @Column({ name: 'valid_to', type: 'date', nullable: true })
    validTo?: string | null;


    @Column({ name: 'created_by', type: 'varchar', length: 100, nullable: true })
    createdBy?: string | null;


    @Column({ name: 'updated_by', type: 'varchar', length: 100, nullable: true })
    updatedBy?: string | null;


    @Column({ name: 'parent_catalog_id', type: 'integer', nullable: true })
    parentCatalogId?: number | null;

    @Column({ name: 'parent_element_id', type: 'integer', nullable: true })
    parentElementId?: number | null;


    @Column({ name: 'attributes', type: 'jsonb', nullable: true })
    attributes?: any;

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


