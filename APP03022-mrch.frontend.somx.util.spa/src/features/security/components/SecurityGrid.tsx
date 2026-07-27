import { useEffect, useMemo, useState, type ReactNode } from 'react';
import GenericButton from '@shared/components/ui/button/GenericButton';
import { GenericTable } from '@shared/components/ui/table';
import type { SecurityRow } from '../types';

interface Props {
  items: SecurityRow[];
  title?: string;
  actionLabel?: string;
  actionIcon?: ReactNode;
  onAction: (row: SecurityRow) => void;
  onSelectionChange?: (selectedIds: number[]) => void;
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

export function SecurityGrid({ items, title: _title, actionLabel = 'Ver detalle', actionIcon = defaultActionIcon, onAction, onSelectionChange }: Readonly<Props>) {
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [selectedRowIds, setSelectedRowIds] = useState<Set<number>>(new Set());

  useEffect(() => {
    onSelectionChange?.(Array.from(selectedRowIds));
  }, [selectedRowIds, onSelectionChange]);

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

  const columns = useMemo(
    () => [
      {
        header: 'ID',
        render: (item: SecurityRow) => item.id,
      },
      {
        header: 'Clave',
        render: (item: SecurityRow) => item.catalogKey,
      },
      {
        header: 'Nombre',
        render: (item: SecurityRow) => item.name,
      },
      {
        header: 'Estatus',
        render: (item: SecurityRow) => (
          <span className={`security-status ${item.status === 1 ? 'active' : 'inactive'}`}>
            {item.status === 1 ? 'Activo' : 'Inactivo'}
          </span>
        ),
      },
      {
        header: 'Total asignados',
        render: (item: SecurityRow) => item.totalAssigned,
      },
      {
        header: 'Acción',
        align: 'center' as const,
        render: (item: SecurityRow) => (
          <GenericButton
            variant="outlineFill"
            className="security-action-btn"
            title={actionLabel}
            aria-label={actionLabel}
            onClick={() => onAction(item)}
          >
            <span className="security-action-icon">{actionIcon}</span>
          </GenericButton>
        ),
      },
    ],
    [actionIcon, actionLabel, onAction],
  );

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
        <div>
          {selectedRowIds.size > 0
            ? `${selectedRowIds.size} seleccionado(s) de ${items.length} registros encontrados`
            : `${items.length} registros encontrados`}
        </div>
      </div>
      <GenericTable
        rows={pageItems}
        columns={columns}
        emptyLabel="Sin resultados"
        perPage={pageSize}
        page={currentPage}
        totalPages={totalPages}
        totalItems={items.length}
        onChangePage={setCurrentPage}
        onChangePerPage={setPageSize}
        enableSelection
        selectedIds={Array.from(selectedRowIds)}
        onSelectRow={(id) => toggleRowSelection(Number(id))}
        selectionHeader={
          <input
            type="checkbox"
            checked={areAllRowsSelected}
            onChange={toggleSelectAll}
            aria-label="Seleccionar todos los registros"
          />
        }
      />
    </>
  );
}

