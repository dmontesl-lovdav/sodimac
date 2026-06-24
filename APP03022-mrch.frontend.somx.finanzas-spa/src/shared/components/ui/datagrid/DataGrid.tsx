import React, { ReactElement, useMemo, useState, useCallback, useEffect } from "react";
import GenericTable, {
  Column as TableColumn,
  RowAction as GenericRowAction,
} from "@/shared/components/ui/table/DataTable";
import { GenericSelect } from "@/shared/components/ui";
import type { ChangeEvent } from "react";
import SimpleLobby from "../lobby/Lobby";
import xmlIconUrl from "@assets/xml.svg";
import pdfIconUrl from "@assets/pdf.svg";
import { exportToCSV, getStandardFilename } from "@/utils/utils";
import { usePaginatedData } from "@/shared/hooks/usePaginatedData";

/** Reexporta los tipos de GenericTable (no tienen 'accessor') */
export type Column<T> = TableColumn<T>;
export type RowAction<T> = GenericRowAction<T>;

/** Tipo interno del DataGrid que sí soporta 'accessor' */
export type DataGridColumn<T> = {
  header: string | React.ReactNode;
  align?: "left" | "center" | "right";
  render?: (row: T) => React.ReactNode;
  accessor?: (row: T) => React.ReactNode;
  exportHeader?: string;
  exportAccessor?: (row: T) => string | number | null | undefined;
};

export type BulkAction<T> = {
  label: string;
  value: string;
  run: (selected: T[], all: T[]) => Promise<void> | void;
};

type DataGridProps<T, F extends Record<string, any> = Record<string, any>> = {
  rows?: T[];
  loading?: boolean;
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
    size: number;
  }>;
  filters?: F;
  initialPage?: number;
  initialSize?: number;

  /** --- Configuración de acciones internas --- */
  enableCsv?: boolean;
  enablePdf?: boolean;
  getPdfUrl?: (row: T) => string | null | undefined;
  csvFilename?: string;
  enableXml?: boolean;
  getXmlContent?: (row: T) => string | null | undefined;
  getFilename?: (row: T) => string;
  rowActions?: GenericRowAction<T>[];
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
 * Helper interno: descargar XML.
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

