import {
    GenericButton,
    GenericInput,
    GenericModal,
    GenericSelect,
} from "@/shared/components/ui";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { withFinanceBreadcrumb } from "@/shared/components/ui/navigation/financeBreadcrumb";
import { useFinanceAlertModal } from "@/shared/hooks/useFinanceAlertModal";
import { FINANCE_LIST_KEYS } from "@/shared/hooks";
import { exportToCSV, formatDate, formatFilenameTimestamp } from "@/utils/utils";

import { ChangeEvent, ReactElement, forwardRef, useCallback, useEffect, useImperativeHandle, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import { OrderClient } from "../api/OrderClient";
import { EMPTY_RECEPTION, ReceptionStatusEditOptions, Reception } from "../interfaces";
import ReceptionHeader from "./parts/ReceptionHeader";
import ReceptionSkusTable from "./parts/ReceptionSkusTable";
import "./ReceptionDetail.css";
import { useReceptionSupplierInfo } from "../receptionSupplierInfo";

export interface ReceptionEditStatusFormHandle {
    submit: () => void;
}

interface ReceptionEditStatusFormProps {
    reception: Reception;
    updateOrderStatus: (reason: string, status: number, uuid: string) => void | Promise<void>;
    showWarning: (title: string, message: string) => void;
}

const ReceptionEditStatusForm = forwardRef<
    ReceptionEditStatusFormHandle,
    ReceptionEditStatusFormProps
>(function ReceptionEditStatusForm(
    {
    reception,
    updateOrderStatus,
    showWarning,
    },
    ref
) {
    const [status, setStatus] = useState(reception.status);
    const [reason, setReason] = useState(reception.comment ?? "");
    const [uuid, setUuid] = useState(reception.invoiceUuid ?? "");

    console.log(reception);

    useEffect(() => {
        if(reception.status !== undefined) {
            setStatus(reception.status);
            setReason(reception.status == 0 ? "":reception.comment ?? "");
            setUuid(reception.invoiceUuid ?? "");
        }
    }, [reception]);

    const checkInformation = useCallback(() => {
        if (reason.trim() === "") {
            showWarning(
                "Validación",
                "Razón o motivo es un campo requerido."
            );
            return;
        }
        if (status === 2 && uuid.trim() === "") {
            showWarning("Validación", "UUID es un campo requerido.");
            return;
        }
        updateOrderStatus(reason, status, uuid);
    }, [reason, status, uuid, showWarning, updateOrderStatus]);

    useImperativeHandle(ref, () => ({ submit: checkInformation }), [checkInformation]);

    return (
        <div className="rc-detail-container">
            <div className="rc-detail-card">
                <div className="rc-row">
                    <div className="rc-label">Editar Estado:</div>
                    
                </div>
                <div className="rc-row">
                    <GenericSelect
                        value={status}
                        onChange={(event: ChangeEvent<HTMLInputElement>) =>
                            setStatus(parseInt(event.target.value, 10))
                        }
                        placeholder="Estado"
                        disablePlaceholder={true}
                        disabled={reception.status != 0}
                        options={ReceptionStatusEditOptions}
                    />
                </div>
                <div className="rc-row">
                    <GenericInput
                        label="Motivo de cambio de estado"
                        placeholder="Escribe la razón por el cambio de estado"
                        value={reason}
                        disabled={reception.status != 0}
                        onChange={(event: ChangeEvent<HTMLInputElement>) =>
                            setReason(event.target.value)
                        }
                    />
                </div>

                {status == 2 && (
                    <div className="rc-row">
                        <GenericInput
                            label="UUID"
                            placeholder="Proporciona el UUID para complementar"
                            value={uuid}
                            disabled={reception.status != 0}
                            onChange={(event: ChangeEvent<HTMLInputElement>) =>
                                setUuid(event.target.value)
                            }
                        />
                    </div>
                )}
                
            </div>
        </div>
    );
});

interface ReceptionDetailProps {
    editable?: boolean
}

export function ReceptionDetail({ editable = false }: ReceptionDetailProps): ReactElement {
    const params = useParams();
    const financeAlert = useFinanceAlertModal();
    const editFormRef = useRef<ReceptionEditStatusFormHandle>(null);

    const [loading, setLoading] = useState(false);
    const [isExporting, setIsExporting] = useState(false);
    const [reception, setReception] = useState<Reception>(EMPTY_RECEPTION);
    const client = OrderClient;

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
        fetchData(params.uuid || "");
    }, [params]);

    const updateOrderStatus = async (reason: string, status: number, uuid: string) => {

        try {
            if (reception && reception.receptionId) {
                setLoading(true);
                const supplierNumber = Number(
                    reception.order?.supplierNumber ?? reception.supplierNumber ?? 0
                );
                const orderNumber = String(
                    reception.order?.orderNumber ?? reception.orderNumber ?? ""
                );
                const receptionNumber = String(
                    reception.receptionNumber ?? ""
                );
                if (!orderNumber || !receptionNumber || !Number.isFinite(supplierNumber)) {
                    financeAlert.showWarning(
                        "Datos incompletos",
                        "Faltan datos de orden o proveedor para actualizar el estado."
                    );
                    return;
                }
                if (status == 2 && !uuid.trim().match(/^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/)) {
                        financeAlert.showWarning(
                            "Datos incorrectos",
                            "El UUID no es válido."
                        );
                        return;
                }

                const payload = {
                    supplierNumber,
                    orderNumber,
                    receptionNumber,
                    status,
                    comments: reason,
                    ...(status === 2 && uuid.trim() !== ""
                        ? { uuid: uuid.trim() }
                        : {}),
                };
                const response = await client.updateReceptionManual(payload);
                //console.log(response);
                financeAlert.showSuccess("Estado actualizado", (response as any).data.message ?? "El estado de la recepción se guardó correctamente.");
                
                const refreshed = await client.getReceptionByUuid(
                    String(params.uuid ?? reception.receptionId)
                );
                setReception(refreshed.data);
                //financeAlert.showSuccess("Estado actualizado", "El estado de la recepción se guardó correctamente.");*/
            }

        } catch (err) {
            const error = (err as any).response.data.detailError;
            financeAlert.showError("Error", error);
            console.error(err);
        } finally {
            setLoading(false);
        }
    };



    const handleExportCSV = () => {
        const skus = reception?.receptionSkus ?? [];
        if (!skus.length) return;
        setIsExporting(true);
        const headers = [
            "Orden Compra",
            "Recepción",
            "Importe",
            "Fecha Recepción",
            "Número Proveedor",
            "SKU",
            "Descripción",
            "Cantidad",
            "Precio Unitario",
            "Importe",
        ];
        const rows = skus.map((item) => [
            reception.order?.orderNumber ?? reception.orderNumber ?? "",
            reception.receptionNumber || reception.receptionId || "",
            String(reception.amount ?? ""),
            reception.receptionDate ? formatDate(String(reception.receptionDate)) : "",
            String(reception.order?.supplierNumber ?? reception.supplierNumber ?? ""),
            item.sku,
            item.description,
            parseInt(item.quantity, 10).toString(),
            parseFloat(item.unitCost).toFixed(2),
            parseFloat(item.totalCost).toFixed(2),
        ]);
        const receptionId = String(reception.receptionNumber || reception.receptionId || "rec");
        const baseName = `recepcion_detalle_${receptionId.replace(/[^\w\-]+/g, "_").slice(0, 80)}_${formatFilenameTimestamp()}`;
        exportToCSV(headers, rows, baseName);
        setIsExporting(false);
    };

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
                        headerActions={
                            editable ? (
                                <GenericButton
                                    disabled={reception.status != 0}
                                    variant="primary"
                                    onClick={() => {
                                        editFormRef.current?.submit();
                                    }}
                                >
                                    Guardar
                                </GenericButton>
                            ) : <GenericButton
                            variant="primary"
                            type="button"
                            disabled={isExporting}
                            onClick={handleExportCSV}
                        >
                            Exportar CSV
                        </GenericButton>
                        }
                    />
                    {editable && (
                        <ReceptionEditStatusForm
                            ref={editFormRef}
                            reception={reception}
                            updateOrderStatus={updateOrderStatus}
                            showWarning={financeAlert.showWarning}
                        />
                    )}
                    {!editable && <ReceptionSkusTable reception={reception} />}
                </>,
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

export default ReceptionDetail;
