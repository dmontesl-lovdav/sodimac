import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
} from 'typeorm';

@Entity({ schema: 'shared_catalogs', name: 'catalog_header' })
export class SharedCatalogHeader {
    @PrimaryGeneratedColumn({ name: 'id', type: 'int' })
    id!: number;

    @Column({ name: 'code', type: 'varchar', length: 255, nullable: false })
    code!: string;

    @Column({ name: 'prefix', type: 'varchar', length: 255, nullable: true })
    prefix?: string;

    @Column({ name: 'name', type: 'varchar', length: 255, nullable: true })
    name?: string;

    @Column({ name: 'description', type: 'varchar', length: 500, nullable: true })
    description?: string;

    @Column({ name: 'module', type: 'varchar', length: 255, nullable: true })
    module?: string;

    @Column({ name: 'status', type: 'int', nullable: true })
    status?: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp', nullable: true })
    createdAt?: Date;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;

    @Column({ name: 'catalog_type', type: 'varchar', length: 255, nullable: true })
    catalogType?: string;

    @Column({ name: 'created_by', type: 'varchar', length: 255, nullable: true })
    createdBy?: string;

    @Column({ name: 'updated_by', type: 'varchar', length: 255, nullable: true })
    updatedBy?: string;
}