/** CatEstatusFactura / CatEstatusNotaCredito — cancelada. */
export const ACCOUNT_STATEMENT_CANCELLED_INVOICE_STATUS = 20;

/** CatEstatusRecepcion — cancelada. */
export const ACCOUNT_STATEMENT_CANCELLED_RECEPTION_STATUS = 7;

/** CatEstatusRecepcion — borrada (tampoco debe ir al estado de cuenta). */
export const ACCOUNT_STATEMENT_DELETED_RECEPTION_STATUS = 8;

export const ACCOUNT_STATEMENT_EXCLUDED_RECEPTION_STATUSES = [
    ACCOUNT_STATEMENT_CANCELLED_RECEPTION_STATUS,
    ACCOUNT_STATEMENT_DELETED_RECEPTION_STATUS,
] as const;

function toStatusNumber(value: unknown): number | null {
    if (vIsEmpty(value)) return null;
    const n = Number(value);
    return Number.isFinite(n) ? n : null;
}

function vIsEmpty(value: unknown): boolean {
    return value === undefined || value === null || String(value).trim() === "";
}

export function isExcludedInvoiceOrCreditNoteStatus(status: unknown): boolean {
    return toStatusNumber(status) === ACCOUNT_STATEMENT_CANCELLED_INVOICE_STATUS;
}

export function isExcludedReceptionStatus(status: unknown): boolean {
    const n = toStatusNumber(status);
    if (n == null) return false;
    return (ACCOUNT_STATEMENT_EXCLUDED_RECEPTION_STATUSES as readonly number[]).includes(n);
}
