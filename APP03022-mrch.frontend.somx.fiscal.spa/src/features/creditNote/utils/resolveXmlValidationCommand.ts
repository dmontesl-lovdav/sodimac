import type { CreditNoteXmlData } from "../parts/types";
import { getXmlValidationMessage } from "../parts/parseValidatedXml";

export type XmlValidationCommand = {
  isValid: boolean;
  dataMsg: string;
  relatedInvoiceUuid: string;
  alert: string | null;
};

export function resolveXmlValidationCommand(data: unknown, parsed: CreditNoteXmlData): XmlValidationCommand {
  if (parsed.tipoDeComprobante !== "E") {
    return { isValid: false, dataMsg: "", relatedInvoiceUuid: "", alert: "El archivo XML no corresponde a una nota de crédito válida. Por favor, valida el documento antes de continuar." };
  }
  const validation = getXmlValidationMessage(data);
  if (validation.ok) {
    const missingRelated = !parsed.uuidRelacionado.trim();
    return {
      isValid: true,
      dataMsg: validation.message,
      relatedInvoiceUuid: parsed.uuidRelacionado,
      alert: missingRelated ? "No se encontró la factura relacionada, verifique el archivo de la nota de crédito." : null,
    };
  }
  return { isValid: false, dataMsg: "", relatedInvoiceUuid: "", alert: validation.message };
}
