import type { ApiClient } from "@/services/ApiClient";

function toQuery(params: any = {}) {
    return new URLSearchParams(
        Object.entries(params).reduce((acc: any, [k, v]) => {
            if (v !== undefined && v !== null && v !== "") {
                acc[k] = String(v);
            }

            return acc;
        }, {})
    ).toString();
}

function withQuery(path: string, query: string): string {
    return query ? `${path}?${query}` : path;
}

function pad2(value: number): string {
    return String(value).padStart(2, "0");
}

function buildThreeWayMatchFileName(
    extension: "csv" | "xlsx"
): string {
    const now = new Date();

    const yyyy = now.getFullYear();
    const mm = pad2(now.getMonth() + 1);
    const dd = pad2(now.getDate());

    const hh = pad2(now.getHours());
    const min = pad2(now.getMinutes());
    const ss = pad2(now.getSeconds());

    return `three_way_match_${yyyy}${mm}${dd}_${hh}${min}${ss}.${extension}`;
}

export function createThreeWayMatchService(api: ApiClient) {
    async function searchThreeWayMatch(params: any = {}) {
        const query = toQuery(params);

        return api.request(
            withQuery("three-way-match", query),
            "get"
        );
    }

    async function exportThreeWayMatchCsv(params: any = {}) {
        const query = toQuery(params);

        return api.requestBinary(
            withQuery("three-way-match/export/csv", query),
            "get",
            null,
            buildThreeWayMatchFileName("csv")
        );
    }

    async function exportThreeWayMatchXlsx(params: any = {}) {
        const query = toQuery(params);

        return api.requestBinary(
            withQuery("three-way-match/export/xlsx", query),
            "get",
            null,
            buildThreeWayMatchFileName("xlsx")
        );
    }

    return {
        searchThreeWayMatch,
        exportThreeWayMatchCsv,
        exportThreeWayMatchXlsx,
    };
}