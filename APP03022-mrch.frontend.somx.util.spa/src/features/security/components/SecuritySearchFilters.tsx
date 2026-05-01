import { useEffect, useMemo } from 'react';
import type { SecurityFilters } from '../types';

interface Props {
  filters: SecurityFilters;
  onChange: (next: SecurityFilters) => void;
}

const toInputDate = (date: Date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

export function SecuritySearchFilters({ filters, onChange }: Props) {
  const { defaultStartDate, defaultEndDate } = useMemo(() => {
    const today = new Date();
    const monthAgo = new Date(today);
    monthAgo.setMonth(monthAgo.getMonth() - 1);

    return {
      defaultStartDate: toInputDate(monthAgo),
      defaultEndDate: toInputDate(today),
    };
  }, []);

  useEffect(() => {
    if (filters.startDate && filters.endDate) {
      return;
    }

    onChange({
      ...filters,
      startDate: filters.startDate || defaultStartDate,
      endDate: filters.endDate || defaultEndDate,
    });
  }, [defaultEndDate, defaultStartDate, filters, onChange]);

  return (
    <div className="security-filters">
      <input
        className="security-input"
        type="date"
        value={filters.startDate || defaultStartDate}
        onChange={(event) => onChange({ ...filters, startDate: event.target.value })}
      />
      <input
        className="security-input"
        type="date"
        value={filters.endDate || defaultEndDate}
        onChange={(event) => onChange({ ...filters, endDate: event.target.value })}
      />
      <input
        className="security-input"
        placeholder="Id"
        value={filters.entityId}
        onChange={(event) => onChange({ ...filters, entityId: event.target.value })}
      />
      <input
        className="security-input"
        placeholder="Nombre"
        value={filters.entityName}
        onChange={(event) => onChange({ ...filters, entityName: event.target.value })}
      />
      <select
        className="security-select"
        value={filters.status}
        onChange={(event) => onChange({ ...filters, status: event.target.value as SecurityFilters['status'] })}
      >
        <option value="">Estatus</option>
        <option value="1">Activo</option>
        <option value="0">Inactivo</option>
      </select>
    </div>
  );
}

