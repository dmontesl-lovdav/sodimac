import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { conversionService, catalogElementService } from '@features/catalogos/services/catalogosApi';
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

export default function ConversionsContainer() {
  const navigate = useNavigate();
  const { elementId } = useParams<{ elementId: string }>();

  const [conversions, setConversions] = useState<any[]>([]);
  const [sourceInfo, setSourceInfo] = useState<any>(null);
  const [totalResults, setTotalResults] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());
  const [modal, setModal] = useState<{ type: string; data?: any } | null>(null);
  const [message, setMessage] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

  const [isExporting, setIsExporting] = useState(false);
  const [exportError, setExportError] = useState<string | null>(null);
  const [filters, setFilters] = useState({ idElemento: '', elemento: '', valor: '', catalogoOrigen: '', estatus: '' });

  const performSearch = useCallback(async (page = 1, overridePageSize?: number) => {
    if (!elementId) return;
    setIsLoading(true);
    try {
      const effectivePageSize = overridePageSize ?? pageSize;
      const params: any = { idElementoOrigen: parseInt(elementId), page, pageSize: effectivePageSize, sortBy: 'createdAt', sortDir: 'desc' };
      if (filters.idElemento) params.idElemento = parseInt(filters.idElemento);
      if (filters.elemento) params.elemento = filters.elemento;
      if (filters.valor) params.valorElemento = filters.valor;
      if (filters.catalogoOrigen) params.catalogoOrigen = filters.catalogoOrigen;
      if (filters.estatus) params.estatus = filters.estatus === 'Activo' ? 1 : 0;
      const res = await conversionService.search(params);
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

  useEffect(() => { performSearch(1); }, [elementId]);

  const handleClear = () => {
    setFilters({ idElemento: '', elemento: '', valor: '', catalogoOrigen: '', estatus: '' });
    performSearch(1);
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
      idElementoOrigen: parseInt(elementId!),
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
    primaryBtn: { backgroundColor: '#0066CC', color: '#fff' },
    outlineBtn: { backgroundColor: '#fff', color: '#0066CC', border: '1px solid #0066CC' },
    dangerBtn: { backgroundColor: '#fff', color: '#dc2626', border: '1px solid #dc2626' },
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
    pageBtnActive: { backgroundColor: '#0066CC', color: '#fff', borderColor: '#0066CC' },
    modal: { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 },
    modalBox: { backgroundColor: '#fff', borderRadius: '0.75rem', padding: '1.5rem', maxWidth: '420px', width: '90%' },
    msg: { padding: '0.75rem 1rem', borderRadius: '0.375rem', marginBottom: '1rem', fontSize: '0.875rem' },
  };

  const formatDate = (d: any) => d ? new Date(d).toLocaleDateString('es-MX') : '-';

  return (
    <div style={S.container}>
      <Breadcrumb
        items={withFinanceBreadcrumb([
          { label: 'Gestión de Catálogos', to: '/util/catalogos' },
          { label: 'Catálogos', to: '/util/catalogos/catalogs' },
          { label: 'Elementos' },
          { label: 'Conversiones' },
        ])}
      />

      {message && (
        <div style={{ ...S.msg, backgroundColor: message.type === 'success' ? '#dcfce7' : '#fee2e2', color: message.type === 'success' ? '#166534' : '#991b1b' }}>
          {message.text}
          <button style={{ float: 'right', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 600 }} onClick={() => setMessage(null)}>✕</button>
        </div>
      )}

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

      <h1 style={S.title}>Conversiones</h1>
      <p style={S.desc}>Busca y agrega o elimina elementos de conversión, define sus valores y obtén información sobre cambios que se realicen a los mismos.</p>

      <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1.5rem' }}>
        <button style={{ ...S.btn, ...S.primaryBtn }} onClick={() => navigate(`/util/catalogos/elementos/${elementId}/conversiones/nueva`)}>+ Nueva Conversión</button>
        <button style={{ ...S.btn, ...S.dangerBtn, opacity: selectedIds.size === 0 ? 0.5 : 1 }} disabled={selectedIds.size === 0}
          onClick={() => setModal({ type: 'deleteMultiple' })}>🗑 Borrar Seleccionados ({selectedIds.size})</button>
      </div>

      <div style={S.filterRow}>
        <div style={S.filterGroup}><label style={S.filterLabel}>ID Elemento</label><input style={S.input} value={filters.idElemento} onChange={e => setFilters({ ...filters, idElemento: e.target.value })} /></div>
        <div style={S.filterGroup}><label style={S.filterLabel}>Elemento</label><input style={S.input} value={filters.elemento} onChange={e => setFilters({ ...filters, elemento: e.target.value })} /></div>
        <div style={S.filterGroup}><label style={S.filterLabel}>Valor Elemento</label><input style={S.input} value={filters.valor} onChange={e => setFilters({ ...filters, valor: e.target.value })} /></div>
        <div style={S.filterGroup}><label style={S.filterLabel}>Catálogo Origen</label><input style={S.input} value={filters.catalogoOrigen} onChange={e => setFilters({ ...filters, catalogoOrigen: e.target.value })} /></div>
        <div style={S.filterGroup}><label style={S.filterLabel}>Estatus</label>
          <select style={S.select} value={filters.estatus} onChange={e => setFilters({ ...filters, estatus: e.target.value })}>
            <option value="">Todos</option><option value="Activo">Activo</option><option value="Inactivo">Inactivo</option>
          </select>
        </div>
        <button style={{ ...S.btn, ...S.ghostBtn }} onClick={handleClear}>Limpiar</button>
        <button style={{ ...S.btn, ...S.outlineBtn }} onClick={() => performSearch(1)}>Buscar</button>
      </div>

      {isLoading ? <p style={{ textAlign: 'center', color: '#64748b' }}>Cargando...</p> :
       conversions.length === 0 ? <p style={{ textAlign: 'center', color: '#64748b', padding: '3rem' }}>No se encontraron conversiones coincidentes con los criterios de búsqueda ingresados.</p> : (
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
                      <div style={{ width: 36, height: 20, borderRadius: 10, backgroundColor: c.esPrincipal ? '#0066CC' : '#cbd5e1', cursor: 'pointer', position: 'relative' }}
                        onClick={() => setModal({ type: c.esPrincipal ? 'unsetPrincipal' : 'setPrincipal', data: c })}>
                        <div style={{ width: 16, height: 16, borderRadius: '50%', backgroundColor: '#fff', position: 'absolute', top: 2, left: c.esPrincipal ? 18 : 2, transition: '0.2s' }} />
                      </div>
                    </td>
                    <td style={S.td}><button style={{ ...S.btn, ...S.ghostBtn, padding: '0.25rem', fontSize: '0.7rem' }}
                      onClick={() => navigate(`/util/catalogos/elementos/${elementId}/conversiones/editar/${c.idConversion}`)}>✏️</button></td>
                    <td style={S.td}><button style={{ ...S.btn, ...S.ghostBtn, padding: '0.25rem', fontSize: '0.7rem', color: '#dc2626' }}
                      onClick={() => setModal({ type: 'delete', data: c })}>🗑</button></td>
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
      )}

      <div style={S.footer}>
        {exportError && (
          <span style={{ fontSize: '0.75rem', color: '#dc2626', marginRight: 'auto' }}>{exportError}</span>
        )}
        <button style={{ ...S.btn, ...S.ghostBtn }} onClick={() => navigate(-1)}>Volver</button>
        {conversions.length > 0 && (
          <select
            style={{
              padding: '0.5rem 0.75rem',
              border: '1px solid #d1d5db',
              borderRadius: '0.25rem',
              fontSize: '0.875rem',
              backgroundColor: '#fff',
              cursor: isExporting ? 'not-allowed' : 'pointer',
              opacity: isExporting ? 0.6 : 1,
            }}
            value=""
            onChange={(e) => { if (e.target.value) handleExport(e.target.value as 'xlsx' | 'csv'); }}
            disabled={isExporting}
          >
            <option value="">{isExporting ? '⏳ Exportando...' : '📋 Exportar como'}</option>
            <option value="xlsx">Hoja de cálculo (XLSX)</option>
            <option value="csv">CSV</option>
          </select>
        )}
      </div>

      {modal && (
        <div style={S.modal}>
          <div style={S.modalBox}>
            <h3 style={{ marginBottom: '1rem', fontSize: '1.1rem' }}>
              {modal.type === 'delete' && '¿Está seguro de eliminar la conversión ' + modal.data?.idConversion + '?'}
              {modal.type === 'deleteMultiple' && `¿Está seguro de eliminar ${selectedIds.size} conversiones seleccionadas?`}
              {modal.type === 'setPrincipal' && `¿Desea marcar la conversión ${modal.data?.idConversion} como principal? Esta acción reemplazará la conversión principal actual.`}
              {modal.type === 'unsetPrincipal' && `¿Desea desactivar la conversión ${modal.data?.idConversion} como principal? Esta acción desactivará la conversión principal actual.`}
            </h3>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
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
      )}
    </div>
  );
}

