import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { exportToCSV, exportToExcel, ExportColumn } from '@features/catalogos/utils/export';
import { catalogService, CatalogResponse } from '@features/catalogos/services/catalogosApi';
import eyeIcon from '@features/catalogos/assets/eye-show.svg';
import editIcon from '@features/catalogos/assets/edit.svg';
import lobbyIcon from '@features/catalogos/assets/lobby.svg';
import { useModalNotification } from '@shared/components/ui/modal';
import { Pagination } from '@shared/components/ui/pagination';
import { APP_EVENT, PermissionGate } from '@shared/security';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import { withFinanceBreadcrumb } from '@shared/components/ui/navigation/financeBreadcrumb';

interface Catalog {
  id: string;
  displayId: string;
  code: string;
  prefix: string;
  name: string;
  description: string;
  type: 'Primario' | 'Secundario';
  elementCount: number;
  status: 'Activo' | 'Inactivo';
  createdBy: string;
  createdAt: string;
  updatedBy: string | null;
  updatedAt: string | null;
}

const apiToCatalog = (item: CatalogResponse): Catalog => ({
  id: String(item.id), 
  displayId: String(item.id).padStart(4, '0'), 
  code: item.code || '',
  prefix: item.prefix || '',
  name: item.name,
  description: item.description || '',
  type: item.catalogType === 'PRIMARIO' ? 'Primario' : 'Secundario',
  elementCount: item.elementCount ?? 0,
  status: item.status === 1 ? 'Activo' : 'Inactivo',
  createdBy: item.createdBy || 'system',
  createdAt: item.createdAt ? item.createdAt.split('T')[0] : '',
  updatedBy: item.updatedBy || null,
  updatedAt: item.updatedAt ? item.updatedAt.split('T')[0] : null,
});

