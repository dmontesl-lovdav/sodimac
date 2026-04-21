import { getDataSource } from "@/config/typeorm-datasource.js";
import { TwmEjecucion } from "@/entities/TwmEjecucion.entity.js";

function repo() {
    return getDataSource().getRepository(TwmEjecucion);
}

export async function createRun(fechaBase: Date, intento: number) {
    const row = repo().create({
        estado: "RUNNING",
        fechaInicio: new Date(),
        intento,
        fechaBase,
    });

    return repo().save(row);
}

export async function closeRun(
    id: string,
    estado: "COMPLETA" | "ERROR"
) {
    return repo().update(
        { id },
        {
            estado,
            fechaFin: new Date(),
        }
    );
}
