import { useEffect, useMemo, useState } from "react";
import type { ReactElement } from "react";
import { useSearchParams } from "react-router-dom";

import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import type { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { withFinanceBreadcrumb } from "@/shared/components/ui/navigation/financeBreadcrumb";
import { StatusPill } from "@/shared/components/ui/statusPill/StatusPill";
import GenericTable from "@/shared/components/ui/table/GenericTable";
import type { Column } from "@/shared/components/ui/table/GenericTable";
import RowActionsMenu from "@/shared/components/ui/table/RowActionsMenu";
import { FINANCE_LIST_KEYS } from "@/shared/hooks";
import { formatDate, formatDateTime } from "@/utils/utils";

import { parseRebateDetailFromSearchParams } from "../utils/rebateDetailQuery";
import eyeIconUrl from "@assets/eye-show.svg";

import RebatePurchaseOrders from "./RebatePurchaseOrders";
import { api, LIST_PATH, styles, fmt, money, statusFromCode, Field, creditNoteHref } from "./rebateDetailView.helpers";
import type { CreditNote, FiscalDetail } from "./rebateDetailView.helpers";

export default function RebateDetailView(): ReactElement {
    const [searchParams] = useSearchParams();

    const d = useMemo(
        () => parseRebateDetailFromSearchParams(searchParams),
        [searchParams]
    );

    const [detail, setDetail] = useState<FiscalDetail | null>(null);
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

            <RebatePurchaseOrders
                rebateId={d.rebateId}
                tipoRebate={d.tipoRebate}
                documentNumber={d.documentNumber}
            />
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
