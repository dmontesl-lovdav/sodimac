import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { conversionService } from '@features/catalogos/services/catalogosApi';
import { exportToCSV, exportToExcel } from '@features/catalogos/utils/export';
import { Pagination } from '@shared/components/ui/pagination';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import { withFinanceBreadcrumb } from '@shared/components/ui/navigation/financeBreadcrumb';

const EXPORT_COLUMNS = [
  { key: 'idElemento', label: 'ID Elemento' },
  { key: 'elemento', label: 'Elemento' },
  { key: 'valor', label: 'Valor' },
  { key: 'fechaInicioVigencia', label: 'Fecha Inicio Vigencia' },
  { key: 'fechaFinVigencia', label: 'Fecha Fin Vigencia' },
  { key: 'estatus', label: 'Estatus' },
  { key: 'catalogoOrigen', label: 'Catálogo Origen' },
  { key: 'idUsuarioRegistro', label: 'ID Usuario Registro' },
  { key: 'fechaRegistro', label: 'Fecha Registro' },
  { key: 'idUsuarioActualizacion', label: 'ID Usuario Actualización' },
  { key: 'fechaActualizacion', label: 'Fecha Actualización' },
];

const toEsMxDate = (raw: unknown): string =>
  raw ? new Date(raw as string).toLocaleDateString('es-MX') : '';

const mapConversionForExport = (c: any) => ({
  idElemento: c.idElemento || '',
  elemento: c.elemento || '',
  valor: c.valor || '',
  fechaInicioVigencia: toEsMxDate(c.fechaInicioVigencia),
  fechaFinVigencia: toEsMxDate(c.fechaFinVigencia),
  estatus: c.estatus || '',
  catalogoOrigen: c.catalogoOrigen || '',
  idUsuarioRegistro: c.idUsuarioRegistro || '',
  fechaRegistro: toEsMxDate(c.fechaRegistro),
  idUsuarioActualizacion: c.idUsuarioActualizacion || '',
  fechaActualizacion: toEsMxDate(c.fechaActualizacion),
});

