
import { forwardRef, useCallback, useEffect, useImperativeHandle, useRef, useState } from "react";
import type { Reception } from "../../interfaces";
import type { ReceptionSupplierInfo } from "../../receptionSupplierInfo";
import { GenericLinearProgress, GenericModal } from "@/shared/components/ui";
import { InvoiceClient } from "../../api/InvoiceClient";
import {
    RECEPTION_INVOICE_TRANSACTION,
    TransactionIdClient,
} from "../../api/transactionIdClient";
import { CatalogDetail, CatalogDetailRow, fetchCatalog, fetchProviderBlockers, fetchSystemParameters, formatAmount, formatDate, parseDisplayDate, startOfLocalDay, endOfLocalDay, SupplierBlock } from "@/utils/utils";
import { buildFiscalSpaUrl } from "@/utils/fiscalSpaUrl";
import type {
    SystemParameter,
    SystemParameterCheckResult,
} from "@/shared/types/systemParameters";

import { useFinanceAlertModal } from "@/shared/hooks/useFinanceAlertModal";
import {
    getInvalidInvoiceTypeMessage,
} from "../../receptionInvoiceCatalogMessages";
import "./ReceptionInvoiceControl.css";

const PARAM_NAME_TOLERANCE_VALUE = 'Tolerancia por importe';
const PARAM_NAME_TOLERANCE_PERCENT = 'Tolerancia por porcentaje';
const PARAM_OPTIONAL_PDF = 10;

function normalizeParamName(s: string | undefined | null): string {
    return (s ?? '').trim().toLowerCase();
}

interface InvoiceData {
    rfcEmisor: string;
    nombreProveedor: string;
    serie: string;
    folio: string;
    monto: string;
    fechaTimbrado: string;
    usoCfdi: string;
    tipoDeComprobante: string;
    uuid: string;
}

interface ReceptionInvoiceControlProps {
    reception: Reception;
    supplierInfo: ReceptionSupplierInfo;
    onActionStateChange?: () => void;
}

export interface ReceptionInvoiceControlHandle {
    submit: () => void;
    clear: () => void;
    isSaveDisabled: boolean;
    isClearDisabled: boolean;
}

export const ReceptionInvoiceControl = forwardRef<
    ReceptionInvoiceControlHandle,
    ReceptionInvoiceControlProps
