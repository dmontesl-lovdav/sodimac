import { createHash, randomUUID } from 'node:crypto';

/** Contrato INTERNO para el futuro adaptador SQL; no son columnas inventadas. */
export interface RebateSyncRules {
    subtotalAccounts: string[];
    fromDate: string;
}

export interface RebatePayload {
    documentNumber: string;
    referenceNumber: string;
    sapDocument: string;
    vendorNumber: number;
    amount: string;
    source: number;
    periodId: number;
    dueDate: string;
    postingDate: string;
    status: 1;
}

export type PreviewStatus = 'READY' | 'EXCLUDED' | 'DUPLICATE' | 'INVALID' | 'CONFLICT';

export interface PreviewItem {
    index: number;
    sourceKey?: string;
    status: PreviewStatus;
    reason: string;
    payload?: RebatePayload;
}

export interface RebatePreviewReport {
    executionId: string;
    mode: 'DRY_RUN';
    startedAt: string;
    finishedAt: string;
    durationMs: number;
    rules: RebateSyncRules;
    totals: Record<PreviewStatus, number> & { read: number; pages: number; sent: 0 };
    readyAmount: string;
    items: PreviewItem[];
    limitations: string[];
}

function object(value: unknown, field: string): Record<string, unknown> {
    if (value === null || typeof value !== 'object' || Array.isArray(value)) {
        throw new Error(`${field}: se requiere un objeto`);
    }
    return value as Record<string, unknown>;
}

function text(value: unknown, field: string, maximum: number): string {
    if (typeof value !== 'string' || !value.trim() || value.trim().length > maximum) {
        throw new Error(`${field}: texto obligatorio, máximo ${maximum} caracteres`);
    }
    return value.trim();
}

/** Fecha civil explícita: evita normalizar silenciosamente 2026-02-30. */
function dateOnly(value: unknown, field: string): string {
    if (typeof value !== 'string' || !/^\d{4}-\d{2}-\d{2}$/.test(value)) {
        throw new Error(`${field}: usar YYYY-MM-DD`);
    }
    const date = new Date(`${value}T00:00:00.000Z`);
    if (!Number.isFinite(date.getTime()) || date.toISOString().slice(0, 10) !== value) {
        throw new Error(`${field}: fecha inválida`);
    }
    return value;
}

function int32(value: unknown, field: string): number {
    const candidate = typeof value === 'string' ? value.trim() : value;
    if (typeof candidate !== 'number' &&
        (typeof candidate !== 'string' || !/^-?\d+$/.test(candidate))) {
        throw new Error(`${field}: se requiere un entero`);
    }
    const number = Number(candidate);
    if (!Number.isSafeInteger(number) || number < -2147483648 || number > 2147483647) {
        throw new Error(`${field}: fuera del rango integer de PostgreSQL`);
    }
    return number;
}

/** FBC actualmente acepta montos no negativos como texto; nunca usa float. */
export function normalizeRebateAmount(value: unknown): string {
    if (typeof value !== 'string' || !/^\d+(\.\d{1,2})?$/.test(value)) {
        throw new Error('IMPORTE: texto decimal no negativo, sin separadores, máximo 2 decimales');
    }
    const [whole = '', fraction = ''] = value.split('.');
    const integer = whole.replace(/^0+(?=\d)/, '');
    if (integer.length > 14) {
        throw new Error('IMPORTE: excede numeric(16,2)');
    }
    return `${integer}.${fraction.padEnd(2, '0')}`;
}

export function transformRebate(value: unknown): RebatePayload {
    const data = object(value, 'data');
    return {
        documentNumber: text(data.NUMERO_DOCUMENTO, 'NUMERO_DOCUMENTO', 100),
        referenceNumber: text(data.REFERENCIA_DOCUMENTO, 'REFERENCIA_DOCUMENTO', 100),
        sapDocument: text(data.DOC_SAP, 'DOC_SAP', 50),
        vendorNumber: int32(data.CODIGO_PROVEEDOR, 'CODIGO_PROVEEDOR'),
        amount: normalizeRebateAmount(data.IMPORTE),
        source: int32(data.TipoRebate, 'TipoRebate'),
        periodId: int32(data.IdPeriodo, 'IdPeriodo'),
        dueDate: dateOnly(data.FECHA_VENCIMIENTO, 'FECHA_VENCIMIENTO'),
        postingDate: dateOnly(data.FECHA_RECEPCION, 'FECHA_RECEPCION'),
        status: 1,
    };
}

export function parseRebateRules(value: unknown): RebateSyncRules {
    const config = object(value, 'rules');
    if (!Array.isArray(config.subtotalAccounts) || config.subtotalAccounts.length === 0) {
        throw new Error('rules.subtotalAccounts: lista no vacía requerida');
    }
    return {
        subtotalAccounts: [...new Set(config.subtotalAccounts.map(
            (account: unknown) => text(account, 'cuenta contable', 100),
        ))],
        fromDate: dateOnly(config.fromDate, 'rules.fromDate'),
    };
}

