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

/** Confirmar / autorizar revisión — solo mientras está publicado o reprocesado. */
export function canConfirmAccountStatement(row: AccountStatementRecord): boolean {
    const status = resolveAccountStatementStatus(row);
    return (
        status === ACCOUNT_STATEMENT_STATUS.PUBLISHED ||
        status === ACCOUNT_STATEMENT_STATUS.REPROCESSED ||
        status === ACCOUNT_STATEMENT_STATUS.GENERATED
    );
}

/** Rechazar / solicitar revisión — no disponible si ya fue confirmado o rechazado. */
export function canRejectAccountStatement(row: AccountStatementRecord): boolean {
    const status = resolveAccountStatementStatus(row);
    return (
        status === ACCOUNT_STATEMENT_STATUS.PUBLISHED ||
        status === ACCOUNT_STATEMENT_STATUS.REPROCESSED ||
        status === ACCOUNT_STATEMENT_STATUS.GENERATED
    );
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
