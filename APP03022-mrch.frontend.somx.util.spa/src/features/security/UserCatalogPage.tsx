import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import GenericButton from '@shared/components/ui/button/GenericButton';
import GenericModal from '@shared/components/ui/modal/GenericModal';
import { getErrorMessage, useAlertModal } from '@shared/hooks/useAlertModal';
import { GenericTable } from '@shared/components/ui/table';
import { SecurityBreadcrumb } from './components/SecurityBreadcrumb';
import { UserCatalogFilters } from './components/UserCatalogFilters';
import { useUserCatalogSearch } from './hooks/useUserCatalog';
import { securityService } from './services/securityService';
import type { UserCatalogRow, UserCatalogSearchFilters } from './types';
import './styles/SecurityCommon.css';

function toDateInputValue(d: Date): string {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

/** Rango inicial: desde hace un mes hasta hoy (fecha de creaci?n). */
function getDefaultCreationDateRange(): Pick<UserCatalogSearchFilters, 'startDate' | 'endDate'> {
  const end = new Date();
  const start = new Date(end);
  start.setMonth(start.getMonth() - 1);
  return { startDate: toDateInputValue(start), endDate: toDateInputValue(end) };
}

const initialFilters: UserCatalogSearchFilters = {
  ...getDefaultCreationDateRange(),
  email: '',
  name: '',
  status: '1',
};

function traceId() {
  return typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : String(Date.now());
}

const viewIcon = (
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

function buildUserCatalogCsv(rows: UserCatalogRow[]): string {
  const headers = ['idUsuario', 'usuario', 'nombre', 'fechaCreacion', 'fechaModificacion', 'estatus'];
  const csvRows = rows.map((row) => [
    row.id,
    row.username,
    row.fullName,
    row.createdAt?.split('T')[0] ?? '',
    row.modifiedAt?.split('T')[0] ?? '',
    row.status === 1 ? 'Activo' : 'Inactivo',
  ]);

  return [
    headers.join(','),
    ...csvRows.map((row) =>
      row
        .map((value) => `"${String(value ?? '').replace(/"/g, '""')}"`)
        .join(','),
    ),
  ].join('\n');
}

function downloadCsv(content: string, filename: string) {
  const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.click();
  window.URL.revokeObjectURL(url);
}

function buildCsvFilename(tableName: string) {
  const now = new Date();
  const dd = String(now.getDate()).padStart(2, '0');
  const mm = String(now.getMonth() + 1).padStart(2, '0');
  const yyyy = String(now.getFullYear());
  const hh = String(now.getHours()).padStart(2, '0');
  const min = String(now.getMinutes()).padStart(2, '0');
  const safeTableName = tableName
    .trim()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/[\\/:*?"<>|]/g, '')
    .replace(/\s+/g, '-');

  return `${safeTableName}-${dd}-${mm}-${yyyy}-${hh}-${min}.csv`;
}

export function UserCatalogPage() {
  const navigate = useNavigate();
  const location = useLocation();

  const [draftFilters, setDraftFilters] = useState<UserCatalogSearchFilters>(() => {
    const nav = location.state as { catalogFilters?: UserCatalogSearchFilters } | null | undefined;
    return {
      ...initialFilters,
      ...(nav?.catalogFilters ?? {}),
    };
  });
  const [appliedFilters, setAppliedFilters] = useState<UserCatalogSearchFilters | null>(() => {
    const nav = location.state as { catalogFilters?: UserCatalogSearchFilters } | null | undefined;
    return nav?.catalogFilters
      ? { ...initialFilters, ...nav.catalogFilters }
      : null;
  });
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(10);
  const [sortBy] = useState('id');
  const [sortDir] = useState<'ASC' | 'DESC'>('ASC');
  const [selectedRowIds, setSelectedRowIds] = useState<Set<number>>(new Set());
  const { showAlert, alertModal } = useAlertModal();

  const appliedInvalid = !appliedFilters?.startDate || !appliedFilters?.endDate;
  const search = useUserCatalogSearch(
    appliedFilters ?? draftFilters,
    page,
    limit,
    sortBy,
    sortDir,
    appliedFilters !== null && !appliedInvalid,
  );

  useEffect(() => {
    if (search.error) console.error('[Cat?logo Usuarios]', search.error);
  }, [search.error]);

  const totalPages = Math.max(1, Math.ceil((search.data?.total ?? 0) / limit));
  const rows = useMemo(() => search.data?.items ?? [], [search.data?.items]);
  const hasSearched = appliedFilters !== null;
  const areAllRowsSelected = rows.length > 0 && rows.every((row) => selectedRowIds.has(row.id));

  useEffect(() => {
    if (!search.isError) return;
    showAlert({
      title: 'Error',
      message: getErrorMessage(search.error, 'No fue posible consultar el catálogo.'),
      severity: 'error',
    });
  }, [search.isError, search.error, showAlert]);

  useEffect(() => {
    const msg = search.data?.warningMessage;
    if (!msg || !search.isSuccess) return;
    showAlert({ title: 'Aviso', message: msg, severity: 'warning' });
  }, [search.data?.warningMessage, search.isSuccess, showAlert]);

  useEffect(() => {
    if (!search.isSuccess || !hasSearched || appliedInvalid) return;
    if (rows.length > 0) return;
    showAlert({
      title: 'Sin resultados',
      message: 'No hay registros para los filtros indicados.',
      severity: 'info',
    });
  }, [search.isSuccess, hasSearched, appliedInvalid, rows.length, showAlert]);
  const columns = useMemo(
    () => [
      {
        header: 'Id Usuario',
        render: (row: UserCatalogRow) => row.id,
      },
      {
        header: 'Usuario',
        render: (row: UserCatalogRow) => row.username,
      },
      {
        header: 'Nombre',
        render: (row: UserCatalogRow) => row.fullName,
      },
      {
        header: 'Fecha creación',
        render: (row: UserCatalogRow) => row.createdAt?.split('T')[0],
      },
      {
        header: 'Fecha modificación',
        render: (row: UserCatalogRow) => row.modifiedAt?.split('T')[0],
      },
      {
        header: 'Estatus',
        render: (row: UserCatalogRow) => (
          <span className={`security-status ${row.status === 1 ? 'active' : 'inactive'}`}>
            {row.status === 1 ? 'Activo' : 'Inactivo'}
          </span>
        ),
      },
      {
        header: 'Acción',
        align: 'center' as const,
        render: (row: UserCatalogRow) => (
          <GenericButton
            variant="outlineFill"
            className="security-action-btn"
            title="Ver detalle"
            aria-label="Ver detalle"
            onClick={() =>
              navigate(`/seguridad/gestion-usuarios/${row.id}`, {
                state: { catalogFilters: appliedFilters ?? draftFilters },
              })
            }
          >
            <span className="security-action-icon">{viewIcon}</span>
          </GenericButton>
        ),
      },
    ],
    [appliedFilters, draftFilters, navigate],
  );

  const handleSearch = () => {
    if (!draftFilters.startDate || !draftFilters.endDate) {
      showAlert({
        title: 'Filtros incompletos',
        message: 'La fecha inicio y la fecha final de creación son obligatorias.',
        severity: 'warning',
      });
      return;
    }
    setAppliedFilters({ ...draftFilters });
    setPage(1);
    setSelectedRowIds(new Set());
  };

  const handleExport = async () => {
    if (!appliedFilters || appliedInvalid) return;

    const filename = buildCsvFilename('Catálogo Usuarios');
    const selectedRows = rows.filter((row) => selectedRowIds.has(row.id));
    if (selectedRows.length > 0) {
      downloadCsv(buildUserCatalogCsv(selectedRows), filename);
      return;
    }

    try {
      await securityService.downloadUserCatalogCsv(appliedFilters, traceId(), filename);
    } catch (e) {
      console.error('[Catálogo Usuarios] CSV', e);
      showAlert({
        title: 'Exportación CSV',
        message: getErrorMessage(e, 'No fue posible exportar el catálogo.'),
        severity: 'error',
      });
    }
  };

  const toggleRowSelection = (id: number, selected: boolean) => {
    setSelectedRowIds((prev) => {
      const next = new Set(prev);
      if (selected) next.add(id);
      else next.delete(id);
      return next;
    });
  };

  const toggleSelectAll = () => {
    setSelectedRowIds((prev) => {
      const next = new Set(prev);
      if (areAllRowsSelected) {
        rows.forEach((row) => next.delete(row.id));
      } else {
        rows.forEach((row) => next.add(row.id));
      }
      return next;
    });
  };

  useEffect(() => {
    const validIds = new Set(rows.map((row) => row.id));
    setSelectedRowIds((prev) => {
      const next = new Set<number>();
      prev.forEach((id) => {
        if (validIds.has(id)) next.add(id);
      });
      return next;
    });
  }, [rows]);

  const loading = search.isFetching;

  return (
    <div className="security-layout">
      <SecurityBreadcrumb items={['Inicio', 'Herramientas y Utilerias', 'Control de Acceso', 'Catálogo Usuarios']} />
      <div className="security-box">
        <div className="security-page-header">
          <div>
            <h1 className="security-title">Catálogo Usuarios</h1>
            <p className="security-subtitle">
              Consulta el perfil, roles, aplicaciones y atributos asociados a cada usuario.
            </p>
          </div>
          <GenericButton variant="primary" onClick={handleExport} disabled={!appliedFilters || appliedInvalid}>
            Exportar a CSV
          </GenericButton>
        </div>
        <div className="security-toolbar">
          <UserCatalogFilters filters={draftFilters} onChange={setDraftFilters} />
          <div className="security-actions">
            <GenericButton variant="outlineFill" onClick={handleSearch}>
              Buscar
            </GenericButton>
            <GenericButton
              variant="outlineFill"
              onClick={() => {
                setDraftFilters(initialFilters);
                setAppliedFilters(null);
                setPage(1);
                setSelectedRowIds(new Set());
              }}
            >
              Limpiar
            </GenericButton>
          </div>
        </div>
        <div className="security-grid-toolbar">
          <div>
            {selectedRowIds.size > 0
              ? `${selectedRowIds.size} seleccionado(s) de ${search.data?.total ?? rows.length} registros encontrados`
              : `${search.data?.total ?? rows.length} registros encontrados`}
          </div>
        </div>
        <GenericTable
          rows={rows}
          columns={columns}
          emptyLabel="Sin resultados"
          perPage={limit}
          page={page}
          totalPages={totalPages}
          totalItems={search.data?.total ?? 0}
          onChangePage={setPage}
          onChangePerPage={(value) => {
            setLimit(value);
            setPage(1);
          }}
          enableSelection
          selectedIds={Array.from(selectedRowIds)}
          onSelectRow={(id, selected) => toggleRowSelection(Number(id), selected)}
          selectionHeader={
            <input
              type="checkbox"
              checked={areAllRowsSelected}
              onChange={toggleSelectAll}
              aria-label="Seleccionar todos los usuarios"
            />
          }
        />
        <div className="security-footer-back">
          <GenericButton variant="back" onClick={() => navigate('/seguridad')}>
            Volver
          </GenericButton>
        </div>
      </div>
      {loading ? <GenericModal visible variant="loading" message="Consultando catálogo..." /> : null}
      {alertModal}
    </div>
  );
}
