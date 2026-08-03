import { useState, useEffect, useCallback, useRef } from "react";
import DataGrid, { DataGridColumn, RowAction, type DataGridHandle } from "@/shared/components/ui/datagrid/DataGrid";
import { APP_EVENT, PermissionGate, useSecurityContext } from "@shared/security";
import { formatDate, formatAmount, fetchCatalogDetails, fetchCatalogAsSelectableOptions, SelectableOption } from "@/utils/utils";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { ReusableFiltersBar, FilterField } from "@/shared/components/ui/filters";
import { createComplementPaymentClient } from "./api/ComplementPaymentClient";
import { ComplementPaymentFilters, EMPTY_COMPLEMENT_PAYMENT, type ComplementPayment } from "./interfaces";
import { Divider, Title, ExportCsvButton } from "@/shared/components/ui/misc";
import { createCreditsClient } from "../creditNote/api/CreditsClient";
import viewIcon from "@assets/eye-show.svg";
import {
  FISCAL_LIST_KEYS,
  saveFiscalListFilters,
  useFiscalListRefetchOnReturn,
  useFiscalListScreenSession,
} from "@/shared/session/fiscalListSession";

const breadcrumb: BreadcrumbItem[] = [
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
  const returningFromDetail = useFiscalListScreenSession(
    FISCAL_LIST_KEYS.complementPayments
  );
  const [filters, setFilters] = useState<ComplementPaymentFilters>(EMPTY_COMPLEMENT_PAYMENT);
  const [filtersReady, setFiltersReady] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [statusPaymentComplement, setStatusPaymentComplement] = useState<{ label: string; value: string }[]>([]);
  const [providerTypeOptions, setProviderTypeOptions] = useState<SelectableOption<string>[]>([]);
  const client = createComplementPaymentClient();
  const invoiceClient = createCreditsClient();
  const gridRef = useRef<DataGridHandle>(null);
  const [canExportCsv, setCanExportCsv] = useState(false);
  const { can } = useSecurityContext();

  const rowActionDescriptors: { gate: { app: string; event: string }; action: RowAction<ComplementPayment> }[] = [
    {
      gate: APP_EVENT.PAYMENT_COMPLEMENTS.VIEW_DETAIL,
      action: {
        title: "Ver facturas relacionadas",
        icon: viewIcon,
        onClick: (row, nav) => nav(`/fiscal/complemento/${encodeURIComponent(row.paymentsUuid)}`),
        isDisabled: (row) => !row.relatedDocumentsCount || row.relatedDocumentsCount <= 0,
      },
    },
  ];
  const rowActions: RowAction<ComplementPayment>[] = rowActionDescriptors
    .filter(({ gate }) => can(gate))
    .map(({ action }) => action);

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
    const v = (x: unknown) => x == null || x === "" || (typeof x === "string" && !x.trim());
    return (
      v(f?.fechaPagoInicio) &&
      v(f?.fechaPagoFin) &&
      v(f?.numeroProveedor) &&
      v(f?.status) &&
      v(f?.serie) &&
      v(f?.folio) &&
      v(f?.uuid)
    );
  }, []);

  const fetchFnOrEmpty = useCallback(
    async (f: ComplementPaymentFilters) =>
      areFiltersEmpty(f)
        ? { content: [] as ComplementPayment[], totalElements: 0, totalPages: 0, page: 0 }
        : handleFetch(f),
    [handleFetch, areFiltersEmpty]
  );

  useEffect(() => {
    const fetchStatus = async () => {
      const statusCatalog = await fetchCatalogDetails("CATESTATUSCOMPLEMENTO");
      if(statusCatalog){
        setStatusPaymentComplement(fetchCatalogAsSelectableOptions(statusCatalog, "Todos los estados"));
      }
    };
    fetchStatus();
  }, []);

  

  useEffect(() => {
    const fetchProviderType = async () => {
      const options = await fetchCatalogDetails("CatTipoProveedor");
      if (options) {
        setProviderTypeOptions(fetchCatalogAsSelectableOptions(options, "Todos los tipos"));
      }
    }
    fetchProviderType();
  }, []);

  const handleSearch = (newFilters: ComplementPaymentFilters) => {
    saveFiscalListFilters(FISCAL_LIST_KEYS.complementPayments.filters, newFilters);
    setFilters(newFilters);
    setHasSearched(true);
  };

  const handleFiltersChange = (newFilters: ComplementPaymentFilters) => {
    setFilters(newFilters);
    setHasSearched(false);
  };

  useFiscalListRefetchOnReturn<ComplementPaymentFilters>(
    FISCAL_LIST_KEYS.complementPayments,
    returningFromDetail,
    handleSearch
  );

  const filterFields: FilterField[] = [
    {
      key: "numeroProveedor",
      label: "Nombre Proveedor",
      type: "providerSelect",
    },
    {
      key: "serie",
      label: "Serie",
      type: "text",
    },
    {
      key: "folio",
      label: "Folio",
      type: "text",
    },
    {
      key: "uuid",
      label: "UUID",
      type: "uuid",
    },
    {
      key: "tipoProveedor",
      label: "Tipo Proveedor",
      type: "select",
      options: providerTypeOptions,
    },
    {
      key: "status",
      label: "Estado complemento de pago",
      type: "select",
      options: statusPaymentComplement,
    },
    {
      key: "fechaPago",
      label: "Fecha búsqueda",
      type: "dateRange",
    },
  ];

  return decorate(
    breadcrumb,
    "/",
    <div>
      <Title
        title="Consulta complemento pago"
        description="Consulta el historial de complementos publicados y su estatus de validación."
        actions={
          <PermissionGate appEvent={APP_EVENT.PAYMENT_COMPLEMENTS.DOWNLOAD_CSV}>
            <ExportCsvButton
              disabled={!canExportCsv}
              onClick={() => gridRef.current?.exportCsv()}
            />
          </PermissionGate>
        }
      />
      <ReusableFiltersBar<ComplementPaymentFilters>
        fields={filterFields}
        initialFilters={EMPTY_COMPLEMENT_PAYMENT}
        onSearch={handleSearch}
        onFiltersChange={handleFiltersChange}
        searchAppEvent={APP_EVENT.PAYMENT_COMPLEMENTS.SEARCH}
        clearAppEvent={APP_EVENT.PAYMENT_COMPLEMENTS.CLEAR_FILTERS}
        onHydrated={(f) => {
          setFilters(f);
          setFiltersReady(true);
        }}
        sessionFiltersKey={FISCAL_LIST_KEYS.complementPayments.filters}
        restoreSavedFilters={returningFromDetail}
      />
      <Divider />
      {filtersReady && (
        <DataGrid<ComplementPayment, ComplementPaymentFilters>
          ref={gridRef}
          columns={columns}
          getRowId={r => r.paymentsUuid}
          fetchFn={fetchFnOrEmpty}
          filters={filters}
          fetchEnabled={hasSearched}
          initialPage={0}
          initialSize={10}
          selectable
          enableCsv
          hideCsvToolbar
          onExportAvailabilityChange={setCanExportCsv}
          csvFilename={`Complementos de pago ${formatDate(new Date().toString(), true)}`}
          enableXml
          enablePdf
          xmlAppEvent={APP_EVENT.PAYMENT_COMPLEMENTS.DOWNLOAD_XML}
          pdfAppEvent={APP_EVENT.PAYMENT_COMPLEMENTS.DOWNLOAD_PDF}
          getXmlContent={handleGetXmlContent}
          rowActions={rowActions}
          filtersEmpty={!hasSearched || areFiltersEmpty(filters)}
        />
      )}
    </div>
  );
}
