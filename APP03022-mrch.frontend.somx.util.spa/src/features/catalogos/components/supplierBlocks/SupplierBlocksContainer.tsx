import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { supplierBlockService, SupplierBlock } from '@features/catalogos/services/catalogosApi';
import { exportToCSV, exportToExcel, ExportColumn } from '@features/catalogos/utils/export';

const SupplierBlocksContainer = () => {
  const navigate = useNavigate();
  const [blocks, setBlocks] = useState<SupplierBlock[]>([]);
  const [filteredBlocks, setFilteredBlocks] = useState<SupplierBlock[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [exportFormat, setExportFormat] = useState<string>('csv');
  const [currentPage, setCurrentPage] = useState(1);
  const [showStatusModal, setShowStatusModal] = useState(false);
  const [blockToToggle, setBlockToToggle] = useState<SupplierBlock | null>(null);
  const pageSize = 10;

  const [filters, setFilters] = useState({
    supplierNumber: '',
    status: '',
    currentlyBlocked: '',
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await supplierBlockService.getAll();
      setBlocks(data);
      setFilteredBlocks(data);
    } catch (err) {
      setError('Error al cargar los datos');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    let result = [...blocks];

    if (filters.supplierNumber) {
      result = result.filter((b) =>
        b.supplierNumber.toLowerCase().includes(filters.supplierNumber.toLowerCase())
      );
    }
    if (filters.status) {
      result = result.filter((b) => b.status === parseInt(filters.status));
    }
    if (filters.currentlyBlocked) {
      const isBlocked = filters.currentlyBlocked === '1';
      result = result.filter((b) => b.currentlyBlocked === isBlocked);
    }

    setFilteredBlocks(result);
    setCurrentPage(1);
  };

  const handleClearFilters = () => {
    setFilters({
      supplierNumber: '',
      status: '',
      currentlyBlocked: '',
    });
    setFilteredBlocks(blocks);
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

  const handleExport = () => {
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

    if (exportFormat === 'csv') {
      exportToCSV(dataToExport, columns, filename);
    } else {
      exportToExcel(dataToExport, columns, filename);
    }
  };

  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      setSelectedIds(paginatedBlocks.map((b) => b.id));
    } else {
      setSelectedIds([]);
    }
  };

  const handleSelectOne = (id: number, checked: boolean) => {
    if (checked) {
      setSelectedIds([...selectedIds, id]);
    } else {
      setSelectedIds(selectedIds.filter((i) => i !== id));
    }
  };

  const handleToggleStatus = async () => {
    if (!blockToToggle) return;
    try {
      const newStatus = blockToToggle.status === 1 ? 0 : 1;
      await supplierBlockService.update(blockToToggle.id, { status: newStatus });
      setShowStatusModal(false);
      setBlockToToggle(null);
      loadData();
    } catch (err) {
      setError('Error al cambiar el estatus');
      console.error(err);
    }
  };

  const totalPages = Math.ceil(filteredBlocks.length / pageSize);
  const paginatedBlocks = filteredBlocks.slice(
    (currentPage - 1) * pageSize,
    currentPage * pageSize
  );

  return (
    <div className="somx-container">
      <div className="somx-breadcrumb">
        <a href="#/catalogos">Catálogos</a>
        <span>/</span>
        <span>Bloqueos de Proveedores</span>
      </div>

      <div className="somx-page-header">
        <h1 className="somx-page-title">Bloqueo de Proveedores</h1>
        <button
          className="somx-btn somx-btn-primary"
          onClick={() => navigate('/util/catalogos/bloqueos/crear')}
        >
          + Nuevo Bloqueo
        </button>
      </div>

      {error && <div className="somx-error">{error}</div>}

      <div className="somx-card">
        <div className="somx-filters">
          <div className="somx-form-group">
            <label className="somx-form-label">Número Proveedor</label>
            <input
              type="text"
              className="somx-form-input"
              value={filters.supplierNumber}
              onChange={(e) => setFilters({ ...filters, supplierNumber: e.target.value })}
              placeholder="Buscar..."
            />
          </div>
          <div className="somx-form-group">
            <label className="somx-form-label">Estatus</label>
            <select
              className="somx-form-select"
              value={filters.status}
              onChange={(e) => setFilters({ ...filters, status: e.target.value })}
            >
              <option value="">Todos</option>
              <option value="1">Activo</option>
              <option value="0">Inactivo</option>
            </select>
          </div>
          <div className="somx-form-group">
            <label className="somx-form-label">Vigente Actualmente</label>
            <select
              className="somx-form-select"
              value={filters.currentlyBlocked}
              onChange={(e) => setFilters({ ...filters, currentlyBlocked: e.target.value })}
            >
              <option value="">Todos</option>
              <option value="1">Sí</option>
              <option value="0">No</option>
            </select>
          </div>
          <div className="somx-filters-actions">
            <button className="somx-btn somx-btn-primary" onClick={handleSearch}>
              Buscar
            </button>
            <button className="somx-btn somx-btn-secondary" onClick={handleClearFilters}>
              Limpiar
            </button>
          </div>
        </div>

        <div className="somx-actions-bar">
          <div className="somx-actions-left"></div>
          <div className="somx-actions-right">
            <select
              className="somx-export-select"
              value={exportFormat}
              onChange={(e) => setExportFormat(e.target.value)}
            >
              <option value="csv">CSV</option>
              <option value="excel">Excel</option>
            </select>
            <button className="somx-btn somx-btn-success somx-btn-sm" onClick={handleExport}>
              Exportar
            </button>
          </div>
        </div>

        {loading ? (
          <div className="somx-loading">
            <div className="somx-loading-spinner"></div>
          </div>
        ) : (
          <>
            <div className="somx-table-container">
              <table className="somx-table">
                <thead>
                  <tr>
                    <th>
                      <input
                        type="checkbox"
                        className="somx-checkbox"
                        checked={
                          paginatedBlocks.length > 0 &&
                          selectedIds.length === paginatedBlocks.length
                        }
                        onChange={(e) => handleSelectAll(e.target.checked)}
                      />
                    </th>
                    <th>ID</th>
                    <th>Número Proveedor</th>
                    <th>Fecha Inicio</th>
                    <th>Fecha Fin</th>
                    <th>Razón</th>
                    <th>Vigente</th>
                    <th>Estatus</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedBlocks.length === 0 ? (
                    <tr>
                      <td colSpan={9} className="somx-text-center somx-text-gray">
                        No se encontraron bloqueos
                      </td>
                    </tr>
                  ) : (
                    paginatedBlocks.map((block) => (
                      <tr key={block.id}>
                        <td>
                          <input
                            type="checkbox"
                            className="somx-checkbox"
                            checked={selectedIds.includes(block.id)}
                            onChange={(e) => handleSelectOne(block.id, e.target.checked)}
                          />
                        </td>
                        <td>{block.id}</td>
                        <td>{block.supplierNumber}</td>
                        <td>{formatDate(block.validFrom)}</td>
                        <td>{formatDate(block.validTo)}</td>
                        <td>{block.blockReason || '-'}</td>
                        <td>
                          <span
                            className={`somx-badge ${
                              block.currentlyBlocked ? 'somx-badge-danger' : 'somx-badge-info'
                            }`}
                          >
                            {block.currentlyBlocked ? 'Sí' : 'No'}
                          </span>
                        </td>
                        <td>
                          <span
                            className={`somx-badge ${
                              block.status === 1 ? 'somx-badge-success' : 'somx-badge-danger'
                            }`}
                          >
                            {block.status === 1 ? 'Activo' : 'Inactivo'}
                          </span>
                        </td>
                        <td>
                          <div className="somx-flex somx-gap-sm">
                            <button
                              className="somx-btn somx-btn-primary somx-btn-sm"
                              onClick={() => navigate(`/util/catalogos/bloqueos/editar/${block.id}`)}
                            >
                              Editar
                            </button>
                            <button
                              className="somx-btn somx-btn-warning somx-btn-sm"
                              onClick={() => {
                                setBlockToToggle(block);
                                setShowStatusModal(true);
                              }}
                            >
                              {block.status === 1 ? 'Inactivar' : 'Activar'}
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            <div className="somx-pagination">
              <div className="somx-pagination-info">
                Mostrando {(currentPage - 1) * pageSize + 1} -{' '}
                {Math.min(currentPage * pageSize, filteredBlocks.length)} de{' '}
                {filteredBlocks.length} registros
              </div>
              <div className="somx-pagination-controls">
                <button
                  className="somx-btn somx-btn-secondary somx-btn-sm"
                  disabled={currentPage === 1}
                  onClick={() => setCurrentPage(currentPage - 1)}
                >
                  Anterior
                </button>
                <span className="somx-text-sm">
                  Página {currentPage} de {totalPages || 1}
                </span>
                <button
                  className="somx-btn somx-btn-secondary somx-btn-sm"
                  disabled={currentPage === totalPages || totalPages === 0}
                  onClick={() => setCurrentPage(currentPage + 1)}
                >
                  Siguiente
                </button>
              </div>
            </div>
          </>
        )}
      </div>

      {showStatusModal && (
        <div className="somx-modal-overlay">
          <div className="somx-modal">
            <div className="somx-modal-header">
              <h3 className="somx-modal-title">Confirmar Cambio de Estatus</h3>
              <button
                className="somx-modal-close"
                onClick={() => setShowStatusModal(false)}
              >
                ×
              </button>
            </div>
            <div className="somx-modal-body">
              <p>
                ¿Está seguro de {blockToToggle?.status === 1 ? 'inactivar' : 'activar'} el
                bloqueo del proveedor <strong>{blockToToggle?.supplierNumber}</strong>?
              </p>
            </div>
            <div className="somx-modal-footer">
              <button
                className="somx-btn somx-btn-secondary"
                onClick={() => setShowStatusModal(false)}
              >
                Cancelar
              </button>
              <button className="somx-btn somx-btn-warning" onClick={handleToggleStatus}>
                {blockToToggle?.status === 1 ? 'Inactivar' : 'Activar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SupplierBlocksContainer;








