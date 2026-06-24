import { GenericTable } from "@shared/components/ui";
import {
    formatDate,
    formatAmount,
    SelectableOption,
} from "@/utils/utils";
import { StatusPill } from "@/shared/components/ui/statusPill/StatusPill";
import type { ProvidersOptions, Rebate } from "../interfaces";
import {
    getRebateVendorNumber,
} from "../interfaces";
import { REBATE_DETAIL_ROUTE } from "../constants";
import { buildRebateDetailSearchParams } from "../utils/rebateDetailQuery";

import eyeIconUrl from "@assets/eye-show.svg";
import plusIconUrl from "@assets/icons/plus.svg";

import { buildFiscalSpaUrl } from "@/utils/fiscalSpaUrl";

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
    rebateTypeOptions: SelectableOption<string>[];
    providers: ProvidersOptions[];
}


export default function DiscountsGridTable({
    rows,
    renderStatus,
    rebateTypeOptions,
    providers,
    ...props
}: Props) {

    const columns = [
        { header: "Documento", render: (r: Rebate) => r.documentNumber ?? "--" },
        {
            header: "Referencia",
            render: (r: Rebate) => r.documentReference ?? "--",
        },
        {
            header: "Tipo Rebate",
            render: (r: Rebate) => rebateTypeOptions.find((item) => item.value === String(r.source))?.label ?? "--",
        },
        { header: "Documento SAP", render: (r: Rebate) => r.sapDocument ?? "--" },
        { header: "Importe", render: (r: Rebate) => formatAmount(r.amount) },
        { header: "Período", render: (r: Rebate) => r.periodId ?? "--" },
        {
            header: "Número Proveedor",
            render: (r: Rebate) => r.vendorNumber ?? "--",
        },
        {
            header: "Nombre Proveedor",
            render: (r: Rebate) => {
                return providers.find((item) => item.value === String(r.vendorNumber))
                        ?.label.split("(")[0] ?? "--";
            },
        },
        {
            header: "Fecha Aplicación",
            render: (r: Rebate) => (r.postingDate ? formatDate(r.postingDate) : "N/D"),
        },
        {
            header: "Fecha Vencimiento",
            render: (r: Rebate) => (r.dueDate ? formatDate(r.dueDate) : "--"),
        },
        {
            header: "Estatus",
            render: (r: Rebate) => (
                <StatusPill type={renderStatus(r.status).type}>
                    {renderStatus(r.status).label}
                </StatusPill>
            ),
        },
        {
            header: "Acciones",
            align: "center" as const,
            render: (r: Rebate, nav: (path: string) => void) => {
                const vendorNum = getRebateVendorNumber(r);
                const disabled = false;
                const tipoLabel = rebateTypeOptions.find((item) => item.value === String(r.source))?.label ?? "--";

                const fiscalParams = new URLSearchParams({
                    numeroProveedor: String(vendorNum ?? ""),
                    numeroDocumento: String(r.documentNumber ?? ""),
                    referenciaDocumento: r.documentReference ?? "--",
                    rebateId: String(r.rebateId ?? ""),
                    supplierNumber: String(vendorNum ?? ""),
                    documentNumber: String(r.documentNumber ?? ""),
                    postingDate: String(r.postingDate ?? ""),
                    amount: String(r.amount ?? ""),
                    tipoRebate: tipoLabel,
                });
                if (r.stampedRebate?.invoiceFiscalUuid) {
                    fiscalParams.set("uuid", String(r.stampedRebate.invoiceFiscalUuid));
                }

                const detailParams = buildRebateDetailSearchParams(r);
                detailParams.set("tipoRebate", tipoLabel);

                return (
                    <div className="table-actions">
                        <button
                            title="Ver descuento relacionado"
                            type="button"
                            disabled={disabled}
                            onClick={() =>
                                !disabled &&
                                nav(
                                    `${REBATE_DETAIL_ROUTE}?${detailParams.toString()}`
                                )
                            }
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
                            onClick={() =>
                                !disabled &&
                                (window.location.href = buildFiscalSpaUrl(
                                    "publicar-nota-credito",
                                    fiscalParams
                                ))
                            }
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
