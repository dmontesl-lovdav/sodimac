import { getDataSource } from "@/config/typeorm-datasource.js";
import { ThreeWayMatch } from "@/entities/ThreeWayMatch.entity.js";

function repo() {
    return getDataSource().getRepository(ThreeWayMatch);
}

type SupplierInfo = {
    nombreProveedor: string | null;
    tipoProveedorId: string | null;
    tipoProveedor: string | null;
};

type ThreeWayMatchWithSupplierInfo = ThreeWayMatch & {
    nombreProveedor?: string | null;
    tipoProveedorId?: string | null;
    tipoProveedor?: string | null;
};

type FindWithFiltersParams = {
    tipoFecha:
    | "fechaRecepcion"
    | "fechaTimbrado"
    | "fechaOrdenCompra"
    | "fechaPago";

    fechaInicio: Date;
    fechaFin: Date;

    numeroProveedor?: string;
    tipoProveedor?: number;
    ordenCompra?: string;
    recepcion?: string;

    allowedVendors?: string[] | null;

    page?: number;
    limit?: number;
};

export function deleteNotPaid(fechaBase: Date) {
    return repo()
        .createQueryBuilder()
        .delete()
        .where("estatus <> 5")
        .andWhere("fechaRecepcion <= :fechaBase", {
            fechaBase,
        })
        .execute();
}

export function insertBase(
    rows: Partial<ThreeWayMatch>[]
) {
    return repo().insert(rows);
}

export function updateById(
    id: string,
    patch: Partial<ThreeWayMatch>
) {
    return repo().update(
        {
            id,
        },
        patch
    );
}

export function findByStatus(status: number) {
    return repo().find({
        where: {
            estatus: status,
        },
    });
}

/**
 * Obtiene el nombre, identificador de tipo y descripción
 * del tipo de proveedor desde shared_catalogs.
 */
async function findSupplierInfo(
    vendorNumbers: string[]
): Promise<Map<string, SupplierInfo>> {
    const supplierInfoMap =
        new Map<string, SupplierInfo>();

    const normalizedVendorNumbers = Array.from(
        new Set(
            vendorNumbers
                .map((value) => String(value).trim())
                .filter((value) => value !== "")
        )
    );

    if (normalizedVendorNumbers.length === 0) {
        return supplierInfoMap;
    }

    const ds = getDataSource();

    const supplierRows = await ds.query(
        `
            SELECT
                supplier.supplier_number::text
                    AS "numeroProveedor",

                supplier.business_name::text
                    AS "nombreProveedor",

                supplier.supplier_type_id::text
                    AS "tipoProveedorId",

                supplier_type.description::text
                    AS "tipoProveedor"

            FROM shared_catalogs.supplier supplier

            LEFT JOIN shared_catalogs.supplier_type supplier_type
                ON supplier_type.id::text =
                   supplier.supplier_type_id::text

            WHERE supplier.supplier_number::text =
                  ANY($1::text[])
        `,
        [normalizedVendorNumbers]
    );

    for (
        const row of supplierRows as Array<{
            numeroProveedor: string;
            nombreProveedor: string | null;
            tipoProveedorId: string | null;
            tipoProveedor: string | null;
        }>
    ) {
        const normalizedVendorNumber =
            String(row.numeroProveedor).trim();

        supplierInfoMap.set(
            normalizedVendorNumber,
            {
                nombreProveedor:
                    row.nombreProveedor?.trim() || null,

                tipoProveedorId:
                    row.tipoProveedorId != null
                        ? String(row.tipoProveedorId).trim()
                        : null,

                tipoProveedor:
                    row.tipoProveedor?.trim() || null,
            }
        );
    }

    return supplierInfoMap;
}

/**
 * Enriquece los registros de Three Way Match con los
 * datos del proveedor.
 */
