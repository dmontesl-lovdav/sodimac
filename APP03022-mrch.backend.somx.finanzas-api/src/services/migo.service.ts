import { randomUUID } from "node:crypto";
import * as migoRepo from "@/repositories/migo.repo.js";
import { ResponseHandler } from '@/response/ResponseHandler.js';
import { StatusCodes } from 'http-status-codes';
import { ResponsePageableDTO } from '@/response/ResponseHandler.dto.js';
import { MigoStatus } from "@/entities/MigoDocument.entity.js";
import { validateLayout, type ParsedRow } from "@/services/migoLayoutValidation.service.js";
import { logger } from "@/utils/logger.js";
import { datasource } from "@/config/typeorm-datasource.js";
import { PurchaseOrder } from "@/entities/PurchaseOrder.entity.js";
import { MigoDocumentReception } from "@/entities/MigoDocumentReception.entity.js";
import type {
    ListMigoDocumentsQueryDto,
    ListMigoReceptionsQueryDto,
    RejectMigoDto,
} from "@/schemas/migo.schema.js";

function generateFolio(): string {
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const d = String(now.getDate()).padStart(2, '0');
    const h = String(now.getHours()).padStart(2, '0');
    const min = String(now.getMinutes()).padStart(2, '0');
    const s = String(now.getSeconds()).padStart(2, '0');
    const ms = String(now.getMilliseconds()).padStart(3, '0');
    return `MIGO-${y}${m}${d}-${h}${min}${s}${ms}`;
}

function toNum(val: string): number {
    return Number(val.replace(',', '.'));
}

export async function listDocuments(q: ListMigoDocumentsQueryDto) {
    if (q.publishedAtEnd) {
        const end = new Date(q.publishedAtEnd);
        end.setDate(end.getDate() + 1);
        q.publishedAtEnd = end;
    }

    const { result, total } = await migoRepo.findDocumentsPaginated(q);
    const totalPages = Math.ceil(total / q.pageSize);

    const page: ResponsePageableDTO = {
        content: result,
        totalElements: total,
        numberOfElements: result.length,
        totalPages,
        pageNumber: q.pageNumber,
        pageSize: q.pageSize,
    };

    return ResponseHandler.responseBuilder("", page, 0, StatusCodes.OK, true, "");
}

export async function getDocumentById(id: string) {
    const doc = await migoRepo.findDocumentById(id);
    if (!doc) {
        return ResponseHandler.responseBuilder("Documento MIGO no encontrado", null, -1, StatusCodes.NOT_FOUND, false, "");
    }
    return ResponseHandler.responseBuilder("", doc, 0, StatusCodes.OK, true, "");
}

export async function listReceptions(q: ListMigoReceptionsQueryDto) {
    const { result, total } = await migoRepo.findReceptionsByDocumentPaginated(q);
    const totalPages = Math.ceil(total / q.pageSize);

    const page: ResponsePageableDTO = {
        content: result,
        totalElements: total,
        numberOfElements: result.length,
        totalPages,
        pageNumber: q.pageNumber,
        pageSize: q.pageSize,
    };

    return ResponseHandler.responseBuilder("", page, 0, StatusCodes.OK, true, "");
}

