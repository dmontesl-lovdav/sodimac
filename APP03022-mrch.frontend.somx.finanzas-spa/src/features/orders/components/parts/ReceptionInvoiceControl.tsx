
import { ChangeEvent, useState } from "react";
import { EMPTY_INVOICE, Invoice, Order, Reception } from "../../interfaces";
import { GenericButton, GenericInput, GenericLinearProgress } from "@/shared/components/ui";
import { InvoiceClient } from "../../api/InvoiceClient";
import { formatAmount, formatDate } from "@/utils/utils";
import SuccessMessage from "@/shared/components/ui/alerts/SuccessMessage";
import ErrorMessage from "@/shared/components/ui/alerts/ErrorMessage";
import "./ReceptionInvoiceControl.css";

interface InvoiceData {
    rfcEmisor: string;
    nombreProveedor: string;
    serie: string;
    folio: string;
    monto: string;
    fechaTimbrado: string;
    usoCfdi: string;
    uuid: string;
}

interface ReceptionInvoiceControlProps {
    reception: Reception
}

export const ReceptionInvoiceControl = ({ reception }: ReceptionInvoiceControlProps) => {
    const client =  InvoiceClient;
    const [fileXML, setFileXML] = useState<File | null>(null);
    const [filePDF, setFilePDF] = useState<File | null>(null);
    const [isValidating, setIsValidating] = useState(false);
    const [isProcessing, setIsProcessing] = useState(false);
    const [invoiceData, setInvoiceData] = useState<InvoiceData | null>(null);
    const [isFinished, setIsFinished] = useState(false);
    const [dataMsg, setDataMsg] = useState("");
    const [isValidInvoice, setIsValidInvoice] = useState(false);

    const [series, setSeries] = useState("");
    const [uuid, setUuid] = useState("");


    const validateInvoice = async () => {
        if (!fileXML) {
            setIsValidInvoice(false);
            setDataMsg("El archivo XML es requerido")
            return;
        }
        if (invoiceData && invoiceData.monto) {
            const invoiceAmount = parseFloat(invoiceData.monto);
            const receptionAmount = parseFloat(reception.amount+"");

            setDataMsg("");
            if (series.trim() === "") {
                setIsValidInvoice(false);
                setDataMsg("Serie es un campo requerido")
                return;
            }
            if (uuid.trim() === "") {
                setIsValidInvoice(false);
                setDataMsg("UUID es un campo requerido")
                return;
            }
            if (series.trim() !== invoiceData?.serie) {
                setIsValidInvoice(false);
                setDataMsg("La serie no coincide en la factura")
                return;
            }
            if (uuid.trim() !== invoiceData?.uuid) {
                setIsValidInvoice(false);
                setDataMsg("El UUID no coincide en la factura")
                return;
            }

            const difference = Math.abs(invoiceAmount - receptionAmount);
            if (difference >40) {
                setIsValidInvoice(true);
                try {
                    setIsProcessing(true);
                    const response = await client.create(fileXML);
                    if (response.fiscalUuid) {
                        if (response.pendingAddenda) {
                            const updated = await client.update(response.invoiceUuid, {
                                "idUsuarioActualizacion": "1",
                                "estatus": "2",
                                "numeroProveedor": reception.order.supplierNumber,
                                "fiscalUuid": response.fiscalUuid,
                                "uuid": uuid
                            });
                            console.log(updated)
                        }
                        setDataMsg("Tu factura se procesó correctamente");
                        setIsValidInvoice(true)
                        setIsFinished(true);
                    } else {
                        setDataMsg(response.code + ": " + response.message);
                        setIsValidInvoice(false)
                    }
                    console.log(response)
                } catch (response: any) {
                    setDataMsg(response.code + ": " + response.message);
                    setIsValidInvoice(false)
                } finally {
                    setIsProcessing(false);
                }
            } else {
                setIsValidInvoice(false);
                setDataMsg("Hay una diferencia mayor a $40 entre la factura y el total a facturar");
            }

        }
    }

    const handleFilePDFChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const selectedFile = e.target.files?.[0] || null;
        setFilePDF(selectedFile);
    };

    const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        setDataMsg("");
        setInvoiceData(null);
        setIsValidating(true)
        const selectedFile = e.target.files?.[0] || null;
        if (selectedFile) {
            try {
                const data = await client.validateInvoice(selectedFile)
                setInvoiceData({
                    rfcEmisor: data.emisor.rfc,
                    nombreProveedor: data.emisor.nombre,
                    serie: data.comprobante.serie,
                    folio: data.comprobante.folio,
                    monto: data.comprobante.subTotal,
                    fechaTimbrado: data.comprobante.fecha,
                    usoCfdi: data.receptor.usoCFDI,
                    uuid: data.timbreFiscalDigital.uuid
                });
                setIsValidInvoice(data.metadatos.estado === "SUCCESS")
                setDataMsg(data.metadatos.mensaje)
            } catch (response: any) {
                setDataMsg(response.code + ": " + response.message);
                setIsValidInvoice(false)
            } finally {
                setIsValidating(false);
            }
        }
        setFileXML(selectedFile);
    };


return (
  <div className="rc-invoice-control">
    <div className="rc-invoice-layout">
      <div className="rc-invoice-form">
        <h2 className="rc-title">Subir Factura</h2>
        <p>Sube tu factura XML, tus datos serán validados de acuerdo a lo proporcionado</p>
        <div className="rc-field">
          <GenericInput
            label="Serie de la factura"
            placeholder="00000"
            value={series}
            onChange={(event: ChangeEvent<HTMLInputElement>) => setSeries(event.target.value)}
          />
        </div>
        <div className="rc-field">
          <GenericInput
            label="UUID de la factura"
            placeholder="Escribe la serie UUID"
            value={uuid}
            onChange={(event: ChangeEvent<HTMLInputElement>) => setUuid(event.target.value)}
          />
        </div>
        <div className="rc-upload-grid">
          {isValidating ? (
            <GenericLinearProgress />
          ) : (
            <label className="rc-upload-label">
              <input
                type="file"
                accept=".xml"
                className="rc-file-input"
                onChange={handleFileChange}
              />
              <p className="rc-upload-text">Subir XML de la factura (Requerido)</p>
              {fileXML && (
                <p className="rc-upload-file">{fileXML.name}</p>
              )}
            </label>
          )}
          <label className="rc-upload-label">
            <input
              type="file"
              accept=".pdf"
              className="rc-file-input"
              onChange={handleFilePDFChange}
            />
            <p className="rc-upload-text">Subir PDF de la factura (opcional)</p>
            {filePDF && (
              <p className="rc-upload-file">{filePDF.name}</p>
            )}
          </label>
        </div>

        {isValidInvoice ? (
          <SuccessMessage message={dataMsg} />
        ) : (
          <ErrorMessage message={dataMsg} />
        )}
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
                    <td className="rc-cell rc-cell-label">Monto:</td>
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
                </tbody>
              </table>
            </div>

            <div className="rc-summary-actions">
              {!isProcessing && <GenericLinearProgress />}
              <GenericButton
                disabled={isFinished || isProcessing}
                className="rc-save-btn"
                variant="outline"
                onClick={() => validateInvoice()}
              >
                Guardar
              </GenericButton>
            </div>
          </div>
        </div>
      )}
    </div>
  </div>
);

};
