import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
    ManyToOne,
    JoinColumn,
    Check
} from 'typeorm';
import { StatusCatalog } from './StatusCatalog.entity.js';

/**
 * Bloqueos de proveedores
 */
@Entity('supplier_block')
@Check('"start_date" < "end_date"')
export class SupplierBlock {
    @PrimaryGeneratedColumn('uuid', { name: 'supplier_block_id' })
    supplierBlockId!: string;

    @Column({ name: 'supplier_number', type: 'bigint', nullable: true })
    supplierNumber?: number;

    @Column({ name: 'start_date', type: 'timestamp', nullable: true })
    startDate?: Date;

    @Column({ name: 'end_date', type: 'timestamp', nullable: true })
    endDate?: Date;

    @Column({ name: 'status', type: 'numeric', precision: 2, nullable: true })
    status?: number;

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
    @ManyToOne(() => StatusCatalog)
    @JoinColumn({ name: 'status' })
    statusCatalog?: StatusCatalog;
}
