import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
} from 'typeorm';

@Entity({ schema: 'shared_catalogs', name: 'catalog_detail' })
export class SharedCatalogDetail {
    @PrimaryGeneratedColumn({ name: 'id', type: 'int' })
    id!: number;

    @Column({ name: 'header_id', type: 'int', nullable: false })
    headerId!: number;

    @Column({ name: 'key', type: 'varchar', length: 255, nullable: true })
    key?: string;

    @Column({ name: 'dict_id', type: 'int', nullable: true })
    dictId?: number;

    @Column({ name: 'color', type: 'varchar', length: 255, nullable: true })
    color?: string;

    @Column({ name: 'sort_order', type: 'int', nullable: true })
    sortOrder?: number;

    @Column({ name: 'status', type: 'int', nullable: true })
    status?: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp', nullable: true })
    createdAt?: Date;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;

    @Column({ name: 'internal_status', type: 'int', nullable: true })
    internalStatus?: number;

    @Column({ name: 'external_key', type: 'varchar', length: 255, nullable: true })
    externalKey?: string;

    @Column({ name: 'value', type: 'varchar', length: 500, nullable: true })
    value?: string;

    @Column({ name: 'valid_from', type: 'timestamp', nullable: true })
    validFrom?: Date;

    @Column({ name: 'valid_to', type: 'timestamp', nullable: true })
    validTo?: Date;

    @Column({ name: 'created_by', type: 'varchar', length: 255, nullable: true })
    createdBy?: string;

    @Column({ name: 'attributes', type: 'jsonb', nullable: true })
    attributes?: Record<string, any>;

    @Column({ name: 'parent_catalog_id', type: 'int', nullable: true })
    parentCatalogId?: number;

    @Column({ name: 'parent_element_id', type: 'int', nullable: true })
    parentElementId?: number;

    @Column({ name: 'updated_by', type: 'varchar', length: 255, nullable: true })
    updatedBy?: string;
}