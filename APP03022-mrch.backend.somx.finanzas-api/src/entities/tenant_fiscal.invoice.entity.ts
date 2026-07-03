import { Entity, Column, PrimaryGeneratedColumn ,
    OneToMany,
} from 'typeorm';
import { Addendum } from './tenant_fiscal.addendum.entity.js';

@Entity({ name: 'invoice', schema: "tenant_fiscal" })
export class Invoice {

    @PrimaryGeneratedColumn('uuid', { name: 'invoice_uuid' })
    invoiceUuid!: string;

    // Folio fiscal (UUID SAT). Necesario para exponerlo en el nodo addenda del GET /purchase-orders
    // El invoice_uuid es la PK interna; el folio fiscal es el UUID del SAT.
    @Column({ name: 'fiscal_uuid', type: 'uuid', nullable: true })
    fiscalUuid?: string;

    @Column({ name: 'document_type', type: 'varchar', length: 1, nullable: false, unique: true })
    documentType!: string;

    @Column({ name: 'total', type: 'numeric', precision: 16, scale: 2, nullable: false })
    total?: number;

    @Column({ name: 'subtotal', type: 'numeric', precision: 16, scale: 2, nullable: false })
    subtotal?: number;

    @Column({ name: 'folio', type: 'varchar', nullable: false, unique: true })
    folio!: string;

    @Column({ name: 'series', type: 'varchar', nullable: false, unique: true })
    series!: string;

    @Column({ name: 'issue_date', type: 'timestamp', nullable: false })
    issueDate?: Date;

    @Column({ name: 'certification_date', type: 'timestamp', nullable: true })
    certificationDate?: Date;

    // Relaciones
    @OneToMany(() => Addendum, addendum => addendum.invoice,  { cascade: true, eager: false  })
    addendums?: Addendum[];

}