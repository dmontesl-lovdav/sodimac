import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { supplierService, Supplier, SupplierType } from '@features/catalogos/services/catalogosApi';
import { exportToCSV, exportToExcel, ExportColumn } from '@features/catalogos/utils/export';
import { useModalNotification } from '@shared/components/ui/modal';
import { extractApiErrorMessage } from '@shared/utils/errorMessage';
import { APP_EVENT, PermissionGate } from '@shared/security';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import { withFinanceBreadcrumb } from '@shared/components/ui/navigation/financeBreadcrumb';
import { Pagination } from '@shared/components/ui/pagination';
import editIcon from '@features/catalogos/assets/edit.svg';
import deleteIcon from '@features/catalogos/assets/delete.svg';

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
  resultsLeft: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.75rem',
    flexWrap: 'wrap' as const,
  },
  resultsCount: {
    fontSize: '0.875rem',
    fontWeight: 600,
    color: '#1f2937',
  },
  linkBtn: {
    padding: '0.4rem 0.8rem',
    backgroundColor: 'transparent',
    color: '#003865',
    border: '1px solid #003865',
    borderRadius: '0.375rem',
    fontSize: '0.8125rem',
    fontWeight: 500,
    cursor: 'pointer',
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

const SuppliersIcon = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
    <circle cx="9" cy="7" r="4" />
    <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
    <path d="M16 3.13a4 4 0 0 1 0 7.75" />
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

const SuppliersContainer = () => {
  const navigate = useNavigate();
  const { showError, showConfirm, ModalNode } = useModalNotification();
  const [filteredSuppliers, setFilteredSuppliers] = useState<Supplier[]>([]);
  const [supplierTypes, setSupplierTypes] = useState<SupplierType[]>([]);
  const [loading, setLoading] = useState(false);
  const [togglingId, setTogglingId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [hasSearched, setHasSearched] = useState(false);

  const [filters, setFilters] = useState({
    supplierNumber: '',
    businessName: '',
    rfc: '',
    typeId: '',
    status: '',
  });

  useEffect(() => {
    loadTypes();
  }, []);

  const loadTypes = async () => {
    try {
      const typesData = await supplierService.getTypes();
      setSupplierTypes(typesData);
    } catch (err) {
      console.error('Error al cargar tipos de proveedor:', err);
    }
  };

  const applyFilters = (data: Supplier[]): Supplier[] => {
    let result = [...data];
    if (filters.supplierNumber) {
      result = result.filter((s) =>
        s.supplierNumber.toLowerCase().includes(filters.supplierNumber.toLowerCase()),
      );
    }
    if (filters.businessName) {
      result = result.filter((s) =>
        s.businessName.toLowerCase().includes(filters.businessName.toLowerCase()),
      );
    }
    if (filters.rfc) {
      result = result.filter((s) => s.rfc.toLowerCase().includes(filters.rfc.toLowerCase()));
    }
    if (filters.typeId) {
      result = result.filter((s) => s.supplierType?.id === parseInt(filters.typeId));
    }
    if (filters.status) {
      result = result.filter((s) => s.status === parseInt(filters.status));
    }
    return result;
  };

  const loadData = async (): Promise<Supplier[]> => {
    setLoading(true);
    setError(null);
    try {
      const suppliersData = await supplierService.getAll();
      return suppliersData;
    } catch (err) {
      console.error('Error al cargar proveedores:', err);
      setError(
        extractApiErrorMessage(err, {
          fallback: 'No fue posible cargar la información de proveedores. Inténtalo más tarde.',
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
    setFilteredSuppliers(applyFilters(data));
    setCurrentPage(1);
  };

  const handleClearFilters = () => {
    setFilters({ supplierNumber: '', businessName: '', rfc: '', typeId: '', status: '' });
    setFilteredSuppliers([]);
    setHasSearched(false);
    setCurrentPage(1);
  };

  const handleExport = (format: 'csv' | 'excel') => {
    const columns: ExportColumn[] = [
      { key: 'id', label: 'ID' },
      { key: 'supplierNumber', label: 'Número Proveedor' },
      { key: 'rfc', label: 'RFC' },
      { key: 'businessName', label: 'Razón Social' },
      { key: 'supplierTypeName', label: 'Tipo' },
      { key: 'paymentConditionName', label: 'Condición de Pago' },
      { key: 'statusText', label: 'Estatus' },
    ];

    const dataToExport = filteredSuppliers.map((s) => ({
      id: s.id,
      supplierNumber: s.supplierNumber,
      rfc: s.rfc,
      businessName: s.businessName,
      supplierTypeName: s.supplierType?.description || '',
      paymentConditionName: s.paymentCondition?.conditionName || '',
      statusText: s.status === 1 ? 'Activo' : 'Inactivo',
    }));

    const filename = `proveedores_${new Date().toISOString().split('T')[0]}`;

    if (format === 'csv') {
      exportToCSV(dataToExport, columns, filename);
    } else {
      exportToExcel(dataToExport, columns, filename);
    }
  };

  const selectOne = (id: number) => {
    setSelectedIds([...selectedIds, id]);
  };

  const unselectOne = (id: number) => {
    setSelectedIds(selectedIds.filter((i) => i !== id));
  };

  const handleToggleStatus = async (supplier: Supplier) => {
    if (togglingId) return;
    setTogglingId(supplier.id);
    try {
      const newStatus = supplier.status === 1 ? 0 : 1;
      await supplierService.update(supplier.id, { status: newStatus });
      const refreshed = await loadData();
      setFilteredSuppliers(applyFilters(refreshed));
    } catch (err) {
      console.error('Error al cambiar estatus de proveedor:', err);
      showError(
        extractApiErrorMessage(err, {
          fallback: 'No fue posible cambiar el estatus del proveedor. Inténtalo nuevamente.',
        }),
        'No se pudo cambiar el estatus',
      );
    } finally {
      setTogglingId(null);
    }
  };

  const askDelete = (supplier: Supplier) => {
    showConfirm({
      title: 'Eliminar proveedor',
      message: `Esta acción es permanente. ¿Deseas eliminar a "${supplier.businessName}"?`,
      severity: 'warning',
      confirmText: 'Eliminar',
      cancelText: 'Cancelar',
      onConfirm: async () => {
        try {
          await supplierService.delete(supplier.id);
          const refreshed = await loadData();
          setFilteredSuppliers(applyFilters(refreshed));
        } catch (err) {
          console.error('Error al eliminar proveedor:', err);
          showError(
            extractApiErrorMessage(err, {
              fallback: 'No fue posible eliminar el proveedor. Inténtalo nuevamente.',
            }),
            'No se pudo eliminar',
          );
        }
      },
    });
  };

  const totalPages = Math.max(1, Math.ceil(filteredSuppliers.length / pageSize));
  const safePage = Math.min(currentPage, totalPages);
  const paginatedSuppliers = filteredSuppliers.slice(
    (safePage - 1) * pageSize,
    safePage * pageSize,
  );

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <Breadcrumb
          items={withFinanceBreadcrumb([
            { label: 'Gestión de Catálogos', to: '/util/catalogos' },
            { label: 'Proveedores' },
          ])}
        />
      </div>

      <main style={styles.main}>
        <section style={styles.card}>
          <div style={styles.headerContent}>
            <div>
              <div style={styles.titleRow}>
                <div style={styles.titleIcon}>
                  <SuppliersIcon />
                </div>
                <h1 style={styles.title}>Administración de Proveedores</h1>
              </div>
              <p style={styles.description}>
                Busca, gestiona y consulta el catálogo de proveedores. Administra su información,
                tipos, condiciones de pago y estatus.
              </p>
            </div>
            <div style={styles.headerActions}>
              <PermissionGate appEvent={APP_EVENT.SUPPLIERS_CATALOG.DOWNLOAD_CSV}>
                <button
                  style={{
                    ...styles.secondaryBtn,
                    opacity: filteredSuppliers.length === 0 ? 0.5 : 1,
                    cursor: filteredSuppliers.length === 0 ? 'not-allowed' : 'pointer',
                  }}
                  onClick={() => handleExport('csv')}
                  disabled={filteredSuppliers.length === 0}
                  title="Exportar a CSV"
                >
                  <ExportFileIcon />
                  Exportar CSV
                </button>
                <button
                  style={{
                    ...styles.secondaryBtn,
                    opacity: filteredSuppliers.length === 0 ? 0.5 : 1,
                    cursor: filteredSuppliers.length === 0 ? 'not-allowed' : 'pointer',
                  }}
                  onClick={() => handleExport('excel')}
                  disabled={filteredSuppliers.length === 0}
                  title="Exportar a Excel"
                >
                  <ExportFileIcon />
                  Exportar Excel
                </button>
              </PermissionGate>
              <button
                style={styles.primaryBtn}
                onClick={() => navigate('/util/catalogos/proveedores/crear')}
              >
                <span style={{ fontSize: '1.25rem', lineHeight: 1 }}>⊕</span>
                {' '}
                Nuevo Proveedor
              </button>
            </div>
          </div>

          {error && <div style={styles.error}>{error}</div>}

          <div style={styles.filtersRow}>
            <div style={styles.filterGroup}>
              <label htmlFor="filter-supplier-number" style={styles.filterLabel}>Número Proveedor</label>
              <input
                id="filter-supplier-number"
                type="text"
                style={styles.filterInput}
                value={filters.supplierNumber}
                onChange={(e) => setFilters({ ...filters, supplierNumber: e.target.value })}
                placeholder="Buscar..."
              />
            </div>
            <div style={styles.filterGroup}>
              <label htmlFor="filter-supplier-business-name" style={styles.filterLabel}>Razón Social</label>
              <input
                id="filter-supplier-business-name"
                type="text"
                style={styles.filterInput}
                value={filters.businessName}
                onChange={(e) => setFilters({ ...filters, businessName: e.target.value })}
                placeholder="Buscar..."
              />
            </div>
            <div style={styles.filterGroup}>
              <label htmlFor="filter-supplier-rfc" style={styles.filterLabel}>RFC</label>
              <input
                id="filter-supplier-rfc"
                type="text"
                style={styles.filterInput}
                value={filters.rfc}
                onChange={(e) => setFilters({ ...filters, rfc: e.target.value })}
                placeholder="Buscar..."
              />
            </div>
            <div style={styles.filterGroup}>
              <label htmlFor="filter-supplier-type" style={styles.filterLabel}>Tipo</label>
              <select
                id="filter-supplier-type"
                style={styles.filterSelect}
                value={filters.typeId}
                onChange={(e) => setFilters({ ...filters, typeId: e.target.value })}
              >
                <option value="">Todos</option>
                {supplierTypes.map((type) => (
                  <option key={type.id} value={type.id}>
                    {type.description}
                  </option>
                ))}
              </select>
            </div>
            <div style={styles.filterGroup}>
              <label htmlFor="filter-supplier-status" style={styles.filterLabel}>Estatus</label>
              <select
                id="filter-supplier-status"
                style={styles.filterSelect}
                value={filters.status}
                onChange={(e) => setFilters({ ...filters, status: e.target.value })}
              >
                <option value="">Todos</option>
                <option value="1">Activo</option>
                <option value="0">Inactivo</option>
              </select>
            </div>
            <PermissionGate appEvent={APP_EVENT.SUPPLIERS_CATALOG.SEARCH}>
              <button style={styles.outlineBtn} onClick={handleSearch}>
                Buscar
              </button>
            </PermissionGate>
            <PermissionGate appEvent={APP_EVENT.SUPPLIERS_CATALOG.CLEAR_FILTERS}>
              <button style={styles.outlineBtn} onClick={handleClearFilters}>
                Limpiar
              </button>
            </PermissionGate>
          </div>

          <div style={styles.resultsHeader}>
            <div style={styles.resultsLeft}>
              <span style={styles.resultsCount}>
                {hasSearched ? `${filteredSuppliers.length} Proveedores encontrados` : '0 Proveedores encontrados'}
              </span>
              <button
                style={{
                  ...styles.linkBtn,
                  opacity: selectedIds.length === 1 ? 1 : 0.55,
                  cursor: selectedIds.length === 1 ? 'pointer' : 'not-allowed',
                }}
                disabled={selectedIds.length !== 1}
                onClick={() => {
                  const supplier = filteredSuppliers.find((s) => s.id === selectedIds[0]);
                  if (supplier) {
                    navigate(`/util/catalogos/proveedores/${supplier.id}/bloquear`);
                  }
                }}
              >
                Gestionar Bloqueo
              </button>
            </div>
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
                  Aplica los filtros y presiona Buscar para consultar los proveedores.
                </div>
              );
            }
            if (filteredSuppliers.length === 0) {
              return (
                <div style={styles.noResults}>
                  No se encontraron proveedores con los criterios de búsqueda ingresados.
                </div>
              );
            }
            return (
            <>
              <div style={styles.tableWrapper}>
                <table style={styles.table}>
                  <thead>
                    <tr>
                      <th style={{ ...styles.th, width: '32px' }}></th>
                      <th style={styles.th}>ID</th>
                      <th style={styles.th}>Número</th>
                      <th style={styles.th}>RFC</th>
                      <th style={styles.th}>Razón Social</th>
                      <th style={styles.th}>Tipo</th>
                      <th style={styles.th}>Condición Pago</th>
                      <th style={styles.th}>Estatus</th>
                      <th style={styles.th}>Editar</th>
                      <th style={styles.th}>Act./Desact.</th>
                      <th style={styles.th}>Eliminar</th>
                    </tr>
                  </thead>
                  <tbody>
                    {paginatedSuppliers.map((supplier) => (
                      <tr key={supplier.id}>
                        <td style={styles.td}>
                          <input
                            type="checkbox"
                            checked={selectedIds.includes(supplier.id)}
                            onChange={(e) => (e.target.checked ? selectOne(supplier.id) : unselectOne(supplier.id))}
                          />
                        </td>
                        <td style={styles.td}>{supplier.id}</td>
                        <td style={styles.td}>{supplier.supplierNumber}</td>
                        <td style={styles.td}>{supplier.rfc}</td>
                        <td style={styles.td}>{supplier.businessName}</td>
                        <td style={styles.td}>{supplier.supplierType?.description || '-'}</td>
                        <td style={styles.td}>
                          {supplier.paymentCondition?.conditionName || '-'}
                        </td>
                        <td style={styles.td}>
                          <span
                            style={{
                              ...styles.statusBadge,
                              ...(supplier.status === 1
                                ? styles.statusActive
                                : styles.statusInactive),
                            }}
                          >
                            {supplier.status === 1 ? 'Activo' : 'Inactivo'}
                          </span>
                        </td>
                        <td style={styles.td}>
                          <button
                            style={styles.actionBtn}
                            title="Editar"
                            onClick={() =>
                              navigate(`/util/catalogos/proveedores/editar/${supplier.id}`)
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
                          <button
                            type="button"
                            style={{
                              ...styles.toggleSwitch,
                              opacity: togglingId === supplier.id ? 0.5 : 1,
                              pointerEvents: togglingId === supplier.id ? 'none' : 'auto',
                              border: 'none',
                              padding: 0,
                              appearance: 'none',
                              background: 'transparent',
                            }}
                            onClick={() => handleToggleStatus(supplier)}
                            title={
                              supplier.status === 1
                                ? 'Desactivar proveedor'
                                : 'Activar proveedor'
                            }
                          >
                            <span
                              style={{
                                ...styles.toggleSlider,
                                ...(supplier.status === 1 ? styles.toggleSliderActive : {}),
                              }}
                            >
                              <span
                                style={{
                                  ...styles.toggleKnob,
                                  ...(supplier.status === 1 ? styles.toggleKnobActive : {}),
                                }}
                              />
                            </span>
                          </button>
                        </td>
                        <td style={styles.td}>
                          <button
                            style={styles.actionBtn}
                            title="Eliminar"
                            onClick={() => askDelete(supplier)}
                          >
                            <img
                              src={deleteIcon}
                              alt="Eliminar"
                              style={{ width: '20px', height: '20px' }}
                            />
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <Pagination
                currentPage={safePage}
                totalPages={totalPages}
                totalItems={filteredSuppliers.length}
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

export default SuppliersContainer;
