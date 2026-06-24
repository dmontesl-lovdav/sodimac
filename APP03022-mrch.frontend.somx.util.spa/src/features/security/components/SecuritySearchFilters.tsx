import { useEffect, useMemo, useRef } from 'react';
import { GenericDateRangePicker } from '@shared/components/ui/date';
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

const fromInputDate = (date: string) => {
  if (!date) {
    return null;
  }

  const [year, month, day] = date.split('-').map(Number);
  return new Date(year, month - 1, day);
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

  /** Solo la primera vez con ambas fechas vacías (montaje): sugerir rango. No repetir si el usuario borra con la X. */
  const dateDefaultsSeededRef = useRef(false);

  /** Solo al montar: si aún no hay fechas, sugerir rango. Evita depender de `filters` en el array (cambios en otros campos). */
  const filtersRefForSeed = useRef(filters);
  filtersRefForSeed.current = filters;

  useEffect(() => {
    if (dateDefaultsSeededRef.current) return;
    const f = filtersRefForSeed.current;
    if (f.startDate || f.endDate) {
      dateDefaultsSeededRef.current = true;
      return;
    }
    dateDefaultsSeededRef.current = true;
    onChange({
      ...f,
      startDate: defaultStartDate,
      endDate: defaultEndDate,
    });
  }, [defaultEndDate, defaultStartDate, onChange]);

  return (
    <div className="security-filters">
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
      <GenericDateRangePicker
        value={[
          filters.startDate ? fromInputDate(filters.startDate) : null,
          filters.endDate ? fromInputDate(filters.endDate) : null,
        ]}
        onChange={([startDate, endDate]: [Date | null, Date | null]) =>
          onChange({
            ...filters,
            startDate: startDate ? toInputDate(startDate) : '',
            endDate: endDate ? toInputDate(endDate) : '',
          })
        }
        placeholder="Fecha desde - hasta"
        className="security-date-range"
        size="sm"
      />
    </div>
  );
}

