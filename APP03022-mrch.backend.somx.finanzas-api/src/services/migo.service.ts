import { randomUUID } from "node:crypto";
import * as migoRepo from "@/repositories/migo.repo.js";
import * as svcAxios from "@/services/axios.service.js";
import { ResponseHandler } from '@/response/ResponseHandler.js';
import { StatusCodes } from 'http-status-codes';
import { ResponsePageableDTO } from '@/response/ResponseHandler.dto.js';
import { MigoStatus } from "@/entities/MigoDocument.entity.js";
import { validateLayout, parseLayoutDate, type ParsedRow } from "@/services/migoLayoutValidation.service.js";
import { logger } from "@/utils/logger.js";
import { datasource } from "@/config/typeorm-datasource.js";
import { PurchaseOrder } from "@/entities/PurchaseOrder.entity.js";
import { MigoDocumentReception } from "@/entities/MigoDocumentReception.entity.js";
import { In } from "typeorm";
import type { Supplier } from "@/response/GenericCatalogDetails.dto.js";
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

type SupplierInfo = { vendorName: string; emailFinancial: string };

async function buildSupplierIndexByNumber(
    authToken: string,
): Promise<Map<number, SupplierInfo>> {
    const index = new Map<number, SupplierInfo>();
    let supplierList: Supplier[] = [];
    try {
        supplierList = await svcAxios.GetSuppliers(authToken);
    } catch (err) {
        logger.warn(`[MIGO] No se pudo obtener catálogo de proveedores: ${(err as Error).message}`);
        return index;
    }
    for (const s of supplierList) {
        const n = Number(s.supplierNumber);
        if (!Number.isFinite(n)) continue;
        index.set(n, {
            vendorName: s.businessName ?? "",
            emailFinancial: s.emailFinancial ?? "",
        });
    }
    return index;
}

async function buildSupplierIndexByOc(
    rows: MigoDocumentReception[],
    supplierIndexByNumber: Map<number, SupplierInfo>,
): Promise<Map<string, SupplierInfo>> {
    const index = new Map<string, SupplierInfo>();
    const distinctOcs = [...new Set(rows.map(r => String(r.nroOc)).filter(Boolean))];
    if (distinctOcs.length === 0) return index;

    let purchaseOrders: PurchaseOrder[] = [];
    try {
        purchaseOrders = await datasource.getRepository(PurchaseOrder).find({
            where: { orderNumber: In(distinctOcs) },
        });
    } catch (err) {
        logger.warn(`[MIGO] No se pudieron resolver PurchaseOrders por nroOc: ${(err as Error).message}`);
        return index;
    }

    for (const po of purchaseOrders) {
        const info = supplierIndexByNumber.get(Number(po.supplierNumber));
        if (info) index.set(po.orderNumber, info);
    }
    return index;
}

export async function listReceptions(q: ListMigoReceptionsQueryDto, authToken = "") {
    const { result, total } = await migoRepo.findReceptionsByDocumentPaginated(q);
    const totalPages = Math.ceil(total / q.pageSize);

    const supplierIndexByNumber = await buildSupplierIndexByNumber(authToken);
    const supplierIndexByOc = await buildSupplierIndexByOc(result, supplierIndexByNumber);

    const enrichedRows = result.map(row => {
        const supplierNumberRaw = (row.numeroProveedor ?? '').toString().trim();
        const supplierNumberKey = Number(supplierNumberRaw);
        const byNumber = Number.isFinite(supplierNumberKey)
            ? supplierIndexByNumber.get(supplierNumberKey)
            : undefined;
        const byOc = supplierIndexByOc.get(String(row.nroOc));
        const supplierInfo = byNumber ?? byOc;
        return {
            ...row,
            vendorName: supplierInfo?.vendorName ?? "",
            emailFinancial: supplierInfo?.emailFinancial ?? "",
        };
    });

    const page: ResponsePageableDTO = {
        content: enrichedRows,
        totalElements: total,
        numberOfElements: enrichedRows.length,
        totalPages,
        pageNumber: q.pageNumber,
        pageSize: q.pageSize,
    };

    return ResponseHandler.responseBuilder("", page, 0, StatusCodes.OK, true, "");
}

type LayoutValidationResult = ReturnType<typeof validateLayout>;

interface DuplicateReceptionResult {
    duplicateList: string;
    duplicates: string[];
}

