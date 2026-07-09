
import React, { ReactElement, useMemo, useState, useCallback, useEffect, useRef, forwardRef, useImperativeHandle } from "react";
import GenericTable, {
  Column as TableColumn,
  RowAction as GenericRowAction,
} from "@/shared/components/ui/table/DataTable";
import { GenericButton, GenericModal } from "@/shared/components/ui";
import xmlIconUrl from "@assets/xml.svg";
import pdfIconUrl from "@assets/pdf.svg";
import { exportToCSV, getStandardFilename } from "@/utils/utils";
import { usePaginatedData, type UsePaginatedDataOptions, parseFetchError } from "@/shared/components/ui/datagrid/hooks/usePaginatedData";
import { useSecurityContext } from "@shared/security";

/** Reexporta los tipos de GenericTable (no tienen 'accessor') */
export type Column<T> = TableColumn<T>;
export type RowAction<T> = GenericRowAction<T>;

/** Tipo interno del DataGrid que sí soporta 'accessor' */
export type DataGridColumn<T> = {
  header: string | React.ReactNode;
  align?: "left" | "center" | "right";
  render?: (row: T) => React.ReactNode;      // render sin nav
  accessor?: (row: T) => React.ReactNode;    // valor simple
  exportHeader?: string;
  exportAccessor?: (row: T) => string | number | null | undefined;
};

export type BulkAction<T> = {
  label: string;
  value: string;
  run: (selected: T[], all: T[]) => Promise<void> | void;
};

type DataGridProps<T, F = any> = {
  rows?: T[];
  loading?: boolean;
  /** Permite pasar columnas del DataGrid (con accessor) */
  columns: DataGridColumn<T>[];
  getRowId: (row: T) => string | number;
  page?: number;
  perPage?: number;
  totalPages?: number;
  totalItems?: number;
  emptyLabel?: string;
  selectable?: boolean;
  onChangePage?: (page: number) => void;
  onChangePerPage?: (perPage: number) => void;

  /** --- Paginación automática (si se pasa fetchFn, ignora rows y loading) --- */
  fetchFn?: (filters: F & { page: number; size: number }) => Promise<{
    content: T[];
    totalElements: number;
    totalPages: number;
    page: number;
    size?: number;
  }>;
  filters?: F;
  initialPage?: number;
  initialSize?: number;

  /** --- Configuración de acciones internas --- */
  enableCsv?: boolean;                 // default: true
  enablePdf?: boolean;                 // default: false
  getPdfUrl?: (row: T) => string | null | undefined; // URL del PDF
  csvFilename?: string;                // default: "Export"
  /** Oculta el botón CSV sobre la tabla (usar ExportCsvButton en el encabezado). */
  hideCsvToolbar?: boolean;
  /** Indica si hay datos exportables (p. ej. para habilitar botón en header). */
  onExportAvailabilityChange?: (canExport: boolean) => void;
  enableXml?: boolean;                 // default: false
  getXmlContent?: (row: T) => string | null | undefined | Promise<string | null | undefined>;
  getFilename?: (row: T) => string; // default: `{folio|invoiceUuid|getRowId}`
  /** Acciones por fila adicionales (se muestran antes de XML/PDF) */
  rowActions?: GenericRowAction<T>[];

  xmlAppEvent?: { app: string; event: string };
  pdfAppEvent?: { app: string; event: string };

  /** Si es true y no hay filas, se muestra SimpleLobby con este mensaje (ej. "Realiza una búsqueda en los filtros"). */
  filtersEmpty?: boolean;
  emptyFiltersMessage?: string;
  /** Si es false, no consulta hasta que el usuario pulse Buscar. Por defecto false. */
  fetchEnabled?: boolean;
  /** Incrementar en cada búsqueda explícita para re-consultar aunque los filtros no cambien. */
  searchToken?: number;
};

/** ------------------------------------------------------------
 * Parsea el XML de error que devuelve el API cuando falla el PDF.
 * Ejemplo: <FiscalErrorResponse><errorCode>ERR001</errorCode>...</FiscalErrorResponse>
 * ------------------------------------------------------------ */
function parseFiscalXmlError(xmlText: string): string {
  try {
    const doc = new DOMParser().parseFromString(xmlText, "application/xml");
    const errorCode   = doc.querySelector("errorCode")?.textContent?.trim();
    const message     = doc.querySelector("message")?.textContent?.trim();
    const addInfo     = doc.querySelector("additionalInfo")?.textContent?.trim();
    if (message) {
      return [errorCode ? `[${errorCode}]` : "", message, addInfo]
        .filter(Boolean)
        .join(" — ");
    }
  } catch {
    /* ignore */
  }
  return "Error al descargar el PDF. No hay PDF disponible para descarga.";
}

