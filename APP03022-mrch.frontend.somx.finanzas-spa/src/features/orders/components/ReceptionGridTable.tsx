import { GenericTable } from "@shared/components/ui";
import { useNavigate } from "react-router-dom";
import type { Reception } from "../interfaces";
import { formatDate, formatAmount, fetchCatalog, mapCatalogResponseToFilterOptions } from "@/utils/utils";
import { StatusPill } from "@/shared/components/ui/statusPill/StatusPill";
import { APP_EVENT, PermissionGate } from "@shared/security";

import eyeIconUrl from "@assets/eye-show.svg";
import editIconUrl from "@assets/edit.svg";
import invoiceIconUrl from "@assets/icons/document.svg";
import creditsIconUrl from "@assets/xml.svg";
import "./ReceptionGridTable.css";

import { resolveReceptionStatusDisplay } from "../receptionStatusDisplay";
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

const resolveColor=(color: string) => {
const colorMap: Record<string, string> = {
    "amarillo": "warning",
    "amarrillo": "warning",
    "verde": "success",
    "rojo": "error",
    "azul": "info",
    "morado": "info",
    "naranja": "warning",
    "gris": "info",
} as const;
return colorMap[color] ?? "info";
};

const getInvoiceDocumentKind = (reception: Reception) => {
    const inv = getAdendumReferences(reception);
    if (!inv) return "--";
    return inv.document_type ?? inv.documentType ?? "--";
};

export default function ReceptionGridTable({ rows, ...props }: Props) {
    const nav = useNavigate();
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
            header: "Guía",
            render: (r: Reception) => r.order?.shippingGuideNumber ?? "--",
        },
        {
            header: "Sucursal",
            align: "center" as const,
            render: (r: Reception) => r.destinationId ?? "--",
        },
        {
            header: "Número Proveedor",
            render: (r: Reception) => r.supplier?.supplierNumber ?? r.supplierNumber ?? "--",
        },
        {
            header: "Nombre Proveedor",
            render: (r: Reception) => r.supplier?.businessName ?? r.vendorName ?? r.order?.vendorName ?? "--",
        },
        {
            header: "Tipo Proveedor",
            render: (r: Reception) => resolveProviderTypeLabel(r),
        },
        {
            header: "Documento",
            render: (r: Reception) => getInvoiceDocumentKind(r),
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
                const status = statusCatalog.find(item => item.value == String(r.status));
                return status ? <StatusPill type={status.color}>{status.label}</StatusPill> : "--";
            },
        },
        {
            header: "Acción",
            render: (r: Reception) => {
                const disabledEdit = r.status != 0;
                const disabledInvoice = r.status != 0;
                const disabledCredits = r.status != 1  && r.status != 3 && r.status != 4 && r.status != 5 && r.status != 6;

                return (
                    <div className="rc-actions">
                        <PermissionGate appEvent={APP_EVENT.RECEPTIONS.VIEW_DETAIL}>
                            <button
                                title="Ver Detalles"
                                onClick={() => nav(`/finanzas/recepciones/${r.receptionId}`)}
                                className="rc-action-btn"
                                type="button"
                            >
                                <img src={eyeIconUrl} alt="Ver" className="rc-action-icon" />
                            </button>
                        </PermissionGate>

                        <PermissionGate appEvent={APP_EVENT.RECEPTIONS.EDIT_RECEPTION}>
                            <button
                                title="Editar Recepción"
                                onClick={() => nav(`/finanzas/recepciones/${r.receptionId}/editar`)}
                                disabled={disabledEdit}
                                className="rc-action-btn"
                                type="button"
                            >
                                <img src={editIconUrl} alt="Editar" className="rc-action-icon" />
                            </button>
                        </PermissionGate>

                        <PermissionGate appEvent={APP_EVENT.RECEPTIONS.LINK_INVOICE}>
                            <button
                                title="Facturación"
                                onClick={() => nav(`/finanzas/recepciones/${r.receptionId}/factura`)}
                                className="rc-action-btn"
                                disabled={disabledInvoice}
                                type="button"
                            >
                                <img src={invoiceIconUrl} alt="Facturación" className="rc-action-icon" />
                            </button>
                        </PermissionGate>

                        <PermissionGate appEvent={APP_EVENT.RECEPTIONS.LINK_CREDIT_NOTE}>
                            <button
                                title="Ver Factura"
                                onClick={() => nav(`/finanzas/recepciones/${r.receptionId}/notas-credito`)}
                                className="rc-action-btn"
                                disabled={disabledCredits}
                                type="button"
                            >
                                <img src={creditsIconUrl} alt="Factura" className="rc-action-icon" />
                            </button>
                        </PermissionGate>
                    </div>
                );
            },
        },
    ];

    return (
        <div className="rc-list-grid-wrap">
            <GenericTable
                rows={rows}
                columns={columns}
                emptyLabel="Sin resultados"
                {...props}
            />
        </div>
    );
}