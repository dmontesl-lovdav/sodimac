import { GenericInput, GenericSelect, GenericModal, GenericTable } from "@/shared/components/ui";
import { GenericButton } from "@/shared/components/ui/button";
import { useFinanceAlertModal } from "@/shared/hooks/useFinanceAlertModal";
import { FINANCE_LIST_KEYS } from "@/shared/hooks";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { withFinanceBreadcrumb } from "@/shared/components/ui/navigation/financeBreadcrumb";

import { ChangeEvent, ReactElement, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { OrderClient } from "../api/OrderClient";
import { EMPTY_RECEPTION, ReceptionStatusOptions, Reception, Addendum, Invoice } from "../interfaces";
import ReceptionHeader from "./parts/ReceptionHeader";
import ReceptionSkusTable from "./parts/ReceptionSkusTable";
import ErrorMessage from "@/shared/components/ui/alerts/ErrorMessage";
import { InvoiceClient } from "../api/InvoiceClient";
import { StatusPill } from "@/shared/components/ui/statusPill/StatusPill";
import { formatDate, formatAmount, getStandardFilename, downloadXML } from "@/utils/utils";
import xmlIconUrl from "@/assets/xml.svg";
import "./ReceptionCredits.css";
import { useReceptionSupplierInfo } from "../receptionSupplierInfo";
import { Column } from "@/shared/components/ui/table/GenericTable";

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
    const invoice = InvoiceClient;
    const addenda = reception.listAddendum?.map((addendum: Addendum) => addendum.invoice);

    const columns: Column<Invoice>[] = [
        { header: "UUID", render: (r: any) => r.invoiceUuid },
        { header: "Fecha Registro", render: r => r.certificationDate ? formatDate(r.certificationDate) : "N/D" },
        { header: "Importe", render: r => formatAmount(r.total) },
        { header: "Serie", render: r => r.series },
        { header: "Folio", render: r => r.folio ?? "--" },
        { header: "Tipo", render: r => r.documentType ?? "--" }
        /*
        {
            header: "Acción",
            align: "center",
            render: (r) => (
                <div className="rc-action-wrap">
                    <button
                        title="Descargar XML"
                        onClick={async () => {
                            const data = await invoice.getXmlDocument(r.invoiceUuid ?? "");
                            const xmlString = await data.text();
                            downloadXML(xmlString, getStandardFilename(r.invoiceUuid ?? "") + ".xml");
                        }}
                        className="rc-action-btn"
                    >
                        <img src={xmlIconUrl} alt="Ver" className="rc-action-icon" />
                    </button>
                </div>
            ),
        },
        */
    ];

    return (
        <div className="rc-credits-container">
            <div className="rc-credits-card">
                <div className="rc-credits-row">
                    <div className="rc-credits-title">Facturas asignadas</div>
                </div>
                <div className="rc-credits-spacer" />
                <GenericTable
                    rows={addenda}
                    columns={columns}
                    emptyLabel="Sin resultados"
                />
            </div>
        </div>
    );
};

export function ReceptionDetail(): ReactElement {
    const params = useParams();
    const financeAlert = useFinanceAlertModal();

    const [loading, setLoading] = useState(false);
    const [reception, setReception] = useState<Reception>(EMPTY_RECEPTION);

    const supplierInfo = useReceptionSupplierInfo(reception);

    useEffect(() => {
        const fetchData = async (uuid: string) => {
            setLoading(true);
            try {
                const response = await OrderClient.getReceptionByUuid(uuid);
                setReception(response.data);
            } catch (err) {
                financeAlert.showErrorFrom(
                    "Error",
                    err,
                    "No fue posible cargar la recepción."
                );
            } finally {
                setLoading(false);
            }
        };

        if (params.uuid) {
            fetchData(params.uuid);
        }
    }, [params]);

    const breadcrumb: BreadcrumbItem[] = withFinanceBreadcrumb([
        { label: "Recepciones", to: "/finanzas/recepciones" },
        { label: `${reception.receptionNumber}` },
    ]);

    return (
        <>
            {decorate(
                breadcrumb,
                "/finanzas/recepciones",
                <>
                    <ReceptionHeader
                        reception={reception}
                        supplierInfo={supplierInfo}
                    />
                    {buildDetail(reception)}
                </>,
                loading,
                undefined,
                { financeListSession: FINANCE_LIST_KEYS.receptions }
            )}
            <GenericModal
                visible={financeAlert.alertVisible}
                variant="alert"
                severity={financeAlert.alertSeverity}
                title={financeAlert.alertTitle}
                message={financeAlert.alertMessage}
                buttonText="Aceptar"
                onClose={financeAlert.closeAlert}
            />
        </>
    );
}

export default ReceptionDetail;