>(function ReceptionInvoiceControl(
    { reception, supplierInfo, onActionStateChange },
    ref
) {
    const client =  InvoiceClient;
    const invoiceAlert = useFinanceAlertModal();
    const xmlInputRef = useRef<HTMLInputElement>(null);
    const pdfInputRef = useRef<HTMLInputElement>(null);
    const fileXMLRef = useRef<File | null>(null);
    const filePDFRef = useRef<File | null>(null);
    const [systemParameters, setSystemParameters] = useState<SystemParameter[] | null>(null);
    const [fileXML, setFileXML] = useState<File | null>(null);
    const [filePDF, setFilePDF] = useState<File | null>(null);
    const [isValidating, setIsValidating] = useState(false);
    const [isProcessing, setIsProcessing] = useState(false);
    const [invoiceData, setInvoiceData] = useState<InvoiceData | null>(null);
    const [isFinished, setIsFinished] = useState(false);
    const [dataMsg, setDataMsg] = useState("");
    const [registeredFiscalUuid, setRegisteredFiscalUuid] = useState("");
    const [isValidInvoice, setIsValidInvoice] = useState(false);
    const [optionalPdf, setOptionalPdf] = useState<SystemParameterCheckResult>({ value: "0", isEnabled: false });
    const [isBlockedType, setIsBlockedType] = useState(false);
    const [isBlockedProvider, setIsBlockedProvider] = useState(false);
    
    const checkSystemParameterValue = (parameterId: number): SystemParameterCheckResult => {
        const parameter = systemParameters?.find((p) => p.idParameter === parameterId);
        if (!parameter) {
            return { value: "", isEnabled: false };
        }

        return {
            value: parameter.value,
            isEnabled: parameter.status == "1",
        };
    };

    const checkSystemParameterByName = (paramName: string): SystemParameterCheckResult => {
        const target = normalizeParamName(paramName);
        const parameter = systemParameters?.find(
            (p) => normalizeParamName(p.name) === target,
        );
        if (!parameter) {
            return { value: "", isEnabled: false };
        }
        return {
            value: parameter.value,
            isEnabled: parameter.status == "1",
        };
    };

    useEffect(() => {
        const fetchSystemParametersData = async () => {
            const response = await fetchSystemParameters();
            setSystemParameters(response?.data ?? null);
        };
        const fetchBlockers = async () => {
            const response_blockers = await fetchCatalog("CatBloqueoTipoProveedor");
            const response_provider_blocks = await fetchProviderBlockers();

            // @ts-ignore
            const blockers = response_blockers?.details as CatalogDetailRow[];
            // @ts-ignore
            const provider_blocks = response_provider_blocks as SupplierBlock[];

            if (blockers) {
                const isTypeBlocked = blockers.some(
                    (detail) => detail.value == String(supplierInfo.supplierType?.id)
                );
                if (isTypeBlocked) {
                    setIsBlockedType(true);
                    showValidationAlert("Actualmente existe un bloqueo para la publicación de facturas según el tipo de proveedor. Por favor, valida con el área de Finanzas de Sodimac para continuar.");
                    return;
                }
            }

            if (provider_blocks) {
                const now = new Date();
                const isBlocked = provider_blocks.some((b) => {
                    if (!b.currentlyBlocked || String(b.supplierNumber) !== String(supplierInfo.number)) {
                        return false;
                    }
                    const from = parseDisplayDate(b.validFrom);
                    const to = parseDisplayDate(b.validTo);
                    if (!from || !to) return false;
                    return now >= startOfLocalDay(from) && now <= endOfLocalDay(to);
                });
                if (isBlocked) {
                    setIsBlockedProvider(true);
                    showValidationAlert("Actualmente existe un bloqueo para la publicación de facturas según el proveedor. Por favor, valida con el área de Finanzas de Sodimac para continuar.");
                }
            }
        };
        if(supplierInfo) {
          fetchSystemParametersData();
          fetchBlockers();
        }
    }, [supplierInfo]);

    useEffect(() => {
        setOptionalPdf(checkSystemParameterValue(PARAM_OPTIONAL_PDF));
    }, [systemParameters]);
    const resetFileInputs = () => {
        if (xmlInputRef.current) xmlInputRef.current.value = "";
        if (pdfInputRef.current) pdfInputRef.current.value = "";
    };

    const handleClearForm = useCallback(() => {
        resetFileInputs();
        fileXMLRef.current = null;
        filePDFRef.current = null;
        setFileXML(null);
        setFilePDF(null);
        setInvoiceData(null);
        setIsFinished(false);
        setIsValidInvoice(false);
        setDataMsg("");
        setRegisteredFiscalUuid("");
        setIsValidating(false);
        setIsProcessing(false);
    }, []);

    const showValidationAlert = (message: string) => {
        setIsValidInvoice(false);
        setDataMsg("");
        invoiceAlert.showWarning("Atención", message);
    };

    const showFinishAlert = (response: { message?: string; warnings?: string[] }) => {
        setDataMsg("");
        const warnings = (response.warnings ?? []).filter((w) => String(w).trim() !== "");
        if (warnings.length > 0) {
            invoiceAlert.showWarning("Atención", warnings.join("\n\n"));
            return;
        }
        invoiceAlert.showSuccess(
            "Operación exitosa",
            response.message || "Tu factura se procesó correctamente."
        );
    };

    const validateInvoice = async () => {
        const currentXml = fileXMLRef.current ?? xmlInputRef.current?.files?.[0] ?? null;
        const currentPdf = filePDFRef.current ?? pdfInputRef.current?.files?.[0] ?? null;

        if (!currentXml) {
            showValidationAlert("El archivo XML es requerido");
            return;
        }
        if (!optionalPdf.isEnabled && !currentPdf) {
            showValidationAlert("El archivo PDF es requerido");
            return;
        }
        if (!invoiceData?.monto) {
            showValidationAlert("Primero selecciona y valida el XML; cuando aparezca el resumen podrás guardar.");
            return;
        }
        if (invoiceData.tipoDeComprobante !== "I") {
            showValidationAlert("El archivo XML no corresponde a una factura válida. Por favor, valida el documento antes de continuar.");
            return;
        }
        if (supplierInfo.rfc != invoiceData?.rfcEmisor){
            showValidationAlert("El RFC del proveedor no coincide con la factura publicada. Por favor, valida el archivo XML.");
            return;
        }

        const invoiceAmount = parseFloat(invoiceData.monto);
        const receptionAmount = parseFloat(reception.amount + "");

        setDataMsg("");
        const uuidFromXml = (invoiceData.uuid ?? "").trim();
        if (!uuidFromXml) {
            showValidationAlert("El XML validado no contiene UUID (timbre fiscal). Vuelve a cargar un XML válido." );
            return;
        }

        const difference = Math.abs(invoiceAmount - receptionAmount);
        const toleranceValue = checkSystemParameterByName(PARAM_NAME_TOLERANCE_VALUE);
        const tolerancePercent = checkSystemParameterByName(PARAM_NAME_TOLERANCE_PERCENT);
        
        let tolerance = 0;
        if (toleranceValue.isEnabled && toleranceValue.value != null) {
            tolerance = Number(toleranceValue.value);
        } else if (tolerancePercent.isEnabled && tolerancePercent.value != null) {
            tolerance = invoiceAmount * (Number(tolerancePercent.value) / 100);
        }

        if (difference <= tolerance || invoiceAmount > receptionAmount) {
            setIsValidInvoice(true);
            try {
                setIsProcessing(true);
                const trace = await TransactionIdClient.create({
                    ...RECEPTION_INVOICE_TRANSACTION,
                    metadatos: {
                        receptionId: reception.receptionId,
                        receptionNumber: reception.receptionNumber,
                        purchaseOrderId: reception.purchaseOrderId,
                        orderNumber: reception.orderNumber,
                        supplierNumber: reception.supplierNumber,
                    },
                });
                const response = await client.create(currentXml, currentPdf, {
                    idTransaccion: trace.uuidInterno,
                    receptionId: reception.receptionId,
                    supplierNumber: supplierInfo.number,
                    purchaseOrderNumber: String(
                        reception.order?.orderNumber ?? reception.orderNumber ?? ""
                    ),
                });
                if (response.fiscalUuid) {
                    if (response.pendingAddenda) {
                        await client.update(response.invoiceUuid, {
                            idUsuarioActualizacion: "1",
                            estatus: "2",
                            numeroProveedor: supplierInfo.number,
                            fiscalUuid: response.fiscalUuid,
                            uuid: uuidFromXml,
                            idTransaccion: trace.uuidInterno,
                        });
                    }
                    const fiscalUuid = String(response.fiscalUuid || uuidFromXml).trim();
                    setRegisteredFiscalUuid(fiscalUuid);
                    setDataMsg("");
                    setIsValidInvoice(true);
                    setIsFinished(true);
                    showFinishAlert(response);
                } else {
                    setDataMsg("");
                    invoiceAlert.showError(
                        "Error",
                        `${response.code}: ${response.message}`
                    );
                    setIsValidInvoice(false);
                }
            } catch (response: unknown) {
                invoiceAlert.showErrorFrom(
                    "Error",
                    response,
                    "No fue posible procesar la factura."
                );
                setIsValidInvoice(false);
            } finally {
                setIsProcessing(false);
            }
        } else {
            showValidationAlert(`Hay una diferencia mayor a ${formatAmount(tolerance)} entre la factura y el total a facturar`);
            return;
        }
    }

    const handleFilePDFChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const selectedFile = e.target.files?.[0] ?? null;
        filePDFRef.current = selectedFile;
        setFilePDF(selectedFile);
        if (selectedFile) {
            setIsValidInvoice(true);
        }
    };

    const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        setDataMsg("");
        setInvoiceData(null);
        setIsValidating(true)
        const selectedFile = e.target.files?.[0] ?? null;
        if (selectedFile) {
            fileXMLRef.current = selectedFile;
            setFileXML(selectedFile);
            try {
                const data = await client.validateInvoice(selectedFile);
                const parsedInvoice: InvoiceData = {
                    rfcEmisor: data.emisor.rfc,
                    nombreProveedor: data.emisor.nombre,
                    serie: data.comprobante.serie,
                    folio: data.comprobante.folio,
                    monto: data.comprobante.subTotal,
                    fechaTimbrado: data.comprobante.fecha,
                    usoCfdi: data.receptor.usoCFDI,
                    tipoDeComprobante: data.comprobante.tipoDeComprobante,
                    uuid: data.timbreFiscalDigital.uuid,
                };
                setInvoiceData(parsedInvoice);

                if (parsedInvoice.tipoDeComprobante !== "I") {
                    setIsValidInvoice(false);
                    setDataMsg("");
                    invoiceAlert.showWarning(
                        "Atención",
                        await getInvalidInvoiceTypeMessage()
                    );
                    return;
                }

                const xmlValidationOk = data.metadatos.estado === "SUCCESS";
                const rfcMatches = supplierInfo.rfc == parsedInvoice.rfcEmisor;
                setIsValidInvoice(xmlValidationOk && rfcMatches);

                if (!rfcMatches) {
                    setDataMsg("");
                    invoiceAlert.showWarning(
                        "Atención", "El RFC del proveedor no coincide con la factura publicada. Por favor, valida el archivo XML.");
                    return;
                }

                if (xmlValidationOk) {
                    setDataMsg("El archivo XML fue validado correctamente.");
                } else {
                    setDataMsg("");
                    invoiceAlert.showWarning(
                        "Atención",
                        data.metadatos.mensaje || "El archivo XML no corresponde a una factura válida. Por favor, valida el documento antes de continuar."
                    );
                }
            } catch (response: any) {
                invoiceAlert.showErrorFrom(
                    "Error al validar XML",
                    response,
                    "El archivo XML no corresponde a una factura válida. Por favor, valida el documento antes de continuar."
                );
                setIsValidInvoice(false)
            } finally {
                setIsValidating(false);
            }
        }
        
    };

    const isSaveDisabled =
        isFinished || isProcessing || !invoiceData || !isValidInvoice || isBlockedType || isBlockedProvider ;

    const validateInvoiceRef = useRef(validateInvoice);
    validateInvoiceRef.current = validateInvoice;

    useImperativeHandle(
        ref,
        () => ({
            submit: () => {
                validateInvoiceRef.current();
            },
            clear: handleClearForm,
            isSaveDisabled,
            isClearDisabled: isProcessing || isFinished || isBlockedType || isBlockedProvider,
        }),
        [isSaveDisabled, isProcessing, isFinished, isBlockedType, isBlockedProvider, handleClearForm]
    );

    useEffect(() => {
        onActionStateChange?.();
    }, [isSaveDisabled, isProcessing, isBlockedType, isBlockedProvider, onActionStateChange]);

