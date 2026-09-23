import { useEffect, useMemo, useState } from "react";
import type { CSSProperties, ReactElement, ReactNode } from "react";
import { useSearchParams } from "react-router-dom";

import { createApiClient } from "@/services/ApiClient";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import type { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { withFinanceBreadcrumb } from "@/shared/components/ui/navigation/financeBreadcrumb";
import { StatusPill } from "@/shared/components/ui/statusPill/StatusPill";
import GenericTable from "@/shared/components/ui/table/GenericTable";
import type { Column } from "@/shared/components/ui/table/GenericTable";
import RowActionsMenu from "@/shared/components/ui/table/RowActionsMenu";
import { FINANCE_LIST_KEYS } from "@/shared/hooks";
import { formatAmount, formatDate, formatDateTime } from "@/utils/utils";
import { buildFiscalSpaUrl } from "@/utils/fiscalSpaUrl";

import { RebateStatusOptions } from "../interfaces";
import { parseRebateDetailFromSearchParams } from "../utils/rebateDetailQuery";
import eyeIconUrl from "@assets/eye-show.svg";

const LIST_PATH = "/finanzas/descuentos-comerciales";
const api = createApiClient();

interface CreditNote {
    id: string;
    uuid: string;
    registeredAt: string | null;
    amount: string | null;
    series: string | null;
    folio: string | null;
}

interface PurchaseOrderDetail {
    purchaseOrder: string;
    receivedAmount: string;
    receptionDate: string;
    discountType: number;
    discountAmount: string;
}
interface OrdersDetail {
    rebateId: string;
    orders: PurchaseOrderDetail[];
    message: string | null;
}

interface FiscalDetail {
    rebateId: string;
    vendorNumber: number | null;
    invoiceFiscalUuid: string | null;
    ncFiscalUuid: string | null;
    creditNotes: CreditNote[];
    message: string | null;
}

const styles: Record<string, CSSProperties> = {
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

function fmt(value: unknown): string {
    if (value === null || value === undefined || value === "") {
        return "N/D";
    }

    return String(value);
}

function money(value: unknown): string {
    if (value === null || value === undefined || value === "") {
        return "N/D";
    }

    const amount = Number(value);
    return Number.isFinite(amount) ? formatAmount(amount) : "N/D";
}

function statusFromCode(value: string): {
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

function creditNoteHref(
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

export default function RebateDetailView(): ReactElement {
    const [searchParams] = useSearchParams();

    const d = useMemo(
        () => parseRebateDetailFromSearchParams(searchParams),
        [searchParams]
    );

    const [detail, setDetail] = useState<FiscalDetail | null>(null);
    const [ordersDetail, setOrdersDetail] = useState<OrdersDetail | null>(null);
    const [ordersError, setOrdersError] = useState("");
    const [ordersLoading, setOrdersLoading] = useState(false);
    const [ordersPage, setOrdersPage] = useState(1);
    const [ordersPerPage, setOrdersPerPage] = useState(10);

    useEffect(() => {
        let cancelled = false;
        setOrdersDetail(null);
        setOrdersError("");
        setOrdersPage(1);
        setOrdersLoading(Boolean(d.rebateId));
        if (!d.rebateId) return;
        void (async () => {
            try {
                const response = await api.request<OrdersDetail | { data: OrdersDetail }>(
                    `rebates/${encodeURIComponent(d.rebateId)}/purchase-order-details`, "get"
                );
                const payload = response && "data" in response ? response.data : response;
                if (!payload || payload.rebateId !== d.rebateId || !Array.isArray(payload.orders)) {
                    throw new Error("Detalle de órdenes inválido");
                }
                if (!cancelled) setOrdersDetail(payload);
            } catch {
                if (!cancelled) setOrdersError("No fue posible consultar las órdenes de compra. Vuelve a abrir el detalle.");
            } finally {
                if (!cancelled) setOrdersLoading(false);
            }
        })();
        return () => { cancelled = true; };
    }, [d.rebateId]);

    const orderColumns = useMemo<Column<PurchaseOrderDetail>[]>(() => [
        { header: "Orden de compra", render: row => row.purchaseOrder },
        { header: "Monto recibido", align: "right", render: row => money(row.receivedAmount) },
        {
            header: "Fecha de recepción", render: row => {
                // Fecha civil: no convertir medianoche UTC a la zona local.
                const parts = row.receptionDate?.split("-");
                return parts?.length === 3 ? `${parts[2]}/${parts[1]}/${parts[0]}` : "N/D";
            }
        },
        {
            header: "Tipo de descuento", render: row =>
                d.tipoRebate || `Tipo ${row.discountType}`
        },
        { header: "Monto de descuento", align: "right", render: row => money(row.discountAmount) },
    ], [d.originId, d.tipoRebate]);

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [page, setPage] = useState(1);
    const [perPage, setPerPage] = useState(10);

    useEffect(() => {
        let cancelled = false;

        setDetail(null);
        setError("");
        setPage(1);

        if (!d.rebateId) {
            setLoading(false);
            return;
        }

        setLoading(true);

        async function loadDetail() {
            try {
                const response = await api.request<
                    FiscalDetail | { data: FiscalDetail }
                >(
                    `rebates/${encodeURIComponent(d.rebateId)}/fiscal-detail`,
                    "get"
                );

                const payload =
                    response && "data" in response
                        ? response.data
                        : response;

                if (
                    !payload ||
                    payload.rebateId !== d.rebateId ||
                    !Array.isArray(payload.creditNotes)
                ) {
                    throw new Error("Respuesta de detalle fiscal inválida");
                }

                if (!cancelled) {
                    setDetail(payload);
                }
            } catch {
                if (!cancelled) {
                    setError(
                        "No fue posible consultar la nota de crédito relacionada. Vuelve a abrir el detalle."
                    );
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        }

        void loadDetail();

        return () => {
            cancelled = true;
        };
    }, [d.rebateId]);

    const currentDetail =
        detail?.rebateId === d.rebateId ? detail : null;

    const supplierNumber =
        currentDetail?.vendorNumber != null
            ? String(currentDetail.vendorNumber)
            : d.supplierNumber;

    const columns = useMemo<Column<CreditNote>[]>(
        () => [
            {
                header: "UUID",
                render: (row) => fmt(row.uuid),
            },
            {
                header: "Fecha Registro",
                render: (row) =>
                    row.registeredAt
                        ? formatDateTime(row.registeredAt, { seconds: true })
                        : "N/D",
            },
            {
                header: "Importe",
                align: "right",
                render: (row) => money(row.amount),
            },
            {
                header: "Serie",
                render: (row) => fmt(row.series),
            },
            {
                header: "Folio",
                render: (row) => fmt(row.folio),
            },
            {
                header: "Acción",
                align: "center",
                render: (row) =>
                    row.uuid ? (
                        <RowActionsMenu
                            items={[
                                {
                                    title: "Ver nota de crédito",
                                    icon: eyeIconUrl,
                                    onClick: () => {
                                        window.open(
                                            creditNoteHref(row, supplierNumber),
                                            "_blank",
                                            "noopener,noreferrer"
                                        );
                                    },
                                },
                            ]}
                        />
                    ) : (
                        "N/D"
                    ),
            },
        ],
        [supplierNumber]
    );

    const breadcrumb: BreadcrumbItem[] = useMemo(
        () =>
            withFinanceBreadcrumb([
                {
                    label: "Descuentos Comerciales",
                    to: LIST_PATH,
                },
                {
                    label: d.documentNumber
                        ? `Documento ${d.documentNumber}`
                        : "Detalle",
                },
            ]),
        [d.documentNumber]
    );

    const hasPayload =
        Boolean(d.documentNumber) ||
        Boolean(d.rebateId) ||
        Boolean(d.supplierNumber);

    const status = statusFromCode(d.status);
    const docLabel = d.documentNumber || d.rebateId || "—";

    const emptyLabel = loading
        ? "Cargando nota de crédito..."
        : !d.rebateId
            ? "Vuelve al listado y abre el detalle para consultar la nota de crédito."
            : currentDetail?.message ||
            "Sin notas de crédito relacionadas";

    const content = !hasPayload ? (
        <div style={styles.container}>
            <div style={styles.title}>Detalle de descuento</div>
            <div style={styles.emptyBox}>
                Vuelve al listado y abre el detalle de un descuento comercial.
            </div>
        </div>
    ) : (
        <div style={styles.container}>
            <div style={styles.header}>
                <div style={styles.title}>
                    Detalle de descuento comercial
                </div>
                <div style={styles.subtitle}>
                    Información relacionada al documento{" "}
                    <strong>{docLabel}</strong>
                </div>
            </div>

            <div style={styles.sectionTitle}>Datos generales</div>

            <div style={styles.grid}>
                <Field label="Número Documento">
                    {fmt(d.documentNumber)}
                </Field>

                <Field label="Documento SAP">
                    {fmt(d.sapDocument)}
                </Field>

                <Field label="Estatus">
                    {d.status !== "" ? (
                        <StatusPill type={status.type}>
                            {status.label}
                        </StatusPill>
                    ) : (
                        "N/D"
                    )}
                </Field>

                <Field label="Tipo Rebate">
                    {fmt(d.tipoRebate)}
                </Field>

                <Field label="Período">
                    {fmt(d.periodId)}
                </Field>

                <Field label="Importe">
                    {money(d.amount)}
                </Field>

                <Field label="Fecha Vencimiento">
                    {d.dueDate ? formatDate(d.dueDate) : "N/D"}
                </Field>

                <Field label="Fecha Alta">
                    {d.createdAt
                        ? formatDateTime(d.createdAt, { seconds: true })
                        : "N/D"}
                </Field>

                <Field label="Número Proveedor">
                    {fmt(supplierNumber)}
                </Field>

                <Field label="Nombre Proveedor">
                    {fmt(d.vendorName)}
                </Field>

                <Field label="Referencia">
                    {fmt(d.documentReference)}
                </Field>

                <Field label="Fecha Aplicación">
                    {d.postingDate ? formatDate(d.postingDate) : "N/D"}
                </Field>
            </div>

            <div style={styles.sectionTitle}>
                Nota de crédito relacionada
            </div>

            {error ? (
                <div role="alert" style={styles.errorBox}>
                    {error}
                </div>
            ) : (
                <div style={styles.tableCard}>
                    <GenericTable<CreditNote>
                        rows={currentDetail?.creditNotes ?? []}
                        columns={columns}
                        emptyLabel={emptyLabel}
                        page={page}
                        perPage={perPage}
                        onChangePage={setPage}
                        onChangePerPage={(value) => {
                            setPerPage(value);
                            setPage(1);
                        }}
                    />
                </div>
            )}

            <div style={styles.sectionTitle}>Órdenes de compra del descuento</div>
            {ordersError ? (
                <div role="alert" style={styles.errorBox}>{ordersError}</div>
            ) : (
                <div style={styles.tableCard}>
                    <GenericTable<PurchaseOrderDetail>
                        rows={ordersDetail?.rebateId === d.rebateId ? ordersDetail.orders : []}
                        columns={orderColumns}
                        emptyLabel={ordersLoading ? "Cargando órdenes de compra…" :
                            ordersDetail?.message || "Sin órdenes de compra relacionadas"}
                        page={ordersPage}
                        perPage={ordersPerPage}
                        onChangePage={setOrdersPage}
                        onChangePerPage={value => { setOrdersPerPage(value); setOrdersPage(1); }}
                    />
                </div>
            )}
        </div>
    );

    return (
        <>
            {decorate(
                breadcrumb,
                LIST_PATH,
                content,
                false,
                undefined,
                {
                    financeListSession: FINANCE_LIST_KEYS.discounts,
                }
            )}
        </>
    );
}