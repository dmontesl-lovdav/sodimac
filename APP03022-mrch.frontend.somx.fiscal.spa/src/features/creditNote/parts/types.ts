export type CreditNoteXmlData = {
  rfcEmisor: string;
  nombreProveedor: string;
  serie: string;
  folio: string;
  monto: string;
  fechaTimbrado: string;
  usoCfdi: string;
  tipoDeComprobante: string;
  uuid: string;
  uuidRelacionado: string;
  formaPago: string;
};

export type PublishQuery = {
  supplierNumber: string;
  documentNumber: string;
};

/** Respuesta directa de POST invoices/register (publicar NC). */
export type PublishCreditNoteResponse = {
  code?: string;
  message?: string;
  success?: boolean;
  invoiceUuid?: string;
  fiscalUuid?: string;
  series?: string | null;
  folio?: string | null;
  documentType?: string;
  issuerRfc?: string;
  receiverRfc?: string;
  total?: string;
  issueDate?: string;
  hasAddenda?: boolean;
  pendingAddenda?: boolean;
  warnings?: string[];
  processedAt?: string;
};

export type FinishModalState = {
  severity: "success" | "warning" | "error";
  title: string;
  message: string;
};
