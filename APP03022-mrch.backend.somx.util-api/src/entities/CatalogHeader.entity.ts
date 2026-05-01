
import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    Index,
    BeforeInsert,
    BeforeUpdate
} from 'typeorm';

@Entity({ name: 'catalog_header', schema: 'shared_catalogs' })
@Index('idx_catalog_header_code', ['code'], { unique: true })
@Index('idx_catalog_header_module', ['module'])
@Index('idx_catalog_header_status', ['status'])
export class CatalogHeader {
    public static readonly STATUS_ACTIVE = 1;
    public static readonly STATUS_INACTIVE = 0;

    @PrimaryGeneratedColumn({ name: 'id', type: 'integer' })
    id!: number;

    @Column({ name: 'code', type: 'varchar', length: 64, nullable: false, unique: true })
    code!: string;

    @Column({ name: 'prefix', type: 'varchar', length: 4, nullable: false })
    prefix!: string;

    @Column({ name: 'name', type: 'varchar', length: 128, nullable: false })
    name!: string;

    @Column({ name: 'description', type: 'varchar', length: 512, nullable: true })
    description?: string | null;

    @Column({ name: 'module', type: 'varchar', length: 32, nullable: true })
    module?: string | null;

    @Column({ name: 'catalog_type', type: 'varchar', length: 20, nullable: false, default: 'SIMPLE' })
    catalogType!: string;

    @Column({ name: 'status', type: 'integer', nullable: false, default: 1 })
    status!: number;

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
