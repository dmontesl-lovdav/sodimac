import { useMemo } from "react";
import type { NavigateFunction } from "react-router-dom";
import { GenericTable } from "@shared/components/ui";
import type { RowAction } from "@/shared/components/ui/table/GenericTable";
import {
    formatDate,
    formatAmount,
    capitalizeWord,
    SelectableOption,
} from "@/utils/utils";
import { StatusPill } from "@/shared/components/ui/statusPill/StatusPill";
import type { Rebate, RebateFilters } from "../interfaces";
import { getRebateVendorNumber, StatusVerDescuentoComercial, StatusRelacionarNotaCredito } from "../interfaces";
import { REBATE_DETAIL_ROUTE } from "../constants";
import { buildRebateDetailSearchParams } from "../utils/rebateDetailQuery";

import eyeIconUrl from "@assets/eye-show.svg";
import plusIconUrl from "@assets/icons/plus.svg";

import { buildFiscalSpaUrl } from "@/utils/fiscalSpaUrl";
import { APP_EVENT, useSecurityContext } from "@shared/security";
import { saveDiscountSearchRestore } from "../utils/discountSearchRestore";

interface Props {
    lastSearch?: RebateFilters | null;
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
    lastSearch,
    renderStatus,
    rebateTypeOptions,
    providers,
    ...props
}: Props) {
    const { can } = useSecurityContext();

    const returnProvider = (r: Rebate) => {
        return providers.find((item) => item.supplierNumber == r.vendorNumber);
    };

    const getTipoLabel = (r: Rebate) =>
        rebateTypeOptions.find((item) => item.value === String(r.source))?.label ?? "--";

    const columns = [
        { header: "Documento", render: (r: Rebate) => r.documentNumber ?? "--" },
        {
            header: "Tipo Rebate",
            render: (r: Rebate) => getTipoLabel(r),
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
            render: (r: Rebate) => returnProvider(r)?.businessName ?? "--",
        },
        {
            header: "Tipo Proveedor",
            render: (r: Rebate) => {
                const code = returnProvider(r)?.supplierType?.code;
                return code ? capitalizeWord(code) : "--";
            },
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
    ];

    const rowActionDescriptors = useMemo(
        () =>
            [
                {
                    gate: APP_EVENT.DISCOUNTS.VIEW_DETAIL,
                    action: {
                        title: "Ver descuento relacionado",
                        icon: eyeIconUrl,
                        onClick: (r: Rebate, nav: NavigateFunction) => {
                            const detailParams = buildRebateDetailSearchParams(r);
                            detailParams.set("tipoRebate", getTipoLabel(r));
                            nav(`${REBATE_DETAIL_ROUTE}?${detailParams.toString()}`);
                        },
                        isDisabled: (r: Rebate) =>
                            !StatusVerDescuentoComercial.includes(r.status ?? 0),
                    } satisfies RowAction<Rebate>,
                },
                {
                    gate: APP_EVENT.DISCOUNTS.LINK_CREDIT_NOTE,
                    action: {
                        title: "Relacionar Nota de Crédito",
                        icon: plusIconUrl,
                        onClick: (r: Rebate) => {
                            const vendorNum = getRebateVendorNumber(r);
                            const tipoLabel = getTipoLabel(r);
                            const fiscalParams = new URLSearchParams({
                                numeroProveedor: String(vendorNum ?? ""),
                                numeroDocumento: String(r.documentNumber ?? ""),
                                referenciaDocumento: r.documentReference ?? r.referenceNumber ?? "",
                                rebateId: String(r.rebateId ?? ""),
                                supplierNumber: String(vendorNum ?? ""),
                                documentNumber: String(r.documentNumber ?? ""),
                                documentReference: r.documentReference ?? r.referenceNumber ?? "",
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
                            fiscalParams.set("restoreSearch", "1");
                            if (lastSearch) {
                                saveDiscountSearchRestore({
                                    ...lastSearch,
                                    pageNumber: props.page,
                                    pageSize: props.perPage,
                                });
                            }
                            window.location.href = buildFiscalSpaUrl(
                                "publicar-nota-credito",
                                fiscalParams
                            );
                        },
                        isDisabled: (r: Rebate) =>
                            !StatusRelacionarNotaCredito.includes(r.status ?? 0),
                    } satisfies RowAction<Rebate>,
                },
            ] as const,
        // providers / rebateTypeOptions used inside handlers via closure; keep deps aligned
        [providers, rebateTypeOptions, lastSearch, props.page, props.perPage]
    );

    const actions: RowAction<Rebate>[] = rowActionDescriptors
        .filter(({ gate }) => can(gate))
        .map(({ action }) => action);

    return (
        <GenericTable
            rows={rows}
            columns={columns}
            actions={actions}
            emptyLabel="Sin resultados"
            {...props}
        />
    );
}
