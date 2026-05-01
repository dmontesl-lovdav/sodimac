import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  supplierService,
  Supplier,
  SupplierType,
  PaymentCondition,
  SupplierCreateDto,
  SupplierUpdateDto,
} from '@features/catalogos/services/catalogosApi';

const SupplierForm = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = Boolean(id);

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [supplierTypes, setSupplierTypes] = useState<SupplierType[]>([]);
  const [paymentConditions, setPaymentConditions] = useState<PaymentCondition[]>([]);

  const [formData, setFormData] = useState({
    supplierNumber: '',
    rfc: '',
    businessName: '',
    supplierTypeId: '',
    paymentConditionId: '',
    logo: '',
    status: '1',
  });

  useEffect(() => {
    loadCatalogs();
    if (isEditing && id) {
      loadSupplier(parseInt(id));
    }
  }, [id, isEditing]);

  const loadCatalogs = async () => {
    try {
      const [types, conditions] = await Promise.all([
        supplierService.getTypes(),
        supplierService.getPaymentConditions(),
      ]);
      setSupplierTypes(types);
      setPaymentConditions(conditions);
    } catch (err) {
      console.error('Error loading catalogs:', err);
    }
  };

  const loadSupplier = async (supplierId: number) => {
    setLoading(true);
    try {
      const supplier = await supplierService.getById(supplierId);
      setFormData({
        supplierNumber: supplier.supplierNumber,
        rfc: supplier.rfc,
        businessName: supplier.businessName,
        supplierTypeId: supplier.supplierType?.id?.toString() || '',
        paymentConditionId: supplier.paymentCondition?.id?.toString() || '',
        logo: supplier.logo || '',
        status: supplier.status.toString(),
      });
    } catch (err) {
      setError('Error al cargar el proveedor');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError(null);

    try {
      if (isEditing && id) {
        const updateData: SupplierUpdateDto = {
          rfc: formData.rfc,
          businessName: formData.businessName,
          supplierTypeId: formData.supplierTypeId ? parseInt(formData.supplierTypeId) : undefined,
          paymentConditionId: formData.paymentConditionId
            ? parseInt(formData.paymentConditionId)
            : undefined,
          logo: formData.logo || undefined,
          status: parseInt(formData.status),
        };
        await supplierService.update(parseInt(id), updateData);
      } else {
        const createData: SupplierCreateDto = {
          supplierNumber: formData.supplierNumber,
          rfc: formData.rfc,
          businessName: formData.businessName,
          supplierTypeId: formData.supplierTypeId ? parseInt(formData.supplierTypeId) : undefined,
          paymentConditionId: formData.paymentConditionId
            ? parseInt(formData.paymentConditionId)
            : undefined,
          logo: formData.logo || undefined,
        };
        await supplierService.create(createData);
      }
      navigate('/util/catalogos/proveedores');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Error al guardar el proveedor');
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
        <a href="#/catalogos/proveedores">Proveedores</a>
        <span>/</span>
        <span>{isEditing ? 'Editar' : 'Crear'}</span>
      </div>

      <div className="somx-page-header">
        <h1 className="somx-page-title">
          {isEditing ? 'Editar Proveedor' : 'Nuevo Proveedor'}
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
                required
                disabled={isEditing}
                placeholder="Ej: PROV001"
              />
            </div>

            <div className="somx-form-group">
              <label className="somx-form-label">RFC *</label>
              <input
                type="text"
                name="rfc"
                className="somx-form-input"
                value={formData.rfc}
                onChange={handleChange}
                required
                maxLength={13}
                placeholder="Ej: ABC123456789"
              />
            </div>

            <div className="somx-form-group" style={{ gridColumn: 'span 2' }}>
              <label className="somx-form-label">Razón Social *</label>
              <input
                type="text"
                name="businessName"
                className="somx-form-input"
                value={formData.businessName}
                onChange={handleChange}
                required
                placeholder="Nombre completo del proveedor"
              />
            </div>

            <div className="somx-form-group">
              <label className="somx-form-label">Tipo de Proveedor</label>
              <select
                name="supplierTypeId"
                className="somx-form-select"
                value={formData.supplierTypeId}
                onChange={handleChange}
              >
                <option value="">Seleccionar...</option>
                {supplierTypes.map((type) => (
                  <option key={type.id} value={type.id}>
                    {type.description}
                  </option>
                ))}
              </select>
            </div>

            <div className="somx-form-group">
              <label className="somx-form-label">Condición de Pago</label>
              <select
                name="paymentConditionId"
                className="somx-form-select"
                value={formData.paymentConditionId}
                onChange={handleChange}
              >
                <option value="">Seleccionar...</option>
                {paymentConditions.map((condition) => (
                  <option key={condition.id} value={condition.id}>
                    {condition.conditionName} ({condition.days} días)
                  </option>
                ))}
              </select>
            </div>

            <div className="somx-form-group">
              <label className="somx-form-label">Logo (URL)</label>
              <input
                type="text"
                name="logo"
                className="somx-form-input"
                value={formData.logo}
                onChange={handleChange}
                placeholder="https://..."
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
              onClick={() => navigate('/util/catalogos/proveedores')}
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="somx-btn somx-btn-primary"
              disabled={saving}
            >
              {saving ? 'Guardando...' : isEditing ? 'Actualizar' : 'Crear'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default SupplierForm;