export default function DataGrid<T, F extends Record<string, any> = Record<string, any>>({
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
  csvFilename = "Listado",
  enableXml = false,
  enablePdf = false,
  getPdfUrl,
  getXmlContent,
  getFilename,
  rowActions: customRowActions,
}: DataGridProps<T, F>): ReactElement {
  const paginatedData =
    fetchFn && filters
      ? usePaginatedData<T, F>({
        fetchFn,
        initialFilters: filters,
        initialPage,
        initialSize,

      })
      : null;

  const rows = paginatedData ? paginatedData.rows : externalRows || [];
  const loading = paginatedData ? paginatedData.loading : externalLoading || false;
  const page = paginatedData ? paginatedData.page : externalPage || 1;
  const perPage = paginatedData ? paginatedData.size : externalPerPage || 25;
  const totalPages = paginatedData
    ? paginatedData.totalPages
    : externalTotalPages || Math.ceil(rows.length / perPage || 1);
  const totalItems = paginatedData ? paginatedData.totalItems : externalTotalItems || rows.length;
  const onChangePage = paginatedData ? paginatedData.changePage : externalOnChangePage;
  const onChangePerPage = paginatedData ? paginatedData.changePerPage : externalOnChangePerPage;
  const effectiveEmptyLabel = emptyLabel || (loading ? "Cargando..." : "Sin resultados");

  const [selectedIds, setSelectedIds] = useState<Set<string | number>>(new Set());
  const [bulkValue, setBulkValue] = useState<string>("");
  const [processing, setProcessing] = useState<boolean>(false);

  useEffect(() => {
    setSelectedIds(new Set());
  }, [rows]);

  const toggleRow = useCallback(
    (row: T) => {
      const id = getRowId(row);
      setSelectedIds((prev) => {
        const next = new Set(prev);
        if (next.has(id)) next.delete(id);
        else next.add(id);
        return next;
      });
    },
    [getRowId]
  );

  const allSelected = useMemo(
    () => selectedIds.size === rows.length && rows.length > 0,
    [selectedIds, rows]
  );

  const toggleAll = useCallback(() => {
    setSelectedIds((prev) => {
      if (prev.size === rows.length) return new Set();
      return new Set(rows.map(getRowId));
    });
  }, [rows, getRowId]);

  const selectedRows = useMemo(
    () => rows.filter((r) => selectedIds.has(getRowId(r))),
    [rows, selectedIds, getRowId]
  );

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

  const toTableColumns = useCallback(
    (cols: DataGridColumn<T>[]): TableColumn<T>[] => {
      const base = cols.map(({ header, align, render, accessor }) => {
        const renderWithNav = (row: T, _nav?: any) => {
          if (typeof render === "function") return render(row);
          if (typeof accessor === "function") return accessor(row);
          return null;
        };

        return {
          header,
          align,
          render: renderWithNav,
        } as TableColumn<T>;
      });

      if (selectable && selectionColumn) return [selectionColumn, ...base];
      return base;
    },
    [selectable, selectionColumn]
  );

  const tableColumns = useMemo(() => toTableColumns(columns), [columns, toTableColumns]);

  const resolveFilename = useCallback(
    (row: T) => {
      if (typeof getFilename === "function") return getFilename(row);
      return getStandardFilename(row);
    },
    [getFilename]
  );

  const internalRowActions: GenericRowAction<T>[] = useMemo(() => {
    const actions: GenericRowAction<T>[] = [];

    if (enableXml) {
      const xmlGetter: (row: T) => string | null | undefined =
        getXmlContent ?? ((row: any) => row?.xmlContent);

      actions.push({
        title: "Exportar XML",
        icon: xmlIconUrl,
        onClick: (row: T) => {
          const xml = xmlGetter(row);
          const fname = `${resolveFilename(row)}.xml`;
          downloadXML(xml, fname);
        },
      });
    }

    if (enablePdf) {
      const pdfUrlGetter: (row: T) => string | null | undefined =
        getPdfUrl ??
        ((row: any) => {
          const baseUrl = process.env.API_BASE_URL || "";
          const fiscalUuid = row?.fiscalUuid;
          if (!fiscalUuid) return null;
          return `${baseUrl.replace(/\/+$/, "")}/pdf/from-uuid/${fiscalUuid}?inline=true`;
        });

      actions.push({
        title: "Descargar PDF",
        icon: pdfIconUrl,
        onClick: (row: T) => {
          const url = pdfUrlGetter(row);
          const fname = `${resolveFilename(row)}.pdf`;
          downloadPDF(url, fname);
        },
      });
    }

    return actions;
  }, [enableXml, enablePdf, getXmlContent, getPdfUrl, resolveFilename]);

  const allRowActions = useMemo(
    () => [...(customRowActions ?? []), ...internalRowActions],
    [customRowActions, internalRowActions]
  );

  const internalBulkActions: BulkAction<T>[] = useMemo(() => {
    if (!enableCsv) return [];

    return [
      {
        label: "Exportar CSV",
        value: "csv",
        run: (selected, all) => {
          const data = selected.length ? selected : all;

          const headers = columns.map((col) => col.exportHeader ?? headerToString(col.header, ""));

          const rowsForCsv = data.map((row) =>
            columns.map((col) => {
              const v = getCellValue<T>(col, row);
              return v ?? "";
            })
          );

          exportToCSV(headers, rowsForCsv, csvFilename);
        },
      },
    ];
  }, [enableCsv, columns, csvFilename]);

  const onBulkChange = async (e: ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value;
    setBulkValue(value);

    const action = internalBulkActions.find((a) => a.value === value);
    if (!action) return;

    try {
      setProcessing(true);
      await Promise.resolve(action.run(selectedRows.length ? selectedRows : rows, rows));
    } finally {
      setProcessing(false);
      setBulkValue("");
    }
  };

  if (!loading && rows.length === 0) {
    return <SimpleLobby message="Sin resultados" />;
  }

  return (
    <div className="results-container">
      <GenericTable<T>
        rows={rows}
        columns={tableColumns}
        actions={allRowActions}
        emptyLabel={effectiveEmptyLabel}
        perPage={perPage}
        page={page}
        totalPages={totalPages}
        totalItems={totalItems}
        onChangePage={onChangePage}
        onChangePerPage={onChangePerPage}
      />

      {internalBulkActions.length > 0 && (
        <div className="somx-action-container">
          {processing ? (
            <p>Se está procesando tu acción...</p>
          ) : (
            <GenericSelect
              label="Acción"
              placeholder="Selecciona una acción"
              value={bulkValue}
              onChange={onBulkChange}
              options={internalBulkActions.map((a) => ({
                label: a.label,
                value: a.value,
              }))}
            />
          )}
        </div>
      )}
    </div>
  );
}

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