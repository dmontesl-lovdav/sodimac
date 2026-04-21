import { getDataSource } from "@/config/typeorm-datasource.js";
import { ThreeWayMatch } from "@/entities/ThreeWayMatch.entity.js";

function repo() {
    return getDataSource().getRepository(ThreeWayMatch);
}

export function deleteNotPaid(fechaBase: Date) {
    return repo()
        .createQueryBuilder()
        .delete()
        .where("estatus <> 5")
        .andWhere("fechaRecepcion <= :fechaBase", { fechaBase })
        .execute();
}

export function insertBase(rows: Partial<ThreeWayMatch>[]) {
    return repo().insert(rows);
}

export function updateById(id: string, patch: Partial<ThreeWayMatch>) {
    return repo().update({ id }, patch);
}

export function findByStatus(status: number) {
    return repo().find({ where: { estatus: status } });
}

/**
 * Nuevo método para pantalla Three Way Match
 */
export async function findWithFilters(params: {
    tipoFecha: "fechaRecepcion" |
    "fechaTimbrado" |
    "fechaOrdenCompra" |
    "fechaPago";
    fechaInicio: Date;
    fechaFin: Date;
    numeroProveedor?: string;
    ordenCompra?: string;
    recepcion?: string;
    page?: number;
    limit?: number;
}) {

    const {
        tipoFecha,
        fechaInicio,
        fechaFin,
        numeroProveedor,
        ordenCompra,
        recepcion,
        page = 1,
        limit = 20
    } = params;

    const qb = repo().createQueryBuilder("t");

    // ========================
    // Filtro dinámico por fecha
    // ========================
    const columnMap: Record<string, string> = {
        fechaRecepcion: "t.fechaRecepcion",
        fechaTimbrado: "t.fechaTimbrado",
        fechaOrdenCompra: "t.fechaOrdenCompra",
        fechaPago: "t.fechaPago",
    };

    const column = columnMap[tipoFecha];

    if (!column) {
        throw new Error(`Invalid tipoFecha: ${tipoFecha}`);
    }

    qb.where(`${column} BETWEEN :inicio AND :fin`, {
        inicio: fechaInicio,
        fin: fechaFin,
    });

    // ========================
    // Filtros opcionales
    // ========================
    if (numeroProveedor) {
        qb.andWhere("t.numeroProveedor = :numeroProveedor", {
            numeroProveedor,
        });
    }

    if (ordenCompra) {
        qb.andWhere("t.ordenCompra = :ordenCompra", {
            ordenCompra,
        });
    }

    if (recepcion) {
        qb.andWhere("t.recepcion = :recepcion", {
            recepcion,
        });
    }

    // ========================
    // Paginación
    // ========================
    const skip = (page - 1) * limit;

    qb.skip(skip)
        .take(limit)
        .orderBy(column, "DESC");

    const [data, total] = await qb.getManyAndCount();

    return {
        data,
        total,
        page,
        limit,
        totalPages: Math.ceil(total / limit),
    };
}
