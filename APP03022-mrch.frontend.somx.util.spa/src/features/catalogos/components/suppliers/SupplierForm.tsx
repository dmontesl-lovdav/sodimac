import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  supplierService,
  SupplierType,
  PaymentCondition,
  SupplierCreateDto,
  SupplierUpdateDto,
} from '@features/catalogos/services/catalogosApi';
import { useModalNotification } from '@shared/components/ui/modal';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import { withFinanceBreadcrumb } from '@shared/components/ui/navigation/financeBreadcrumb';

const styles = {
  container: {
    padding: '1.5rem 2rem',
    backgroundColor: '#ffffff',
    minHeight: '100vh',
    fontFamily: 'inherit',
    color: '#1f2937',
  } as const,
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
  pageHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '1.25rem',
    flexWrap: 'wrap' as const,
    gap: '1rem',
  },
  pageTitle: {
    fontSize: '1.5rem',
    fontWeight: 600,
    color: '#1f2937',
    margin: 0,
  },
  card: {
    backgroundColor: '#ffffff',
    border: '1px solid #e5e7eb',
    borderRadius: '0.5rem',
    padding: '1.5rem',
    boxShadow: '0 1px 2px rgba(0,0,0,0.04)',
  },
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    gap: '1rem',
  } as const,
  formGroup: {
    display: 'flex',
    flexDirection: 'column' as const,
    gap: '0.4rem',
  },
  formLabel: {
    fontSize: '0.8125rem',
    fontWeight: 500,
    color: '#374151',
  },
  formInput: {
    padding: '0.5rem 0.75rem',
    border: '1px solid #d1d5db',
    borderRadius: '0.375rem',
    fontSize: '0.875rem',
    color: '#1f2937',
    backgroundColor: '#ffffff',
    outline: 'none',
    fontFamily: 'inherit',
  },
  actions: {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: '0.75rem',
    marginTop: '1.5rem',
    paddingTop: '1rem',
    borderTop: '1px solid #e5e7eb',
  },
  btnPrimary: {
    padding: '0.55rem 1.5rem',
    backgroundColor: '#002d4c',
    color: '#ffffff',
    border: '1px solid #002d4c',
    borderRadius: '0.375rem',
    fontSize: '0.875rem',
    fontWeight: 500,
    cursor: 'pointer',
  },
  btnSecondary: {
    padding: '0.55rem 1.5rem',
    backgroundColor: '#ffffff',
    color: '#1f2937',
    border: '1px solid #d1d5db',
    borderRadius: '0.375rem',
    fontSize: '0.875rem',
    fontWeight: 500,
    cursor: 'pointer',
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
  loading: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    padding: '3rem 1rem',
  },
  spinner: {
    width: '2.25rem',
    height: '2.25rem',
    border: '3px solid #e5e7eb',
    borderTopColor: '#002d4c',
    borderRadius: '50%',
    animation: 'somx-spin 0.8s linear infinite',
  },
};

