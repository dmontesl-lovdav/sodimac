import { useState, useEffect, useCallback } from "react";
import DataGrid, { DataGridColumn, RowAction } from "@/shared/components/ui/datagrid/DataGrid";
import { formatDate, formatAmount, fetchProvidersAsCatalog, fetchCatalog } from "@/utils/utils";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { ReusableFiltersBar, FilterField } from "@/shared/components/ui/filters";
import { createComplementPaymentClient } from "./api/ComplementPaymentClient";
import { ComplementPaymentFilters, EMPTY_COMPLEMENT_PAYMENT, RelatedInvoice, type ComplementPayment, type PaymentHeaderData } from "./interfaces";
import { Divider, Title } from "@/shared/components/ui/misc";
import { createCreditsClient } from "../creditNote/api/CreditsClient";
import viewIcon from "@assets/eye-show.svg";
import type { SelectableOption } from "@/utils/utils";
import { useParams } from "react-router-dom";

const breadcrumb: BreadcrumbItem[] = [
  { label: "Home", to: "/" },
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

  const rowActions: RowAction<RelatedInvoice>[] = [
    {
      title: "Ver factura relacionada",
      icon: viewIcon,
      onClick: (row, nav) => nav(`/fiscal/complemento/${encodeURIComponent(row.relatedDocumentUuid)}`),
    }
  ];

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
    "/",
    <div>
      <Title title="Facturas relacionadas" description="Consulta las facturas relacionadas con el complemento de pago seleccionado."  />
      
      <Divider />
      <DataGrid<RelatedInvoice, ComplementPaymentFilters>
          columns={columns}
          getRowId={r => r.relatedDocumentUuid}
          fetchFn={handleFetch}
          filters={{ uuid }}
          initialPage={0}
          initialSize={10}
          selectable
          enableCsv
          csvFilename={`Factura complemento ${uuid} ${formatDate(new Date().toString(), true)}`}
          enableXml
          enablePdf
          rowActions={rowActions}
          filtersEmpty={false}
        />
    </div>
  );
}
