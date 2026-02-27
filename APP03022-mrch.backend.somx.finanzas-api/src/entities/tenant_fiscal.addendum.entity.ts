import { Entity, Column, PrimaryGeneratedColumn ,
        CreateDateColumn,
    UpdateDateColumn,
    OneToOne,
    ManyToOne,
    JoinColumn
} from 'typeorm';
import { Reception } from './Reception.entity.js';
import { Invoice } from './tenant_fiscal.invoice.entity.js';

@Entity({ name: 'addendum', schema: "tenant_fiscal" })
export class Addendum {
    @PrimaryGeneratedColumn('uuid', { name: 'addendum_uuid' })
    addendumUuid!: string;

    @Column({ name: 'invoice_uuid', type: 'varchar', nullable: false, unique: true })
    invoiceUuid!: string;

    @Column({ name: 'supplier_number', type: 'int' })
    supplierNumber!: number;

    @Column({ name: 'reception_number', type: 'varchar', nullable: false, unique: true })
    receptionNumber!: string;

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
    @OneToOne(() => Reception, reception => reception.addendums)
    @JoinColumn({ name: 'reception_number' })
    reception?: Reception;

    @ManyToOne(() => Invoice)
    @JoinColumn({ name: 'invoice_uuid' })
    invoice?: Invoice;
}