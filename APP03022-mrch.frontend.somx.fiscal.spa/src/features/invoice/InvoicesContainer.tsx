
import { useState, useEffect, useMemo, useCallback } from "react";
import { useLocation } from "react-router-dom";
import DataGrid, { DataGridColumn, RowAction } from "@/shared/components/ui/datagrid/DataGrid";
import { formatDate, formatAmount, fetchProvidersAsCatalog, fetchCatalogMessage, fetchCatalog } from "@/utils/utils";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { ReusableFiltersBar, FilterField } from "@/shared/components/ui/filters";
import { GenericModal } from "@/shared/components/ui";
import { createInvoicesClient } from "./api/InvoiceClient";
import deleteIcon from "@/assets/delete.svg";
import viewIcon from "@/assets/eye-show.svg";
import reprocessIcon from "@/assets/reprocess.svg";
import type { SelectableOption } from "@/utils/utils";
import {
  InvoiceFilters,
  EMPTY_INVOICE,
  INVOICE_STATUS_RECHAZO_CONTABLE,
  INVOICE_PENDIENTE_ADDENDA,
  INVOICE_RECIBIDO_PARCIAL,
  type Invoice,
} from "./interfaces";
import { Divider, Title } from "@/shared/components/ui/misc";
import { ModalMsg } from "@/shared/components/ui/modal/ModalMsg";

const breadcrumb: BreadcrumbItem[] = [
  { label: "Home", to: "/" },
  { label: "Fiscal", to: "/" },
  { label: "Facturas" },
];

const columns: DataGridColumn<Invoice>[] = [
  { header: "Serie", accessor: r => r.series ?? "--", exportAccessor: r => r.series },
  { header: "Folio", accessor: r => r.folio ?? "--", exportAccessor: r => r.folio },
  { header: "Subtotal", accessor: r => r.subtotal != null ? formatAmount(r.subtotal) : "--", exportAccessor: r => r.subtotal },
  { header: "Total", accessor: r => r.total != null ? formatAmount(r.total) : "--", exportAccessor: r => r.total },
  { header: "UUID Factura", accessor: r => r.invoiceUuid, exportAccessor: r => r.invoiceUuid },
  { header: "Nombre Proveedor", accessor: r => r.supplierName ?? "--", exportAccessor: r => r.supplierName },
  { header: "Tipo Proveedor", accessor: r => r.tipoProveedor ?? "--", exportAccessor: r => r.tipoProveedor },
  { header: "Fecha Emisión", accessor: r => r.issueDate ? formatDate(r.issueDate) : "N/D", exportAccessor: r => r.issueDate },
  { header: "Fecha Recepción", accessor: r => r.certificationDate ? formatDate(r.certificationDate) : "N/D", exportAccessor: r => r.certificationDate },
  { header: "Estado", accessor: r => r.statusName ?? "--", exportAccessor: r => r.statusName },
  { header: "NC relacionadas", accessor: r => r.notasCreditoRelacionadas.length === 0 ? "0" : r.notasCreditoRelacionadas.length, exportAccessor: r => r.notasCreditoRelacionadas.length === 0 ? "0" : r.notasCreditoRelacionadas.length },
  
];

const MSG_REPROCESO_DEFAULT = "¿Desea volver a procesar esta factura para intentar contabilizarla nuevamente? Esta acción reemplazará el intento anterior.";