export async function uploadCsv(fileContent: string, fileName: string, createdBy?: number) {
    const validation = validateLayout(fileContent);

    if (validation.globalError) {
        return ResponseHandler.responseBuilder(
            validation.globalError.message,
            { code: validation.globalError.code, totalRows: validation.totalRows },
            -1,
            StatusCodes.BAD_REQUEST,
            false,
            validation.globalError.message
        );
    }

    const validRows = validation.parsedRows.filter(r => r.isValid);
    const invalidRows = validation.parsedRows.filter(r => !r.isValid);

    const receptions: Array<Record<string, any>> = validRows.map((row: ParsedRow) => {
        const rec: Record<string, any> = {
            nroOc: toNum(row.Nro_OC),
            nroRecepcion: toNum(row.Nro_Recepcion),
            sucursal: toNum(row.Sucursal),
            fechaRecepcion: new Date(row.Fecha_Recepcion),
            importeSinImpuesto: toNum(row.Importe_sin_impuesto),
            cantidad: toNum(row.Cantidad),
            importeUnitario: toNum(row.Importe_Unitario),
            importeSinImpuestoDet: toNum(row.Importe_SinImpuesto),
            isValid: true,
            rowNumber: row.rowNumber,
        };
        if (row.Nro_Guia) rec.nroGuia = row.Nro_Guia;
        if (row.Origen) rec.origen = row.Origen;
        if (row.SKU) rec.sku = row.SKU;
        if (row.Descripcion_Sku) rec.descripcionSku = row.Descripcion_Sku;
        if (row.MontoOC) rec.montoOc = toNum(row.MontoOC);
        return rec;
    });

    const distinctOcs = new Set(validRows.map(r => r.Nro_OC));
    const distinctReceptions = new Set(validRows.map(r => r.Nro_Recepcion));
    const ocMontoMap = new Map<string, number>();
    for (const r of validRows) {
        if (!ocMontoMap.has(r.Nro_OC)) {
            ocMontoMap.set(r.Nro_OC, r.MontoOC ? toNum(r.MontoOC) : 0);
        }
    }
    const totalMontoOc = Array.from(ocMontoMap.values()).reduce((s, v) => s + v, 0);

    const folio = generateFolio();
    const doc: Record<string, any> = {
        folio,
        fileName,
        totalRecords: validation.totalRows,
        numeroOc: distinctOcs.size,
        numeroRecepcion: distinctReceptions.size,
        montoOc: Math.round(totalMontoOc * 100) / 100,
        numeroRechazoOc: 0,
        status: validRows.length > 0 ? MigoStatus.PUBLICADO : MigoStatus.RECHAZADO,
        publishedAt: new Date(),
        receptions,
    };
    if (createdBy != null) doc.createdBy = createdBy;

    const saved = await migoRepo.saveDocument(doc);

    const invalidDetails = invalidRows.map(r => ({
        row: r.rowNumber,
        errors: r.errors,
    }));

    const summaryMsg = `La validación del layout ha finalizado correctamente.\nTotal de registros: ${validation.totalRows}\nTotal válidos: ${validation.totalValid}\nTotal incorrectos: ${validation.totalInvalid}`;

    logger.info(`[MIGO] Document created folio=${folio}, total=${validation.totalRows}, valid=${validation.totalValid}, invalid=${validation.totalInvalid}`);

    return ResponseHandler.responseBuilder(
        summaryMsg,
        {
            document: saved,
            summary: {
                totalRows: validation.totalRows,
                totalValid: validation.totalValid,
                totalInvalid: validation.totalInvalid,
            },
            invalidRows: invalidDetails,
        },
        0,
        StatusCodes.CREATED,
        true,
        ""
    );
}

interface PromotionStats {
    receptionsCreated: number;
    receptionsSkipped: number;
    skusCreated: number;
}

interface DriverError {
    code?: string;
    detail?: string;
    message?: string;
    column?: string;
    table?: string;
    constraint?: string;
}

function extractDriverDetails(err: unknown): DriverError {
    const e = err as { driverError?: DriverError; code?: string; detail?: string; message?: string };
    const drv = e?.driverError ?? {};

    const out: DriverError = {};
    const code = drv.code ?? e?.code;
    if (code !== undefined) out.code = code;
    const detail = drv.detail ?? e?.detail;
    if (detail !== undefined) out.detail = detail;
    const msg = drv.message ?? e?.message ?? (err instanceof Error ? err.message : String(err));
    if (msg !== undefined) out.message = msg;
    if (drv.column !== undefined) out.column = drv.column;
    if (drv.table !== undefined) out.table = drv.table;
    if (drv.constraint !== undefined) out.constraint = drv.constraint;
    return out;
}

