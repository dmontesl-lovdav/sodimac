import { Entity, PrimaryGeneratedColumn, Column, OneToOne, JoinColumn } from "typeorm";
import { ShippingGuideDocument } from './ShippingGuideDocument.entity.js';
/**
 * Se guarda el contenido de los archivos xml y csv de carta porte
 */
@Entity('shipping_guide_file')
export class ShippingGuideFile {
    @PrimaryGeneratedColumn('uuid', { name: 'shipping_guide_file_uuid' })
    shippingGuideFileId!: string;

    // @Column({ name: 'shipping_guide_document_uuid', type: 'uuid' })
    // shippingGuideDocumentId!: string;

    @Column({ name: 'file_name', type: 'varchar' })
    fileName!: string;

    @Column({ name: 'mime_type', type: 'varchar' })
    mimeType!: string;

    @Column({ name: 'file_type', type: 'varchar' })
    fileType!: string;

    @Column({name: 'data', type: "bytea" }) // importante para postgres
    data!: Buffer;

    
  // lado dueño de la relación (tiene la FK)
    @OneToOne(() => ShippingGuideDocument, (shippingGuideDocument) => shippingGuideDocument.shippingGuideFile)
    @JoinColumn({ name: "shipping_guide_document_uuid" }) // 👈 FK en esta tabla
    shippingGuideDocument?: ShippingGuideDocument;

}