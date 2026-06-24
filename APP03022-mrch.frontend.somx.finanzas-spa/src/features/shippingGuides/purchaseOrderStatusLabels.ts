/** Etiquetas legibles para estatus numérico de orden de compra en detalle de guía. */
const PURCHASE_ORDER_STATUS_LABELS: Record<number, string> = {
    0: "Cancelada",
    1: "Registrada",
    2: "En proceso",
    3: "Cerrada",
    4: "Cancelada",
};

export function resolvePurchaseOrderStatusDescription(
    status: number | string | null | undefined
): string {
    if (status === null || status === undefined || status === "") return "N/D";
    const n = Number(status);
    if (Number.isFinite(n) && PURCHASE_ORDER_STATUS_LABELS[n]) {
        return PURCHASE_ORDER_STATUS_LABELS[n];
    }
    return String(status);
}