async function rawInsertReception(
    manager: import('typeorm').EntityManager,
    data: {
        receptionNumber: string;
        amount: number;
        status: number;
        comment: string;
        receptionDate: Date;
        guideNumber?: string;
        originId?: number;
        purchaseOrderId?: string;
        createdBy?: number;
    },
): Promise<string> {
    const receptionId = randomUUID();
    const now = new Date();

    const columns: string[] = [
        'reception_id',
        'reception_number',
        'amount',
        'status',
        'comment',
        'reception_date',
        'created_at',
    ];
    const values: any[] = [
        receptionId,
        data.receptionNumber,
        data.amount,
        data.status,
        data.comment,
        data.receptionDate,
        now,
    ];
    if (data.guideNumber !== undefined) {
        columns.push('guide_number');
        values.push(data.guideNumber);
    }
    if (data.originId !== undefined) {
        columns.push('origin_id');
        values.push(data.originId);
    }
    if (data.purchaseOrderId !== undefined) {
        columns.push('purchase_order_uuid');
        values.push(data.purchaseOrderId);
    }
    if (data.createdBy !== undefined) {
        columns.push('created_by');
        values.push(data.createdBy);
    }

    const placeholders = columns.map((_, i) => `$${i + 1}`).join(', ');
    const sql = `INSERT INTO reception (${columns.join(', ')}) VALUES (${placeholders})`;
    await manager.query(sql, values);
    return receptionId;
}

async function rawInsertReceptionSku(
    manager: import('typeorm').EntityManager,
    data: {
        receptionId: string;
        sku: string;
        description: string;
        quantity: number;
        unitCost: number;
        totalCost: number;
        status: number;
        createdBy?: number;
    },
): Promise<void> {
    const skuId = randomUUID();
    const now = new Date();

    const columns: string[] = [
        'reception_sku_id',
        'reception_id',
        'sku',
        'description',
        'quantity',
        'unit_cost',
        'total_cost',
        'status',
        'created_at',
    ];
    const values: any[] = [
        skuId,
        data.receptionId,
        data.sku,
        data.description,
        data.quantity,
        data.unitCost,
        data.totalCost,
        data.status,
        now,
    ];
    if (data.createdBy !== undefined) {
        columns.push('created_by');
        values.push(data.createdBy);
    }
    const placeholders = columns.map((_, i) => `$${i + 1}`).join(', ');
    const sql = `INSERT INTO reception_sku (${columns.join(', ')}) VALUES (${placeholders})`;
    await manager.query(sql, values);
}

