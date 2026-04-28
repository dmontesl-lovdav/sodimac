import { Column, CreateDateColumn, Entity, PrimaryGeneratedColumn, UpdateDateColumn } from 'typeorm';

@Entity({ schema: 'shared_catalogs', name: 'catalog_detail' })
export class CatalogDetail {
    @PrimaryGeneratedColumn({ name: 'id' })
    id!: number;

    @Column({ name: 'header_id', type: 'integer' })
    headerId!: number;

    /** Columna SQL `"key"` */
    @Column({ name: 'key', type: 'varchar', length: 200 })
    detailKey!: string;

    @Column({ name: 'dict_id', type: 'integer' })
    dictId!: number;

    @Column({ name: 'color', type: 'varchar', length: 100, nullable: true })
    color?: string | null;

    @Column({ name: 'sort_order', type: 'integer', nullable: true })
    sortOrder?: number | null;

    @Column({ name: 'status', type: 'smallint', default: 1 })
    status!: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp' })
    createdAt!: Date;

    @Column({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date | null;

    @Column({ name: 'internal_status', type: 'integer', nullable: true })
    internalStatus?: number | null;

    @Column({ name: 'external_key', type: 'varchar', length: 200, nullable: true })
    externalKey?: string | null;

    @Column({ name: 'value', type: 'varchar', length: 2000, nullable: true })
    value?: string | null;

    @Column({ name: 'valid_from', type: 'date', nullable: true })
    validFrom?: Date | null;

    @Column({ name: 'valid_to', type: 'date', nullable: true })
    validTo?: Date | null;

    @Column({ name: 'created_by', type: 'varchar', length: 100, nullable: true })
    createdBy?: string | null;

    @Column({ name: 'attributes', type: 'text', nullable: true })
    attributes?: string | null;

    @Column({ name: 'parent_catalog_id', type: 'integer', nullable: true })
    parentCatalogId?: number | null;

    @Column({ name: 'parent_element_id', type: 'integer', nullable: true })
    parentElementId?: number | null;

    @Column({ name: 'updated_by', type: 'varchar', length: 100, nullable: true })
    updatedBy?: string | null;
}
