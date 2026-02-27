import { useMemo, useState } from 'react';
import { Breadcrumb } from './components/Breadcrumb';
import { ToolbarFilters } from './components/ToolbarFilters';
import { EmptyState } from './components/EmptyState';
import { ParameterGrid } from './components/ParameterGrid';
import { mockParameters } from './mockData';
import type { Parameter, ParameterFilters } from './types';
import documentIconUrl from '@/shared/icons/document.svg';

const INITIAL_FILTERS: ParameterFilters = {
  parameterId: '',
  parameterName: '',
  module: '',
  parameterType: '',
  status: '',
  includeHistory: false,
};

export const ParameterConfigPage = () => {
  const [filters, setFilters] = useState<ParameterFilters>(INITIAL_FILTERS);
  const [hasSearched, setHasSearched] = useState(false);

  const filteredItems: Parameter[] = useMemo(() => {
    if (!hasSearched) {
      return [];
    }

    return mockParameters.filter((parameter) => {
      const matchesId =
        !filters.parameterId ||
        parameter.id.toLowerCase().includes(filters.parameterId.toLowerCase());
      const matchesName =
        !filters.parameterName ||
        parameter.name
          .toLowerCase()
          .includes(filters.parameterName.toLowerCase());
      const matchesModule =
        !filters.module || parameter.module === filters.module;
      const matchesType =
        !filters.parameterType ||
        parameter.parameterType === filters.parameterType;
      const matchesStatus =
        !filters.status ||
        (filters.status === 'ACTIVE' && parameter.status === 'ACTIVE') ||
        (filters.status === 'INACTIVE' && parameter.status === 'INACTIVE');

      return (
        matchesId &&
        matchesName &&
        matchesModule &&
        matchesType &&
        matchesStatus
      );
    });
  }, [filters, hasSearched]);

  const handleSearch = () => {
    setHasSearched(true);
  };

  const handleClear = () => {
    setFilters(INITIAL_FILTERS);
    setHasSearched(false);
  };

  const handleCreateParameter = () => {
    // In a real implementation this would navigate to a "new parameter" flow.
    // For now we log so the shell can wire navigation as needed.
    // eslint-disable-next-line no-console
    console.log('Nuevo parámetro');
  };

  const handleBack = () => {
    // This keeps the microfrontend self-contained while still providing a sensible default.
    if (window.history.length > 1) {
      window.history.back();
      return;
    }

    // Fallback: let the shell override this URL if needed.
    window.location.href = '/herramientas-utilerias';
  };

  return (
    <div className="parameters-page">
      <Breadcrumb
        items={['Inicio', 'Herramientas y Utilerías', 'Configuración de Parámetros']}
      />

      <div className="parameters-page__panel">
        <section className="parameters-page__header">
          <div className="parameters-page__title-group">
            <div className="parameters-page__icon" aria-hidden="true">
              <img src={documentIconUrl} alt="" />
            </div>
            <h1 className="parameters-page__title">Configuración de Parámetros</h1>
          </div>
        </section>

        <section className="parameters-page__subheader">
          <p className="parameters-page__description">
            Busca, gestiona y consulta parámetros junto con sus valores. Además,
            obten información detallada sobre los cambios realizados en cada uno
            de ellos.
          </p>
          <div className="parameters-page__header-actions">
            <button
              type="button"
              className="btn btn--primary"
              onClick={handleCreateParameter}
            >
              Nuevo Parámetro
            </button>
          </div>
        </section>

        <ToolbarFilters
          filters={filters}
          onFiltersChange={setFilters}
          onSearch={handleSearch}
          onClear={handleClear}
        />

        {hasSearched ? <ParameterGrid items={filteredItems} /> : <EmptyState />}

        <div className="parameters-page__footer">
          <button
            type="button"
            onClick={handleBack}
            className="btn btn--ghost"
          >
            Volver
          </button>
        </div>
      </div>
    </div>
  );
};