async function promoteMigoReceptionsToNormalReceptions(
    migoDocumentId: string,
    folio: string,
    updatedBy: number | undefined,
): Promise<PromotionStats> {
    const stats: PromotionStats = { receptionsCreated: 0, receptionsSkipped: 0, skusCreated: 0 };

    const allMigoRows = await migoRepo.findAllReceptionsByDocument(migoDocumentId);
    const validRows = allMigoRows.filter(r => r.isValid !== false);

    if (validRows.length === 0) {
        logger.warn(`[MIGO] No hay recepciones válidas para promover (doc=${migoDocumentId})`);
        return stats;
    }

    const groups = new Map<string, MigoDocumentReception[]>();
    for (const row of validRows) {
        const key = `${row.nroOc}::${row.nroRecepcion}`;
        const arr = groups.get(key);
        if (arr) arr.push(row);
        else groups.set(key, [row]);
    }

    await datasource.transaction(async (manager) => {
        const purchaseOrderRepo = manager.getRepository(PurchaseOrder);

        for (const [, rows] of groups) {
            const first = rows[0]!;
            const receptionNumber = String(first.nroRecepcion);

            const existsRows = await manager.query(
                'SELECT 1 FROM reception WHERE reception_number = $1 LIMIT 1',
                [receptionNumber],
            );
            if (Array.isArray(existsRows) && existsRows.length > 0) {
                stats.receptionsSkipped++;
                continue;
            }

            let purchaseOrderId: string | undefined;
            try {
                const orderNumber = String(first.nroOc);
                const po = await purchaseOrderRepo.findOne({ where: { orderNumber } });
                if (po) purchaseOrderId = po.purchaseOrderId;
            } catch (err) {
                logger.warn(
                    `[MIGO] No se pudo resolver PurchaseOrder OC=${first.nroOc}: ${(err as Error).message}`,
                );
            }

            const totalAmount = rows.reduce(
                (acc, r) => acc + (Number(r.importeSinImpuestoDet) || 0),
                0,
            );

            const receptionInsert: Parameters<typeof rawInsertReception>[1] = {
                receptionNumber,
                amount: Math.round(totalAmount * 100) / 100,
                status: 1,
                comment: `MIGO ${folio}`,
                receptionDate: first.fechaRecepcion,
            };
            if (first.nroGuia) receptionInsert.guideNumber = first.nroGuia;
            if (first.sucursal != null) receptionInsert.originId = Number(first.sucursal);
            if (purchaseOrderId) receptionInsert.purchaseOrderId = purchaseOrderId;
            if (updatedBy != null) receptionInsert.createdBy = updatedBy;

            const receptionId = await rawInsertReception(manager, receptionInsert);
            stats.receptionsCreated++;

            for (const row of rows) {
                const skuInsert: Parameters<typeof rawInsertReceptionSku>[1] = {
                    receptionId,
                    sku: ((row.sku ?? '').trim() || 'N/A').slice(0, 15),
                    description: ((row.descripcionSku ?? '').trim() || (row.sku ?? 'N/A')).slice(0, 256),
                    quantity: Number(row.cantidad) || 0,
                    unitCost: Number(row.importeUnitario) || 0,
                    totalCost: Number(row.importeSinImpuestoDet) || 0,
                    status: 1,
                };
                if (updatedBy != null) skuInsert.createdBy = updatedBy;

                await rawInsertReceptionSku(manager, skuInsert);
                stats.skusCreated++;
            }
        }
    });

    return stats;
}

export async function authorizeDocument(migoDocumentId: string, updatedBy?: number) {
    const doc = await migoRepo.findDocumentById(migoDocumentId);
    if (!doc) {
        return ResponseHandler.responseBuilder("Documento MIGO no encontrado", null, -1, StatusCodes.NOT_FOUND, false, "");
    }
    if (doc.status !== MigoStatus.PUBLICADO) {
        return ResponseHandler.responseBuilder("Solo se pueden autorizar documentos en estatus 'Publicado' (9)", null, -1, StatusCodes.BAD_REQUEST, false, "");
    }

    let promotionStats: PromotionStats;
    try {
        promotionStats = await promoteMigoReceptionsToNormalReceptions(
            migoDocumentId,
            doc.folio,
            updatedBy,
        );
    } catch (err) {
        const detail = extractDriverDetails(err);
        logger.error(
            `[MIGO] Error al promover recepciones a tabla reception ` +
            `doc=${migoDocumentId} folio=${doc.folio} ` +
            `pgCode=${detail.code ?? 'n/a'} ` +
            `pgConstraint=${detail.constraint ?? 'n/a'} ` +
            `pgTable=${detail.table ?? 'n/a'} ` +
            `pgColumn=${detail.column ?? 'n/a'} ` +
            `pgDetail=${detail.detail ?? 'n/a'} ` +
            `raw=${detail.message ?? 'n/a'}`,
        );

        const reasonBits: string[] = [];
        if (detail.code) reasonBits.push(`código ${detail.code}`);
        if (detail.constraint) reasonBits.push(`constraint ${detail.constraint}`);
        if (detail.column) reasonBits.push(`columna ${detail.column}`);
        const reasonSummary = reasonBits.length > 0 ? ` (${reasonBits.join(', ')})` : '';

        const userMsg =
            `No fue posible publicar las recepciones en el módulo de recepciones${reasonSummary}. ` +
            `La autorización fue revertida.`;

        return ResponseHandler.responseBuilder(
            userMsg,
            { code: detail.code, constraint: detail.constraint, detail: detail.detail, message: detail.message },
            -1,
            StatusCodes.INTERNAL_SERVER_ERROR,
            false,
            detail.detail ?? detail.message ?? 'unknown',
        );
    }

    const now = new Date();
    const updateData: Record<string, any> = {
        status: MigoStatus.AUTORIZADO,
        authorizedAt: now,
        fechaFlujo: now,
    };
    if (updatedBy != null) updateData.updatedBy = updatedBy;
    const updated = await migoRepo.updateDocument(migoDocumentId, updateData);

    logger.info(
        `[MIGO] Document authorized id=${migoDocumentId}, status 9→0, ` +
        `receptions created=${promotionStats.receptionsCreated} skipped=${promotionStats.receptionsSkipped} skus=${promotionStats.skusCreated}`,
    );

    const summaryMsg =
        promotionStats.receptionsCreated > 0
            ? `Documento autorizado. Se publicaron ${promotionStats.receptionsCreated} recepción(es) y ${promotionStats.skusCreated} artículo(s) en el módulo de recepciones.`
            : 'Documento autorizado. Las recepciones ya estaban publicadas previamente.';

    return ResponseHandler.responseBuilder(summaryMsg, updated, 0, StatusCodes.OK, true, "");
}

