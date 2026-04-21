import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
    JoinColumn,
    OneToOne
} from 'typeorm';
import { OriginCatalog } from './OriginCatalog.entity.js';
import { StatusCatalog } from './StatusCatalog.entity.js';
import { Reception } from './Reception.entity.js';
import { ShippingGuidePurchaseOrder } from './ShippingGuidePurchaseOrder.entity.js';


/**
 * Órdenes de compra del sistema
 */
@Entity('addendum_manual')
export class AddendumManual {
    @PrimaryGeneratedColumn('uuid', { name: 'addendum_manual_uuid' })
    addendumManualId!: string;

    @Column({ name: 'invoice_uuid', type: 'uuid', nullable: false })
    invoiceId!: string;

    @Column({ name: 'supplier_number', type: 'bigint', nullable: false })
    supplierNumber!: number;

    @Column({ name: 'reception_id', type: 'uuid', nullable: false })
    receptionId?: string;

    @Column({ name: 'purchase_order_number', type: 'varchar', length: 50, nullable: false })
    orderNumber?: string;

    @Column({ name: 'supplier_type_id', type: 'numeric',  nullable: false })
    supplierTypeId?: number;

    @Column({ name: 'user_id', type: 'bigint', nullable: true })
    userId?: number;

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
    @OneToOne(() => Reception)
    @JoinColumn({ name: 'reception_id' })
    reception?: Reception;

}