async function enrichWithSupplierInfo(
    data: ThreeWayMatch[]
): Promise<ThreeWayMatchWithSupplierInfo[]> {
    if (data.length === 0) {
        return [];
    }

    const vendorNumbers = Array.from(
        new Set(
            data
                .map((item) => item.numeroProveedor)
                .filter(
                    (value) =>
                        value !== null &&
                        value !== undefined
                )
                .map((value) => String(value).trim())
                .filter((value) => value !== "")
        )
    );

    if (vendorNumbers.length === 0) {
        return data.map((item) => ({
            ...item,
            nombreProveedor: null,
            tipoProveedorId: null,
            tipoProveedor: null,
        }));
    }

    const supplierInfoMap =
        await findSupplierInfo(vendorNumbers);

    return data.map((item) => {
        const normalizedVendorNumber =
            String(item.numeroProveedor).trim();

        const supplierInfo =
            supplierInfoMap.get(normalizedVendorNumber);

        return {
            ...item,

            nombreProveedor:
                supplierInfo?.nombreProveedor ?? null,

            tipoProveedorId:
                supplierInfo?.tipoProveedorId ?? null,

            tipoProveedor:
                supplierInfo?.tipoProveedor ?? null,
        };
    });
}

/**
 * Consulta paginada para pantalla,
 * CSV y Excel de Three Way Match.
 */
export async function findWithFilters(
    params: FindWithFiltersParams
) {
    const {
        tipoFecha,
        fechaInicio,
        fechaFin,
        numeroProveedor,
        tipoProveedor,
        ordenCompra,
        recepcion,
        allowedVendors = null,
        page = 1,
        limit = 20,
    } = params;

    const qb =
        repo().createQueryBuilder("t");

    // ========================
    // Filtro dinámico por fecha
    // ========================
    const columnMap: Record<
        FindWithFiltersParams["tipoFecha"],
        string
    > = {
        fechaRecepcion: "t.fechaRecepcion",
        fechaTimbrado: "t.fechaTimbrado",
        fechaOrdenCompra: "t.fechaOrdenCompra",
        fechaPago: "t.fechaPago",
    };

    const column = columnMap[tipoFecha];

    if (!column) {
        throw new Error(
            `Invalid tipoFecha: ${tipoFecha}`
        );
    }

    qb.where(
        `${column} BETWEEN :inicio AND :fin`,
        {
            inicio: fechaInicio,
            fin: fechaFin,
        }
    );

    // ========================
    // Seguridad: proveedores
    // permitidos por el BFF
    // ========================
    const normalizedAllowedVendors =
        (allowedVendors ?? [])
            .map((value) =>
                String(value).trim()
            )
            .filter((value) => value !== "");

    if (normalizedAllowedVendors.length > 0) {
        qb.andWhere(
            `"t"."vendor_number" IN (:...allowedVendors)`,
            {
                allowedVendors:
                    normalizedAllowedVendors,
            }
        );
    }

    // ========================
    // Número de proveedor
    // ========================
    if (numeroProveedor?.trim()) {
        qb.andWhere(
            `"t"."vendor_number" = :numeroProveedor`,
            {
                numeroProveedor:
                    numeroProveedor.trim(),
            }
        );
    }

    // ========================
    // Tipo de proveedor
    // ========================
    if (tipoProveedor !== undefined) {
        qb.andWhere(
            `
                EXISTS (
                    SELECT 1
                    FROM shared_catalogs.supplier supplier
                    WHERE
                        CAST(
                            supplier.supplier_number
                            AS TEXT
                        ) =
                        CAST(
                            "t"."vendor_number"
                            AS TEXT
                        )
                    AND
                        CAST(
                            supplier.supplier_type_id
                            AS TEXT
                        ) =
                        CAST(
                            :tipoProveedor
                            AS TEXT
                        )
                )
            `,
            {
                tipoProveedor:
                    String(tipoProveedor).trim(),
            }
        );
    }

    // ========================
    // Orden de compra
    // ========================
    if (ordenCompra?.trim()) {
        qb.andWhere(
            "t.ordenCompra = :ordenCompra",
            {
                ordenCompra:
                    ordenCompra.trim(),
            }
        );
    }

    // ========================
    // Recepción
    // ========================
    if (recepcion?.trim()) {
        qb.andWhere(
            "t.recepcion = :recepcion",
            {
                recepcion:
                    recepcion.trim(),
            }
        );
    }

    // ========================
    // Paginación
    // ========================
    const skip =
        (page - 1) * limit;

    qb.skip(skip)
        .take(limit)
        .orderBy(
            column,
            "DESC"
        );

    const [data, total] =
        await qb.getManyAndCount();

    const enrichedData =
        await enrichWithSupplierInfo(data);

    return {
        data: enrichedData,
        total,
        page,
        limit,
        totalPages:
            Math.ceil(total / limit),
    };
}