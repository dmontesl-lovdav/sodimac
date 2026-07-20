import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { supplierBlockService, SupplierBlock } from '@features/catalogos/services/catalogosApi';
import { exportToCSV, exportToExcel, ExportColumn } from '@features/catalogos/utils/export';
import { useModalNotification } from '@shared/components/ui/modal';
import { extractApiErrorMessage } from '@shared/utils/errorMessage';
import editIcon from '@features/catalogos/assets/edit.svg';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import { withFinanceBreadcrumb } from '@shared/components/ui/navigation/financeBreadcrumb';
import { Pagination } from '@shared/components/ui/pagination';

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
  } as const,
  breadcrumbLink: {
    color: '#003865',
    textDecoration: 'none',
    cursor: 'pointer',
  } as const,
  main: {
    padding: '0 2rem 2rem 2rem',
  },
  card: {
    border: '1px solid #d1d5db',
    borderRadius: '0.375rem',
    padding: '1.5rem',
    backgroundColor: 'transparent',
  },
  headerContent: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: '1.5rem',
    flexWrap: 'nowrap' as const,
    gap: '1rem',
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
  description: {
    fontSize: '0.875rem',
    color: '#6b7280',
    maxWidth: '600px',
    lineHeight: 1.5,
    marginTop: '0.5rem',
  },
  headerActions: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
    flexWrap: 'nowrap' as const,
    justifyContent: 'flex-end',
    flexShrink: 0,
  },
  primaryBtn: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.375rem',
    padding: '0.5rem 0.875rem',
    backgroundColor: '#002d4c',
    color: '#ffffff',
    border: 'none',
    borderRadius: '0.375rem',
    fontSize: '0.8125rem',
    fontWeight: 500,
    cursor: 'pointer',
    whiteSpace: 'nowrap' as const,
  },
  secondaryBtn: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.375rem',
    padding: '0.5rem 0.75rem',
    backgroundColor: '#ffffff',
    color: '#1f2937',
    border: '1px solid #d1d5db',
    borderRadius: '0.375rem',
    fontSize: '0.8125rem',
    fontWeight: 500,
    cursor: 'pointer',
    whiteSpace: 'nowrap' as const,
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
    minWidth: '120px',
    maxWidth: '180px',
    outline: 'none',
  },
  filterSelect: {
    padding: '0.4rem 0.5rem',
    border: '1px solid #d1d5db',
    borderRadius: '0.375rem',
    fontSize: '0.8rem',
    minWidth: '120px',
    maxWidth: '160px',
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
  resultsHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '0.75rem',
    flexWrap: 'wrap' as const,
    gap: '1rem',
  },
  resultsCount: {
    fontSize: '0.875rem',
    fontWeight: 600,
    color: '#1f2937',
  },
  exportRow: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
  },
  exportSelect: {
    padding: '0.4rem 0.6rem',
    border: '1px solid #d1d5db',
    borderRadius: '0.375rem',
    fontSize: '0.8125rem',
    backgroundColor: '#ffffff',
    color: '#1f2937',
    cursor: 'pointer',
    outline: 'none',
  },
  exportBtn: {
    padding: '0.5rem 1.25rem',
    backgroundColor: '#ffffff',
    color: '#003865',
    border: '1px solid #003865',
    borderRadius: '0.375rem',
    fontSize: '0.875rem',
    fontWeight: 500,
    cursor: 'pointer',
  },
  tableWrapper: {
    overflowX: 'auto' as const,
    border: '1px solid #e5e7eb',
    borderRadius: '0.375rem',
  },
  table: {
    width: '100%',
    borderCollapse: 'collapse' as const,
    fontSize: '0.75rem',
  },
  th: {
    padding: '0.6rem 0.5rem',
    textAlign: 'left' as const,
    fontWeight: 600,
    color: '#002d4c',
    backgroundColor: '#eaf5fc',
    whiteSpace: 'normal' as const,
    fontSize: '0.7rem',
    lineHeight: 1.25,
    verticalAlign: 'middle' as const,
  },
  td: {
    padding: '0.5rem 0.5rem',
    borderBottom: '1px solid #e5e7eb',
    color: '#4b5563',
    fontSize: '0.75rem',
    verticalAlign: 'middle' as const,
    wordBreak: 'break-word' as const,
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
  badgeDanger: { backgroundColor: '#fee2e2', color: '#991b1b' },
  badgeInfo: { backgroundColor: '#dbeafe', color: '#1e40af' },
  actionBtn: {
    padding: '0.25rem',
    backgroundColor: 'transparent',
    border: 'none',
    cursor: 'pointer',
    color: '#002d4c',
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  toggleSwitch: {
    position: 'relative' as const,
    display: 'inline-block',
    width: '40px',
    height: '20px',
    cursor: 'pointer',
  },
  toggleSlider: {
    position: 'absolute' as const,
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: '#cbd5e1',
    transition: '0.3s',
    borderRadius: '20px',
  },
  toggleSliderActive: {
    backgroundColor: '#0066CC',
  },
  toggleKnob: {
    position: 'absolute' as const,
    height: '16px',
    width: '16px',
    left: '2px',
    bottom: '2px',
    backgroundColor: 'white',
    transition: '0.3s',
    borderRadius: '50%',
  },
  toggleKnobActive: {
    transform: 'translateX(20px)',
  },
  pagination: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: '1rem',
    paddingTop: '1rem',
    borderTop: '1px solid #e5e7eb',
    flexWrap: 'wrap' as const,
    gap: '0.75rem',
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
  loading: {
    display: 'flex',
    justifyContent: 'center',
    padding: '2.5rem 1rem',
  },
  spinner: {
    width: '2.25rem',
    height: '2.25rem',
    border: '3px solid #e5e7eb',
    borderTopColor: '#002d4c',
    borderRadius: '50%',
    animation: 'somx-spin 0.8s linear infinite',
  },
  error: {
    backgroundColor: '#fee2e2',
    color: '#991b1b',
    border: '1px solid #fecaca',
    borderRadius: '0.375rem',
    padding: '0.75rem 1rem',
    marginBottom: '1rem',
    fontSize: '0.875rem',
  },
  noResults: {
    padding: '3rem',
    textAlign: 'center' as const,
    color: '#6b7280',
  },
};

const BlocksIcon = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
    <path d="M7 11V7a5 5 0 0 1 10 0v4" />
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

const SupplierBlocksContainer = () => {
  const navigate = useNavigate();
  const { showError, ModalNode } = useModalNotification();
  const [filteredBlocks, setFilteredBlocks] = useState<SupplierBlock[]>([]);
  const [loading, setLoading] = useState(false);
  const [togglingId, setTogglingId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [hasSearched, setHasSearched] = useState(false);

  const [filters, setFilters] = useState({
    supplierNumber: '',
    status: '',
    currentlyBlocked: '',
  });

  const applyFilters = (data: SupplierBlock[]): SupplierBlock[] => {
    let result = [...data];
    if (filters.supplierNumber) {
      result = result.filter((b) =>
        b.supplierNumber.toLowerCase().includes(filters.supplierNumber.toLowerCase()),
      );
    }
    if (filters.status) {
      result = result.filter((b) => b.status === parseInt(filters.status));
    }
    if (filters.currentlyBlocked) {
      const isBlocked = filters.currentlyBlocked === '1';
      result = result.filter((b) => b.currentlyBlocked === isBlocked);
    }
    return result;
  };

  const loadData = async (): Promise<SupplierBlock[]> => {
    setLoading(true);
    setError(null);
    try {
      const data = await supplierBlockService.getAll();
      return data;
    } catch (err) {
      console.error('Error al cargar bloqueos:', err);
      setError(
        extractApiErrorMessage(err, {
          fallback: 'No fue posible cargar los bloqueos de proveedores. Inténtalo más tarde.',
        }),
      );
      return [];
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    setHasSearched(true);
    const data = await loadData();
    setFilteredBlocks(applyFilters(data));
    setCurrentPage(1);
  };

  const handleClearFilters = () => {
    setFilters({ supplierNumber: '', status: '', currentlyBlocked: '' });
    setFilteredBlocks([]);
    setHasSearched(false);
    setCurrentPage(1);
  };

  const formatDate = (dateStr: string | null | undefined): string => {
    if (!dateStr) return '-';
    try {
      return new Date(dateStr).toLocaleDateString('es-MX');
    } catch {
      return dateStr;
    }
  };

  const handleExport = (format: 'csv' | 'excel') => {
    const columns: ExportColumn[] = [
      { key: 'id', label: 'ID' },
      { key: 'supplierNumber', label: 'Número Proveedor' },
      { key: 'validFrom', label: 'Fecha Inicio' },
      { key: 'validTo', label: 'Fecha Fin' },
      { key: 'blockReason', label: 'Razón' },
      { key: 'currentlyBlockedText', label: 'Vigente' },
      { key: 'statusText', label: 'Estatus' },
      { key: 'createdAt', label: 'Fecha Creación' },
    ];

    const dataToExport = filteredBlocks.map((b) => ({
      id: b.id,
      supplierNumber: b.supplierNumber,
      validFrom: b.validFrom,
      validTo: b.validTo,
      blockReason: b.blockReason || '',
      currentlyBlockedText: b.currentlyBlocked ? 'Sí' : 'No',
      statusText: b.status === 1 ? 'Activo' : 'Inactivo',
      createdAt: b.createdAt ? formatDate(b.createdAt) : '',
    }));

    const filename = `bloqueos_proveedores_${new Date().toISOString().split('T')[0]}`;

    if (format === 'csv') {
      exportToCSV(dataToExport, columns, filename);
    } else {
      exportToExcel(dataToExport, columns, filename);
    }
  };

  const handleToggleStatus = async (block: SupplierBlock) => {
    if (togglingId) return;
    setTogglingId(block.id);
    try {
      const newStatus = block.status === 1 ? 0 : 1;
      await supplierBlockService.update(block.id, { status: newStatus });
      const refreshed = await loadData();
      setFilteredBlocks(applyFilters(refreshed));
    } catch (err) {
      console.error('Error al cambiar estatus del bloqueo:', err);
      showError(
        extractApiErrorMessage(err, {
          fallback: 'No fue posible cambiar el estatus del bloqueo. Inténtalo nuevamente.',
        }),
        'No se pudo cambiar el estatus',
      );
    } finally {
      setTogglingId(null);
    }
  };

  const totalPages = Math.max(1, Math.ceil(filteredBlocks.length / pageSize));
  const safePage = Math.min(currentPage, totalPages);
  const paginatedBlocks = filteredBlocks.slice(
    (safePage - 1) * pageSize,
    safePage * pageSize,
  );

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <Breadcrumb
          items={withFinanceBreadcrumb([
            { label: 'Gestión de Catálogos', to: '/util/catalogos' },
            { label: 'Bloqueo de Proveedores' },
          ])}
        />
      </div>

      <main style={styles.main}>
        <section style={styles.card}>
          <div style={styles.headerContent}>
            <div>
              <div style={styles.titleRow}>
                <div style={styles.titleIcon}>
                  <BlocksIcon />
                </div>
                <h1 style={styles.title}>Bloqueo de Proveedores</h1>
              </div>
              <p style={styles.description}>
                Busca, registra y gestiona los bloqueos aplicados a proveedores con sus razones,
                vigencias y estatus.
              </p>
            </div>
            <div style={styles.headerActions}>
              <button
                style={{
                  ...styles.secondaryBtn,
                  opacity: filteredBlocks.length === 0 ? 0.5 : 1,
                  cursor: filteredBlocks.length === 0 ? 'not-allowed' : 'pointer',
                }}
                onClick={() => handleExport('csv')}
                disabled={filteredBlocks.length === 0}
                title="Exportar a CSV"
              >
                <ExportFileIcon />
                Exportar CSV
              </button>
              <button
                style={{
                  ...styles.secondaryBtn,
                  opacity: filteredBlocks.length === 0 ? 0.5 : 1,
                  cursor: filteredBlocks.length === 0 ? 'not-allowed' : 'pointer',
                }}
                onClick={() => handleExport('excel')}
                disabled={filteredBlocks.length === 0}
                title="Exportar a Excel"
              >
                <ExportFileIcon />
                Exportar Excel
              </button>
              <button
                style={styles.primaryBtn}
                onClick={() => navigate('/util/catalogos/bloqueos/crear')}
              >
                <span style={{ fontSize: '1.25rem', lineHeight: 1 }}>⊕</span>
                Nuevo Bloqueo
              </button>
            </div>
          </div>

          {error && <div style={styles.error}>{error}</div>}

          <div style={styles.filtersRow}>
            <div style={styles.filterGroup}>
              <label htmlFor="filter-block-supplier-number" style={styles.filterLabel}>Número Proveedor</label>
              <input
                id="filter-block-supplier-number"
                type="text"
                inputMode="numeric"
                pattern="[0-9]*"
                style={styles.filterInput}
                value={filters.supplierNumber}
                onChange={(e) =>
                  setFilters({
                    ...filters,
                    supplierNumber: e.target.value.replace(/\D+/g, ''),
                  })
                }
                placeholder="Buscar..."
              />
            </div>
            <div style={styles.filterGroup}>
              <label htmlFor="filter-block-status" style={styles.filterLabel}>Estatus</label>
              <select
                id="filter-block-status"
                style={styles.filterSelect}
                value={filters.status}
                onChange={(e) => setFilters({ ...filters, status: e.target.value })}
              >
                <option value="">Todos</option>
                <option value="1">Activo</option>
                <option value="0">Inactivo</option>
              </select>
            </div>
            <div style={styles.filterGroup}>
              <label htmlFor="filter-block-currently" style={styles.filterLabel}>Vigente Actualmente</label>
              <select
                id="filter-block-currently"
                style={styles.filterSelect}
                value={filters.currentlyBlocked}
                onChange={(e) => setFilters({ ...filters, currentlyBlocked: e.target.value })}
              >
                <option value="">Todos</option>
                <option value="1">Sí</option>
                <option value="0">No</option>
              </select>
            </div>
            <button style={styles.outlineBtn} onClick={handleSearch}>
              Buscar
            </button>
            <button style={styles.outlineBtn} onClick={handleClearFilters}>
              Limpiar
            </button>
          </div>

          <div style={styles.resultsHeader}>
            <span style={styles.resultsCount}>
              {hasSearched ? `${filteredBlocks.length} Bloqueos encontrados` : '0 Bloqueos encontrados'}
            </span>
          </div>

          {(() => {
            if (loading) {
              return (
                <div style={styles.loading}>
                  <div style={styles.spinner}></div>
                </div>
              );
            }
            if (!hasSearched) {
              return (
                <div style={styles.noResults}>
                  Aplica los filtros y presiona Buscar para consultar los bloqueos.
                </div>
              );
            }
            if (filteredBlocks.length === 0) {
              return (
                <div style={styles.noResults}>
                  No se encontraron bloqueos con los criterios de búsqueda ingresados.
                </div>
              );
            }
            return (
            <>
              <div style={styles.tableWrapper}>
                <table style={styles.table}>
                  <thead>
                    <tr>
                      <th style={styles.th}>ID</th>
                      <th style={styles.th}>Número Proveedor</th>
                      <th style={styles.th}>Fecha Inicio</th>
                      <th style={styles.th}>Fecha Fin</th>
                      <th style={styles.th}>Razón</th>
                      <th style={styles.th}>Vigente</th>
                      <th style={styles.th}>Estatus</th>
                      <th style={styles.th}>Editar</th>
                      <th style={styles.th}>Act./Desact.</th>
                    </tr>
                  </thead>
                  <tbody>
                    {paginatedBlocks.map((block) => (
                      <tr key={block.id}>
                        <td style={styles.td}>{block.id}</td>
                        <td style={styles.td}>{block.supplierNumber}</td>
                        <td style={styles.td}>{formatDate(block.validFrom)}</td>
                        <td style={styles.td}>{formatDate(block.validTo)}</td>
                        <td style={styles.td}>{block.blockReason || '-'}</td>
                        <td style={styles.td}>
                          <span
                            style={{
                              ...styles.statusBadge,
                              ...(block.currentlyBlocked
                                ? styles.badgeDanger
                                : styles.badgeInfo),
                            }}
                          >
                            {block.currentlyBlocked ? 'Sí' : 'No'}
                          </span>
                        </td>
                        <td style={styles.td}>
                          <span
                            style={{
                              ...styles.statusBadge,
                              ...(block.status === 1
                                ? styles.statusActive
                                : styles.statusInactive),
                            }}
                          >
                            {block.status === 1 ? 'Activo' : 'Inactivo'}
                          </span>
                        </td>
                        <td style={styles.td}>
                          <button
                            style={styles.actionBtn}
                            title="Editar"
                            onClick={() =>
                              navigate(`/util/catalogos/bloqueos/editar/${block.id}`)
                            }
                          >
                            <img
                              src={editIcon}
                              alt="Editar"
                              style={{ width: '20px', height: '20px' }}
                            />
                          </button>
                        </td>
                        <td style={styles.td}>
                          <div
                            style={{
                              ...styles.toggleSwitch,
                              opacity: togglingId === block.id ? 0.5 : 1,
                              pointerEvents: togglingId === block.id ? 'none' : 'auto',
                            }}
                            role="button"
                            tabIndex={0}
                            onClick={() => handleToggleStatus(block)}
                            onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); handleToggleStatus(block); } }}
                            title={
                              block.status === 1 ? 'Desactivar bloqueo' : 'Activar bloqueo'
                            }
                          >
                            <span
                              style={{
                                ...styles.toggleSlider,
                                ...(block.status === 1 ? styles.toggleSliderActive : {}),
                              }}
                            >
                              <span
                                style={{
                                  ...styles.toggleKnob,
                                  ...(block.status === 1 ? styles.toggleKnobActive : {}),
                                }}
                              />
                            </span>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <Pagination
                currentPage={safePage}
                totalPages={totalPages}
                totalItems={filteredBlocks.length}
                pageSize={pageSize}
                onPageChange={setCurrentPage}
                onPageSizeChange={(newSize) => {
                  setPageSize(newSize);
                  setCurrentPage(1);
                }}
              />
            </>
            );
          })()}
        </section>
      </main>
      {ModalNode}
    </div>
  );
};

export default SupplierBlocksContainer;
