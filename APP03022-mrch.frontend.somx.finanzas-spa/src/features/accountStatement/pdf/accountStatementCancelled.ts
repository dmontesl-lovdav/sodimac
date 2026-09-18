import type { CatalogStatusItem } from '../interfaces/accountStatementReport';

/** CatEstatusFactura / CatEstatusNotaCredito — cancelada. */
export const CANCELLED_INVOICE_OR_CREDIT_NOTE_STATUS = 20;

/** CatEstatusRecepcion — cancelada. */
export const CANCELLED_RECEPTION_STATUS = 7;

/** CatEstatusRecepcion — borrada (tampoco debe mostrarse en el PDF). */
export const DELETED_RECEPTION_STATUS = 8;

const EXCLUDED_RECEPTION_STATUSES = [
    CANCELLED_RECEPTION_STATUS,
    DELETED_RECEPTION_STATUS,
] as const;

export type StatusOption = {
    value?: string | number;
    label?: string;
};

function toStatusNumber(value: unknown): number | null {
    if (value === undefined || value === null || String(value).trim() === '') {
        return null;
    }
    const n = Number(value);
    return Number.isFinite(n) ? n : null;
}

function textIsCancelled(value: unknown): boolean {
    return String(value ?? '')
        .toLowerCase()
        .includes('cancelad');
}

function catalogKeyIsCancelled(key: unknown): boolean {
    const k = String(key ?? '').toUpperCase();
    return k === 'EFA0020' || k === 'ENC0020';
}

function findCatalogItem(
    status: unknown,
    catalog: CatalogStatusItem[]
): CatalogStatusItem | undefined {
    const raw = String(status ?? '').trim();
    if (!raw) return undefined;
    const n = toStatusNumber(raw);
    return catalog.find((item) => {
        const key = String(item.key ?? '');
        const keyDigits = key.match(/(\d+)$/)?.[1];
        return (
            key === raw ||
            key.toLowerCase() === raw.toLowerCase() ||
            String(item.description ?? '').toLowerCase() === raw.toLowerCase() ||
            (n != null && item.sortOrder === n) ||
            (n != null && keyDigits != null && Number(keyDigits) === n)
        );
    });
}

function optionLabelForStatus(
    status: unknown,
    options: StatusOption[]
): string | undefined {
    const raw = String(status ?? '').trim();
    return options.find((item) => String(item.value) === raw)?.label;
}

export function isCancelledReception(
    status: unknown,
    receptionStatuses: StatusOption[] = []
): boolean {
    if (textIsCancelled(status)) return true;
    const n = toStatusNumber(status);
    if (n != null && (EXCLUDED_RECEPTION_STATUSES as readonly number[]).includes(n)) {
        return true;
    }
    return textIsCancelled(optionLabelForStatus(status, receptionStatuses));
}

export function isCancelledInvoiceOrCreditNote(
    status: unknown,
    catalog: CatalogStatusItem[] = [],
    extraOptions: StatusOption[] = []
): boolean {
    if (textIsCancelled(status) || catalogKeyIsCancelled(status)) return true;
    const n = toStatusNumber(status);
    if (n === CANCELLED_INVOICE_OR_CREDIT_NOTE_STATUS) return true;
    const match = findCatalogItem(status, catalog);
    if (
        match &&
        (textIsCancelled(match.description) || catalogKeyIsCancelled(match.key))
    ) {
        return true;
    }
    return textIsCancelled(optionLabelForStatus(status, extraOptions));
}

export function omitCancelledAccountStatementRows(
    rows: Record<string, unknown>[] | undefined,
    isCancelled: (status: unknown) => boolean
): Record<string, unknown>[] {
    return (rows ?? []).filter((row) => !isCancelled(row.status));
}

/**
 * La tabla de OC del PDF se arma con la recepción. Si esa recepción viene
 * cancelada (o el API ya no la mandó), la OC tampoco debe listarse.
 */
export function omitPurchaseOrdersWithoutVisibleReception(
    purchaseOrders: Record<string, unknown>[] | undefined,
    visibleReceptions: Record<string, unknown>[],
    receptionStatuses: StatusOption[] = []
): Record<string, unknown>[] {
    const visibleIds = new Set(
        visibleReceptions
            .map((row) => String(row.purchaseOrderId ?? '').trim())
            .filter(Boolean)
    );

    return (purchaseOrders ?? []).filter((po) => {
        if (isCancelledReception(po.status, receptionStatuses)) return false;
        const id = String(po.purchaseOrderId ?? po.orderNumber ?? '').trim();
        return id !== '' && visibleIds.has(id);
    });
}