function buildReceptionPairMap(
    validRows: ParsedRow[],
): Map<string, { oc: number; reception: number }> {
    const pairMap = new Map<string, { oc: number; reception: number }>();

    for (const row of validRows) {
        const oc = toNum(row.Nro_OC);
        const reception = toNum(row.Nro_Recepcion);

        pairMap.set(
            `${oc}::${reception}`,
            { oc, reception },
        );
    }

    return pairMap;
}

async function findDuplicateReceptions(
    validRows: ParsedRow[],
): Promise<DuplicateReceptionResult | null> {
    const pairMap = buildReceptionPairMap(validRows);

    if (pairMap.size === 0) {
        return null;
    }

    const existing = await migoRepo.findExistingReceptionPairs(
        [...pairMap.values()],
    );

    if (existing.size === 0) {
        return null;
    }

    const duplicates = [...existing];
    const duplicateList = duplicates
        .map((key) => {
            const [oc, reception] = key.split('::');
            return `OC ${oc} / Recepción ${reception}`;
        })
        .join('; ');

    return {
        duplicateList,
        duplicates,
    };
}

function buildReceptionRecord(
    row: ParsedRow,
): Record<string, any> {
    const reception: Record<string, any> = {
        nroOc: toNum(row.Nro_OC),
        nroRecepcion: toNum(row.Nro_Recepcion),
        numeroProveedor: row.Numero_Proveedor || null,
        sucursal: toNum(row.Sucursal),
        fechaRecepcion:
            parseLayoutDate(row.Fecha_Recepcion) ??
            new Date(row.Fecha_Recepcion),
        importeSinImpuesto: toNum(row.Importe_sin_impuesto),
        cantidad: toNum(row.Cantidad),
        importeUnitario: toNum(row.Importe_Unitario),
        importeSinImpuestoDet: toNum(row.Importe_SinImpuesto),
        isValid: true,
        rowNumber: row.rowNumber,
    };

    addOptionalReceptionFields(reception, row);

    return reception;
}

function addOptionalReceptionFields(
    reception: Record<string, any>,
    row: ParsedRow,
): void {
    if (row.Nro_Guia) {
        reception.nroGuia = row.Nro_Guia;
    }

    if (row.Origen) {
        reception.origen = row.Origen;
    }

    if (row.SKU) {
        reception.sku = row.SKU;
    }

    if (row.Descripcion_Sku) {
        reception.descripcionSku = row.Descripcion_Sku;
    }

    const montoOc = getReceptionAmount(row);
    if (montoOc !== undefined) {
        reception.montoOc = montoOc;
    }
}

function getReceptionAmount(
    row: ParsedRow,
): number | undefined {
    if (row.MontoOC) {
        return toNum(row.MontoOC);
    }

    if (row.Importe_sin_impuesto) {
        return toNum(row.Importe_sin_impuesto);
    }

    return undefined;
}

function calculateTotalMontoOc(
    validRows: ParsedRow[],
): number {
    const amountByPurchaseOrder = new Map<string, number>();

    for (const row of validRows) {
        if (amountByPurchaseOrder.has(row.Nro_OC)) {
            continue;
        }

        amountByPurchaseOrder.set(
            row.Nro_OC,
            getReceptionAmount(row) ?? 0,
        );
    }

    return [...amountByPurchaseOrder.values()]
        .reduce((sum, value) => sum + value, 0);
}

function buildMigoDocument(
    validation: LayoutValidationResult,
    validRows: ParsedRow[],
    receptions: Array<Record<string, any>>,
    fileName: string,
    folio: string,
    createdBy?: number,
): Record<string, any> {
    const distinctOcs = new Set(
        validRows.map((row) => row.Nro_OC),
    );
    const distinctReceptions = new Set(
        validRows.map((row) => row.Nro_Recepcion),
    );
    const totalMontoOc = calculateTotalMontoOc(validRows);

    const document: Record<string, any> = {
        folio,
        fileName,
        totalRecords: validation.totalRows,
        numeroOc: distinctOcs.size,
        numeroRecepcion: distinctReceptions.size,
        montoOc: Math.round(totalMontoOc * 100) / 100,
        numeroRechazoOc: 0,
        status:
            validRows.length > 0
                ? MigoStatus.PUBLICADO
                : MigoStatus.RECHAZADO,
        publishedAt: new Date(),
        receptions,
    };

    if (createdBy != null) {
        document.createdBy = createdBy;
    }

    return document;
}

