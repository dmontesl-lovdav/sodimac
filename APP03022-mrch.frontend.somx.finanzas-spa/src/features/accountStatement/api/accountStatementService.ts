import { createApiClient } from "@/services/ApiClient";
import type {
    AccountStatementFilters,
    PagedAccountStatementResult,
    AccountStatementRecord,
} from '../interfaces';
import type { AccountStatementReportPayload } from '../interfaces/accountStatementReport';

const api = createApiClient();

function normalizeRecord(raw: Record<string, unknown>): AccountStatementRecord {
    return {
        accountStatementUuid: String(raw.accountStatementUuid ?? ""),
        vendorNumber: String(raw.vendorNumber ?? ""),
        vendorName: String(raw.vendorName ?? ""),
        year: Number(raw.year ?? 0),
        month: Number(raw.month ?? 0),
        status:
            raw.status != null && raw.status !== ""
                ? Number(raw.status)
                : undefined,
        statusLabel: (raw.statusLabel as AccountStatementRecord["statusLabel"]) ?? "Generado",
        processedAt: String(raw.processedAt ?? ""),
        reviewedAt: String(raw.reviewedAt ?? ""),
    };
}

export const AccountStatementService = {
    async search(filters: AccountStatementFilters, page: number, pageSize: number): Promise<PagedAccountStatementResult> {
        const params: Record<string, string | number> = {
            year: filters.year,
            page,
            pageSize,
        };
        if (filters.providerId) params.vendorNumber = Number(filters.providerId);
        if (filters.month === 'all') params.month = 'all';
        else if (typeof filters.month === 'number') params.month = filters.month;

        const result = await api.request<PagedAccountStatementResult & { total?: number }>(
            "account-statement",
            "get",
            undefined,
            { params }
        );

        const items = (result?.items ?? []).map((row) =>
            normalizeRecord(row as Record<string, unknown>)
        );

        return {
            items,
            totalItems: Number(result?.totalItems ?? result?.total ?? items.length),
            totalPages: Number(result?.totalPages ?? 1),
            currentPage: Number(result?.currentPage ?? page),
        };
    },

    async getReportData(statementId: string): Promise<AccountStatementReportPayload> {
        return api.request<AccountStatementReportPayload>(
            `account-statement/${statementId}/report-data`,
            "get"
        );
    },

    async confirmReview(statementId: string): Promise<void> {
        return api.request<void>(`account-statement/${statementId}/confirm-review`, "patch");
    },

    async requestReview(statementId: string): Promise<void> {
        return api.request<void>(`account-statement/${statementId}/request-review`, "patch");
    },
};

