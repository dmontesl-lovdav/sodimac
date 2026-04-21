import { useState, useEffect, useCallback } from "react";
import DataGrid, { DataGridColumn, RowAction } from "@/shared/components/ui/datagrid/DataGrid";
import { formatDate, formatAmount, fetchProvidersAsCatalog, fetchCatalog } from "@/utils/utils";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { ReusableFiltersBar, FilterField } from "@/shared/components/ui/filters";
import { createComplementPaymentClient } from "./api/ComplementPaymentClient";
import { ComplementPaymentFilters, EMPTY_COMPLEMENT_PAYMENT, type ComplementPayment, type PaymentHeaderData } from "./interfaces";
import { Divider, Title } from "@/shared/components/ui/misc";
import { createCreditsClient } from "../creditNote/api/CreditsClient";
import viewIcon from "@assets/eye-show.svg";
import type { SelectableOption } from "@/utils/utils";

const breadcrumb: BreadcrumbItem[] = [
  { label: "Home", to: "/" },
  { label: "Fiscal", to: "/" },
  { label: "Consulta complemento pago" },
];

const columns: DataGridColumn<ComplementPayment>[] = [
  { header: "Serie", accessor: r => r.series ?? "--", exportAccessor: r => r.series },
  { header: "Folio", accessor: r => r.folio ?? "--", exportAccessor: r => r.folio },
  { header: "UUID", accessor: r => r.paymentsUuid ?? "--", exportAccessor: r => r.paymentsUuid },
  { header: "Total", accessor: r => r.totalAmount != null ? formatAmount(r.totalAmount) : "--", exportAccessor: r => r.totalAmount },
  { header: "Fecha de Emisión", accessor: r => r.createdAt ? formatDate(r.createdAt) : "N/D", exportAccessor: r => r.createdAt },
  { header: "RFC Emisor", accessor: r => r.issuerRfc ?? "--", exportAccessor: r => r.issuerRfc },
  { header: "Nombre Emisor", accessor: r => r.issuerName ?? "--", exportAccessor: r => r.issuerName },
  { header: "Fecha de Pago", accessor: r => r.paymentDate ? formatDate(r.paymentDate) : "N/D", exportAccessor: r => r.paymentDate },
  { header: "Facturas Relacionadas", accessor: r => r.relatedDocumentsCount ?? "--", exportAccessor: r => r.relatedDocumentsCount },
  { header: "Estatus", accessor: r => r.statusDescription ?? "--", exportAccessor: r => r.statusDescription },
 
  ];

export default function ComplementContainer() {
  const [filters, setFilters] = useState<ComplementPaymentFilters>(EMPTY_COMPLEMENT_PAYMENT);
  const [filtersReady, setFiltersReady] = useState(false);
  const [providers, setProviders] = useState<SelectableOption[]>([]);
  const [statusPaymentComplement, setStatusPaymentComplement] = useState<SelectableOption[]>([]);
  const client = createComplementPaymentClient();
  const invoiceClient = createCreditsClient();

  const rowActions: RowAction<ComplementPayment>[] = [
    {
      title: "Ver facturas relacionadas",
      icon: viewIcon,
      onClick: (row, nav) => nav(`/fiscal/complemento/${encodeURIComponent(row.paymentsUuid)}`),
      isDisabled: (row) => !row.relatedDocumentsCount || row.relatedDocumentsCount <= 0,
    }
  ];

  const handleGetXmlContent = useCallback(async (row: ComplementPayment) => {
    const { data } = await invoiceClient.getXmlDocument(row.paymentsUuid);
    return data;
  }, [invoiceClient]);

  const handleFetch = useCallback(async (f: ComplementPaymentFilters) => {
    const result = await client.getComplementPayments(f);
    return {
      content: result?.content ?? [],
      totalElements: result?.totalElements ?? 0,
      totalPages: result?.totalPages ?? 0,
      page: result?.page ?? f.page,
    };
  }, [client]);

  const areFiltersEmpty = useCallback((f: ComplementPaymentFilters) => {
    const v = (x: any) => (x == null || x === "") || (typeof x === "string" && !x.trim());
    return v(f?.fechaPagoInicio) && v(f?.fechaPagoFin) && v(f?.rfcEmisor) && v(f?.status) && v(f?.serie) && v(f?.folio) && v(f?.uuid);
  }, []);

  const fetchFnOrEmpty = useCallback(
    async (f: ComplementPaymentFilters) =>
      areFiltersEmpty(f)
        ? { content: [] as ComplementPayment[], totalElements: 0, totalPages: 0, page: 0 }
        : handleFetch(f),
    [handleFetch, areFiltersEmpty]
  );

  useEffect(() => {
    const fetchProviders = async () => {
      const response = await fetchProvidersAsCatalog();
      if (response) {
        setProviders(response);
      }
    };
    fetchProviders();
    const fetchStatus = async () => {
      const options = await fetchCatalog("CatEstatusPago");
      if(options){
        const mappedOptions = options.details.map((opt: any) => ({ label: opt.description, value: String(opt.internalStatus) }));
        setStatusPaymentComplement(mappedOptions);
      }
    };
    fetchStatus();
  }, []);

  const handleSearch = (newFilters: ComplementPaymentFilters) => {
    setFilters(newFilters);
  };

  const filterFields: FilterField[] = [
    {
      key: "fechaPago",
      label: "Rango de fechas de pago",
      type: "dateRange",
    },
    {
      key: "rfcEmisor",
      label: "Proveedor",
      type: "select",
      options: providers
    },
    {
      key: "status",
      label: "Estatus",
      type: "select",
      options: statusPaymentComplement,
      placeholder: "Seleccione estatus",
    },
    {
      key: "serie",
      label: "Serie",
      type: "text",
      placeholder: "XXXX000XXX",
    },
    {
      key: "folio",
      label: "Folio",
      type: "text",
      placeholder: "000000",
    },
    {
      key: "uuid",
      label: "UUID",
      type: "text",
      placeholder: "000000-0000-0000",
    },
  ];

  return decorate(
    breadcrumb,
    "/",
    <div>
      <Title title="Consulta complemento pago" description="Consulta el historial de complementos publicados y su estatus de validación. "  />
      <ReusableFiltersBar<ComplementPaymentFilters>
        fields={filterFields}
        initialFilters={EMPTY_COMPLEMENT_PAYMENT}
        onSearch={handleSearch}
        onHydrated={(f) => {
          setFilters(f);
          setFiltersReady(true);
        }}
        storageKey="complementPaymentFilters"
      />
      <Divider />
      {filtersReady && (
        <DataGrid<ComplementPayment, ComplementPaymentFilters>
          columns={columns}
          getRowId={r => r.paymentsUuid}
          fetchFn={fetchFnOrEmpty}
          filters={filters}
          initialPage={0}
          initialSize={10}
          selectable
          enableCsv
          csvFilename={`Complementos de pago ${formatDate(new Date().toString(), true)}`}
          enableXml
          enablePdf
          getXmlContent={handleGetXmlContent}
          rowActions={rowActions}
          filtersEmpty={areFiltersEmpty(filters)}
        />
      )}
    </div>
  );
}
