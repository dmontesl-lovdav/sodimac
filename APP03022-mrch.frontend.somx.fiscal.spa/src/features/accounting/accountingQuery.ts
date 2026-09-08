export type AccountingViewPayload = {
  series: string;
  folio: string;
  fiscalUuid: string;
  subtotal: string;
  noOrdenCompra: string;
  noRecepcion: string;
  numeroProveedor: string;
  supplierName: string;
  statusName: string;
  documentNumber: string;
  sapDocument: string;
  sapMessage: string;
  accountingDate: string;
};

type AccountingSource = {
  series?: string | null;
  folio?: string | null;
  fiscalUuid?: string | null;
  subtotal?: number | string | null;
  noOrdenCompra?: string | null;
  noRecepcion?: string | null;
  numeroProveedor?: string | number | null;
  supplierName?: string | null;
  statusName?: string | null;
  documentNumber?: string | null;
  sapDocument?: string | null;
  sapMessage?: string | null;
  accountingDate?: string | null;
};

function setIfPresent(p: URLSearchParams, key: string, value: unknown): void {
  if (value === undefined || value === null || value === "") return;
  p.set(key, String(value));
}

export function buildAccountingSearchParams(row: AccountingSource): URLSearchParams {
  const p = new URLSearchParams();
  setIfPresent(p, "series", row.series);
  setIfPresent(p, "folio", row.folio);
  setIfPresent(p, "uuid", row.fiscalUuid);
  setIfPresent(p, "subtotal", row.subtotal);
  setIfPresent(p, "oc", row.noOrdenCompra);
  setIfPresent(p, "recepcion", row.noRecepcion);
  setIfPresent(p, "numeroProveedor", row.numeroProveedor);
  setIfPresent(p, "nombreProveedor", row.supplierName);
  setIfPresent(p, "estado", row.statusName);
  setIfPresent(p, "documentNumber", row.documentNumber);
  setIfPresent(p, "sapDocument", row.sapDocument);
  setIfPresent(p, "sapMessage", row.sapMessage);
  setIfPresent(p, "accountingDate", row.accountingDate);
  return p;
}

function g(params: URLSearchParams, key: string): string {
  return params.get(key)?.trim() ?? "";
}

export function parseAccountingSearchParams(search: string): AccountingViewPayload {
  const params = new URLSearchParams(search);
  return {
    series: g(params, "series"),
    folio: g(params, "folio"),
    fiscalUuid: g(params, "uuid"),
    subtotal: g(params, "subtotal"),
    noOrdenCompra: g(params, "oc"),
    noRecepcion: g(params, "recepcion"),
    numeroProveedor: g(params, "numeroProveedor"),
    supplierName: g(params, "nombreProveedor"),
    statusName: g(params, "estado"),
    documentNumber: g(params, "documentNumber"),
    sapDocument: g(params, "sapDocument"),
    sapMessage: g(params, "sapMessage"),
    accountingDate: g(params, "accountingDate"),
  };
}
