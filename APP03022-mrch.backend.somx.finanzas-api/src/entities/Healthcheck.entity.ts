import { Entity, Column, PrimaryGeneratedColumn } from "typeorm";

@Entity("healthcheck")
export class Healthcheck {
    @PrimaryGeneratedColumn("uuid", { name: "healthcheck_uuid" })
    healthcheckUuid!: string;

    @Column({ name: "service_name", type: "varchar", length: 100 })
    serviceName!: string;

    @Column({ name: "status", type: "varchar", length: 20, default: "OK" })
    status!: string;

    @Column({ name: "message", type: "varchar", length: 255, nullable: true })
    message!: string | null;

    @Column({ name: "created_at", type: "timestamp", default: () => "CURRENT_TIMESTAMP" })
    createdAt!: Date;

    @Column({ name: "updated_at", type: "timestamp", nullable: true })
    updatedAt!: Date | null;
}