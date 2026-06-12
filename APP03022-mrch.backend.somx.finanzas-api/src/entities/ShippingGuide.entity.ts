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
import { ShippingGuideDocument } from './ShippingGuideDocument.entity.js';
import { ShippingGuidePurchaseOrder } from './ShippingGuidePurchaseOrder.entity.js';

/**
 * Guías de envío
 */
@Entity('shipping_guide')
export class ShippingGuide {
    @PrimaryGeneratedColumn('uuid', { name: 'shipping_guide_id' })
    shippingGuideId!: string;
    
    @Column({ name: 'guide_number', type: 'varchar', length: 50, nullable: false })
    guideNumber?: string;

    @Column({ name: 'vendor_number', type: 'bigint', nullable: false })
    vendorNumber?: number;

    @Column({ name: 'truck_plate', type: 'varchar', length: 30, nullable: false })
    truckPlate?: string;

    @Column({ name: 'trailer_plate', type: 'varchar', length: 30, nullable: true })
    trailerPlate?: string;

    @Column({ name: 'source_id', type: 'numeric', nullable: false })
    originId?: number;

    @Column({ name: 'destination_id', type: 'numeric', nullable: false })
    destinationId?: number;

    @Column({ name: 'delivery_type', type: 'numeric', nullable: false })
    deliveryType?: number;

    @Column({ name: 'status', type: 'numeric', precision: 2, nullable: true })
    status?: number;

    @Column({ name: 'comments', type: 'varchar', length: 100, nullable: true })
    comments?: string;

    @Column({ name: 'delivery_date', type: 'timestamp', nullable: false })
    deliveryDate?: Date;

    @Column({ name: 'estimated_arrival', type: 'timestamp', nullable: true })
    shippingDate?: Date;

    // Auditoría
    @Column({ name: 'created_by', type: 'bigint', nullable: true })
    createdBy?: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp', default: () => 'CURRENT_TIMESTAMP' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'bigint', nullable: true })
    updatedBy?: number;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;

    @Column({ name: 'is_status_updated', type: 'boolean', nullable: true })
    isStatusUpdated?: boolean;

    // Relaciones
    @ManyToOne(() => OriginCatalog)
    @JoinColumn({ name: 'source_id' })
    origin?: OriginCatalog;

    @ManyToOne(() => StatusCatalog)
    @JoinColumn({ name: 'status' })
    statusCatalog?: StatusCatalog;

    // Relaciones
    @OneToMany(() => ShippingGuideDocument, shippingGuideDocument => shippingGuideDocument.shippingGuide,  { cascade: true, eager: true })
    shippingGuideDocuments?: ShippingGuideDocument[];

    @OneToMany(() => ShippingGuidePurchaseOrder, shippingGuidePurchaseOrder => shippingGuidePurchaseOrder.shippingGuide, { cascade: true, eager: true })
    shippingGuidePurchaseOrders?: ShippingGuidePurchaseOrder[];
}
