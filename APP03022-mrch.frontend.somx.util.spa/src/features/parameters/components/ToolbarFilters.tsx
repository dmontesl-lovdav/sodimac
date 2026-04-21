import { ChangeEvent, FC, FormEvent } from 'react';
import GenericButton from '@shared/components/ui/button/GenericButton';
import type { ParameterFilters } from '../types';
import { CatalogItem } from '../services/parameterService';

import '../styles/ParameterGridToolbar.css';

interface ToolbarFiltersProps {
  filters: ParameterFilters;
  onFiltersChange: (filters: ParameterFilters) => void;
  onSearch: () => void;
  onClear: () => void;
  catalogs: {
    modules: CatalogItem[];
    parameterTypes: CatalogItem[];
    statuses: CatalogItem[];
  };
  isLoading: boolean;
  error: string | null;
}

export const ToolbarFilters: FC<ToolbarFiltersProps> = ({
  filters,
  onFiltersChange,
  onSearch,
  onClear,
  catalogs,
  isLoading,
  error,
}) => {
  const handleInputChange = (
    event: ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) => {
    const target = event.target as HTMLInputElement | HTMLSelectElement;
    const { name, value } = target;
    const isCheckbox =
      target instanceof HTMLInputElement && target.type === 'checkbox';

    const newFilters = {
      ...filters,
      [name]: isCheckbox ? target.checked : value,
    };

    if (name === 'status' && value === '1') {
      newFilters.includeHistory = false;
    }

    onFiltersChange(newFilters);
  };

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    onSearch();
  };

  const isHistoryDisabled = filters.status === '1';

  return (
    <form className="param-toolbar" onSubmit={handleSubmit}>
      <div className="param-toolbar__field">
        <label className="param-toolbar__label" htmlFor="parameterId">ID Parámetro</label>
        <input
          id="parameterId"
          name="parameterId"
          type="text"
          className="param-toolbar__input"
          placeholder="ID Parámetro"
          value={filters.parameterId}
          onChange={handleInputChange}
        />
      </div>

      <div className="param-toolbar__field" style={{ minWidth: '180px' }}>
        <label className="param-toolbar__label" htmlFor="parameterName">Nombre Parámetro</label>
        <input
          id="parameterName"
          name="parameterName"
          type="text"
          className="param-toolbar__input"
          placeholder="Nombre Parámetro"
          value={filters.parameterName}
          onChange={handleInputChange}
        />
      </div>

      <div className="param-toolbar__field">
        <label className="param-toolbar__label" htmlFor="module">Módulo</label>
        <select
          id="module"
          name="module"
          className="param-toolbar__select"
          value={filters.module}
          onChange={handleInputChange}
          disabled={isLoading}
        >
          <option value="">Módulo</option>
          {catalogs.modules.map((option) => (
            <option key={option.value} value={option.value}>{option.label}</option>
          ))}
        </select>
      </div>

      <div className="param-toolbar__field">
        <label className="param-toolbar__label" htmlFor="parameterType">Tipo Parámetro</label>
        <select
          id="parameterType"
          name="parameterType"
          className="param-toolbar__select"
          value={filters.parameterType}
          onChange={handleInputChange}
          disabled={isLoading}
        >
          <option value="">Tipo Parámetro</option>
          {catalogs.parameterTypes.map((option) => (
            <option key={option.value} value={option.value}>{option.label}</option>
          ))}
        </select>
      </div>

      <div className="param-toolbar__field">
        <label className="param-toolbar__label" htmlFor="status">Estatus</label>
        <select
          id="status"
          name="status"
          className="param-toolbar__select"
          value={filters.status}
          onChange={handleInputChange}
          disabled={isLoading}
        >
          <option value="">Estatus</option>
          {catalogs.statuses.map((option) => (
            <option key={option.value} value={option.value}>{option.label}</option>
          ))}
        </select>
        {error && <span className="param-toolbar__error">{error}</span>}
      </div>

      <div className="param-toolbar__field" style={{ paddingTop: '24px' }}>
        <label
          className="param-toolbar__checkbox-label"
          style={{
            opacity: isHistoryDisabled ? 0.5 : 1,
            cursor: isHistoryDisabled ? 'not-allowed' : 'pointer',
          }}
        >
          <input
            id="includeHistory"
            name="includeHistory"
            type="checkbox"
            checked={filters.includeHistory}
            onChange={handleInputChange}
            disabled={isHistoryDisabled}
          />
          <span>Incluir Historial</span>
        </label>
      </div>

      <div className="param-toolbar__actions">
        <GenericButton variant="outline" onClick={onClear} disabled={isLoading}>
          Limpiar
        </GenericButton>
        <GenericButton variant="outline" type="submit" disabled={isLoading}>
          {isLoading ? 'Buscando...' : 'Buscar'}
        </GenericButton>
      </div>
    </form>
  );
};
