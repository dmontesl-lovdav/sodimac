import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    Index,
    Unique,
    BeforeInsert,
    BeforeUpdate
} from 'typeorm';

@Entity({ name: 'status_train', schema: 'shared_catalogs' })
@Unique('uk_status_train', ['optionId', 'sourceStatus', 'targetStatus'])
@Index('idx_status_train_option', ['optionId'])
@Index('idx_status_train_source', ['optionId', 'sourceStatus'])
export class StatusTrain {
    public static readonly OPTION_INVOICE = 1;
    public static readonly OPTION_CREDIT_NOTE = 2;
    public static readonly OPTION_PAYMENT_COMPLEMENT = 3;
    public static readonly OPTION_CARTA_PORTE = 4;

    @PrimaryGeneratedColumn({ name: 'id', type: 'integer' })
    id!: number;

    @Column({ name: 'option_id', type: 'integer', nullable: false })
    optionId!: number;

    @Column({ name: 'source_status', type: 'integer', nullable: false })
    sourceStatus!: number;

    @Column({ name: 'target_status', type: 'integer', nullable: false })
    targetStatus!: number;

    @Column({ name: 'created_by', type: 'bigint', nullable: false, transformer: {
        to: (v: number | null | undefined) => v,
        from: (v: string | number | null | undefined) => v == null ? v : Number(v)
    } })
    createdBy!: number;

    @Column({ name: 'created_at', type: 'timestamp', nullable: false, update: false })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'bigint', nullable: true, transformer: {
        to: (v: number | null | undefined) => v,
        from: (v: string | number | null | undefined) => v == null ? v : Number(v)
    } })
    updatedBy?: number | null;

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