function buildInvalidDetails(
    invalidRows: ParsedRow[],
): Array<{ row: number; errors: ParsedRow['errors'] }> {
    return invalidRows.map((row) => ({
        row: row.rowNumber,
        errors: row.errors,
    }));
}

function buildValidationSummary(
    validation: LayoutValidationResult,
): string {
    return (
        'La validación del layout ha finalizado correctamente.\n' +
        `Total de registros: ${validation.totalRows}\n` +
        `Total válidos: ${validation.totalValid}\n` +
        `Total incorrectos: ${validation.totalInvalid}`
    );
}

function buildGlobalValidationErrorResponse(
    validation: LayoutValidationResult,
) {
    const globalError = validation.globalError!;

    return ResponseHandler.responseBuilder(
        globalError.message,
        {
            code: globalError.code,
            totalRows: validation.totalRows,
        },
        -1,
        StatusCodes.BAD_REQUEST,
        false,
        globalError.message,
    );
}

function buildDuplicateErrorResponse(
    duplicateResult: DuplicateReceptionResult,
) {
    const message =
        'El documento no se puede cargar porque contiene ' +
        `recepciones que ya existen: ${duplicateResult.duplicateList}.`;

    logger.warn(
        `[MIGO] Upload rechazado por duplicados: ${duplicateResult.duplicateList}`,
    );

    return ResponseHandler.responseBuilder(
        message,
        {
            code: 'WRN7021',
            duplicates: duplicateResult.duplicates,
        },
        -1,
        StatusCodes.BAD_REQUEST,
        false,
        message,
    );
}

const SUPPLIER_STATUS_INACTIVE = 0;

type SupplierIssueReason = 'inactive' | 'not_catalogued';
interface SupplierIssue {
    supplierNumber: string;
    reason: SupplierIssueReason;
}

function normalizeSupplierNumber(value: unknown): string {
    const raw = String(value ?? '').trim();
    if (raw === '') return '';
    const num = Number(raw);
    return Number.isFinite(num) ? String(num) : raw;
}

async function findSupplierIssues(
    validRows: ParsedRow[],
    authToken: string,
): Promise<SupplierIssue[] | null> {
    const distinct = [
        ...new Set(
            validRows
                .map((row) => normalizeSupplierNumber(row.Numero_Proveedor))
                .filter((value) => value !== ''),
        ),
    ];
    if (distinct.length === 0) return null;

    let supplierList: Supplier[];
    try {
        supplierList = await svcAxios.GetSuppliers(authToken);
    } catch (err) {
        logger.warn(
            `[MIGO] No se pudo validar proveedores (catálogo no disponible): ${(err as Error).message}`,
        );
        return null;
    }

    const statusByNumber = new Map<string, number>();
    for (const s of supplierList) {
        const key = normalizeSupplierNumber(s.supplierNumber);
        if (key !== '') statusByNumber.set(key, Number(s.status));
    }

    const issues: SupplierIssue[] = [];
    for (const num of distinct) {
        if (!statusByNumber.has(num)) {
            issues.push({ supplierNumber: num, reason: 'not_catalogued' });
        } else if (statusByNumber.get(num) === SUPPLIER_STATUS_INACTIVE) {
            issues.push({ supplierNumber: num, reason: 'inactive' });
        }
    }

    return issues.length > 0 ? issues : null;
}

function supplierIssueMessage(issue: SupplierIssue): string {
    if (issue.reason === 'inactive') {
        return `No es posible publicar la recepción del proveedor ${issue.supplierNumber} se encuentra inactivo, favor de validar.`;
    }
    return `No es posible publicar la recepción del proveedor ${issue.supplierNumber} porque no se encuentra catálogado, favor de validar.`;
}

function buildSupplierIssuesErrorResponse(issues: SupplierIssue[]) {
    const message = issues.map(supplierIssueMessage).join('\n');
    const hasInactive = issues.some((i) => i.reason === 'inactive');
    const code = hasInactive ? 'WRN7036' : 'WRN7037';

    logger.warn(
        `[MIGO] Upload rechazado por proveedor(es): ${issues
            .map((i) => `${i.supplierNumber}(${i.reason})`)
            .join(', ')}`,
    );

    return ResponseHandler.responseBuilder(
        message,
        {
            code,
            issues: issues.map((i) => ({
                supplierNumber: i.supplierNumber,
                code: i.reason === 'inactive' ? 'WRN7036' : 'WRN7037',
            })),
        },
        -1,
        StatusCodes.BAD_REQUEST,
        false,
        message,
    );
}

