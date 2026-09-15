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