export async function rejectDocument(dto: RejectMigoDto, updatedBy?: number) {
    const doc = await migoRepo.findDocumentById(dto.migoDocumentId);
    if (!doc) {
        return ResponseHandler.responseBuilder("Documento MIGO no encontrado", null, -1, StatusCodes.NOT_FOUND, false, "");
    }
    if (doc.status !== MigoStatus.PUBLICADO) {
        return ResponseHandler.responseBuilder("Solo se pueden rechazar documentos en estatus 'Publicado' (9)", null, -1, StatusCodes.BAD_REQUEST, false, "");
    }

    const rejectData: Record<string, any> = {
        status: MigoStatus.RECHAZADO,
        rejectionReason: dto.rejectionReason,
        numeroRechazoOc: doc.numeroOc,
    };
    if (updatedBy != null) rejectData.updatedBy = updatedBy;
    const updated = await migoRepo.updateDocument(dto.migoDocumentId, rejectData);

    logger.info(`[MIGO] Document rejected id=${dto.migoDocumentId}, status 9→8`);
    return ResponseHandler.responseBuilder("Documento rechazado", updated, 0, StatusCodes.OK, true, "");
}

export async function exportReceptionsCsv(migoDocumentId: string): Promise<string> {
    const recs = await migoRepo.findAllReceptionsByDocument(migoDocumentId);

    const HEADERS = EXPECTED_CSV_HEADERS;
    const lines = [HEADERS.join(',')];

    for (const r of recs) {
        const fechaStr: string = r.fechaRecepcion
            ? (r.fechaRecepcion instanceof Date ? r.fechaRecepcion : new Date(String(r.fechaRecepcion))).toISOString().split('T')[0] ?? ''
            : '';
        const cells: string[] = [
            String(r.nroOc), String(r.nroRecepcion), String(r.sucursal),
            r.nroGuia || '', r.origen || '',
            fechaStr,
            String(r.importeSinImpuesto), r.sku || '',
            `"${(r.descripcionSku || '').replace(/"/g, '""')}"`,
            String(r.cantidad), String(r.importeUnitario), String(r.importeSinImpuestoDet),
        ];
        if (r.montoOc != null) cells.push(String(r.montoOc));
        lines.push(cells.join(','));
    }

    return lines.join('\n');
}

const EXPECTED_CSV_HEADERS = [
    'Nro_OC', 'Nro_Recepcion', 'Sucursal', 'Nro_Guia', 'Origen',
    'Fecha_Recepcion', 'Importe_sin_impuesto', 'SKU', 'Descripcion_Sku',
    'Cantidad', 'Importe_Unitario', 'Importe_SinImpuesto',
];
