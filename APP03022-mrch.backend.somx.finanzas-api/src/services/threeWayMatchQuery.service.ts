import * as r from "@/repositories/threeWayMatch.repo.js";
import type { ListThreeWayMatchQuery } from "@/schemas/threeWayMatch.schema.js";
import { HttpError } from "@/utils/HttpError.js";
import ExcelJS from "exceljs";

function monthsDiff(a: Date, b: Date): number {
    const ms = Math.abs(b.getTime() - a.getTime());

    return ms / (1000 * 60 * 60 * 24 * 30);
}

function validateRange(
    q: ListThreeWayMatchQuery
): void {
    const months = monthsDiff(
        q.fechaInicio,
        q.fechaFin
    );

    if (months > 6) {
        throw new HttpError(
            400,
            "Date range cannot exceed 6 months"
        );
    }
}

function buildFilters(
    q: ListThreeWayMatchQuery,
    page: number,
    limit: number,
    allowedVendors: string[] | null
) {
    return {
        tipoFecha: q.tipoFecha,
        fechaInicio: q.fechaInicio,
        fechaFin: q.fechaFin,

        ...(q.numeroProveedor && {
            numeroProveedor: q.numeroProveedor,
        }),

        ...(q.tipoProveedor !== undefined && {
            tipoProveedor: q.tipoProveedor,
        }),

        ...(q.ordenCompra && {
            ordenCompra: q.ordenCompra,
        }),

        ...(q.recepcion && {
            recepcion: q.recepcion,
        }),

        allowedVendors,
        page,
        limit,
    };
}

const exportColumns = [
    {
        header: "Orden Compra",
        key: "ordenCompra",
        width: 20,
    },
    {
        header: "Recepción",
        key: "recepcion",
        width: 20,
    },
    {
        header: "Monto Recepción",
        key: "montoRecepcion",
        width: 20,
    },
    {
        header: "Fecha Recepción",
        key: "fechaRecepcion",
        width: 20,
    },
    {
        header: "Serie",
        key: "serie",
        width: 15,
    },
    {
        header: "Folio",
        key: "folio",
        width: 20,
    },
    {
        header: "UUID",
        key: "uuid",
        width: 40,
    },
    {
        header: "Monto Factura",
        key: "montoFactura",
        width: 20,
    },
    {
        header: "Fecha Recepción Factura",
        key: "fechaTimbrado",
        width: 25,
    },
    {
        header: "Documento SAP",
        key: "documentoSap",
        width: 20,
    },
    {
        header: "Monto Contable",
        key: "montoContable",
        width: 20,
    },
    {
        header: "Fecha Contable",
        key: "fechaContable",
        width: 20,
    },
    {
        header: "Documento Pago",
        key: "referenciaPago",
        width: 20,
    },
    {
        header: "Monto Pago",
        key: "montoPago",
        width: 20,
    },
    {
        header: "Tipo Proveedor",
        key: "tipoProveedor",
        width: 30,
    },
    {
        header: "Número Proveedor",
        key: "numeroProveedor",
        width: 20,
    },
    {
        header: "Nombre Proveedor",
        key: "nombreProveedor",
        width: 35,
    },
    {
        header: "Fecha Pago",
        key: "fechaPago",
        width: 20,
    },
] as const;

type ExportColumnKey =
    typeof exportColumns[number]["key"];

type ExportFallbackKey =
    | "numeroDocumento"
    | "tipoProveedorId"
    | "nombreProveedorSap"
    | "supplierName"
    | "vendorName"
    | "proveedorNombre";

type ExportRow = Partial<
    Record<
        ExportColumnKey | ExportFallbackKey,
        unknown
    >
>;

function toExportRow(
    row: unknown
): ExportRow {
    return row as ExportRow;
}

function getExportValue(
    row: ExportRow,
    key: ExportColumnKey
): unknown {
    /*
     * La pantalla utiliza numeroDocumento como primera
     * opción y referenciaPago como respaldo.
     */
    if (key === "referenciaPago") {
        return (
            row.numeroDocumento ??
            row.referenciaPago ??
            ""
        );
    }

    /*
     * Utiliza la descripción del tipo de proveedor.
     * Si no está disponible, utiliza el ID como respaldo.
     */
    if (key === "tipoProveedor") {
        return (
            row.tipoProveedor ??
            row.tipoProveedorId ??
            ""
        );
    }

    /*
     * Conserva los alias anteriores por compatibilidad.
     */
    if (key === "nombreProveedor") {
        return (
            row.nombreProveedor ??
            row.nombreProveedorSap ??
            row.supplierName ??
            row.vendorName ??
            row.proveedorNombre ??
            ""
        );
    }

    return row[key] ?? "";
}

function escapeCsvValue(
    value: unknown
): string {
    const text =
        value === null ||
            value === undefined
            ? ""
            : String(value);

    return `"${text.replace(/"/g, '""')}"`;
}

export async function list(
    q: ListThreeWayMatchQuery,
    allowedVendors: string[] | null = null
) {
    validateRange(q);

    return r.findWithFilters(
        buildFilters(
            q,
            q.page ?? 1,
            q.limit ?? 20,
            allowedVendors
        )
    );
}

export async function exportCsv(
    q: ListThreeWayMatchQuery,
    allowedVendors: string[] | null = null
): Promise<string> {
    validateRange(q);

    const result = await r.findWithFilters(
        buildFilters(
            q,
            1,
            100000,
            allowedVendors
        )
    );

    const rows =
        result.data.map(toExportRow);

    const headers =
        exportColumns.map((column) =>
            escapeCsvValue(column.header)
        );

    const csvLines = [
        headers.join(","),

        ...rows.map((row) =>
            exportColumns
                .map((column) =>
                    escapeCsvValue(
                        getExportValue(
                            row,
                            column.key
                        )
                    )
                )
                .join(",")
        ),
    ];

    /*
     * El BOM UTF-8 se agrega en requestBinary
     * del frontend para evitar duplicarlo.
     */
    return csvLines.join("\n");
}

export async function exportXlsx(
    q: ListThreeWayMatchQuery,
    allowedVendors: string[] | null = null
): Promise<Buffer> {
    validateRange(q);

    const result = await r.findWithFilters(
        buildFilters(
            q,
            1,
            100000,
            allowedVendors
        )
    );

    const workbook =
        new ExcelJS.Workbook();

    const sheet =
        workbook.addWorksheet(
            "ThreeWayMatch"
        );

    sheet.columns =
        exportColumns.map((column) => ({
            header: column.header,
            key: column.key,
            width: column.width,
        }));

    result.data
        .map(toExportRow)
        .forEach((row) => {
            sheet.addRow(
                Object.fromEntries(
                    exportColumns.map(
                        (column) => [
                            column.key,
                            getExportValue(
                                row,
                                column.key
                            ),
                        ]
                    )
                )
            );
        });

    const buffer =
        await workbook.xlsx.writeBuffer();

    return Buffer.from(buffer);
}