import { GenericTable } from "@shared/components/ui";
import {
    formatDate,
    formatAmount,
    capitalizeWord,
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
import { APP_EVENT, PermissionGate } from "@shared/security";

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
    providers: any[];
}


export default function DiscountsGridTable({
    rows,
    renderStatus,
    rebateTypeOptions,
    providers,
    ...props
}: Props) {
    console.log(providers);


const returnProvider = (r: Rebate) => {
    console.log(r.vendorNumber);
    console.log(providers);
    return providers.find((item) => item.supplierNumber == r.vendorNumber);
}

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
                return returnProvider(r)?.businessName ?? "--";
            },
        },
        {
            header: "Tipo Proveedor",
            render: (r: Rebate) => {
                const code = returnProvider(r)?.supplierType?.code;
                return code ? capitalizeWord(code) : "--";
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
                    referenciaDocumento: r.documentReference || r.referenceNumber || "",
                    rebateId: String(r.rebateId ?? ""),
                    supplierNumber: String(vendorNum ?? ""),
                    documentNumber: String(r.documentNumber ?? ""),
                    documentReference: r.documentReference || r.referenceNumber || "",
                    sapDocument: String(r.sapDocument ?? ""),
                    postingDate: String(r.postingDate ?? ""),
                    dueDate: String(r.dueDate ?? ""),
                    amount: String(r.amount ?? ""),
                    periodId: String(r.periodId ?? ""),
                    tipoRebate: tipoLabel,
                    vendorName: returnProvider(r)?.businessName ?? "--",
                });
                if (r.stampedRebate?.invoiceFiscalUuid) {
                    fiscalParams.set("uuid", String(r.stampedRebate.invoiceFiscalUuid));
                }

                const detailParams = buildRebateDetailSearchParams(r);
                detailParams.set("tipoRebate", tipoLabel);

                return (
                    <div className="table-actions">
                        <PermissionGate appEvent={APP_EVENT.DISCOUNTS.VIEW_DETAIL}>
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
                        </PermissionGate>
                        <PermissionGate appEvent={APP_EVENT.DISCOUNTS.LINK_CREDIT_NOTE}>
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
                        </PermissionGate>
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
