import { PAYMENT_STATUSES } from "./interfaces";

export function resolvePaymentStatusDisplay(statusId: number): {
    label: string;
    type: "success" | "warning" | "error" | "info";
} {
    const found = PAYMENT_STATUSES.find((s) => s.id === statusId);
    const label = found?.description ?? String(statusId);

    if (statusId === 1) {
        return { label, type: "success" };
    }
    if (statusId === 2) {
        return { label, type: "error" };
    }
    return { label, type: "warning" };
}

export function canRelatePaymentComplement(row: {
    statusId?: number;
    status?: string;
}): boolean {
    if (row.statusId === 0) return true;
    return /pendiente/i.test(row.status ?? "");
}