const styles = {
  container: {
    minHeight: '100vh',
    width: '100%',
    backgroundColor: '#ffffff',
  },
  header: {
    padding: '1rem 2rem',
  },
  breadcrumb: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
    fontSize: '0.875rem',
    color: '#6b7280',
    marginBottom: '1rem',
  },
  breadcrumbLink: {
    color: '#003865',
    textDecoration: 'none',
  },
  main: {
    padding: '0 2rem 2rem 2rem',
  },
  card: {
    border: '1px solid #d1d5db',
    borderRadius: '0.375rem',
    padding: '1.5rem',
    backgroundColor: 'transparent',
  },
  titleRow: {
    display: 'flex',
    alignItems: 'flex-start',
    gap: '1rem',
    marginBottom: '0.5rem',
  },
  titleIcon: {
    width: '48px',
    height: '48px',
    backgroundColor: '#EAF3FB',
    borderRadius: '0.5rem',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: '#002d4c',
  },
  title: {
    fontSize: '1.5rem',
    fontWeight: 500,
    color: '#1f2937',
    margin: 0,
  },
  headerContent: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: '1.5rem',
  },
  description: {
    fontSize: '0.875rem',
    color: '#6b7280',
    maxWidth: '600px',
    lineHeight: 1.5,
    marginTop: '0.5rem',
  },
  primaryBtn: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.5rem',
    padding: '0.625rem 1.5rem',
    backgroundColor: '#002d4c',
    color: '#ffffff',
    border: 'none',
    borderRadius: '0.375rem',
    fontSize: '0.875rem',
    fontWeight: 500,
    cursor: 'pointer',
  },
  secondaryBtn: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.5rem',
    padding: '0.625rem 1rem',
    backgroundColor: '#ffffff',
    color: '#1f2937',
    border: '1px solid #d1d5db',
    borderRadius: '0.375rem',
    fontSize: '0.875rem',
    fontWeight: 500,
    cursor: 'pointer',
  },
  headerActions: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.75rem',
    flexWrap: 'wrap' as const,
    justifyContent: 'flex-end',
  },
  filtersRow: {
    display: 'flex',
    alignItems: 'flex-end',
    gap: '1rem',
    flexWrap: 'wrap' as const,
    marginBottom: '1.5rem',
    paddingBottom: '1.5rem',
    borderBottom: '1px solid #e5e7eb',
  },
  filterGroup: {
    display: 'flex',
    flexDirection: 'column' as const,
    gap: '0.25rem',
  },
  filterLabel: {
    fontSize: '0.75rem',
    color: '#6b7280',
    fontWeight: 500,
  },
  filterInput: {
    padding: '0.4rem 0.5rem',
    border: '1px solid #d1d5db',
    borderRadius: '0.375rem',
    fontSize: '0.8rem',
    minWidth: '90px',
    maxWidth: '130px',
    outline: 'none',
  },
  filterSelect: {
    padding: '0.4rem 0.5rem',
    border: '1px solid #d1d5db',
    borderRadius: '0.375rem',
    fontSize: '0.8rem',
    minWidth: '90px',
    maxWidth: '130px',
    backgroundColor: '#ffffff',
    outline: 'none',
  },
  ghostBtn: {
    padding: '0.5rem 1rem',
    backgroundColor: 'transparent',
    color: '#003865',
    border: 'none',
    fontSize: '0.875rem',
    fontWeight: 500,
    cursor: 'pointer',
    textDecoration: 'underline',
  },
  outlineBtn: {
    padding: '0.5rem 1.5rem',
    backgroundColor: '#ffffff',
    color: '#1f2937',
    border: '1px solid #d1d5db',
    borderRadius: '0.375rem',
    fontSize: '0.875rem',
    fontWeight: 500,
    cursor: 'pointer',
  },
  emptyState: {
    display: 'flex',
    flexDirection: 'column' as const,
    alignItems: 'center',
    justifyContent: 'center',
    padding: '4rem 2rem',
    textAlign: 'center' as const,
  },
  emptyImage: {
    width: '280px',
    height: 'auto',
    marginBottom: '1.5rem',
    opacity: 0.9,
  },
  emptyText: {
    fontSize: '1rem',
    color: '#4b5563',
    fontWeight: 500,
  },
  resultsHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '0.75rem',
  },
  resultsCount: {
    fontSize: '0.875rem',
    fontWeight: 600,
    color: '#1f2937',
  },
  selectAll: {
    fontSize: '0.75rem',
    color: '#6b7280',
  },
  table: {
    width: '100%',
    borderCollapse: 'collapse' as const,
    fontSize: '0.7rem',
  },
  th: {
    padding: '0.4rem 0.3rem',
    textAlign: 'left' as const,
    fontWeight: 500,
    color: '#002d4c',
    backgroundColor: '#eaf5fc',
    whiteSpace: 'nowrap' as const,
    fontSize: '0.65rem',
  },
  td: {
    padding: '0.4rem 0.3rem',
    borderBottom: '1px solid #e5e7eb',
    color: '#4b5563',
    verticalAlign: 'middle' as const,
    fontSize: '0.7rem',
    maxWidth: '100px',
    overflow: 'hidden' as const,
    textOverflow: 'ellipsis' as const,
    whiteSpace: 'nowrap' as const,
  },
  statusBadge: {
    display: 'inline-block',
    padding: '0.125rem 0.5rem',
    borderRadius: '9999px',
    fontSize: '0.75rem',
    fontWeight: 500,
  },
  statusActive: {
    backgroundColor: '#dcfce7',
    color: '#166534',
  },
  statusInactive: {
    backgroundColor: '#fef3c7',
    color: '#92400e',
  },
  actionBtn: {
    padding: '0.25rem',
    backgroundColor: 'transparent',
    border: 'none',
    cursor: 'pointer',
    color: '#002d4c',
    opacity: 1,
    transition: 'opacity 0.2s',
  },
  pagination: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    flexWrap: 'wrap' as const,
    rowGap: '0.5rem',
    columnGap: '1rem',
    marginTop: '1rem',
    paddingTop: '1rem',
    borderTop: '1px solid #e5e7eb',
  },
  paginationLeft: {
    display: 'flex',
    alignItems: 'center',
    gap: '1rem',
    fontSize: '0.8125rem',
    color: '#6b7280',
    flexWrap: 'wrap' as const,
  },
  paginationRight: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
  },
  pageBtn: {
    padding: '0.375rem 0.75rem',
    border: '1px solid #d1d5db',
    backgroundColor: '#ffffff',
    borderRadius: '0.25rem',
    fontSize: '0.8125rem',
    cursor: 'pointer',
  },
  pageBtnActive: {
    backgroundColor: '#002d4c',
    color: '#ffffff',
    borderColor: '#002d4c',
  },
  footer: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: '1rem',
  },
  noResults: {
    padding: '3rem',
    textAlign: 'center' as const,
    color: '#6b7280',
  },
};

const CatalogIcon = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
    <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
  </svg>
);

const ExportFileIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
    <polyline points="14 2 14 8 20 8" />
    <line x1="12" y1="18" x2="12" y2="12" />
    <polyline points="9 15 12 18 15 15" />
  </svg>
);

export default function CatalogsContainer() {
  const navigate = useNavigate();
  const { showError, ModalNode } = useModalNotification();
  const [hasSearched, setHasSearched] = useState(false);
  const [catalogs, setCatalogs] = useState<Catalog[]>([]);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [isExporting, setIsExporting] = useState(false);
  const [exportError, setExportError] = useState<string | null>(null);

  const [filters, setFilters] = useState({
    id: '',
    code: '',
    prefix: '',
    name: '',
    description: '',
    type: '',
    status: '',
  });

  const [isLoading, setIsLoading] = useState(false);
  const [totalResults, setTotalResults] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  
  const handleSearch = async (
    page: number = 1,
    resetSelection: boolean = false,
    overridePageSize?: number,
  ) => {
    setIsLoading(true);
    try {
      const effectivePageSize = overridePageSize ?? pageSize;
      const params: any = {
        page: page,
        pageSize: effectivePageSize,
        sortBy: 'createdAt',
        sortDir: 'desc',
      };

      if (filters.id) {
        params.id = parseInt(filters.id);
      }
      if (filters.name) {
        params.nombre = filters.name;
      }
      if (filters.description) {
        params.descripcion = filters.description;
      }
      if (filters.type) {
        params.tipo = filters.type === 'Primario' ? 'PRIMARIO' : 'SECUNDARIO';
      }
      if (filters.status) {
        params.estatus = filters.status === 'Activo' ? 1 : 0;
      }
      if (filters.code) {
        params.code = filters.code;
      }
      if (filters.prefix) {
        params.prefix = filters.prefix;
      }

      const response = await catalogService.search(params);
      
      const result = response.items.map(apiToCatalog);
      setCatalogs(result);
      setTotalResults(response.total);
      setTotalPages(response.totalPages);
      setHasSearched(true);
      setCurrentPage(page);
      if (resetSelection) {
        setSelectedIds([]);
      }
    } catch (error: any) {
      console.error('Error searching catalogs:', error);
      const message = error?.response?.status === 500 
        ? 'Ocurrió un problema al consultar catálogos. Intente nuevamente.'
        : 'No fue posible realizar la búsqueda. Verifique los criterios e intente nuevamente.';
      showError(message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleClear = () => {
    setFilters({ id: '', code: '', prefix: '', name: '', description: '', type: '', status: '' });
    setCatalogs([]);
    setHasSearched(false);
    setSelectedIds([]);
    setTotalResults(0);
    setTotalPages(0);
    setCurrentPage(1);
  };

  const handlePageChange = (newPage: number) => {
    handleSearch(newPage, false);
  };

  const handleSelectAll = (checked: boolean) => {
    const currentPageIds = paginatedCatalogs.map((c) => c.id);
    if (checked) {
      const newSelected = new Set([...selectedIds, ...currentPageIds]);
      setSelectedIds(Array.from(newSelected));
    } else {
      setSelectedIds(selectedIds.filter((id) => !currentPageIds.includes(id)));
    }
  };

  const selectOne = (id: string) => {
    setSelectedIds([...selectedIds, id]);
  };

  const unselectOne = (id: string) => {
    setSelectedIds(selectedIds.filter((i) => i !== id));
  };

  const isAllCurrentPageSelected = () => {
    if (paginatedCatalogs.length === 0) return false;
    return paginatedCatalogs.every((c) => selectedIds.includes(c.id));
  };

  const buildExportSearchParams = () => {
    const allParams: any = {
      page: 1,
      pageSize: totalResults || 5000,
      sortBy: 'createdAt',
      sortDir: 'desc',
    };
    if (filters.id) allParams.id = parseInt(filters.id);
    if (filters.name) allParams.nombre = filters.name;
    if (filters.description) allParams.descripcion = filters.description;
    if (filters.type) allParams.tipo = filters.type === 'Primario' ? 'PRIMARIO' : 'SECUNDARIO';
    if (filters.status) allParams.estatus = filters.status === 'Activo' ? 1 : 0;
    if (filters.code) allParams.code = filters.code;
    if (filters.prefix) allParams.prefix = filters.prefix;
    return allParams;
  };

  const exportColumns: ExportColumn[] = [
    { key: 'id', label: 'ID Catálogo' },
    { key: 'code', label: 'Código' },
    { key: 'prefix', label: 'Prefijo' },
    { key: 'name', label: 'Nombre Catálogo' },
    { key: 'description', label: 'Descripción Catálogo' },
    { key: 'type', label: 'Tipo Catálogo' },
    { key: 'elementCount', label: 'Total Elementos' },
    { key: 'status', label: 'Estatus' },
    { key: 'createdBy', label: 'ID Usuario Registro' },
    { key: 'createdAt', label: 'Fecha Registro' },
    { key: 'updatedBy', label: 'ID Usuario Actualización' },
    { key: 'updatedAt', label: 'Fecha Actualización' },
  ];

  const handleExport = async (format: 'csv' | 'xlsx') => {
    if (catalogs.length === 0 || isExporting) return;

    setIsExporting(true);
    setExportError(null);

    try {
      const response = await catalogService.search(buildExportSearchParams());
      const fullDataset = response.items.map(apiToCatalog);

      const dataToExport: Catalog[] = selectedIds.length > 0
        ? fullDataset.filter((c) => selectedIds.includes(c.id))
        : fullDataset;

      if (dataToExport.length === 0) {
        setExportError(
          selectedIds.length > 0
            ? 'No se encontraron los catálogos seleccionados en el conjunto filtrado.'
            : 'No hay catálogos para exportar.'
        );
        return;
      }

      const timestamp = new Date().toISOString().replace(/[-:T]/g, '').slice(0, 14);
      const filename = `catalogos_${timestamp}`;
      const rows = dataToExport as unknown as Record<string, unknown>[];

      if (format === 'csv') {
        exportToCSV(rows, exportColumns, filename);
      } else {
        exportToExcel(rows, exportColumns, filename);
      }
    } catch (error) {
      setExportError('Ocurrió un problema al exportar. Intente nuevamente.');
      console.error('Export error:', error);
    } finally {
      setIsExporting(false);
    }
  };

  const paginatedCatalogs = catalogs;

  const formatDate = (date: string | null) => {
    if (!date) return '-';
    const d = new Date(date);
    return d.toLocaleDateString('es-MX', { day: '2-digit', month: '2-digit', year: 'numeric' }).replace(/\//g, '-');
  };

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <Breadcrumb
          items={withFinanceBreadcrumb([
            { label: 'Gestión de Catálogos', to: '/util/catalogos' },
            { label: 'Catálogos' },
          ])}
        />
      </div>

      <div style={styles.main}>
        <div style={styles.card}>
          <div style={styles.headerContent}>
            <div>
              <div style={styles.titleRow}>
                <div style={styles.titleIcon}>
                  <CatalogIcon />
                </div>
                <h1 style={styles.title}>Catálogos</h1>
              </div>
              <p style={styles.description}>
                Busca, gestiona y consulta catálogos junto con sus elementos. Además, obtén
                información detallada sobre los cambios realizados en cada uno de ellos.
              </p>
            </div>
            <div style={styles.headerActions}>
              <PermissionGate appEvent={APP_EVENT.CATALOGS_CATALOG.DOWNLOAD_CSV}>
                {(() => {
                  const exportDisabled = isExporting || !hasSearched || catalogs.length === 0;
                  return (
                    <>
                      <button
                        style={{ ...styles.secondaryBtn, opacity: exportDisabled ? 0.5 : 1, cursor: exportDisabled ? 'not-allowed' : 'pointer' }}
                        onClick={() => handleExport('csv')}
                        disabled={exportDisabled}
                        title="Exportar a CSV"
                      >
                        <ExportFileIcon />
                        Exportar CSV
                      </button>
                      <button
                        style={{ ...styles.secondaryBtn, opacity: exportDisabled ? 0.5 : 1, cursor: exportDisabled ? 'not-allowed' : 'pointer' }}
                        onClick={() => handleExport('xlsx')}
                        disabled={exportDisabled}
                        title="Exportar a Excel"
                      >
                        <ExportFileIcon />
                        Exportar Excel
                      </button>
                    </>
                  );
                })()}
              </PermissionGate>
              <button
                style={styles.primaryBtn}
                onClick={() => navigate('/util/catalogos/catalogs/crear')}
              >
                <span style={{ fontSize: '1.25rem', lineHeight: 1 }}>⊕</span>
                {' '}
                Nuevo Catálogo
              </button>
            </div>
          </div>

          <div style={styles.filtersRow}>
            <div style={styles.filterGroup}>
              <label htmlFor="filter-catalog-id" style={styles.filterLabel}>ID Catálogo</label>
              <input
                id="filter-catalog-id"
                type="text"
                style={styles.filterInput}
                value={filters.id}
                onChange={(e) => setFilters({ ...filters, id: e.target.value })}
                placeholder=""
              />
            </div>
            <div style={styles.filterGroup}>
              <label htmlFor="filter-catalog-code" style={styles.filterLabel}>Código</label>
              <input
                id="filter-catalog-code"
                type="text"
                style={styles.filterInput}
                value={filters.code}
                onChange={(e) => setFilters({ ...filters, code: e.target.value.toUpperCase() })}
                placeholder=""
              />
            </div>
            <div style={styles.filterGroup}>
              <label htmlFor="filter-catalog-prefix" style={styles.filterLabel}>Prefijo</label>
              <input
                id="filter-catalog-prefix"
                type="text"
                style={styles.filterInput}
                value={filters.prefix}
                onChange={(e) => setFilters({ ...filters, prefix: e.target.value.toUpperCase() })}
                placeholder=""
              />
            </div>
            <div style={styles.filterGroup}>
              <label htmlFor="filter-catalog-name" style={styles.filterLabel}>Nombre Catálogo</label>
              <input
                id="filter-catalog-name"
                type="text"
                style={{ ...styles.filterInput, minWidth: '120px' }}
                value={filters.name}
                onChange={(e) => setFilters({ ...filters, name: e.target.value })}
                placeholder=""
              />
            </div>
            <div style={styles.filterGroup}>
              <label htmlFor="filter-catalog-description" style={styles.filterLabel}>Descripción Catálogo</label>
              <input
                id="filter-catalog-description"
                type="text"
                style={{ ...styles.filterInput, minWidth: '130px', maxWidth: '180px' }}
                value={filters.description}
                onChange={(e) => setFilters({ ...filters, description: e.target.value })}
                placeholder=""
              />
            </div>
            <div style={styles.filterGroup}>
              <label htmlFor="filter-catalog-type" style={styles.filterLabel}>Tipo Catálogo</label>
              <select
                id="filter-catalog-type"
                style={styles.filterSelect}
                value={filters.type}
                onChange={(e) => setFilters({ ...filters, type: e.target.value })}
              >
                <option value="">Todos</option>
                <option value="Primario">Primario</option>
                <option value="Secundario">Secundario</option>
              </select>
            </div>
            <div style={styles.filterGroup}>
              <label htmlFor="filter-catalog-status" style={styles.filterLabel}>Estatus</label>
              <select
                id="filter-catalog-status"
                style={styles.filterSelect}
                value={filters.status}
                onChange={(e) => setFilters({ ...filters, status: e.target.value })}
              >
                <option value="">Todos</option>
                <option value="Activo">Activo</option>
                <option value="Inactivo">Inactivo</option>
              </select>
            </div>
            <PermissionGate appEvent={APP_EVENT.CATALOGS_CATALOG.SEARCH}>
              <button style={styles.outlineBtn} onClick={() => handleSearch(1, true)}>
                Buscar
              </button>
            </PermissionGate>
            <PermissionGate appEvent={APP_EVENT.CATALOGS_CATALOG.CLEAR_FILTERS}>
              <button style={styles.outlineBtn} onClick={handleClear}>
                Limpiar
              </button>
            </PermissionGate>
          </div>

          {(() => {
            if (!hasSearched) {
              return (
                <div style={styles.emptyState}>
                  <img src={lobbyIcon} alt="Buscar catálogos" style={styles.emptyImage} />
                  <p style={styles.emptyText}>
                    Utiliza el filtro para realizar una búsqueda de catálogos
                  </p>
                </div>
              );
            }
            if (catalogs.length === 0) {
              return (
                <div style={styles.noResults}>
                  <p>No se encontraron catálogos coincidentes con los criterios de búsqueda ingresados.</p>
                </div>
              );
            }
            return (
            <>
              <div style={styles.resultsHeader}>
                <div>
                  <span style={styles.resultsCount}>{totalResults} Catálogos encontrados</span>
                  <br />
                  <span style={styles.selectAll}>
                    {selectedIds.length > 0 
                      ? `${selectedIds.length} seleccionado(s) de ${catalogs.length}`
                      : 'Seleccionar todo'
                    }
                  </span>
                </div>
              </div>

              <div style={{ overflowX: 'auto' }}>
                <table style={styles.table}>
                  <thead>
                    <tr>
                      <th style={{ ...styles.th, width: '40px' }}>
                        <input
                          type="checkbox"
                          checked={isAllCurrentPageSelected()}
                          onChange={(e) => handleSelectAll(e.target.checked)}
                        />
                      </th>
                      <th style={styles.th}>ID</th>
                      <th style={styles.th}>Código</th>
                      <th style={styles.th}>Prefijo</th>
                      <th style={styles.th}>Nombre</th>
                      <th style={styles.th}>Descripción</th>
                      <th style={styles.th}>Tipo Catálogo</th>
                      <th style={{ ...styles.th, textAlign: 'center', width: '120px' }}>Total Elementos</th>
                      <th style={styles.th}>Estatus</th>
                      <th style={styles.th}>ID Usuario Registro</th>
                      <th style={styles.th}>Fecha Registro</th>
                      <th style={styles.th}>ID Usuario Actualización</th>
                      <th style={styles.th}>Fecha Actualización</th>
                      <th style={styles.th}>Ver</th>
                      <th style={styles.th}>Editar</th>
                    </tr>
                  </thead>
                  <tbody>
                    {paginatedCatalogs.map((catalog) => (
                      <tr key={catalog.id}>
                        <td style={styles.td}>
                          <input
                            type="checkbox"
                            checked={selectedIds.includes(catalog.id)}
                            onChange={(e) => (e.target.checked ? selectOne(catalog.id) : unselectOne(catalog.id))}
                          />
                        </td>
                        <td style={styles.td}>{catalog.displayId}</td>
                        <td style={styles.td}>{catalog.code}</td>
                        <td style={styles.td}>{catalog.prefix}</td>
                        <td style={styles.td}>{catalog.name}</td>
                        <td style={styles.td}>{catalog.description}</td>
                        <td style={styles.td}>{catalog.type}</td>
                        <td style={{ ...styles.td, textAlign: 'center' }}>
                          {catalog.elementCount.toLocaleString('es-MX')}
                        </td>
                        <td style={styles.td}>
                          <span
                            style={{
                              ...styles.statusBadge,
                              ...(catalog.status === 'Activo' ? styles.statusActive : styles.statusInactive),
                            }}
                          >
                            {catalog.status}
                          </span>
                        </td>
                        <td style={styles.td}>{catalog.createdBy}</td>
                        <td style={styles.td}>{formatDate(catalog.createdAt)}</td>
                        <td style={styles.td}>{catalog.updatedBy || '-'}</td>
                        <td style={styles.td}>{formatDate(catalog.updatedAt)}</td>
                        <td style={styles.td}>
                          <button
                            style={styles.actionBtn}
                            title="Ver"
                            onClick={() => navigate(`/util/catalogos/catalogs/${catalog.id}/elementos`)}
                          >
                            <img src={eyeIcon} alt="Ver" style={{ width: '20px', height: '20px' }} />
                          </button>
                        </td>
                        <td style={styles.td}>
                          <button
                            style={styles.actionBtn}
                            title="Editar"
                            onClick={() => navigate(`/util/catalogos/catalogs/editar/${catalog.id}`)}
                          >
                            <img src={editIcon} alt="Editar" style={{ width: '20px', height: '20px' }} />
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                totalItems={totalResults}
                pageSize={pageSize}
                disabled={isLoading}
                onPageChange={handlePageChange}
                onPageSizeChange={(newSize) => {
                  setPageSize(newSize);
                  setCurrentPage(1);
                  handleSearch(1, false, newSize);
                }}
              />

              <div style={styles.footer}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  {selectedIds.length > 0 && (
                    <span style={{ fontSize: '0.8125rem', color: '#002D4C' }}>
                      {selectedIds.length} seleccionado(s)
                    </span>
                  )}
                  {exportError && (
                    <span style={{ fontSize: '0.8125rem', color: '#dc2626' }}>
                      {exportError}
                    </span>
                  )}
                </div>
                <button style={styles.ghostBtn} onClick={() => navigate('/util/catalogos')}>
                  Volver
                </button>
              </div>
            </>
            );
          })()}

          {!hasSearched && (
            <div style={{ ...styles.footer, justifyContent: 'flex-end' }}>
              <button style={styles.ghostBtn} onClick={() => navigate('/util/catalogos')}>
                Volver
              </button>
            </div>
          )}
        </div>
      </div>
      {ModalNode}
    </div>
  );
}

