import type { Invoice } from "../../invoice/interfaces";
import { isCommercialDiscountFlow } from "./publishQuery";
import type { PublishQuery } from "./types";

type BuildPublishFormDataArgs = {
  xmlFile: File;
  pdfFile: File | null;
  traceId: string;
  query: PublishQuery;
  relatedInvoice: Invoice | null;
};

export function buildPublishFormData({
  xmlFile,
  pdfFile,
  traceId,
  query,
  relatedInvoice,
}: BuildPublishFormDataArgs): FormData {
  const formData = new FormData();
  formData.append("file", xmlFile);
  if (pdfFile) formData.append("pdfFile", pdfFile);
  formData.append("idTransaccion", traceId);

  if (isCommercialDiscountFlow(query)) {
    formData.append("documentNumber", query.documentNumber);
    formData.append("supplierNumber", query.supplierNumber);
    formData.append("rebateId", query.rebateId);
    formData.append("tipoNotaCredito", "2");
    return formData;
  }

  formData.append("supplierNumber", relatedInvoice?.numeroProveedor ?? "");
  formData.append("tipoNotaCredito", "1");
  return formData;
}
