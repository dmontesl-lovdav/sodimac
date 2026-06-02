import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
} from 'typeorm';

@Entity({ schema: 'shared_catalogs', name: 'supplier_type' })
export class SharedSupplierType {
    @PrimaryGeneratedColumn({ name: 'id', type: 'int' })
    id!: number;

    @Column({ name: 'code', type: 'varchar', length: 255, nullable: false })
    code!: string;

    @Column({ name: 'description', type: 'varchar', length: 255, nullable: true })
    description?: string;
}