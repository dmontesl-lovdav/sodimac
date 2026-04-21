import { GenericTable } from "@shared/components/ui";
import { formatDate, formatAmount } from "@/utils/utils";
import { StatusPill } from "@/shared/components/ui/statusPill/StatusPill";
import type { Rebate } from "../interfaces";

import eyeIconUrl from "@assets/eye-show.svg";
import plusIconUrl from "@assets/icons/plus.svg";

const fiscal_app =  process.env.FISCAL_SPA_URL || "";

interface Props {
    rows: Rebate[];
    page: number;
    perPage: number;
    totalPages: number;
    totalItems: number;
    loading: boolean;
    onChangePage: (page: number) => void;
    onChangePerPage: (size: number) => void;
    renderStatus: (status: number) => { type: string; label: string };
}

const REBATE_DETAIL_ROUTE = "/finanzas/descuentos-comerciales/detalle"; // TODO: ruta real detalle descuento

export default function DiscountsGridTable({
    rows,
    renderStatus,
    ...props
}: Props) {
    const columns = [
        { header: "Id Proveedor", render: (r: Rebate) => r.supplierNumber ?? "--" },
        { header: "Proveedor", render: (r: Rebate) => r.supplierNumber ?? "--" },
        { header: "Número de documento", render: (r: Rebate) => r.documentNumber ?? "--" },
        {
            header: "Referencia de documento",
            render: (r: Rebate) => r.documentReference ?? "--",
        },
        { header: "Tipo de rebate", render: (r: Rebate) => r.originId ?? "--" },
        { header: "Documento SAP", render: (r: Rebate) => r.sapDocument ?? "--" },
        {
            header: "Fecha Aplicación",
            render: (r: Rebate) => (r.postingDate ? formatDate(r.postingDate) : "N/D"),
        },
        {
            header: "Fecha Vencimiento",
            render: (r: Rebate) => (r.dueDate ? formatDate(r.dueDate) : "--"),
        },
        { header: "Importe", render: (r: Rebate) => formatAmount(r.amount) },
        { header: "Período", render: (r: Rebate) => r.periodId ?? "--" },
        {
            header: "Acciones",
            align: "center" as const,
            render: (r: Rebate, nav: (path: string) => void) => {
                const disabled = !r?.supplierNumber || !r?.documentNumber;

                const params = new URLSearchParams({
                    supplierNumber: String(r.supplierNumber ?? ""),
                    documentNumber: String(r.documentNumber ?? ""),
                    postingDate: String(r.postingDate ?? ""),
                    amount: String(r.amount ?? ""),
                });

                return (
                    <div className="table-actions">
                        <button
                        title="Ver descuento relacionado"
                        type="button"
                        disabled={disabled}
                        onClick={() => !disabled && nav(`${REBATE_DETAIL_ROUTE}?${params.toString()}`)}
                        style={{
                            cursor: disabled ? "not-allowed" : "pointer",
                            opacity: disabled ? 0.4 : 1,
                            background: "transparent",
                            border: "none",
                            padding: 0,
                        }}
                    >
                        <img src={eyeIconUrl} alt="Ver" width={20} height={20} />
                    </button>
                    <button
                        title="Relacionar Nota de Crédito"
                        type="button"
                        disabled={disabled}
                        onClick={() => !disabled && (window.location.href = `${fiscal_app}/publicar-nota-credito?${params.toString()}`)}
                        style={{
                            cursor: disabled ? "not-allowed" : "pointer",
                            opacity: disabled ? 0.4 : 1,
                            background: "transparent",
                            border: "none",
                            padding: 0,
                        }}
                    >
                        <img src={plusIconUrl} alt="Relacionar" width={20} height={20} />
                    </button>
                    </div>
                    
                );
            },
        },

        {
            header: "Estatus",
            render: (r: Rebate) => (
                <StatusPill type={renderStatus(r.status).type}>
                    {renderStatus(r.status).label}
                </StatusPill>
            ),
        },
    ];

    return (
        <GenericTable
            rows={rows}
            columns={columns}
            emptyLabel="Sin resultados"
            {...props}
        />
    );
}