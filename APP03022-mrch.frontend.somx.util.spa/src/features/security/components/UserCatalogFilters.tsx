import { GenericDateRangePicker } from '@shared/components/ui/date';
import type { UserCatalogSearchFilters } from '../types';

interface Props {
  filters: UserCatalogSearchFilters;
  onChange: (next: UserCatalogSearchFilters) => void;
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

export function UserCatalogFilters({ filters, onChange }: Readonly<Props>) {
  return (
    <div className="security-filters">
      <input
        className="security-input"
        type="email"
        placeholder="Email (opcional)"
        value={filters.email}
        onChange={(e) => onChange({ ...filters, email: e.target.value })}
      />
      <input
        className="security-input"
        placeholder="Nombre (opcional)"
        value={filters.name}
        onChange={(e) => onChange({ ...filters, name: e.target.value })}
      />
      <GenericDateRangePicker
        value={[
          fromInputDate(filters.startDate),
          fromInputDate(filters.endDate),
        ]}
        onChange={([startDate, endDate]: [Date | null, Date | null]) =>
          onChange({
            ...filters,
            startDate: startDate ? toInputDate(startDate) : '',
            endDate: endDate ? toInputDate(endDate) : '',
          })
        }
        placeholder="Fecha creación desde - hasta"
        className="security-date-range"
        size="sm"
        inputProps={{ 'aria-label': 'Rango de fecha de creación' }}
      />
    </div>
  );
}
