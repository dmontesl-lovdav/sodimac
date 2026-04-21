// src/entities/TwmEjecucion.entity.ts
import { Entity, PrimaryGeneratedColumn, Column } from "typeorm";

@Entity({ schema: "tenant_finance", name: "twm_ejecucion" })
export class TwmEjecucion {
    @PrimaryGeneratedColumn("uuid")
    id!: string;

    @Column({ name: "estado", type: "varchar", length: 20 })
    estado!: string;

    @Column({ name: "fechainicio", type: "timestamp" })
    fechaInicio!: Date;

    @Column({ name: "fechafin", type: "timestamp", nullable: true })
    fechaFin!: Date | null;

    @Column({ name: "intento", type: "int" })
    intento!: number;

    @Column({ name: "fechabase", type: "date" })
    fechaBase!: Date;
}
