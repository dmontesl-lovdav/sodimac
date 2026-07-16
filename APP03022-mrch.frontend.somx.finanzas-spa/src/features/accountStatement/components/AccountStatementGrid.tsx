// ✅ FILE: src/features/account-statement/components/AccountStatementGrid.tsx
import type { ReactElement } from "react";
import { GenericTable } from "@shared/components/ui";
import type { AccountStatementRecord } from "../interfaces";
import {
    canConfirmAccountStatement,
    canRejectAccountStatement,
} from "../accountStatementActions";

import eyeShowIcon from "@assets/eye-show.svg";
import requestConfirmIcon from "@assets/RequestConfirmIcon.svg";
import deleteIcon from "@assets/delete.svg";

import { formatDate, MONTHS } from "@/utils/utils";
import { APP_EVENT, PermissionGate } from "@shared/security";

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

function enabledActionBtnClass(): string {
    return "as-action-btn";
}

function disabledActionBtnClass(): string {
    return "as-action-btn as-action-btn--disabled";
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
            render: (r: AccountStatementRecord) => r.vendorNumber || "--",
        },
        {
            header: "Nombre Proveedor",
            render: (r: AccountStatementRecord) => r.vendorName || "--",
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
        {
            header: "Acción",
            align: "center" as const,
            render: (r: AccountStatementRecord) => {
                const canConfirm = canConfirmAccountStatement(r);
                const canReject = canRejectAccountStatement(r);

                return (
                    <div className="as-actions">
                        <PermissionGate appEvent={APP_EVENT.ACCOUNT_STATEMENT.VIEW_DETAIL}>
                            <button
                                title="Ver"
                                onClick={() => onView(r)}
                                className="as-action-btn"
                                type="button"
                            >
                                <img src={eyeShowIcon} alt="Ver" className="as-action-icon" />
                            </button>
                        </PermissionGate>

                        <PermissionGate appEvent={APP_EVENT.ACCOUNT_STATEMENT.CONFIRM_REVIEW}>
                            <button
                                title={
                                    canConfirm
                                        ? "Confirmar revisión"
                                        : "No disponible para este estatus"
                                }
                                onClick={() => {
                                    if (canConfirm) onReview(r);
                                }}
                                className={
                                    canConfirm
                                        ? enabledActionBtnClass()
                                        : disabledActionBtnClass()
                                }
                                type="button"
                                disabled={!canConfirm}
                            >
                                <img
                                    src={requestConfirmIcon}
                                    alt="Confirmar revisión"
                                    className="as-action-icon"
                                />
                            </button>
                        </PermissionGate>

                        <PermissionGate appEvent={APP_EVENT.ACCOUNT_STATEMENT.REQUEST_REVIEW}>
                            <button
                                title={
                                    canReject
                                        ? "Rechazar"
                                        : "No disponible para este estatus"
                                }
                                onClick={() => {
                                    if (canReject) onReject(r);
                                }}
                                className={
                                    canReject
                                        ? enabledActionBtnClass()
                                        : disabledActionBtnClass()
                                }
                                type="button"
                                disabled={!canReject}
                            >
                                <img src={deleteIcon} alt="Rechazar" className="as-action-icon" />
                            </button>
                        </PermissionGate>
                    </div>
                );
            },
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