/** ------------------------------------------------------------
 * Descarga el PDF haciendo un fetch real para poder detectar
 * respuestas de error (XML) en lugar de abrir el link directo.
 * ------------------------------------------------------------ */
async function fetchAndDownloadPdf(
  pdfUrl: string | null | undefined,
  filename: string,
  onError: (msg: string) => void
): Promise<void> {
  const url = (pdfUrl ?? "").trim();
  if (!url) {
    onError("No hay URL de PDF disponible para este registro.");
    return;
  }
  const safeName = filename.toLowerCase().endsWith(".pdf") ? filename : `${filename}.pdf`;
  try {
    const res = await fetch(url, { credentials: "include" });
    const contentType = res.headers.get("Content-Type") ?? "";

    if (!res.ok || contentType.includes("xml") || contentType.includes("text/")) {
      const text = await res.text();
      onError(parseFiscalXmlError(text));
      return;
    }

    const blob = await res.blob();
    const blobUrl = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = blobUrl;
    a.download = safeName;
    a.style.display = "none";
    document.body.appendChild(a);
    a.click();
    requestAnimationFrame(() => {
      document.body.removeChild(a);
      URL.revokeObjectURL(blobUrl);
    });
  } catch {
    onError("Error al descargar el PDF. Inténtalo nuevamente.");
  }
}

/** ------------------------------------------------------------
 * Helper interno: descargar XML (sin depender de props).
 * ------------------------------------------------------------ */
function downloadXML(xmlContent?: string | null, filename: string = "archivo.xml") {
  const xml = (xmlContent ?? "").trim();
  if (!xml) {
    console.warn("downloadXML: xmlContent vacío o nulo.");
    return;
  }
  const safeName = filename.toLowerCase().endsWith(".xml") ? filename : `${filename}.xml`;
  const blob = new Blob([xml], { type: "text/xml;charset=utf-8" });

  const anyNav = window.navigator as any;
  if (anyNav?.msSaveOrOpenBlob) {
    anyNav.msSaveOrOpenBlob(blob, safeName);
    return;
  }

  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = safeName;
  a.style.display = "none";
  document.body.appendChild(a);
  a.click();
  requestAnimationFrame(() => {
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  });
}

/** ------------------------------------------------------------
 * Helpers internos para CSV desde columnas
 * ------------------------------------------------------------ */
function headerToString(header: string | React.ReactNode, fallback = ""): string {
  return typeof header === "string" ? header : fallback;
}

function getCellValue<T>(col: DataGridColumn<T>, row: T): string | number | null | undefined {
  if (typeof col.exportAccessor === "function") return col.exportAccessor(row);
  if (typeof col.accessor === "function") return col.accessor(row) as any;
  if (typeof col.render === "function") return col.render(row) as any;
  return "";
}

export type DataGridHandle = {
  exportCsv: () => void;
};

export function exportDataGridToCsv<T>(
  columns: DataGridColumn<T>[],
  data: T[],
  csvFilename: string
): void {
  const headers = columns.map((col) =>
    col.exportHeader ?? headerToString(col.header, "")
  );

  const rowsForCsv = data.map((row) =>
    columns.map((col) => {
      const v = getCellValue(col, row);
      return v ?? "";
    })
  );

  exportToCSV(headers, rowsForCsv, csvFilename);
}

function buildXmlRowAction<T>(
  getXmlContent: ((row: T) => any) | undefined,
  getFilename: ((row: T) => string) | undefined,
  setXmlErrorMsg: (msg: string | undefined) => void,
): GenericRowAction<T> {
  const xmlGetter: (row: T) => any = getXmlContent ?? ((row: any) => row?.xmlContent);
  const nameGetter: (row: T) => string = getFilename ?? ((row: T) => `${getStandardFilename(row)}.xml`);
  return {
    title: "Exportar XML",
    icon: xmlIconUrl,
    onClick: (row: T) => {
      xmlGetter(row)
        .then((xml: string | null | undefined) => {
          if (!xml?.trim()) { setXmlErrorMsg("Error al obtener el XML"); return; }
          downloadXML(xml, nameGetter(row));
        })
        .catch((err: unknown) => {
          console.error(err);
          const payload = parseFetchError(err) ?? { message: "Error al obtener el XML" };
          setXmlErrorMsg([payload.errorCode, payload.message].filter(Boolean).join(" - "));
        });
    },
  };
}

