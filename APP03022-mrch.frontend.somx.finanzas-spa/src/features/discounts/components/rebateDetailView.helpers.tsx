import type { CSSProperties, ReactNode } from "react";
import { createApiClient } from "@/services/ApiClient";
import { formatAmount } from "@/utils/utils";
import { buildFiscalSpaUrl } from "@/utils/fiscalSpaUrl";
import { RebateStatusOptions } from "../interfaces";

export const LIST_PATH = "/finanzas/descuentos-comerciales";
export const api = createApiClient();

export interface CreditNote {
    id: string;
    uuid: string;
    registeredAt: string | null;
    amount: string | null;
    series: string | null;
    folio: string | null;
}

export interface FiscalDetail {
    rebateId: string;
    vendorNumber: number | null;
    invoiceFiscalUuid: string | null;
    ncFiscalUuid: string | null;
    creditNotes: CreditNote[];
    message: string | null;
}

export const styles: Record<string, CSSProperties> = {
    container: {
        display: "flex",
        flexDirection: "column",
        gap: "1.5rem",
    },
    header: {
        display: "flex",
        flexDirection: "column",
        gap: "0.5rem",
    },
    title: {
        fontSize: "1.25rem",
        fontWeight: 700,
    },
    subtitle: {
        fontSize: "0.875rem",
        color: "#4b5563",
    },
    grid: {
        display: "grid",
        gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))",
        gap: "1.25rem",
        backgroundColor: "#ffffff",
        border: "1px solid #e5e7eb",
        borderRadius: "0.5rem",
        padding: "1rem",
    },
    sectionTitle: {
        fontSize: "1rem",
        fontWeight: 700,
        marginTop: "0.25rem",
    },
    label: {
        color: "#6b7280",
        fontSize: "1.05rem",
    },
    value: {
        fontSize: "0.875rem",
        fontWeight: 600,
        wordBreak: "break-word",
    },
    emptyBox: {
        padding: "1.25rem",
        background: "#fef3c7",
        border: "1px solid #fcd34d",
        borderRadius: "0.5rem",
        color: "#92400e",
        fontSize: "0.875rem",
    },
    errorBox: {
        padding: "1rem",
        border: "1px solid #fca5a5",
        background: "#fef2f2",
        borderRadius: "0.5rem",
        color: "#991b1b",
    },
    tableCard: {
        padding: "1rem",
        border: "1px solid #e5e7eb",
        borderRadius: "0.5rem",
        background: "#ffffff",
        minWidth: 0,
        overflowX: "auto",
    },
};

export function fmt(value: unknown): string {
    if (value === null || value === undefined || value === "") {
        return "N/D";
    }

    return String(value);
}

export function money(value: unknown): string {
    if (value === null || value === undefined || value === "") {
        return "N/D";
    }

    const amount = Number(value);
    return Number.isFinite(amount) ? formatAmount(amount) : "N/D";
}

export function statusFromCode(value: string): {
    type: string;
    label: string;
} {
    const found = RebateStatusOptions.find(
        (option) => option.value === Number(value)
    );

    return found
        ? { type: found.type, label: found.label }
        : { type: "error", label: value || "N/D" };
}

export function Field({
    label,
    children,
}: {
    label: string;
    children: ReactNode;
}) {
    return (
        <div>
            <div style={styles.label}>{label}</div>
            <div style={styles.value}>{children}</div>
        </div>
    );
}

export function creditNoteHref(
    note: CreditNote,
    supplierNumber: string
): string {
    const params = new URLSearchParams({
        uuid: note.uuid,
    });

    if (supplierNumber) {
        params.set("supplierNumber", supplierNumber);
    }

    if (note.registeredAt) {
        const registered = new Date(note.registeredAt);

        if (Number.isFinite(registered.getTime())) {
            // Incluye días adyacentes para evitar excluir la NC
            // por la conversión entre UTC y la zona horaria del portal.
            const start = new Date(registered);
            const end = new Date(registered);

            start.setUTCDate(start.getUTCDate() - 1);
            end.setUTCDate(end.getUTCDate() + 1);

            params.set("start", start.toISOString().slice(0, 10));
            params.set("end", end.toISOString().slice(0, 10));
        }
    }

    return buildFiscalSpaUrl("notas-credito", params);
}
