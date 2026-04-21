import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
    ManyToOne,
    JoinColumn
} from 'typeorm';

import { Reception } from './Reception.entity.js';

@Entity('reception_sku')
export class ReceptionSku {
    @PrimaryGeneratedColumn('uuid', { name: 'reception_sku_id' })
    receptionSkuId!: string;

    @Column({ name: 'reception_id', type: 'uuid', nullable: true })
    receptionId!: string;

    @Column({ name: 'sku', type: 'varchar', length: 15, nullable: false })
    sku!: string;

    @Column({ name: 'description', type: 'varchar', length: 256, nullable: false })
    description!: string;

    @Column({ name: 'quantity', type: 'numeric', precision: 20, scale: 6, nullable: true })
    quantity?: number;

    @Column({ name: 'unit_cost', type: 'numeric', precision: 16, scale: 2, nullable: true })
    unitCost?: number;

    @Column({ name: 'total_cost', type: 'numeric', precision: 26, scale: 2, nullable: true })
    totalCost?: number;

    @Column({ name: 'status', type: 'numeric', precision: 2, nullable: true })
    status?: number;

    @Column({ name: 'created_by', type: 'bigint', nullable: true })
    createdBy?: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp', default: () => 'CURRENT_TIMESTAMP' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'bigint', nullable: true })
    updatedBy?: number;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;

    @ManyToOne(() => Reception)
    @JoinColumn({ name: 'reception_id' })
    reception?: any;
}
