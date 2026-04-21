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

export function createThreeWayMatchService(api: ApiClient) {
    async function searchThreeWayMatch(params: any = {}) {
        const query = toQuery(params);
        return api.request(`three-way-match${query ? `?${query}` : ""}`, "get");
    }

    async function exportThreeWayMatchCsv(params: any = {}) {
        const query = toQuery(params);
        return api.requestBinary(
            `three-way-match/export/csv${query ? `?${query}` : ""}`,
            "get",
            null,
            "three-way-match.csv"
        );
    }

    async function exportThreeWayMatchXlsx(params: any = {}) {
        const query = toQuery(params);
        return api.requestBinary(
            `three-way-match/export/xlsx${query ? `?${query}` : ""}`,
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