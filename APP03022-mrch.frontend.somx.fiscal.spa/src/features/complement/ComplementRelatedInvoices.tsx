import { useCallback, useRef, useState } from "react";
import DataGrid, { DataGridColumn, RowAction, type DataGridHandle } from "@/shared/components/ui/datagrid/DataGrid";
import { APP_EVENT, PermissionGate, useSecurityContext } from "@shared/security";
import { formatDate, formatAmount } from "@/utils/utils";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { createComplementPaymentClient } from "./api/ComplementPaymentClient";
import { ComplementPaymentFilters, RelatedInvoice } from "./interfaces";
import { Divider, Title, ExportCsvButton } from "@/shared/components/ui/misc";
import viewIcon from "@assets/eye-show.svg";
import { useParams } from "react-router-dom";
import {
  FISCAL_LIST_KEYS,
  useFiscalListReturnFromDetail,
} from "@/shared/session/fiscalListSession";

const breadcrumb: BreadcrumbItem[] = [
  { label: "Fiscal", to: "/" },
  { label: "Consulta complemento pago", to: "/fiscal/consulta-complemento-pago" },
  { label: "Facturas relacionadas", to: "/fiscal/complemento/:uuid" },
];

const columns: DataGridColumn<RelatedInvoice>[] = [
  { header: "Serie", accessor: r => r.series ?? "--", exportAccessor: r => r.series },
  { header: "Folio", accessor: r => r.folio ?? "--", exportAccessor: r => r.folio },
  { header: "UUID", accessor: r => r.relatedDocumentUuid ?? "--", exportAccessor: r => r.relatedDocumentUuid },
  { header: "Total", accessor: r => r.amountPaid != null ? formatAmount(r.amountPaid) : "--", exportAccessor: r => r.amountPaid },
  { header: "Balance Previo", accessor: r => r.previousBalance != null ? formatAmount(r.previousBalance) : "--", exportAccessor: r => r.previousBalance },
];

export default function ComplementRelatedInvoices() {
 const client = createComplementPaymentClient();
const { uuid } = useParams<{ uuid: string }>();
  useFiscalListReturnFromDetail(FISCAL_LIST_KEYS.complementPayments);
  const gridRef = useRef<DataGridHandle>(null);
  const [canExportCsv, setCanExportCsv] = useState(false);
  const { hasEvent } = useSecurityContext();

  const rowActionDescriptors: { gate: { app: string; event: string }; action: RowAction<RelatedInvoice> }[] = [
    {
      gate: APP_EVENT.PAYMENT_COMPLEMENTS.VIEW_DETAIL,
      action: {
        title: "Ver factura relacionada",
        icon: viewIcon,
        onClick: (row, nav) => nav(`/fiscal/complemento/${encodeURIComponent(row.relatedDocumentUuid)}`),
      },
    },
  ];
  const rowActions: RowAction<RelatedInvoice>[] = rowActionDescriptors
    .filter(({ gate }) => hasEvent(gate.app, gate.event))
    .map(({ action }) => action);

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
      page: result?.page ?? 0,
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
          rowActions={rowActions}
          filtersEmpty={false}
        />
    </div>
  );
}
