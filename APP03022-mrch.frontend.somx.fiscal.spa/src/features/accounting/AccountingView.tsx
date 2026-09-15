import { useEffect, useMemo, useState } from "react";
import { useLocation } from "react-router-dom";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { Title, Divider } from "@/shared/components/ui/misc";
import { GenericTable } from "@/shared/components/ui/table";
import { GenericModal } from "@/shared/components/ui";
import { formatAmount, formatDate } from "@/utils/utils";
import {
  FISCAL_LIST_KEYS,
  useFiscalListReturnFromDetail,
} from "@/shared/session/fiscalListSession";
import { parseAccountingSearchParams } from "./accountingQuery";
import { createSapDocumentClient } from "./sapDocumentClient";
import { toAccountingDetailRow } from "./accountingApi";
import "../creditNote/parts/DiscountInfoGrid.css";

type Field = { label: string; value: string };

type DetailRow = {
  documentNumber: string;
  sapDocument: string;
  sapMessage: string;
  accountingDate: string;
};

const dash = (v: string): string => (v.trim() ? v : "--");

function formatMoney(raw: string): string {
  if (!raw.trim()) return "--";
  const n = Number(raw);
  return Number.isFinite(n) ? formatAmount(n) : dash(raw);
}

function formatMaybeDate(raw: string): string {
  if (!raw.trim()) return "--";
  return formatDate(raw);
}

export default function AccountingView() {
  const location = useLocation();
  const isCreditNote = location.pathname.includes("notas-credito");
  useFiscalListReturnFromDetail(
    isCreditNote ? FISCAL_LIST_KEYS.creditNotes : FISCAL_LIST_KEYS.invoices
  );

  const data = useMemo(
    () => parseAccountingSearchParams(location.search),
    [location.search]
  );

  const [detailRows, setDetailRows] = useState<DetailRow[]>([]);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    const fiscalUuid = data.fiscalUuid.trim();
    if (!fiscalUuid) {
      setDetailRows([]);
      return;
    }

    let cancelled = false;
    setLoading(true);
    setErrorMessage("");

    createSapDocumentClient()
      .listByFiscalUuid(fiscalUuid)
      .then((items) => {
        if (cancelled) return;
        setDetailRows(
          items.map((item) => {
            const row = toAccountingDetailRow(item);
            return {
              documentNumber: dash(row.documentNumber),
              sapDocument: dash(row.sapDocument),
              sapMessage: dash(row.sapMessage),
              accountingDate: formatMaybeDate(row.accountingDate),
            };
          })
        );
      })
      .catch((err: unknown) => {
        if (cancelled) return;
        setDetailRows([]);
        setErrorMessage(
          err instanceof Error ? err.message : "No fue posible consultar la contabilidad SAP."
        );
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [data.fiscalUuid]);

  const listPath = isCreditNote ? "/fiscal/notas-credito" : "/fiscal/facturas";
  const listLabel = isCreditNote ? "Notas de Crédito" : "Facturas";
  const breadcrumb: BreadcrumbItem[] = [
    { label: "Fiscal", to: "/" },
    { label: listLabel, to: listPath },
    { label: "Ver Contabilidad" },
  ];

  const headerFields: Field[] = [
    { label: "Serie", value: dash(data.series) },
    { label: "Folio", value: dash(data.folio) },
    { label: "UUID", value: dash(data.fiscalUuid) },
    { label: "Subtotal", value: formatMoney(data.subtotal) },
    { label: "OC", value: dash(data.noOrdenCompra) },
    { label: "Recepción", value: dash(data.noRecepcion) },
    { label: "Número de Proveedor", value: dash(data.numeroProveedor) },
    { label: "Nombre Proveedor", value: dash(data.supplierName) },
    { label: "Estado", value: dash(data.statusName) },
  ];

  return decorate(
    breadcrumb,
    listPath,
    <>
      <Title
        title="Ver Contabilidad"
        description={
          isCreditNote
            ? "Datos principales de la nota de crédito y su información contable."
            : "Datos principales de la factura y su información contable."
        }
      />
      <div className="pcn-discount-header-card">
        <div className="pcn-discount-header-top">
          <span className="pcn-discount-section-title">
            {isCreditNote ? "Datos de la nota de crédito" : "Datos de la factura"}
          </span>
        </div>
        <div className="pcn-discount-summary-grid">
          {headerFields.map((field) => (
            <div key={field.label} className="pcn-discount-summary-item">
              <div className="pcn-discount-label">{field.label}</div>
              <div>{field.value}</div>
            </div>
          ))}
        </div>
      </div>
      <Divider />
      <span className="pcn-discount-section-title">Detalle contable</span>
      <GenericTable
        rows={detailRows}
        emptyLabel="Sin información contable"
        columns={[
          { header: "Número de documento", render: (r) => r.documentNumber },
          { header: "Documento SAP", render: (r) => r.sapDocument },
          { header: "Mensaje SAP", render: (r) => r.sapMessage },
          { header: "Fecha Contable", render: (r) => r.accountingDate },
        ]}
        page={1}
        perPage={10}
        totalPages={1}
        totalItems={detailRows.length}
      />
      <GenericModal visible={loading} variant="loading" message="Consultando documentos SAP…" />
      <GenericModal
        visible={Boolean(errorMessage)}
        variant="alert"
        severity="error"
        title="Error"
        message={errorMessage}
        buttonText="Aceptar"
        onClose={() => setErrorMessage("")}
      />
    </>
  );
}
