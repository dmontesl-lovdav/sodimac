import { UpdateDateColumn, CreateDateColumn } from 'typeorm';


export class BaseEntity {

    constructor( createdAt: Date, updatedAt: Date) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

    }

    @CreateDateColumn({ name: 'created_at', type: 'timestamp', default: () => 'CURRENT_TIMESTAMP' })
    createdAt: Date;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt: Date;
}