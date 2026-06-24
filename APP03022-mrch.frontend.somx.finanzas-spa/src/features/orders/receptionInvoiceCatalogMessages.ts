import { fetchCatalogDetailMessage } from "@/utils/utils";

export const RECEPTION_INVOICE_MSG_RFC_MISMATCH =
    "El RFC del proveedor no coincide con la factura publicada. Por favor, valida el archivo XML.";

const INVALID_INVOICE_TYPE_FALLBACK =
    "El archivo XML no corresponde a una factura válida. Por favor, valida el documento antes de continuar.";

/** CatMsgAdvertencia — XML con tipo de comprobante distinto de I. */
const INVALID_INVOICE_TYPE_CATALOG = "CatMsgAdvertencia";
const INVALID_INVOICE_TYPE_KEY = "WRN7018";

let cachedInvalidInvoiceTypeMessage: string | null = null;

export async function getInvalidInvoiceTypeMessage(): Promise<string> {
    if (cachedInvalidInvoiceTypeMessage) {
        return cachedInvalidInvoiceTypeMessage;
    }
    cachedInvalidInvoiceTypeMessage = await fetchCatalogDetailMessage(
        INVALID_INVOICE_TYPE_CATALOG,
        INVALID_INVOICE_TYPE_KEY,
        INVALID_INVOICE_TYPE_FALLBACK
    );
    return cachedInvalidInvoiceTypeMessage;
}