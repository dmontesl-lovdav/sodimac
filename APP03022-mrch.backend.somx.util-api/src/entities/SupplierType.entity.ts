import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    Index,
    BeforeInsert,
    BeforeUpdate
} from 'typeorm';

@Entity({ name: 'supplier_type', schema: 'shared_catalogs' })
@Index('idx_st_code', ['code'], { unique: true })
@Index('idx_st_status', ['status'])
export class SupplierType {
    public static readonly STATUS_ACTIVE = 1;
    public static readonly STATUS_INACTIVE = 0;

    @PrimaryGeneratedColumn({ name: 'id', type: 'integer' })
    id!: number;

    @Column({ name: 'code', type: 'varchar', length: 20, nullable: false, unique: true })
    code!: string;

    @Column({ name: 'description', type: 'varchar', length: 100, nullable: false })
    description!: string;

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

