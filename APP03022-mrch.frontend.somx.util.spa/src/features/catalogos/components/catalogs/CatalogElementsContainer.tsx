import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { exportToCSV, exportToExcel } from '@features/catalogos/utils/export';
import { catalogService, catalogElementService, CatalogResponse, CatalogElementSearchParams, CatalogSimple } from '@features/catalogos/services/catalogosApi';
import { useModalNotification } from '@shared/components/ui/modal';
import { Pagination } from '@shared/components/ui/pagination';
import { extractApiErrorMessage } from '@shared/utils/errorMessage';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import { withFinanceBreadcrumb } from '@shared/components/ui/navigation/financeBreadcrumb';

interface CatalogData {
  id: string;
  name: string;
  description: string;
  type: 'Primario' | 'Secundario';
  status: 'Activo' | 'Inactivo';
  createdBy: string;
  createdAt: string;
  updatedBy: string | null;
  updatedAt: string | null;
}

interface CatalogElement {
  id: string;
  key: string;
  element: string;
  value: string;
  startDate: string;
  endDate: string;
  status: 'Activo' | 'Inactivo';
  parentCatalogId: string | null;
  parentCatalogName: string | null;
  parentElementId: string | null;
  parentElementName: string | null;
  createdBy: string;
  createdAt: string;
  updatedBy: string | null;
  updatedAt: string | null;
}

interface ParentCatalog {
  id: string;
  name: string;
}

