import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
    ManyToOne,
    JoinColumn,
    OneToMany
} from 'typeorm';
import { OriginCatalog } from './OriginCatalog.entity.js';
import { StatusCatalog } from './StatusCatalog.entity.js';
import { Reception } from './Reception.entity.js';
import { ShippingGuidePurchaseOrder } from './ShippingGuidePurchaseOrder.entity.js';


/**
 * Órdenes de compra del sistema
 */
@Entity('purchase_order')
export class PurchaseOrder {
    @PrimaryGeneratedColumn('uuid', { name: 'purchase_order_uuid' })
    purchaseOrderId!: string;

    @Column({ name: 'order_number', type: 'varchar', length: 50, nullable: false })
    orderNumber!: string;

    @Column({ name: 'vendor_number', type: 'bigint', nullable: false })
    supplierNumber!: number;

    @Column({ name: 'source_id', type: 'numeric', precision: 3, nullable: true })
    originId?: number;

    @Column({ name: 'total_amount', type: 'numeric', precision: 16, scale: 2, nullable: true })
    amount?: number;

    @Column({ name: 'status', type: 'numeric', precision: 2, nullable: true })
    status?: number;

    @Column({ name: 'order_date', type: 'timestamp', nullable: true })
    purchaseOrderDate?: Date;

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
    @OneToMany(() => Reception, reception => reception.purchaseOrder,  { cascade: true, eager: false  })
    receptions?: Reception[];
  
    @OneToMany(() => ShippingGuidePurchaseOrder, shippingGuidePurchaseOrder => shippingGuidePurchaseOrder.purchaseOrder,  { cascade: true, eager: false  })
    shippingGuidePurchaseOrders?: ShippingGuidePurchaseOrder[];


    // @ManyToOne(() => Supplier, supplier => supplier.purchaseOrders)
    // @JoinColumn({ name: 'vendor_number' })
    // supplier?: Supplier;

    // This property will be mapped by leftJoinAndMapOne
    //listSupplier?: Supplier; 

}
