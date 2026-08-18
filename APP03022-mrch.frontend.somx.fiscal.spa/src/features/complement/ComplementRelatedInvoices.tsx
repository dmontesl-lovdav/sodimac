import { useCallback, useEffect, useRef, useState } from "react";
import DataGrid, { DataGridColumn, type DataGridHandle } from "@/shared/components/ui/datagrid/DataGrid";
import { APP_EVENT, PermissionGate } from "@shared/security";
import { formatDate, formatAmount } from "@/utils/utils";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { createComplementPaymentClient } from "./api/ComplementPaymentClient";
import {
  ComplementPayment,
  ComplementPaymentFilters,
  RelatedInvoice,
} from "./interfaces";
import { Divider, Title, ExportCsvButton } from "@/shared/components/ui/misc";
import { useParams } from "react-router-dom";
import {
  FISCAL_LIST_KEYS,
  useFiscalListReturnFromDetail,
} from "@/shared/session/fiscalListSession";
import ComplementHeaderInfo from "./parts/ComplementHeaderInfo";

const breadcrumb: BreadcrumbItem[] = [
  { label: "Fiscal", to: "/" },
  { label: "Consulta complemento pago", to: "/fiscal/consulta-complemento-pago" },
  { label: "Facturas relacionadas" },
];

const columns: DataGridColumn<RelatedInvoice>[] = [
  { header: "Serie", accessor: r => r.series ?? "--", exportAccessor: r => r.series },
  { header: "Folio", accessor: r => r.folio ?? "--", exportAccessor: r => r.folio },
  {
    header: "UUID",
    accessor: r => r.fiscalUuid ?? r.documentUuid ?? "--",
    exportAccessor: r => r.fiscalUuid ?? r.documentUuid,
  },
  {
    header: "Total",
    accessor: r => (r.amountPaid != null ? formatAmount(r.amountPaid) : "--"),
    exportAccessor: r => r.amountPaid,
  },
  {
    header: "Balance Previo",
    accessor: r => (r.previousBalance != null ? formatAmount(r.previousBalance) : "--"),
    exportAccessor: r => r.previousBalance,
  },
];

export default function ComplementRelatedInvoices() {
  const { uuid } = useParams<{ uuid: string }>();
  useFiscalListReturnFromDetail(FISCAL_LIST_KEYS.complementPayments);
  const clientRef = useRef(createComplementPaymentClient());
  const client = clientRef.current;
  const gridRef = useRef<DataGridHandle>(null);
  const [canExportCsv, setCanExportCsv] = useState(false);
  const [header, setHeader] = useState<ComplementPayment | null>(null);
  const [headerError, setHeaderError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const loadHeader = async () => {
      if (!uuid) {
        setHeader(null);
        return;
      }
      try {
        const data = await client.getComplementByPaymentsUuid(uuid);
        if (!cancelled) {
          setHeader(data);
          setHeaderError(data ? null : "No se encontró el complemento de pago");
        }
      } catch {
        if (!cancelled) {
          setHeader(null);
          setHeaderError("No se pudo cargar la cabecera del complemento");
        }
      }
    };
    loadHeader();
    return () => {
      cancelled = true;
    };
  }, [uuid, client]);

  const handleGetXmlContent = useCallback(
    async (row: RelatedInvoice) => {
      const fiscalUuid = row.fiscalUuid;
      if (!fiscalUuid) {
        throw new Error("UUID fiscal no disponible para esta factura");
      }
      const { data } = await client.getXmlDocument(fiscalUuid);
      return data;
    },
    [client]
  );

  const getPdfUrl = useCallback(
    (row: RelatedInvoice) => {
      // PDF de factura/NC usa invoice_uuid interno
      if (!row.documentUuid) return null;
      return client.getInvoicePdfUrl(row.documentUuid);
    },
    [client]
  );

  const handleFetch = useCallback(async () => {
    if (!uuid) {
      return {
        content: [],
        totalElements: 0,
        totalPages: 0,
        page: 0,
      };
    }

    const result = await client.getRelatedInvoices(uuid);
    return {
      content: result?.content ?? [],
      totalElements: result?.totalElements ?? 0,
      totalPages: result?.totalPages ?? 0,
      page: result?.page ?? result?.number ?? 0,
    };
  }, [client, uuid]);

  return decorate(
    breadcrumb,
    "/fiscal/consulta-complemento-pago",
    <div>
      <Title
        title="Facturas relacionadas"
        description="Consulta las facturas relacionadas con el complemento de pago seleccionado."
        actions={
          <PermissionGate appEvent={APP_EVENT.PAYMENT_COMPLEMENTS.DOWNLOAD_CSV_DETAIL}>
            <ExportCsvButton
              disabled={!canExportCsv}
              onClick={() => gridRef.current?.exportCsv()}
            />
          </PermissionGate>
        }
      />

      {header && <ComplementHeaderInfo complement={header} />}
      {headerError && !header && (
        <p style={{ color: "#b45309", marginBottom: 12 }}>{headerError}</p>
      )}

      <Divider />
      <DataGrid<RelatedInvoice, ComplementPaymentFilters>
        ref={gridRef}
        columns={columns}
        getRowId={r => r.relatedDocumentUuid}
        fetchFn={handleFetch}
        filters={{ uuid }}
        fetchEnabled
        initialPage={0}
        initialSize={10}
        selectable
        enableCsv
        hideCsvToolbar
        onExportAvailabilityChange={setCanExportCsv}
        csvFilename={`Factura complemento ${uuid} ${formatDate(new Date().toString(), true)}`}
        enableXml
        enablePdf
        xmlAppEvent={APP_EVENT.PAYMENT_COMPLEMENTS.DOWNLOAD_XML}
        pdfAppEvent={APP_EVENT.PAYMENT_COMPLEMENTS.DOWNLOAD_PDF}
        getXmlContent={handleGetXmlContent}
        getPdfUrl={getPdfUrl}
        getFilename={row => row.fiscalUuid || row.documentUuid || "documento"}
        filtersEmpty={false}
      />
    </div>
  );
}
