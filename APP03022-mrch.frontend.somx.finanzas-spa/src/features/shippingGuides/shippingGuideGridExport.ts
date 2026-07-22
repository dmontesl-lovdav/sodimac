import { formatDate } from "@/utils/utils";
import type { ShippingGuide } from "./interfaces";
import {
    getRegisteredShippingGuideStatusLabels,
    resolveShippingGuideStatusDescription,
} from "./shippingGuideStatusCatalog";
import { getShippingGuideStatusCode } from "./utils/shippingGuideStatus";

/** Columnas del grid (`ShippingGuideGrid`) para reportes CSV / Excel. */
export const SHIPPING_GUIDE_GRID_EXPORT_HEADERS = [
    "Guía Embarque",
    "Placa",
    "Placa Remolque",
    "Origen",
    "Tipo Entrega",
    "Orden Compra",
    "Número Proveedor",
    "Nombre Proveedor",
    "Fecha Entrega",
    "Fecha Envió",
    "Fecha Registro",
    "Estatus",
] as const;

function getCatalogDisplay(
    item?: {
        description?: string;
        value?: string;
        key?: string;
        internalStatus?: number;
    } | null
): string {
    if (!item) return "N/D";
    return (
        item.description ??
        item.value ??
        item.key ??
        (item.internalStatus != null ? String(item.internalStatus) : "N/D")
    );
}

function formatGuideDate(value?: string | null): string {
    return value ? formatDate(value) : "N/D";
}

/** Una fila de exportación alineada con las columnas visibles del grid. */
export function mapShippingGuideToGridExportRow(
    guide: ShippingGuide
): string[] {
    const code = getShippingGuideStatusCode(guide);
    return [
        guide.guideNumber ?? guide.shippingGuideId ?? "",
        guide.truckPlate ?? "N/D",
        guide.trailerPlate ?? "N/D",
        getCatalogDisplay(guide.OrigenCartaPorte),
        getCatalogDisplay(guide.deliveryType),
        guide.orderNumber ?? "N/D",
        String(guide.vendorNumber ?? guide.supplier?.supplierNumber ?? ""),
        guide.supplier?.businessName ?? "N/D",
        formatGuideDate(guide.deliveryDate),
        formatGuideDate(guide.shippingDate),
        formatGuideDate(guide.createdAt),
        resolveShippingGuideStatusDescription(
            code,
            guide.status,
            getRegisteredShippingGuideStatusLabels() ?? undefined
        ),
    ];
}
