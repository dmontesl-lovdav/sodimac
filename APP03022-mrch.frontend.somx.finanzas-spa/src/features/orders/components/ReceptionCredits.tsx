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
import { formatDate, formatAmount, getStandardFilename, downloadXML, fetchProviders } from "@/utils/utils";
import xmlIconUrl from "@/assets/xml.svg";
import "./ReceptionCredits.css";
import { useReceptionSupplierInfo } from "../receptionSupplierInfo";
import { Column } from "@/shared/components/ui/table/GenericTable";
import { buildFiscalSpaUrl } from "@/utils/fiscalSpaUrl";
import viewIcon from '@assets/eye-show.svg';

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

const BuildDetail = ({ reception }: { reception: Reception }) => {
    const [invoices, setInvoices] = useState<Addendum[]>([]);
    const [suppliers, setSuppliers] = useState<any[]>([]);

    useEffect(() => {
        fetchProviders()
            .then((response) => setSuppliers(response ?? []))
            .catch(() => undefined);

        const relatedUuid = reception.listAddendum?.[0]?.invoice?.invoiceUuid;
        if (!relatedUuid || !reception.createdAt) {
            setInvoices([]);
            return;
        }

        const endDate = reception.createdAt.split("T")[0];
        const start = new Date(reception.createdAt);
        start.setMonth(start.getMonth() - 6);
        const startDate = start.toISOString().split("T")[0];

       InvoiceClient.getCreditNotesByInvoiceUuid(relatedUuid, startDate, endDate).then((response) => {
            const credits = (response?.content ?? []).map((item: Invoice & { invoiceUuid?: string; createdAt?: string }) => ({
                addendumUuid: item.invoiceUuid ?? "",
                invoiceUuid: item.invoiceUuid ?? "",
                supplierNumber: item.emisorRfc,
                receptionNumber: reception.receptionNumber ?? "",
                createdAt: item.createdAt ?? "",
                invoice: {
                    invoiceUuid: item.invoiceUuid,
                    emisorRfc: item.emisorRfc,
                    subtotal: item.subtotal,
                    series: item.series,
                    folio: item.folio,
                    documentType: item.documentType,
                    total: item.total ?? 0,
                    xml_content: "",
                    createdAt: item.createdAt,
                },
            }));
            setInvoices(credits);
        });
    }, [reception]);

    const addenda = [...(reception.listAddendum ?? []), ...invoices];
    console.log(addenda);

    const columns: Column<Addendum>[] = [
        { header: "UUID", render: (r) => r.invoice?.invoiceUuid ?? "--" },
        { header: "Fecha Registro", render: r => r.createdAt ? formatDate(r.createdAt) : "N/D" },
        { header: "Importe", render: (r) => formatAmount(r.invoice?.subtotal ?? 0) },
        { header: "Serie", render: (r) => r.invoice?.series ?? "--" },
        { header: "Folio", render: (r) => r.invoice?.folio ?? "--" },
        { header: "Tipo", render: (r) => r.invoice?.documentType ?? "--" },
        {
            header: "Acción",
            align: "center",
            render: (r) => {
                const isCreditNote = r.invoice?.documentType === "E";
                const emisorRfc =
                    r.invoice?.emisorRfc ??
                    reception.supplier?.rfc ??
                    "";
                const supplierRfc =
                    suppliers.find((s) => s.rfc === emisorRfc)?.supplierNumber ?? emisorRfc;
                const endDate = reception.createdAt?.split("T")[0] ?? "";
                const start = new Date(reception.createdAt ?? "");
                start.setMonth(start.getMonth() - 6);
                const startDate = start.toISOString().split("T")[0];
                const relatedInvoiceUuid =
                    reception.listAddendum?.[0]?.invoice?.invoiceUuid ?? "";

                const params = new URLSearchParams({
                    supplierNumber: supplierRfc,
                    
                    start: startDate,
                    end: endDate,
                });
                if(!isCreditNote) {
                    params.set("uuid", r.invoice?.fiscalUuid ?? "");
                }

                if (isCreditNote && relatedInvoiceUuid) {
                    params.set("relatedInvoiceUuid", relatedInvoiceUuid);
                }

                return (
                    <div className="rc-action-wrap">
                        <a
                            href={buildFiscalSpaUrl(
                                isCreditNote ? "notas-credito" : "facturas",
                                params
                            )}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="rc-invoice-fiscal-link"
                        >
                            <button
                                title={isCreditNote ? "Ver nota de crédito" : "Ver factura"}
                                className="rc-action-btn"
                                type="button"
                            >
                                <img
                                    src={viewIcon}
                                    alt={isCreditNote ? "Ver nota de crédito" : "Ver factura"}
                                    className="rc-action-icon"
                                />
                            </button>
                        </a>
                    </div>
                );
            },
        },
        
    ];

    return (
        <div className="rc-credits-container">
            <div className="rc-credits-card">
                <div className="rc-credits-row">
                    <div className="rc-credits-title">Documento fiscal relacionado</div>
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
                    <BuildDetail reception={reception} />
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
