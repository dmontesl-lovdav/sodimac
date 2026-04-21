// src/entities/TwmLogs.entity.ts
import { Entity, PrimaryGeneratedColumn, Column } from "typeorm";

@Entity({ schema: "tenant_finance", name: "twm_logs" })
export class TwmLogs {
    @PrimaryGeneratedColumn("uuid")
    id!: string;

    @Column({ name: "id_ejecucion", type: "uuid" })
    idEjecucion!: string;

    @Column({ name: "severidad", type: "varchar", length: 20 })
    severidad!: string; // INFO | WARN | ERROR | CRITICAL

    @Column({ name: "codigo_mensaje", type: "varchar", length: 100 })
    codigoMensaje!: string;

    @Column({ name: "mensaje_params", type: "jsonb", nullable: true })
    mensajeParams!: any;

    @Column({ name: "stack_trace", type: "text", nullable: true })
    stackTrace!: string | null;

    @Column({ name: "fecha_hora", type: "timestamp", default: () => "CURRENT_TIMESTAMP" })
    fechaHora!: Date;
}