export default function InvoicesGrid() {
  const location = useLocation();

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
  const [providers, setProviders] = useState<SelectableOption[]>([]);
  const [statusInvoices, setStatusInvoices] = useState<SelectableOption[]>([]);
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
  const isAdmin = true;

  useEffect(() => {
    const fetchProviders = async () => {
      const response = await fetchProvidersAsCatalog();
      if (response) {
        setProviders(response);
      }
    };
    const fetchStatus = async () => {
      const options = await fetchCatalog("CatEstatusFactura");
      if(options){
        const mappedOptions = options.details.map((opt: any) => ({ label: opt.description, value: opt.internalStatus }));
        setStatusInvoices(mappedOptions);
      }
    }
    fetchProviders();
    fetchStatus();
  }, []);

  const handleGetXmlContent = useCallback(async (row: Invoice) => {
    const { data } = await client.getXmlDocument(row.invoiceUuid);
    return data;
  }, [client]);

  const handleFetch = useCallback(async (f: InvoiceFilters) => {
    const result = await client.getInvoices(f);
    return result;
  }, [client]);

  const handleSearch = (newFilters: InvoiceFilters) => {
    setFilters(newFilters);
  };

  /** Con uuid en la URL se busca por UUID (no vacío). Sin uuid, vacío = faltan fechas de recepción. */
  const areFiltersEmpty = (f: InvoiceFilters) => (!f?.fechaInicioRecepcion?.trim() || !f?.fechaFinalRecepcion?.trim());

  const fetchFnOrEmpty = useCallback(
    async (f: InvoiceFilters) =>
      areFiltersEmpty(f)
        ? { content: [] as Invoice[], totalElements: 0, totalPages: 0, page: 0 }
        : handleFetch(f),
    [handleFetch, areFiltersEmpty]
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
      //TODO: ajustar cuando el API soporte el id del proveedor
      const result = await client.cancelInvoice(row.invoiceUuid || "", row.numeroProveedor ?? "1001") as { data?: unknown };
      if (result?.data) {
        // respuesta con data
      }
      setRefreshKey((k: number) => k + 1);
    } catch (error: any) {
      const customMsg = (error.response.data.code+": "+error.response.data.message)|| error.message;
      console.error("Error al cancelar la factura:", customMsg || error.message);
      setErrorMsg(customMsg  || "Error al cancelar la factura");
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
      //TODO: ajustar cuando el API soporte el id del proveedor
      await client.reprocessInvoice(row.invoiceUuid || "", row.numeroProveedor ?? "1001");
      setRefreshKey((k: number) => k + 1);
    } catch (error: any) {
      const customMsg = (error.response.data.code+": "+error.response.data.message)|| error.message;
      console.error("Error al reprocesar la factura:", customMsg || error.message);
      setErrorMsg(customMsg  || "Error al reprocesar la factura");
    } finally {
      setProcessLoading(false);
      
    }
  };

  const rowActions: RowAction<Invoice>[] = isAdmin
    ? [
      {
          title: "Ver notas de crédito relacionadas",
          icon: viewIcon,
          onClick: (_row, _nav) => _nav(`/fiscal/notas-credito?uuid=${_row.notasCreditoRelacionadas[0]?.fiscalUuid}&start=${filters.fechaInicioRecepcion}&end=${filters.fechaFinalRecepcion}`),
          isDisabled: (row) => row.notasCreditoRelacionadas.length === 0,
        },
        {
          title: "Reproceso contable",
          icon: reprocessIcon,
          onClick: (_row, _nav) => openReprocessConfirm(_row),
          isDisabled: (row) => row.status !== INVOICE_STATUS_RECHAZO_CONTABLE,
        },
        
        {
          title: "Cancelar factura",
          icon: deleteIcon,
          onClick: (_row, _nav) => openCancelConfirm(_row),
          isDisabled: (row) => row.status !== INVOICE_PENDIENTE_ADDENDA && row.status !== INVOICE_RECIBIDO_PARCIAL,
        },
        
      ]
    : [];

  const filterFields: FilterField[] = [
    {
      key: "fechaRecepcion",
      label: "Fecha de Recepción",
      type: "dateRange",
      required: true,
    },
    {
      key: "rfcEmisor",
      label: "Proveedor",
      type: "select",
      options: providers,
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
    {
      key: "estatus",
      label: "Estatus",
      type: "select",
      options: statusInvoices,
      placeholder: "Estado",
    },
  ];

  return decorate(
    breadcrumb,
    "/",
    <div>
      <Title title="Listado de Facturas" description="Consulta el historial de facturas emitidas y su estatus de validación." />
      <GenericModal
        visible={reprocessConfirmOpen}
        variant="confirm"
        message={reprocessConfirmMessage}
        confirmText="Aceptar"
        cancelText="Cancelar"
        onConfirm={handleReprocessConfirm}
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
        onConfirm={handleCancelConfirm}
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
      
      <ModalMsg severity="error" visible={!!errorMsg} msg={errorMsg || ""} onClose={() => setErrorMsg(null)} />
      
      <ReusableFiltersBar<InvoiceFilters>
        key={customFilters.uuid ? `invoice-${customFilters.uuid}` : "invoice-default"}
        fields={filterFields}
        initialFilters={initialFilters}
        onSearch={handleSearch}
        onHydrated={(f) => {
          setFilters(f);
          setFiltersReady(true);
        }}
        storageKey={customFilters.uuid ? undefined : "invoiceFilters"}
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
            columns={columns}
            rowActions={rowActions}
            getRowId={r => r.invoiceUuid}
            fetchFn={fetchFnOrEmpty}
            filters={filters}
            initialPage={0}
            initialSize={10}
            selectable
            enableCsv
            csvFilename={`Facturas ${formatDate(new Date().toString(), true)}`}
            enableXml
            enablePdf
            getXmlContent={handleGetXmlContent}
            filtersEmpty={areFiltersEmpty(filters)}
          />
        </div>
      )}
    </div>
  );
}
