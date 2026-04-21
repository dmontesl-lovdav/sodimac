import * as r from "@/repositories/threeWayMatch.repo.js";
import type { ListThreeWayMatchQuery } from "@/schemas/threeWayMatch.schema.js";
import { HttpError } from "@/utils/HttpError.js";
import ExcelJS from "exceljs";

function monthsDiff(a: Date, b: Date) {
    const ms = Math.abs(b.getTime() - a.getTime());
    return ms / (1000 * 60 * 60 * 24 * 30);
}

function validateRange(q: ListThreeWayMatchQuery) {
    const m = monthsDiff(q.fechaInicio, q.fechaFin);

    if (m > 6) {
        throw new HttpError(
            400,
            "Date range cannot exceed 6 months"
        );
    }
}

function buildFilters(q: ListThreeWayMatchQuery, page: number, limit: number) {
    return {
        tipoFecha: q.tipoFecha,
        fechaInicio: q.fechaInicio,
        fechaFin: q.fechaFin,
        ...(q.numeroProveedor && { numeroProveedor: q.numeroProveedor }),
        ...(q.ordenCompra && { ordenCompra: q.ordenCompra }),
        ...(q.recepcion && { recepcion: q.recepcion }),
        page,
        limit,
    };
}

export async function list(q: ListThreeWayMatchQuery) {

    validateRange(q);

    return r.findWithFilters(
        buildFilters(q, q.page ?? 1, q.limit ?? 20)
    );
}

export async function exportCsv(q: ListThreeWayMatchQuery) {

    validateRange(q);

    const result = await r.findWithFilters(
        buildFilters(q, 1, 100000)
    );

    const rows = result.data;

    const headers = [
        "Vendor",
        "Purchase Order",
        "Reception",
        "Reception Date",
        "Invoice UUID",
        "Invoice Amount",
        "SAP Document",
        "Payment Reference",
        "Payment Date",
        "Payment Amount",
        "Status",
    ];

    const csvLines = [
        headers.join(","),
        ...rows.map(row =>
            [
                row.numeroProveedor,
                row.ordenCompra,
                row.recepcion,
                row.fechaRecepcion,
                row.uuid,
                row.montoFactura,
                row.documentoSap,
                row.referenciaPago,
                row.fechaPago,
                row.montoPago,
                row.estatus,
            ]
                .map(v => `"${v ?? ""}"`)
                .join(",")
        ),
    ];

    return csvLines.join("\n");
}

export async function exportXlsx(q: ListThreeWayMatchQuery) {

    validateRange(q);

    const result = await r.findWithFilters(
        buildFilters(q, 1, 100000)
    );

    const workbook = new ExcelJS.Workbook();
    const sheet = workbook.addWorksheet("ThreeWayMatch");

    sheet.columns = [
        { header: "Vendor", key: "numeroProveedor", width: 20 },
        { header: "Purchase Order", key: "ordenCompra", width: 20 },
        { header: "Reception", key: "recepcion", width: 20 },
        { header: "Reception Date", key: "fechaRecepcion", width: 20 },
        { header: "Invoice UUID", key: "uuid", width: 30 },
        { header: "Invoice Amount", key: "montoFactura", width: 20 },
        { header: "SAP Document", key: "documentoSap", width: 20 },
        { header: "Payment Reference", key: "referenciaPago", width: 20 },
        { header: "Payment Date", key: "fechaPago", width: 20 },
        { header: "Payment Amount", key: "montoPago", width: 20 },
        { header: "Status", key: "estatus", width: 15 },
    ];

    result.data.forEach(row => {
        sheet.addRow({
            numeroProveedor: row.numeroProveedor,
            ordenCompra: row.ordenCompra,
            recepcion: row.recepcion,
            fechaRecepcion: row.fechaRecepcion,
            uuid: row.uuid,
            montoFactura: row.montoFactura,
            documentoSap: row.documentoSap,
            referenciaPago: row.referenciaPago,
            fechaPago: row.fechaPago,
            montoPago: row.montoPago,
            estatus: row.estatus,
        });
    });

    const buffer = await workbook.xlsx.writeBuffer();
    return Buffer.from(buffer);
}
