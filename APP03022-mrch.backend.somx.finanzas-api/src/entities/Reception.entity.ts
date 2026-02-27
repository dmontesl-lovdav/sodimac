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
import { OriginCatalog } from './OriginCatalog.entity.js';
import { StatusCatalog } from './StatusCatalog.entity.js';
import { ReceptionSku } from './ReceptionSku.entity.js';
import { Addendum } from './tenant_fiscal.addendum.entity.js';

/**
 * Recepciones de mercancía
 */
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
    purchaseOrder?: PurchaseOrder;

    @OneToMany(() => ReceptionSku, receptionSku => receptionSku.reception, { cascade: true, eager: true })
    receptionSkus?: ReceptionSku[];

    // @OneToMany(() => Addendum, (addendum) => addendum.reception, { cascade: true, eager: true })
    // @JoinColumn({ name: 'reception_number' })
    // addendums?: Addendum[];

    @OneToOne(() => Addendum, addendum => addendum.reception, { cascade: true, eager: true })
    @JoinColumn({ name: 'reception_number' })
    addendums?: Addendum;

    // This property will be mapped by leftJoinAndMapOne
    listAddendum?: Addendum; 
}
