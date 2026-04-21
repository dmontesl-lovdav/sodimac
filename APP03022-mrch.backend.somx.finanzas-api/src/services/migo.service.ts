import * as migoRepo from "@/repositories/migo.repo.js";
import { ResponseHandler } from '@/response/ResponseHandler.js';
import { StatusCodes } from 'http-status-codes';
import { ResponsePageableDTO } from '@/response/ResponseHandler.dto.js';
import { MigoStatus } from "@/entities/MigoDocument.entity.js";
import { validateLayout, type ParsedRow } from "@/services/migoLayoutValidation.service.js";
import { logger } from "@/utils/logger.js";
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

export async function authorizeDocument(migoDocumentId: string, updatedBy?: number) {
    const doc = await migoRepo.findDocumentById(migoDocumentId);
    if (!doc) {
        return ResponseHandler.responseBuilder("Documento MIGO no encontrado", null, -1, StatusCodes.NOT_FOUND, false, "");
    }
    if (doc.status !== MigoStatus.PUBLICADO) {
        return ResponseHandler.responseBuilder("Solo se pueden autorizar documentos en estatus 'Publicado' (9)", null, -1, StatusCodes.BAD_REQUEST, false, "");
    }

    const now = new Date();
    const updateData: Record<string, any> = {
        status: MigoStatus.AUTORIZADO,
        authorizedAt: now,
        fechaFlujo: now,
    };
    if (updatedBy != null) updateData.updatedBy = updatedBy;
    const updated = await migoRepo.updateDocument(migoDocumentId, updateData);

    logger.info(`[MIGO] Document authorized id=${migoDocumentId}, status 9→0`);
    return ResponseHandler.responseBuilder("Documento autorizado exitosamente", updated, 0, StatusCodes.OK, true, "");
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
