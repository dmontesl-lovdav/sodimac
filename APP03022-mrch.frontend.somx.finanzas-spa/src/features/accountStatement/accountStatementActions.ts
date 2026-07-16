import type { AccountStatementRecord, AccountStatementStatus } from "./interfaces";

/** Alineado con finanzas back accountStatement.service.ts */
export const ACCOUNT_STATEMENT_STATUS = {
    GENERATED: 1,
    PUBLISHED: 2,
    REVIEWED: 3,
    REJECTED: 4,
    REPROCESSED: 5,
} as const;

const LABEL_TO_STATUS: Record<AccountStatementStatus, number> = {
    Generado: ACCOUNT_STATEMENT_STATUS.GENERATED,
    Publicado: ACCOUNT_STATEMENT_STATUS.PUBLISHED,
    Revisado: ACCOUNT_STATEMENT_STATUS.REVIEWED,
    Rechazado: ACCOUNT_STATEMENT_STATUS.REJECTED,
    Reprocesado: ACCOUNT_STATEMENT_STATUS.REPROCESSED,
};

export function resolveAccountStatementStatus(
    row: AccountStatementRecord
): number | null {
    if (typeof row.status === "number" && Number.isFinite(row.status)) {
        return row.status;
    }
    if (row.statusLabel && LABEL_TO_STATUS[row.statusLabel] != null) {
        return LABEL_TO_STATUS[row.statusLabel];
    }
    return null;
}

const ACTIONABLE_STATUSES: ReadonlySet<number> = new Set([
    ACCOUNT_STATEMENT_STATUS.PUBLISHED,
    ACCOUNT_STATEMENT_STATUS.REPROCESSED,
    ACCOUNT_STATEMENT_STATUS.GENERATED,
]);

function hasActionableStatus(row: AccountStatementRecord): boolean {
    const status = resolveAccountStatementStatus(row);
    return status != null && ACTIONABLE_STATUSES.has(status);
}

/** Confirmar / autorizar revisión — solo mientras está publicado, reprocesado o generado. */
export function canConfirmAccountStatement(row: AccountStatementRecord): boolean {
    return hasActionableStatus(row);
}

/** Rechazar / solicitar revisión — mismos estatus accionables que confirmar. */
export function canRejectAccountStatement(row: AccountStatementRecord): boolean {
    return hasActionableStatus(row);
}

export function withAccountStatementStatus(
    row: AccountStatementRecord,
    status: number,
    statusLabel: AccountStatementStatus,
    reviewedAt?: string
): AccountStatementRecord {
    return {
        ...row,
        status,
        statusLabel,
        reviewedAt: reviewedAt ?? row.reviewedAt,
    };
}
