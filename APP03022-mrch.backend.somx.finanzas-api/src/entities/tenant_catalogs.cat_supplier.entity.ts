import { Entity, Column, PrimaryGeneratedColumn ,
} from 'typeorm';

@Entity({ name: 'cat_supplier', schema: "tenant_catalogs" })
export class Supplier {

    @PrimaryGeneratedColumn('uuid', { name: 'supplier_uuid' })
    supplierUuid!: string;

    @Column({ name: 'supplier_number', type: 'int',  nullable: false, unique: true })
    supplierNumber?: number;

    @Column({ name: 'rfc', type: 'varchar', nullable: false, unique: true })
    rfc!: string;

    @Column({ name: 'company_name', type: 'varchar', nullable: false, unique: true })
    company_name!: string;

    @Column({ name: 'supplier_type', type: 'int',  nullable: false })
    supplierType?: number;

    @Column({ name: 'created_by', type: 'bigint', nullable: true })
    createdBy?: number;

    @Column({ name: 'created_at', type: 'timestamp', default: () => 'CURRENT_TIMESTAMP' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'bigint', nullable: true })
    updatedBy?: number;

    @Column({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;

    // @OneToMany(() => PurchaseOrder, purchaseOrder => purchaseOrder.supplier, { cascade: true, eager: true })
    // @JoinColumn({ name: 'supplier_number' })
    // purchaseOrders?: PurchaseOrder[];

}