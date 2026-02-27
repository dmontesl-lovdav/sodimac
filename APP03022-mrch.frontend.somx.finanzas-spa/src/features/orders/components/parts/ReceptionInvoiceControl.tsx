
import { ChangeEvent, useState } from "react";
import { EMPTY_INVOICE, Invoice, Order, Reception } from "../../interfaces";
import { GenericButton, GenericInput, GenericLinearProgress } from "@/shared/components/ui";
import { InvoiceClient } from "../../api/InvoiceClient";
import { formatAmount, formatDate } from "@/utils/utils";
import SuccessMessage from "@/shared/components/ui/alerts/SuccessMessage";
import ErrorMessage from "@/shared/components/ui/alerts/ErrorMessage";

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
    const client = new InvoiceClient();
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
  <div className="somx-bg-white somx-mt-2 somx-p-6 somx-border somx-border-gray-200 somx-mx-auto">
    <div className="somx-grid somx-grid-cols-2 somx-gap-4 somx-text-sm">
      <div>
        <h2 className="somx-text-lg somx-font-bold somx-mb-4">Subir Factura</h2>
        <p>Sube tu factura XML, tus datos serán validados de acuerdo a lo proporcionado</p>
        <div className="somx-flex">
          <GenericInput
            label="Serie de la factura"
            placeholder="00000"
            value={series}
            onChange={(event: ChangeEvent<HTMLInputElement>) => setSeries(event.target.value)}
          />
        </div>
        <div className="somx-flex">
          <GenericInput
            label="UUID de la factura"
            placeholder="Escribe la serie UUID"
            value={uuid}
            onChange={(event: ChangeEvent<HTMLInputElement>) => setUuid(event.target.value)}
          />
        </div>
        <div className="somx-grid somx-grid-cols-2 somx-gap-2">
          {isValidating ? (
            <GenericLinearProgress />
          ) : (
            <label className="somx-mb-2 somx-mt-2 somx-block somx-w-full somx-cursor-pointer somx-border-2 somx-border-dashed somx-border-gray-300 somx-rounded-lg somx-p-6 somx-text-center somx-hover:border-blue-400 somx-transition">
              <input
                type="file"
                accept=".xml"
                className="somx-hidden"
                onChange={handleFileChange}
              />
              <p className="somx-text-gray-500">Subir XML de la factura (Requerido)</p>
              {fileXML && (
                <p className="somx-mt-2 somx-text-blue-600 somx-font-semibold">{fileXML.name}</p>
              )}
            </label>
          )}
          <label className="somx-mb-2 somx-mt-2 somx-block somx-w-full somx-cursor-pointer somx-border-2 somx-border-dashed somx-border-gray-300 somx-rounded-lg somx-p-6 somx-text-center somx-hover:border-blue-400 somx-transition">
            <input
              type="file"
              accept=".pdf"
              className="somx-hidden"
              onChange={handleFilePDFChange}
            />
            <p className="somx-text-gray-500">Subir PDF de la factura (opcional)</p>
            {filePDF && (
              <p className="somx-mt-2 somx-text-blue-600 somx-font-semibold">{filePDF.name}</p>
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
        <div>
          <div className="somx-bg-gray-50 somx-rounded-lg somx-p-4 somx-shadow-inner">
            <div className="somx-text-sm">
              <table className="somx-border somx-border-gray-300 somx-w-full">
                <tbody>
                  <tr>
                    <td className="somx-font-medium somx-p-2">RFC Emisor:</td>
                    <td className="somx-p-2">{invoiceData.rfcEmisor}</td>
                  </tr>
                  <tr>
                    <td className="somx-font-medium somx-p-2">Nombre Proveedor:</td>
                    <td className="somx-p-2">{invoiceData.nombreProveedor}</td>
                  </tr>
                  <tr>
                    <td className="somx-font-medium somx-p-2">Serie:</td>
                    <td className="somx-p-2">{invoiceData.serie}</td>
                  </tr>
                  <tr>
                    <td className="somx-font-medium somx-p-2">UUID:</td>
                    <td className="somx-p-2">{invoiceData.uuid}</td>
                  </tr>
                  <tr>
                    <td className="somx-font-medium somx-p-2">Folio:</td>
                    <td className="somx-p-2">{invoiceData.folio}</td>
                  </tr>
                  <tr>
                    <td className="somx-font-medium somx-p-2">Monto:</td>
                    <td className="somx-p-2">{formatAmount(parseFloat(invoiceData.monto))}</td>
                  </tr>
                  <tr>
                    <td className="somx-font-medium somx-p-2">Fecha Timbrado:</td>
                    <td className="somx-p-2">{formatDate(invoiceData.fechaTimbrado, true)}</td>
                  </tr>
                  <tr>
                    <td className="somx-font-medium somx-p-2">Uso CFDI:</td>
                    <td className="somx-p-2">{invoiceData.usoCfdi}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div className="somx-flex somx-flex-end">
              {!isProcessing && <GenericLinearProgress />}
              <GenericButton
                disabled={isFinished || isProcessing}
                className="somx-mt-2"
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
