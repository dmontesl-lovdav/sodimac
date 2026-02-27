import { useEffect, useMemo, useState, type FC } from 'react';
import type { Parameter } from '../types';
import editIcon from '@/shared/icons/edit.svg';

interface ParameterGridProps {
  items: Parameter[];
}

const COLUMN_LABELS = {
  select: 'Seleccionar',
  id: 'ID Parámetro',
  name: 'Nombre Parámetro',
  description: 'Descripción',
  module: 'Módulo',
  parameterType: 'Tipo Parámetro',
  value: 'Valor',
  version: 'Versión',
  startDate: 'Fecha Inicio Vigencia',
  endDate: 'Fecha Fin Vigencia',
  status: 'Estatus',
  createdBy: 'Usuario Registro',
  createdAt: 'Fecha Registro',
  updatedBy: 'Usuario Modificación',
  updatedAt: 'Fecha Modificación',
  edit: 'Editar',
  toggle: 'Activar / Desactivar',
} as const;

const formatDate = (value: string) => {
  const [year, month, day] = value.split('-');
  return `${day}-${month}-${year}`;
};

export const ParameterGrid: FC<ParameterGridProps> = ({ items }) => {
  const [pageSize, setPageSize] = useState(10);
  const [pageSizeInput, setPageSizeInput] = useState('10');
  const [currentPage, setCurrentPage] = useState(1);
  const [goToPage, setGoToPage] = useState('1');

  const totalItems = items.length;
  const totalPages = Math.max(1, Math.ceil(totalItems / pageSize));

  useEffect(() => {
    setCurrentPage(1);
  }, [pageSize, totalItems]);

  useEffect(() => {
    setPageSizeInput(String(pageSize));
  }, [pageSize]);

  const clampedCurrentPage = Math.min(currentPage, totalPages);

  useEffect(() => {
    setGoToPage(String(clampedCurrentPage));
  }, [clampedCurrentPage]);

  const pageItems = useMemo(() => {
    const startIndex = (clampedCurrentPage - 1) * pageSize;
    return items.slice(startIndex, startIndex + pageSize);
  }, [clampedCurrentPage, items, pageSize]);

  const rangeStart =
    totalItems === 0 ? 0 : (clampedCurrentPage - 1) * pageSize + 1;
  const rangeEnd = totalItems === 0 ? 0 : Math.min(clampedCurrentPage * pageSize, totalItems);

  const visiblePages = useMemo(() => {
    const maxVisible = 5;
    const pages: number[] = [];
    const start = Math.max(
      1,
      Math.min(clampedCurrentPage, Math.max(1, totalPages - maxVisible + 1)),
    );
    const end = Math.min(totalPages, start + maxVisible - 1);

    for (let page = start; page <= end; page += 1) {
      pages.push(page);
    }

    return pages;
  }, [clampedCurrentPage, totalPages]);

  const handleGoToCommit = () => {
    const parsed = Number(goToPage);
    if (!Number.isFinite(parsed)) {
      setGoToPage(String(clampedCurrentPage));
      return;
    }

    const nextPage = Math.min(totalPages, Math.max(1, Math.trunc(parsed)));
    setCurrentPage(nextPage);
  };

  const handlePageSizeCommit = () => {
    const parsed = Number(pageSizeInput);
    if (!Number.isFinite(parsed)) {
      setPageSizeInput(String(pageSize));
      return;
    }

    const nextSize = Math.min(999, Math.max(1, Math.trunc(parsed)));
    setPageSize(nextSize);
  };

  if (items.length === 0) {
    return null;
  }

  return (
    <section className="parameters-grid">
      <div className="parameters-grid__summary">
        {items.length} Catálogos encontrados
      </div>

      <div className="parameters-grid__select-all">Seleccionar todo</div>

      <div className="parameters-grid__table-wrapper">
        <div
          className="parameters-grid__header"
          role="rowgroup"
        >
          <div className="parameters-grid__cell parameters-grid__cell--checkbox">
            <input type="checkbox" aria-label="Seleccionar todo" />
          </div>
          <div className="parameters-grid__cell">ID Parámetro</div>
          <div className="parameters-grid__cell">Nombre Parámetro</div>
          <div className="parameters-grid__cell">Descripción</div>
          <div className="parameters-grid__cell">Módulo</div>
          <div className="parameters-grid__cell">Tipo Parámetro</div>
          <div className="parameters-grid__cell">Valor</div>
          <div className="parameters-grid__cell">Versión</div>
          <div className="parameters-grid__cell">Fecha Inicio Vigencia</div>
          <div className="parameters-grid__cell">Fecha Fin Vigencia</div>
          <div className="parameters-grid__cell">Estatus</div>
          <div className="parameters-grid__cell">Usuario Registro</div>
          <div className="parameters-grid__cell">Fecha Registro</div>
          <div className="parameters-grid__cell">Usuario Modificación</div>
          <div className="parameters-grid__cell">Fecha Modificación</div>
          <div className="parameters-grid__cell">Editar</div>
          <div className="parameters-grid__cell">Activar / Desactivar</div>
        </div>

        {pageItems.map((parameter) => (
          <div
            key={parameter.id}
            className="parameters-grid__row"
            role="row"
          >
            <div
              className="parameters-grid__cell parameters-grid__cell--checkbox"
              data-label={COLUMN_LABELS.select}
            >
              <input
                type="checkbox"
                aria-label={`Seleccionar ${parameter.name}`}
              />
            </div>
            <div className="parameters-grid__cell" data-label={COLUMN_LABELS.id}>
              {parameter.id}
            </div>
            <div className="parameters-grid__cell" data-label={COLUMN_LABELS.name}>
              {parameter.name}
            </div>
            <div
              className="parameters-grid__cell"
              data-label={COLUMN_LABELS.description}
            >
              {parameter.description}
            </div>
            <div className="parameters-grid__cell" data-label={COLUMN_LABELS.module}>
              {parameter.module}
            </div>
            <div
              className="parameters-grid__cell"
              data-label={COLUMN_LABELS.parameterType}
            >
              {parameter.parameterType}
            </div>
            <div className="parameters-grid__cell" data-label={COLUMN_LABELS.value}>
              {parameter.value}
            </div>
            <div className="parameters-grid__cell" data-label={COLUMN_LABELS.version}>
              {parameter.version.toFixed(1)}
            </div>
            <div
              className="parameters-grid__cell"
              data-label={COLUMN_LABELS.startDate}
            >
              {formatDate(parameter.startDate)}
            </div>
            <div className="parameters-grid__cell" data-label={COLUMN_LABELS.endDate}>
              {formatDate(parameter.endDate)}
            </div>
            <div className="parameters-grid__cell" data-label={COLUMN_LABELS.status}>
              <span
                className={`status-badge status-badge--${
                  parameter.status === 'ACTIVE' ? 'success' : 'danger'
                }`}
              >
                {parameter.status === 'ACTIVE' ? 'Activo' : 'Inactivo'}
              </span>
            </div>
            <div
              className="parameters-grid__cell"
              data-label={COLUMN_LABELS.createdBy}
            >
              {parameter.createdBy}
            </div>
            <div
              className="parameters-grid__cell"
              data-label={COLUMN_LABELS.createdAt}
            >
              {formatDate(parameter.createdAt)}
            </div>
            <div
              className="parameters-grid__cell"
              data-label={COLUMN_LABELS.updatedBy}
            >
              {parameter.updatedBy ?? '-'}
            </div>
            <div
              className="parameters-grid__cell"
              data-label={COLUMN_LABELS.updatedAt}
            >
              {parameter.updatedAt ? formatDate(parameter.updatedAt) : '-'}
            </div>
            <div
              className="parameters-grid__cell parameters-grid__cell--edit"
              data-label={COLUMN_LABELS.edit}
            >
              <button
                type="button"
                className="btn-icon"
                aria-label={`Editar parámetro ${parameter.id}`}
              >
                <img src={editIcon} alt="edit icon" className='edit-icon' />
              </button>
            </div>
            <div
              className="parameters-grid__cell parameters-grid__cell--toggle"
              data-label={COLUMN_LABELS.toggle}
            >
              <label className="switch">
                <input
                  type="checkbox"
                  checked={parameter.status === 'ACTIVE'}
                  readOnly
                />
                <span className="switch__slider" />
              </label>
            </div>
          </div>
        ))}
      </div>

      <div
        className="parameters-grid__pagination"
        role="navigation"
        aria-label="Paginación"
      >
        <div className="pagination__left">
          <label className="pagination__label" htmlFor="pageSize">
            Items por página:
          </label>
          <input
            id="pageSize"
            className="pagination__input pagination__input--pagesize"
            type="number"
            inputMode="numeric"
            min={1}
            max={999}
            value={pageSizeInput}
            onChange={(event) => setPageSizeInput(event.target.value)}
            onBlur={handlePageSizeCommit}
            onKeyDown={(event) => {
              if (event.key === 'Enter') {
                handlePageSizeCommit();
              }
            }}
          />

          <label className="pagination__label" htmlFor="goToPage">
            Ir a:
          </label>
          <input
            id="goToPage"
            className="pagination__input"
            type="number"
            inputMode="numeric"
            min={1}
            max={totalPages}
            value={goToPage}
            onChange={(event) => setGoToPage(event.target.value)}
            onBlur={handleGoToCommit}
            onKeyDown={(event) => {
              if (event.key === 'Enter') {
                handleGoToCommit();
              }
            }}
          />

          <div className="pagination__range" aria-live="polite">
            {rangeStart}-{rangeEnd} de {totalItems}
          </div>
        </div>

        <div className="pagination__right" aria-label="Navegación de páginas">
          <button
            type="button"
            className="pagination__nav"
            onClick={() => setCurrentPage((page) => Math.max(1, page - 1))}
            disabled={clampedCurrentPage <= 1}
            aria-label="Página anterior"
          >
            ‹
          </button>

          <div className="pagination__pages">
            {visiblePages.map((page) => (
              <button
                key={page}
                type="button"
                className={`pagination__page${
                  page === clampedCurrentPage ? ' pagination__page--active' : ''
                }`}
                onClick={() => setCurrentPage(page)}
                aria-current={page === clampedCurrentPage ? 'page' : undefined}
              >
                {page}
              </button>
            ))}
          </div>

          <button
            type="button"
            className="pagination__nav"
            onClick={() => setCurrentPage((page) => Math.min(totalPages, page + 1))}
            disabled={clampedCurrentPage >= totalPages}
            aria-label="Página siguiente"
          >
            ›
          </button>
        </div>
      </div>
    </section>
  );
};
