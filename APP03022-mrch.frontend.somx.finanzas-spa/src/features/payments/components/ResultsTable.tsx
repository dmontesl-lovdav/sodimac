import { ReactElement } from "react";
import { useNavigate } from "react-router-dom";

import { GenericTable } from "@shared/components/ui";

import eyeIconUrl from "@assets/eye-show.svg";
import plusIconUrl from "@assets/icons/plus.svg";

import type { PaymentRecord } from "../interfaces";
import type { PaymentFiltersValues } from "./FiltersBar";

import "../styles/PaymentsResultsTable.css";

interface ResultsTableProps {
    rows: PaymentRecord[];
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
    const fiscal_app =  process.env.FISCAL_SPA_URL || "";
    const formatPaymentDate = (date: string): string => {
        const match = date?.match(/^(\d{1,2})([/-])(\d{1,2})\2(\d{4})$/);
        if (!match) return date;

        const day = match[1];
        const separator = match[2];
        const month = match[3].padStart(2, "0");
        const year = match[4];

        return `${day}${separator}${month}${separator}${year}`;
    };

    const handleViewDetail = (row: PaymentRecord) => {
        const params = new URLSearchParams({
            ref: row.documentReference,
            provider: row.providerNumber,
            year: row.paymentYear,
            headerUuid: row.paymentHeaderUuid || "",
        });

        nav(`/finanzas/pagos/detalle?${params.toString()}`, {
            state: { payment: row, filters: lastFilters },
        });
    };


    const canAddComplement = (row: PaymentRecord): boolean => {
        return row.statusId === 0;
    };

    const columns = [
        { header: "Referencia de pago", render: (r: PaymentRecord) => r.documentReference || "--" },
        { header: "Número proveedor", render: (r: PaymentRecord) => r.providerNumber || "--" },
        { header: "Nombre proveedor", render: (r: PaymentRecord) => r.providerName || "--" },
        { header: "Moneda", render: (r: PaymentRecord) => r.currency || "--" },
        {
            header: "Importe",
            render: (r: PaymentRecord) =>
                `$${r.amount?.toLocaleString("es-MX", { minimumFractionDigits: 2 })}`,
            align: "right" as const,
        },
        { header: "Fecha de pago", render: (r: PaymentRecord) => formatPaymentDate(r.paymentDate) || "--" },
        { header: "Año de pago", render: (r: PaymentRecord) => r.paymentYear || "--" },
        { header: "Estatus", render: (r: PaymentRecord) => r.status || "--" },
        { header: "Fecha de registro", render: (r: PaymentRecord) => r.createdAt || "--" },
        { header: "Fecha de actualización", render: (r: PaymentRecord) => r.updatedAt || "--" },
        {
            header: "Acción",
            align: "center" as const,
            render: (r: PaymentRecord) => {
                const disabledAdd = !canAddComplement(r);

                const params = new URLSearchParams({
                    ref: r.documentReference || "",
                    provider: r.providerNumber || "",
                    currency: r.currency || "",
                    amount: String(r.amount ?? 0),
                    paymentDate: r.paymentDate || "",
                    year: r.paymentYear || "",
                });

                return (
                    <div className="pay-actions">
                        <button
                            title="Ver detalle"
                            onClick={() => handleViewDetail(r)}
                            className="pay-action-btn"
                            type="button"
                        >
                            <img src={eyeIconUrl} alt="Ver" className="pay-action-icon" />
                        </button>

                        <button
                            title={
                                disabledAdd
                                    ? "No disponible (solo pagos pendientes de complemento)"
                                    : "Agregar complemento de pago"
                            }
                            onClick={() => !disabledAdd && (window.location.href = `${fiscal_app}/publicar-complemento?${params.toString()}`)}
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
                emptyLabel={loading ? "Cargando..." : "No se encontraron pagos con los criterios establecidos"}
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