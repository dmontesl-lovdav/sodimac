import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  supplierBlockService,
  supplierService,
  SupplierBlock,
  Supplier,
  SupplierBlockCreateDto,
  SupplierBlockUpdateDto,
} from '@features/catalogos/services/catalogosApi';

const SupplierBlockForm = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = Boolean(id);

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [supplierInfo, setSupplierInfo] = useState<Supplier | null>(null);
  const [supplierSearching, setSupplierSearching] = useState(false);

  const [formData, setFormData] = useState({
    supplierNumber: '',
    validFrom: '',
    validTo: '',
    blockReason: '',
    status: '1',
  });

  useEffect(() => {
    if (isEditing && id) {
      loadBlock(parseInt(id));
    }
  }, [id, isEditing]);

  const loadBlock = async (blockId: number) => {
    setLoading(true);
    try {
      const block = await supplierBlockService.getById(blockId);
      setFormData({
        supplierNumber: block.supplierNumber,
        validFrom: block.validFrom,
        validTo: block.validTo,
        blockReason: block.blockReason || '',
        status: block.status.toString(),
      });
      await searchSupplier(block.supplierNumber);
    } catch (err) {
      setError('Error al cargar el bloqueo');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const searchSupplier = async (supplierNumber: string) => {
    if (!supplierNumber) {
      setSupplierInfo(null);
      return;
    }
    setSupplierSearching(true);
    try {
      const supplier = await supplierService.getByNumber(supplierNumber);
      setSupplierInfo(supplier);
    } catch (err) {
      setSupplierInfo(null);
    } finally {
      setSupplierSearching(false);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSupplierBlur = () => {
    if (formData.supplierNumber && !isEditing) {
      searchSupplier(formData.supplierNumber);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError(null);

    try {
      if (isEditing && id) {
        const updateData: SupplierBlockUpdateDto = {
          validFrom: formData.validFrom,
          validTo: formData.validTo,
          blockReason: formData.blockReason || undefined,
          status: parseInt(formData.status),
        };
        await supplierBlockService.update(parseInt(id), updateData);
      } else {
        const createData: SupplierBlockCreateDto = {
          supplierNumber: formData.supplierNumber,
          validFrom: formData.validFrom,
          validTo: formData.validTo,
          blockReason: formData.blockReason || undefined,
        };
        await supplierBlockService.create(createData);
      }
      navigate('/util/catalogos/bloqueos');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Error al guardar el bloqueo');
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="somx-container">
        <div className="somx-loading">
          <div className="somx-loading-spinner"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="somx-container">
      <div className="somx-breadcrumb">
        <a href="#/catalogos">Catálogos</a>
        <span>/</span>
        <a href="#/catalogos/bloqueos">Bloqueos</a>
        <span>/</span>
        <span>{isEditing ? 'Editar' : 'Crear'}</span>
      </div>

      <div className="somx-page-header">
        <h1 className="somx-page-title">
          {isEditing ? 'Editar Bloqueo' : 'Nuevo Bloqueo de Proveedor'}
        </h1>
      </div>

      {error && <div className="somx-error">{error}</div>}

      <div className="somx-card">
        <form onSubmit={handleSubmit}>
          <div className="somx-grid somx-grid-cols-2 somx-gap-md">
            <div className="somx-form-group">
              <label className="somx-form-label">Número de Proveedor *</label>
              <input
                type="text"
                name="supplierNumber"
                className="somx-form-input"
                value={formData.supplierNumber}
                onChange={handleChange}
                onBlur={handleSupplierBlur}
                required
                disabled={isEditing}
                placeholder="Ej: PROV001"
              />
              {supplierSearching && (
                <span className="somx-text-sm somx-text-gray">Buscando...</span>
              )}
            </div>

            <div className="somx-form-group">
              <label className="somx-form-label">Proveedor</label>
              <input
                type="text"
                className="somx-form-input"
                value={supplierInfo ? supplierInfo.businessName : 'No encontrado'}
                disabled
                style={{
                  background: supplierInfo ? '#dcfce7' : '#fee2e2',
                  color: supplierInfo ? '#166534' : '#991b1b',
                }}
              />
            </div>

            {supplierInfo && (
              <>
                <div className="somx-form-group">
                  <label className="somx-form-label">RFC</label>
                  <input
                    type="text"
                    className="somx-form-input"
                    value={supplierInfo.rfc}
                    disabled
                  />
                </div>
                <div className="somx-form-group">
                  <label className="somx-form-label">Tipo de Proveedor</label>
                  <input
                    type="text"
                    className="somx-form-input"
                    value={supplierInfo.supplierType?.description || '-'}
                    disabled
                  />
                </div>
              </>
            )}

            <div className="somx-form-group">
              <label className="somx-form-label">Fecha Inicio Vigencia *</label>
              <input
                type="date"
                name="validFrom"
                className="somx-form-input"
                value={formData.validFrom}
                onChange={handleChange}
                required
              />
            </div>

            <div className="somx-form-group">
              <label className="somx-form-label">Fecha Fin Vigencia *</label>
              <input
                type="date"
                name="validTo"
                className="somx-form-input"
                value={formData.validTo}
                onChange={handleChange}
                required
              />
            </div>

            <div className="somx-form-group" style={{ gridColumn: 'span 2' }}>
              <label className="somx-form-label">Razón del Bloqueo</label>
              <textarea
                name="blockReason"
                className="somx-form-input"
                value={formData.blockReason}
                onChange={handleChange}
                rows={3}
                placeholder="Describa el motivo del bloqueo..."
                style={{ resize: 'vertical' }}
              />
            </div>

            {isEditing && (
              <div className="somx-form-group">
                <label className="somx-form-label">Estatus</label>
                <select
                  name="status"
                  className="somx-form-select"
                  value={formData.status}
                  onChange={handleChange}
                >
                  <option value="1">Activo</option>
                  <option value="0">Inactivo</option>
                </select>
              </div>
            )}
          </div>

          <div className="somx-flex somx-gap-md somx-mt-md">
            <button
              type="button"
              className="somx-btn somx-btn-secondary"
              onClick={() => navigate('/util/catalogos/bloqueos')}
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="somx-btn somx-btn-primary"
              disabled={saving || !supplierInfo}
            >
              {saving ? 'Guardando...' : isEditing ? 'Actualizar' : 'Crear Bloqueo'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default SupplierBlockForm;








