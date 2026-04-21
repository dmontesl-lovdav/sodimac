
import { useState, useEffect, useCallback, useMemo } from "react";
import DataGrid, { DataGridColumn, RowAction } from "@/shared/components/ui/datagrid/DataGrid";
import { formatDate, formatAmount, fetchProvidersAsCatalog, fetchCatalog, fetchCatalogMessage } from "@/utils/utils";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { ReusableFiltersBar, FilterField } from "@/shared/components/ui/filters";
import { createCreditsClient } from "./api/CreditsClient";
import { CREDIT_NOTE_PENDIENTE_CONTABILIZAR, CREDIT_NOTE_RECHAZO_CONTABLE, CreditNoteFilters, EMPTY_CREDIT_NOTE, type CreditNote } from "./interfaces";
import { Divider, Title } from "@/shared/components/ui/misc";
import viewIcon from '@assets/eye-show.svg';
import trashIcon from '@assets/delete.svg';
import { GenericModal } from "@/shared/components/ui";
import { ModalMsg } from "@/shared/components/ui/modal/ModalMsg";
import { GenericButton } from "@/shared/components/ui";
import type { SelectableOption } from "@/utils/utils";
import { useLocation, useNavigate } from "react-router-dom";

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
  { header: "Nombre Proveedor", accessor: r => r.supplierName ?? "--", exportAccessor: r => r.supplierName },
  { header: "Tipo Proveedor", accessor: r => r.tipoProveedor ?? "--", exportAccessor: r => r.tipoProveedor },
  { header: "Fecha Emisión", accessor: r => r.issueDate ? formatDate(r.issueDate) : "N/D", exportAccessor: r => r.issueDate },
  { header: "Fecha Certificación", accessor: r => r.certificationDate ? formatDate(r.certificationDate) : "N/D", exportAccessor: r => r.certificationDate },
  { header: "Estado", accessor: r => r.statusName ?? "--", exportAccessor: r => r.statusName },
];

