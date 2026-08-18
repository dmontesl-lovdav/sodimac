import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { withFinanceBreadcrumb } from "@/shared/components/ui/navigation/financeBreadcrumb";
import { StatusPill } from "@/shared/components/ui/statusPill/StatusPill";
import { formatAmount, formatDate, formatDateTime } from "@/utils/utils";
import { ReactElement, ReactNode, useMemo } from "react";
import { useSearchParams } from "react-router-dom";
import { RebateStatusOptions } from "../interfaces";
import { parseRebateDetailFromSearchParams } from "../utils/rebateDetailQuery";
import { FINANCE_LIST_KEYS } from "@/shared/hooks";

import { buildFiscalSpaUrl } from "@/utils/fiscalSpaUrl";
import eyeIconUrl from "@assets/eye-show.svg";

const LIST_PATH = "/finanzas/descuentos-comerciales";



const styles = {
    container: {
        display: "flex",
        flexDirection: "column" as const,
        gap: "1.5rem",
    },
    header: {
        display: "flex",
        flexDirection: "column" as const,
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
        gridTemplateColumns: "repeat(5, minmax(0, 1fr))",
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
        wordBreak: "break-word" as const,
    },
    emptyBox: {
        padding: "1.25rem",
        background: "#fef3c7",
        border: "1px solid #fcd34d",
        borderRadius: "0.5rem",
        color: "#92400e",
        fontSize: "0.875rem",
    },
    actionButton: {
        background: "transparent",
        border: "none",
        padding: 0,
        cursor: "pointer",
        opacity: 0.8,
    },
};

function fmt(v: unknown): string {
    if (v === null || v === undefined || v === "") return "N/D";
    return String(v);
}

function statusFromCode(statusStr: string): { type: string; label: string } {
    const n = Number(statusStr);
    const found = RebateStatusOptions.find((o) => o.value === n);
    if (found) return { type: found.type, label: found.label };
    return { type: "error", label: statusStr ?? "N/D" };
}

function Field({
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

export default function RebateDetailView(): ReactElement {
    const [searchParams] = useSearchParams();
    
    const d = useMemo(
        () => parseRebateDetailFromSearchParams(searchParams),
        [searchParams]
    );

    const docLabel =
        d.documentNumber?.trim() ||
        ((t) => (t == null || t === "" ? "—" : t))(d.rebateId?.trim());

    const hasPayload =
        Boolean(d.documentNumber?.trim()) ||
        Boolean(d.rebateId?.trim()) ||
        Boolean(d.supplierNumber?.trim());

    const breadcrumb: BreadcrumbItem[] = useMemo(
        () =>
            withFinanceBreadcrumb([
                { label: "Descuentos Comerciales", to: LIST_PATH },
                {
                    label: d.documentNumber?.trim()
                        ? `Documento ${d.documentNumber}`
                        : "Detalle",
                },
            ]),
        [d.documentNumber]
    );

    const st = statusFromCode(d.status);
    const amountNum = Number(d.amount);
    const amountFmt =
        d.amount !== "" && Number.isFinite(amountNum)
            ? formatAmount(amountNum)
            : "N/D";

    const hasStamped =
        Boolean(d.stampedRebateUuid) ||
        Boolean(d.stampedDocumentNumber) ||
        Boolean(d.stampedReferenceNumber);

    const content = !hasPayload ? (
        <div style={styles.container}>
            <div style={styles.header}>
                <div style={styles.title}>Detalle de descuento</div>
            </div>
            <div style={styles.emptyBox}>
                No hay parámetros de consulta. Vuelve al listado y abre el detalle
                desde el ícono de ver (<strong>ojo</strong>) en una fila con datos.
            </div>
        </div>
    ) : (
        <div style={styles.container}>
            <div style={styles.header}>
                <div style={styles.title}>Detalle de descuento comercial</div>
                <div style={styles.subtitle}>
                    Información relacionada al documento{" "}
                    <strong>{docLabel}</strong>
                </div>
            </div>

            <div style={styles.sectionTitle}>Datos generales</div>
            <div style={styles.grid}>
                <Field label="Número Documento">{fmt(d.documentNumber)}</Field>
                
                <Field label="Documento Sap">{fmt(d.sapDocument)}</Field>
                <Field label="Estatus">
                    {d.status !== "" ? (
                        <StatusPill type={st.type}>{st.label}</StatusPill>
                    ) : (
                        "N/D"
                    )}
                </Field>
                <Field label="Tipo Rebate">
                    {d.tipoRebate ?? "N/D"}
                </Field>
                <Field label="Período">{fmt(d.periodId)}</Field>
                <Field label="Importe">{amountFmt}</Field>
                <Field label="Fecha Vencimiento">
                    {d.dueDate ? formatDate(d.dueDate) : "N/D"}
                </Field>
                
                <Field label="Fecha Alta">{formatDateTime(d.createdAt, { seconds: true })}</Field>
                <Field label="Número Proveedor">{fmt(d.supplierNumber)}</Field>
                <Field label="Nombre Proveedor">{fmt(d.vendorName)}</Field>
                <Field label="Referencia">{fmt(d.documentReference)}</Field>
                <Field label="Fecha Aplicación">
                    {d.postingDate ? formatDate(d.postingDate) : "N/D"}
                </Field>
            </div>

            {hasStamped ? (
                <>
                    <div style={styles.sectionTitle}>Timbrado</div>
                    <div style={styles.grid}>
                        <Field label="UUID NC">
                            {fmt(d.stampedRebateUuid)}
                        </Field>
                        <Field label="Monto NC">{amountFmt}</Field>
                        <Field label="UUID Factura">
                            {fmt(d.stampedInvoiceFiscalUuid)}
                        </Field>
                        <Field label="Monto">{amountFmt}</Field>
                        <Field label="Acciones">
                            <button style={styles.actionButton} onClick={() => {
                                const fiscalParams = new URLSearchParams({
                                    uuid: String(d.stampedRebateUuid ?? ""),
                                    start: d.postingDate,
                                    end: d.postingDate,
                                });
                                window.location.href = buildFiscalSpaUrl(
                                    "notas-credito",
                                    fiscalParams
                                )
                            }}>
                                <img src={eyeIconUrl} alt="Ver NC" width={20} height={20} />
                            </button>
                        </Field>
                    </div>
                </>
            ) : null}
        </div>
    );

    return (
        <>
            {decorate(breadcrumb, LIST_PATH, content, false, undefined, {
                financeListSession: FINANCE_LIST_KEYS.discounts,
            })}
        </>
    );
}
