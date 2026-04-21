import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { supplierService, Supplier, SupplierType } from '../../../services/api';
import { exportToCSV, exportToExcel, ExportColumn } from '../../../utils/export';

const SuppliersContainer = () => {
  const navigate = useNavigate();
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [filteredSuppliers, setFilteredSuppliers] = useState<Supplier[]>([]);
  const [supplierTypes, setSupplierTypes] = useState<SupplierType[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [exportFormat, setExportFormat] = useState<string>('csv');
  const [currentPage, setCurrentPage] = useState(1);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showStatusModal, setShowStatusModal] = useState(false);
  const [supplierToDelete, setSupplierToDelete] = useState<Supplier | null>(null);
  const [supplierToToggle, setSupplierToToggle] = useState<Supplier | null>(null);
  const pageSize = 10;

  const [filters, setFilters] = useState({
    supplierNumber: '',
    businessName: '',
    rfc: '',
    typeId: '',
    status: '',
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [suppliersData, typesData] = await Promise.all([
        supplierService.getAll(),
        supplierService.getTypes(),
      ]);
      setSuppliers(suppliersData);
      setFilteredSuppliers(suppliersData);
      setSupplierTypes(typesData);
    } catch (err) {
      setError('Error al cargar los datos');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    let result = [...suppliers];

    if (filters.supplierNumber) {
      result = result.filter((s) =>
        s.supplierNumber.toLowerCase().includes(filters.supplierNumber.toLowerCase())
      );
    }
    if (filters.businessName) {
      result = result.filter((s) =>
        s.businessName.toLowerCase().includes(filters.businessName.toLowerCase())
      );
    }
    if (filters.rfc) {
      result = result.filter((s) =>
        s.rfc.toLowerCase().includes(filters.rfc.toLowerCase())
      );
    }
    if (filters.typeId) {
      result = result.filter((s) => s.supplierType?.id === parseInt(filters.typeId));
    }
    if (filters.status) {
      result = result.filter((s) => s.status === parseInt(filters.status));
    }

    setFilteredSuppliers(result);
    setCurrentPage(1);
  };

  const handleClearFilters = () => {
    setFilters({
      supplierNumber: '',
      businessName: '',
      rfc: '',
      typeId: '',
      status: '',
    });
    setFilteredSuppliers(suppliers);
    setCurrentPage(1);
  };

  const handleExport = () => {
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

    if (exportFormat === 'csv') {
      exportToCSV(dataToExport, columns, filename);
    } else {
      exportToExcel(dataToExport, columns, filename);
    }
  };

  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      setSelectedIds(paginatedSuppliers.map((s) => s.id));
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

  const handleDelete = async () => {
    if (!supplierToDelete) return;
    try {
      await supplierService.delete(supplierToDelete.id);
      setShowDeleteModal(false);
      setSupplierToDelete(null);
      loadData();
    } catch (err) {
      setError('Error al eliminar el proveedor');
      console.error(err);
    }
  };

  const handleToggleStatus = async () => {
    if (!supplierToToggle) return;
    try {
      const newStatus = supplierToToggle.status === 1 ? 0 : 1;
      await supplierService.update(supplierToToggle.id, { status: newStatus });
      setShowStatusModal(false);
      setSupplierToToggle(null);
      loadData();
    } catch (err) {
      setError('Error al cambiar el estatus');
      console.error(err);
    }
  };

  const totalPages = Math.ceil(filteredSuppliers.length / pageSize);
  const paginatedSuppliers = filteredSuppliers.slice(
    (currentPage - 1) * pageSize,
    currentPage * pageSize
  );

  return (
    <div className="somx-container">
      <div className="somx-breadcrumb">
        <a href="#/catalogos">Catálogos</a>
        <span>/</span>
        <span>Proveedores</span>
      </div>

      <div className="somx-page-header">
        <h1 className="somx-page-title">Administración de Proveedores</h1>
        <button
          className="somx-btn somx-btn-primary"
          onClick={() => navigate('/catalogos/proveedores/crear')}
        >
          + Nuevo Proveedor
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
            <label className="somx-form-label">Razón Social</label>
            <input
              type="text"
              className="somx-form-input"
              value={filters.businessName}
              onChange={(e) => setFilters({ ...filters, businessName: e.target.value })}
              placeholder="Buscar..."
            />
          </div>
          <div className="somx-form-group">
            <label className="somx-form-label">RFC</label>
            <input
              type="text"
              className="somx-form-input"
              value={filters.rfc}
              onChange={(e) => setFilters({ ...filters, rfc: e.target.value })}
              placeholder="Buscar..."
            />
          </div>
          <div className="somx-form-group">
            <label className="somx-form-label">Tipo</label>
            <select
              className="somx-form-select"
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
          <div className="somx-actions-left">
            <button
              className="somx-btn somx-btn-secondary somx-btn-sm"
              disabled={selectedIds.length === 0}
              onClick={() => {
                if (selectedIds.length === 1) {
                  const supplier = filteredSuppliers.find((s) => s.id === selectedIds[0]);
                  if (supplier) {
                    navigate(`/catalogos/proveedores/${supplier.id}/bloquear`);
                  }
                }
              }}
            >
              Gestionar Bloqueo
            </button>
          </div>
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
                          paginatedSuppliers.length > 0 &&
                          selectedIds.length === paginatedSuppliers.length
                        }
                        onChange={(e) => handleSelectAll(e.target.checked)}
                      />
                    </th>
                    <th>ID</th>
                    <th>Número</th>
                    <th>RFC</th>
                    <th>Razón Social</th>
                    <th>Tipo</th>
                    <th>Condición Pago</th>
                    <th>Estatus</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedSuppliers.length === 0 ? (
                    <tr>
                      <td colSpan={9} className="somx-text-center somx-text-gray">
                        No se encontraron proveedores
                      </td>
                    </tr>
                  ) : (
                    paginatedSuppliers.map((supplier) => (
                      <tr key={supplier.id}>
                        <td>
                          <input
                            type="checkbox"
                            className="somx-checkbox"
                            checked={selectedIds.includes(supplier.id)}
                            onChange={(e) => handleSelectOne(supplier.id, e.target.checked)}
                          />
                        </td>
                        <td>{supplier.id}</td>
                        <td>{supplier.supplierNumber}</td>
                        <td>{supplier.rfc}</td>
                        <td>{supplier.businessName}</td>
                        <td>{supplier.supplierType?.description || '-'}</td>
                        <td>{supplier.paymentCondition?.conditionName || '-'}</td>
                        <td>
                          <span
                            className={`somx-badge ${
                              supplier.status === 1 ? 'somx-badge-success' : 'somx-badge-danger'
                            }`}
                          >
                            {supplier.status === 1 ? 'Activo' : 'Inactivo'}
                          </span>
                        </td>
                        <td>
                          <div className="somx-flex somx-gap-sm">
                            <button
                              className="somx-btn somx-btn-primary somx-btn-sm"
                              onClick={() =>
                                navigate(`/catalogos/proveedores/editar/${supplier.id}`)
                              }
                            >
                              Editar
                            </button>
                            <button
                              className="somx-btn somx-btn-warning somx-btn-sm"
                              onClick={() => {
                                setSupplierToToggle(supplier);
                                setShowStatusModal(true);
                              }}
                            >
                              {supplier.status === 1 ? 'Inactivar' : 'Activar'}
                            </button>
                            <button
                              className="somx-btn somx-btn-danger somx-btn-sm"
                              onClick={() => {
                                setSupplierToDelete(supplier);
                                setShowDeleteModal(true);
                              }}
                            >
                              Eliminar
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
                {Math.min(currentPage * pageSize, filteredSuppliers.length)} de{' '}
                {filteredSuppliers.length} registros
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

      {showDeleteModal && (
        <div className="somx-modal-overlay">
          <div className="somx-modal">
            <div className="somx-modal-header">
              <h3 className="somx-modal-title">Confirmar Eliminación</h3>
              <button
                className="somx-modal-close"
                onClick={() => setShowDeleteModal(false)}
              >
                ×
              </button>
            </div>
            <div className="somx-modal-body">
              <p>
                ¿Está seguro de eliminar al proveedor{' '}
                <strong>{supplierToDelete?.businessName}</strong>?
              </p>
            </div>
            <div className="somx-modal-footer">
              <button
                className="somx-btn somx-btn-secondary"
                onClick={() => setShowDeleteModal(false)}
              >
                Cancelar
              </button>
              <button className="somx-btn somx-btn-danger" onClick={handleDelete}>
                Eliminar
              </button>
            </div>
          </div>
        </div>
      )}

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
                ¿Está seguro de{' '}
                {supplierToToggle?.status === 1 ? 'inactivar' : 'activar'} al proveedor{' '}
                <strong>{supplierToToggle?.businessName}</strong>?
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
                {supplierToToggle?.status === 1 ? 'Inactivar' : 'Activar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SuppliersContainer;








