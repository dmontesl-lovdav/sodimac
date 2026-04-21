
import React, { ReactElement, useMemo, useState, useCallback, useEffect } from "react";
import GenericTable, {
  Column as TableColumn,
  RowAction as GenericRowAction,
} from "@/shared/components/ui/table/DataTable";
import { GenericButton, GenericModal } from "@/shared/components/ui";
import SimpleLobby from "../lobby/Lobby";
import xmlIconUrl from "@assets/xml.svg";
import pdfIconUrl from "@assets/pdf.svg";
import { exportToCSV, getStandardFilename } from "@/utils/utils";
import { usePaginatedData, type UsePaginatedDataOptions, parseFetchError } from "@/shared/components/ui/datagrid/hooks/usePaginatedData";
import { ModalMsg } from "@/shared/components/ui/modal/ModalMsg";

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
  enableXml?: boolean;                 // default: false
  getXmlContent?: (row: T) => string | null | undefined | Promise<string | null | undefined>;
  getFilename?: (row: T) => string; // default: `{folio|invoiceUuid|getRowId}`
  /** Acciones por fila adicionales (se muestran antes de XML/PDF) */
  rowActions?: GenericRowAction<T>[];

  /** Si es true y no hay filas, se muestra SimpleLobby con este mensaje (ej. "Realiza una búsqueda en los filtros"). */
  filtersEmpty?: boolean;
  emptyFiltersMessage?: string;
};

/** ------------------------------------------------------------
 * Helper interno: descargar PDF desde una URL.
 * ------------------------------------------------------------ */
function downloadPDF(pdfUrl?: string | null, filename: string = "documento.pdf") {
  const url = (pdfUrl ?? "").trim();
  if (!url) {
    console.warn("downloadPDF: URL vacía o nula.");
    return;
  }
  const safeName = filename.toLowerCase().endsWith(".pdf") ? filename : `${filename}.pdf`;
  const a = document.createElement("a");
  a.href = url;
  a.download = safeName;
  a.style.display = "none";
  document.body.appendChild(a);
  a.click();
  requestAnimationFrame(() => {
    document.body.removeChild(a);
  });
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
/* ts-ignore */
export default function DataGrid<T, F = any>({
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
  enableXml = false,
  enablePdf = false,
  getPdfUrl,
  getXmlContent,
  rowActions: customRowActions,
  filtersEmpty = false,
  emptyFiltersMessage="Realiza una búsqueda en los filtros para mostrar resultados.",
}: DataGridProps<T, F>): ReactElement {
  // Si se pasa fetchFn, usar paginación automática
  const paginatedData = fetchFn && filters ? usePaginatedData<T, F>({
    fetchFn,
    initialFilters: filters,
    initialPage,
    initialSize,
  } as UsePaginatedDataOptions<T, F>) : null;

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

    // Acción XML
    if (enableXml) {
      const xmlGetter: (row: T) => string | null | undefined | Promise<string | null | undefined> =
        getXmlContent ??
        ((row: any) => row?.xmlContent); // auto-detección por convención

      const nameGetter: (row: T) => string = (row: T) => getStandardFilename(row)+".xml";

      actions.push({
        title: "Exportar XML",
        icon: xmlIconUrl,
        onClick: async (row: T) => {
          try {
            const xml = await xmlGetter(row);
            if (!xml?.trim()) {
              setXmlErrorMsg("Error al obtener el XML");
              return;
            }
            downloadXML(xml, nameGetter(row));
          } catch (err) {
            console.error(err);
            const payload = parseFetchError(err) ?? { message: "Error al obtener el XML" };
            setXmlErrorMsg([payload.errorCode, payload.message].filter(Boolean).join(" - "));
          }
        },
      });
    }

    // Acción PDF
    if (enablePdf) {
      const pdfUrlGetter: (row: T) => string | null | undefined =
        getPdfUrl ??
        ((row: any) => {
          const baseUrl = process.env.API_BASE_URL || "";
          const fiscalUuid = row?.fiscalUuid;
          if (!fiscalUuid) return null;
          return `${baseUrl}pdf/from-uuid/${fiscalUuid}?inline=true`;
        });

      const pdfNameGetter: (row: T) => string = (row: T) => getStandardFilename(row)+".pdf";

      actions.push({
        title: "Descargar PDF",
        icon: pdfIconUrl,
        onClick: (row: T) => {
          const url = pdfUrlGetter(row);
          const fname = pdfNameGetter(row);
          downloadPDF(url, fname);
        },
      });
    }

    return actions;
  }, [enableXml, enablePdf, getXmlContent, getStandardFilename, getPdfUrl, getRowId]);

  const allRowActions = useMemo(
    () => [...(customRowActions ?? []), ...internalRowActions],
    [customRowActions, internalRowActions]
  );

  /** -------- acción masiva interna (CSV) -------- */
  const internalBulkActions: BulkAction<T>[] = useMemo(() => {
    if (!enableCsv) return [];

    const csvAction: BulkAction<T> = {
      label: "Exportar a CSV",
      value: "csv",
      run: (selected, all) => {
        const data = selected.length ? selected : all;

        const headers = columns.map(col =>
          col.exportHeader ?? headerToString(col.header, "")
        );

        const rowsForCsv = data.map(row =>
          columns.map(col => {
            const v = getCellValue<T>(col, row);
            return v ?? "";
          })
        );

        exportToCSV(headers, rowsForCsv, csvFilename);
      },
    };

    return [csvAction];
  }, [enableCsv, columns, csvFilename]);

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
    <ModalMsg
      severity="error"
      visible={!!xmlErrorMsg}
      msg={xmlErrorMsg || ""}
      onClose={() => setXmlErrorMsg(undefined)}
    />
  );

  const processingModal = (
    <GenericModal
      visible={processing}
      variant="loading"
      message="Se está procesando tu acción..."
    />
  );

  /** -------- lobby vacío o error de búsqueda -------- */
  if (rows.length === 0) {
    const message =
      filtersEmpty && emptyFiltersMessage
        ? emptyFiltersMessage
        : loading
          ? "Cargando..."
          : fetchError
            ? [fetchError.errorCode, fetchError.message].filter(Boolean).join(" - ") || "Error al obtener los datos"
            : "Sin resultados";
    return (
      <>
        <SimpleLobby message={message} error={!!fetchError && !filtersEmpty} />
        {xmlErrorModal}
      </>
    );
  }

  /** -------- render tabla -------- */
  return (
    <>
      <div className="results-container">
         {internalBulkActions.length > 0 && (
          <div className="fiscal-flex fiscal-gap-2 fiscal-flex-wrap fiscal-justify-end fiscal-mt-4">
            {internalBulkActions.map(action => (
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
          emptyLabel={emptyLabel}
          perPage={perPage}
          page={page}
          totalPages={totalPages}
          totalItems={totalItems}
          onChangePage={onChangePage}
          onChangePerPage={onChangePerPage}
        />
       
      </div>
      {processingModal}
      {xmlErrorModal}
    </>
  );
}

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
