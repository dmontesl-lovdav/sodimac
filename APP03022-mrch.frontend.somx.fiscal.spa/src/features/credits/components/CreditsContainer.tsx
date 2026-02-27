
import { useState } from "react";
import DataGrid, { DataGridColumn, createOnFilter } from "@/shared/components/ui/datagrid/DataGrid";
import { formatDate, formatAmount } from "@/utils/utils";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import FiltersBar from "./parts/FiltersBar";
import { CreditsClient } from "../api/CreditsClient";
import { CreditNoteFilters, EMPTY_CREDIT_NOTE, type CreditNote } from "../interfaces";
import { Divider, Title } from "@/shared/components/ui/misc";

const breadcrumb: BreadcrumbItem[] = [
  { label: "Home", to: "/" },
  { label: "Fiscal", to: "/" },
  { label: "Notas de Crédito" },
];

const columns: DataGridColumn<CreditNote>[] = [
  { header: "UUID Fiscal", accessor: r => r.fiscalUuid ?? "--", exportAccessor: r => r.fiscalUuid },
  { header: "Serie", accessor: r => r.series ?? "--", exportAccessor: r => r.series },
  { header: "Folio", accessor: r => r.folio ?? "--", exportAccessor: r => r.folio },
  { header: "Subtotal", accessor: r => r.subtotal != null ? formatAmount(r.subtotal) : "--", exportAccessor: r => r.subtotal },
  { header: "Total", accessor: r => r.total != null ? formatAmount(r.total) : "--", exportAccessor: r => r.total },
  { header: "UUID Factura", accessor: r => r.invoiceUuid, exportAccessor: r => r.invoiceUuid },
  { header: "Tipo Documento", accessor: r => r.documentType ?? "--", exportAccessor: r => r.documentType },
  { header: "Tipo Documento", accessor: r => r.documentType ?? "--", exportAccessor: r => r.documentType },
  { header: "Nombre Proveedor", accessor: r => r.supplierName ?? "--", exportAccessor: r => r.supplierName },
  { header: "Tipo Proveedor", accessor: r => r.tipoProveedor ?? "--", exportAccessor: r => r.tipoProveedor },
  { header: "Fecha Emisión", accessor: r => r.issueDate ? formatDate(r.issueDate) : "N/D", exportAccessor: r => r.issueDate },
  { header: "Fecha Certificación", accessor: r => r.certificationDate ? formatDate(r.certificationDate) : "N/D", exportAccessor: r => r.certificationDate },
  { header: "Estado", accessor: r => r.statusName ?? "--", exportAccessor: r => r.statusName },
];

export default function CreditsGrid() {
  const [loading, setLoading] = useState<boolean>(false);
  const [initialFilters, setInitialFilters] = useState<CreditNoteFilters>(EMPTY_CREDIT_NOTE);
  const [rows, setRows] = useState<CreditNote[]>([]);
  const client = new CreditsClient();

  const onFilter = createOnFilter<CreditNote, any>({
    fetchFn: (filters) => client.getCreditNotes(filters),
    pickRows: (res) => res?.content ?? [],
    setLoading,
    setRows,
    onError: (err) => console.error("Error al obtener notas de crédito:", err),
  });

  return decorate(
    breadcrumb,
    "/",
    <>
      <Title title="Listado de Notas de Crédito" />
      <FiltersBar onSearch={(filters)=>{
        onFilter(filters);
        setInitialFilters(filters);
      }} initialFilters={initialFilters} />
      <Divider />
      <DataGrid<CreditNote>
        rows={rows}
        loading={loading}
        columns={columns}
        getRowId={r => r.invoiceUuid}
        selectable
        enableCsv
        csvFilename="Notas de Crédito"
        enableXml
        enablePdf
      />
    </>,
    loading
  );
}
