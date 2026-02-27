import type { ChangeEvent, FC, FormEvent } from 'react';
import type { ParameterFilters } from '../types';
import {
  moduleCatalog,
  parameterTypeCatalog,
  statusCatalog,
} from '../mockData';

interface ToolbarFiltersProps {
  filters: ParameterFilters;
  onFiltersChange: (filters: ParameterFilters) => void;
  onSearch: () => void;
  onClear: () => void;
}

export const ToolbarFilters: FC<ToolbarFiltersProps> = ({
  filters,
  onFiltersChange,
  onSearch,
  onClear,
}) => {
  const handleInputChange = (
    event: ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) => {
    const target = event.target as HTMLInputElement | HTMLSelectElement;
    const { name, value } = target;
    const isCheckbox =
      target instanceof HTMLInputElement && target.type === 'checkbox';

    onFiltersChange({
      ...filters,
      [name]: isCheckbox ? target.checked : value,
    });
  };

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    onSearch();
  };

  return (
    <form
      className="parameters-toolbar"
      onSubmit={handleSubmit}
    >
      <div className="parameters-toolbar__left">
        <div className="parameters-toolbar__field">
          <label className="parameters-toolbar__label" htmlFor="parameterId">
            ID Parámetro
          </label>
          <input
            id="parameterId"
            name="parameterId"
            type="text"
            className="parameters-toolbar__input"
            value={filters.parameterId}
            onChange={handleInputChange}
          />
        </div>

        <div className="parameters-toolbar__field">
          <label className="parameters-toolbar__label" htmlFor="parameterName">
            Nombre Parámetro
          </label>
          <input
            id="parameterName"
            name="parameterName"
            type="text"
            className="parameters-toolbar__input"
            value={filters.parameterName}
            onChange={handleInputChange}
          />
        </div>

        <div className="parameters-toolbar__field">
          <label className="parameters-toolbar__label" htmlFor="module">
            Módulo
          </label>
          <select
            id="module"
            name="module"
            className="parameters-toolbar__select"
            value={filters.module}
            onChange={handleInputChange}
          >
            <option value="">Todos</option>
            {moduleCatalog.map((option) => (
              <option
                key={option.value}
                value={option.value}
              >
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <div className="parameters-toolbar__field">
          <label
            className="parameters-toolbar__label"
            htmlFor="parameterType"
          >
            Tipo Parámetro
          </label>
          <select
            id="parameterType"
            name="parameterType"
            className="parameters-toolbar__select"
            value={filters.parameterType}
            onChange={handleInputChange}
          >
            <option value="">Todos</option>
            {parameterTypeCatalog.map((option) => (
              <option
                key={option.value}
                value={option.value}
              >
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <div className="parameters-toolbar__field">
          <label className="parameters-toolbar__label" htmlFor="status">
            Estatus
          </label>
          <select
            id="status"
            name="status"
            className="parameters-toolbar__select"
            value={filters.status}
            onChange={handleInputChange}
          >
            <option value="">Todos</option>
            {statusCatalog.map((option) => (
              <option
                key={option.value}
                value={option.value}
              >
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <div className="parameters-toolbar__field parameters-toolbar__field--checkbox">
          <label className="parameters-toolbar__checkbox-label">
            <input
              id="includeHistory"
              name="includeHistory"
              type="checkbox"
              checked={filters.includeHistory}
              onChange={handleInputChange}
            />
            <span>Incluir Histórico</span>
          </label>
        </div>
      </div>

      <div className="parameters-toolbar__right">
        <button
          type="button"
          className="btn btn--ghost"
          onClick={onClear}
        >
          Limpiar
        </button>

        <button
          type="submit"
          className="btn btn--secondary"
        >
          Buscar
        </button>
      </div>
    </form>
  );
};
