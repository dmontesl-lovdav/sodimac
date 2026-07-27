
import { useState, useEffect, useCallback, useMemo, useRef } from "react";
import DataGrid, { DataGridColumn, RowAction, type DataGridHandle } from "@/shared/components/ui/datagrid/DataGrid";
import { APP_EVENT, PermissionGate, useSecurityContext } from "@shared/security";
import { formatDate, formatAmount, fetchCatalogMessage, getXmlFileNameFromRow, fetchCatalogDetails, fetchCatalogAsSelectableOptions, SelectableOption, getErrorMessage } from "@/utils/utils";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { ReusableFiltersBar, FilterField } from "@/shared/components/ui/filters";
import { createCreditsClient } from "./api/CreditsClient";
import { CREDIT_NOTE_PENDIENTE_CONTABILIZAR, CREDIT_NOTE_PROCESS_SENDED, CREDIT_NOTE_RECHAZO_CONTABLE, CreditNoteFilters, EMPTY_CREDIT_NOTE, type CreditNote } from "./interfaces";
import { Divider, Title, ExportCsvButton } from "@/shared/components/ui/misc";
import viewIcon from '@assets/eye-show.svg';
import trashIcon from '@assets/delete.svg';
import { GenericModal } from "@/shared/components/ui";
import { GenericButton } from "@/shared/components/ui";
import { useLocation, useNavigate } from "react-router-dom";
import {
  FISCAL_LIST_KEYS,
  saveFiscalListFilters,
  useFiscalListRefetchOnReturn,
  useFiscalListScreenSession,
} from "@/shared/session/fiscalListSession";

const breadcrumb: BreadcrumbItem[] = [
  { label: "Fiscal", to: "/" },
  { label: "Notas de Crédito" },
];

export function areCreditNoteFiltersEmpty(f: CreditNoteFilters): boolean {
  return !f?.fechaInicioRecepcion?.trim() || !f?.fechaFinalRecepcion?.trim();
}

const columns: DataGridColumn<CreditNote>[] = [
  { header: "Serie", accessor: r => r.series ?? "--", exportAccessor: r => r.series },
  { header: "Folio", accessor: r => r.folio ?? "--", exportAccessor: r => r.folio },
  { header: "Subtotal", accessor: r => r.subtotal != null ? formatAmount(r.subtotal) : "--", exportAccessor: r => r.subtotal },
  { header: "Total", accessor: r => r.total != null ? formatAmount(r.total) : "--", exportAccessor: r => r.total },
  { header: "UUID", accessor: r => r.invoiceUuid ?? "--", exportAccessor: r => r.invoiceUuid },
  { header: "UUID Factura", accessor: r => r.relatedInvoiceUuid ?? "--", exportAccessor: r => r.relatedInvoiceUuid + "" },
  { header: "Tipo Nota de Crédito", accessor: r => r.tipoNotaCreditoDescripcion ?? "--", exportAccessor: r => r.tipoNotaCreditoDescripcion },
  { header: "Tipo Proveedor", accessor: r => r.tipoProveedorDescripcion ?? "--", exportAccessor: r => r.tipoProveedorDescripcion },
  { header: "Número Proveedor", accessor: r => r.numeroProveedor ?? "--", exportAccessor: r => r.numeroProveedor },
  { header: "Nombre Proveedor", accessor: r => r.supplierName ?? "--", exportAccessor: r => r.supplierName },
  { header: "Fecha Emisión", accessor: r => r.issueDate ? formatDate(r.issueDate) : "N/D", exportAccessor: r => r.issueDate },
  { header: "Fecha Recepción", accessor: r => r.createdAt ? formatDate(r.createdAt) : "N/D", exportAccessor: r => r.createdAt },
  { header: "Estado", accessor: r => r.statusName ?? "--", exportAccessor: r => r.statusName },
];

