import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
    ManyToOne,
    OneToOne,
    OneToMany,
    JoinColumn
} from 'typeorm';

import { PurchaseOrder } from './PurchaseOrder.entity.js';
import { AddendumManual } from './AddendumManual.entity.js';
import { ReceptionSku } from './ReceptionSku.entity.js';
import { Addendum } from './tenant_fiscal.addendum.entity.js';

@Entity('reception')
export class Reception {
    @PrimaryGeneratedColumn('uuid', { name: 'reception_id' })
    receptionId!: string;

    @Column({ name: 'reception_number', type: 'varchar', nullable: false })
    receptionNumber!: string;

    @Column({ name: 'purchase_order_uuid', type: 'uuid', nullable: true })
    purchaseOrderId?: string;

    @Column({ name: 'origin_id', type: 'numeric', nullable: true })
    originId?: number;

    @Column({ name: 'destination_id', type: 'numeric', nullable: true })
    destinationId?: number;

    @Column({ name: 'amount', type: 'numeric', precision: 16, scale: 2, nullable: true })
    amount?: number;

    @Column({ name: 'status', type: 'numeric', precision: 2, nullable: true })
    status?: number;

    @Column({ name: 'comment', type: 'varchar', length: 256, nullable: true })
    comment?: string;

    @Column({ name: 'reception_date', type: 'date', nullable: true })
    receptionDate?: Date;

    @Column({ name: 'guide_number', type: 'varchar', length: 50, nullable: true })
    guideNumber?: string;

    // Auditoría
    @Column({ name: 'created_by', type: 'bigint', nullable: true })
    createdBy?: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp', default: () => 'CURRENT_TIMESTAMP' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'bigint', nullable: true })
    updatedBy?: number;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;

    // Relaciones
    @ManyToOne(() => PurchaseOrder)
    @JoinColumn({ name: 'purchase_order_uuid' })
    purchaseOrder?: any;

    @OneToMany(
        () => ReceptionSku,
        (receptionSku) => receptionSku.reception,
        { cascade: true, eager: true }
    )
    receptionSkus?: any[];

    @OneToOne(() => AddendumManual, addendumManual => addendumManual.reception, { cascade: true, eager: true })
    addendumManual?: AddendumManual;

    @OneToOne(() => Addendum, (addendum) => addendum.reception, { cascade: true, eager: true })

    @JoinColumn({ name: 'reception_id' })
    addendums?: any;

    listAddendum?: any;
}
