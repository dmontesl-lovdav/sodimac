// src/features/three-way-match/api/threeWayMatchService.ts
import type { ApiClient } from "@/services/ApiClient";

function toQuery(params: any = {}) {
    return new URLSearchParams(
        Object.entries(params).reduce((acc: any, [k, v]) => {
            if (v !== undefined && v !== null && v !== "") acc[k] = String(v);
            return acc;
        }, {})
    ).toString();
}

function withQuery(path: string, query: string): string {
    return query ? `${path}?${query}` : path;
}

export function createThreeWayMatchService(api: ApiClient) {
    async function searchThreeWayMatch(params: any = {}) {
        const query = toQuery(params);
        return api.request(withQuery("three-way-match", query), "get");
    }

    async function exportThreeWayMatchCsv(params: any = {}) {
        const query = toQuery(params);
        return api.requestBinary(
            withQuery("three-way-match/export/csv", query),
            "get",
            null,
            "three-way-match.csv"
        );
    }

    async function exportThreeWayMatchXlsx(params: any = {}) {
        const query = toQuery(params);
        return api.requestBinary(
            withQuery("three-way-match/export/xlsx", query),
            "get",
            null,
            "three-way-match.xlsx"
        );
    }

    return {
        searchThreeWayMatch,
        exportThreeWayMatchCsv,
        exportThreeWayMatchXlsx,
    };
}
