import { ReactElement } from "react";
import { useNavigate } from "react-router-dom";

import { GenericTable } from "@shared/components/ui";

import eyeIconUrl from "@assets/eye-show.svg";
import plusIconUrl from "@assets/icons/plus.svg";

import { capitalizeWord, formatDate } from "@/utils/utils";
import { buildFiscalSpaUrl } from "@/utils/fiscalSpaUrl";
import { canRelatePaymentComplement, resolvePaymentStatusDisplay } from "../paymentStatusDisplay";
import type { PaymentRecord } from "../interfaces";
import { StatusPill } from "@/shared/components/ui/statusPill/StatusPill";
import type { PaymentFiltersValues } from "./FiltersBar";

import { APP_EVENT, PermissionGate } from "@shared/security";
import "../styles/PaymentsResultsTable.css";

interface ResultsTableProps {
    rows: PaymentRecord[];
    providers?: any[];
    loading?: boolean;
    isAdmin?: boolean;
    page: number;
    perPage: number;
    totalPages: number;
    totalItems: number;
    onPageChange: (page: number) => void;
    onPerPageChange: (perPage: number) => void;
    onExport?: () => void;
    backPath?: string;
    lastFilters?: PaymentFiltersValues | null;
}

export default function ResultsTable({
    rows = [],
    providers = [],
    loading = false,
    page,
    perPage,
    totalPages,
    totalItems,
    onPageChange,
    onPerPageChange,
    lastFilters = null,
}: ResultsTableProps): ReactElement {
    const nav = useNavigate();

    const findProvider = (providerNumber: string) =>
        providers.find(
            (item) => String(item.supplierNumber) === String(providerNumber)
        );

    const handleViewDetail = (row: PaymentRecord) => {
        const params = new URLSearchParams({
            ref: row.documentReference,
            provider: row.providerNumber,
            year: row.paymentYear,
            headerUuid: row.paymentHeaderUuid ?? "",
        });

        nav(`/finanzas/pagos/detalle?${params.toString()}`, {
            state: { payment: row, filters: lastFilters },
        });
    };

    const canAddComplement = (row: PaymentRecord): boolean =>
        canRelatePaymentComplement(row);

    const columns = [
        { header: "Referencia Pago", render: (r: PaymentRecord) => r.documentReference ?? "--" },
        {
            header: "Importe",
            render: (r: PaymentRecord) =>
                `$${r.amount?.toLocaleString("es-MX", { minimumFractionDigits: 2 })}`,
            align: "right" as const,
        },
        { header: "Moneda", render: (r: PaymentRecord) => r.currency ?? "--" },
        { header: "Año Pago", render: (r: PaymentRecord) => r.paymentYear ?? "--" },
        { header: "Fecha Pago", render: (r: PaymentRecord) => formatDate(r.paymentDate) ?? "--" },
        { header: "Número Proveedor", render: (r: PaymentRecord) => r.providerNumber ?? "--" },
        {
            header: "Nombre Proveedor",
            render: (r: PaymentRecord) =>
                findProvider(r.providerNumber)?.businessName ??
                r.providerName ?? "--",
        },
        {
            header: "Tipo Proveedor",
            render: (r: PaymentRecord) => {
                const code = findProvider(r.providerNumber)?.supplierType?.code;
                return code ? capitalizeWord(code) : "--";
            },
        },
        { header: "Fecha Registro", render: (r: PaymentRecord) => r.createdAt ?? "--" },
        { header: "Fecha Actualización", render: (r: PaymentRecord) => r.updatedAt ?? "--" },
        {
            header: "Estatus",
            render: (r: PaymentRecord) => {
                const st = resolvePaymentStatusDisplay(r.statusId);
                return <StatusPill type={st.type}>{st.label}</StatusPill>;
            },
        },
        {
            header: "Acción",
            align: "center" as const,
            render: (r: PaymentRecord) => {
                const disabledAdd = !canAddComplement(r);

                const params = new URLSearchParams({
                    ref: r.documentReference ?? "",
                    provider: r.providerNumber ?? "",
                    currency: r.currency ?? "",
                    amount: String(r.amount ?? 0),
                    paymentDate: r.paymentDate ?? "",
                    year: r.paymentYear ?? "",
                });

                return (
                    <div className="pay-actions">
                        <PermissionGate appEvent={APP_EVENT.PAYMENTS.VIEW_DETAIL}>
                            <button
                                title="Ver detalle"
                                onClick={() => handleViewDetail(r)}
                                className="pay-action-btn"
                                type="button"
                            >
                                <img src={eyeIconUrl} alt="Ver" className="pay-action-icon" />
                            </button>
                        </PermissionGate>

                        <PermissionGate appEvent={APP_EVENT.PAYMENT_COMPLEMENTS.PUBLISH}>
                            <button
                                title={
                                    disabledAdd
                                        ? "No disponible (solo pagos pendientes de complemento)"
                                        : "Agregar complemento de pago"
                                }
                                onClick={() =>
                                    !disabledAdd &&
                                    (window.location.href = buildFiscalSpaUrl(
                                        "publicar-complemento",
                                        params
                                    ))
                                }
                                className="pay-action-btn"
                                type="button"
                                disabled={disabledAdd}
                                style={{
                                    cursor: disabledAdd ? "not-allowed" : "pointer",
                                    opacity: disabledAdd ? 0.4 : 1,
                                }}
                            >
                                <img src={plusIconUrl} alt="Agregar" className="pay-action-icon" />
                            </button>
                        </PermissionGate>
                    </div>
                );
            },
        },
    ];

    return (
        <div className="pay-results">
            <GenericTable<PaymentRecord>
                rows={rows}
                columns={columns}
                actions={[]}
                emptyLabel={
                    loading
                        ? "Cargando..."
                        : "No se encontraron pagos con los criterios establecidos"
                }
                perPage={perPage}
                page={page}
                totalPages={totalPages}
                totalItems={totalItems}
                onChangePerPage={onPerPageChange}
                onChangePage={onPageChange}
            />
        </div>
    );
}
