import {
    fetchCatalog,
    mapCatalogResponseToFilterOptions,
    type CatalogFilterOption,
} from "@/utils/utils";

/** CatEstatusCartaPorteFBC — respaldo si el catálogo no responde. */
export const SHIPPING_GUIDE_STATUS_FALLBACK: Array<{
    internalStatus: number;
    externalKey: string;
    description: string;
}> = [
    { internalStatus: 1, externalKey: "ECF001", description: "Guía de embarque registrada en el portal de proveedores FBC sin OC" },
    { internalStatus: 2, externalKey: "ECF002", description: "Guía de embarque registrada en el portal de proveedores FBC con OC" },
    { internalStatus: 3, externalKey: "ECF003", description: "Guía de embarque relacionada con una OC y factura" },
    { internalStatus: 4, externalKey: "ECF004", description: "Guía de embarque con OC enviada a contabilizar" },
    { internalStatus: 5, externalKey: "ECF005", description: "Guía de embarque con OC contabilizada" },
    { internalStatus: 6, externalKey: "ECF006", description: "Guía de embarque con OC rechazada en la contabilización" },
    { internalStatus: 7, externalKey: "ECF007", description: "Guía de embarque con OC pagada" },
    { internalStatus: 8, externalKey: "ECF008", description: "Rechazo en la recepción de la guía de embarque" },
    { internalStatus: 9, externalKey: "ECF009", description: "Guía de embarque cancelada en el portal de proveedores FBC" },
];

export const SHIPPING_GUIDE_STATUS_BORRADO = 10;

export type ShippingGuideStatusOption = CatalogFilterOption;

export async function loadShippingGuideStatusFilterOptions(): Promise<
    ShippingGuideStatusOption[]
> {
    const paths = [
        "CatEstatusCartaPorteFBC/details",
        "catalog/CatEstatusCartaPorteFBC",
        "catalog/CatEstatusCartaPorteFBC/details",
        "catalog/CatEstatusCartaPorteFBC",
    ];

    for (const path of paths) {
        const data = await fetchCatalog(path);
        const mapped = mapCatalogResponseToFilterOptions(data);
        if (mapped?.length) {
            return mapped.filter(
                (o) => o.value === "" || Number(o.value) !== SHIPPING_GUIDE_STATUS_BORRADO
            );
        }
    }

    return [
        { label: "Todos los estatus", value: "" },
        ...SHIPPING_GUIDE_STATUS_FALLBACK.filter(
            (e) => e.internalStatus !== SHIPPING_GUIDE_STATUS_BORRADO
        ).map((e) => ({
            label: e.description,
            value: String(e.internalStatus),
        })),
    ];
}

/** La API a veces envía description/value igual al id numérico (p. ej. "1", "2"). */
function isPlaceholderCatalogDescription(
    description: string,
    statusCode: number,
    catalogItem?: { value?: string } | null
): boolean {
    const trimmed = description.trim();
    if (!trimmed) return true;
    if (trimmed === String(statusCode)) return true;
    const val = catalogItem?.value?.trim();
    if (val && trimmed === val) return true;
    if (/^\d+$/.test(trimmed) && Number(trimmed) === statusCode) return true;
    return false;
}

export function resolveShippingGuideStatusDescription(
    statusCode: number,
    catalogItem?: {
        description?: string;
        value?: string;
        key?: string;
        internalStatus?: number;
    } | null,
    labelByCode?: ReadonlyMap<number, string> | Record<number, string>
): string {
    let fromMap: string | undefined;
    if (labelByCode instanceof Map) {
        fromMap = labelByCode.get(statusCode);
    } else if (labelByCode != null) {
        fromMap = (labelByCode as Record<number, string>)[statusCode];
    }
    if (fromMap?.trim()) return fromMap.trim();

    const desc = catalogItem?.description?.trim();
    if (desc && !isPlaceholderCatalogDescription(desc, statusCode, catalogItem)) {
        return desc;
    }

    const byKey = catalogItem?.key?.trim();
    if (byKey) {
        const byExternalKey = SHIPPING_GUIDE_STATUS_FALLBACK.find(
            (e) => e.externalKey === byKey
        );
        if (byExternalKey) return byExternalKey.description;
    }

    const found = SHIPPING_GUIDE_STATUS_FALLBACK.find(
        (e) => e.internalStatus === statusCode
    );
    if (found) return found.description;

    if (catalogItem?.value?.trim()) return catalogItem.value.trim();
    if (byKey) return byKey;
    return String(statusCode);
}

let statusLabelByCode: Map<number, string> | null = null;

/** Registra etiquetas del catálogo cargado en filtros para reutilizar en grid/detalle. */
export function registerShippingGuideStatusLabels(
    options: ShippingGuideStatusOption[]
): void {
    const map = new Map<number, string>();
    for (const opt of options) {
        if (!opt.value?.trim()) continue;
        const code = Number(opt.value);
        if (!Number.isFinite(code)) continue;
        if (opt.label?.trim()) map.set(code, opt.label.trim());
    }
    statusLabelByCode = map.size > 0 ? map : null;
}

export function getRegisteredShippingGuideStatusLabels(): ReadonlyMap<
    number,
    string
> | null {
    return statusLabelByCode;
}
