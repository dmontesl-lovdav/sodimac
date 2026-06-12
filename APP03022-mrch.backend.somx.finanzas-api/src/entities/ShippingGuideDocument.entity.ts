import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
    ManyToOne,
    JoinColumn, OneToOne
} from 'typeorm';

import { ShippingGuide } from './ShippingGuide.entity.js';
import { StatusCatalog } from './StatusCatalog.entity.js';
import { ShippingGuideFile } from './ShippingGuideFile.entity.js';

@Entity('shipping_guide_document')
export class ShippingGuideDocument {
    @PrimaryGeneratedColumn('uuid', { name: 'shipping_guide_document_id' })
    shippingGuideDocumentId!: string;

    @Column({ name: 'shipping_guide_id', type: 'uuid', nullable: true })
    shippingGuideId?: string;

    @Column({ name: 'file_name', type: 'varchar', length: 100, nullable: false })
    fileName?: string;

    @Column({ name: 'file_type', type: 'smallint', nullable: false })
    fileType?: number;

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

    @ManyToOne(() => ShippingGuide)
    @JoinColumn({ name: 'shipping_guide_id' })
    shippingGuide?: any;

    @ManyToOne(() => StatusCatalog)
    @JoinColumn({ name: 'status' })
    statusCatalog?: any;

    
    // relación inversa (no tiene la FK)
    @OneToOne(() => ShippingGuideFile, (shippingGuideFile) => shippingGuideFile.shippingGuideDocument ,  { cascade: true })
    shippingGuideFile?: ShippingGuideFile;

}
