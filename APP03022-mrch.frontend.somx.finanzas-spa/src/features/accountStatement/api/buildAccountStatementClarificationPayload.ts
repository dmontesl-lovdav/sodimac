import type { AccountStatementRecord } from '../interfaces';
import type { CreateClarificationRequestBody } from '../interfaces/clarificationRequest';

function envInt(name: string, fallback: number): number {
    const raw = process.env[name];
    if (raw == null || raw.trim() === '') return fallback;
    const n = Number(raw);
    return Number.isFinite(n) ? n : fallback;
}

function envStr(name: string, fallback: string): string {
    const raw = process.env[name];
    return raw != null && String(raw).trim() !== '' ? String(raw).trim() : fallback;
}

export function buildAccountStatementReviewClarificationPayload(
    record: AccountStatementRecord
): CreateClarificationRequestBody {
    const period =
        typeof record.month === 'number'
            ? `${record.month}/${record.year}`
            : `${record.year}`;

    return {
        company: envStr('ACCOUNT_STATEMENT_CLARIFICATION_COMPANY', 'Empresa S.A.'),
        rut: envStr('ACCOUNT_STATEMENT_CLARIFICATION_RUT', ''),
        businessUnit: envInt('ACCOUNT_STATEMENT_CLARIFICATION_BUSINESS_UNIT', 2),
        country: envInt('ACCOUNT_STATEMENT_CLARIFICATION_COUNTRY', 4),
        module: envInt('ACCOUNT_STATEMENT_CLARIFICATION_MODULE', 61),
        reason: envInt('ACCOUNT_STATEMENT_CLARIFICATION_REASON', 906),
        detail: envInt('ACCOUNT_STATEMENT_CLARIFICATION_DETAIL', 108),
        clazz: envInt('ACCOUNT_STATEMENT_CLARIFICATION_CLAZZ', 23),
        description: `Solicitud de revisión del estado de cuenta ${period} — ${record.vendorName}`,
        nombreProveedor: record.vendorName,
        orderId: record.accountStatementUuid,
    };
}
