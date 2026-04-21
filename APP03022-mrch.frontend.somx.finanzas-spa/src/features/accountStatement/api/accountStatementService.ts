import { createApiClient } from "@/services/ApiClient";
import type {
    AccountStatementFilters,
    PagedAccountStatementResult,
} from '../interfaces';

const api = createApiClient();

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

        return api.request<PagedAccountStatementResult>("account-statement", "get", undefined, { params });
    },

    async getPdf(statementId: string): Promise<Blob> {
        return api.request<Blob>(
            `account-statement/${statementId}/pdf`,
            "get",
            undefined,
            { responseType: "blob" }
        );
    },

    async confirmReview(statementId: string): Promise<void> {
        return api.request<void>(`account-statement/${statementId}/confirm-review`, "patch");
    },

    async requestReview(statementId: string): Promise<void> {
        return api.request<void>(`account-statement/${statementId}/request-review`, "patch");
    },
};

