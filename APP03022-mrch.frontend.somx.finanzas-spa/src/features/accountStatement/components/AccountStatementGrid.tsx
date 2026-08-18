// ✅ FILE: src/features/account-statement/components/AccountStatementGrid.tsx
import type { ReactElement } from "react";
import { useMemo } from "react";
import { GenericTable } from "@shared/components/ui";
import type { RowAction } from "@/shared/components/ui/table/GenericTable";
import type { AccountStatementRecord } from "../interfaces";
import {
    canConfirmAccountStatement,
    canRejectAccountStatement,
} from "../accountStatementActions";

import eyeShowIcon from "@assets/eye-show.svg";
import requestConfirmIcon from "@assets/RequestConfirmIcon.svg";
import deleteIcon from "@assets/delete.svg";

import { formatDate, MONTHS } from "@/utils/utils";
import { APP_EVENT, useSecurityContext } from "@shared/security";

import "../styles/AccountStatementGrid.css";

interface AccountStatementGridProps {
    rows: AccountStatementRecord[];
    loading?: boolean;
    page: number;
    perPage: number;
    totalPages: number;
    totalItems: number;
    onPageChange: (page: number) => void;
    onPerPageChange: (perPage: number) => void;
    onView: (row: AccountStatementRecord) => void;
    onReview: (row: AccountStatementRecord) => void;
    onReject: (row: AccountStatementRecord) => void;
}

function monthLabel(mes: number): string {
    const found = MONTHS.find((m) => m.value === mes);
    return found ? found.label : String(mes);
}

export default function AccountStatementGrid({
    rows = [],
    loading = false,
    page,
    perPage,
    totalPages,
    totalItems,
    onPageChange,
    onPerPageChange,
    onView,
    onReview,
    onReject,
}: AccountStatementGridProps): ReactElement {
    const { can } = useSecurityContext();

    const columns = [
        {
            header: "Mes",
            align: "center" as const,
            render: (r: AccountStatementRecord) => monthLabel(r.month),
        },
        {
            header: "Año",
            align: "center" as const,
            render: (r: AccountStatementRecord) => r.year ?? "--",
        },
        {
            header: "Número Proveedor",
            render: (r: AccountStatementRecord) => r.vendorNumber ?? "--",
        },
        {
            header: "Nombre Proveedor",
            render: (r: AccountStatementRecord) => r.vendorName ?? "--",
        },
        {
            header: "Tipo Proveedor",
            render: (r: AccountStatementRecord) => {
                const code = r.supplierType?.code?.trim();
                if (!code) return "--";
                const lower = code.toLowerCase();
                return lower.charAt(0).toUpperCase() + lower.slice(1);
            },
        },
        {
            header: "Fecha Proceso",
            render: (r: AccountStatementRecord) =>
                r.processedAt ? formatDate(r.processedAt, true) : "--",
        },
        {
            header: "Fecha Revisión",
            render: (r: AccountStatementRecord) =>
                r.reviewedAt ? formatDate(r.reviewedAt, true) : "-",
        },
        {
            header: "Estatus",
            render: (r: AccountStatementRecord) =>
                r.status ? r.statusLabel : "--",
        },
    ];

    const rowActionDescriptors = useMemo(
        () => [
            {
                gate: APP_EVENT.ACCOUNT_STATEMENT.VIEW_DETAIL,
                action: {
                    title: "Ver",
                    icon: eyeShowIcon,
                    onClick: (r) => onView(r),
                } satisfies RowAction<AccountStatementRecord>,
            },
            {
                gate: APP_EVENT.ACCOUNT_STATEMENT.CONFIRM_REVIEW,
                action: {
                    title: "Confirmar revisión",
                    icon: requestConfirmIcon,
                    onClick: (r) => onReview(r),
                    isDisabled: (r) => !canConfirmAccountStatement(r),
                } satisfies RowAction<AccountStatementRecord>,
            },
            {
                gate: APP_EVENT.ACCOUNT_STATEMENT.REQUEST_REVIEW,
                action: {
                    title: "Rechazar",
                    icon: deleteIcon,
                    onClick: (r) => onReject(r),
                    isDisabled: (r) => !canRejectAccountStatement(r),
                } satisfies RowAction<AccountStatementRecord>,
            },
        ],
        [onView, onReview, onReject]
    );

    const actions: RowAction<AccountStatementRecord>[] = rowActionDescriptors
        .filter(({ gate }) => can(gate))
        .map(({ action }) => action);

    return (
        <div className="as-grid">
            <GenericTable<AccountStatementRecord>
                rows={rows}
                columns={columns}
                actions={actions}
                emptyLabel={loading ? "Cargando..." : "Sin resultados"}
                page={page}
                perPage={perPage}
                totalPages={totalPages}
                totalItems={totalItems}
                onChangePage={onPageChange}
                onChangePerPage={onPerPageChange}
            />
        </div>
    );
}
