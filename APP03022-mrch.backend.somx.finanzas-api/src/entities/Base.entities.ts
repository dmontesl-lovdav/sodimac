import {Column } from 'typeorm';


export class BaseEntity {

    constructor( createdAt: Date, updatedAt: Date) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

    }

    @Column({ name: 'createdat', type: 'timestamp', nullable: false })
    createdAt: Date;

    @Column({ name: 'updatedat', type: 'timestamp' })
    updatedAt: Date;
}