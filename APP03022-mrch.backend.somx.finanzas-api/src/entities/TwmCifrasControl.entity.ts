// src/entities/TwmCifrasControl.entity.ts
import { Entity, PrimaryGeneratedColumn, Column } from "typeorm";

@Entity({ schema: "tenant_finance", name: "twm_cifras_control" })
export class TwmCifrasControl {
    @PrimaryGeneratedColumn("uuid")
    id!: string;

    @Column({ name: "id_ejecucion", type: "uuid" })
    idEjecucion!: string;

    @Column({ name: "paso", type: "varchar", length: 10 })
    paso!: string; // P1 | P2 | P3 | P4 | FINAL

    @Column({ name: "total_registros", type: "int" })
    totalRegistros!: number;

    @Column({ name: "total_monto", type: "numeric", precision: 18, scale: 2, nullable: true })
    totalMonto!: number | null;

    @Column({ name: "detalle_json", type: "jsonb", nullable: true })
    detalleJson!: any;

    @Column({ name: "fecha_registro", type: "timestamp", default: () => "CURRENT_TIMESTAMP" })
    fechaRegistro!: Date;
}