function buildPdfRowAction<T>(
  getPdfUrl: ((row: T) => string | null | undefined) | undefined,
  setPdfErrorMsg: (msg: string | undefined) => void,
): GenericRowAction<T> {
  const pdfUrlGetter: (row: T) => string | null | undefined = getPdfUrl ?? ((row: any) => {
    const baseUrl = process.env.API_BASE_URL ?? "";
    const fiscalUuid = row?.invoiceUuid;
    if (!fiscalUuid) return null;
    return `${baseUrl}/invoices/${fiscalUuid}/pdf`;
  });
  return {
    title: "Descargar PDF",
    icon: pdfIconUrl,
    onClick: (row: T) => {
      fetchAndDownloadPdf(pdfUrlGetter(row), `${getStandardFilename(row)}.pdf`, setPdfErrorMsg);
    },
  };
}

function DataGridInner<T, F = any>(
  {
    rows: externalRows,
    loading: externalLoading,
    columns,
    getRowId,
    page: externalPage,
    perPage: externalPerPage,
    totalPages: externalTotalPages,
    totalItems: externalTotalItems,
    emptyLabel,
    selectable = true,
    onChangePage: externalOnChangePage,
    onChangePerPage: externalOnChangePerPage,

    /** Paginación automática */
    fetchFn,
    filters,
    initialPage = 0,
    initialSize = 10,

    /** acciones internas */
    enableCsv = true,
    csvFilename = "Export",
    hideCsvToolbar = false,
    onExportAvailabilityChange,
    enableXml = false,
    enablePdf = false,
    getPdfUrl,
    getXmlContent,
    getFilename,
    rowActions: customRowActions,
    filtersEmpty = false,
    emptyFiltersMessage = "Realiza una búsqueda en los filtros para mostrar resultados.",
    fetchEnabled = false,
    xmlAppEvent,
    pdfAppEvent,
  }: DataGridProps<T, F>,
  ref: React.ForwardedRef<DataGridHandle>
): ReactElement {
  const sec = useSecurityContext();
  const canXml = !xmlAppEvent || sec.hasEvent(xmlAppEvent.app, xmlAppEvent.event);
  const canPdf = !pdfAppEvent || sec.hasEvent(pdfAppEvent.app, pdfAppEvent.event);

  const pagedEnabled = Boolean(fetchFn) && filters != null;
  const paginatedDataResult = usePaginatedData<T, F>({
    fetchFn: fetchFn ?? (() => Promise.resolve({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 0 })),
    initialFilters: (filters ?? {}) as F,
    initialPage,
    initialSize,
    fetchEnabled: pagedEnabled && fetchEnabled,
    searchToken: 0,
  } as UsePaginatedDataOptions<T, F>);
  const paginatedData = pagedEnabled ? paginatedDataResult : null;

  const rows = paginatedData ? paginatedData.rows : (externalRows || []);
  const loading = paginatedData ? paginatedData.loading : (externalLoading || false);
  const fetchError = paginatedData?.error ?? null;
  const page = paginatedData ? paginatedData.page : (externalPage || 1);
  const perPage = paginatedData ? paginatedData.size : (externalPerPage || 25);
  const totalPages = paginatedData ? paginatedData.totalPages : (externalTotalPages || Math.ceil(rows.length / perPage));
  const totalItems = paginatedData ? paginatedData.totalItems : (externalTotalItems || rows.length);
  const onChangePage = paginatedData ? paginatedData.changePage : externalOnChangePage;
  const onChangePerPage = paginatedData ? paginatedData.changePerPage : externalOnChangePerPage;
  const effectiveEmptyLabel = emptyLabel || (loading ? "Cargando..." : "Sin resultados");
  const [selectedIds, setSelectedIds] = useState<Set<string | number>>(new Set());
  const [processing, setProcessing] = useState<boolean>(false);
  const [xmlErrorMsg, setXmlErrorMsg] = useState<string | undefined>(undefined);
  const [pdfErrorMsg, setPdfErrorMsg] = useState<string | undefined>(undefined);
  const [emptySearchAlertOpen, setEmptySearchAlertOpen] = useState(false);
  const prevLoadingRef = useRef(false);

  useEffect(() => {
    const emptyOutcome = rows.length === 0 && !filtersEmpty;
    if (prevLoadingRef.current === true && !loading && emptyOutcome) {
      setEmptySearchAlertOpen(true);
    }
    if (rows.length > 0 || filtersEmpty || loading) {
      setEmptySearchAlertOpen(false);
    }
    prevLoadingRef.current = loading;
  }, [loading, filtersEmpty, rows.length]);

  /** -------- selección -------- */
  useEffect(() => setSelectedIds(new Set()), [rows]);

  const toggleRow = useCallback((row: T) => {
    const id = getRowId(row);
    setSelectedIds(prev => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }, [getRowId]);

  const allSelected = useMemo(
    () => selectedIds.size === rows.length && rows.length > 0,
    [selectedIds, rows]
  );

  const toggleAll = useCallback(() => {
    setSelectedIds(prev => {
      if (prev.size === rows.length) return new Set();
      return new Set(rows.map(getRowId));
    });
  }, [rows, getRowId]);

  const selectedRows = useMemo(
    () => rows.filter(r => selectedIds.has(getRowId(r))),
    [rows, selectedIds, getRowId]
  );

  const runCsvExport = useCallback(() => {
    if (!enableCsv || rows.length === 0) return;
    const data = selectedRows.length ? selectedRows : rows;
    exportDataGridToCsv(columns, data, csvFilename);
  }, [enableCsv, rows, selectedRows, columns, csvFilename]);

  useImperativeHandle(ref, () => ({
    exportCsv: runCsvExport,
  }), [runCsvExport]);

  useEffect(() => {
    if (!onExportAvailabilityChange) return;
    onExportAvailabilityChange(
      Boolean(enableCsv && rows.length > 0 && !loading && !filtersEmpty)
    );
  }, [
    onExportAvailabilityChange,
    enableCsv,
    rows.length,
    loading,
    filtersEmpty,
  ]);

  /** -------- columna de selección (TableColumn<T>) -------- */
  const selectionColumn: TableColumn<T> | null = selectable
    ? {
        header: (
          <input
            type="checkbox"
            checked={allSelected}
            onChange={toggleAll}
            aria-label="Seleccionar todo"
          />
        ),
        render: (row: T) => (
          <input
            type="checkbox"
            checked={selectedIds.has(getRowId(row))}
            onChange={() => toggleRow(row)}
            aria-label="Seleccionar fila"
          />
        ),
        align: "center",
      }
    : null;

  /** -------- conversión a TableColumn<T> con render siempre definido -------- */
  const toTableColumns = useCallback(
    (cols: DataGridColumn<T>[]): TableColumn<T>[] => {
      const base = cols.map(({ header, align, render, accessor }) => {
        const renderWithNav = (row: T, _nav?: any) => {
          if (typeof render === "function") return render(row);
          if (typeof accessor === "function") return accessor(row);
          return null;
        };
        return { header, align, render: renderWithNav } as TableColumn<T>;
      });

      if (selectable && selectionColumn) return [selectionColumn, ...base];
      return base;
    },
    [selectable, selectionColumn]
  );

  const tableColumns = useMemo(() => toTableColumns(columns), [columns, toTableColumns]);

  /** -------- acciones por fila internas (XML y PDF) -------- */
  const internalRowActions: GenericRowAction<T>[] = useMemo(() => {
    const actions: GenericRowAction<T>[] = [];
    if (enableXml && canXml) actions.push(buildXmlRowAction(getXmlContent, getFilename, setXmlErrorMsg));
    if (enablePdf && canPdf) actions.push(buildPdfRowAction(getPdfUrl, setPdfErrorMsg));
    return actions;
  }, [enableXml, enablePdf, canXml, canPdf, getXmlContent, getFilename, getPdfUrl]);

  const allRowActions = useMemo(
    () => [...(customRowActions ?? []), ...internalRowActions],
    [customRowActions, internalRowActions]
  );

  /** -------- acción masiva interna (CSV) -------- */
  const internalBulkActions: BulkAction<T>[] = useMemo(() => {
    if (!enableCsv || hideCsvToolbar) return [];

    const csvAction: BulkAction<T> = {
      label: "Exportar a CSV",
      value: "csv",
      run: (selected, all) => {
        const data = selected.length ? selected : all;
        exportDataGridToCsv(columns, data, csvFilename);
      },
    };

    return [csvAction];
  }, [enableCsv, hideCsvToolbar, columns, csvFilename]);

  /** -------- botones de acción masiva -------- */
  const onBulkClick = async (action: BulkAction<T>) => {
    try {
      setProcessing(true);
      await Promise.resolve(action.run(selectedRows.length ? selectedRows : rows, rows));
    } finally {
      setProcessing(false);
    }
  };

  const xmlErrorModal = (
    <GenericModal
      visible={!!xmlErrorMsg}
      variant="alert"
      severity="error"
      title="Error"
      message={xmlErrorMsg || ""}
      buttonText="Aceptar"
      onClose={() => setXmlErrorMsg(undefined)}
      onConfirm={() => setXmlErrorMsg(undefined)}
    />
  );

  const pdfErrorModal = (
    <GenericModal
      visible={!!pdfErrorMsg}
      variant="alert"
      severity="error"
      title="Error al descargar PDF"
      message={pdfErrorMsg || ""}
      buttonText="Aceptar"
      onClose={() => setPdfErrorMsg(undefined)}
      onConfirm={() => setPdfErrorMsg(undefined)}
    />
  );

  const processingModal = (
    <GenericModal
      visible={processing}
      variant="loading"
      message="Se está procesando tu acción..."
    />
  );

  let tableEmptyLabel = effectiveEmptyLabel;
  if (loading) {
    tableEmptyLabel = "Cargando...";
  } else if (fetchError) {
    tableEmptyLabel = [fetchError.errorCode, fetchError.message].filter(Boolean).join(" - ") || "Error al obtener los datos";
  }

  const showOutcomeAlert = emptySearchAlertOpen && !filtersEmpty && !loading && rows.length === 0;
  const outcomeTitle = fetchError ? "Error" : "Sin resultados obtenidos";
  const outcomeSeverity = fetchError ? ("error" as const) : ("warning" as const);
  const outcomeMessage = fetchError
    ? tableEmptyLabel
    : "No se encontraron registros para los criterios de búsqueda indicados.";

  const emptySearchModal = (
    <GenericModal
      visible={showOutcomeAlert}
      variant="alert"
      severity={outcomeSeverity}
      title={outcomeTitle}
      message={outcomeMessage}
      buttonText="Aceptar"
      onClose={() => setEmptySearchAlertOpen(false)}
      onConfirm={() => setEmptySearchAlertOpen(false)}
    />
  );

  const displayPage = page === 0 ? 1 : page;

  /** -------- render tabla (incluye vacío con encabezados, estilo Carta Porte) -------- */
  return (
    <>
      <div className="results-container">
        {internalBulkActions.length > 0 && (
          <div className="fiscal-flex fiscal-gap-2 fiscal-flex-wrap fiscal-justify-end fiscal-mt-4">
            {internalBulkActions.map((action) => (
              <GenericButton
                key={action.value}
                variant="primary"
                disabled={processing}
                onClick={() => onBulkClick(action)}
              >
                {action.label}
              </GenericButton>
            ))}
          </div>
        )}
        <GenericTable<T>
          rows={rows}
          columns={tableColumns}
          actions={allRowActions}
          emptyLabel={tableEmptyLabel}
          perPage={perPage}
          page={displayPage}
          totalPages={Math.max(1, totalPages)}
          totalItems={totalItems}
          onChangePage={onChangePage}
          onChangePerPage={onChangePerPage}
        />
      </div>
      {processingModal}
      {xmlErrorModal}
      {pdfErrorModal}
      {emptySearchModal}
    </>
  );
}

const DataGrid = forwardRef(DataGridInner) as <T, F = any>(
  props: DataGridProps<T, F> & { ref?: React.Ref<DataGridHandle> }
) => ReactElement;

export default DataGrid;

/* ============================================================
 * Opcional: helper reusado 'dentro de Grid' para crear onFilter
 * (no es prop del Grid; vive en este módulo).
 * ============================================================ */
type CreateOnFilterOptions<T, F> = {
  fetchFn: (filters: F) => Promise<any>;
  pickRows?: (response: any) => T[];
  setLoading: (b: boolean) => void;
  setRows: (rows: T[]) => void;
  onError?: (err: unknown) => void;
};

export function createOnFilter<T, F>({
  fetchFn,
  pickRows = (res: any) => res?.content ?? res ?? [],
  setLoading,
  setRows,
  onError,
}: CreateOnFilterOptions<T, F>) {
  return async (filters: F) => {
    try {
      setLoading(true);
      const result = await fetchFn(filters);
      const data = pickRows(result) ?? [];
      setRows(data);
    } catch (err) {
      onError?.(err);
      console.error("Error en onFilter:", err);
      setRows([]);
    } finally {
      setLoading(false);
    }
  };
}