export default function CreditsGrid() {
  const [filters, setFilters] = useState<CreditNoteFilters>(EMPTY_CREDIT_NOTE);
  const [filtersReady, setFiltersReady] = useState(false);
  const [providers, setProviders] = useState<SelectableOption[]>([]);
  const [statusCreditNotes, setStatusCreditNotes] = useState<SelectableOption[]>([]);
  const [cancelConfirmRow, setCancelConfirmRow] = useState<CreditNote | null>(null);
  const [cancelConfirmMessage, setCancelConfirmMessage] = useState("");
  const [cancelConfirmOpen, setCancelConfirmOpen] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [processLoading, setProcessLoading] = useState(false);
  const client = createCreditsClient<{
    content: CreditNote[];
    totalElements: number;
    totalPages: number;
    page: number;
  }>();
  const location = useLocation();
  const navigate = useNavigate();

  const customFilters = useMemo(() => {
    const params = new URLSearchParams(location.search);
    const lastCreditNoteUuid = localStorage.getItem("lastCreditNoteUuid") ?? undefined;
    const filtersOnUrl: Partial<CreditNoteFilters> = {
      uuid: params.get("uuid") || lastCreditNoteUuid || undefined,
      fechaInicioRecepcion: params.get("start") || undefined,
      fechaFinalRecepcion: params.get("end") || undefined,
    };
    return filtersOnUrl;
  }, [location.search]);

  const initialFilters = useMemo<CreditNoteFilters>(() => {
    return {
      ...EMPTY_CREDIT_NOTE,
      ...customFilters,
    };
  }, [customFilters]);
  

  useEffect(() => {
    const fetchProviders = async () => {
      const response = await fetchProvidersAsCatalog();
      if (response) {
        setProviders(response);
      }
    };
    const fetchStatus = async () => {
      const options = await fetchCatalog("CatEstatusNotaCredito");
      if (options) {
        const mappedOptions = options.details.map((opt: any) => ({ label: opt.description, value: opt.internalStatus }));
        setStatusCreditNotes(mappedOptions);
      }
    }
    fetchProviders();
    fetchStatus();
  }, []);

  const handleGetXmlContent = useCallback(async (row: CreditNote) => {
    const { data } = await client.getXmlDocument(row.invoiceUuid);
    return data;
  }, [client]);

  const handleCancelConfirm = async () => {
    if (!cancelConfirmRow) return;
    setCancelConfirmOpen(false);
    const row = cancelConfirmRow;
    setCancelConfirmRow(null);
    setProcessLoading(true);
    setErrorMsg(null);
    try {
      //TODO: ajustar cuando el API soporte el id del proveedor
      const result = await client.cancelCreditNote(row.invoiceUuid || "", row.numeroProveedor ?? "1001") as { data?: unknown };
      console.log(result);
      if (result?.data) {
        // respuesta con data
      }
    } catch (error: any) {
      const customMsg = (error.response.data.code + ": " + error.response.data.message) || error.message;
      console.error("Error al cancelar la nota de crédito:", customMsg || error.message);
      setErrorMsg(customMsg || "Error al cancelar la nota de crédito");
    } finally {
      setProcessLoading(false);

    }
  };

  const openCancelConfirm = async (row: CreditNote) => {
    setCancelConfirmRow(row);
    setCancelConfirmOpen(true);
    const msg = await fetchCatalogMessage("CatMsgConfirm", "CRF8002");
    if (msg) setCancelConfirmMessage(msg);
  };

  const handleFetch = useCallback(async (f: CreditNoteFilters) => {
    const result = await client.getCreditNotes({ ...f, tipoDocumento: "E" });
    return result;
  }, [client]);

  const handleSearch = (newFilters: CreditNoteFilters) => {
    setFilters(newFilters);
  };

  const areFiltersEmpty = (f: CreditNoteFilters) =>
    !f?.fechaInicioRecepcion?.trim() || !f?.fechaFinalRecepcion?.trim();

  const fetchFnOrEmpty = useCallback(
    async (f: CreditNoteFilters) =>
      areFiltersEmpty(f)
        ? { content: [] as CreditNote[], totalElements: 0, totalPages: 0, page: 0 }
        : handleFetch(f),
    [handleFetch, areFiltersEmpty]
  );

  const rowActions: RowAction<CreditNote>[] = [
    {
      title: "Ver factura relacionada",
      icon: viewIcon,
      onClick: (row, nav) => nav(`/fiscal/facturas?uuid=${encodeURIComponent(row.relatedInvoiceUuid)}&start=${filters.fechaInicioRecepcion}&end=${filters.fechaFinalRecepcion}`),
      isDisabled: (row) => !row.relatedInvoiceUuid,
    },
    {
      title: "Cancelar nota de crédito",
      icon: trashIcon,
      onClick: (_row, _nav) => openCancelConfirm(_row),
      isDisabled: (row) => row.status !== CREDIT_NOTE_PENDIENTE_CONTABILIZAR && row.status !== CREDIT_NOTE_RECHAZO_CONTABLE,
    }
  ];


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
      options: providers
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
      label: "Estatus de la nota",
      type: "select",
      options: statusCreditNotes,
      placeholder: "Estado",
    },
  ];

  return decorate(
    breadcrumb,
    "/",
    <div>
      <Title
        title="Listado de Notas de Crédito"
        description="Visualiza las notas de crédito registradas."
        actions={
          <GenericButton onClick={() => navigate("/fiscal/publicar-nota-credito")} variant="primary">
            Agregar Nota de Crédito
          </GenericButton>
        }
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
          message="Procesando nota de crédito..."
        />
      )}

      <ModalMsg severity="error" visible={!!errorMsg} msg={errorMsg || ""} onClose={() => setErrorMsg(null)} />


      <ReusableFiltersBar<CreditNoteFilters>
        key={customFilters.uuid ? `creditNote-${customFilters.uuid}` : "creditNote-default"}
        fields={filterFields}
        initialFilters={initialFilters}
        onSearch={handleSearch}
        onHydrated={(f) => {
          setFilters(f);
          setFiltersReady(true);
        }}
       storageKey={customFilters.uuid ? undefined : "creditNoteFilters"}
        validateFilters={(f) => {
          if (!f.fechaInicioRecepcion || !f.fechaFinalRecepcion) {
            return "Las fechas de recepción son obligatorias";
          }
          return null;
        }}
      />
      <Divider />
      {filtersReady && (
        <DataGrid<CreditNote, CreditNoteFilters>
          columns={columns}
          getRowId={r => r.invoiceUuid}
          fetchFn={fetchFnOrEmpty}
          filters={filters}
          initialPage={0}
          initialSize={10}
          selectable
          enableCsv
          csvFilename={`Notas de Crédito ${formatDate(new Date().toString(), true)}`}
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
