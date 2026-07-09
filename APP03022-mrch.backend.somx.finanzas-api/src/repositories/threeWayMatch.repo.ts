import { getDataSource } from "@/config/typeorm-datasource.js";
import { ThreeWayMatch } from "@/entities/ThreeWayMatch.entity.js";

function repo() {
    return getDataSource().getRepository(ThreeWayMatch);
}

type ThreeWayMatchWithSupplierName = ThreeWayMatch & {
    nombreProveedor?: string | null;
};

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

async function findSupplierNames(
    vendorNumbers: string[]
): Promise<Map<string, string>> {
    const supplierNameMap = new Map<string, string>();

    const normalizedVendorNumbers = Array.from(
        new Set(
            vendorNumbers
                .map((value) => String(value).trim())
                .filter((value) => value !== "")
        )
    );

    if (normalizedVendorNumbers.length === 0) {
        return supplierNameMap;
    }

    const ds = getDataSource();

    const supplierRows = await ds.query(
        `
            SELECT
                supplier_number::text AS "numeroProveedor",
                business_name::text AS "nombreProveedor"
            FROM shared_catalogs.supplier
            WHERE supplier_number::text = ANY($1::text[])
              AND business_name IS NOT NULL
              AND TRIM(business_name::text) <> ''
        `,
        [normalizedVendorNumbers]
    );

    for (const row of supplierRows as Array<{
        numeroProveedor: string;
        nombreProveedor: string;
    }>) {
        supplierNameMap.set(
            String(row.numeroProveedor).trim(),
            row.nombreProveedor
        );
    }

    return supplierNameMap;
}

async function enrichWithSupplierNames(
    data: ThreeWayMatch[]
): Promise<ThreeWayMatchWithSupplierName[]> {
    if (data.length === 0) {
        return [];
    }

    const vendorNumbers = Array.from(
        new Set(
            data
                .map((item) => item.numeroProveedor)
                .filter((value) => value !== null && value !== undefined)
                .map((value) => String(value).trim())
                .filter((value) => value !== "")
        )
    );

    if (vendorNumbers.length === 0) {
        return data.map((item) => ({
            ...item,
            nombreProveedor: null,
        }));
    }

    const supplierNameMap = await findSupplierNames(vendorNumbers);

    return data.map((item) => ({
        ...item,
        nombreProveedor:
            supplierNameMap.get(String(item.numeroProveedor).trim()) ?? null,
    }));
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
    allowedVendors?: string[] | null;
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
        allowedVendors = null,
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
    // Filtro de seguridad — vendors permitidos del BFF (STM-321)
    // ========================
    if (allowedVendors !== null && allowedVendors.length > 0) {
        qb.andWhere("CAST(t.numeroProveedor AS TEXT) IN (:...allowedVendors)", {
            allowedVendors,
        });
    }

    // ========================
    // Filtros opcionales
    // ========================
    if (numeroProveedor) {
        qb.andWhere("CAST(t.numeroProveedor AS TEXT) = :numeroProveedor", {
            numeroProveedor: String(numeroProveedor),
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

    const enrichedData = await enrichWithSupplierNames(data);

    return {
        data: enrichedData,
        total,
        page,
        limit,
        totalPages: Math.ceil(total / limit),
    };
}