export default function CreditsGrid() {
  const returningFromDetail = useFiscalListScreenSession(FISCAL_LIST_KEYS.creditNotes);
  const [filters, setFilters] = useState<CreditNoteFilters>(EMPTY_CREDIT_NOTE);
  const [filtersReady, setFiltersReady] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [searchToken, setSearchToken] = useState(0);
  const [statusCreditNotes, setStatusCreditNotes] = useState<SelectableOption<string>[]>([]);
  const [tipoNotaCreditoOptions, setTipoNotaCreditoOptions] = useState<SelectableOption<string>[]>([]);
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
  const gridRef = useRef<DataGridHandle>(null);
  const { hasEvent } = useSecurityContext();
  const deepLinkSearchedRef = useRef<string | null>(null);
  const [canExportCsv, setCanExportCsv] = useState(false);
  const [providerTypeOptions, setProviderTypeOptions] = useState<SelectableOption<string>[]>([]);
  const customFilters = useMemo((): Partial<CreditNoteFilters> => {
    const params = new URLSearchParams(location.search);
    const omitEmpty = (key: string) => {
      const v = params.get(key);
      return v ? v : undefined;
    };
    return {
      uuid: omitEmpty("uuid"),
      relatedInvoiceUuid: omitEmpty("relatedInvoiceUuid"),
      fechaInicioRecepcion: omitEmpty("start"),
      fechaFinalRecepcion: omitEmpty("end"),
      idProveedor: omitEmpty("supplierNumber"),
    };
  }, [location.search]);

  const initialFilters = useMemo<CreditNoteFilters>(() => {
    return {
      ...EMPTY_CREDIT_NOTE,
      ...customFilters,
    };
  }, [customFilters]);
  

  useEffect(() => {
    const fetchTipoNotaCredito = async () => {
      const options = await fetchCatalogDetails("CatTipoNotaCredito");
      if (options) {
        setTipoNotaCreditoOptions(fetchCatalogAsSelectableOptions(options, "Todos los tipos"));
      }
    }
    const fetchStatus = async () => {
      const options = await fetchCatalogDetails("CatEstatusNotaCredito");
      if (options) {
        setStatusCreditNotes(fetchCatalogAsSelectableOptions(options, "Todos los estados"));
      }
    }
    const fetchProviderType = async () => {
      const options = await fetchCatalogDetails("CatTipoProveedor");
      if (options) {
        setProviderTypeOptions(fetchCatalogAsSelectableOptions(options, "Todos los tipos"));
      }
    }
    fetchTipoNotaCredito();
    fetchStatus();
    fetchProviderType();
  }, []);

  const handleGetXmlContent = useCallback(async (row: CreditNote) => {
    const { data } = await client.getXmlDocument(row.xmlContent);
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
      await client.cancelCreditNote(row.invoiceUuid ?? "", row.numeroProveedor ?? "1001");
    } catch (error: unknown) {
      setErrorMsg(getErrorMessage(error, "Error al cancelar la nota de crédito"));
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
    saveFiscalListFilters(FISCAL_LIST_KEYS.creditNotes.filters, newFilters);
    setFilters(newFilters);
    setHasSearched(true);
    setSearchToken((t) => t + 1);
  };

  const handleFiltersChange = (newFilters: CreditNoteFilters) => {
    setFilters(newFilters);
    setHasSearched(false);
  };

  const handleFiltersClear = useCallback((cleared: CreditNoteFilters) => {
    deepLinkSearchedRef.current = null;
    saveFiscalListFilters(FISCAL_LIST_KEYS.creditNotes.filters, cleared);
    if (location.search) {
      navigate({ pathname: location.pathname, search: "" }, { replace: true });
    }
  }, [location.pathname, location.search, navigate]);

  const fetchFnOrEmpty = useCallback(
    async (f: CreditNoteFilters) =>
      areCreditNoteFiltersEmpty(f)
        ? { content: [] as CreditNote[], totalElements: 0, totalPages: 0, page: 0 }
        : handleFetch(f),
    [handleFetch]
  );

  useFiscalListRefetchOnReturn<CreditNoteFilters>(
    FISCAL_LIST_KEYS.creditNotes,
    returningFromDetail,
    handleSearch
  );

  const rowActionDescriptors: { gate: { app: string; event: string }; action: RowAction<CreditNote> }[] = [
    {
      gate: APP_EVENT.CREDIT_NOTES.LINK_INVOICE,
      action: {
        title: "Ver factura relacionada",
        icon: viewIcon,
        onClick: (row, nav) => {
          if (!row.relatedInvoiceUuid) return;
          const startDate = row.createdAt ?? "";
          const endDate = row.createdAt ?? "";
          const qs = new URLSearchParams({
            uuid: String(row.relatedInvoiceUuid),
            start: startDate,
            end: endDate,
          });
          nav(`/fiscal/facturas?${qs.toString()}`);
        },
        isDisabled: (row) => !row.relatedInvoiceUuid,
      },
    },
    {
      gate: APP_EVENT.CREDIT_NOTES.CANCEL,
      action: {
        title: "Cancelar nota de crédito",
        icon: trashIcon,
        onClick: (_row) => { openCancelConfirm(_row); },
        isDisabled: (row) => row.status != CREDIT_NOTE_PROCESS_SENDED && row.status != CREDIT_NOTE_PENDIENTE_CONTABILIZAR && row.status != CREDIT_NOTE_RECHAZO_CONTABLE,
      },
    },
  ];
  const rowActions: RowAction<CreditNote>[] = rowActionDescriptors
    .filter(({ gate }) => hasEvent(gate.app, gate.event))
    .map(({ action }) => action);


  const filterFields: FilterField[] = [
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
      type: "uuid",
    },
    {
      key: "relatedInvoiceUuid",
      label: "UUID Factura",
      type: "uuid",
    },
    {
      key: "tipoProveedor",
      label: "Tipo Proveedor",
      type: "select",
      options: providerTypeOptions,
    },
    {
      key: "estatus",
      label: "Estado nota de crédito",
      type: "select",
      options: statusCreditNotes,
    },
    {
      key: "tipoNotaCredito",
      label: "Tipo Nota de Crédito",
      type: "select",
      options: tipoNotaCreditoOptions,
    },
    {
      key: "fechaRecepcion",
      label: "Fecha búsqueda",
      type: "dateRange",
      required: true,
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
          <div className="fiscal-flex fiscal-gap-sm fiscal-flex-wrap fiscal-justify-end">
            <PermissionGate appEvent={APP_EVENT.CREDIT_NOTES.DOWNLOAD_CSV}>
              <ExportCsvButton
                disabled={!canExportCsv}
                variant="outline"
                onClick={() => gridRef.current?.exportCsv()}
              />
            </PermissionGate>
            <PermissionGate appEvent={APP_EVENT.CREDIT_NOTES.PUBLISH}>
              <GenericButton onClick={() => navigate("/fiscal/publicar-nota-credito")} >
                Agregar Nota de Crédito
              </GenericButton>
            </PermissionGate>
          </div>
        }
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
          message="Procesando nota de crédito..."
        />
      )}

      <GenericModal
        visible={!!errorMsg}
        variant="alert"
        severity="error"
        title="Error"
        message={errorMsg ?? ""}
        buttonText="Aceptar"
        onClose={() => setErrorMsg(null)}
        onConfirm={() => setErrorMsg(null)}
      />


      <ReusableFiltersBar<CreditNoteFilters>
        key={customFilters.uuid ? `creditNote-${customFilters.uuid}` : "creditNote-default"}
        fields={filterFields}
        initialFilters={initialFilters}
        resetFiltersOnClear={EMPTY_CREDIT_NOTE}
        onSearch={handleSearch}
        onFiltersChange={handleFiltersChange}
        onClear={handleFiltersClear}
        searchAppEvent={APP_EVENT.CREDIT_NOTES.SEARCH}
        clearAppEvent={APP_EVENT.CREDIT_NOTES.CLEAR_FILTERS}
        onHydrated={(f) => {
          setFiltersReady(true);

          if (returningFromDetail) {
            setFilters(f);
            return;
          }

          const urlKey = [
            customFilters.uuid?.trim(),
            customFilters.relatedInvoiceUuid?.trim(),
            customFilters.idProveedor?.trim(),
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
        sessionFiltersKey={FISCAL_LIST_KEYS.creditNotes.filters}
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
        <div key={searchToken}>
        <DataGrid<CreditNote, CreditNoteFilters>
          ref={gridRef}
          columns={columns}
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
          csvFilename={`Notas de Crédito ${formatDate(new Date().toString(), true)}`}
          enableXml
          enablePdf
          xmlAppEvent={APP_EVENT.CREDIT_NOTES.DOWNLOAD_XML}
          pdfAppEvent={APP_EVENT.CREDIT_NOTES.DOWNLOAD_PDF}
          getXmlContent={handleGetXmlContent}
          getFilename={getXmlFileNameFromRow}
          rowActions={rowActions}
          filtersEmpty={!hasSearched || areCreditNoteFiltersEmpty(filters)}
        />
        </div>
      )}
    </div>
  );
}
