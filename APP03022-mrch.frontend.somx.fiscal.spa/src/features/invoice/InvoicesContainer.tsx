
import { useState, useEffect, useMemo, useCallback, useRef } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import DataGrid, { DataGridColumn, RowAction, type DataGridHandle } from "@/shared/components/ui/datagrid/DataGrid";
import { APP_EVENT, PermissionGate, useSecurityContext } from "@shared/security";
import { formatDate, formatAmount, fetchCatalogMessage, fetchCatalogDetails, getXmlFileNameFromRow, fetchCatalogAsSelectableOptions, getErrorMessage } from "@/utils/utils";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { ReusableFiltersBar, FilterField } from "@/shared/components/ui/filters";
import { GenericModal } from "@/shared/components/ui";
import { createInvoicesClient } from "./api/InvoiceClient";
import deleteIcon from "@/assets/delete.svg";
import viewIcon from "@/assets/eye-show.svg";
import reprocessIcon from "@/assets/reprocess.svg";
import {
  InvoiceFilters,
  EMPTY_INVOICE,
  INVOICE_STATUS_RECHAZO_CONTABLE,
  INVOICE_PENDIENTE_ADDENDA,
  INVOICE_RECIBIDO_PARCIAL,
  type Invoice,
  INVOICE_WRONG_DATA,
  INVOICE_ERROR_DATA,
  INVOICE_PROCESS_SENDED,
} from "./interfaces";
import { Divider, Title, ExportCsvButton } from "@/shared/components/ui/misc";
import {
  FISCAL_LIST_KEYS,
  saveFiscalListFilters,
  useFiscalListRefetchOnReturn,
  useFiscalListScreenSession,
} from "@/shared/session/fiscalListSession";

const breadcrumb: BreadcrumbItem[] = [
  { label: "Fiscal", to: "/" },
  { label: "Facturas" },
];

const columns: DataGridColumn<Invoice>[] = [
  { header: "Serie", accessor: r => r.series ?? "--", exportAccessor: r => r.series },
  { header: "Folio", accessor: r => r.folio ?? "--", exportAccessor: r => r.folio },
  { header: "Subtotal", accessor: r => r.subtotal != null ? formatAmount(r.subtotal) : "--", exportAccessor: r => r.subtotal },
  { header: "Total", accessor: r => r.total != null ? formatAmount(r.total) : "--", exportAccessor: r => r.total },
  { header: "UUID", accessor: r => r.fiscalUuid ?? "--", exportAccessor: r => r.fiscalUuid },
  { header: "Orden Compra", accessor: r => r.noOrdenCompra ?? "--", exportAccessor: r => r.noOrdenCompra },
  { header: "Recepcion", accessor: r => r.noRecepcion ?? "--", exportAccessor: r => r.noRecepcion },
  {
    header: "NC Relacionadas",
    accessor: r =>
      r.notasCreditoRelacionadas.length === 0
        ? "0"
        : String(r.notasCreditoRelacionadas.length),
    exportAccessor: r =>
      r.notasCreditoRelacionadas.length === 0
        ? "0"
        : String(r.notasCreditoRelacionadas.length),
  },
  { header: "Tipo Proveedor", accessor: r => r.tipoProveedorDescripcion ?? "--", exportAccessor: r => r.tipoProveedorDescripcion },
  { header: "Número Proveedor", accessor: r => r.numeroProveedor ?? "--", exportAccessor: r => r.numeroProveedor },
  { header: "Nombre Proveedor", accessor: r => r.supplierName ?? "--", exportAccessor: r => r.supplierName },
  { header: "Fecha Emisión", accessor: r => r.issueDate ? formatDate(r.issueDate) : "N/D", exportAccessor: r => r.issueDate },
  { header: "Fecha Recepción", accessor: r => r.createdAt ? formatDate(r.createdAt) : "N/D", exportAccessor: r => r.certificationDate },
  { header: "Estado", accessor: r => r.statusName ?? "--", exportAccessor: r => r.statusName },
];

const MSG_REPROCESO_DEFAULT = "¿Desea volver a procesar esta factura para intentar contabilizarla nuevamente? Esta acción reemplazará el intento anterior.";

