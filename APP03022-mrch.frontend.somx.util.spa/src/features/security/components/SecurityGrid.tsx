import { useEffect, useMemo, useState, type ReactNode } from 'react';
import GenericButton from '@shared/components/ui/button/GenericButton';
import type { SecurityRow } from '../types';
import { SecurityTablePagination } from './SecurityTablePagination';

interface Props {
  items: SecurityRow[];
  title?: string;
  actionLabel?: string;
  actionIcon?: ReactNode;
  onAction: (row: SecurityRow) => void;
}

const defaultActionIcon = (
  <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
    <path
      d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7S1 12 1 12Z"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
    <circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="2" />
  </svg>
);

function buildCsvFilename(title?: string): string {
  const now = new Date();
  const dd = String(now.getDate()).padStart(2, '0');
  const mm = String(now.getMonth() + 1).padStart(2, '0');
  const yyyy = String(now.getFullYear());
  const hh = String(now.getHours()).padStart(2, '0');
  const min = String(now.getMinutes()).padStart(2, '0');
  const safeTitle = (title ?? 'security-grid')
    .trim()
    .replace(/[\\/:*?"<>|]/g, '')
    .replace(/\s+/g, '_');
  return `${safeTitle}-${dd}-${mm}-${yyyy}-${hh}-${min}.csv`;
}

export function SecurityGrid({ items, title, actionLabel = 'Ver detalle', actionIcon = defaultActionIcon, onAction }: Props) {
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [selectedRowIds, setSelectedRowIds] = useState<Set<number>>(new Set());

  const totalPages = Math.max(1, Math.ceil(items.length / pageSize));
  const pageItems = useMemo(() => {
    const start = (currentPage - 1) * pageSize;
    const end = start + pageSize;
    return items.slice(start, end);
  }, [items, currentPage, pageSize]);

  useEffect(() => {
    setCurrentPage(1);
  }, [items.length, pageSize]);

  useEffect(() => {
    setSelectedRowIds((prev) => {
      const validIds = new Set(items.map((item) => item.id));
      const next = new Set<number>();
      prev.forEach((id) => {
        if (validIds.has(id)) next.add(id);
      });
      return next;
    });
  }, [items]);

  const areAllRowsSelected = items.length > 0 && items.every((item) => selectedRowIds.has(item.id));

  const handleExportCsv = () => {
    const headers = ['id', 'clave', 'nombre', 'estatus', 'totalAsignados'];
    const sourceRows = selectedRowIds.size
      ? items.filter((item) => selectedRowIds.has(item.id))
      : items;
    const rows = sourceRows.map((item) => [
      item.id,
      item.catalogKey,
      item.name,
      item.status === 1 ? 'Activo' : 'Inactivo',
      item.totalAssigned,
    ]);
    const csv = [
      headers.join(','),
      ...rows.map((row) =>
        row
          .map((value) => `"${String(value ?? '').replace(/"/g, '""')}"`)
          .join(','),
      ),
    ].join('\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = buildCsvFilename(title);
    link.click();
    window.URL.revokeObjectURL(url);
  };

  const toggleRowSelection = (id: number) => {
    setSelectedRowIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const toggleSelectAll = () => {
    setSelectedRowIds((prev) => {
      if (items.length > 0 && items.every((item) => prev.has(item.id))) {
        return new Set();
      }
      return new Set(items.map((item) => item.id));
    });
  };

  return (
    <>
      <div className="security-grid-toolbar">
        <div>{selectedRowIds.size > 0 ? `${selectedRowIds.size} seleccionados` : 'Sin selección'}</div>
        <GenericButton variant="primary" onClick={handleExportCsv}>
          Exportar CSV
        </GenericButton>
      </div>
      <table className="security-table">
        <thead>
          <tr>
            <th>
              <input
                type="checkbox"
                checked={areAllRowsSelected}
                onChange={toggleSelectAll}
                aria-label="Seleccionar todos los registros"
              />
            </th>
            <th>ID</th>
            <th>Clave</th>
            <th>Nombre</th>
            <th>Estatus</th>
            <th>Total asignados</th>
            <th>Accion</th>
          </tr>
        </thead>
        <tbody>
          {pageItems.map((item) => (
            <tr key={item.id}>
              <td>
                <input
                  type="checkbox"
                  checked={selectedRowIds.has(item.id)}
                  onChange={() => toggleRowSelection(item.id)}
                  aria-label={`Seleccionar registro ${item.id}`}
                />
              </td>
              <td>{item.id}</td>
              <td>{item.catalogKey}</td>
              <td>{item.name}</td>
              <td>
                <span className={`security-status ${item.status === 1 ? 'active' : 'inactive'}`}>
                  {item.status === 1 ? 'Activo' : 'Inactivo'}
                </span>
              </td>
              <td>{item.totalAssigned}</td>
              <td className="security-action-cell">
                <GenericButton
                  variant="outline"
                  className="security-action-btn"
                  title={actionLabel}
                  aria-label={actionLabel}
                  onClick={() => onAction(item)}
                >
                  <span className="security-action-icon">{actionIcon}</span>
                </GenericButton>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <SecurityTablePagination
        currentPage={currentPage}
        totalPages={totalPages}
        totalItems={items.length}
        pageSize={pageSize}
        onPrev={() => setCurrentPage((prev) => Math.max(1, prev - 1))}
        onNext={() => setCurrentPage((prev) => Math.min(totalPages, prev + 1))}
        onPageSizeChange={setPageSize}
      />
    </>
  );
}

