
import React, { ReactElement, useMemo, useState, useCallback, useEffect } from "react";
import GenericTable, {
  Column as TableColumn,
  RowAction as GenericRowAction,
} from "@/shared/components/ui/table/GenericTable";
import { GenericSelect } from "@/shared/components/ui";
import type { ChangeEvent } from "react";
import SimpleLobby from "../lobby/Lobby";
import xmlIconUrl from "@assets/xml.svg"; 
import { exportToCSV } from "@/utils/utils";

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

type DataGridProps<T> = {
  rows: T[];
  loading: boolean;
  /** Permite pasar columnas del DataGrid (con accessor) */
  columns: DataGridColumn<T>[];
  getRowId: (row: T) => string | number;
  page?: number;
  perPage?: number;
  totalPages?: number;
  emptyLabel?: string;
  selectable?: boolean;

  /** --- Configuración de acciones internas --- */
  enableCsv?: boolean;                 // default: true
  csvFilename?: string;                // default: "Export"
  enableXml?: boolean;                 // default: false
  getXmlContent?: (row: T) => string | null | undefined;
  getXmlFilename?: (row: T) => string; // default: `NC-{folio|invoiceUuid|getRowId}`
};

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

export default function DataGrid<T>({
  rows,
  loading,
  columns,
  getRowId,
  page = 1,
  perPage = 25,
  totalPages = rows.length/perPage,
  emptyLabel = loading ? "Cargando..." : "Sin resultados",
  selectable = true,

  /** acciones internas */
  enableCsv = true,
  csvFilename = "Export",
  enableXml = false,
  getXmlContent,
  getXmlFilename,
}: DataGridProps<T>): ReactElement {
  const [selectedIds, setSelectedIds] = useState<Set<string | number>>(new Set());
  const [bulkValue, setBulkValue] = useState<string>("");
  const [processing, setProcessing] = useState<boolean>(false);

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

  /** -------- acciones por fila internas (XML) -------- */
  const internalRowActions: GenericRowAction<T>[] = useMemo(() => {
    if (!enableXml) return [];

    const xmlGetter: (row: T) => string | null | undefined =
      getXmlContent ??
      ((row: any) => row?.xmlContent); // auto-detección por convención

    const nameGetter: (row: T) => string =
      getXmlFilename ??
      ((row: any) => {
        const folio = row?.folio;
        const inv = row?.invoiceUuid;
        const id = getRowId(row);
        return `NC-${folio || inv || id}`;
      });

    const action: GenericRowAction<T> = {
      title: "Exportar XML",
      icon: xmlIconUrl,
      onClick: (row: T) => {
        const xml = xmlGetter(row);
        const fname = nameGetter(row);
        downloadXML(xml, fname);
      },
    };

    return [action];
  }, [enableXml, getXmlContent, getXmlFilename, getRowId]);

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

  /** -------- select de acción masiva -------- */
  const onBulkChange = async (e: ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value;
    setBulkValue(value);
    const action = internalBulkActions.find(a => a.value === value);
    if (!action) return;
    try {
      setProcessing(true);
      await Promise.resolve(action.run(selectedRows.length ? selectedRows : rows, rows));
    } finally {
      setProcessing(false);
      setBulkValue("");
    }
  };

  /** -------- lobby vacío -------- */
  if (rows.length === 0) {
    return <SimpleLobby message="Sin resultados" />;
  }

  /** -------- render -------- */
  return (
    <div className="results-container">
      <GenericTable<T>
        rows={rows}
        columns={tableColumns}
        actions={internalRowActions}
        emptyLabel={emptyLabel}
        perPage={perPage}
        page={page}
        totalPages={totalPages}
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
              options={internalBulkActions.map(a => ({ label: a.label, value: a.value }))}
            />
          )}
        </div>
      )}
    </div>
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
