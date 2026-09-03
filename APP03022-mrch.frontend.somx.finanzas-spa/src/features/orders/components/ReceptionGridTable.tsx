import { useMemo } from "react";
import { GenericTable } from "@shared/components/ui";
import type { RowAction } from "@/shared/components/ui/table/GenericTable";
import type { Reception } from "../interfaces";
import { formatDate, formatAmount, fetchCatalog, mapCatalogResponseToFilterOptions } from "@/utils/utils";
import { StatusPill } from "@/shared/components/ui/statusPill/StatusPill";
import { APP_EVENT, useSecurityContext } from "@shared/security";

import eyeIconUrl from "@assets/eye-show.svg";
import editIconUrl from "@assets/edit.svg";
import invoiceIconUrl from "@assets/icons/document.svg";
import creditsIconUrl from "@assets/xml.svg";
import "./ReceptionGridTable.css";

import { useEffect, useState } from "react";

interface Props {
    rows: Reception[];
    page: number;
    perPage: number;
    totalPages: number;
    totalItems: number;
    loading: boolean;
    onChangePage: (page: number) => void;
    onChangePerPage: (size: number) => void;
}

const getAdendumReferences = (reception: Reception) => {
    if (!reception.listAddendum || reception.listAddendum.length === 0) return null;
    return reception.listAddendum[0].invoice;
};

const getInvoiceUuid = (reception: Reception) => {
    const inv = getAdendumReferences(reception);
    return (
        inv?.fiscalUuid ??
        inv?.fiscal_uuid ??
        reception.invoiceUuid ??
        reception.order?.invoiceUuid ??
        inv?.invoiceUuid ??
        inv?.invoice_uuid ??
        "--"
    );
};

const resolveColor = (color: string) => {
    const colorMap: Record<string, string> = {
        amarillo: "warning",
        amarrillo: "warning",
        verde: "success",
        rojo: "error",
        azul: "info",
        morado: "info",
        naranja: "warning",
        gris: "info",
    } as const;
    return colorMap[color] ?? "info";
};

export default function ReceptionGridTable({ rows, ...props }: Props) {
    const { can } = useSecurityContext();
    const [providerTypeCatalog, setProviderTypeCatalog] = useState<
        { label: string; value: string }[]
    >([]);
    const [statusCatalog, setStatusCatalog] = useState<
        { label: string; value: string; color: string }[]
    >([]);

    useEffect(() => {
        (async () => {
            const catalog = await fetchCatalog("CatEstatusRecepcion");
            const statusRaw = catalog as { details?: unknown[] } | null;
            const mapped = statusRaw?.details?.map((item: any) => ({
                label: item.description,
                value: item.value,
                color: resolveColor(item.externalKey?.toLowerCase() ?? ""),
            }));
            setStatusCatalog(mapped ?? []);
        })();
    }, []);

    useEffect(() => {
        (async () => {
            const catalog = await fetchCatalog("CatTipoProveedor");
            const tipoRaw = catalog as { details?: unknown[] } | null;
            const rowsCatalog = Array.isArray(tipoRaw?.details)
                ? tipoRaw.details
                : catalog;
            const mapped = mapCatalogResponseToFilterOptions(rowsCatalog);
            if (mapped) {
                setProviderTypeCatalog(
                    mapped.filter((opt) => String(opt.value).trim() !== "")
                );
            }
        })();
    }, []);

    const resolveProviderTypeLabel = (reception: Reception): string => {
        const typeId = reception.supplier?.supplierType?.id;
        if (typeId == null) return "--";
        const hit = providerTypeCatalog.find(
            (item) => item.value === String(typeId)
        );
        return hit?.label ?? "--";
    };

    const columns = [
        { header: "Recepción", render: (r: Reception) => r.receptionNumber ?? r.receptionId ?? "--" },
        {
            header: "Orden Compra",
            render: (r: Reception) => r.order?.orderNumber ?? r.orderNumber ?? "--",
        },
        {
            header: "Número Proveedor",
            render: (r: Reception) => r.supplier?.supplierNumber ?? r.supplierNumber ?? "--",
        },
        {
            header: "Nombre Proveedor",
            render: (r: Reception) =>
                r.supplier?.businessName ?? r.vendorName ?? r.order?.vendorName ?? "--",
        },
        {
            header: "Tipo Proveedor",
            render: (r: Reception) => resolveProviderTypeLabel(r),
        },
        {
            header: "Importe",
            render: (r: Reception) => formatAmount(r.amount),
        },
        {
            header: "Serie",
            render: (r: Reception) => getAdendumReferences(r)?.series ?? "--",
        },
        {
            header: "Folio",
            render: (r: Reception) => getAdendumReferences(r)?.folio ?? "--",
        },
        {
            header: "UUID",
            render: (r: Reception) => getInvoiceUuid(r),
        },
        {
            header: "Fecha Recepción",
            render: (r: Reception) => (r.receptionDate ? formatDate(r.receptionDate) : "N/D"),
        },
        {
            header: "Fecha Registro",
            render: (r: Reception) => (r.createdAt ? formatDate(r.createdAt) : "--"),
        },
        {
            header: "Estatus",
            render: (r: Reception) => {
                const status = statusCatalog.find((item) => item.value == String(r.status));
                return status ? (
                    <StatusPill type={status.color}>{status.label}</StatusPill>
                ) : (
                    "--"
                );
            },
        },
    ];

    const rowActionDescriptors = useMemo(
        () => [
            {
                gate: APP_EVENT.RECEPTIONS.VIEW_DETAIL,
                action: {
                    title: "Ver Detalles",
                    icon: eyeIconUrl,
                    onClick: (r, nav) => nav(`/finanzas/recepciones/${r.receptionId}`),
                } satisfies RowAction<Reception>,
            },
            {
                gate: APP_EVENT.RECEPTIONS.EDIT_RECEPTION,
                action: {
                    title: "Editar Recepción",
                    icon: editIconUrl,
                    onClick: (r, nav) => nav(`/finanzas/recepciones/${r.receptionId}/editar`),
                    isDisabled: (r) => r.status != 0 && r.status != 7,
                } satisfies RowAction<Reception>,
            },
            {
                gate: APP_EVENT.RECEPTIONS.LINK_INVOICE,
                action: {
                    title: "Facturación",
                    icon: invoiceIconUrl,
                    onClick: (r, nav) => nav(`/finanzas/recepciones/${r.receptionId}/factura`),
                    isDisabled: (r) => r.status != 0,
                } satisfies RowAction<Reception>,
            },
            {
                gate: APP_EVENT.RECEPTIONS.LINK_CREDIT_NOTE,
                action: {
                    title: "Ver Factura",
                    icon: creditsIconUrl,
                    onClick: (r, nav) =>
                        nav(`/finanzas/recepciones/${r.receptionId}/notas-credito`),
                    isDisabled: (r) =>
                        r.status != 1 &&
                        r.status != 3 &&
                        r.status != 4 &&
                        r.status != 5 &&
                        r.status != 6,
                } satisfies RowAction<Reception>,
            },
        ],
        []
    );

    const actions: RowAction<Reception>[] = rowActionDescriptors
        //.filter(({ gate }) => can(gate))
        .map(({ action }) => action);

    return (
        <div className="rc-list-grid-wrap">
            <GenericTable
                rows={rows}
                columns={columns}
                actions={actions}
                emptyLabel="Sin resultados"
                {...props}
            />
        </div>
    );
}