export async function uploadCsv(
    fileContent: string,
    fileName: string,
    createdBy?: number,
    authToken = '',
) {
    const validation = validateLayout(fileContent);

    if (validation.globalError) {
        return buildGlobalValidationErrorResponse(validation);
    }

    const validRows = validation.parsedRows.filter(
        (row) => row.isValid,
    );
    const invalidRows = validation.parsedRows.filter(
        (row) => !row.isValid,
    );

    const supplierIssues = await findSupplierIssues(validRows, authToken);
    if (supplierIssues) {
        return buildSupplierIssuesErrorResponse(supplierIssues);
    }

    const duplicateResult = await findDuplicateReceptions(validRows);
    if (duplicateResult) {
        return buildDuplicateErrorResponse(duplicateResult);
    }

    const receptions = validRows.map(buildReceptionRecord);
    const folio = generateFolio();
    const document = buildMigoDocument(
        validation,
        validRows,
        receptions,
        fileName,
        folio,
        createdBy,
    );
    const saved = await migoRepo.saveDocument(document);
    const invalidDetails = buildInvalidDetails(invalidRows);
    const summaryMsg = buildValidationSummary(validation);

    logger.info(
        `[MIGO] Document created folio=${folio}, ` +
        `total=${validation.totalRows}, ` +
        `valid=${validation.totalValid}, ` +
        `invalid=${validation.totalInvalid}`,
    );

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
        '',
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

function resolveSchema(manager: import('typeorm').EntityManager): string {
    const opts = manager.connection.options as { schema?: string };
    const candidates = [opts.schema, process.env.DB_SCHEMA];
    for (const c of candidates) {
        if (typeof c === 'string' && c.trim().length > 0) return c.trim();
    }
    return 'tenant_finance';
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
    const schema = resolveSchema(manager);

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
    const sql = `INSERT INTO "${schema}".reception (${columns.join(', ')}) VALUES (${placeholders})`;
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
    const schema = resolveSchema(manager);

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
    const sql = `INSERT INTO "${schema}".reception_sku (${columns.join(', ')}) VALUES (${placeholders})`;
    await manager.query(sql, values);
}

function groupMigoReceptions(
    rows: MigoDocumentReception[],
): Map<string, MigoDocumentReception[]> {
    const groups = new Map<string, MigoDocumentReception[]>();

    for (const row of rows) {
        const key = `${row.nroOc}::${row.nroRecepcion}`;
        const currentRows = groups.get(key);

        if (currentRows) {
            currentRows.push(row);
        } else {
            groups.set(key, [row]);
        }
    }

    return groups;
}

function calculateRoundedReceptionAmount(
    rows: MigoDocumentReception[],
): number {
    const totalAmount = rows.reduce(
        (acc, row) =>
            acc + (Number(row.importeSinImpuestoDet) || 0),
        0,
    );

    return Math.round(totalAmount * 100) / 100;
}

function isValidVendorNumber(value: number): boolean {
    return Number.isFinite(value) && value > 0;
}

function buildPurchaseOrderDraft(
    first: MigoDocumentReception,
    orderNumber: string,
    vendorNumber: number,
    roundedTotalAmount: number,
    updatedBy: number | undefined,
): PurchaseOrder {
    const draft = new PurchaseOrder();
    const branchNumber = Number(first.sucursal);

    draft.orderNumber = orderNumber;
    draft.supplierNumber = vendorNumber;
    draft.purchaseOrderDate =
        first.fechaRecepcion ?? new Date();
    draft.originId = Number.isFinite(branchNumber)
        ? branchNumber
        : 0;
    draft.amount = roundedTotalAmount;
    draft.status = 1;

    if (updatedBy != null) {
        draft.createdBy = updatedBy;
    }

    return draft;
}

async function resolvePurchaseOrderId(
    manager: import('typeorm').EntityManager,
    first: MigoDocumentReception,
    roundedTotalAmount: number,
    updatedBy: number | undefined,
    folio: string,
): Promise<string | undefined> {
    const purchaseOrderRepo =
        manager.getRepository(PurchaseOrder);
    const orderNumber = String(first.nroOc);

    try {
        let purchaseOrder = await purchaseOrderRepo.findOne({
            where: { orderNumber },
        });

        if (!purchaseOrder && first.numeroProveedor) {
            const vendorRaw =
                String(first.numeroProveedor).trim();
            const vendorNumber = Number(vendorRaw);

            if (!isValidVendorNumber(vendorNumber)) {
                logger.warn(
                    `[MIGO] numero_proveedor inválido para OC=${orderNumber}: '${vendorRaw}'. La recepción quedará sin OC.`,
                );
                return undefined;
            }

            const draft = buildPurchaseOrderDraft(
                first,
                orderNumber,
                vendorNumber,
                roundedTotalAmount,
                updatedBy,
            );

            purchaseOrder =
                await purchaseOrderRepo.save(draft);

            logger.info(
                `[MIGO] OC ${orderNumber} no existía. Creada automáticamente con vendor=${vendorNumber} (origen MIGO doc=${folio}).`,
            );
        }

        return purchaseOrder?.purchaseOrderId;
    } catch (err) {
        logger.warn(
            `[MIGO] No se pudo resolver PurchaseOrder OC=${first.nroOc}: ${(err as Error).message}`,
        );
        return undefined;
    }
}

async function receptionAlreadyExists(
    manager: import('typeorm').EntityManager,
    schema: string,
    receptionNumber: string,
    purchaseOrderId: string | undefined,
): Promise<boolean> {
    const existingRows = purchaseOrderId
        ? await manager.query(
            `SELECT 1 FROM "${schema}".reception WHERE reception_number = $1 AND purchase_order_uuid = $2 LIMIT 1`,
            [receptionNumber, purchaseOrderId],
        )
        : await manager.query(
            `SELECT 1 FROM "${schema}".reception WHERE reception_number = $1 AND purchase_order_uuid IS NULL LIMIT 1`,
            [receptionNumber],
        );

    return (
        Array.isArray(existingRows) &&
        existingRows.length > 0
    );
}

function buildReceptionInsert(
    first: MigoDocumentReception,
    receptionNumber: string,
    roundedTotalAmount: number,
    purchaseOrderId: string | undefined,
    folio: string,
    updatedBy: number | undefined,
): Parameters<typeof rawInsertReception>[1] {
    const receptionInsert:
        Parameters<typeof rawInsertReception>[1] = {
        receptionNumber,
        amount: roundedTotalAmount,
        status: 0,
        comment: `MIGO ${folio}`,
        receptionDate: first.fechaRecepcion,
    };

    if (first.nroGuia) {
        receptionInsert.guideNumber = first.nroGuia;
    }

    if (first.sucursal != null) {
        receptionInsert.originId =
            Number(first.sucursal);
    }

    if (purchaseOrderId) {
        receptionInsert.purchaseOrderId =
            purchaseOrderId;
    }

    if (updatedBy != null) {
        receptionInsert.createdBy = updatedBy;
    }

    return receptionInsert;
}

function buildSkuInsert(
    row: MigoDocumentReception,
    receptionId: string,
    updatedBy: number | undefined,
): Parameters<typeof rawInsertReceptionSku>[1] {
    const skuInsert:
        Parameters<typeof rawInsertReceptionSku>[1] = {
        receptionId,
        sku:
            ((row.sku ?? '').trim() || 'N/A')
                .slice(0, 15),
        description:
            (
                (row.descripcionSku ?? '').trim() ||
                (row.sku ?? 'N/A')
            ).slice(0, 256),
        quantity:
            Number(row.cantidad) || 0,
        unitCost:
            Number(row.importeUnitario) || 0,
        totalCost:
            Number(row.importeSinImpuestoDet) || 0,
        status: 0,
    };

    if (updatedBy != null) {
        skuInsert.createdBy = updatedBy;
    }

    return skuInsert;
}

async function insertReceptionSkus(
    manager: import('typeorm').EntityManager,
    rows: MigoDocumentReception[],
    receptionId: string,
    updatedBy: number | undefined,
    stats: PromotionStats,
): Promise<void> {
    for (const row of rows) {
        const skuInsert = buildSkuInsert(
            row,
            receptionId,
            updatedBy,
        );

        await rawInsertReceptionSku(
            manager,
            skuInsert,
        );

        stats.skusCreated++;
    }
}

async function promoteReceptionGroup(
    manager: import('typeorm').EntityManager,
    rows: MigoDocumentReception[],
    folio: string,
    updatedBy: number | undefined,
    stats: PromotionStats,
): Promise<void> {
    const first = rows[0]!;
    const receptionNumber =
        String(first.nroRecepcion);
    const schema = resolveSchema(manager);
    const roundedTotalAmount =
        calculateRoundedReceptionAmount(rows);

    const purchaseOrderId =
        await resolvePurchaseOrderId(
            manager,
            first,
            roundedTotalAmount,
            updatedBy,
            folio,
        );

    const alreadyExists =
        await receptionAlreadyExists(
            manager,
            schema,
            receptionNumber,
            purchaseOrderId,
        );

    if (alreadyExists) {
        logger.info(
            `[MIGO] Recepción ${receptionNumber} ya publicada para OC=${first.nroOc} (purchase_order_uuid=${purchaseOrderId ?? 'NULL'}). Saltando.`,
        );
        stats.receptionsSkipped++;
        return;
    }

    const receptionInsert =
        buildReceptionInsert(
            first,
            receptionNumber,
            roundedTotalAmount,
            purchaseOrderId,
            folio,
            updatedBy,
        );

    const receptionId =
        await rawInsertReception(
            manager,
            receptionInsert,
        );

    stats.receptionsCreated++;

    await insertReceptionSkus(
        manager,
        rows,
        receptionId,
        updatedBy,
        stats,
    );
}

async function executePromotionTransaction(
    manager: import('typeorm').EntityManager,
    groups: Map<string, MigoDocumentReception[]>,
    migoDocumentId: string,
    folio: string,
    updatedBy: number | undefined,
    stats: PromotionStats,
): Promise<void> {
    const dbSchema = resolveSchema(manager);

    logger.info(
        `[MIGO] Promoviendo recepciones doc=${migoDocumentId} usando schema="${dbSchema}"`,
    );

    for (const rows of groups.values()) {
        await promoteReceptionGroup(
            manager,
            rows,
            folio,
            updatedBy,
            stats,
        );
    }
}

async function promoteMigoReceptionsToNormalReceptions(
    migoDocumentId: string,
    folio: string,
    updatedBy: number | undefined,
): Promise<PromotionStats> {
    const stats: PromotionStats = {
        receptionsCreated: 0,
        receptionsSkipped: 0,
        skusCreated: 0,
    };

    const allMigoRows =
        await migoRepo.findAllReceptionsByDocument(
            migoDocumentId,
        );

    const validRows = allMigoRows.filter(
        (row) => row.isValid !== false,
    );

    if (validRows.length === 0) {
        logger.warn(
            `[MIGO] No hay recepciones válidas para promover (doc=${migoDocumentId})`,
        );

        return {
            receptionsCreated: 0,
            receptionsSkipped: 0,
            skusCreated: 0,
        };
    }

    const groups = groupMigoReceptions(validRows);

    await datasource.transaction(
        async (manager) =>
            executePromotionTransaction(
                manager,
                groups,
                migoDocumentId,
                folio,
                updatedBy,
                stats,
            ),
    );

    return {
        receptionsCreated:
            stats.receptionsCreated,
        receptionsSkipped:
            stats.receptionsSkipped,
        skusCreated:
            stats.skusCreated,
    };
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
        let fechaStr: string = '';

        if (r.fechaRecepcion) {
            const fecha =
                r.fechaRecepcion instanceof Date
                    ? r.fechaRecepcion
                    : new Date(String(r.fechaRecepcion));

            fechaStr = fecha.toISOString().split('T')[0] || '';
        }
        const cells: string[] = [
            String(r.nroOc), String(r.nroRecepcion),
            r.numeroProveedor || '',
            String(r.sucursal),
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
    'Nro_OC', 'Nro_Recepcion', 'Numero_Proveedor', 'Sucursal', 'Nro_Guia', 'Origen',
    'Fecha_Recepcion', 'Importe_sin_impuesto', 'SKU', 'Descripcion_Sku',
    'Cantidad', 'Importe_Unitario', 'Importe_SinImpuesto',
];