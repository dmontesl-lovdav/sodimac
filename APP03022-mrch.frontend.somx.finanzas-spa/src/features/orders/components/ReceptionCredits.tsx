import { GenericInput, GenericSelect } from "@/shared/components/ui";
import { GenericButton } from "@/shared/components/ui/button";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";

import { ChangeEvent, ReactElement, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { OrderClient } from "../api/OrderClient";
import { EMPTY_RECEPTION, ReceptionStatusOptions, Reception, Addendum } from "../interfaces";
import ReceptionHeader from "./parts/ReceptionHeader";
import ReceptionSkusTable from "./parts/ReceptionSkusTable";
import ErrorMessage from "@/shared/components/ui/alerts/ErrorMessage";
import { InvoiceClient } from "../api/InvoiceClient";
import DataGrid, { DataGridColumn } from "@/shared/components/ui/datagrid/DataGrid";
import { StatusPill } from "@/shared/components/ui/statusPill/StatusPill";
import { formatDate, formatAmount, getStandardFilename, downloadXML } from "@/utils/utils";
import xmlIconUrl from "@/assets/xml.svg";
import "./ReceptionCredits.css";

const styles = {
    container: {
        marginTop: '1.25rem',
        display: 'grid',
        gridTemplateColumns: '1fr',
    },
    card: {
        padding: '1.25rem',
        border: '1px solid #e5e7eb',
    },
    row: {
        display: 'flex',
    },
    label: {
        fontWeight: 700,
    },
};

const buildDetail = (reception: Reception) => {
    // ❌ NO new
    const invoice = InvoiceClient;

    const columns: DataGridColumn<Addendum>[] = [
        { header: "Recepción", accessor: r => r.receptionNumber, exportAccessor: r => r.receptionNumber },
        { header: "UUID", accessor: r => r.invoiceUuid, exportAccessor: r => r.invoiceUuid },
        { header: "Fecha Registro", accessor: r => r.createdAt ? formatDate(r.createdAt) : "N/D", exportAccessor: r => r.createdAt },
        { header: "Importe", accessor: r => formatAmount(r.invoice.total), exportHeader: "Monto", exportAccessor: r => r.invoice.total },
        { header: "Serie", accessor: r => r.invoice.series, exportAccessor: r => r.invoice.series },
        { header: "Folio", accessor: r => r.invoice.folio ?? "--", exportAccessor: r => r.invoice.folio ?? "" },
        { header: "Tipo", accessor: r => r.invoice.documentType ?? "--", exportAccessor: r => r.invoice.documentType ?? "" },
        {
            header: "Acción",
            align: "center",
            render: (r) => (
                <div className="rc-action-wrap">
                    <button
                        title="Descargar XML"
                        onClick={async () => {
                            const data = await invoice.getXmlDocument(r.invoiceUuid);
                            const xmlString = await data.text();
                            downloadXML(xmlString, getStandardFilename(r.invoice) + ".xml");
                        }}
                        className="rc-action-btn"
                    >
                        <img src={xmlIconUrl} alt="Ver" className="rc-action-icon" />
                    </button>
                </div>
            ),
        },
    ];

    return (
        <div className="rc-credits-container">
            <div className="rc-credits-card">
                <div className="rc-credits-row">
                    <div className="rc-credits-title">Facturas y notas de crédito asignadas</div>
                </div>
                <div className="rc-credits-spacer" />
                <DataGrid<Addendum>
                    loading
                    rows={reception.listAddendum || []}
                    columns={columns}
                    getRowId={r => r.addendumUuid}
                    perPage={25}
                    page={1}
                    totalPages={1}
                    selectable
                    enableCsv
                    csvFilename={getStandardFilename(reception.listAddendum?.[0]?.invoice)}
                />
            </div>
        </div>
    );
};

export function ReceptionDetail(): ReactElement {
    const params = useParams();
    const [loading, setLoading] = useState(false);
    const [reception, setReception] = useState<Reception>(EMPTY_RECEPTION);

    useEffect(() => {
        const fetchData = async (uuid: string) => {
            setLoading(true);
            try {
                // ❌ NO new
                const response = await OrderClient.getReceptionByUuid(uuid);
                setReception(response.data);
            } finally {
                setLoading(false);
            }
        };

        if (params.uuid) {
            fetchData(params.uuid);
        }
    }, [params]);

    const breadcrumb: BreadcrumbItem[] = [
        { label: "Finanzas", to: "/finanzas" },
        { label: "Recepciones", to: "/finanzas/recepciones" },
        { label: `${reception.receptionNumber}` },
    ];

    return decorate(
        breadcrumb,
        "/finanzas/recepciones",
        <>
            <ReceptionHeader reception={reception} />
            {buildDetail(reception)}
        </>,
        loading
    );
}

export default ReceptionDetail;
