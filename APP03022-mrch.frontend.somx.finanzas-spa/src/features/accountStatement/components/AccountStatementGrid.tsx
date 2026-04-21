// ✅ FILE: src/features/account-statement/components/AccountStatementGrid.tsx
import type { ReactElement } from "react";
import { GenericTable } from "@shared/components/ui";
import type { AccountStatementRecord } from "../interfaces";

import eyeShowIcon from "@assets/eye-show.svg";
import requestConfirmIcon from "@assets/RequestConfirmIcon.svg";
import deleteIcon from "@assets/delete.svg";

import { formatDate, MONTHS } from "@/utils/utils";

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
    const columns = [
        {
            header: "Id Estado Cuenta",
            render: (r: AccountStatementRecord) => r.accountStatementUuid || "--",
        },
        {
            header: "Número Proveedor",
            render: (r: AccountStatementRecord) => r.vendorNumber || "--",
        },
        {
            header: "Nombre Proveedor",
            render: (r: AccountStatementRecord) => r.vendorName || "--",
        },
        {
            header: "Año",
            align: "center" as const,
            render: (r: AccountStatementRecord) => r.year ?? "--",
        },
        {
            header: "Mes",
            align: "center" as const,
            render: (r: AccountStatementRecord) => monthLabel(r.month),
        },
        {
            header: "Estatus",
            render: (r: AccountStatementRecord) => r.statusLabel || "--",
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
            header: "Acción",
            align: "center" as const,
            render: (r: AccountStatementRecord) => (
                <div className="as-actions">
                    <button
                        title="Ver"
                        onClick={() => onView(r)}
                        className="as-action-btn"
                        type="button"
                    >
                        <img src={eyeShowIcon} alt="Ver" className="as-action-icon" />
                    </button>

                    <button
                        title="Revisión"
                        onClick={() => onReview(r)}
                        className="as-action-btn"
                        type="button"
                    >
                        <img
                            src={requestConfirmIcon}
                            alt="Revisión"
                            className="as-action-icon"
                        />
                    </button>

                    <button
                        title="Rechazar"
                        onClick={() => onReject(r)}
                        className="as-action-btn"
                        type="button"
                    >
                        <img src={deleteIcon} alt="Rechazar" className="as-action-icon" />
                    </button>
                </div>
            ),
        },
    ];

    return (
        <div className="as-grid">
            <GenericTable<AccountStatementRecord>
                rows={rows}
                columns={columns}
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