const SupplierForm = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditing = Boolean(id);
  const { showSuccess, showError, ModalNode } = useModalNotification();

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
    emailPrincipal: '',
    emailFinancial: '',
    emailCommercial: '',
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
        emailPrincipal: supplier.emailPrincipal ?? '',
        emailFinancial: supplier.emailFinancial ?? '',
        emailCommercial: supplier.emailCommercial ?? '',
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
    if (name === 'supplierNumber') {
      const onlyDigits = value.replace(/\D+/g, '');
      setFormData({ ...formData, [name]: onlyDigits });
      return;
    }
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError(null);

    try {
      const emailPrincipal = formData.emailPrincipal.trim();
      const emailFinancial = formData.emailFinancial.trim();
      const emailCommercial = formData.emailCommercial.trim();

      if (isEditing && id) {
        const updateData: SupplierUpdateDto = {
          rfc: formData.rfc,
          businessName: formData.businessName,
          supplierTypeId: formData.supplierTypeId
            ? parseInt(formData.supplierTypeId)
            : undefined,
          paymentConditionId: formData.paymentConditionId
            ? parseInt(formData.paymentConditionId)
            : undefined,
          logo: formData.logo || undefined,
          emailPrincipal: emailPrincipal || undefined,
          emailFinancial: emailFinancial || undefined,
          emailCommercial: emailCommercial || undefined,
          status: parseInt(formData.status),
        };
        await supplierService.update(parseInt(id), updateData);
        showSuccess('Proveedor actualizado correctamente.', 'Actualización exitosa', () =>
          navigate('/util/catalogos/proveedores'),
        );
      } else {
        const createData: SupplierCreateDto = {
          supplierNumber: formData.supplierNumber,
          rfc: formData.rfc,
          businessName: formData.businessName,
          supplierTypeId: formData.supplierTypeId
            ? parseInt(formData.supplierTypeId)
            : undefined,
          paymentConditionId: formData.paymentConditionId
            ? parseInt(formData.paymentConditionId)
            : undefined,
          logo: formData.logo || undefined,
          emailPrincipal,
          emailFinancial,
          emailCommercial: emailCommercial || undefined,
        };
        await supplierService.create(createData);
        showSuccess('Proveedor creado correctamente.', 'Creación exitosa', () =>
          navigate('/util/catalogos/proveedores'),
        );
      }
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Error al guardar el proveedor';
      setError(msg);
      showError(msg);
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div style={styles.container}>
        <div style={styles.loading}>
          <div style={styles.spinner}></div>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <Breadcrumb
        items={withFinanceBreadcrumb([
          { label: 'Gestión de Catálogos', to: '/util/catalogos' },
          { label: 'Proveedores', to: '/util/catalogos/proveedores' },
          { label: isEditing ? 'Editar' : 'Crear' },
        ])}
      />

      <div style={styles.pageHeader}>
        <h1 style={styles.pageTitle}>{isEditing ? 'Editar Proveedor' : 'Nuevo Proveedor'}</h1>
      </div>

      {error && <div style={styles.error}>{error}</div>}

      <div style={styles.card}>
        <form onSubmit={handleSubmit}>
          <div style={styles.grid}>
            <div style={styles.formGroup}>
              <label style={styles.formLabel}>Número de Proveedor *</label>
              <input
                type="text"
                inputMode="numeric"
                pattern="[0-9]*"
                name="supplierNumber"
                style={styles.formInput}
                value={formData.supplierNumber}
                onChange={handleChange}
                required
                disabled={isEditing}
                placeholder="Ej: 100123"
              />
            </div>

            <div style={styles.formGroup}>
              <label style={styles.formLabel}>RFC *</label>
              <input
                type="text"
                name="rfc"
                style={styles.formInput}
                value={formData.rfc}
                onChange={handleChange}
                required
                maxLength={13}
                placeholder="Ej: ABC123456789"
              />
            </div>

            <div style={{ ...styles.formGroup, gridColumn: 'span 2' }}>
              <label style={styles.formLabel}>Razón Social *</label>
              <input
                type="text"
                name="businessName"
                style={styles.formInput}
                value={formData.businessName}
                onChange={handleChange}
                required
                placeholder="Nombre completo del proveedor"
              />
            </div>

            <div style={styles.formGroup}>
              <label style={styles.formLabel}>Tipo de Proveedor</label>
              <select
                name="supplierTypeId"
                style={styles.formInput}
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

            <div style={styles.formGroup}>
              <label style={styles.formLabel}>Condición de Pago</label>
              <select
                name="paymentConditionId"
                style={styles.formInput}
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

            <div style={styles.formGroup}>
              <label style={styles.formLabel}>Logo (URL)</label>
              <input
                type="text"
                name="logo"
                style={styles.formInput}
                value={formData.logo}
                onChange={handleChange}
                placeholder="https://..."
              />
            </div>

            <div style={styles.formGroup}>
              <label style={styles.formLabel}>
                Email Principal <span style={{ color: '#dc2626' }}>*</span>
              </label>
              <input
                type="email"
                name="emailPrincipal"
                style={styles.formInput}
                value={formData.emailPrincipal}
                onChange={handleChange}
                maxLength={255}
                required
                placeholder="principal@proveedor.com"
              />
            </div>

            <div style={styles.formGroup}>
              <label style={styles.formLabel}>
                Email CXC <span style={{ color: '#dc2626' }}>*</span>
              </label>
              <input
                type="email"
                name="emailFinancial"
                style={styles.formInput}
                value={formData.emailFinancial}
                onChange={handleChange}
                maxLength={255}
                required
                placeholder="cxc@proveedor.com"
              />
            </div>

            <div style={styles.formGroup}>
              <label style={styles.formLabel}>Email Comercial</label>
              <input
                type="email"
                name="emailCommercial"
                style={styles.formInput}
                value={formData.emailCommercial}
                onChange={handleChange}
                maxLength={255}
                placeholder="comercial@proveedor.com"
              />
            </div>

            {isEditing && (
              <div style={{ ...styles.formGroup, gridColumn: 'span 2' }}>
                <label style={styles.formLabel}>Estatus</label>
                <select
                  name="status"
                  style={styles.formInput}
                  value={formData.status}
                  onChange={handleChange}
                >
                  <option value="1">Activo</option>
                  <option value="0">Inactivo</option>
                </select>
              </div>
            )}
          </div>

          <div style={styles.actions}>
            <button
              type="button"
              style={styles.btnSecondary}
              onClick={() => navigate('/util/catalogos/proveedores')}
            >
              Cancelar
            </button>
            <button
              type="submit"
              style={{ ...styles.btnPrimary, opacity: saving ? 0.65 : 1, cursor: saving ? 'not-allowed' : 'pointer' }}
              disabled={saving}
            >
              {saving ? 'Guardando...' : isEditing ? 'Actualizar' : 'Crear'}
            </button>
          </div>
        </form>
      </div>
      {ModalNode}
    </div>
  );
};

export default SupplierForm;
