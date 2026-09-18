import type { GenericCatalogDetails } from "@/response/GenericCatalogDetails.dto.js";

export function catalogAcceptsReceptionTypeId(
    catalog: GenericCatalogDetails[],
    typeId: number
): boolean {
    if (!catalog.length) {
        return true;
    }
    return catalog.some((cat) => {
        const valueNum = Number(String(cat.value ?? "").trim());
        if (Number.isFinite(valueNum) && valueNum === typeId) {
            return true;
        }
        if (Number(cat.internalStatus) === typeId) {
            return true;
        }
        const catalogId = Number((cat as GenericCatalogDetails & { id?: number }).id);
        return Number.isFinite(catalogId) && catalogId === typeId;
    });
}

export function toNumericReceptionTypeId(raw: unknown): number | undefined {
    if (raw === "" || raw === null || typeof raw === "undefined") {
        return undefined;
    }
    const parsed = Number(raw);
    if (!Number.isFinite(parsed) || parsed < 1) {
        return undefined;
    }
    return parsed;
}

/** Índices value → description, luego internalStatus e id (misma prioridad que el front). */
export function catalogDetailsToIdLabelMap(
    rows: Array<GenericCatalogDetails & { id?: number | string }>
): Map<number, string> {
    const map = new Map<number, string>();
    for (const row of rows ?? []) {
        const label =
            [row.description, row.value, row.externalKey, row.key].find(
                (s): s is string => typeof s === "string" && String(s).trim().length > 0
            )?.trim() ?? "";
        if (!label) {
            continue;
        }
        const keys = [
            Number(String(row.value ?? "").trim()),
            Number(row.internalStatus),
            Number(row.id),
        ];
        for (const key of keys) {
            if (Number.isFinite(key) && !map.has(key)) {
                map.set(key, label);
            }
        }
    }
    return map;
}
