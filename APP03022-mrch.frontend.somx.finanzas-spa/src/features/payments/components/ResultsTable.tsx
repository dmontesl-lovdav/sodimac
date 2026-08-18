import { ReactElement, useMemo } from "react";
import { useNavigate } from "react-router-dom";

import { GenericTable } from "@shared/components/ui";
import type { RowAction } from "@/shared/components/ui/table/GenericTable";

import eyeIconUrl from "@assets/eye-show.svg";
import plusIconUrl from "@assets/icons/plus.svg";

import { capitalizeWord, formatDate } from "@/utils/utils";
import { buildFiscalSpaUrl } from "@/utils/fiscalSpaUrl";
import {
    canRelatePaymentComplement,
    resolvePaymentStatusDisplay,
} from "../paymentStatusDisplay";
import type { PaymentRecord } from "../interfaces";
import { StatusPill } from "@/shared/components/ui/statusPill/StatusPill";
import type { PaymentFiltersValues } from "./FiltersBar";

import { APP_EVENT, useSecurityContext } from "@shared/security";
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
    const { can } = useSecurityContext();

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
            state: {
                payment: row,
                filters: lastFilters,
            },
        });
    };

    /*
     * Orden solicitado para la pantalla principal:
     * Referencia Pago, Año Pago, Fecha Pago, Importe, Moneda,
     * Tipo Proveedor, Número Proveedor, Nombre Proveedor,
     * Fecha Registro, Fecha Actualización, Estatus y Acción.
     */
    const columns = [
        {
            header: "Referencia Pago",
            render: (r: PaymentRecord) => r.documentReference ?? "--",
        },
        {
            header: "Año Pago",
            render: (r: PaymentRecord) => r.paymentYear ?? "--",
        },
        {
            header: "Importe",
            render: (r: PaymentRecord) =>
                `$${r.amount?.toLocaleString("es-MX", {
                    minimumFractionDigits: 2,
                })}`,
            align: "right" as const,
        },
        {
            header: "Moneda",
            render: (r: PaymentRecord) => r.currency ?? "--",
        },
        {
            header: "Tipo Proveedor",
            render: (r: PaymentRecord) => {
                const code = findProvider(r.providerNumber)?.supplierType?.code;
                return code ? capitalizeWord(code) : "--";
            },
        },
        {
            header: "Número Proveedor",
            render: (r: PaymentRecord) => r.providerNumber ?? "--",
        },
        {
            header: "Nombre Proveedor",
            render: (r: PaymentRecord) =>
                findProvider(r.providerNumber)?.businessName ??
                r.providerName ??
                "--",
        },
        {
            header: "Fecha Registro",
            render: (r: PaymentRecord) => r.createdAt ?? "--",
        },
        {
            header: "Estatus",
            render: (r: PaymentRecord) => {
                const status = resolvePaymentStatusDisplay(r.statusId);
                return (
                    <StatusPill type={status.type}>{status.label}</StatusPill>
                );
            },
        },
    ];

    const rowActionDescriptors = useMemo(
        () => [
            {
                gate: APP_EVENT.PAYMENTS.VIEW_DETAIL,
                action: {
                    title: "Ver detalle",
                    icon: eyeIconUrl,
                    onClick: (r: PaymentRecord) => handleViewDetail(r),
                } satisfies RowAction<PaymentRecord>,
            },
            {
                gate: APP_EVENT.PAYMENT_COMPLEMENTS.PUBLISH,
                action: {
                    title: "Agregar complemento de pago",
                    icon: plusIconUrl,
                    onClick: (r: PaymentRecord) => {
                        const params = new URLSearchParams({
                            ref: r.documentReference ?? "",
                            provider: r.providerNumber ?? "",
                            currency: r.currency ?? "",
                            amount: String(r.amount ?? 0),
                            paymentDate: r.paymentDate ?? "",
                            year: r.paymentYear ?? "",
                            uuid: r.paymentHeaderUuid ?? "",
                            status: r.status ?? "",
                        });
                        window.location.href = buildFiscalSpaUrl(
                            "publicar-complemento",
                            params
                        );
                    },
                    isDisabled: (r: PaymentRecord) => !canRelatePaymentComplement(r),
                } satisfies RowAction<PaymentRecord>,
            },
        ],
        [lastFilters]
    );

    const actions: RowAction<PaymentRecord>[] = rowActionDescriptors
        .filter(({ gate }) => can(gate))
        .map(({ action }) => action);

    return (
        <div className="pay-results">
            <GenericTable<PaymentRecord>
                rows={rows}
                columns={columns}
                actions={actions}
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