const buildExportTimestamp = (): string => {
  const now = new Date();
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}_${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}`;
};

const ExportFileIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
    <polyline points="14 2 14 8 20 8" />
    <line x1="12" y1="18" x2="12" y2="12" />
    <polyline points="9 15 12 18 15 15" />
  </svg>
);

const EditIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
  </svg>
);

const TrashIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
    <polyline points="3 6 5 6 21 6" />
    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
    <line x1="10" y1="11" x2="10" y2="17" />
    <line x1="14" y1="11" x2="14" y2="17" />
  </svg>
);

export default function ConversionsContainer() {
  const navigate = useNavigate();
  const location = useLocation();
  const catalogId = (location.state as { catalogId?: string | number } | null)?.catalogId;
  const { elementId } = useParams<{ elementId: string }>();

  const [conversions, setConversions] = useState<any[]>([]);
  const [sourceInfo, setSourceInfo] = useState<any>(null);
  const [sourceCatalogId, setSourceCatalogId] = useState<string | number | undefined>(undefined);
  const [totalResults, setTotalResults] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [isLoading, setIsLoading] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());
  const [modal, setModal] = useState<{ type: string; data?: any } | null>(null);
  const [message, setMessage] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

  const [isExporting, setIsExporting] = useState(false);
  const [exportError, setExportError] = useState<string | null>(null);
  const [filters, setFilters] = useState({ idElemento: '', elemento: '', valor: '', catalogoOrigen: '', estatus: '' });

  const performSearch = useCallback(async (page = 1, overridePageSize?: number) => {
    if (!elementId) return;
    setIsLoading(true);
    setHasSearched(true);
    try {
      const effectivePageSize = overridePageSize ?? pageSize;
      const params: any = { idElementoOrigen: parseInt(elementId), page, pageSize: effectivePageSize, sortBy: 'createdAt', sortDir: 'desc' };
      if (filters.idElemento) params.idElemento = parseInt(filters.idElemento);
      if (filters.elemento) params.elemento = filters.elemento;
      if (filters.valor) params.valorElemento = filters.valor;
      if (filters.catalogoOrigen) params.catalogoOrigen = filters.catalogoOrigen;
      if (filters.estatus) params.estatus = filters.estatus === 'Activo' ? 1 : 0;
      const res = await conversionService.search(params);
      const firstItem = res.items?.[0] as { idCatalogoElementoOrigen?: number | null } | undefined;
      if (firstItem?.idCatalogoElementoOrigen != null) setSourceCatalogId(firstItem.idCatalogoElementoOrigen);
      setConversions(res.items || []);
      setTotalResults(res.total || 0);
      setTotalPages(res.totalPages || 0);
      setCurrentPage(page);
    } catch (e) {
      setMessage({ text: 'Ocurrió un problema al consultar las conversiones. Intente nuevamente.', type: 'error' });
    } finally { setIsLoading(false); }
  }, [elementId, filters, pageSize]);

  useEffect(() => {
    if (elementId) {
      conversionService.search({ idElementoOrigen: parseInt(elementId), page: 1, pageSize: 1 })
        .then((res: any) => {
          const firstItem = res.items?.[0];
          if (firstItem?.idCatalogoElementoOrigen != null) setSourceCatalogId(firstItem.idCatalogoElementoOrigen);
          if (res.sourceElement) {
            setSourceInfo(res.sourceElement);
          } else if (res.items && res.items.length > 0) {
            const first = res.items[0];
            setSourceInfo({
              nombre: first.elementoOrigen || first.elemento,
              catalogoOrigen: first.catalogoElementoOrigen || first.catalogoOrigen,
              estatus: first.estatusElementoOrigen || 'Activo',
              fechaRegistro: first.fechaRegistro,
              valor: first.valorElementoOrigen || first.valor || '-',
            });
          }
        })
        .catch(() => {});
    }
  }, [elementId]);

  const handleClear = () => {
    setFilters({ idElemento: '', elemento: '', valor: '', catalogoOrigen: '', estatus: '' });
    setConversions([]);
    setTotalResults(0);
    setTotalPages(0);
    setCurrentPage(1);
    setSelectedIds(new Set());
    setHasSearched(false);
  };

  const handleDelete = async (id: number) => {
    try { await conversionService.delete(id); setMessage({ text: 'Conversión eliminada.', type: 'success' }); performSearch(currentPage); }
    catch { setMessage({ text: 'Ocurrió un problema al eliminar la(s) conversión(es). Intente nuevamente.', type: 'error' }); }
    setModal(null);
  };

  const handleDeleteMultiple = async () => {
    try { await conversionService.deleteMultiple(Array.from(selectedIds)); setSelectedIds(new Set()); setMessage({ text: 'Conversiones eliminadas.', type: 'success' }); performSearch(currentPage); }
    catch { setMessage({ text: 'Ocurrió un problema al eliminar la(s) conversión(es). Intente nuevamente.', type: 'error' }); }
    setModal(null);
  };

  const handleSetPrincipal = async (id: number, isPrincipal: boolean) => {
    try {
      await conversionService.updatePrincipal(id, { conversionPrincipal: isPrincipal });
      setMessage({ text: isPrincipal ? 'Conversión marcada como principal correctamente.' : 'La conversión principal ha sido desactivada.', type: 'success' });
      performSearch(currentPage);
    } catch { setMessage({ text: 'Ocurrió un problema al actualizar la conversión principal. Intente nuevamente.', type: 'error' }); }
    setModal(null);
  };

  const toggleSelect = (id: number) => {
    const s = new Set(selectedIds);
    s.has(id) ? s.delete(id) : s.add(id);
    setSelectedIds(s);
  };

  const toggleSelectAll = () => {
    if (conversions.every(c => selectedIds.has(c.idConversion))) {
      const s = new Set(selectedIds);
      conversions.forEach(c => s.delete(c.idConversion));
      setSelectedIds(s);
    } else {
      const s = new Set(selectedIds);
      conversions.forEach(c => s.add(c.idConversion));
      setSelectedIds(s);
    }
  };

  const buildConversionSearchParams = () => {
    const params: any = {
      idElementoOrigen: parseInt(elementId ?? ''),
      page: 1,
      pageSize: totalResults || 1000,
      sortBy: 'createdAt',
      sortDir: 'desc',
    };
    if (filters.idElemento) params.idElemento = parseInt(filters.idElemento);
    if (filters.elemento) params.elemento = filters.elemento;
    if (filters.valor) params.valorElemento = filters.valor;
    if (filters.catalogoOrigen) params.catalogoOrigen = filters.catalogoOrigen;
    if (filters.estatus) params.estatus = filters.estatus === 'Activo' ? 1 : 0;
    return params;
  };

  const fetchExportDataset = async () => {
    const res = await conversionService.search(buildConversionSearchParams());
    const items = res.items || [];
    return selectedIds.size > 0
      ? items.filter((c: any) => selectedIds.has(c.idConversion))
      : items;
  };

  const handleExport = async (format: 'xlsx' | 'csv') => {
    if (isExporting || !elementId) return;
    setIsExporting(true);
    setExportError(null);

    try {
      const dataToExport = await fetchExportDataset();
      const exportData = dataToExport.map(mapConversionForExport);

      if (exportData.length === 0) {
        setExportError('No hay datos para exportar.');
        return;
      }

      const filename = `conversiones_${buildExportTimestamp()}`;
      if (format === 'xlsx') {
        exportToExcel(exportData, EXPORT_COLUMNS, filename);
      } else {
        exportToCSV(exportData, EXPORT_COLUMNS, filename);
      }
    } catch (error: any) {
      if (error?.response?.status === 403) {
        setExportError('No cuenta con permisos para exportar conversiones.');
      } else {
        setExportError('Ocurrió un problema al exportar. Intente nuevamente.');
      }
    } finally {
      setIsExporting(false);
    }
  };

  const S: any = {
    container: { padding: '1.5rem', fontFamily: 'Inter,system-ui,sans-serif', backgroundColor: '#fff', minHeight: '100vh' },
    card: { backgroundColor: '#f8fafc', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1rem 1.5rem', marginBottom: '1.5rem' },
    title: { fontSize: '1.5rem', fontWeight: 600, color: '#1e293b' },
    desc: { fontSize: '0.875rem', color: '#64748b', marginBottom: '1.5rem' },
    filterRow: { display: 'flex', flexWrap: 'wrap', gap: '0.75rem', marginBottom: '1.5rem', alignItems: 'flex-end' },
    filterGroup: { display: 'flex', flexDirection: 'column', gap: '0.25rem', minWidth: '120px' },
    filterLabel: { fontSize: '0.7rem', color: '#64748b', fontWeight: 500 },
    input: { padding: '0.4rem 0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem', fontSize: '0.8rem' },
    select: { padding: '0.4rem 0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem', fontSize: '0.8rem', backgroundColor: '#fff' },
    btn: { padding: '0.5rem 1rem', borderRadius: '0.375rem', fontSize: '0.875rem', fontWeight: 500, cursor: 'pointer', border: 'none' },
    primaryBtn: { backgroundColor: '#002D4C', color: '#fff' },
    outlineBtn: { backgroundColor: '#fff', color: '#002D4C', border: '1px solid #002D4C' },
    secondaryBtn: { display: 'inline-flex', alignItems: 'center', gap: '0.5rem', padding: '0.625rem 1rem', backgroundColor: '#fff', color: '#1f2937', border: '1px solid #d1d5db', borderRadius: '0.375rem', fontSize: '0.875rem', fontWeight: 500, cursor: 'pointer' },
    dangerBtn: { backgroundColor: '#fff', color: '#dc2626', border: '1px solid #dc2626' },
    modalTitle: { fontSize: '1.125rem', fontWeight: 600, color: '#1e293b', marginBottom: '0.5rem' },
    modalSubtitle: { fontSize: '0.875rem', color: '#64748b', marginBottom: '1.5rem' },
    emptyState: { padding: '3rem', textAlign: 'center', color: '#64748b', backgroundColor: '#f8fafc', borderRadius: '0.5rem', border: '1px solid #e5e7eb' },
    ghostBtn: { backgroundColor: 'transparent', color: '#64748b', textDecoration: 'underline', border: 'none' },
    table: { width: '100%', borderCollapse: 'collapse', fontSize: '0.7rem' },
    th: { padding: '0.4rem 0.3rem', textAlign: 'left', fontWeight: 500, color: '#002d4c', backgroundColor: '#eaf5fc', borderBottom: '1px solid #e5e7eb', fontSize: '0.65rem', whiteSpace: 'nowrap' },
    td: { padding: '0.4rem 0.3rem', borderBottom: '1px solid #e5e7eb', color: '#1e293b', fontSize: '0.7rem', whiteSpace: 'nowrap' },
    badge: { padding: '0.125rem 0.5rem', borderRadius: '9999px', fontSize: '0.65rem', fontWeight: 500 },
    active: { backgroundColor: '#dcfce7', color: '#166534' },
    inactive: { backgroundColor: '#fee2e2', color: '#991b1b' },
    footer: { display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' },
    pagination: { display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '0.5rem', marginTop: '1rem' },
    pageBtn: { padding: '0.25rem 0.5rem', border: '1px solid #d1d5db', borderRadius: '0.25rem', fontSize: '0.8rem', cursor: 'pointer', backgroundColor: '#fff' },
    pageBtnActive: { backgroundColor: '#002D4C', color: '#fff', borderColor: '#002D4C' },
    modal: { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 },
    modalBox: { backgroundColor: '#fff', borderRadius: '0.75rem', padding: '1.5rem', maxWidth: '420px', width: '90%' },
    msg: { padding: '0.75rem 1rem', borderRadius: '0.375rem', marginBottom: '1rem', fontSize: '0.875rem' },
  };

  const formatDate = (d: any) => d ? new Date(d).toLocaleDateString('es-MX') : '-';

  const effectiveCatalogId = catalogId ?? sourceCatalogId;
  const goToElementos = () => {
    if (effectiveCatalogId != null) {
      navigate(`/util/catalogos/catalogs/${effectiveCatalogId}/elementos`);
    } else {
      navigate(-1);
    }
  };

  return (
    <div style={S.container}>
      <Breadcrumb
        items={withFinanceBreadcrumb([
          { label: 'Gestión de Catálogos', to: '/util/catalogos' },
          { label: 'Catálogos', to: '/util/catalogos/catalogs' },
          effectiveCatalogId != null
            ? { label: 'Elementos', to: `/util/catalogos/catalogs/${effectiveCatalogId}/elementos` }
            : { label: 'Elementos', onClick: goToElementos },
          { label: 'Conversiones' },
        ])}
      />

      {message && (
        <div style={{ ...S.msg, backgroundColor: message.type === 'success' ? '#dcfce7' : '#fee2e2', color: message.type === 'success' ? '#166534' : '#991b1b' }}>
          {message.text}
          <button style={{ float: 'right', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 600 }} onClick={() => setMessage(null)}>✕</button>
        </div>
      )}

      <h1 style={S.title}>Conversiones</h1>

      {sourceInfo && (
        <div style={S.card}>
          <div style={{ fontSize: '0.875rem', fontWeight: 600, marginBottom: '0.5rem', color: '#1e293b' }}>Información del Elemento Origen</div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '0.75rem' }}>
            <div><span style={{ fontSize: '0.7rem', color: '#64748b' }}>Nombre del Elemento</span><br/><span style={{ fontSize: '0.8rem', fontWeight: 500 }}>{sourceInfo.nombre || '-'}</span></div>
            <div><span style={{ fontSize: '0.7rem', color: '#64748b' }}>Catálogo de Origen</span><br/><span style={{ fontSize: '0.8rem', fontWeight: 500 }}>{sourceInfo.catalogoOrigen || '-'}</span></div>
            <div><span style={{ fontSize: '0.7rem', color: '#64748b' }}>Estatus Elemento</span><br/>
              <span style={{ ...S.badge, ...(sourceInfo.estatus === 'Activo' ? S.active : S.inactive), fontSize: '0.7rem' }}>{sourceInfo.estatus}</span></div>
            <div><span style={{ fontSize: '0.7rem', color: '#64748b' }}>Fecha de Registro</span><br/><span style={{ fontSize: '0.8rem', fontWeight: 500 }}>{formatDate(sourceInfo.fechaRegistro)}</span></div>
            <div><span style={{ fontSize: '0.7rem', color: '#64748b' }}>Valor del Elemento</span><br/><span style={{ fontSize: '0.8rem', fontWeight: 500 }}>{sourceInfo.valor || '-'}</span></div>
          </div>
        </div>
      )}

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', marginBottom: '1.5rem' }}>
        <p style={{ ...S.desc, marginBottom: 0, maxWidth: '520px' }}>Busca y agrega o elimina elementos de conversión, define sus valores y obtén información sobre cambios que se realicen a los mismos.</p>
        <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          {hasSearched && conversions.length > 0 && (
            <>
              <button
                style={{ ...S.secondaryBtn, opacity: isExporting ? 0.5 : 1, cursor: isExporting ? 'not-allowed' : 'pointer' }}
                onClick={() => handleExport('csv')}
                disabled={isExporting}
                title="Exportar a CSV"
              >
                <ExportFileIcon />
                Exportar CSV
              </button>
              <button
                style={{ ...S.secondaryBtn, opacity: isExporting ? 0.5 : 1, cursor: isExporting ? 'not-allowed' : 'pointer' }}
                onClick={() => handleExport('xlsx')}
                disabled={isExporting}
                title="Exportar a Excel"
              >
                <ExportFileIcon />
                Exportar Excel
              </button>
            </>
          )}
          <button style={{ ...S.btn, ...S.outlineBtn, display: 'inline-flex', alignItems: 'center', gap: '0.4rem', opacity: selectedIds.size === 0 ? 0.5 : 1, cursor: selectedIds.size === 0 ? 'not-allowed' : 'pointer' }} disabled={selectedIds.size === 0}
            onClick={() => setModal({ type: 'deleteMultiple' })}><TrashIcon /> Borrar Seleccionados ({selectedIds.size})</button>
          <button style={{ ...S.btn, ...S.primaryBtn }} onClick={() => navigate(`/util/catalogos/elementos/${elementId}/conversiones/nueva`)}>+ Nueva Conversión</button>
        </div>
      </div>

      <div style={S.filterRow}>
        <div style={S.filterGroup}><label htmlFor="filter-conv-id-elemento" style={S.filterLabel}>ID Elemento</label><input id="filter-conv-id-elemento" style={S.input} value={filters.idElemento} onChange={e => setFilters({ ...filters, idElemento: e.target.value })} /></div>
        <div style={S.filterGroup}><label htmlFor="filter-conv-elemento" style={S.filterLabel}>Elemento</label><input id="filter-conv-elemento" style={S.input} value={filters.elemento} onChange={e => setFilters({ ...filters, elemento: e.target.value })} /></div>
        <div style={S.filterGroup}><label htmlFor="filter-conv-valor" style={S.filterLabel}>Valor Elemento</label><input id="filter-conv-valor" style={S.input} value={filters.valor} onChange={e => setFilters({ ...filters, valor: e.target.value })} /></div>
        <div style={S.filterGroup}><label htmlFor="filter-conv-catalogo-origen" style={S.filterLabel}>Catálogo Origen</label><input id="filter-conv-catalogo-origen" style={S.input} value={filters.catalogoOrigen} onChange={e => setFilters({ ...filters, catalogoOrigen: e.target.value })} /></div>
        <div style={S.filterGroup}><label htmlFor="filter-conv-estatus" style={S.filterLabel}>Estatus</label>
          <select id="filter-conv-estatus" style={S.select} value={filters.estatus} onChange={e => setFilters({ ...filters, estatus: e.target.value })}>
            <option value="">Todos</option><option value="Activo">Activo</option><option value="Inactivo">Inactivo</option>
          </select>
        </div>
        <button style={{ ...S.btn, ...S.ghostBtn }} onClick={handleClear}>Limpiar</button>
        <button style={{ ...S.btn, ...S.outlineBtn }} onClick={() => performSearch(1)}>Buscar</button>
      </div>

      {(() => {
        if (!hasSearched) {
          return <div style={S.emptyState}>Utiliza el filtro para realizar una búsqueda de conversiones.</div>;
        }
        if (isLoading) {
          return <p style={{ textAlign: 'center', color: '#64748b' }}>Cargando...</p>;
        }
        if (conversions.length === 0) {
          return <p style={{ textAlign: 'center', color: '#64748b', padding: '3rem' }}>No se encontraron conversiones coincidentes con los criterios de búsqueda ingresados.</p>;
        }
        return (
        <>
          <div style={{ fontSize: '0.875rem', fontWeight: 600, marginBottom: '0.75rem' }}>{totalResults} Elementos encontrados</div>
          <div style={{ overflowX: 'auto' }}>
            <table style={S.table}>
              <thead><tr>
                <th style={S.th}><input type="checkbox" checked={conversions.length > 0 && conversions.every(c => selectedIds.has(c.idConversion))} onChange={toggleSelectAll} /></th>
                <th style={S.th}>ID</th><th style={S.th}>Elemento</th><th style={S.th}>Valor</th>
                <th style={S.th}>F. Inicio</th><th style={S.th}>F. Fin</th><th style={S.th}>Estatus</th>
                <th style={S.th}>Cat. Origen</th><th style={S.th}>Usr. Reg.</th><th style={S.th}>F. Reg.</th>
                <th style={S.th}>Usr. Act.</th><th style={S.th}>F. Act.</th>
                <th style={S.th}>Principal</th><th style={S.th}>Editar</th><th style={S.th}>Borrar</th>
              </tr></thead>
              <tbody>
                {conversions.map((c: any) => (
                  <tr key={c.idConversion}>
                    <td style={S.td}><input type="checkbox" checked={selectedIds.has(c.idConversion)} onChange={() => toggleSelect(c.idConversion)} /></td>
                    <td style={S.td}>{c.idElemento}</td>
                    <td style={S.td}>{c.elemento}</td>
                    <td style={S.td}>{c.valor || '-'}</td>
                    <td style={S.td}>{formatDate(c.fechaInicioVigencia)}</td>
                    <td style={S.td}>{formatDate(c.fechaFinVigencia)}</td>
                    <td style={S.td}><span style={{ ...S.badge, ...(c.estatus === 'Activo' ? S.active : S.inactive) }}>{c.estatus}</span></td>
                    <td style={S.td}>{c.catalogoOrigen}</td>
                    <td style={S.td}>{c.idUsuarioRegistro || '-'}</td>
                    <td style={S.td}>{formatDate(c.fechaRegistro)}</td>
                    <td style={S.td}>{c.idUsuarioActualizacion || '-'}</td>
                    <td style={S.td}>{formatDate(c.fechaActualizacion)}</td>
                    <td style={S.td}>
                      <button type="button" style={{ width: 36, height: 20, borderRadius: 10, backgroundColor: c.esPrincipal ? '#002D4C' : '#cbd5e1', cursor: 'pointer', position: 'relative', border: 'none', padding: 0, appearance: 'none' }}
                        aria-label="Cambiar conversión principal"
                        onClick={() => setModal({ type: c.esPrincipal ? 'unsetPrincipal' : 'setPrincipal', data: c })}>
                        <div style={{ width: 16, height: 16, borderRadius: '50%', backgroundColor: '#fff', position: 'absolute', top: 2, left: c.esPrincipal ? 18 : 2, transition: '0.2s' }} />
                      </button>
                    </td>
                    <td style={S.td}><button type="button" title="Editar" aria-label="Editar" style={{ background: 'transparent', border: 'none', padding: '0.25rem', cursor: 'pointer', color: '#002D4C', display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}
                      onClick={() => navigate(`/util/catalogos/elementos/${elementId}/conversiones/editar/${c.idConversion}`)}><EditIcon /></button></td>
                    <td style={S.td}><button type="button" title="Borrar" aria-label="Borrar" style={{ background: 'transparent', border: 'none', padding: '0.25rem', cursor: 'pointer', color: '#dc2626', display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}
                      onClick={() => setModal({ type: 'delete', data: c })}><TrashIcon /></button></td>
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
            onPageChange={(p) => performSearch(p)}
            onPageSizeChange={(newSize) => {
              setPageSize(newSize);
              performSearch(1, newSize);
            }}
          />
        </>
        );
      })()}

      <div style={S.footer}>
        {exportError && (
          <span style={{ fontSize: '0.75rem', color: '#dc2626', marginRight: 'auto' }}>{exportError}</span>
        )}
        <button style={{ ...S.btn, ...S.ghostBtn }} onClick={goToElementos}>Volver</button>
      </div>

      {modal && (() => {
        const titleByType: Record<string, string> = {
          delete: `¿Está seguro de eliminar la conversión ${modal.data?.idConversion}?`,
          deleteMultiple: `¿Está seguro de eliminar ${selectedIds.size} conversiones seleccionadas?`,
          setPrincipal: `¿Desea marcar la conversión ${modal.data?.idConversion} como principal?`,
          unsetPrincipal: `¿Desea desactivar la conversión ${modal.data?.idConversion} como principal?`,
        };
        const subtitleByType: Record<string, string> = {
          delete: 'Esta acción eliminará la conversión y no podrá ser recuperada. ¿Desea continuar?',
          deleteMultiple: 'Esta acción eliminará las conversiones seleccionadas y no podrán ser recuperadas. ¿Desea continuar?',
          setPrincipal: 'Esta acción reemplazará la conversión principal actual.',
          unsetPrincipal: 'Esta acción desactivará la conversión principal actual.',
        };
        const modalTitle = titleByType[modal.type] ?? '';
        const modalSubtitle = subtitleByType[modal.type] ?? '';
        return (
          <div style={S.modal}>
            <div style={S.modalBox}>
              <h3 style={S.modalTitle}>{modalTitle}</h3>
              {modalSubtitle && <p style={S.modalSubtitle}>{modalSubtitle}</p>}
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem' }}>
                <button style={{ ...S.btn, border: '1px solid #d1d5db', backgroundColor: '#fff', color: '#374151' }} onClick={() => setModal(null)}>No</button>
                <button style={{ ...S.btn, ...S.primaryBtn }} onClick={() => {
                  if (modal.type === 'delete') handleDelete(modal.data.idConversion);
                  else if (modal.type === 'deleteMultiple') handleDeleteMultiple();
                  else if (modal.type === 'setPrincipal') handleSetPrincipal(modal.data.idConversion, true);
                  else if (modal.type === 'unsetPrincipal') handleSetPrincipal(modal.data.idConversion, false);
                }}>Sí</button>
              </div>
            </div>
          </div>
        );
      })()}
    </div>
  );
}

