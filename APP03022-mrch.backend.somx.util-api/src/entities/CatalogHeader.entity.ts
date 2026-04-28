import { Column, CreateDateColumn, Entity, PrimaryGeneratedColumn, UpdateDateColumn } from 'typeorm';

@Entity({ schema: 'shared_catalogs', name: 'catalog_header' })
export class CatalogHeader {
    @PrimaryGeneratedColumn({ name: 'id' })
    id!: number;

    @Column({ name: 'code', type: 'varchar', length: 120 })
    code!: string;

    @Column({ name: 'prefix', type: 'varchar', length: 20, nullable: true })
    prefix?: string;

    @Column({ name: 'name', type: 'varchar', length: 200 })
    name!: string;

    @Column({ name: 'description', type: 'varchar', length: 2000, nullable: true })
    description?: string;

    @Column({ name: 'module', type: 'varchar', length: 50, nullable: true })
    appModule!: string | null;

    @Column({ name: 'status', type: 'smallint', default: 1 })
    status!: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp' })
    createdAt!: Date;

    @Column({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date | null;

    @Column({ name: 'catalog_type', type: 'varchar', length: 40, nullable: true })
    catalogType?: string | null;

    @Column({ name: 'created_by', type: 'integer', nullable: true })
    createdBy?: number | null;

    @Column({ name: 'updated_by', type: 'integer', nullable: true })
    updatedBy?: number | null;
}