const styles = {
  container: {
    minHeight: '100vh',
    backgroundColor: '#ffffff',
    padding: '1.5rem 2rem',
  },
  breadcrumb: {
    fontSize: '0.875rem',
    color: '#64748b',
    marginBottom: '1.5rem',
  },
  breadcrumbLink: {
    color: '#0066CC',
    textDecoration: 'none',
    cursor: 'pointer',
  },
  card: {
    backgroundColor: '#ffffff',
    border: '1px solid #e5e7eb',
    borderRadius: '0.75rem',
    padding: '2rem',
  },
  header: {
    display: 'flex',
    alignItems: 'center',
    gap: '1rem',
    marginBottom: '1.5rem',
  },
  headerIcon: {
    width: '48px',
    height: '48px',
    backgroundColor: '#EAF3FB',
    borderRadius: '0.5rem',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: '#003865',
  },
  title: {
    fontSize: '1.5rem',
    fontWeight: 600,
    color: '#1e293b',
    margin: 0,
  },
  catalogCard: {
    backgroundColor: '#f8fafc',
    border: '1px solid #e5e7eb',
    borderRadius: '0.5rem',
    padding: '1rem 1.5rem',
    marginBottom: '1.5rem',
  },
  catalogCardTitle: {
    fontSize: '0.875rem',
    fontWeight: 600,
    color: '#1e293b',
    marginBottom: '0.75rem',
  },
  catalogCardGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
    gap: '1rem',
  },
  catalogCardField: {
    display: 'flex',
    flexDirection: 'column' as const,
  },
  catalogCardLabel: {
    fontSize: '0.75rem',
    color: '#64748b',
    marginBottom: '0.25rem',
  },
  catalogCardValue: {
    fontSize: '0.875rem',
    color: '#1e293b',
    fontWeight: 500,
  },
  statusBadge: {
    display: 'inline-flex',
    alignItems: 'center',
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
    backgroundColor: '#fee2e2',
    color: '#991b1b',
  },
  sectionDescription: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '1.5rem',
    flexWrap: 'wrap' as const,
    gap: '1rem',
  },
  descriptionText: {
    fontSize: '0.875rem',
    color: '#64748b',
    maxWidth: '500px',
  },
  buttonsGroup: {
    display: 'flex',
    gap: '0.75rem',
  },
  outlineBtn: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.5rem',
    padding: '0.5rem 1rem',
    fontSize: '0.875rem',
    fontWeight: 500,
    color: '#0066CC',
    backgroundColor: 'transparent',
    border: '1px solid #0066CC',
    borderRadius: '0.375rem',
    cursor: 'pointer',
    transition: 'all 0.2s ease',
  },
  primaryBtn: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.5rem',
    padding: '0.5rem 1rem',
    fontSize: '0.875rem',
    fontWeight: 500,
    color: '#ffffff',
    backgroundColor: '#0066CC',
    border: '1px solid #0066CC',
    borderRadius: '0.375rem',
    cursor: 'pointer',
    transition: 'all 0.2s ease',
  },
  ghostBtn: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.5rem',
    padding: '0.5rem 1rem',
    fontSize: '0.875rem',
    fontWeight: 500,
    color: '#64748b',
    backgroundColor: 'transparent',
    border: 'none',
    cursor: 'pointer',
    textDecoration: 'underline',
  },
  filtersContainer: {
    display: 'flex',
    flexWrap: 'wrap' as const,
    gap: '1rem',
    marginBottom: '1.5rem',
    padding: '1rem',
    backgroundColor: '#f8fafc',
    borderRadius: '0.5rem',
    border: '1px solid #e5e7eb',
    alignItems: 'flex-end',
  },
  filterField: {
    display: 'flex',
    flexDirection: 'column' as const,
    minWidth: '140px',
    flex: 1,
  },
  filterLabel: {
    fontSize: '0.75rem',
    color: '#64748b',
    marginBottom: '0.25rem',
  },
  filterInput: {
    padding: '0.5rem 0.75rem',
    fontSize: '0.875rem',
    border: '1px solid #d1d5db',
    borderRadius: '0.375rem',
    backgroundColor: '#ffffff',
    width: '100%',
  },
  filterSelect: {
    padding: '0.5rem 0.75rem',
    fontSize: '0.875rem',
    border: '1px solid #d1d5db',
    borderRadius: '0.375rem',
    backgroundColor: '#ffffff',
    cursor: 'pointer',
    width: '100%',
  },
  filterButtons: {
    display: 'flex',
    gap: '0.5rem',
    alignItems: 'center',
  },
  resultsHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '1rem',
    flexWrap: 'wrap' as const,
    gap: '1rem',
  },
  resultsCount: {
    fontSize: '0.875rem',
    fontWeight: 500,
    color: '#1e293b',
  },
  selectAllLabel: {
    fontSize: '0.75rem',
    color: '#64748b',
  },
  tableWrapper: {
    overflowX: 'auto' as const,
    border: '1px solid #e5e7eb',
    borderRadius: '0.5rem',
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
    borderBottom: '1px solid #e5e7eb',
    whiteSpace: 'normal' as const,
    lineHeight: 1.25,
    fontSize: '0.7rem',
    verticalAlign: 'middle' as const,
  },
  td: {
    padding: '0.5rem 0.5rem',
    borderBottom: '1px solid #e5e7eb',
    color: '#1e293b',
    fontSize: '0.75rem',
    verticalAlign: 'middle' as const,
    whiteSpace: 'normal' as const,
    wordBreak: 'break-word' as const,
  },
  actionBtn: {
    padding: '0.25rem 0.5rem',
    fontSize: '0.75rem',
    color: '#64748b',
    backgroundColor: 'transparent',
    border: 'none',
    cursor: 'pointer',
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  toggleSwitch: {
    position: 'relative' as const,
    display: 'inline-block',
    width: '40px',
    height: '20px',
  },
  toggleSlider: {
    position: 'absolute' as const,
    cursor: 'pointer',
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
  toggleSliderBefore: {
    position: 'absolute' as const,
    content: '""',
    height: '16px',
    width: '16px',
    left: '2px',
    bottom: '2px',
    backgroundColor: 'white',
    transition: '0.3s',
    borderRadius: '50%',
  },
  toggleSliderBeforeActive: {
    transform: 'translateX(20px)',
  },
  emptyState: {
    padding: '3rem',
    textAlign: 'center' as const,
    color: '#64748b',
    backgroundColor: '#f8fafc',
    borderRadius: '0.5rem',
    border: '1px solid #e5e7eb',
  },
  pagination: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    gap: '0.5rem',
    marginTop: '1rem',
    flexWrap: 'wrap' as const,
  },
  paginationInfo: {
    fontSize: '0.875rem',
    color: '#64748b',
    marginRight: '1rem',
  },
  paginationBtn: {
    padding: '0.375rem 0.75rem',
    fontSize: '0.875rem',
    border: '1px solid #d1d5db',
    borderRadius: '0.375rem',
    backgroundColor: '#ffffff',
    cursor: 'pointer',
    color: '#374151',
  },
  paginationBtnActive: {
    backgroundColor: '#0066CC',
    color: '#ffffff',
    borderColor: '#0066CC',
  },
  paginationBtnDisabled: {
    opacity: 0.5,
    cursor: 'not-allowed',
  },
  footer: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: '1.5rem',
    flexWrap: 'wrap' as const,
    gap: '1rem',
  },
  exportDropdown: {
    position: 'relative' as const,
    display: 'inline-block',
  },
  exportMenu: {
    position: 'absolute' as const,
    right: 0,
    bottom: '100%',
    backgroundColor: '#ffffff',
    boxShadow: '0 -4px 6px rgba(0, 0, 0, 0.1)',
    borderRadius: '0.375rem',
    zIndex: 10,
    minWidth: '150px',
    marginBottom: '0.5rem',
  },
  exportMenuItem: {
    padding: '0.75rem 1rem',
    cursor: 'pointer',
    fontSize: '0.875rem',
    color: '#374151',
    borderBottom: '1px solid #e5e7eb',
  },
  exportMenuItemLast: {
    borderBottom: 'none',
  },
};

const ViewIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
    <circle cx="12" cy="12" r="3" />
  </svg>
);

const EditIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" />
    <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z" />
  </svg>
);

const ElementsIcon = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
    <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
    <path d="M9 12h6M9 16h6" />
  </svg>
);

const toDdMmYyyy = (iso: string | null | undefined): string => {
  if (!iso) return '';
  const datePart = iso.split('T')[0];
  if (!datePart) return '';
  return datePart.split('-').reverse().join('-');
};

const mapApiElementToLocal = (apiElement: any): CatalogElement => ({
  id: String(apiElement.id).padStart(4, '0'),
  key: apiElement.key || apiElement.element || '',
  element: apiElement.element || apiElement.nombre || '',
  value: apiElement.value || apiElement.valor || '',
  startDate: toDdMmYyyy(apiElement.validFrom),
  endDate: toDdMmYyyy(apiElement.validTo),
  status: apiElement.status === 1 ? 'Activo' : 'Inactivo',
  parentCatalogId: apiElement.parentCatalogId ? String(apiElement.parentCatalogId) : null,
  parentCatalogName: apiElement.parentCatalogName || null,
  parentElementId: apiElement.parentElementId ? String(apiElement.parentElementId) : null,
  parentElementName: apiElement.parentElementName || null,
  createdBy: apiElement.createdBy || 'system',
  createdAt: toDdMmYyyy(apiElement.createdAt),
  updatedBy: apiElement.updatedBy || null,
  updatedAt: apiElement.updatedAt ? toDdMmYyyy(apiElement.updatedAt) : null,
});

const buildSearchParams = (
  filters: {
    idElement: string;
    key: string;
    element: string;
    value: string;
    parentCatalogId: string;
    parentElementId: string;
    status: string;
  },
  page: number,
  pageSize: number,
): CatalogElementSearchParams => {
  const searchParams: CatalogElementSearchParams = { page, pageSize };
  if (filters.idElement) searchParams.idElemento = parseInt(filters.idElement);
  if (filters.key) searchParams.clave = filters.key;
  if (filters.element) searchParams.elemento = filters.element;
  if (filters.value) searchParams.valor = filters.value;
  if (filters.parentCatalogId) searchParams.idCatalogoPadre = parseInt(filters.parentCatalogId);
  if (filters.parentElementId) searchParams.idElementoPadre = parseInt(filters.parentElementId);
  if (filters.status) searchParams.estatus = filters.status === 'Activo' ? 1 : 0;
  return searchParams;
};

const mapCatalogResponse = (response: CatalogResponse): CatalogData => ({
  id: String(response.id).padStart(4, '0'),
  name: response.name,
  description: response.description || '',
  type: response.catalogType === 'PRIMARIO' ? 'Primario' : 'Secundario',
  status: response.status === 1 ? 'Activo' : 'Inactivo',
  createdBy: response.createdBy || 'system',
  createdAt: response.createdAt ? response.createdAt.split('T')[0] ?? '' : '',
  updatedBy: response.updatedBy || null,
  updatedAt: response.updatedAt ? response.updatedAt.split('T')[0] ?? null : null,
});

const EMPTY_FILTERS = {
  idElement: '',
  key: '',
  element: '',
  value: '',
  parentCatalogId: '',
  parentElementId: '',
  status: '',
};

function ParentDisplay({ isPrimario, value }: Readonly<{ isPrimario: boolean; value: string | null | undefined }>) {
  if (isPrimario) return <span style={{ color: '#CCCCCC' }}>(No aplica)</span>;
  if (value) return <span>{value}</span>;
  return <span style={{ color: '#999999' }}>(Sin relación)</span>;
}

