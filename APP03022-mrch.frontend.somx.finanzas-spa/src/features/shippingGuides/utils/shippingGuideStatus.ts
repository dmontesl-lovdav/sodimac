import type { ShippingGuide, ShippingGuideCatalogItem } from "../interfaces";

export function getShippingGuideStatusCode(guide: ShippingGuide): number {
    const raw: number | ShippingGuideCatalogItem = guide.status;
    if (typeof raw === "number" && Number.isFinite(raw)) return raw;
    const n = Number(raw.internalStatus);
    return Number.isFinite(n) ? n : 0;
}
