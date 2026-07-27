import { type FC } from 'react';
import GenericTable from '@shared/components/ui/table/GenericTable';
import type { Parameter } from '../types';
import type { CatalogItem } from '../services/parameterService';
import editIcon from '@/shared/icons/edit.svg';

import '../styles/ParameterGridTable.css';

interface ParameterGridProps {
  items: Parameter[];
  totalItems?: number;
  catalogs: {
    modules: CatalogItem[];
    parameterTypes: CatalogItem[];
    statuses: CatalogItem[];
  };
  selectedIds: Set<string>;
  onSelectionChange: (ids: Set<string>) => void;
  onEdit?: (parameter: Parameter) => void;
  onStatusChange?: (parameter: Parameter) => void;
  latestVersionIds?: Set<string>;
  pageSize: number;
  currentPage: number;
  totalPages: number;
  onPageSizeChange: (size: number) => void;
  onPageChange: (page: number) => void;
}

const formatDate = (value: string | null | undefined): string => {
  if (!value) return '-';
  const parts = value.split('T')[0]?.split('-');
  if (!parts || parts.length !== 3) return value;
  return `${parts[2]}-${parts[1]}-${parts[0]}`;
};

const formatVersion = (version: string | number): string => {
  const versionNum = typeof version === 'number' ? version : parseFloat(version);
  return versionNum.toFixed(1);
};

export const ParameterGrid: FC<ParameterGridProps> = ({
  items,
  totalItems,
  catalogs,
  selectedIds,
  onSelectionChange,
  onEdit,
  onStatusChange,
  latestVersionIds = new Set(),
  pageSize,
  currentPage,
  totalPages,
  onPageSizeChange,
  onPageChange,
}) => {
  const currentPageIds = items.map(item => item.id);

  const handleSelectAll = (checked: boolean) => {
    const newSelected = new Set(selectedIds);
    if (checked) {
      currentPageIds.forEach(id => newSelected.add(id));
    } else {
      currentPageIds.forEach(id => newSelected.delete(id));
    }
    onSelectionChange(newSelected);
  };

  const handleSelectItem = (id: string, checked: boolean) => {
    const newSelected = new Set(selectedIds);
    if (checked) newSelected.add(id);
    else newSelected.delete(id);
    onSelectionChange(newSelected);
  };

  const isAllSelected = items.length > 0 && currentPageIds.every(id => selectedIds.has(id));

  const getLabel = (value: string, catalog: CatalogItem[]) =>
    catalog.find((item) => item.value === value)?.label ?? value;

  const hasExpired = (p: Parameter): boolean => {
    if (!p.endDate) return false;
    const today = new Date();
    const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
    const endDateStr = p.endDate.split('T')[0];
    return (endDateStr ?? '') < todayStr;
  };

  const getEffectiveStatus = (p: Parameter): number => hasExpired(p) ? 0 : p.status;

  const isEditable = (p: Parameter): boolean => latestVersionIds.has(p.id) && !hasExpired(p);

  const canChangeStatus = (p: Parameter): boolean => latestVersionIds.has(p.id) && !hasExpired(p);

  const columns = [
    {
      header: (
        <input type="checkbox" checked={isAllSelected} onChange={(e: any) => handleSelectAll(e.target.checked)} />
      ),
      render: (row: Parameter) => (
        <input type="checkbox" checked={selectedIds.has(row.id)} onChange={(e: any) => handleSelectItem(row.id, e.target.checked)} />
      ),
      align: 'center' as const,
    },
    { header: 'ID Parámetro', render: (row: Parameter) => row.id },
    { header: 'Nombre Parámetro', render: (row: Parameter) => row.name },
    { header: 'Descripción', render: (row: Parameter) => row.description },
    { header: 'Módulo', render: (row: Parameter) => row.module },
    { header: 'Tipo Parámetro', render: (row: Parameter) => getLabel(row.parameterType, catalogs.parameterTypes) },
    { header: 'Valor', render: (row: Parameter) => row.value },
    { header: 'Versión', align: 'center' as const, render: (row: Parameter) => formatVersion(row.version) },
    { header: 'Fecha Inicio', render: (row: Parameter) => formatDate(row.startDate) },
    { header: 'Fecha Fin', render: (row: Parameter) => formatDate(row.endDate) },
    {
      header: 'Estatus',
      align: 'center' as const,
      render: (row: Parameter) => {
        const eff = getEffectiveStatus(row);
        return (
          <span className={`status-badge status-badge--${eff === 1 ? 'success' : 'danger'}`}>
            {getLabel(String(eff), catalogs.statuses)}
          </span>
        );
      },
    },
    { header: 'Usuario Registro', render: (row: Parameter) => row.createdBy },
    { header: 'Fecha Registro', render: (row: Parameter) => formatDate(row.createdAt) },
    { header: 'Usuario Mod.', render: (row: Parameter) => row.updatedBy ?? '-' },
    { header: 'Fecha Mod.', render: (row: Parameter) => row.updatedAt ? formatDate(row.updatedAt) : '-' },
    {
      header: 'Activar / Desactivar',
      align: 'center' as const,
      render: (row: Parameter) => {
        const enabled = canChangeStatus(row);
        const isActive = getEffectiveStatus(row) === 1;
        const getTitle = () => {
          if (!enabled) return 'No se puede modificar el estatus de esta versión';
          return isActive ? 'Desactivar parámetro' : 'Activar parámetro';
        };
        return (
          <label
            className={`switch ${!enabled ? 'switch--disabled' : ''}`}
            style={{
              opacity: enabled ? 1 : 0.5,
              cursor: enabled ? 'pointer' : 'not-allowed',
            }}
            title={getTitle()}
            aria-label={getTitle()}
          >
            <input
              type="checkbox"
              checked={isActive}
              disabled={!enabled}
              onChange={() => {
                if (enabled) onStatusChange?.(row);
              }}
            />
            <span className="switch__slider" />
          </label>
        );
      },
    },
  ];

  const actions = [
    {
      title: 'Editar parámetro',
      icon: editIcon,
      onClick: (row: Parameter) => onEdit?.(row),
      isDisabled: (row: Parameter) => !isEditable(row),
    },
  ];

  return (
    <section>
      <div className="param-grid-summary">
        <span className="param-grid-count">{totalItems ?? items.length} Parámetros encontrados</span>
        <div className="param-grid-selection">
          {selectedIds.size > 0 && (
            <span className="param-selected-count">
              ({selectedIds.size} {selectedIds.size === 1 ? 'seleccionado' : 'seleccionados'})
            </span>
          )}
        </div>
      </div>

      <GenericTable
        rows={items}
        columns={columns}
        actions={actions}
        emptyLabel="No se encontraron parámetros registrados."
        perPage={pageSize}
        page={currentPage}
        totalPages={totalPages}
        totalItems={totalItems}
        onChangePerPage={onPageSizeChange}
        onChangePage={onPageChange}
      />
    </section>
  );
};