return (
  <>
  <div className="rc-invoice-control">
    

    {isProcessing ? <GenericLinearProgress /> : null}

    <div className="rc-invoice-layout">
      <div className="rc-invoice-form">
        <h2 className="rc-title">Subir Factura</h2>
        
        <div className="rc-upload-grid">
          {isValidating ? (
            <GenericLinearProgress />
          ) : (
            <label className={`rc-upload-label${isFinished || isBlockedType || isBlockedProvider ? " rc-upload-label--disabled" : ""}`}>
              <input
                ref={xmlInputRef}
                type="file"
                accept=".xml"
                className="rc-file-input"
                disabled={isFinished || isBlockedType || isBlockedProvider}
                onChange={handleFileChange}
              />
              <p className="rc-upload-text">Subir XML de la factura (Requerido)</p>
              {fileXML && (
                <p className="rc-upload-file">{fileXML.name}</p>
              )}
            </label>
          )}
          <label className={`rc-upload-label${isFinished || isBlockedType || isBlockedProvider ? " rc-upload-label--disabled" : ""}`}>
            <input
              ref={pdfInputRef}
              type="file"
              accept=".pdf"
              className="rc-file-input"
              disabled={isFinished || isBlockedType || isBlockedProvider}
              onChange={handleFilePDFChange}
            />
            <p className="rc-upload-text">Subir PDF de la factura ({optionalPdf.isEnabled ? "Opcional" : "Requerido"})</p>
            {filePDF && (
              <p className="rc-upload-file">{filePDF.name}</p>
            )}
          </label>
        </div>

        {registeredFiscalUuid ? (
          <p
            className="rc-invoice-notice rc-invoice-notice--success"
            role="status"
            aria-live="polite"
          >
            Consulta la factura en el módulo fiscal:{" "}
            <a
              href={buildFiscalSpaUrl(
                "facturas",
                new URLSearchParams({ uuid: registeredFiscalUuid })
              )}
              target="_blank"
              rel="noopener noreferrer"
              className="rc-invoice-fiscal-link"
            >
              Ver factura ({registeredFiscalUuid})
            </a>
          </p>
        ) : dataMsg.trim() !== "" ? (
          <p
            className={`rc-invoice-notice rc-invoice-notice--${isValidInvoice ? "success" : "error"}`}
            role="status"
            aria-live="polite"
          >
            {dataMsg}
          </p>
        ) : null}
      </div>

      {invoiceData && (
        <div className="rc-invoice-summary-wrap">
          <div className="rc-invoice-summary">
            <div className="rc-summary-text">
              <table className="rc-summary-table">
                <tbody>
                  <tr>
                    <td className="rc-cell rc-cell-label">RFC Emisor:</td>
                    <td className="rc-cell">{invoiceData.rfcEmisor}</td>
                  </tr>
                  <tr>
                    <td className="rc-cell rc-cell-label">Nombre Proveedor:</td>
                    <td className="rc-cell">{invoiceData.nombreProveedor}</td>
                  </tr>
                  <tr>
                    <td className="rc-cell rc-cell-label">Serie:</td>
                    <td className="rc-cell">{invoiceData.serie}</td>
                  </tr>
                  <tr>
                    <td className="rc-cell rc-cell-label">UUID:</td>
                    <td className="rc-cell">{invoiceData.uuid}</td>
                  </tr>
                  <tr>
                    <td className="rc-cell rc-cell-label">Folio:</td>
                    <td className="rc-cell">{invoiceData.folio}</td>
                  </tr>
                  <tr>
                    <td className="rc-cell rc-cell-label">Importe:</td>
                    <td className="rc-cell">{formatAmount(parseFloat(invoiceData.monto))}</td>
                  </tr>
                  <tr>
                    <td className="rc-cell rc-cell-label">Fecha Timbrado:</td>
                    <td className="rc-cell">{formatDate(invoiceData.fechaTimbrado, true)}</td>
                  </tr>
                  <tr>
                    <td className="rc-cell rc-cell-label">Uso CFDI:</td>
                    <td className="rc-cell">{invoiceData.usoCfdi}</td>
                  </tr>
                  <tr>
                    <td className="rc-cell rc-cell-label">Tipo Comprobante:</td>
                    <td className="rc-cell">{invoiceData.tipoDeComprobante}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  </div>

  <GenericModal
    visible={invoiceAlert.alertVisible}
    variant="alert"
    severity={invoiceAlert.alertSeverity}
    title={invoiceAlert.alertTitle}
    message={invoiceAlert.alertMessage}
    buttonText="Aceptar"
    onClose={invoiceAlert.closeAlert}
  />
  </>
);

});
