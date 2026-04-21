import { GenericTable } from "@shared/components/ui";
import { useNavigate } from "react-router-dom";
import type { Reception } from "../interfaces";
import { formatDate, formatAmount } from "@/utils/utils";
import { StatusPill } from "@/shared/components/ui/statusPill/StatusPill";

import eyeIconUrl from "@assets/eye-show.svg";
import editIconUrl from "@assets/edit.svg";
import invoiceIconUrl from "@assets/icons/document.svg";
import creditsIconUrl from "@assets/xml.svg";
import "./ReceptionGridTable.css";

import { ReceptionStatusOptions } from "../interfaces";

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

const renderStatus = (status: number) => {
    const selected = ReceptionStatusOptions.filter((item) => item.value == status);
    if (selected.length === 1) return selected[0];
    return { value: -1, type: "error", label: "Desconocido" };
};

const mergeShippingNumbers = (shippings: any[]): string => {
    if (!shippings || shippings.length === 0) return "--";
    return shippings
        .map((s) => s?.shippingGuide?.guideNumber ?? s?.guideNumber ?? "")
        .filter(Boolean)
        .join(", ");
};

const getAdendumReferences = (reception: Reception) => {
    if (!reception.listAddendum || reception.listAddendum.length === 0) return null;
    return reception.listAddendum[0].invoice;
};

export default function ReceptionGridTable({ rows, ...props }: Props) {
    const nav = useNavigate();

    const columns = [
        { header: "Recepción", render: (r: Reception) => r.receptionId || "--" },
        {
            header: "Orden",
            render: (r: Reception) => r.order?.orderNumber ?? r.orderNumber ?? "--",
        },
        {
            header: "Guía",
            render: (r: Reception) => mergeShippingNumbers(r.shippingGuidePurchaseOrders),
        },
        { header: "Origen", render: (r: Reception) => r.originId ?? "--" },
        {
            header: "Fecha Recepción",
            render: (r: Reception) => (r.receptionDate ? formatDate(r.receptionDate) : "N/D"),
        },
        {
            header: "Fecha Registro Documento",
            render: (r: Reception) => (r.createdAt ? formatDate(r.createdAt) : "--"),
        },
        {
            header: "Importe",
            render: (r: Reception) => formatAmount(r.amount),
        },
        {
            header: "Estatus",
            render: (r: Reception) => (
                <StatusPill type={renderStatus(r.status).type}>
                    {renderStatus(r.status).label}
                </StatusPill>
            ),
        },
        {
            header: "Id Proveedor",
            render: (r: Reception) => r.order?.supplierNumber ?? r.supplierNumber ?? "--",
        },
        {
            header: "Nombre Proveedor",
            render: (r: Reception) => r.supplier?.businessName || "--",
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
            render: (r: Reception) => getAdendumReferences(r)?.invoiceUuid ?? "--",
        },
        {
            header: "Acción",
            render: (r: Reception) => {
                const addendum = getAdendumReferences(r);
                const disabledEdit = r.status != 0;
                const disabledInvoice = r.status != 0 || !!addendum;
                const disabledCredits = !addendum;

                return (
                    <div className="rc-actions">
                        <button
                            title="Ver Detalles"
                            onClick={() => nav(`/recepciones/${r.receptionId}`)}
                            className="rc-action-btn"
                            type="button"
                        >
                            <img src={eyeIconUrl} alt="Ver" className="rc-action-icon" />
                        </button>

                        <button
                            title="Editar Recepción"
                            onClick={() => nav(`/recepciones/${r.receptionId}/editar`)}
                            disabled={disabledEdit}
                            className="rc-action-btn"
                            type="button"
                        >
                            <img src={editIconUrl} alt="Editar" className="rc-action-icon" />
                        </button>

                        <button
                            title="Facturación"
                            onClick={() => nav(`/recepciones/${r.receptionId}/factura`)}
                            className="rc-action-btn"
                            disabled={disabledInvoice}
                            type="button"
                        >
                            <img src={invoiceIconUrl} alt="Facturación" className="rc-action-icon" />
                        </button>

                        <button
                            title="Ver notas de crédito"
                            onClick={() => nav(`/recepciones/${r.receptionId}/notas-credito`)}
                            className="rc-action-btn"
                            disabled={disabledCredits}
                            type="button"
                        >
                            <img src={creditsIconUrl} alt="Notas de crédito" className="rc-action-icon" />
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