function inspectRecord(value: unknown, index: number, rules: RebateSyncRules): PreviewItem {
    const row = object(value, 'record');
    const sourceKey = text(row.sourceKey, 'sourceKey', 300);
    if (typeof row.sapPosted !== 'boolean') {
        throw new Error('sapPosted: boolean requerido; no se deduce de DOC_SAP');
    }
    const account = text(row.account, 'account', 100);
    const eligibilityDate = dateOnly(row.eligibilityDate, 'eligibilityDate');
    const excluded = !row.sapPosted ? 'SAP_NOT_POSTED'
        : !rules.subtotalAccounts.includes(account) ? 'NOT_SUBTOTAL_ACCOUNT'
            : eligibilityDate < rules.fromDate ? 'BEFORE_MINIMUM_DATE' : undefined;
    if (excluded) return { index, sourceKey, status: 'EXCLUDED', reason: excluded };
    return { index, sourceKey, status: 'READY', reason: 'VALIDATED_LOCAL_ONLY', payload: transformRebate(row.data) };
}

/**
 * Ejecuta validación y transformación. NO escribe ni consulta FBC/SQL Server.
 * La entrada completa es local y limitada por el worker; pageSize divide trabajo
 * en bloques, no representa paginación de una base de datos.
 */
export async function runRebateSyncPreview(
    input: unknown,
    options: { pageSize?: number; signal?: AbortSignal } = {},
): Promise<RebatePreviewReport> {
    const started = Date.now();
    const root = object(input, 'input');
    const rules = parseRebateRules(root.rules);
    if (!Array.isArray(root.records)) throw new Error('records: se requiere un arreglo');
    const pageSize = options.pageSize ?? 100;
    if (!Number.isInteger(pageSize) || pageSize < 1 || pageSize > 1000) {
        throw new Error('pageSize: entero entre 1 y 1000');
    }
    const items: PreviewItem[] = [];
    // Primero validar TODO, después decidir qué llaves son conflictivas.
    const fingerprints = new Map<string, string>();
    const conflicts = new Set<string>();
    for (let offset = 0; offset < root.records.length; offset += pageSize) {
        options.signal?.throwIfAborted();
        const page = root.records.slice(offset, offset + pageSize);
        for (const [position, row] of page.entries()) {
            const index = offset + position;
            let item: PreviewItem;
            try {
                item = inspectRecord(row, index, rules);
            } catch (error: unknown) {
                item = { index, status: 'INVALID', reason: error instanceof Error ? error.message : 'INVALID_RECORD' };
            }
            if (item.sourceKey !== undefined && item.payload !== undefined) {
                const fingerprint = createHash('sha256').update(JSON.stringify(item.payload)).digest('hex');
                const previous = fingerprints.get(item.sourceKey);
                if (previous !== undefined && previous !== fingerprint) conflicts.add(item.sourceKey);
                fingerprints.set(item.sourceKey, fingerprint);
            }
            items.push(item);
        }
        // Permite atender SIGINT/SIGTERM entre bloques.
        await new Promise<void>((resolve) => setImmediate(resolve));
    }
    options.signal?.throwIfAborted();
    const seen = new Set<string>();
    const totals: RebatePreviewReport['totals'] = {
        read: items.length, pages: Math.ceil(items.length / pageSize), sent: 0,
        READY: 0, EXCLUDED: 0, DUPLICATE: 0, INVALID: 0, CONFLICT: 0,
    };
    let amountInCents = BigInt(0);
    for (const item of items) {
        if (item.status === 'READY' && item.sourceKey !== undefined && item.payload !== undefined) {
            if (conflicts.has(item.sourceKey)) {
                item.status = 'CONFLICT';
                item.reason = 'SAME_SOURCE_KEY_DIFFERENT_PAYLOAD';
            } else if (seen.has(item.sourceKey)) {
                item.status = 'DUPLICATE';
                item.reason = 'REPEATED_IN_THIS_INPUT_ONLY';
            } else {
                seen.add(item.sourceKey);
                amountInCents += BigInt(item.payload.amount.replace('.', ''));
            }
        }
        totals[item.status]++;
    }
    return {
        executionId: randomUUID(), mode: 'DRY_RUN',
        startedAt: new Date(started).toISOString(), finishedAt: new Date().toISOString(),
        durationMs: Date.now() - started, rules, totals,
        readyAmount: `${amountInCents / BigInt(100)}.${String(amountInCents % BigInt(100)).padStart(2, '0')}`,
        items,
        limitations: [
            'No extracción SQL, autenticación HTTP ni envío real.',
            'sourceKey, sapPosted, account y eligibilityDate son datos normalizados del adaptador pendiente.',
            'READY significa validado localmente, no elegible confirmado en SAP ni ausente en FBC.',
            'DUPLICATE solo aplica a esta entrada; no es idempotencia persistente.',
            'Sin auditoría SQL, reintentos automáticos ni alertas externas.',
        ],
    };
}

/** Planificador puro para el futuro control persistente, no dispara reintentos. */
export function nextRebateRetryAt(attempt: number, failedAt: Date): Date | null {
    if (!Number.isInteger(attempt) || attempt < 1 || attempt > 3 || !Number.isFinite(failedAt.getTime())) {
        throw new Error('Intento esperado: 1, 2 o 3 y fecha válida');
    }
    return attempt < 3 ? new Date(failedAt.getTime() + 60 * 60 * 1000) : null;
}
