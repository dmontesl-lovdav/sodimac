import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { GenericModal } from "@/shared/components/ui";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { withFinanceBreadcrumb } from "@/shared/components/ui/navigation/financeBreadcrumb";
import { useFinanceAlertModal } from "@/shared/hooks/useFinanceAlertModal";
import { FINANCE_LIST_KEYS } from "@/shared/hooks";
import { ReactElement, useCallback, useEffect, useRef, useState } from "react";
import { GenericButton } from "@/shared/components/ui";
import { useParams } from "react-router-dom";
import { OrderClient } from "../api/OrderClient";
import { EMPTY_RECEPTION, Reception } from "../interfaces";
import { ReceptionInvoiceControl, type ReceptionInvoiceControlHandle } from "./parts/ReceptionInvoiceControl";
import ReceptionHeader from "./parts/ReceptionHeader";
import "./ReceptionInvoice.css";
import { useReceptionSupplierInfo } from "../receptionSupplierInfo";


export function ReceptionInvoice(): ReactElement {
    const params = useParams();
    const financeAlert = useFinanceAlertModal();
    const invoiceControlRef = useRef<ReceptionInvoiceControlHandle>(null);
    const [, setActionStateVersion] = useState(0);
    const bumpInvoiceActionState = useCallback(() => {
        setActionStateVersion((v) => v + 1);
    }, []);

    const [loading, setLoading] = useState(false);
    const [reception, setReception] = useState<Reception>(EMPTY_RECEPTION);
    const client: any = OrderClient;
    const supplierInfo = useReceptionSupplierInfo(reception);

    useEffect(() => {
        const fetchData = async (uuid: string) => {
            setLoading(true);
            try {
                const response = await client.getReceptionByUuid(uuid);
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
        if(params.uuid){
            fetchData(params.uuid);
        }
    }, [params]);

    const breadcrumb: BreadcrumbItem[] = withFinanceBreadcrumb([
        { label: "Recepciones", to: "/finanzas/recepciones" },
        { label: `${reception.receptionId}` },
    ]);

    return (
        <>
            {decorate(
                breadcrumb,
                "/finanzas/recepciones",
                <div className="rc-reception-invoice">
                    <ReceptionHeader
                        reception={reception}
                        supplierInfo={supplierInfo}
                        headerActions={
                            <div className="rc-header-actions-group">
                                <GenericButton
                                    disabled={invoiceControlRef.current?.isSaveDisabled ?? true}
                                    variant="primary"
                                    onClick={() => {
                                        invoiceControlRef.current?.submit();
                                    }}
                                >
                                    Guardar
                                </GenericButton>
                                <GenericButton
                                    variant="outlineFill"
                                    disabled={invoiceControlRef.current?.isClearDisabled ?? false}
                                    onClick={() => {
                                        invoiceControlRef.current?.clear();
                                    }}
                                >
                                    Limpiar
                                </GenericButton>
                            </div>
                        }
                    />
                    <ReceptionInvoiceControl
                        ref={invoiceControlRef}
                        reception={reception}
                        supplierInfo={supplierInfo}
                        onActionStateChange={bumpInvoiceActionState}
                    />
                </div>,
                loading,
                undefined,
                {
                    actionsAlign: "end",
                    financeListSession: FINANCE_LIST_KEYS.receptions,
                }
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

export default ReceptionInvoice;