export default function CatalogElementsContainer() {
  const navigate = useNavigate();
  const { showError, ModalNode } = useModalNotification();
  const { id } = useParams<{ id: string }>();

  const [catalog, setCatalog] = useState<CatalogData | null>(null);
  const [filteredElements, setFilteredElements] = useState<CatalogElement[]>([]);
  const [showResults, setShowResults] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSearching, setIsSearching] = useState(false);

  const [filters, setFilters] = useState({ ...EMPTY_FILTERS });

  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(10);
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [togglingId, setTogglingId] = useState<string | null>(null);
  const [isExporting, setIsExporting] = useState(false);
  const [exportError, setExportError] = useState<string | null>(null);
  const [parentCatalogs, setParentCatalogs] = useState<ParentCatalog[]>([]);
  const [filterParentElements, setFilterParentElements] = useState<Array<{ id: string; name: string }>>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    const loadParentCatalogs = async () => {
      try {
        const primaryCatalogs = await catalogService.getPrimaryCatalogs();
        setParentCatalogs(primaryCatalogs.map((c: CatalogSimple) => ({
          id: String(c.id),
          name: c.name,
        })));
      } catch (e) {
        console.warn('Could not load parent catalogs:', e);
      }
    };

    const fetchCatalog = async () => {
      if (!id) return;
      setIsLoading(true);
      try {
        const response: CatalogResponse = await catalogService.getById(parseInt(id));
        setCatalog(mapCatalogResponse(response));
        await loadParentCatalogs();
      } catch (error) {
        console.error('Error fetching catalog:', error);
        setCatalog(null);
      } finally {
        setIsLoading(false);
      }
    };

    fetchCatalog();
  }, [id]);

  const handleFilterChange = (field: string, value: string) => {
    if (field === 'parentCatalogId') {
      setFilters((prev) => ({ ...prev, parentCatalogId: value, parentElementId: '' }));
      if (value) {
        catalogElementService.getActiveElements(parseInt(value))
          .then((elems) => setFilterParentElements(elems.map((e: any) => ({ id: String(e.id), name: e.element || e.value || '' }))))
          .catch(() => setFilterParentElements([]));
      } else {
        setFilterParentElements([]);
      }
      return;
    }
    setFilters((prev) => ({ ...prev, [field]: value }));
  };

  const performSearch = useCallback(async (page: number = 1, overridePageSize?: number) => {
    if (!id) return;

    setIsSearching(true);
    try {
      const effectivePageSize = overridePageSize ?? itemsPerPage;
      const searchParams = buildSearchParams(filters, page, effectivePageSize);

      const response = await catalogElementService.search(parseInt(id), searchParams);
      
      const mappedElements = response.items.map(mapApiElementToLocal);
      setFilteredElements(mappedElements);
      setTotalElements(response.total);
      setTotalPages(response.totalPages);
      setShowResults(true);
      setCurrentPage(page);
    } catch (error) {
      console.error('Error searching elements:', error);
      setFilteredElements([]);
      setTotalElements(0);
      setTotalPages(0);
    } finally {
      setIsSearching(false);
    }
  }, [id, filters, itemsPerPage]);

  const handleSearch = () => {
    setSelectedIds(new Set());
    performSearch(1);
  };

  const handlePageChange = (newPage: number) => {
    if (newPage >= 1 && newPage <= totalPages && newPage !== currentPage) {
      performSearch(newPage);
    }
  };

  const handleClear = () => {
    setFilters({ ...EMPTY_FILTERS });
    setFilterParentElements([]);
    setFilteredElements([]);
    setShowResults(false);
    setSelectedIds(new Set());
    setCurrentPage(1);
    setTotalElements(0);
    setTotalPages(0);
  };

  const handleToggleStatus = async (elementId: string) => {
    setTogglingId(elementId);

    try {
      const currentElement = filteredElements.find(el => el.id === elementId);
      if (!currentElement) return;

      const newStatus = currentElement.status === 'Activo' ? 0 : 1;

      await catalogElementService.changeStatus(parseInt(elementId), newStatus, 'system');

      const newStatusText = newStatus === 1 ? 'Activo' : 'Inactivo';
      setFilteredElements((prev) =>
        prev.map((el) =>
          el.id === elementId
            ? { ...el, status: newStatusText }
            : el
        )
      );
    } catch (error) {
      console.error('Error changing status:', error);
      showError(
        extractApiErrorMessage(error, {
          fallback: 'No fue posible cambiar el estatus del elemento. Inténtalo nuevamente.',
        }),
        'No se pudo cambiar el estatus',
      );
    } finally {
      setTogglingId(null);
    }
  };

  const paginatedElements = filteredElements;

  const handleSelectAll = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.checked) {
      const newSelected = new Set(selectedIds);
      paginatedElements.forEach((el) => newSelected.add(el.id));
      setSelectedIds(newSelected);
    } else {
      const newSelected = new Set(selectedIds);
      paginatedElements.forEach((el) => newSelected.delete(el.id));
      setSelectedIds(newSelected);
    }
  };

  const handleSelectRow = (id: string, checked: boolean) => {
    const newSelected = new Set(selectedIds);
    if (checked) {
      newSelected.add(id);
    } else {
      newSelected.delete(id);
    }
    setSelectedIds(newSelected);
  };

  const isAllOnPageSelected =
    paginatedElements.length > 0 &&
    paginatedElements.every((el) => selectedIds.has(el.id));

  const handleExport = async (format: 'xlsx' | 'csv') => {
    if (isExporting || !id) return;

    setIsExporting(true);
    setExportError(null);

    try {
      const searchParams = buildSearchParams(filters, 1, totalElements || 1000);
      const response = await catalogElementService.search(parseInt(id), searchParams);
      const allElements = response.items.map(mapApiElementToLocal);

      const dataToExport = selectedIds.size > 0
        ? allElements.filter((el) => selectedIds.has(el.id))
        : allElements;

      const columns = [
        { key: 'id', label: 'ID Elemento' },
        { key: 'key', label: 'Clave Elemento' },
        { key: 'element', label: 'Elemento' },
        { key: 'value', label: 'Valor' },
        { key: 'startDate', label: 'Fecha Inicio Vigencia' },
        { key: 'endDate', label: 'Fecha Fin Vigencia' },
        { key: 'status', label: 'Estatus' },
        { key: 'parentCatalogName', label: 'Catálogo Padre Relacionado' },
        { key: 'parentElementName', label: 'Elemento Padre Relacionado' },
        { key: 'createdBy', label: 'ID Usuario Registro' },
        { key: 'createdAt', label: 'Fecha Registro' },
        { key: 'updatedBy', label: 'ID Usuario Actualización' },
        { key: 'updatedAt', label: 'Fecha Actualización' },
      ];

      const timestamp = new Date().toISOString().replace(/[-:T]/g, '').slice(0, 14);
      const filename = `elementos_${catalog?.name.replace(/\s/g, '_')}_${timestamp}`;

      const exportData = dataToExport.map(el => ({
        id: el.id,
        key: el.key,
        element: el.element,
        value: el.value || '',
        startDate: el.startDate || '',
        endDate: el.endDate || '',
        status: el.status,
        parentCatalogName: el.parentCatalogName ?? '',
        parentElementName: el.parentElementName ?? '',
        createdBy: el.createdBy || '',
        createdAt: el.createdAt || '',
        updatedBy: el.updatedBy ?? '',
        updatedAt: el.updatedAt ?? '',
      }));

      if (exportData.length === 0) {
        setExportError('No hay datos para exportar.');
        return;
      }

      if (format === 'xlsx') {
        exportToExcel(exportData, columns, filename);
      } else {
        exportToCSV(exportData, columns, filename);
      }
    } catch (error) {
      console.error('Export error:', error);
      setExportError('Ocurrió un problema al exportar. Intente nuevamente.');
    } finally {
      setIsExporting(false);
    }
  };

  if (isLoading) {
    return (
      <div style={styles.container}>
        <div style={{ padding: '3rem', textAlign: 'center' }}>
          <p style={{ color: '#64748b' }}>Cargando catálogo...</p>
        </div>
      </div>
    );
  }

  if (!catalog) {
    return (
      <div style={styles.container}>
        <div style={{ padding: '3rem', textAlign: 'center' }}>
          <p style={{ color: '#991b1b' }}>Catálogo no encontrado.</p>
          <button style={styles.primaryBtn} onClick={() => navigate('/util/catalogos/catalogs')}>
            Volver a Catálogos
          </button>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <Breadcrumb
        items={withFinanceBreadcrumb([
          { label: 'Gestión de Catálogos', to: '/util/catalogos' },
          { label: 'Catálogos', to: '/util/catalogos/catalogs' },
          { label: 'Elementos' },
        ])}
      />

      <div style={styles.card}>
        <div style={styles.header}>
          <div style={styles.headerIcon}>
            <ElementsIcon />
          </div>
          <h1 style={styles.title}>Elementos</h1>
        </div>

        <div style={styles.catalogCard}>
          <div style={styles.catalogCardTitle}>Información del Catálogo</div>
          <div style={styles.catalogCardGrid}>
            <div style={styles.catalogCardField}>
              <span style={styles.catalogCardLabel}>Nombre del Catálogo</span>
              <span style={styles.catalogCardValue}>{catalog.name}</span>
            </div>
            <div style={{ ...styles.catalogCardField, gridColumn: 'span 2' }}>
              <span style={styles.catalogCardLabel}>Descripción del Catálogo</span>
              <span style={styles.catalogCardValue}>{catalog.description}</span>
            </div>
            <div style={styles.catalogCardField}>
              <span style={styles.catalogCardLabel}>Tipo de Catálogo</span>
              <span style={styles.catalogCardValue}>{catalog.type}</span>
            </div>
            <div style={styles.catalogCardField}>
              <span style={styles.catalogCardLabel}>Estatus</span>
              <span
                style={{
                  ...styles.statusBadge,
                  ...(catalog.status === 'Activo' ? styles.statusActive : styles.statusInactive),
                }}
              >
                {catalog.status === 'Activo' ? 'Activado' : 'Desactivado'}
              </span>
            </div>
            <div style={styles.catalogCardField}>
              <span style={styles.catalogCardLabel}>Fecha Registro</span>
              <span style={styles.catalogCardValue}>{catalog.createdAt}</span>
            </div>
            <div style={styles.catalogCardField}>
              <span style={styles.catalogCardLabel}>ID Usuario Registro</span>
              <span style={styles.catalogCardValue}>{catalog.createdBy}</span>
            </div>
            <div style={styles.catalogCardField}>
              <span style={styles.catalogCardLabel}>Fecha Actualización</span>
              <span style={styles.catalogCardValue}>{catalog.updatedAt || '-'}</span>
            </div>
            <div style={styles.catalogCardField}>
              <span style={styles.catalogCardLabel}>ID Usuario Actualización</span>
              <span style={styles.catalogCardValue}>{catalog.updatedBy || '-'}</span>
            </div>
          </div>
        </div>

        <div style={styles.sectionDescription}>
          <p style={styles.descriptionText}>
            Busca y gestiona elementos del catálogo, sus valores y obtén información sobre cambios
            que se realicen a los mismos.
          </p>
          <div style={styles.buttonsGroup}>
            <button
              style={styles.outlineBtn}
              onClick={() => navigate(`/util/catalogos/catalogs/${id}/elementos/importar`)}
            >
              ⬇ Importar Elementos
            </button>
            <button
              style={styles.primaryBtn}
              onClick={() => navigate(`/util/catalogos/catalogs/${id}/elementos/nuevo`)}
            >
              ⊕ Nuevo Elemento
            </button>
          </div>
        </div>

        <div style={styles.filtersContainer}>
          <div style={styles.filterField}>
            <label htmlFor="filter-elem-id" style={styles.filterLabel}>ID Elemento</label>
            <input
              id="filter-elem-id"
              type="text"
              style={styles.filterInput}
              value={filters.idElement}
              onChange={(e) => handleFilterChange('idElement', e.target.value)}
              placeholder="ID Elemento"
            />
          </div>
          <div style={styles.filterField}>
            <label htmlFor="filter-elem-key" style={styles.filterLabel}>Clave del Elemento</label>
            <input
              id="filter-elem-key"
              type="text"
              style={styles.filterInput}
              value={filters.key}
              onChange={(e) => handleFilterChange('key', e.target.value.toUpperCase())}
              placeholder="Clave"
            />
          </div>
          <div style={styles.filterField}>
            <label htmlFor="filter-elem-element" style={styles.filterLabel}>Elemento</label>
            <input
              id="filter-elem-element"
              type="text"
              style={styles.filterInput}
              value={filters.element}
              onChange={(e) => handleFilterChange('element', e.target.value)}
              placeholder="Elemento"
            />
          </div>
          <div style={styles.filterField}>
            <label htmlFor="filter-elem-value" style={styles.filterLabel}>Valor Elemento</label>
            <input
              id="filter-elem-value"
              type="text"
              style={styles.filterInput}
              value={filters.value}
              onChange={(e) => handleFilterChange('value', e.target.value)}
              placeholder="Valor Elemento"
            />
          </div>
          <div style={styles.filterField}>
            <label htmlFor="filter-elem-parent-catalog" style={styles.filterLabel}>Catálogo Padre</label>
            <select
              id="filter-elem-parent-catalog"
              style={styles.filterSelect}
              value={filters.parentCatalogId}
              onChange={(e) => handleFilterChange('parentCatalogId', e.target.value)}
            >
              <option value="">Seleccionar...</option>
              {parentCatalogs.map((pc) => (
                <option key={pc.id} value={pc.id}>
                  {pc.name}
                </option>
              ))}
            </select>
          </div>
          <div style={styles.filterField}>
            <label htmlFor="filter-elem-parent-element" style={styles.filterLabel}>Elemento Padre</label>
            <select
              id="filter-elem-parent-element"
              style={{
                ...styles.filterSelect,
                ...(filters.parentCatalogId ? {} : { backgroundColor: '#f8fafc', color: '#94a3b8', cursor: 'not-allowed' }),
              }}
              value={filters.parentElementId}
              onChange={(e) => handleFilterChange('parentElementId', e.target.value)}
              disabled={!filters.parentCatalogId}
            >
              <option value="">{filters.parentCatalogId ? 'Seleccionar...' : 'Seleccione primero un catálogo padre'}</option>
              {filterParentElements.map((pe) => (
                <option key={pe.id} value={pe.id}>{pe.name}</option>
              ))}
            </select>
          </div>
          <div style={styles.filterField}>
            <label htmlFor="filter-elem-status" style={styles.filterLabel}>Estatus</label>
            <select
              id="filter-elem-status"
              style={styles.filterSelect}
              value={filters.status}
              onChange={(e) => handleFilterChange('status', e.target.value)}
            >
              <option value="">Todos</option>
              <option value="Activo">Activo</option>
              <option value="Inactivo">Inactivo</option>
            </select>
          </div>
          <div style={styles.filterButtons}>
            <button
              type="button"
              style={{ ...styles.ghostBtn, appearance: 'none' }}
              onClick={handleClear}
            >
              Limpiar
            </button>
            <button
              style={styles.outlineBtn}
              onClick={handleSearch}
              disabled={isSearching}
            >
              {isSearching ? 'Buscando...' : 'Buscar'}
            </button>
          </div>
        </div>

        {(() => {
          if (!showResults) {
            return (
              <div style={styles.emptyState}>
                Utiliza el filtro para realizar una búsqueda de elementos.
              </div>
            );
          }
          if (filteredElements.length === 0) {
            return (
              <div style={styles.emptyState}>
                No se encontraron elementos coincidentes con los criterios de búsqueda ingresados.
              </div>
            );
          }
          return (
          <>
            <div style={styles.resultsHeader}>
              <div>
                <span style={styles.resultsCount}>
                  {selectedIds.size > 0
                    ? `${selectedIds.size} seleccionado(s) de ${totalElements} Elementos encontrados`
                    : `${totalElements} Elementos encontrados`}
                </span>
                <div style={styles.selectAllLabel}>Seleccionar todo</div>
              </div>
            </div>

            {exportError && (
              <div
                style={{
                  backgroundColor: '#fee2e2',
                  color: '#991b1b',
                  padding: '0.75rem 1rem',
                  borderRadius: '0.375rem',
                  marginBottom: '1rem',
                  fontSize: '0.875rem',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                }}
              >
                <span>{exportError}</span>
                <button
                  style={{
                    background: 'none',
                    border: 'none',
                    color: '#991b1b',
                    cursor: 'pointer',
                    fontWeight: 600,
                  }}
                  onClick={() => setExportError(null)}
                >
                  ✕
                </button>
              </div>
            )}

            <div style={styles.tableWrapper}>
              <table style={styles.table}>
                <thead>
                  <tr>
                    <th style={{ ...styles.th, width: '32px', padding: '0.6rem 0.25rem' }}>
                      <input
                        type="checkbox"
                        checked={isAllOnPageSelected}
                        onChange={handleSelectAll}
                      />
                    </th>
                    <th style={styles.th}>ID</th>
                    <th style={styles.th}>Clave</th>
                    <th style={styles.th}>Elemento</th>
                    <th style={styles.th}>Valor</th>
                    <th style={styles.th}>F. Inicio</th>
                    <th style={styles.th}>F. Fin</th>
                    <th style={styles.th}>Estatus</th>
                    <th style={styles.th}>Cat. Padre Relacionado</th>
                    <th style={styles.th}>Elem. Padre Relacionado</th>
                    <th style={styles.th}>Usr. Reg.</th>
                    <th style={styles.th}>F. Reg.</th>
                    <th style={styles.th}>Usr. Act.</th>
                    <th style={styles.th}>F. Act.</th>
                    <th style={styles.th}>Conversión</th>
                    <th style={styles.th}>Editar</th>
                    <th style={styles.th}>Act./Desact.</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedElements.map((el) => (
                    <tr key={el.id}>
                      <td style={styles.td}>
                        <input
                          type="checkbox"
                          checked={selectedIds.has(el.id)}
                          onChange={(e) => handleSelectRow(el.id, e.target.checked)}
                        />
                      </td>
                      <td style={styles.td}>{el.id}</td>
                      <td style={styles.td}>{el.key}</td>
                      <td style={styles.td}>{el.element}</td>
                      <td style={styles.td}>{el.value || '-'}</td>
                      <td style={styles.td}>{el.startDate}</td>
                      <td style={styles.td}>{el.endDate}</td>
                      <td style={styles.td}>
                        <span
                          style={{
                            ...styles.statusBadge,
                            ...(el.status === 'Activo'
                              ? styles.statusActive
                              : styles.statusInactive),
                          }}
                        >
                          {el.status}
                        </span>
                      </td>
                      <td style={styles.td}>
                        <ParentDisplay isPrimario={catalog?.type === 'Primario'} value={el.parentCatalogName} />
                      </td>
                      <td style={styles.td}>
                        <ParentDisplay isPrimario={catalog?.type === 'Primario'} value={el.parentElementName} />
                      </td>
                      <td style={styles.td}>{el.createdBy}</td>
                      <td style={styles.td}>{el.createdAt}</td>
                      <td style={styles.td}>{el.updatedBy || '-'}</td>
                      <td style={styles.td}>{el.updatedAt || '-'}</td>
                      <td style={styles.td}>
                        <button
                          style={styles.actionBtn}
                          title="Ver Conversión"
                          onClick={() => navigate(`/util/catalogos/elementos/${el.id}/conversiones`)}
                        >
                          <ViewIcon />
                        </button>
                      </td>
                      <td style={styles.td}>
                        <button
                          style={styles.actionBtn}
                          title="Editar"
                          onClick={() => navigate(`/util/catalogos/catalogs/${id}/elementos/editar/${el.id}`)}
                        >
                          <EditIcon />
                        </button>
                      </td>
                      <td style={styles.td}>
                        <button
                          type="button"
                          style={{
                            ...styles.toggleSwitch,
                            opacity: togglingId === el.id ? 0.5 : 1,
                            pointerEvents: togglingId === el.id ? 'none' : 'auto',
                            border: 'none',
                            padding: 0,
                            appearance: 'none',
                            background: 'transparent',
                          }}
                          aria-label="Cambiar estatus del elemento"
                          onClick={() => handleToggleStatus(el.id)}
                        >
                          <span
                            style={{
                              ...styles.toggleSlider,
                              ...(el.status === 'Activo' ? styles.toggleSliderActive : {}),
                            }}
                          >
                            <span
                              style={{
                                ...styles.toggleSliderBefore,
                                ...(el.status === 'Activo' ? styles.toggleSliderBeforeActive : {}),
                              }}
                            />
                          </span>
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
              totalItems={totalElements}
              pageSize={itemsPerPage}
              onPageChange={handlePageChange}
              onPageSizeChange={(newSize) => {
                setItemsPerPage(newSize);
                if (showResults) {
                  performSearch(1, newSize);
                }
              }}
            />
          </>
          );
        })()}

        <div style={{ ...styles.footer, justifyContent: 'flex-end' }}>
          {exportError && (
            <span style={{ fontSize: '0.75rem', color: '#dc2626', marginRight: 'auto' }}>{exportError}</span>
          )}
          <button
            type="button"
            style={{ ...styles.ghostBtn, appearance: 'none' }}
            onClick={() => navigate('/util/catalogos/catalogs')}
          >
            Volver
          </button>
          {showResults && filteredElements.length > 0 && (
            <select
              style={{
                padding: '0.5rem 0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.25rem',
                fontSize: '0.875rem',
                backgroundColor: '#ffffff',
                cursor: isExporting ? 'not-allowed' : 'pointer',
                opacity: isExporting ? 0.6 : 1,
              }}
              value=""
              onChange={(e) => {
                if (e.target.value) {
                  handleExport(e.target.value as 'xlsx' | 'csv');
                }
              }}
              disabled={isExporting}
            >
              <option value="">{isExporting ? '⏳ Exportando...' : '📋 Exportar como'}</option>
              <option value="xlsx">Hoja de cálculo (XLSX)</option>
              <option value="csv">CSV</option>
            </select>
          )}
        </div>
      </div>
      {ModalNode}
    </div>
  );
}

