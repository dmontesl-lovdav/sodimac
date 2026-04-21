import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
    ManyToOne,
    JoinColumn
} from 'typeorm';

import { ShippingGuide } from './ShippingGuide.entity.js';
import { PurchaseOrder } from './PurchaseOrder.entity.js';

@Entity('shipping_guide_purchase_order')
export class ShippingGuidePurchaseOrder {
    @PrimaryGeneratedColumn('uuid', { name: 'shipping_guide_purchase_order_id' })
    shippingGuidePurchaseOrderId!: string;

    @Column({ name: 'shipping_guide_id', type: 'uuid', nullable: false })
    shippingGuideId!: string;

    @Column({ name: 'purchase_order_uuid', type: 'uuid', nullable: false })
    purchaseOrderId!: string;

    @Column({ name: 'created_by', type: 'bigint', nullable: true })
    createdBy?: number;

    @CreateDateColumn({
        name: 'created_at',
        type: 'timestamp',
        default: () => 'CURRENT_TIMESTAMP',
    })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'bigint', nullable: true })
    updatedBy?: number;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;

    @ManyToOne(() => ShippingGuide)
    @JoinColumn({ name: 'shipping_guide_id' })
    shippingGuide?: any;

    @ManyToOne(() => PurchaseOrder, { eager: true })
    @JoinColumn({ name: 'purchase_order_uuid' })
    purchaseOrder?: any;
}