export default function InvoicesGrid() {
  const location = useLocation();
  const navigate = useNavigate();
  const returningFromDetail = useFiscalListScreenSession(FISCAL_LIST_KEYS.invoices);

  const customFilters = useMemo(() => {
    const params = new URLSearchParams(location.search);
    const filtersOnUrl: Partial<InvoiceFilters> = {
      uuid: params.get("uuid") || undefined,
      fechaInicioRecepcion: params.get("start") || undefined,
      fechaFinalRecepcion: params.get("end") || undefined,
    };
    return filtersOnUrl;
  }, [location.search]);

  const initialFilters = useMemo<InvoiceFilters>(() => {
    return {
      ...EMPTY_INVOICE,
      ...customFilters,
    };
  }, [customFilters]);

  const [filters, setFilters] = useState<InvoiceFilters>(EMPTY_INVOICE);
  const [filtersReady, setFiltersReady] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [statusInvoices, setStatusInvoices] = useState<{ label: string; value: string }[]>([]);
  const [providerTypeOptions, setProviderTypeOptions] = useState<
    { label: string; value: string }[]
  >([]);
  const [reprocessConfirmOpen, setReprocessConfirmOpen] = useState(false);
  const [reprocessConfirmRow, setReprocessConfirmRow] = useState<Invoice | null>(null);
  const [reprocessConfirmMessage, setReprocessConfirmMessage] = useState(MSG_REPROCESO_DEFAULT);
  const [cancelConfirmOpen, setCancelConfirmOpen] = useState(false);
  const [cancelConfirmRow, setCancelConfirmRow] = useState<Invoice | null>(null);
  const [cancelConfirmMessage, setCancelConfirmMessage] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [processLoading, setProcessLoading] = useState(false);
  const client = createInvoicesClient<{
    content: Invoice[];
    totalElements: number;
    totalPages: number;
    page: number;
  }>();
  const gridRef = useRef<DataGridHandle>(null);
  const deepLinkSearchedRef = useRef<string | null>(null);
  const [canExportCsv, setCanExportCsv] = useState(false);
  const { hasEvent } = useSecurityContext();

  useEffect(() => {
    const loadCatalogs = async () => {
      const [statusCatalog, tipoProveedorCatalog ] = await Promise.all([
        fetchCatalogDetails("CatEstatusFactura"),
        fetchCatalogDetails("CatTipoProveedor")
      ]);
      
      if (statusCatalog) {
        setStatusInvoices(
          fetchCatalogAsSelectableOptions(statusCatalog, "Todos los estados")
        );
      }

      if (tipoProveedorCatalog) {
        setProviderTypeOptions(
          fetchCatalogAsSelectableOptions(tipoProveedorCatalog, "Todos los tipos")
        );
      }
    };

    loadCatalogs();
  }, []);

  const handleGetXmlContent = useCallback(async (row: Invoice) => {
    const { data } = await client.getXmlDocument(row.xmlContent);
    return data;
  }, [client]);

  const handleFetch = useCallback(async (f: InvoiceFilters) => {
    const result = await client.getInvoices(f);
    return result;
  }, [client]);

  const handleSearch = (newFilters: InvoiceFilters) => {
    saveFiscalListFilters(FISCAL_LIST_KEYS.invoices.filters, newFilters);
    setFilters(newFilters);
    setHasSearched(true);
  };

  const handleFiltersChange = (newFilters: InvoiceFilters) => {
    setFilters(newFilters);
    setHasSearched(false);
  };

  const handleFiltersClear = useCallback((cleared: InvoiceFilters) => {
    deepLinkSearchedRef.current = null;
    saveFiscalListFilters(FISCAL_LIST_KEYS.invoices.filters, cleared);
    if (location.search) {
      navigate({ pathname: location.pathname, search: "" }, { replace: true });
    }
  }, [location.pathname, location.search, navigate]);

  /** Con uuid en la URL se busca por UUID (no vacío). Sin uuid, vacío = faltan fechas de recepción. */
  const areFiltersEmpty = (f: InvoiceFilters) => (!f?.fechaInicioRecepcion?.trim() || !f?.fechaFinalRecepcion?.trim());

  const fetchFnOrEmpty = useCallback(
    async (f: InvoiceFilters) =>
      areFiltersEmpty(f)
        ? { content: [] as Invoice[], totalElements: 0, totalPages: 0, page: 0 }
        : handleFetch(f),
    [handleFetch, areFiltersEmpty]
  );

  useFiscalListRefetchOnReturn<InvoiceFilters>(
    FISCAL_LIST_KEYS.invoices,
    returningFromDetail,
    handleSearch
  );

  const openReprocessConfirm = async (row: Invoice) => {
    setReprocessConfirmRow(row);
    setReprocessConfirmOpen(true);
    const msg = await fetchCatalogMessage("CatMsgAdvertencia", "WRN7008");
    if (msg) setReprocessConfirmMessage(msg);
  };

  const openCancelConfirm = async (row: Invoice) => {
    setCancelConfirmRow(row);
    setCancelConfirmOpen(true);
    const msg = await fetchCatalogMessage("CatMsgConfirm", "CRF8001");
    if (msg) setCancelConfirmMessage(msg);
  };

    const handleCancelConfirm = async () => {
    if (!cancelConfirmRow) return;
    setCancelConfirmOpen(false);
    const row = cancelConfirmRow;
    setCancelConfirmRow(null);
    setProcessLoading(true);
    setErrorMsg(null);
    try {
      if(row.numeroProveedor=="" || row.numeroProveedor==null) {
        setErrorMsg("No se puede cancelar la factura sin número de proveedor");
        return;
      }
      await client.cancelInvoice(row.fiscalUuid || "", row.numeroProveedor ?? "");
      setRefreshKey((k: number) => k + 1);
    } catch (error: unknown) {
      setErrorMsg(getErrorMessage(error, "Error al cancelar la factura"));
    } finally {
      setProcessLoading(false);
      
    }
  };

  const handleReprocessConfirm = async () => {
    if (!reprocessConfirmRow) return;
    setReprocessConfirmOpen(false);
    const row = reprocessConfirmRow;
    setReprocessConfirmRow(null);
    setProcessLoading(true);
    setErrorMsg(null);
    try {
      if(row.numeroProveedor=="" || row.numeroProveedor==null) {
        setErrorMsg("No se puede reprocesar la factura sin número de proveedor");
        return;
      }
      await client.reprocessInvoice(row.invoiceUuid || "", row.numeroProveedor ?? "");
      setRefreshKey((k: number) => k + 1);
    } catch (error: unknown) {
      setErrorMsg(getErrorMessage(error, "Error al reprocesar la factura"));
    } finally {
      setProcessLoading(false);
      
    }
  };

  const rowActionDescriptors: { gate: { app: string; event: string }; action: RowAction<Invoice> }[] = [
    {
      gate: APP_EVENT.INVOICES.LINK_CREDIT_NOTE,
      action: {
        title: "Ver notas de crédito relacionadas",
        icon: viewIcon,
        onClick: (_row, _nav) => {
          const ncUuid = _row.notasCreditoRelacionadas[0]?.fiscalUuid;
          if (!ncUuid) return;
          const qs = new URLSearchParams({
            uuid: ncUuid,
            start: filters.fechaInicioRecepcion,
            end: filters.fechaFinalRecepcion,
          });
          _nav(`/fiscal/notas-credito?${qs.toString()}`);
        },
        isDisabled: (row) => row.notasCreditoRelacionadas.length === 0,
      },
    },
    {
      gate: APP_EVENT.INVOICES.UPDATE_STATUS,
      action: {
        title: "Reproceso contable",
        icon: reprocessIcon,
        onClick: (_row) => { openReprocessConfirm(_row); },
        isDisabled: (row) => row.status !== INVOICE_STATUS_RECHAZO_CONTABLE,
      },
    },
    {
      gate: APP_EVENT.INVOICES.CANCEL,
      action: {
        title: "Cancelar factura",
        icon: deleteIcon,
        onClick: (_row) => { openCancelConfirm(_row); },
        isDisabled: (row) => row.status !== INVOICE_PROCESS_SENDED && row.status !== INVOICE_WRONG_DATA && row.status !== INVOICE_ERROR_DATA,
      },
    },
  ];
  const rowActions: RowAction<Invoice>[] = rowActionDescriptors
    .filter(({ gate }) => hasEvent(gate.app, gate.event))
    .map(({ action }) => action);

  const filterFields: FilterField[] = useMemo(
    () => [
      {
        key: "idProveedor",
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
        type: "text",
      },
      {
        key: "tipoProveedor",
        label: "Tipo Proveedor",
        type: "selectFloating",
        options: providerTypeOptions,
      },
      {
        key: "estatus",
        label: "Estado",
        type: "selectFloating",
        options: statusInvoices,
      },
      {
        key: "fechaRecepcion",
        label: "Fecha búsqueda",
        type: "dateRange",
        required: true,
      },
    ],
    [providerTypeOptions, statusInvoices]
  );

  return decorate(
    breadcrumb,
    "/",
    <div>
      <Title
        title="Listado de Facturas"
        description="Consulte el historial de facturas recibidas y el estatus de validación de cada una."
        actions={
          <PermissionGate appEvent={APP_EVENT.INVOICES.DOWNLOAD_CSV}>
            <ExportCsvButton
              disabled={!canExportCsv}
              onClick={() => gridRef.current?.exportCsv()}
            />
          </PermissionGate>
        }
      />
      <GenericModal
        visible={reprocessConfirmOpen}
        variant="confirm"
        message={reprocessConfirmMessage}
        confirmText="Aceptar"
        cancelText="Cancelar"
        onConfirm={() => { handleReprocessConfirm(); }}
        onCancel={() => {
          setReprocessConfirmOpen(false);
          setReprocessConfirmRow(null);
        }}
      />

      <GenericModal
        visible={cancelConfirmOpen}
        variant="confirm"
        message={cancelConfirmMessage}
        confirmText="Aceptar"
        cancelText="Cancelar"
        onConfirm={() => { handleCancelConfirm(); }}
        onCancel={() => {
          setCancelConfirmOpen(false);
          setCancelConfirmRow(null);
        }}
      />

      {processLoading && (
        <GenericModal
          visible
          variant="loading"
          message="Procesando factura..."
        />
      )}
      
      <GenericModal
        visible={!!errorMsg}
        variant="alert"
        severity="error"
        title="Error"
        message={errorMsg || ""}
        buttonText="Aceptar"
        onClose={() => setErrorMsg(null)}
        onConfirm={() => setErrorMsg(null)}
      />
      
      <ReusableFiltersBar<InvoiceFilters>
        key={customFilters.uuid ? `invoice-${customFilters.uuid}` : "invoice-default"}
        fields={filterFields}
        initialFilters={initialFilters}
        resetFiltersOnClear={EMPTY_INVOICE}
        onSearch={handleSearch}
        onFiltersChange={handleFiltersChange}
        onClear={handleFiltersClear}
        searchAppEvent={APP_EVENT.INVOICES.SEARCH}
        clearAppEvent={APP_EVENT.INVOICES.CLEAR_FILTERS}
        onHydrated={(f) => {
          setFiltersReady(true);

          if (returningFromDetail) {
            setFilters(f);
            return;
          }

          const urlKey = [
            customFilters.uuid?.trim(),
            customFilters.fechaInicioRecepcion?.trim(),
            customFilters.fechaFinalRecepcion?.trim(),
          ]
            .filter(Boolean)
            .join("|");

          const canAutoSearch =
            urlKey &&
            f.fechaInicioRecepcion?.trim() &&
            f.fechaFinalRecepcion?.trim() &&
            deepLinkSearchedRef.current !== urlKey;

          if (canAutoSearch) {
            deepLinkSearchedRef.current = urlKey;
            handleSearch(f);
            return;
          }

          setFilters(f);
        }}
        sessionFiltersKey={FISCAL_LIST_KEYS.invoices.filters}
        restoreSavedFilters={returningFromDetail}
        validateFilters={(f) => {
          if (!f.fechaInicioRecepcion || !f.fechaFinalRecepcion) {
            return "Las fechas de recepción son obligatorias";
          }
          return null;
        }}
      />
      <Divider />
      {filtersReady && (
        <div key={refreshKey}>
          <DataGrid<Invoice, InvoiceFilters>
            ref={gridRef}
            columns={columns}
            rowActions={rowActions}
            getRowId={r => r.invoiceUuid}
            fetchFn={fetchFnOrEmpty}
            filters={filters}
            fetchEnabled={hasSearched}
            initialPage={0}
            initialSize={10}
            selectable
            enableCsv
            hideCsvToolbar
            onExportAvailabilityChange={setCanExportCsv}
            csvFilename={`Facturas ${formatDate(new Date().toString(), true)}`}
            enableXml
            enablePdf
            xmlAppEvent={APP_EVENT.INVOICES.DOWNLOAD_XML}
            pdfAppEvent={APP_EVENT.INVOICES.DOWNLOAD_PDF}
            getXmlContent={handleGetXmlContent}
            getFilename={getXmlFileNameFromRow}
            filtersEmpty={!hasSearched || areFiltersEmpty(filters)}
          />
        </div>
      )}
    </div>
  );
}
